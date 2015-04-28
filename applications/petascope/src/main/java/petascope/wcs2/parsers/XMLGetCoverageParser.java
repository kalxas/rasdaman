/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.parsers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import nu.xom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import petascope.ConfigManager;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import static petascope.util.XMLSymbols.*;
import petascope.util.XMLUtil;
import static petascope.util.XMLUtil.*;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.InterpolationExtension;
import petascope.wcs2.extensions.RangeSubsettingExtension;
import petascope.wcs2.extensions.ScalingExtension;
import petascope.wcs2.handlers.RequestHandler;
import petascope.wcs2.parsers.subsets.DimensionSlice;
import petascope.wcs2.parsers.subsets.DimensionTrim;
import static petascope.wcs2.parsers.subsets.DimensionSubset.QUOTED_SUBSET;

/**
 * Parse a GetCapabilities XML request.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class XMLGetCoverageParser extends XMLParser<GetCoverageRequest> {

    private static final Logger log = LoggerFactory.getLogger(XMLGetCoverageParser.class);

    // constants
    public static final String LABEL_SUBSETTING_CRS = "subsettingcrs";
    public static final String LABEL_OUTPUT_CRS = "outputcrs";

    // XML validation
    private Schema schema;
    private SchemaFactory schemaFactory;
    private final String WCS2_GETCOV_SCHEMA = "http://schemas.opengis.net/wcs/2.0/wcsGetCoverage.xsd";

    // constructor
    public XMLGetCoverageParser(){
        if(ConfigManager.XML_VALIDATION){
            try {
                log.debug("Loading XML schema definition from " + WCS2_GETCOV_SCHEMA + "...");
                schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = schemaFactory.newSchema(new URL(WCS2_GETCOV_SCHEMA));
                log.debug("Done.");
            } catch(SAXException e) {
                log.error("Could not initialize the GetCoverage XML Schema validator. Schema validation will be disabled.",e);
            } catch(MalformedURLException e) {
                log.error("Could not initialize the GetCoverage XML Schema validator. Schema validation will be disabled.",e);
            }
        }
    }

    @Override
    public GetCoverageRequest parse(HTTPRequest request) throws WCSException {

        // input XML validation
        if(ConfigManager.XML_VALIDATION){
            validateInput(request.getRequestString(), schema);
        }

        // check how many coveradeIds (== 1)
        Element root = parseInput(request.getRequestString());
        List<Element> coverageIds = collectAll(root, PREFIX_WCS, LABEL_COVERAGE_ID, CTX_WCS);
        if (coverageIds.size() != 1) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "A GetCoverage request must specify one " + LABEL_COVERAGE_ID + ".");
        }

        // Get params required for contructor: format and mediatype
        Element formatEl    = XMLUtil.firstChildRecursive(root, LABEL_FORMAT);
        Element mediaTypeEl = XMLUtil.firstChildRecursive(root, LABEL_MEDIATYPE);
        String format    = null != formatEl    ?    formatEl.getValue() : "";
        String mediaType = null != mediaTypeEl ? mediaTypeEl.getValue() : "";

        // sanity check
        if (FormatExtension.MIME_MULTIPART.equals(mediaType)
                && FormatExtension.MIME_GML.equals(format)) {
            throw new WCSException(ExceptionCode.InvalidRequest, "The '" +
                    LABEL_MEDIATYPE + "=" + FormatExtension.MIME_MULTIPART + "' & '" +
                    LABEL_FORMAT    + "=" + FormatExtension.MIME_GML +
                    "' combination is not applicable");
        }

        // init GetCoverage request
        GetCoverageRequest ret = new GetCoverageRequest(
                getText(coverageIds.get(0)),
                format,
                FormatExtension.MIME_MULTIPART.equals(mediaType)
        );

        // parse
        List<Element> children = ch(root);
        for (Element e : children) {
            String name = e.getLocalName();
            List<Element> c = ch(e);
            try {
                if(name.equals(LABEL_EXTENSION)) {
                    this.parseExtensions(ret, c);
                }
                if (name.equals(LABEL_DIMENSION_TRIM)) {
                    ret.addSubset(new DimensionTrim(getText(c.get(0)), getText(c.get(1)), getText(c.get(2))));
                    // Check timestamps validity
                    if (null != getText(c.get(1)) && getText(c.get(1)).matches(QUOTED_SUBSET)) {
                        ((DimensionTrim)ret.getSubset(getText(c.get(0)))).timestampSubsetCheck();
                    }
                } else if (name.equals(LABEL_DIMENSION_SLICE)) {
                    ret.addSubset(new DimensionSlice(getText(c.get(0)), getText(c.get(1))));
                    // Check timestamps validity
                    if (null != getText(c.get(1)) && getText(c.get(1)).matches(QUOTED_SUBSET)) {
                        ((DimensionSlice)ret.getSubset(getText(c.get(0)))).timestampSubsetCheck();
                    }
                }
            } catch (WCSException ex) {
                throw new WCSException(ExceptionCode.InvalidRequest, "Error parsing dimension subset:\n\n" + e.toXML(), ex);
            }
        }
        return ret;
    }

    @Override
    public String getOperationName() {
        return RequestHandler.GET_COVERAGE;
    }

    /**
     * Handles XML elements with label Extension.
     * Each extension should add a parsing method inside.
     *
     * @param gcRequest the coverage to which to add the parsed information
     * @param extensionChildren the children of the extension element
     * @throws WCSException
     */
    private void parseExtensions(GetCoverageRequest gcRequest, List<Element> extensionChildren) throws WCSException{
        for (Element currentElem : extensionChildren) {
            //Parse RangeSubsetting elements
            if (RangeSubsettingExtension.isXMLRangeSubsettingExtension(currentElem.getLocalName())) {
                RangeSubsettingExtension.parseGetCoverageXMLRequest(gcRequest, currentElem);
            } else if (ScalingExtension.isXMLScalingExtension(currentElem.getLocalName())) {
                ScalingExtension.parseGetCoverageXMLRequest(gcRequest, currentElem);
            } else if (InterpolationExtension.isXMLInterpolationExtension(currentElem.getLocalName())) {
                InterpolationExtension.parseGetCoverageXMLRequest(gcRequest, currentElem);
            }
        }
    }
}
