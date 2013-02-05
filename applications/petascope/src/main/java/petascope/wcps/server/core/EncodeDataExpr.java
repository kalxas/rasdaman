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
package petascope.wcps.server.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.core.IDynamicMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.MiscUtil;
import petascope.util.WCPSConstants;
import petascope.wcs2.extensions.FormatExtension;

// This is the equivalent of the "ProcessingExprType" complex XML type.
public class EncodeDataExpr extends AbstractRasNode {
       
    private static Logger log = LoggerFactory.getLogger(EncodeDataExpr.class);

    private IRasNode coverageExprType;
    private String extraParams;
    private String format;
    private String mime;
    private Boolean store;

    public EncodeDataExpr(Node node, XmlQuery request) throws WCPSException {
        Node child;
        String nodeName;
        log.trace(node.getNodeName());

        for (child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            nodeName = child.getNodeName();
            
            if (nodeName.equals("#" + WCPSConstants.MSG_TEXT)) {
                continue;
            }

            if (nodeName.equals(WCPSConstants.MSG_FORMAT)) {
                format = child.getFirstChild().getNodeValue();
                mime = request.getMetadataSource().formatToMimetype(format);
                log.trace("  " + WCPSConstants.MSG_FORMAT + ": " + format + ", " + WCPSConstants.MSG_MIME + ": " + mime);
                continue;
            }

            if (nodeName.equals(WCPSConstants.MSG_EXTRA_PARAMETERS)) {
                extraParams = child.getFirstChild().getNodeValue();
                log.trace("  " + WCPSConstants.MSG_EXTRA_PARAMS + ": " + extraParams);
                continue;
            }

            try {
                coverageExprType = new CoverageExpr(child, request);
            } catch (WCPSException ex) {
                throw ex;
            }
            
            /// Keep this child for XML tree crawling:
            super.children.add(coverageExprType);
        }

        Node _store = node.getAttributes().getNamedItem(WCPSConstants.MSG_STORE);
        if (_store != null) {
            store = _store.getNodeValue().equals(WCPSConstants.MSG_TRUE);
        }
    }

    public String getMime() {
        return mime;
    }
 
    public String toRasQL() {
        // TODO: cjucovschi - implement store

        String result = "";
        IDynamicMetadataSource metadataSource = Wcps.getDynamicMetadataSource();

        if (format.equals(WCPSConstants.MSG_RAW)) {
            result = coverageExprType.toRasQL();
        } else {
            // check if there is a gdal id, it may be a rasdaman specific format like CSV
            boolean encode = true;
            String gdalid = metadataSource.formatToGdalid(format);
            if (gdalid == null) {
                String frmt = metadataSource.gdalidToFormat(format);
                if (frmt == null) {
                    // we don't use the encode function but a rasdaman format function
                    encode = false;
                } else {
                    gdalid = format;
                }
            }
            
            // determine function name either encode() or csv() (and similar)
            if (encode) {
                result = WCPSConstants.MSG_ENCODE;
            } else {
                result = format;
            }
            
            // first parameter to function
            result += "(" + coverageExprType.toRasQL();
            
            // second parameter has to be the gdal format name, in case of encode()
            if (encode) {
                result += ", \"" + gdalid + "\"";
            }
            
            // finally extra parameters to the encoding function.
            // They can be either explicitely set by the user (or by WCS engine) or,
            // in case of GTiff/JPEG200 enconding, automatically filled with (geo)bounds
            if (extraParams != null) {
                extraParams = '"' + extraParams + '"';
                result = result + ", " + extraParams;
            } else if (encode && coverageExprType instanceof CoverageExpr
                    && (format.equals(FormatExtension.TIFF_ENCODING) || format.equals(FormatExtension.JP2_ENCODING))) {
                
                // Get the bounds of the 2D requested coverage
                try {
                    List<Double> reqBounds = this.getRequestBounds(coverageExprType, 2);
                    CoverageInfo info = ((CoverageExpr) coverageExprType).getCoverageInfo();
                    
                    if (!reqBounds.isEmpty() && info != null) {
                        // Build the whole string (dimensions of reqBounds are already checked inside getRequestBounds)
                        String crs = (info.getBbox() == null) ? "" : info.getBbox().getCrsName();
                        MiscUtil.CrsProperties crsProperties =
                                (new MiscUtil()).new CrsProperties(
                                reqBounds.get(0), // x1_min
                                reqBounds.get(1), // x1_max
                                reqBounds.get(2), // x2_min
                                reqBounds.get(3), // x2_max
                                crs);
                        
                        // Append params to the rasql query:
                        extraParams = crsProperties.toString();
                        result = result + ", \"" + extraParams + "\"";
                    }
                } catch (WCPSException ex) {
                    log.warn("GDAL extra params won't be set: " + ex.getMessage());
                }
            }            
            result = result + ")";
        }
        return result;
    }
    
