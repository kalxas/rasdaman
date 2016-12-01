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
import java.util.List;
import java.util.Map;
import nu.xom.Element;
import nu.xom.Elements;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import static petascope.util.KVPSymbols.KEY_INTERPOLATION;
import petascope.util.XMLSymbols;
import static petascope.util.XMLSymbols.LABEL_INTERPOLATION;
import petascope.wcs2.parsers.GetCoverageRequest;

/**
 * Minimal implementation of WCS interpolation extension [OGC 12-049].
 * Upon scaling, rasdaman performs nearest-neighbor interpolation:
 * this is the currently unique kind of interpolation offered.
 *
 * @see "https://portal.opengeospatial.org/files/12-049"
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class InterpolationExtension implements Extension {

    /**
     * Reference URI for Nearest Neighbor interpolation.
     * Still this is not an existing OGC URI, but URL OGC policy recommends using URIs anyway.
     */
    public static final String NEAREST_NEIGHBOR_URI = "http://www.opengis.net/def/interpolation/OGC/0/nearest-neighbor";

    /**
     * List of supported URIs, required in the capabilities document.
     */
    private static final List<String> supportedInterpolationTypes;
    static {
        supportedInterpolationTypes = new ArrayList<String>(1);
        supportedInterpolationTypes.add(NEAREST_NEIGHBOR_URI);
    }

    public String getExtensionIdentifier() {
        return ExtensionsRegistry.INTERPOLATION_IDENTIFIER;
    }

    /**
     * @return False: this extension has is no parent extension with identifier.
     */
    public Boolean hasParent() {
        return false;
    }

    /**
     * @return The identifier of the parent extension.
     */
    public String getParentExtensionIdentifier() {
        return "";
    }

    /**
     * Get list of supported interpolation types.
     * Currently only nearest-neighbor.
     * @return List containing URI standard identifiers of interpolation supported by the server.
     */
    public List<String> getSupportedInterpolation() {
        return supportedInterpolationTypes;
    }

    /**
     * Prepare parsing for the more general GetCoverage parser.
     * @param params
     * @param request
     * @throws WCSException
     */
    public static void parseGetCoverageKVPRequest(Map<String, List<String>> params, GetCoverageRequest request) throws WCSException {
        parseGetCoverageRequest(params.get(KEY_INTERPOLATION), request);
    }

    /**
     * Check that an input GetCoverage request is compliant with service capabilities,
     * concerning the interpolation extension.
     * @param intParams
     * @param request
     * @throws WCSException
     */
    public static void parseGetCoverageRequest(List<String> intParams, GetCoverageRequest request) throws WCSException {
        if (null != intParams && !intParams.isEmpty()) {
            // max 1 `interpolation' KV-pair
            if (intParams.size() > 1) {
                throw new WCSException(ExceptionCode.InvalidRequest,
                                       "Multiple \"" + KEY_INTERPOLATION + "\" parameters in the request: must be unique.");
            }
            // if set, it must be a supported one:
            else if (!supportedInterpolationTypes.contains(intParams.get(0))) {
                throw new WCSException(ExceptionCode.InterpolationMethodNotSupported);
            }
        }
    }

    /**
     * Check that an input XML GetCoverage request is compliant with service capabilities,
     * concerning the interpolation extension.
     * @param gcRequest
     * @param intElem
     * @throws WCSException
     */
    public static void parseGetCoverageXMLRequest(GetCoverageRequest gcRequest, Element intElem) throws WCSException {

        // check
        if (!isXMLInterpolationExtension(intElem.getLocalName())) {
            throw new WCSException(ExceptionCode.InternalComponentError,
                                   "The parser was expecting a" + XMLSymbols.LABEL_INTERPOLATION + " element, " + intElem.getLocalName() + " given");
        }

        // read the globalInterpolation param
        // (interpolation-per-axis is not supported currently)
        Elements children = intElem.getChildElements();
        if (children.size() != 1) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                                   "One <" + XMLSymbols.LABEL_GLOBAL_INTERPOLATION + "> element is required inside an <"
                                   + XMLSymbols.LABEL_INTERPOLATION + "> element.");
        } else {
            Element globalIntElement = children.get(0);
            if (globalIntElement.getLocalName().equalsIgnoreCase(XMLSymbols.LABEL_GLOBAL_INTERPOLATION)) {
                // must be supported
                String intValue = globalIntElement.getValue();
                if (!supportedInterpolationTypes.contains(intValue)) {
                    throw new WCSException(ExceptionCode.InterpolationMethodNotSupported);
                }
            }
        }
    }

    /**
     * Recognizes an XML root element of an Interpolation extension.
     * @param elementName
     * @return True if this is the root of an XML Interpolation extension.
     */
    public static boolean isXMLInterpolationExtension(String elementName) {
        return elementName.equalsIgnoreCase(LABEL_INTERPOLATION);
    }
}
