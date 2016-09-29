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

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import petascope.exceptions.PetascopeException;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.Subset;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps2.metadata.service.RasqlTranslationService;
import petascope.wcps2.metadata.service.SubsetParsingService;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataService;
import petascope.wcps2.result.WcpsResult;
import petascope.wcps2.result.parameters.DimensionIntervalList;
import petascope.wcps2.result.parameters.SubsetDimension;

/**
 * Translation class for slicing expression in wcps.
 * <code>
 * $c[i(0),j(100)]
 * </code>
 * <p/>
 * translates to
 * <p/>
 * <code>
 * c[0,100]
 * </code>
 *
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class SliceExpressionHandler {

    public static WcpsResult handle(WcpsResult coverageExpression, DimensionIntervalList dimensionIntervalList,
                                    AxisIteratorAliasRegistry axisIteratorAliasRegistry,
                                    WcpsCoverageMetadataService wcpsCoverageMetadataService,
                                    RasqlTranslationService rasqlTranslationService, SubsetParsingService subsetParsingService) throws PetascopeException {

        String rasql = coverageExpression.getRasql();
        String template = TEMPLATE.replace("$covExp", rasql);

        WcpsCoverageMetadata metadataInput = coverageExpression.getMetadata();

        List<SubsetDimension> subsetDimensions = dimensionIntervalList.getIntervals();

        // subset dimension without "$"
        List<SubsetDimension> pureSubsetDimensions = subsetParsingService.getPureSubsetDimensions(subsetDimensions);
        // subset dimension with "$"
        List<SubsetDimension> axisIteratorSubsetDimensions = subsetParsingService.getAxisIteratorSubsetDimensions(subsetDimensions);

        // Only apply subsets if subset dimensions don't contain the "$"
        List<Subset> numericSubsets = subsetParsingService.convertToNumericSubsets(pureSubsetDimensions, metadataInput);

        // Update the coverag expression metadata with the new subsets
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.applySubsets(true, metadataInput, numericSubsets);

        //now the metadata contains the correct geo and rasdaman subsets
        // NOTE: if subset dimension has "$" as axis iterator, just keep it and don't translate it to numeric as numeric subset.


        String rasqlSubset = "";
        /*String dimensions = rasqlTranslationService.constructRasqlDomain(metadataInput.getAxes(),
                                                                             axisIteratorSubsetDimensions, axisIteratorAliasRegistry);
        rasqlSubset = template.replace("$dimensionIntervalList", dimensions);*/
        // NOTE: in case of coverage constant e.g: <[-1:1,-1:1],-1,0,1,....> it should not replace the interval inside the "< >"
        if (rasql.contains("<") || !rasql.contains("[")) {
            String dimensions = rasqlTranslationService.constructRasqlDomain(metadataInput.getAxes(),
                                                                             axisIteratorSubsetDimensions, axisIteratorAliasRegistry);
            rasqlSubset = template.replace("$dimensionIntervalList", dimensions);
        } else {
            // update the interval of the existing expression in template string
            // e.g: slice(c[0:5,0:100,0:230], {89}) -> c[0:5,0:100,89]
            // need to replace the interval correctly
            String tmp = rasql.substring(rasql.indexOf("[") + 1, rasql.indexOf("]"));
            String[] intervals = tmp.split(",");

            String axisName = subsetDimensions.get(0).getAxisName();
            Axis axis = metadata.getAxisByName(axisName);
            int axisOrder = axis.getRasdamanOrder();

            String dimension = rasqlTranslationService.constructRasqlDomain(axis, axisIteratorSubsetDimensions, axisIteratorAliasRegistry);
            intervals[axisOrder] = dimension;

            // 0:5,0:100,89
            String intervalsStr = "[" + StringUtils.join(intervals, ",") + "]";
            rasqlSubset = rasql.replaceAll("\\[.*?\\]", intervalsStr);
        }

        //now remove the sliced axis from metadata
        // then the slicing axis also need to be removed from coverage metadata.
        wcpsCoverageMetadataService.stripSlicingAxes(metadata, axisIteratorSubsetDimensions);

        WcpsResult result = new WcpsResult(metadata, rasqlSubset);

        return result;
    }

    private final static String TEMPLATE = "$covExp[$dimensionIntervalList]";
}
