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
import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import petascope.util.WCPSConstants;

public class ReduceScalarExpr implements IRasNode {
    
    private static Logger log = LoggerFactory.getLogger(ReduceScalarExpr.class);

    CoverageExpr expr;
    String op;

    public ReduceScalarExpr(Node node, XmlQuery xq) throws WCPSException {
        log.trace(node.getNodeName());
        if (node.getNodeName().equals(WCPSConstants.MSG_REDUCE)) {
            node = node.getFirstChild();
        }
        while ((node != null) && node.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        String nodeName = node.getNodeName().toLowerCase();

        if (nodeName.equals(WCPSConstants.MSG_ALL) ||
                nodeName.equals(WCPSConstants.MSG_SOME) ||
                nodeName.equals(WCPSConstants.MSG_COUNT) || 
                nodeName.equals(WCPSConstants.MSG_ADD) || 
                nodeName.equals(WCPSConstants.MSG_AVG) || 
                nodeName.equals(WCPSConstants.MSG_MIN) || 
                nodeName.equals(WCPSConstants.MSG_MAX)) {
            op = nodeName;

            if (!op.equals(WCPSConstants.MSG_ALL) && !op.equals(WCPSConstants.MSG_SOME)) {
                op = op + "_" + WCPSConstants.MSG_CELLS;
            }
            log.trace(WCPSConstants.MSG_REDUCE_OPERATION + op);

            node = node.getFirstChild();

            while ((node != null) && (node.getNodeName().equals("#" + WCPSConstants.MSG_TEXT))) {
                node = node.getNextSibling();
            }

            expr = new CoverageExpr(node, xq);
        } else {
            throw new WCPSException(WCPSConstants.ERRTXT_INVALID_REDUCE_SCALAR_EXPR + nodeName);
        }
    }

    public String toRasQL() {
        return op + "(" + expr.toRasQL() + ")";
    }
}
