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

public class NumericScalarExpr implements IRasNode {
    
    private static Logger log = LoggerFactory.getLogger(NumericScalarExpr.class);

    private IRasNode first, second;
    private String op, value;
    private boolean twoChildren;
    private double dvalue;

    public NumericScalarExpr(Node node, XmlQuery xq) throws WCPSException {
        twoChildren = false;

        while ((node != null) && node.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        String nodeName = node.getNodeName();
        
        log.trace(nodeName);

        op = "";

        if (nodeName.equals(WCPSConstants.MSG_NUMERIC_CONSTANT)) {
            twoChildren = false;
            op = code(nodeName);
            value = node.getFirstChild().getNodeValue();
            try {
                dvalue = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new WCPSException(WCPSConstants.ERRTXT_COULD_NOT_UNDERSTAND_CONST + value);
            }
        } else if (nodeName.equals(WCPSConstants.MSG_COMPLEX_CONSTANT)
                || nodeName.equals(WCPSConstants.MSG_CONDENSE)
                || nodeName.equals(WCPSConstants.MSG_REDUCE)) {
            op = code(nodeName);
            twoChildren = false;
            if (nodeName.equals(WCPSConstants.MSG_COMPLEX_CONSTANT)) {
                first = new ComplexConstant(node, xq);
            }
            if (nodeName.equals(WCPSConstants.MSG_CONDENSE)) {
                first = new CondenseScalarExpr(node, xq);
            }
            if (nodeName.equals(WCPSConstants.MSG_REDUCE)) {
                first = new ReduceScalarExpr(node, xq);
            }
        } else if (nodeName.equals(WCPSConstants.MSG_NUMERIC_UNARY_MINUS) 
                || nodeName.equals(WCPSConstants.MSG_NUMERIC_SQRT)
                || nodeName.equals(WCPSConstants.MSG_NUMERIC_ABS)) {
            op = code(nodeName);
            twoChildren = false;
            first = new NumericScalarExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WCPSConstants.MSG_NUMERIC_ADD)
                || nodeName.equals(WCPSConstants.MSG_NUMERIC_MINUS)
                || nodeName.equals(WCPSConstants.MSG_NUMERIC_MULT)
                || nodeName.equals(WCPSConstants.MSG_NUMERIC_DIV)) {
            try {
                op = code(nodeName);
                twoChildren = true;
                Node child = node.getFirstChild();
                first = new NumericScalarExpr(child, xq);
                second = new NumericScalarExpr(child.getNextSibling(), xq);
            } catch (WCPSException e) {
                log.error(WCPSConstants.ERRTXT_FAILED_PARSE_NUM_EXPR);
            }
        } else if (nodeName.equals(WCPSConstants.MSG_VARIABLE_REF)) {
            try {
                op = code(nodeName);
                twoChildren = false;
                first = new VariableReference(node, xq);
                log.trace(WCPSConstants.MSG_MATCHING_VAR_REF + first.toRasQL());
            } catch (WCPSException e) {
                log.error(WCPSConstants.ERRTXT_FAILED_MATCH_VAR_REF
                        + e.toString());
            }
        } else {
            throw new WCPSException(WCPSConstants.ERRTXT_UNEXPECTED_NUM_SCALAR_EXPR
                    + node.getNodeName());
        }
        log.trace("  " + WCPSConstants.MSG_OPERATION + ": " + op + ", " + WCPSConstants.MSG_BINARY + ": " + twoChildren);
    }

    public String toRasQL() {
        String result = "";
        if (twoChildren == false)
        {
            if (op.equals(WCPSConstants.MSG_VARIABLE)) {
                result = first.toRasQL();
            } else if (op.equals(WCPSConstants.MSG_VALUE)) {
                result = value;
            } else if (op.equals("-")) {
                    result = "-" + first.toRasQL();
            } else if (op.equals(WCPSConstants.MSG_SQRT)) {
                    result = WCPSConstants.MSG_SQRT + "(" + first.toRasQL() + ")";
            } else if (op.equals(WCPSConstants.MSG_CHILD)) {
                result = first.toRasQL();
            } else if (op.equals(WCPSConstants.MSG_ABS)) {
                result = WCPSConstants.MSG_ABS + "(" + first.toRasQL() + ")";
            }
        }else if (twoChildren == true) {
            result = "(" + first.toRasQL() + ")" + op
                    + "(" + second.toRasQL() + ")";
        } else {
            return " " + WCPSConstants.ERRTXT_ERROR + " ";
        }

        return result;
    }

    private String code(String name) {
        String op = "";
        if (name.equals(WCPSConstants.MSG_NUMERIC_CONSTANT)) {
            op = WCPSConstants.MSG_VALUE;
        }
        if (name.equals(WCPSConstants.MSG_NUMERIC_UNARY_MINUS) || name.equals(WCPSConstants.MSG_NUMERIC_MINUS)) {
            op = "-";
        }
        if (name.equals(WCPSConstants.MSG_NUMERIC_ADD)) {
            op = "+";
        }
        if (name.equals(WCPSConstants.MSG_NUMERIC_MULT)) {
            op = "*";
        }
        if (name.equals(WCPSConstants.MSG_NUMERIC_DIV)) {
            op = "/";
        }
        if (name.equals(WCPSConstants.MSG_NUMERIC_SQRT)) {
            op = WCPSConstants.MSG_SQRT;
        }
        if (name.equals(WCPSConstants.MSG_NUMERIC_ABS)) {
            op  = WCPSConstants.MSG_ABS;
        }
        if (name.equals(WCPSConstants.MSG_CONDENSE) || name.equals(WCPSConstants.MSG_REDUCE)
                || name.equals(WCPSConstants.MSG_COMPLEX_CONSTANT)) {
            op = WCPSConstants.MSG_CHILD;
        }
        if (name.equals(WCPSConstants.MSG_VARIABLE_REF)) {
            op = WCPSConstants.MSG_VARIABLE;
        }

        return op;
    }

    public boolean isSingleValue() {
        return op.equals(WCPSConstants.MSG_VALUE);
    }

    public double getSingleValue() {
        return dvalue;
    }
}
