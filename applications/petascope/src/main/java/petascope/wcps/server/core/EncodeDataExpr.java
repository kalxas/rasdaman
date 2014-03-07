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
package petascope.wcps.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.core.IDynamicMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.MiscUtil;
import petascope.util.WcpsConstants;
import static petascope.util.ras.RasConstants.*;
import petascope.wcs2.extensions.FormatExtension;

// This is the equivalent of the "ProcessingExprType" complex XML type.
public class EncodeDataExpr extends AbstractRasNode {
       
    private static Logger log = LoggerFactory.getLogger(EncodeDataExpr.class);

    private IRasNode coverageExprType;
    private String extraParams;
    private String format;
    private String mime;
    private Boolean store;

    public EncodeDataExpr(Node node, XmlQuery request) throws WCPSException, SecoreException {
        Node child;
        String nodeName;
        log.trace(node.getNodeName());

        for (child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            nodeName = child.getNodeName();
            
            if (nodeName.equals("#" + WcpsConstants.MSG_TEXT)) {
                continue;
            }

            if (nodeName.equals(WcpsConstants.MSG_FORMAT)) {
                format = child.getFirstChild().getNodeValue();
                mime = request.getMetadataSource().formatToMimetype(format);
                log.trace("  " + WcpsConstants.MSG_FORMAT + ": " + format + ", " + WcpsConstants.MSG_MIME + ": " + mime);
                continue;
            }

            if (nodeName.equals(WcpsConstants.MSG_EXTRA_PARAMETERS)) {
                Node paramsChild = child.getFirstChild();
                if (paramsChild != null) {
                    extraParams = paramsChild.getNodeValue();
                    log.trace("extra params: " + extraParams);
                }
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

        Node _store = node.getAttributes().getNamedItem(WcpsConstants.MSG_STORE);
        if (_store != null) {
            store = _store.getNodeValue().equals(WcpsConstants.MSG_TRUE);
        }
    }

    public String getMime() {
        return mime;
    }
 
    public String toRasQL() {
        // TODO: cjucovschi - implement store

        String result = "";
        IDynamicMetadataSource metadataSource = Wcps.getDynamicMetadataSource();

        if (format.equals(WcpsConstants.MSG_RAW)) {
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
                result = RASQL_ENCODE;
            } else {
                result = format;
            }
            
            // first parameter to function
            result += "(" + coverageExprType.toRasQL();
            
            // second parameter has to be the gdal format name, in case of encode()
            if (encode) {
                result += ", \"" + gdalid + "\"";
            }
            
            if (!encode) {
                if (extraParams != null) {
                    result = result + ", \"" + extraParams + "\"";
                }
            } else if (coverageExprType instanceof CoverageExpr) {
                // finally extra parameters to the encoding function.
                // They can be either explicitely set by the user (or by WCS engine) or,
                // in case of GTiff/JPEG200 enconding, automatically filled with (geo)bounds
                
                // Get the bounds of the 2D requested coverage
                try {
                    CrsUtil.CrsProperties crsProperties = new CrsUtil.CrsProperties((CoverageExpr)coverageExprType, 2);
                    CoverageInfo info = ((CoverageExpr) coverageExprType).getCoverageInfo();
                    
                    if (info != null) {
                        // Build the whole string (dimensions of reqBounds are already checked inside getRequestBounds)
                        if (info.getBbox() != null) {
                            crsProperties.setCrs(info.getBbox().getCrsName());
                        }
                        
                        // Append params to the rasql query:
                        extraParams = crsProperties.toString(extraParams);
                        result = result + ", \"" + extraParams + "\"";
                    }
                } catch (WCPSException ex) {
                    log.warn("GDAL extra CRS parameters not set due to error: " + ex.getMessage());
                }
            }            
            result = result + ")";
        }
        return result;
    }
}
