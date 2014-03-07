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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import nu.xom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import static petascope.util.KVPSymbols.KEY_SCALEAXES;
import static petascope.util.KVPSymbols.KEY_SCALEEXTENT;
import static petascope.util.KVPSymbols.KEY_SCALEFACTOR;
import static petascope.util.KVPSymbols.KEY_SCALESIZE;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.util.XMLSymbols;
import static petascope.util.XMLSymbols.LABEL_CRSAXIS;
import static petascope.util.XMLSymbols.LABEL_HIGH;
import static petascope.util.XMLSymbols.LABEL_LOW;
import static petascope.util.XMLSymbols.LABEL_SCALEAXESBYFACTOR;
import static petascope.util.XMLSymbols.LABEL_SCALEBYFACTOR;
import static petascope.util.XMLSymbols.LABEL_SCALEFACTOR;
import static petascope.util.XMLSymbols.LABEL_SCALETOEXTENT;
import static petascope.util.XMLSymbols.LABEL_SCALETOSIZE;
import static petascope.util.XMLSymbols.LABEL_SCALING;
import static petascope.util.XMLSymbols.LABEL_TARGETSIZE;
import static petascope.util.XMLUtil.ch;
import static petascope.util.XMLUtil.getText;
import static petascope.wcs2.extensions.RangeSubsettingExtension.RANGESUBSET_KVP_PARAMNAME;
import static petascope.wcs2.extensions.RangeSubsettingExtension.RANGESUBSET_KVP_RANGE_SEPARATOR;
import petascope.wcs2.parsers.GetCoverageRequest;

/**
 * Manage Scaling Extension (OGC 12-039).
 *
 * @author <a href="mailto:m.rusu@jacobs-university.de">Mihaela Rusu</a>
 */
public class ScalingExtension implements Extension {

