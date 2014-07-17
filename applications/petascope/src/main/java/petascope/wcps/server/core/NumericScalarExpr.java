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
import static petascope.util.WcpsConstants.MSG_STAR;
import static petascope.util.ras.RasConstants.*;

public class NumericScalarExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(NumericScalarExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_NUMERIC_CONSTANT.toLowerCase(),
        WcpsConstants.MSG_COMPLEX_CONSTANT.toLowerCase(),
        WcpsConstants.MSG_CONDENSE.toLowerCase(),
        WcpsConstants.MSG_REDUCE.toLowerCase(),
        WcpsConstants.MSG_NUMERIC_UNARY_MINUS.toLowerCase(),
        WcpsConstants.MSG_NUMERIC_SQRT.toLowerCase(),
        WcpsConstants.MSG_NUMERIC_ABS.toLowerCase(),
        WcpsConstants.MSG_NUMERIC_ADD.toLowerCase(),
        WcpsConstants.MSG_NUMERIC_MINUS.toLowerCase(),
        WcpsConstants.MSG_NUMERIC_MULT.toLowerCase(),
        WcpsConstants.MSG_NUMERIC_DIV.toLowerCase(),
        WcpsConstants.MSG_VARIABLE_REF.toLowerCase(),
    };
    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private IRasNode first, second;
    private String op, value;
    private boolean twoChildren;
    private double dvalue;

    public NumericScalarExpr(String value) throws WCPSException, SecoreException {
        this.value = value;
        try {
            dvalue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            if (!value.equals(MSG_STAR)) {
                throw new WCPSException("Could not understand constant: " + value);
            }
        }
        this.twoChildren = false;
        this.op = WcpsConstants.MSG_VALUE;
    }

    public NumericScalarExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        twoChildren = false;

        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        String nodeName = node.getNodeName();

        log.trace(nodeName);

        op = "";

        if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_CONSTANT)) {
            twoChildren = false;
            op = code(nodeName);
            value = node.getFirstChild().getNodeValue();
            try {
                dvalue = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                if (!value.equals(MSG_STAR)) {
                    throw new WCPSException("Could not understand constant: " + value);
                }
            }
        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_COMPLEX_CONSTANT)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_CONDENSE)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_REDUCE)) {
            op = code(nodeName);
            twoChildren = false;
            if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_COMPLEX_CONSTANT)) {
                first = new ComplexConstant(node, xq);
            }
            if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_CONDENSE)) {
                first = new CondenseScalarExpr(node, xq);
            }
            if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_REDUCE)) {
                first = new ReduceScalarExpr(node, xq);
            }
        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_UNARY_MINUS)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_SQRT)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_ABS)) {
            op = code(nodeName);
            twoChildren = false;
            first = new NumericScalarExpr(node.getFirstChild(), xq);
        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_ADD)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_MINUS)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_MULT)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_DIV)) {
            try {
                op = code(nodeName);
                twoChildren = true;
                Node child = node.getFirstChild();
                first = new NumericScalarExpr(child, xq);
                second = new NumericScalarExpr(child.getNextSibling(), xq);
            } catch (WCPSException e) {
                log.error("Failed to parse a numeric expression pair.");
            }
        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_VARIABLE_REF)) {
            try {
                op = code(nodeName);
                twoChildren = false;
                first = new VariableReference(node, xq);
                log.trace("Matched variable reference: " + first.toRasQL());
            } catch (WCPSException e) {
                log.error("Failed to match variable reference: " + e.toString());
            }
        } else {
            throw new WCPSException("Unexpected NumericScalarExpression node: " + node.getNodeName());
        }
        log.trace("  " + WcpsConstants.MSG_OPERATION + ": " + op + ", " + WcpsConstants.MSG_BINARY + ": " + twoChildren);

        // Keep the children for XML tree re-traversing
        if (twoChildren) {
            super.children.addAll(Arrays.asList(first, second));
        } else if (first != null) {
            super.children.add(first);
        }
    }

    public String toRasQL() {
        String result = "";
        if (twoChildren == false)
        {
            if (op.equalsIgnoreCase(WcpsConstants.MSG_VARIABLE)) {
                result = first.toRasQL();
            } else if (op.equalsIgnoreCase(WcpsConstants.MSG_VALUE)) {
                result = value;
            } else if (op.equalsIgnoreCase("-")) {
                    result = "-" + first.toRasQL();
            } else if (op.equalsIgnoreCase(WcpsConstants.MSG_SQRT)) {
                    result = RASQL_SQRT + "(" + first.toRasQL() + ")";
            } else if (op.equalsIgnoreCase(WcpsConstants.MSG_CHILD)) {
                result = first.toRasQL();
            } else if (op.equalsIgnoreCase(WcpsConstants.MSG_ABS)) {
                result = RASQL_ABS + "(" + first.toRasQL() + ")";
            }
        }else if (twoChildren == true) {
            result = "(" + first.toRasQL() + ")" + op
                    + "(" + second.toRasQL() + ")";
        } else {
            return " " + WcpsConstants.MSG_ERROR + " ";
        }

        return result;
    }

    private String code(String name) {
        String op = "";
        if (name.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_CONSTANT)) {
            op = WcpsConstants.MSG_VALUE;
        }
        if (name.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_UNARY_MINUS) || name.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_MINUS)) {
            op = "-";
        }
        if (name.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_ADD)) {
            op = "+";
        }
        if (name.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_MULT)) {
            op = "*";
        }
        if (name.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_DIV)) {
            op = "/";
        }
        if (name.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_SQRT)) {
            op = WcpsConstants.MSG_SQRT;
        }
        if (name.equalsIgnoreCase(WcpsConstants.MSG_NUMERIC_ABS)) {
            op  = WcpsConstants.MSG_ABS;
        }
        if (name.equalsIgnoreCase(WcpsConstants.MSG_CONDENSE) || name.equalsIgnoreCase(WcpsConstants.MSG_REDUCE)
                || name.equalsIgnoreCase(WcpsConstants.MSG_COMPLEX_CONSTANT)) {
            op = WcpsConstants.MSG_CHILD;
        }
        if (name.equalsIgnoreCase(WcpsConstants.MSG_VARIABLE_REF)) {
            op = WcpsConstants.MSG_VARIABLE;
        }

        return op;
    }

    public boolean isSingleValue() {
        return op.equalsIgnoreCase(WcpsConstants.MSG_VALUE);
    }

    public double getSingleValue() {
        return dvalue;
    }
}
