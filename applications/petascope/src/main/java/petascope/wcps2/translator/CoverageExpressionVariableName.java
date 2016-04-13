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
import petascope.wcps2.metadata.CoverageRegistry;

/**
 * Class to translate a coverage variable name
 * <code>
 * $c
 * </code>
 * translates to
 * <code>
 * c
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageExpressionVariableName extends CoverageExpression {

    /**
     * Constructor for the class
     *
     * @param coverageVariableName the variable name
     */
    public CoverageExpressionVariableName(String coverageVariableName, CoverageRegistry coverageRegistry) {
        this.coverageVariableName = coverageVariableName;
        if(coverageRegistry != null) {
            loadCoverage(coverageRegistry);
        }
    }

    @Override
    public String toRasql() {
        return coverageVariableName.replace("$", "");
    }

    @Override
    protected String nodeInformation() {
        return new StringBuilder("(").append(coverageVariableName).append(")").toString();
    }

    private void loadCoverage(CoverageRegistry registry) {
        if(registry.coverageAliasExists(coverageVariableName)) {
            setCoverage(registry.getCoverageByAlias(coverageVariableName));
        }
        else{
            //transient coverages
            setCoverage(Coverage.DEFAULT_COVERAGE);
        }
    }

    public String getCoverageVariableName() {
        return coverageVariableName;
    }

    public void setCoverageVariableName(String coverageVariableName) {
        this.coverageVariableName = coverageVariableName;
    }

    private String coverageVariableName;
}
