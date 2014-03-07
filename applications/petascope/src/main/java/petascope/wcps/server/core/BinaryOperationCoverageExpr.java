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
import static petascope.util.ras.RasConstants.*;

public class BinaryOperationCoverageExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(BinaryOperationCoverageExpr.class);
    
    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_PLUS_S,
        WcpsConstants.MSG_MINUS_S,
        WcpsConstants.MSG_MULT,
        WcpsConstants.MSG_DIV_S,
        WcpsConstants.MSG_AND,
        WcpsConstants.MSG_OR,
        WcpsConstants.MSG_EQUALS,
        WcpsConstants.MSG_LESS_THAN,
        WcpsConstants.MSG_GREATER_THAN,
        WcpsConstants.MSG_LESS_OR_EQUAL,
        WcpsConstants.MSG_GREATER_OR_EQUAL,
        WcpsConstants.MSG_OVERLAY,
        WcpsConstants.MSG_NOT_EQUALS,
        WcpsConstants.MSG_POW,
    };
    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private IRasNode first, second;
    private CoverageExprPairType pair;
    private CoverageInfo info;
    private String operation;

    public BinaryOperationCoverageExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        String nodeName = node.getNodeName();
        log.trace(nodeName);

        boolean okay = false;    // will be true if the node is recognized

        if (nodeName.equals(WcpsConstants.MSG_PLUS_S)) {
            operation = WcpsConstants.MSG_PLUS;
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_MINUS_S)) {
            operation = WcpsConstants.MSG_MINUS;
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_MULT)) {
            operation = WcpsConstants.MSG_STAR;
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_DIV_S)) {
            operation = WcpsConstants.MSG_DIV;
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_AND) || nodeName.equals(WcpsConstants.MSG_OR) || nodeName.equals("xor")) {
            operation = nodeName;
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_EQUALS)) {
            operation = WcpsConstants.MSG_EQUAL;
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_LESS_THAN)) {
            operation = "<";
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_GREATER_THAN)) {
            operation = ">";
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_LESS_OR_EQUAL)) {
            operation = "<=";
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_GREATER_OR_EQUAL)) {
            operation = ">=";
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_NOT_EQUALS)) {
            operation = "!=";
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_OVERLAY)) {
            operation = WcpsConstants.MSG_OVERLAY;
            okay = true;
        } else if (nodeName.equals(WcpsConstants.MSG_POW)) {
            operation = WcpsConstants.MSG_POW;
            okay = true;
        }

        if (!okay) {
            throw new WCPSException("Unexpected binary operation : " + nodeName);
        }
        log.trace("  " + WcpsConstants.MSG_OPERATION + ": " + operation);
        
        Node operand = node.getFirstChild();
        while (operand.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            operand = operand.getNextSibling();
        }

        pair = new CoverageExprPairType(operand, xq);
        info = new CoverageInfo(((ICoverageInfo) pair).getCoverageInfo());
        first = pair.getFirst();
        second = pair.getSecond();
        
        // Keep the children to let XML tree be re-traversed
        super.children.addAll(Arrays.asList(first, second));
    }

    public CoverageInfo getCoverageInfo() {
        return info;
    }

    public String toRasQL() {
        String ret = "";
        if (operation.equals(WcpsConstants.MSG_OVERLAY)) {
            // overlay is reversed in rasql
            ret = "((" + second.toRasQL() + ")" + RASQL_OVERLAY + "(" + first.toRasQL() + "))";
        } else if (operation.equals(WcpsConstants.MSG_POW)) {
            ret = RASQL_POW + "(" + first.toRasQL() + ", " + second.toRasQL() + ")";
        } else {
            ret = "((" + first.toRasQL() + ")" + operation + "(" + second.toRasQL() + "))";
        }
        return ret;
    }
}