    /** 
     * Returns the bounds of the requested coverage with trim-updates and letting out sliced dimensions.
     * Dimensionality of the bounds is checked against the DIM argument.
     * @param queryRoot The root node of the XML query, used to fetch trims and slices
     * @param info      
     * @return
     * @throws WCPSException 
     */
    private List<Double> getRequestBounds(IRasNode queryRoot, Integer DIM) throws WCPSException {
        
        // variables
        List<Double> outList = new ArrayList<Double>();
        CoverageInfo info = ((CoverageExpr) queryRoot).getCoverageInfo();
        
        if (info != null) {
            Map<Integer, String>   orderToName = new HashMap<Integer, String>(); // (order of subset) not necessarily = (order of coverage axes)
            Map<String, Double[]> nameToBounds = new HashMap<String, Double[]>();
            
            // Fetch the subset operations (trims) in the WCPS query to set appropriate (geo)bounds
            List<TrimCoverageExpr>   trims = MiscUtil.childrenOfType(queryRoot, TrimCoverageExpr.class);
            List<SliceCoverageExpr> slices = MiscUtil.childrenOfType(queryRoot, SliceCoverageExpr.class);
            
            // Check each dimension: slice->discard, trim->setBounds, otherwise set bbox bounds
            for (int i=0; i<info.getNumDimensions(); i++) {
                String dimName = info.getDomainElement(i).getName();
                
                // Check slices
                boolean sliced = false;
                for (SliceCoverageExpr slice : slices) {
                    if (slice.slicesDimension(dimName)) {
                        sliced = true; // Skip to next axis
                    }
                }
                
                // The dimension is surely in the output
                if (!sliced) {
                    try {
                        orderToName.put(info.getDomainIndexByName(dimName), dimName);
                        
                        // Set the bounds of this dimension: total bbox first, then update in case of trims in the request
                        nameToBounds.put(dimName, new Double[]{info.getDomainElement(i).getNumLo(), info.getDomainElement(i).getNumHi()});
                        for (TrimCoverageExpr trim : trims) {
                            if (trim.trimsDimension(dimName)) {
                                // Set bounds specified in the trim (themselves trimmed by bbox values)                                
                                Double[] trimBounds = trim.trimmingValues(dimName);
                                if (trimBounds[0] < nameToBounds.get(dimName)[0]) trimBounds[0] = nameToBounds.get(dimName)[0]; // trimLo < bboxLo
                                if (trimBounds[1] > nameToBounds.get(dimName)[1]) trimBounds[1] = nameToBounds.get(dimName)[1]; // trimHi > bboxHi
                                nameToBounds.remove(dimName);
                                nameToBounds.put(dimName, trimBounds);
                            }
                        }
                    } catch (WCPSException ex) {
                        log.error(ex.getMessage());
                        throw ex;
                    }
                }
            }
            
            // Check dimensions is exactly 2:
            if (orderToName.size() != DIM) {
                String message = "Trying to encode a " + format + " but the number of output dimensions is " + orderToName.size() + ".";
                log.error(message);
                throw new WCPSException(ExceptionCode.InvalidRequest, message);
            }
            // end of method
            
            // Set the bounds in the proper order (according to the order of the axes in the coverage
            Double[] dom1 = nameToBounds.get(orderToName.get(Collections.min(orderToName.keySet())));
            Double[] dom2 = nameToBounds.get(orderToName.get(Collections.max(orderToName.keySet())));
            
            // Output: min1, max1, min2, max2
            outList.addAll(Arrays.asList(dom1));
            outList.addAll(Arrays.asList(dom2));
        }
        
        return outList;
    }
}
