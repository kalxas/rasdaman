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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.xml.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;

// This is the equivalent of the "ProcessingExprType" complex XML type.
public class EncodeDataExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(EncodeDataExpr.class);

    private IRasNode coverageExprType;
    private String extraParams;
    private String format;
    private Boolean store;

    public EncodeDataExpr(Node node, WCPSXmlQueryParsingService request) throws WCPSException, SecoreException {
        Node child;
        String nodeName;
        log.trace(node.getNodeName());

        for (child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            nodeName = child.getNodeName();

            if (nodeName.equals(WcpsConstants.MSG_FORMAT)) {
                // e.g: "png"
//              <format>png</format> 
                format = child.getFirstChild().getNodeValue().toLowerCase();
                log.trace("  " + WcpsConstants.MSG_FORMAT + ": " + format);
                continue;
            } else if (nodeName.equals(WcpsConstants.MSG_EXTRA_PARAMETERS)) {
                // e.g: "nodata=0"
//              <extraParameters>nodata=0</extraParameters>
                Node extraParametersChild = child.getFirstChild();
                if (extraParametersChild != null) {
                    extraParams = extraParametersChild.getNodeValue();
                    log.trace("extra params: " + extraParams);
                }
                continue;
            } else {
                // the next element is not a simple element, consider it is a CoverageExpr element
                coverageExprType = new CoverageExpr(child, request);
            }

            /// Keep this child for XML tree crawling:
            super.children.add(coverageExprType);
        }

        Node storeTmp = node.getAttributes().getNamedItem(WcpsConstants.MSG_STORE);
        if (storeTmp != null) {
            store = storeTmp.getNodeValue().equals(WcpsConstants.MSG_TRUE);
        }
    }

    @Override
    public String toAbstractSyntax() {
        // TODO: cjucovschi - implement store
        String result = "encode( ";
        result = result + coverageExprType.toAbstractSyntax();

        if (format != null) {
            // e.g: "png"
            result = result + ", \"" + format + "\"";
        }
        if (extraParams != null) {
            // e.g: "nodata=0"
            result = result + ", \"" + extraParams + "\"";
        }
        result = result + " )";

        return result;
    }
}