    private static final Logger log = LoggerFactory.getLogger(ScalingExtension.class);

    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.SCALING_IDENTIFIER;
    }

    /**
     * Recognizes an XML root element of a Scaling extension.
     * @param elementName
     * @return True if this is the root of an XML Scaling extension.
     */
    public static boolean isXMLScalingExtension(String elementName) {
        return elementName.equalsIgnoreCase(LABEL_SCALING);
    }

    /**
     * Parses the XML children elements of a Scale extension.
     * @param gcRequest
     * @param scaleElem
     * @throws WCSException
     */
    public static void parseGetCoverageXMLRequest(GetCoverageRequest gcRequest, Element scaleElem) throws WCSException {

        // check
        if (!isXMLScalingExtension(scaleElem.getLocalName())) {
            throw new WCSException(ExceptionCode.InternalComponentError,
                    "The parser was expecting a" + XMLSymbols.LABEL_RANGESUBSET + " element, " + scaleElem.getLocalName() + " given");
        }

        // parse
        for (Element elem : ch(scaleElem)) {
            String elname = elem.getLocalName();
            List<Element> cE = ch(elem);
            if (elname.equals(LABEL_SCALEBYFACTOR)) {
                if (cE != null && (cE.size() != 1 || gcRequest.isScaled())) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
                } else if (cE != null) {
                    log.debug(LABEL_SCALEBYFACTOR);
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
                    gcRequest.getScaling().setFactor(scaleFactor);
                    gcRequest.getScaling().setType(1);
                }
            } else if (elname.equals(LABEL_SCALEAXESBYFACTOR)) {
                if (cE != null && gcRequest.isScaled()) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
                } else if (cE != null) {
                    log.debug(LABEL_SCALEAXESBYFACTOR);
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
                        if (gcRequest.getScaling().isPresentFactor(axis)) {
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
                        gcRequest.getScaling().addFactor(axis, scaleFactor);
                    }
                    gcRequest.getScaling().setType(2);
                }
            } else if (elname.equals(LABEL_SCALETOSIZE)) {
                if (cE != null && gcRequest.isScaled()) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
                } else if (cE != null) {
                    log.debug(LABEL_SCALETOSIZE);
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
                        if (gcRequest.getScaling().isPresentFactor(axis)) {
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

                        gcRequest.getScaling().addSize(axis, scaleSize);
                    }
                    gcRequest.getScaling().setType(3);
                }
            } else if (elname.equals(LABEL_SCALETOEXTENT)) {
                if (cE != null && gcRequest.isScaled()) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
                } else if (cE != null) {
                    log.debug(LABEL_SCALETOEXTENT);
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
                        if (gcRequest.getScaling().isPresentFactor(axis)) {
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
                        if (gcRequest.getScaling().isPresentExtent(axis)) {
                            throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                        }
                        if (hi < lo) {
                            throw new WCSException(ExceptionCode.InvalidExtent.locator(shi));
                        }

                        gcRequest.getScaling().addExtent(axis, new Pair(lo, hi));
                    }
                    gcRequest.getScaling().setType(4);
                }
            }
        }
    }

    /**
     * Parses possible KV pairs associated with the Scaling extension.
     * @param params
     * @param request
     * @throws WCSException
     */
    public static void parseGetCoverageKVPRequest(Map<String, List<String>> params, GetCoverageRequest request) throws WCSException {

        // get scaling options
        List<String> list = params.get(KEY_SCALEFACTOR);
        if (list != null && list.size() != 1 || request.isScaled()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
        } else if (list != null) {
            float scaleFactor;
            try {
                scaleFactor = Float.parseFloat(ListUtil.head(list));
            } catch (NumberFormatException e) {
                throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(ListUtil.head(list)));
            }
            if (scaleFactor <= 0) {
                throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(ListUtil.head(list)));
            }
            request.getScaling().setFactor(scaleFactor);
            request.getScaling().setType(1);
        }

        list = params.get(KEY_SCALEAXES);
        if (list != null && request.isScaled()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
        } else if (list != null) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String keyvalue = it.next();
                String fact = "", axis = "";
                int splitPos = keyvalue.indexOf("(");
                if (splitPos != -1) {
                    axis = keyvalue.substring(0, splitPos);
                    fact = keyvalue.substring(splitPos + 1, keyvalue.length()-1);
                } else
                    throw new WCSException(ExceptionCode.InvalidRequest, "Wrong scaling parameter format: must be axis(factor).");
                if (request.getScaling().isPresentFactor(axis))
                    throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                float scaleFactor;
                try {
                    scaleFactor = Float.parseFloat(fact);
                } catch (NumberFormatException e) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(fact));
                }
                if (scaleFactor <= 0) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(fact));
                }
                request.getScaling().addFactor(axis, scaleFactor);
            }
            request.getScaling().setType(2);
        }

        list = params.get(KEY_SCALESIZE);
        if (list != null && request.isScaled()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
        } else if (list != null) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String keyvalue = it.next();
                String fact = "", axis = "";
                int splitPos = keyvalue.indexOf("(");
                if (splitPos != -1) {
                    axis = keyvalue.substring(0, splitPos);
                    fact = keyvalue.substring(splitPos + 1, keyvalue.length()-1);
                } else
                    throw new WCSException(ExceptionCode.InvalidRequest, "Wrong scaling parameter format: must be axis(size).");
                if (request.getScaling().isPresentSize(axis))
                    throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                long scaleSize;
                try {
                    scaleSize = Long.parseLong(fact);
                } catch (NumberFormatException e) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(fact));
                }
                if (scaleSize < 0) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Scaling size is not positive.");
                }

                request.getScaling().addSize(axis, scaleSize);
            }
            request.getScaling().setType(3);
        }
        list = params.get(KEY_SCALEEXTENT);
        if (list != null && request.isScaled()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
        } else if (list != null) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String keyvalue = it.next();
                String fact = "", axis = "";
                int splitPos = keyvalue.indexOf("(");
                if (splitPos != -1) {
                    axis = keyvalue.substring(0, splitPos);
                    fact = keyvalue.substring(splitPos + 1, keyvalue.length()-1);
                } else
                    throw new WCSException(ExceptionCode.InvalidRequest, "Wrong scaling parameter format: must be axis(lo:hi).");
                if (request.getScaling().isPresentExtent(axis))
                    throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                String shi = "", slo = "";
                StringTokenizer st = new StringTokenizer(fact, ":");
                if (st.countTokens() != 2)
                    throw new WCSException(ExceptionCode.InvalidRequest, "Wrong format for scaling parameters: must be 'lo:hi'.");
                slo = st.nextToken();
                shi = st.nextToken();
                long hi, lo;
                try {
                    lo = Long.parseLong(slo);
                } catch (NumberFormatException e) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(slo));
                }
                try {
                    hi = Long.parseLong(shi);
                } catch (NumberFormatException e) {
                    throw new WCSException(ExceptionCode.InvalidScaleFactor.locator(shi));
                }
                if (request.getScaling().isPresentExtent(axis)) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Axis name repeated in the scaling request: must be unique.");
                }
                if (hi < lo) {
                    throw new WCSException(ExceptionCode.InvalidExtent.locator(shi));
                }

                request.getScaling().addExtent(axis, new Pair(lo, hi));
            }
            request.getScaling().setType(4);
        }
    }
}
