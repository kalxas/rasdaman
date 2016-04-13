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

import petascope.wcps2.error.managed.processing.CoverageNotFoundException;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.metadata.CoverageRegistry;

/**
 * Translation node from wcps to rasql for the for clause.
 * Example:
 * <code>
 * for $c1 in COL1
 * </code>
 * translates to
 * <code>
 * COL1 as c1
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ForClause extends IParseTreeNode {

    /**
     * Constructor for the class
     *
     * @param coverageVariable the coverage variable name
     * @param coverageName     the name of the coverage
     * @param coverageRegistry the coverage registry
     */
    public ForClause(String coverageVariable, String coverageName, CoverageRegistry coverageRegistry) {
        this.coverageIterator = coverageVariable;
        this.coverageName = coverageName;
        this.coverageRegistry = coverageRegistry;
        coverageRegistry.addCoverageMapping(coverageName, coverageVariable);
    }


    @Override
    public String toRasql() {
        checkCorrectness();
        String translatedCoverageIterator = coverageIterator;
        //if the coverageVariable starts with $, remove it to make it valid rasql
        if (coverageIterator.startsWith(COVERAGE_VARIABLE_PREFIX)) {
            translatedCoverageIterator = coverageIterator.replace(COVERAGE_VARIABLE_PREFIX, "");
        }
        String template = TEMPLATE.replace("$iterator", translatedCoverageIterator)
            .replace("$collectionName", coverageName);
        return template;
    }

    /**
     * Checks if the coverage referenced exists and if not throws an error
     *
     * @throws WCPSProcessingError
     */
    private void checkCorrectness() {
        if (!coverageRegistry.coverageExists(coverageName)) {
            throw new CoverageNotFoundException(coverageName);
        }
    }

    @Override
    protected String nodeInformation() {
        return new StringBuilder("(").append(coverageIterator).append(",").append(coverageName).append(")").toString();
    }

    private final String coverageIterator;
    private final String coverageName;
    private final static String TEMPLATE = "$collectionName AS $iterator";
    private final static String COVERAGE_VARIABLE_PREFIX = "$";
    private final CoverageRegistry coverageRegistry;
}
