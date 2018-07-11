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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import org.apache.commons.lang3.StringUtils;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AxisIterator;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;

/**
 * Translation node from wcps coverageConstant to rasql Example:  <code>
 * COVERAGE m
 * OVER x(0:1), y(2:4)
 * VALUES <1;2;3;4;5>
 * </code> translates to
 * <code>
 * <[0:1,2:4] 1, 2; 3,4,5>
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class CoverageConstantHandler extends AbstractOperatorHandler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    @Autowired
    private SubsetParsingService subsetParsingService;

    public WcpsResult handle(String coverageName, ArrayList<AxisIterator> axisIterators,
            List<String> constantList) {

        List<WcpsSubsetDimension> subsetDimensions = new ArrayList();
        for (AxisIterator axisIterator : axisIterators) {
            subsetDimensions.add(axisIterator.getSubsetDimension());
        }
        String intervals = rasqlTranslationService.constructRasqlDomainFromSubsets(subsetDimensions);
        ArrayList<String> constantsByDimension = new ArrayList<>();

        for (String constant : constantList) {
            constantsByDimension.add(constant);
        }
        String rasql = TEMPLATE.replace("$intervals", intervals).replace("$constants", StringUtils.join(constantsByDimension, ","));
        List<Subset> subsets = subsetParsingService.convertToRawNumericSubsets(subsetDimensions);
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.createCoverage(coverageName, subsets);
        WcpsResult result = new WcpsResult(metadata, rasql);
        return result;
    }

    private final String TEMPLATE = "<[$intervals] $constants>";
}
