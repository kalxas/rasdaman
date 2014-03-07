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
package petascope.wcs2.extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import nu.xom.Element;
import nu.xom.Elements;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.XMLSymbols;
import static petascope.util.XMLSymbols.LABEL_RANGEITEM;
import static petascope.util.XMLSymbols.LABEL_RANGESUBSET;
import static petascope.util.XMLUtil.ch;
import petascope.wcs2.helpers.rangesubsetting.RangeComponent;
import petascope.wcs2.helpers.rangesubsetting.RangeInterval;
import petascope.wcs2.helpers.rangesubsetting.RangeSubset;
import petascope.wcs2.helpers.rest.RESTUrl;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import petascope.wcs2.parsers.RESTParser;

/**
 * This class manages the Subsetting Extension in accordance to the OGC-12-040
 * standard The extension allows extraction of specific fields, according to the
 * range type specification, from the range set of a coverage during server-side
 * processing of a coverage in a GetCoverage request.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public class RangeSubsettingExtension implements Extension {

    /**
     * Implementation of the Extension requirements
     *
     * @return the identifier for this extension
     */
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.RANGE_SUBSETTING_IDENTIFIER;
    }

    /**
     * Handles the request adding the needed components for it to be properly
     * parsed
     *
     * @param request the request object
     * @param coverageMeta metadata of the coverage
     * @throws WCSException
     */
    public void handle(GetCoverageRequest request, GetCoverageMetadata coverageMeta) throws WCSException {
        request.getRangeSubset().setCoverageComponents(coverageMeta.getRangeFields());
    }

    /**
     * Processes a wcps query to add the range subsetting parameters. It does
     * this by replacing the coverage identifier, i.e. c with a listing of the
     * ranges that were requested by the client i.e. { red: red; green:green;
     * blue:blue}
     *
     * @param coverage the coverage identifier
     * @param rs the range subset parameter of the request
     * @return a new identifier that can be used instead of the old one
     */
    public static String processWCPSRequest(String coverage, RangeSubset rs) throws WCSException {
        StringBuilder ret = new StringBuilder("{");
        ArrayList<String> ranges = rs.getSelectedComponents();
        for (String range : ranges) {
            ret.append(range).append(":").append(coverage).append(".").append(range).append("; ");
        }
        //remove the last "; "
        ret.delete(ret.length() - 2, ret.length() - 1);
        ret.append("}");
        return ret.toString();
    }

    /**
     * Recognizes an XML root element of a RangeSubsetting extension.
     * @param elementName
     * @return True if this is the root of an XML RangeSubsetting extension.
     */
      public static boolean isXMLRangeSubsettingExtension(String elementName) {
        return elementName.equalsIgnoreCase(LABEL_RANGESUBSET);
    }

    /**
     * Helper method to parse a RangeItem XML element and adds the parsed
     * information to the GetCoverage Request
     *
     * @param gcRequest - the request to add the parsed info
     * @param rangeElem - the XML element to be parsed
     * @throws WCSException
     */
    public static void parseGetCoverageXMLRequest(GetCoverageRequest gcRequest, Element rangeElem) throws WCSException {

        // check
        if (!isXMLRangeSubsettingExtension(rangeElem.getLocalName())) {
            throw new WCSException(ExceptionCode.InternalComponentError,
                    "The parser was expecting a" + XMLSymbols.LABEL_RANGESUBSET + " element, " + rangeElem.getLocalName() + " given");
        }

        // loop through the listed rangeItems
        for (Element currentElem : ch(rangeElem)) {
            if (currentElem.getLocalName().equalsIgnoreCase(LABEL_RANGEITEM)) {

                Elements children = currentElem.getChildElements();
                for (int i = 0; i < children.size(); i++) {
                    Element currentChild = children.get(i);

                    if (currentChild.getLocalName().equalsIgnoreCase(XMLSymbols.LABEL_RANGEINTERVAL)) {
                        Elements components = currentChild.getChildElements();
                        if (components.size() != 2) {
                            throw new WCSException(ExceptionCode.InternalComponentError,
                                    "A RangeInterval element needs to have exactly one startComponent and one endComponent");
                        }
                        String startComponent = null, endComponent = null;
                        for (int j = 0; j < components.size(); j++) {
                            if (components.get(j).getLocalName().equalsIgnoreCase(XMLSymbols.LABEL_STARTCOMPONENT)) {
                                startComponent = components.get(j).getValue().trim();
                            } else if (components.get(j).getLocalName().equalsIgnoreCase(XMLSymbols.LABEL_ENDCOMPONENT)) {
                                endComponent = components.get(j).getValue().trim();
                            }
                        }
                        if (startComponent == null || endComponent == null) {
                            throw new WCSException(ExceptionCode.InternalComponentError,
                                    "A RangeInterval element needs to have exactly one startComponent and one endComponent");
                        }
                        gcRequest.getRangeSubset().addRangeItem(new RangeInterval(startComponent, endComponent));

                    } else if (currentChild.getLocalName().equalsIgnoreCase(XMLSymbols.LABEL_RANGECOMPONENT)) {
                        gcRequest.getRangeSubset().addRangeItem(new RangeComponent(currentChild.getValue().trim()));
                    }
                }
            }
        }
    }

    /**
     * Helper method to parse the rangesubsets parameters for this request It
     * recognizes the following formats: band | band1:bandN | band1, band2,
     * bandX:bandY
     *
     * @param params - the GET parameters of this request
     * @param request - the coverage request to which to add the parsed
     * information
     * @throws WCSException
     */
    public static void parseGetCoverageKVPRequest(Map<String, List<String>> params, GetCoverageRequest request) throws WCSException {
        if (params.containsKey(RANGESUBSET_KVP_PARAMNAME)) {
            List<String> rangeParams = params.get(RANGESUBSET_KVP_PARAMNAME);
            RangeSubsettingExtension.parseGetCoverageRequest(rangeParams, RANGESUBSET_KVP_RANGE_SEPARATOR, request);
        }
    }

    public static void parseGetCoverageRESTRequest(RESTUrl rUrl, GetCoverageRequest request) throws WCSException {
        if (rUrl.existsKey(RANGESUBSET_REST_PARAMNAME) && !rUrl.getByKey(RANGESUBSET_REST_PARAMNAME).isEmpty()) {
            List<String> rangeParams = new ArrayList<String>(Arrays.asList(
                    rUrl.getByKey(RANGESUBSET_REST_PARAMNAME).get(0).split(RESTParser.ENUMERATOR_SEPARATOR)));
            RangeSubsettingExtension.parseGetCoverageRequest(rangeParams,RANGESUBSET_REST_RANGE_SEPARATOR, request);
        }
    }

    public static void parseGetCoverageRequest(List<String> rangeParams, String rangeSep, GetCoverageRequest request) throws WCSException {
        for (int i = 0; i < rangeParams.size(); i++) {
            if (rangeParams.get(i).contains(rangeSep)) {
                String[] rangeComp = rangeParams.get(i).split(rangeSep);
                request.getRangeSubset().addRangeItem(new RangeInterval(rangeComp[0], rangeComp[1]));
            } else {
                request.getRangeSubset().addRangeItem(new RangeComponent(rangeParams.get(i)));
            }
        }
    }
    public static final String RANGESUBSET_KVP_PARAMNAME = "rangesubset";
    public static final String RANGESUBSET_KVP_RANGE_SEPARATOR = ":";

    public static final String RANGESUBSET_REST_PARAMNAME = "rangesubset";
    public static final String RANGESUBSET_REST_RANGE_SEPARATOR = RESTParser.RANGE_SEPARATOR;
}
