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
package petascope.wcps2.handler;

import petascope.wcps2.metadata.service.CoverageAliasRegistry;
import petascope.wcps2.metadata.service.CoverageRegistry;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps2.result.WcpsResult;
import petascope.wcps2.result.parameters.AxisIterator;
import petascope.wcps2.result.parameters.SubsetDimension;

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
 * or axis iterator:
 * for c in (mr) return encode(coverage cov $px x(0:20) values $px)
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageVariableNameHandler {

    public static WcpsResult handle(String coverageAlias, CoverageAliasRegistry coverageAliasRegistry,
                                    CoverageRegistry coverageRegistry, AxisIteratorAliasRegistry axisIteratorAliasRegistry) {
        String rasql;
        WcpsCoverageMetadata metadata;
        String coverageName = coverageAliasRegistry.getCoverageName(coverageAlias);
        // NOTE: if coverageName is null then the coverage alias points to non-existing coverage
        // assume it is an axisiterator
        if (coverageName == null) {
            AxisIterator axisIterator = axisIteratorAliasRegistry.getAxisIterator(coverageAlias);
            rasql = axisIterator.getRasqlAliasName() + "[" + axisIterator.getAxisIteratorOrder() + "]";
            //axis iterator, no coverage information, just pass the info up

            metadata = null;
        } else {
            // coverage does exist
            metadata = coverageRegistry.lookupCoverage(coverageName);
            rasql = coverageAlias.replace(SubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN, "");
        }

        WcpsResult result = new WcpsResult(metadata, rasql);
        return result;
    }
}
