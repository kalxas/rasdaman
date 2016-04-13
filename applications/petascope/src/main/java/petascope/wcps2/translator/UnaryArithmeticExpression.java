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
 * Translation node from wcps unary arithmetic expression to rasql
 * Example:
 * <code>
 * abs($c1)
 * </code>
 * translates to
 * <code>
 * abs(c1)
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class UnaryArithmeticExpression extends CoverageExpression {

    public UnaryArithmeticExpression(String operator, IParseTreeNode coverageExpr) {
        this.operator = operator;
        this.coverageExpr = coverageExpr;
        addChild(coverageExpr);
        setCoverage(((CoverageExpression) coverageExpr).getCoverage());
    }

    @Override
    public String toRasql() {
        String template = TEMPLATE.replace("$coverage", this.coverageExpr.toRasql());
        //real and imaginary translate to postfix operations in rasql
        //yielding .re and .im
        if (this.operator.toLowerCase().equals(POST_REAL) || this.operator.toLowerCase().equals(POST_IMAGINARY)) {
            template = template.replace("$preOperator", "").replace("$postOperator", "." + this.operator);
        } else {
            template = template.replace("$preOperator", this.operator + "(").replace("$postOperator", ")");
        }
        return template;
    }

    private String operator;
    private IParseTreeNode coverageExpr;
    private final String TEMPLATE = "$preOperator $coverage $postOperator";
    private final String POST_REAL = "re";
    private final String POST_IMAGINARY = "im";
}
