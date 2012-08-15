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

public class UnaryOperationCoverageExpr implements IRasNode, ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(UnaryOperationCoverageExpr.class);

    private CoverageExpr child;
    private CoverageInfo info;
    private String operation;
    private String params;

    public UnaryOperationCoverageExpr(Node node, XmlQuery xq)
            throws WCPSException {
        String nodeName = node.getNodeName();
        log.trace(nodeName);

        if (nodeName.equals(WCPSConstants.MSG_UNARY_PLUS)) {
            operation = "+";
            child = new CoverageExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WCPSConstants.MSG_UNARY_MINUS)) {
            operation = "-";
            child = new CoverageExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WCPSConstants.MSG_SQRT) || nodeName.equals(WCPSConstants.MSG_ABS)
                || nodeName.equals(WCPSConstants.MSG_EXP) || nodeName.equals(WCPSConstants.MSG_LOG) || nodeName.equals(WCPSConstants.MSG_LN)
                || nodeName.equals(WCPSConstants.MSG_SIN) || nodeName.equals(WCPSConstants.MSG_COS) || nodeName.equals(WCPSConstants.MSG_TAN)
                || nodeName.equals(WCPSConstants.MSG_SINH) || nodeName.equals(WCPSConstants.MSG_COSH)
                || nodeName.equals(WCPSConstants.MSG_TANH) || nodeName.equals(WCPSConstants.MSG_ARCSIN)
                || nodeName.equals(WCPSConstants.MSG_ARCCOS) || nodeName.equals(WCPSConstants.MSG_ARCTAN)
                || nodeName.equals(WCPSConstants.MSG_NOT) || nodeName.equals(WCPSConstants.MSG_RE) || nodeName.equals(WCPSConstants.MSG_IM)) {
            operation = nodeName;
            child = new CoverageExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WCPSConstants.MSG_BIT)) {
            operation = WCPSConstants.MSG_BIT;
            Node c = node.getFirstChild();

            while (c != null) {
                if (c.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
                    c = c.getNextSibling();
                    continue;
                }

                if (c.getNodeName().equals(WCPSConstants.MSG_BITINDEX)) {
                    try {
                        params = c.getFirstChild().getNodeValue();
                        int i = Integer.parseInt(params);
                        log.trace(WCPSConstants.MSG_FOUND_BITINDEX + " = " + params);
                    } catch (NumberFormatException e) {
                        throw new WCPSException(WCPSConstants.ERRTXT_INVALID_NUMBER_AS_BITINDEX + ": " + params);
                    }
                } else {
                    child = new CoverageExpr(c, xq);
                }

                c = c.getNextSibling();
            }
        } else if (nodeName.equals(WCPSConstants.MSG_CAST)) {
            operation = WCPSConstants.MSG_CAST;
            Node c = node.getFirstChild();

            while (c != null) {
                log.trace("  " + WCPSConstants.MSG_CHILD + " " + WCPSConstants.MSG_NAME + ": " + c.getNodeName());
                if (c.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
                    c = c.getNextSibling();
                    continue;
                }

                if (c.getNodeName().equals(WCPSConstants.MSG_TYPE)) {
                    RangeField typeNode = new RangeField(c, xq);
                    params = typeNode.toRasQL();
                } else {
                    child = new CoverageExpr(c, xq);
                }

                c = c.getNextSibling();
            }
        } else if (nodeName.equals(WCPSConstants.MSG_FIELD_SELECT)) {
            operation = WCPSConstants.MSG_SELECT;
            Node c = node.getFirstChild();

            while (c != null) {
                if (c.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
                    c = c.getNextSibling();
                    continue;
                }

                if (c.getNodeName().equals(WCPSConstants.MSG_FIELD)) {
                    FieldName nameNode = new FieldName(c.getFirstChild(), xq);
                    params = nameNode.toRasQL();
                } else {
                    child = new CoverageExpr(c, xq);
                }

                c = c.getNextSibling();
            }
        } else {
            throw new WCPSException(WCPSConstants.ERRTXT_UNKNOWN_UNARY_OP + ": " + nodeName);
        }

        info = new CoverageInfo(child.getCoverageInfo());
        log.trace("  " + WCPSConstants.MSG_OPERATION + ": " + operation);
    }

    public CoverageInfo getCoverageInfo() {
        return info;
    }

    public String toRasQL() {
        if (operation.equals(WCPSConstants.MSG_SQRT) || operation.equals(WCPSConstants.MSG_ABS) || operation.equals(WCPSConstants.MSG_EXP)
                || operation.equals(WCPSConstants.MSG_LOG) || operation.equals(WCPSConstants.MSG_LN) || operation.equals(WCPSConstants.MSG_SIN)
                || operation.equals(WCPSConstants.MSG_COS) || operation.equals(WCPSConstants.MSG_TAN)
                || operation.equals(WCPSConstants.MSG_SIN) || operation.equals(WCPSConstants.MSG_COS)
                || operation.equals(WCPSConstants.MSG_TANH) || operation.equals(WCPSConstants.MSG_ARCSIN)
                || operation.equals(WCPSConstants.MSG_ARCCOS) || operation.equals(WCPSConstants.MSG_ARCTAN)
                || operation.equals(WCPSConstants.MSG_NOT) || operation.equals("+") || operation.equals("-")) {
            return operation + "(" + child.toRasQL() + ")";
        } else if (operation.equals(WCPSConstants.MSG_CAST)) {
            // Use rasql's direct "type-casting" facility for constant scalar expressions
            // For example, (char)1 does not work, but 1c is a valid expression.
            if (child.isScalarExpr() && params.equals(WCPSConstants.MSG_CHAR))
                return child.toRasQL() + WCPSConstants.MSG_C;
            else
                return "(" + params + ")(" + child.toRasQL() + ")";
        } else if (operation.equals(WCPSConstants.MSG_SELECT)) {
            return "(" + child.toRasQL() + ")." + params;
        } else if (operation.equals(WCPSConstants.MSG_BIT)) {
            return WCPSConstants.MSG_BIT + "(" + child.toRasQL() + "," + params + ")";
        }

        return " " + WCPSConstants.ERRTXT_ERROR + " ";
    }
}
