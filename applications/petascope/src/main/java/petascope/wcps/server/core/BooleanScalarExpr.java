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
import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.util.WcpsConstants;
import static petascope.util.ras.RasConstants.*;

public class BooleanScalarExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(BooleanScalarExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_BOOLEAN_CONSTANT.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_AND.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_OR.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_XOR.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_LESSTHAN.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_LESSOREQUAL.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_GREATERTHAN.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_GREATEROREQUAL.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_EQUALNUMERIC.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_NOTEQUALNUMERIC.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_EQUAL_STRING.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_NOT_EQUAL_STRING.toLowerCase(),
        WcpsConstants.MSG_BOOLEAN_NOT.toLowerCase(),
        WcpsConstants.MSG_BIT.toLowerCase(),
    };
    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private IRasNode first, second;
    private String op;
    private boolean simple;    // true if the expression is just a value
    private String value;

    public BooleanScalarExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        if (node == null) {
            throw new WCPSException("Unexpected null node.");
        }

        String nodeName = node.getNodeName();

        simple = false;

        if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_CONSTANT)) {
            simple = true;
            value = node.getFirstChild().getNodeValue();
        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_AND)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_OR)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_XOR)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSTHAN)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSOREQUAL)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATERTHAN)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATEROREQUAL)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUALNUMERIC)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOTEQUALNUMERIC)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUAL_STRING)
                   || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOT_EQUAL_STRING)) {
            // Logical operations
            if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_AND)
                    || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_OR)
                    || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_XOR)) {
                // Remove the "boolean" in front
                op = nodeName.substring(7).toLowerCase();

                Node child = node.getFirstChild();

                first = new BooleanScalarExpr(child, xq);
                child = child.getNextSibling();
                second = new BooleanScalarExpr(child, xq);
            } else { // Boolean Comparison operations between numbers or strings
                if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSTHAN) || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSOREQUAL)
                        || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATERTHAN) || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATEROREQUAL)
                        || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUALNUMERIC) || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOTEQUALNUMERIC)) {
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSTHAN)) {
                        op = "<";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSOREQUAL)) {
                        op = "<=";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATERTHAN)) {
                        op = ">";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATEROREQUAL)) {
                        op = ">=";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUALNUMERIC)) {
                        op = "=";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOTEQUALNUMERIC)) {
                        op = "!=";
                    }

                    Node child = node.getFirstChild();

                    first = new NumericScalarExpr(child, xq);
                    child = child.getNextSibling();
                    second = new NumericScalarExpr(child, xq);
                } else {
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUAL_STRING)) {
                        op = "=";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOT_EQUAL_STRING)) {
                        op = "!=";
                    }

                    Node child = node.getFirstChild();

                    first = new StringScalarExpr(child, xq);
                    child = child.getNextSibling();
                    second = new StringScalarExpr(child, xq);
                }
            }

        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOT)) {
            op = WcpsConstants.MSG_NOT;
            first = new BooleanScalarExpr(node.getFirstChild(), xq);
        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BIT)) {
            op = WcpsConstants.MSG_BIT;
            first = new CoverageExpr(node.getFirstChild(), xq);
            second = new NumericScalarExpr(node.getFirstChild().getNextSibling(), xq);
        } else {
            throw new WCPSException("Unexpected Binary Expression node : " + node.getNodeName());
        }
        log.trace("Boolean Scalar Expr SUCCESS: " + node.getNodeName());

        // Keep children for XML tree crawling
        super.children.add(first);
        if (second != null) {
            super.children.add(second);    // "!" operation is unary
        }
    }

    public String toRasQL() {
        if (simple) {
            return value;
        }

        if (op.equalsIgnoreCase(WcpsConstants.MSG_NOT)) {
            return RASQL_NOT + "(" + first.toRasQL() + ")";
        } else if (op.equalsIgnoreCase(WcpsConstants.MSG_BIT)) {
            return RASQL_BIT + "(" + first.toRasQL() + "," + second.toRasQL() + ")";
        } else {
            return "(" + first.toRasQL() + ")" + op + "(" + second.toRasQL() + ")";
        }
    }
}
