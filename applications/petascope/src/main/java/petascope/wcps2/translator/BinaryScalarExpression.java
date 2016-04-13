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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.translator;

/**
 * Class that represents a binary scalar expression
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class BinaryScalarExpression extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param firstParameter  the first operand
     * @param operator        the operator of the operation
     * @param secondParameter the second operand
     */
    public BinaryScalarExpression(String firstParameter, String operator, String secondParameter) {
        this.firstParameter = firstParameter;
        this.secondParameter = secondParameter;
        this.operator = operator;
    }

    @Override
    public String toRasql() {
        return firstParameter + operator + secondParameter;
    }

    public final String firstParameter;
    public final String secondParameter;
    public final String operator;
}
