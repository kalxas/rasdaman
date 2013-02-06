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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.core.IDynamicMetadataSource;
import petascope.exceptions.WCPSException;
import petascope.util.WCPSConstants;

// This is the equivalent of the "ProcessingExprType" complex XML type.
public class EncodeDataExpr implements IRasNode {
    
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

            // finally extra parameters to the encoding function
            if (extraParams != null) {
                extraParams = '"' + extraParams + '"';
                result = result + ", " + extraParams;
            }

            result = result + ")";
        }

        return result;
    }
}
