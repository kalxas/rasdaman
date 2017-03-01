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

import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataService;
import petascope.wcps2.result.WcpsResult;

/**
 * Translation node from wcps coverage list to rasql for the LogicalCOverageExpressions
 * Example:
 * <code>
 * $c1 OR $c2
 * </code>
 * translates to
 * <code>
 * c1 OR c2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class BinaryCoverageExpressionHandler {

    public static WcpsResult handle(WcpsResult firstCoverage, String operator, WcpsResult secondCoverage,
                                    WcpsCoverageMetadataService metadataService) throws WCPSProcessingError {
        //create the resulting rasql string
        String rasql = firstCoverage.getRasql() + " " + operator + " " +  secondCoverage.getRasql();
        //create the resulting metadata
        WcpsCoverageMetadata metadata = metadataService.getResultingMetadata(firstCoverage.getMetadata(), secondCoverage.getMetadata());
        WcpsResult result = new WcpsResult(metadata, rasql);
        return result;
    }
}
