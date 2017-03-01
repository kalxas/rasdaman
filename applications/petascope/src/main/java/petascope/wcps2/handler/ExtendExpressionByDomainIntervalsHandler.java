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

import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;

/**
 * Class to translate a scale wcps expression into rasql
 * <code>
 *    EXTEND($coverageExpression, [$dimensionIntervalList])
 *    e.g: extend(c[t(0)], { imageCrsdomain(Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)) })
 * </code>
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ExtendExpressionByDomainIntervalsHandler {

    public static WcpsResult handle(WcpsResult coverageExpression, WcpsMetadataResult wcpsMetadataResult, String dimensionIntervalList) {

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // scale(coverageExpression, {domainIntervals})

        // it will not get all the axis to build the intervals in case of (extend() and scale())
        String rasql = TEMPLATE.replace("$coverage", coverageExpression.getRasql())
                       .replace("$intervalList", dimensionIntervalList);

        return new WcpsResult(metadata, rasql);
    }

    //in case we will need to handle scale with a factor, use a method such as below
    //public static WcpsResult handle(WcpsResult coverageExpression, BigDecimal scaleFactor)

    private static final String TEMPLATE = "EXTEND($coverage, [$intervalList])";
}
