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

import org.apache.commons.lang3.StringUtils;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.RasqlTranslationService;
import petascope.wcps2.metadata.service.SubsetParsingService;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataService;
import petascope.wcps2.result.WcpsResult;
import petascope.wcps2.result.parameters.AxisIterator;
import petascope.wcps2.result.parameters.SubsetDimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Translation node from wcps coverageConstant to  rasql
 * Example:
 * <code>
 * COVERAGE m
 * OVER x(0:1), y(2:4)
 * VALUES <1;2;3;4;5>
 * </code>
 * translates to
 * <code>
 * <[0:1,2:4] 1, 2; 3,4,5>
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageConstantHandler {

    public static WcpsResult handle(String coverageName, ArrayList<AxisIterator> axisIterators,
                                    List<String> constantList, WcpsCoverageMetadataService wcpsCoverageMetadataService,
                                    RasqlTranslationService rasqlTranslationService,
                                    SubsetParsingService subsetParsingService) {

        List<SubsetDimension> subsetDimensions = new ArrayList();
        for(AxisIterator axisIterator: axisIterators){
            subsetDimensions.add(axisIterator.getSubsetDimension());
        }
        String intervals = rasqlTranslationService.constructRasqlDomainFromSubsets(subsetDimensions);
        ArrayList<String> constantsByDimension = new ArrayList<String>();

        for (String constant:constantList) {
            constantsByDimension.add(constant);
        }
        String rasql = TEMPLATE.replace("$intervals", intervals).replace("$constants", StringUtils.join(constantsByDimension, ","));
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.createCoverage(coverageName,
                subsetParsingService.convertToRawNumericSubsets(subsetDimensions));
        WcpsResult result = new WcpsResult(metadata, rasql);
        return result;
    }

    private static final String TEMPLATE = "<[$intervals] $constants>";
}
