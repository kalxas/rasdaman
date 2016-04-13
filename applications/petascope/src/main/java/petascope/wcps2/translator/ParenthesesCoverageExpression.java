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

import petascope.wcps2.metadata.Coverage;

/**
 * Translator class for coverage expressions that are surrounded by parenthesis.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ParenthesesCoverageExpression extends CoverageExpression {

    public ParenthesesCoverageExpression(IParseTreeNode coverageExpression) {
        this.coverageExpression = coverageExpression;
        addChild(coverageExpression);

        //@TODO this should be redone in the grammar as PaenthesesCoverageExpression and ParanthesesScalarExpression to avoid this check
        if (coverageExpression instanceof CoverageExpression) {
            setCoverage(((CoverageExpression) coverageExpression).getCoverage());
        } else {
            setCoverage(Coverage.DEFAULT_COVERAGE);
        }
    }

    @Override
    public String toRasql() {
        return " ( " + coverageExpression.toRasql() + " ) ";
    }

    private final IParseTreeNode coverageExpression;
}
