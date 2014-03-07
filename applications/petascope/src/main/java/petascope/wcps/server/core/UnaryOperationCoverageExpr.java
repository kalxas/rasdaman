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

public class UnaryOperationCoverageExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(UnaryOperationCoverageExpr.class);
    
    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = { 
        WcpsConstants.MSG_UNARY_PLUS, WcpsConstants.MSG_UNARY_MINUS,
        WcpsConstants.MSG_SQRT, WcpsConstants.MSG_ABS,
        WcpsConstants.MSG_EXP, WcpsConstants.MSG_LOG,
        WcpsConstants.MSG_SIN, WcpsConstants.MSG_COS,
        WcpsConstants.MSG_SINH, WcpsConstants.MSG_COSH,
        WcpsConstants.MSG_TANH, WcpsConstants.MSG_ARCSIN,
        WcpsConstants.MSG_ARCCOS, WcpsConstants.MSG_ARCTAN,
        WcpsConstants.MSG_NOT, WcpsConstants.MSG_RE,
        WcpsConstants.MSG_LN, WcpsConstants.MSG_BIT,
        WcpsConstants.MSG_TAN, WcpsConstants.MSG_CAST,
        WcpsConstants.MSG_IM, WcpsConstants.MSG_FIELD_SELECT,
    };
    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private CoverageExpr child;
    private CoverageInfo info;
    private String operation;
    private String params;

    public UnaryOperationCoverageExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        String nodeName = node.getNodeName();
        log.trace(nodeName);

        if (nodeName.equals(WcpsConstants.MSG_UNARY_PLUS)) {
            operation = "+";
            child = new CoverageExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WcpsConstants.MSG_UNARY_MINUS)) {
            operation = "-";
            child = new CoverageExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WcpsConstants.MSG_SQRT) || nodeName.equals(WcpsConstants.MSG_ABS)
                || nodeName.equals(WcpsConstants.MSG_EXP) || nodeName.equals(WcpsConstants.MSG_LOG) || nodeName.equals(WcpsConstants.MSG_LN)
                || nodeName.equals(WcpsConstants.MSG_SIN) || nodeName.equals(WcpsConstants.MSG_COS) || nodeName.equals(WcpsConstants.MSG_TAN)
                || nodeName.equals(WcpsConstants.MSG_SINH) || nodeName.equals(WcpsConstants.MSG_COSH)
                || nodeName.equals(WcpsConstants.MSG_TANH) || nodeName.equals(WcpsConstants.MSG_ARCSIN)
                || nodeName.equals(WcpsConstants.MSG_ARCCOS) || nodeName.equals(WcpsConstants.MSG_ARCTAN)
                || nodeName.equals(WcpsConstants.MSG_NOT) || nodeName.equals(WcpsConstants.MSG_RE) || nodeName.equals(WcpsConstants.MSG_IM)) {
            operation = nodeName;
            child = new CoverageExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WcpsConstants.MSG_BIT)) {
            operation = WcpsConstants.MSG_BIT;
            Node c = node.getFirstChild();

            while (c != null) {
                if (c.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                    c = c.getNextSibling();
                    continue;
                }

                if (c.getNodeName().equals(WcpsConstants.MSG_BITINDEX)) {
                    try {
                        params = c.getFirstChild().getNodeValue();
                        int i = Integer.parseInt(params);
                        log.trace("Found bitIndex = " + params);
                    } catch (NumberFormatException e) {
                        throw new WCPSException("Invalid Number as bitIndex: " + params);
                    }
                } else {
                    child = new CoverageExpr(c, xq);
                }

                c = c.getNextSibling();
            }
        } else if (nodeName.equals(WcpsConstants.MSG_CAST)) {
            operation = WcpsConstants.MSG_CAST;
            Node c = node.getFirstChild();

            while (c != null) {
                log.trace("  " + WcpsConstants.MSG_CHILD + " " + WcpsConstants.MSG_NAME + ": " + c.getNodeName());
                if (c.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                    c = c.getNextSibling();
                    continue;
                }

                if (c.getNodeName().equals(WcpsConstants.MSG_TYPE)) {
                    RangeField typeNode = new RangeField(c, xq);
                    params = typeNode.toRasQL();
                } else {
                    child = new CoverageExpr(c, xq);
                }

                c = c.getNextSibling();
            }
        } else if (nodeName.equals(WcpsConstants.MSG_FIELD_SELECT)) {
            operation = WcpsConstants.MSG_SELECT;
            Node c = node.getFirstChild();

            while (c != null) {
                if (c.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                    c = c.getNextSibling();
                    continue;
                }

                if (c.getNodeName().equals(WcpsConstants.MSG_FIELD)) {
                    FieldName nameNode = new FieldName(c.getFirstChild(), xq);
                    params = nameNode.toRasQL();
                } else {
                    child = new CoverageExpr(c, xq);
                }

                c = c.getNextSibling();
            }
        } else {
            throw new WCPSException("Unknown unary operation: " + nodeName);
        }

        info = new CoverageInfo(child.getCoverageInfo());
        log.trace("  " + WcpsConstants.MSG_OPERATION + ": " + operation);
        
        // Add children to let the XML query be re-traversed
        if (child != null) super.children.add(child);        
    }

    public CoverageInfo getCoverageInfo() {
        return info;
    }

    public String toRasQL() {
        if (operation.equals(WcpsConstants.MSG_SQRT) || operation.equals(WcpsConstants.MSG_ABS) || operation.equals(WcpsConstants.MSG_EXP)
                || operation.equals(WcpsConstants.MSG_LOG) || operation.equals(WcpsConstants.MSG_LN) || operation.equals(WcpsConstants.MSG_SIN)
                || operation.equals(WcpsConstants.MSG_COS) || operation.equals(WcpsConstants.MSG_TAN)
                || operation.equals(WcpsConstants.MSG_SIN) || operation.equals(WcpsConstants.MSG_COS)
                || operation.equals(WcpsConstants.MSG_TANH) || operation.equals(WcpsConstants.MSG_ARCSIN)
                || operation.equals(WcpsConstants.MSG_ARCCOS) || operation.equals(WcpsConstants.MSG_ARCTAN)
                || operation.equals(WcpsConstants.MSG_NOT) || operation.equals("+") || operation.equals("-")) {
            return operation + "(" + child.toRasQL() + ")";
        } else if (operation.equals(WcpsConstants.MSG_CAST)) {
            // Use rasql's direct "type-casting" facility for constant scalar expressions
            // For example, (char)1 does not work, but 1c is a valid expression.
            if (child.isScalarExpr() && params.equals(WcpsConstants.MSG_CHAR))
                return child.toRasQL() + RASQL_C;
            else
                return "(" + params + ")(" + child.toRasQL() + ")";
        } else if (operation.equals(WcpsConstants.MSG_SELECT)) {
            return "(" + child.toRasQL() + ")." + params;
        } else if (operation.equals(WcpsConstants.MSG_BIT)) {
            return RASQL_BIT + "(" + child.toRasQL() + "," + params + ")";
        }

        return " " + WcpsConstants.MSG_ERROR + " ";
    }
}
