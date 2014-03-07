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
        WcpsConstants.MSG_NUMERIC_CONSTANT,
        WcpsConstants.MSG_COMPLEX_CONSTANT,
        WcpsConstants.MSG_CONDENSE,
        WcpsConstants.MSG_REDUCE,
        WcpsConstants.MSG_NUMERIC_UNARY_MINUS,
        WcpsConstants.MSG_NUMERIC_SQRT,
        WcpsConstants.MSG_NUMERIC_ABS,
        WcpsConstants.MSG_NUMERIC_ADD,
        WcpsConstants.MSG_NUMERIC_MINUS,
        WcpsConstants.MSG_NUMERIC_MULT,
        WcpsConstants.MSG_NUMERIC_DIV,
        WcpsConstants.MSG_VARIABLE_REF,
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

        if (nodeName.equals(WcpsConstants.MSG_NUMERIC_CONSTANT)) {
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
        } else if (nodeName.equals(WcpsConstants.MSG_COMPLEX_CONSTANT)
                || nodeName.equals(WcpsConstants.MSG_CONDENSE)
                || nodeName.equals(WcpsConstants.MSG_REDUCE)) {
            op = code(nodeName);
            twoChildren = false;
            if (nodeName.equals(WcpsConstants.MSG_COMPLEX_CONSTANT)) {
                first = new ComplexConstant(node, xq);
            }
            if (nodeName.equals(WcpsConstants.MSG_CONDENSE)) {
                first = new CondenseScalarExpr(node, xq);
            }
            if (nodeName.equals(WcpsConstants.MSG_REDUCE)) {
                first = new ReduceScalarExpr(node, xq);
            }
        } else if (nodeName.equals(WcpsConstants.MSG_NUMERIC_UNARY_MINUS)
                || nodeName.equals(WcpsConstants.MSG_NUMERIC_SQRT)
                || nodeName.equals(WcpsConstants.MSG_NUMERIC_ABS)) {
            op = code(nodeName);
            twoChildren = false;
            first = new NumericScalarExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WcpsConstants.MSG_NUMERIC_ADD)
                || nodeName.equals(WcpsConstants.MSG_NUMERIC_MINUS)
                || nodeName.equals(WcpsConstants.MSG_NUMERIC_MULT)
                || nodeName.equals(WcpsConstants.MSG_NUMERIC_DIV)) {
            try {
                op = code(nodeName);
                twoChildren = true;
                Node child = node.getFirstChild();
                first = new NumericScalarExpr(child, xq);
                second = new NumericScalarExpr(child.getNextSibling(), xq);
            } catch (WCPSException e) {
                log.error("Failed to parse a numeric expression pair.");
            }
        } else if (nodeName.equals(WcpsConstants.MSG_VARIABLE_REF)) {
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
            if (op.equals(WcpsConstants.MSG_VARIABLE)) {
                result = first.toRasQL();
            } else if (op.equals(WcpsConstants.MSG_VALUE)) {
                result = value;
            } else if (op.equals("-")) {
                    result = "-" + first.toRasQL();
            } else if (op.equals(WcpsConstants.MSG_SQRT)) {
                    result = RASQL_SQRT + "(" + first.toRasQL() + ")";
            } else if (op.equals(WcpsConstants.MSG_CHILD)) {
                result = first.toRasQL();
            } else if (op.equals(WcpsConstants.MSG_ABS)) {
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
        if (name.equals(WcpsConstants.MSG_NUMERIC_CONSTANT)) {
            op = WcpsConstants.MSG_VALUE;
        }
        if (name.equals(WcpsConstants.MSG_NUMERIC_UNARY_MINUS) || name.equals(WcpsConstants.MSG_NUMERIC_MINUS)) {
            op = "-";
        }
        if (name.equals(WcpsConstants.MSG_NUMERIC_ADD)) {
            op = "+";
        }
        if (name.equals(WcpsConstants.MSG_NUMERIC_MULT)) {
            op = "*";
        }
        if (name.equals(WcpsConstants.MSG_NUMERIC_DIV)) {
            op = "/";
        }
        if (name.equals(WcpsConstants.MSG_NUMERIC_SQRT)) {
            op = WcpsConstants.MSG_SQRT;
        }
        if (name.equals(WcpsConstants.MSG_NUMERIC_ABS)) {
            op  = WcpsConstants.MSG_ABS;
        }
        if (name.equals(WcpsConstants.MSG_CONDENSE) || name.equals(WcpsConstants.MSG_REDUCE)
                || name.equals(WcpsConstants.MSG_COMPLEX_CONSTANT)) {
            op = WcpsConstants.MSG_CHILD;
        }
        if (name.equals(WcpsConstants.MSG_VARIABLE_REF)) {
            op = WcpsConstants.MSG_VARIABLE;
        }

        return op;
    }

    public boolean isSingleValue() {
        return op.equals(WcpsConstants.MSG_VALUE);
    }

    public double getSingleValue() {
        return dvalue;
    }
}
