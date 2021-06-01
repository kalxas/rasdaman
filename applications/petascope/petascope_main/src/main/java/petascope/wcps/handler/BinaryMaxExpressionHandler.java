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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.WcpsResult;

/**
 *
 * e.g: max(test_mr, test_mr1)
 * or max(1, 2)
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class BinaryMaxExpressionHandler {
    
    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;

    public WcpsResult handle(WcpsResult firstCoverage, WcpsResult secondCoverage) {
        String template = TEMPLATE.replace("$coverageExp1", firstCoverage.getRasql())
                                  .replace("$coverageExp2", secondCoverage.getRasql());
        
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.getResultingMetadata(firstCoverage.getMetadata(), secondCoverage.getMetadata(), 
                                                                                         firstCoverage.getRasql(), secondCoverage.getRasql());
        
        if (firstCoverage.getMetadata() != null) {
            metadata.addToContributingMetadatasSet(firstCoverage.getMetadata(), firstCoverage.getRasql());
        } 
        
        if (secondCoverage.getMetadata() != null) {
            metadata.addToContributingMetadatasSet(secondCoverage.getMetadata(), secondCoverage.getRasql());
        }
        

        return new WcpsResult(metadata, template);
    }

    private final String TEMPLATE = "($coverageExp1 MAX $coverageExp2)";

}
