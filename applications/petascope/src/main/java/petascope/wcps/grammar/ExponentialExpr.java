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

import org.slf4j.LoggerFactory;
import petascope.util.WcpsConstants;

/**
 * ExponentialExpr
 * Creation date: (3/3/2003 2:28:43 AM)
 * @author: mattia parigiani, Sorin Stancu-Mara, Andrei Aiordachioaie
 */
public class ExponentialExpr implements IParseTreeNode {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(IndexExpr.class);

    CoverageExpr coverageExpr;
    String expOperator;
    Double powerArg = null;

    public ExponentialExpr(String op, CoverageExpr ce) {
        expOperator = op;
        coverageExpr = ce;
    }

    public ExponentialExpr(String op, String arg, CoverageExpr ce) {
        expOperator = op;
        if (arg == null) {
            log.error("Power exponent is null.");
            throw new IllegalArgumentException("Power exponent is null.");
        }
        try {
            powerArg = Double.parseDouble(arg);
        } catch (NumberFormatException ex) {
            log.error("Power exponent is not a valid floating point number: " + arg);
            throw new IllegalArgumentException("Power exponent is not a valid floating point number: " + arg);
        }
        coverageExpr = ce;
    }

    public String toXML() {
        String result = "";

        result += "<" + expOperator + ">";
        result += coverageExpr.toXML();
        if (powerArg != null) {
            result += "<" + WcpsConstants.MSG_NUMERIC_CONSTANT + ">" +
                    powerArg +
                    "</" + WcpsConstants.MSG_NUMERIC_CONSTANT + ">";
        }
        result += "</" + expOperator + ">";
        return result;
    }
}
