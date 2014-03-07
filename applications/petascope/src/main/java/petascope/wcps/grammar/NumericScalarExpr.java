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
package petascope.wcps.grammar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.util.WcpsConstants;

/**
 * NumericScalarExpr
 * Creation date: (3/3/2003 2:28:43 AM)
 * @author: mattia parigiani, Sorin Stancu-Mara, Andrei Aiordachioaie
 */
public class NumericScalarExpr implements IParseTreeNode {

    private static Logger log = LoggerFactory.getLogger(NumericScalarExpr.class);

    CondenseExpr condense;
    String constValue;
    String function;
    NumericScalarExpr leftNumericScalarExpr, rightNumericScalarExpr;

    public NumericScalarExpr(CondenseExpr c) {
        log.trace("NumericScalarExpr condense");
        condense = c;
        function = WcpsConstants.MSG_CONDENSE;
    }

    public NumericScalarExpr(String val) {
        log.trace("NumericScalarExpr " + val);
        if (val.contains(WcpsConstants.MSG_PLUS_I)) {
            ComplexConst cc = new ComplexConst(val);

            constValue = cc.toXML();
            function = WcpsConstants.MSG_COMPLEX_CONSTANT;
        } else {
            constValue = val;
            function = WcpsConstants.MSG_NUMERIC_CONSTANT;
        }
    }

    public NumericScalarExpr(String op, NumericScalarExpr expr) {
        log.trace("NumericScalarExpr " + op + " num");
        leftNumericScalarExpr = expr;

        if (op.equals("-")) {
            function = WcpsConstants.MSG_NUMERIC_UNARY_MINUS;
        } else
        if (op.equals(WcpsConstants.MSG_SQRT)) {
            function = WcpsConstants.MSG_NUMERIC_SQRT;
        } else
        if (op.equals(WcpsConstants.MSG_ABS)) {
            function = WcpsConstants.MSG_NUMERIC_ABS;
        } else {
            log.error("Unary Operator " + op + " is not recognized.");
        }
    }

    public NumericScalarExpr(String varOp, String varName) {
        log.trace("NumericScalarExpr " + varOp + ", " + varName);
        if (varOp.equals(WcpsConstants.MSG_VAR)) {
            function = WcpsConstants.MSG_VARIABLE_REF;
            constValue = varName;
        } else {
            log.error("Internal error. This should have been a variable name: " + varName);
        }
    }

    public NumericScalarExpr(String op, NumericScalarExpr lbe, NumericScalarExpr rbe) {
        log.trace("NumericScalarExpr a " + op + " b");
        leftNumericScalarExpr = lbe;
        rightNumericScalarExpr = rbe;

        if (op.equals(WcpsConstants.MSG_PLUS)) {
            function = WcpsConstants.MSG_NUMERIC_ADD;
        } else if (op.equals(WcpsConstants.MSG_MINUS)) {
            function = WcpsConstants.MSG_NUMERIC_MINUS;
        } else if (op.equals(WcpsConstants.MSG_STAR)) {
            function = WcpsConstants.MSG_NUMERIC_MULT;
        } else if (op.equals(WcpsConstants.MSG_DIV)) {
            function = WcpsConstants.MSG_NUMERIC_DIV;
        } else {
            log.error("Operator " + op + " is not recognized.");
        }
    }

    public String toXML() {
        String result;

        if (function.equals(WcpsConstants.MSG_COMPLEX_CONSTANT)) {
            return constValue;
        }
        if (function.equals(WcpsConstants.MSG_CONDENSE)) {
            return condense.toXML();
        }

        result = "<" + function + ">";

        if (function.equals(WcpsConstants.MSG_NUMERIC_CONSTANT) || function.equals(WcpsConstants.MSG_VARIABLE_REF)) {
            result += constValue;
        } else {
            result += leftNumericScalarExpr.toXML();

            if (rightNumericScalarExpr != null) {
                result += rightNumericScalarExpr.toXML();
            }
        }

        result += "</" + function + ">";

        return result;
    }
}
