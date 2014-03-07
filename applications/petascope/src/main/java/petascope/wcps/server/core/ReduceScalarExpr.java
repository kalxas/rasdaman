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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.WcpsConstants;

public class ReduceScalarExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(ReduceScalarExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_ALL,
        WcpsConstants.MSG_SOME,
        WcpsConstants.MSG_COUNT,
        WcpsConstants.MSG_ADD,
        WcpsConstants.MSG_AVG,
        WcpsConstants.MSG_MIN,
        WcpsConstants.MSG_MAX,
    };
    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    CoverageExpr expr;
    String op;

    public ReduceScalarExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        log.trace(node.getNodeName());
        if (node.getNodeName().equals(WcpsConstants.MSG_REDUCE)) {
            node = node.getFirstChild();
        }
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        String nodeName = node.getNodeName().toLowerCase();

        if (nodeName.equals(WcpsConstants.MSG_ALL) ||
                nodeName.equals(WcpsConstants.MSG_SOME) ||
                nodeName.equals(WcpsConstants.MSG_COUNT) ||
                nodeName.equals(WcpsConstants.MSG_ADD) ||
                nodeName.equals(WcpsConstants.MSG_AVG) ||
                nodeName.equals(WcpsConstants.MSG_MIN) ||
                nodeName.equals(WcpsConstants.MSG_MAX)) {
            op = nodeName;

            if (!op.equals(WcpsConstants.MSG_ALL) && !op.equals(WcpsConstants.MSG_SOME)) {
                op = op + "_" + WcpsConstants.MSG_CELLS;
            }
            log.trace("Reduce operation: " + op);

            node = node.getFirstChild();

            while ((node != null) && (node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT))) {
                node = node.getNextSibling();
            }

            expr = new CoverageExpr(node, xq);

            // Keep the child for XML tree re-traversing
            super.children.add(expr);

        } else {
            throw new WCPSException("invalid ReduceScalarExprType node: " + nodeName);
        }
    }

    public String toRasQL() {
        return op + "(" + expr.toRasQL() + ")";
    }
}
