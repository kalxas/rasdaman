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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
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
import static petascope.ConfigManager.XML_VALIDATION_T;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.TimeUtil;
import static petascope.util.XMLSymbols.*;
import petascope.util.XMLUtil;
import static petascope.util.XMLUtil.*;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.RangeSubsettingExtension;
import petascope.wcs2.handlers.RequestHandler;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSlice;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionTrim;

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
        if(ConfigManager.XML_VALIDATION.equals(XML_VALIDATION_T)){
            try {
                log.info("Loading XML schema definition from " + WCS2_GETCOV_SCHEMA + "...");
                schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = schemaFactory.newSchema(new URL(WCS2_GETCOV_SCHEMA));
                log.info("Done.");
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
        if(ConfigManager.XML_VALIDATION.equals(XML_VALIDATION_T)){
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
                    ret.getSubsets().add(new DimensionTrim(getText(c.get(0)), getText(c.get(1)), getText(c.get(2))));
                } else if (name.equals(LABEL_DIMENSION_SLICE)) {
                    ret.getSubsets().add(new DimensionSlice(getText(c.get(0)), getText(c.get(1))));
                    if (getText(c.get(0)).equals(AxisTypes.T_AXIS)) {
                        // Check timestamps validity
                        if (getText(c.get(1)) != null
                                && !CrsUtil.GRID_CRS.equals(getText(c.get(1)))     // No timestamp validity needed hence.
                                && !TimeUtil.isValidTimestamp(getText(c.get(1)))) {
                            throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getText(c.get(1)) + "\" is not valid (pattern is YYYY-MM-DD).");
                        }
                    }
                } else if (name.equals(LABEL_CRS)) {
                    String subCrs = null, outCrs = null;
                    for (Element attr : c) {
                        if (attr.getLocalName().equals(ATT_SUBSET_CRS)) {
                            if (subCrs == null) subCrs = getText(attr);
                            else throw new WCSException(ExceptionCode.InvalidRequest, "Multiple \"" + LABEL_SUBSETTING_CRS + "\" parameters in the request: must be unique.");
                            // check validity of CrsExt specification
                            if (!CrsUtil.CrsUri.isValid(subCrs))
                                throw new WCSException(ExceptionCode.NotASubsettingCrs,
                                        LABEL_SUBSETTING_CRS + "\"" + subCrs + "\" is not valid.");
                            if (!CrsUtil.isSupportedCrsCode(subCrs))
                                throw new WCSException(ExceptionCode.SubsettingCrsNotSupported,
                                        LABEL_SUBSETTING_CRS + "\"" + subCrs + "\" is not supported.");
                        }
                        else if (attr.getLocalName().equals(ATT_OUTPUT_CRS)) {
                            if (outCrs == null) outCrs = getText(attr);
                            else throw new WCSException(ExceptionCode.InvalidRequest, "Multiple \"" + LABEL_OUTPUT_CRS + "\" parameters in the request: must be unique.");
                            // check validity of CrsExt specification
                            if (!CrsUtil.CrsUri.isValid(outCrs))
                                throw new WCSException(ExceptionCode.NotAnOutputCrs,
                                        LABEL_OUTPUT_CRS + " \"" + outCrs + "\" is not valid.");
                            if (!CrsUtil.isSupportedCrsCode(outCrs))
                                throw new WCSException(ExceptionCode.SubsettingCrsNotSupported,
                                        LABEL_OUTPUT_CRS + " \"" + outCrs + "\" is not supported.");
                        }
                    }
                    ret.getCrsExt().setSubsettingCrs(subCrs);
                    ret.getCrsExt().setOutputCrs(outCrs);

                } else if (name.equals(LABEL_SCALING)) {
                    log.trace("here");
                    for (Element elem : c) {
                        String elname = elem.getLocalName();
                        List<Element> cE = ch(elem);
                        if (elname.equals(LABEL_SCALEBYFACTOR)) {
                            if (cE != null && (cE.size() != 1 || ret.isScaled())) {
                                throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
                            } else if (cE != null) {
                                log.trace("here2");
                                float scaleFactor;
                                Element el = cE.get(0);
                                String value = getText(el);
                                try {
                                    scaleFactor = Float.parseFloat(value);
                                } catch (NumberFormatException ex) {
                                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(value));
                                }
                                if (scaleFactor <= 0) {
                                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(value));
                                }
                                ret.getScaling().setFactor(scaleFactor);
                                ret.getScaling().setType(1);
                            }
                        } else if (elname.equals(LABEL_SCALEAXESBYFACTOR)) {
                            if (cE != null && ret.isScaled()) {
                                throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
                            } else if (cE != null) {
                                for (Element el : cE) {
                                    List<Element> chi = ch(el);
                                    String axis = "", fact = "";
                                    for (Element ele : chi) {
                                        String ename = ele.getLocalName();
                                        if (ename.equals(LABEL_CRSAXIS)) {
                                            axis = getText(ele);
                                        } else if (ename.equals(LABEL_SCALEFACTOR)) {
                                            fact = getText(ele);
                                        }
                                    }
                                    if (ret.getScaling().isPresentFactor(axis)) {
                                        throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                                    }
                                    float scaleFactor;
                                    try {
                                        scaleFactor = Float.parseFloat(fact);
                                    } catch (NumberFormatException ex) {
                                        throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(fact));
                                    }
                                    if (scaleFactor <= 0) {
                                        throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(fact));
                                    }
                                    ret.getScaling().addFactor(axis, scaleFactor);
                                }
                                ret.getScaling().setType(2);
                            }
                        } else if (elname.equals(LABEL_SCALETOSIZE)) {
                            if (cE != null && ret.isScaled()) {
                                throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
                            } else if (cE != null) {
                                for (Element el : cE) {
                                    List<Element> chi = ch(el);
                                    String axis = "", fact = "";
                                    for (Element ele : chi) {
                                        String ename = ele.getLocalName();
                                        if (ename.equals(LABEL_CRSAXIS)) {
                                            axis = getText(ele);
                                        } else if (ename.equals(LABEL_TARGETSIZE)) {
                                            fact = getText(ele);
                                        }
                                    }
                                    if (ret.getScaling().isPresentFactor(axis)) {
                                        throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                                    }
                                    int scaleSize;
                                    try {
                                        scaleSize = Integer.parseInt(fact);
                                    } catch (NumberFormatException ex) {
                                        throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(fact));
                                    }
                                    if (scaleSize < 0) {
                                        throw new WCSException(ExceptionCode.InvalidRequest, "Scaling size is not positive.");
                                    }

                                    ret.getScaling().addSize(axis, scaleSize);
                                }
                                ret.getScaling().setType(3);
                            }
                        } else if (elname.equals(LABEL_SCALETOEXTENT)) {
                            if (cE != null && ret.isScaled()) {
                                throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
                            } else if (cE != null) {
                                for (Element el : cE) {
                                    List<Element> chi = ch(el);
                                    String axis = "", slo = "", shi = "";
                                    for (Element ele : chi) {
                                        String ename = ele.getLocalName();
                                        if (ename.equals(LABEL_CRSAXIS)) {
                                            axis = getText(ele);
                                        } else if (ename.equals(LABEL_LOW)) {
                                            slo = getText(ele);
                                        } else if (ename.equals(LABEL_HIGH)) {
                                            shi = getText(ele);
                                        }
                                    }
                                    if (ret.getScaling().isPresentFactor(axis)) {
                                        throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                                    }
                                    int lo;
                                    try {
                                        lo = Integer.parseInt(slo);
                                    } catch (NumberFormatException ex) {
                                        throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(slo));
                                    }
                                    int hi;
                                    try {
                                        hi = Integer.parseInt(shi);
                                    } catch (NumberFormatException ex) {
                                        throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(shi));
                                    }
                                    if (ret.getScaling().isPresentExtent(axis)) {
                                        throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                                    }
                                    if (hi < lo) {
                                        throw new WCSException(ExceptionCode.InvalidExtent.locator(shi));
                                    }

                                    ret.getScaling().addExtent(axis, new Pair(lo, hi));
                                }
                                ret.getScaling().setType(4);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                if (((PetascopeException)ex).getExceptionCode().getExceptionCode().equalsIgnoreCase(ExceptionCode.NotASubsettingCrs.getExceptionCode())
                    || ((PetascopeException)ex).getExceptionCode().getExceptionCode().equalsIgnoreCase(ExceptionCode.NotAnOutputCrs.getExceptionCode()))
                    throw (WCSException)ex;
                else
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
            if (currentElem.getLocalName().equalsIgnoreCase(LABEL_RANGESUBSET)) {
                RangeSubsettingExtension.parseGetCoverageXMLRequest(gcRequest, currentElem);
            }
        }
    }
}
