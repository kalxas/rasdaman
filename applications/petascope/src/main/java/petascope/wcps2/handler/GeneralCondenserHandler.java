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


import java.util.ArrayList;
import java.util.List;
import petascope.wcps2.metadata.model.Subset;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps2.metadata.service.RasqlTranslationService;
import petascope.wcps2.metadata.service.SubsetParsingService;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataService;
import petascope.wcps2.result.WcpsResult;
import petascope.wcps2.result.parameters.AxisIterator;
import petascope.wcps2.result.parameters.SubsetDimension;

/**
 * Translation node from wcps coverage list to rasql for the general condenser
 * Example:
 * <code>
 * CONDENSE +
 * OVER x x(0:100)
 * WHERE true
 * USING 2
 * </code>
 * translates to
 * <code>
 * CONDENSE +
 * OVER x in [0:100]
 * WHERE true
 * USING 2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class GeneralCondenserHandler {

    public static WcpsResult handle(String operation, ArrayList<AxisIterator> axisIterators, WcpsResult whereClause,
                                    WcpsResult using,
                                    AxisIteratorAliasRegistry axisIteratorAliasRegistry,
                                    WcpsCoverageMetadataService wcpsCoverageMetadataService,
                                    RasqlTranslationService rasqlTranslationService, SubsetParsingService subsetParsingService) {
        // contains subset dimension without "$"
        List<SubsetDimension> pureSubsetDimensions = new ArrayList<SubsetDimension>();
        // contains subset dimension with "$"
        List<SubsetDimension> axisIteratorSubsetDimensions = new ArrayList<SubsetDimension>();

        // All of the axis iterators uses the same rasql alias name (e.g: px)
        String rasqlAliasName = "";

        for (AxisIterator i : axisIterators) {
            String alias = i.getAliasName();
            SubsetDimension subsetDimension = i.getSubsetDimension();

            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = alias.replace(SubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN, "");
            }
            // Check if axis iterator's subset dimension which has the "$"
            if (i.getSubsetDimension().getStringRepresentation().contains(SubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN)) {
                axisIteratorSubsetDimensions.add(subsetDimension);
            } else {
                pureSubsetDimensions.add(subsetDimension);
            }
        }

        //create a coverage with the domain expressed in the condenser
        List<Subset> numericSubsets = subsetParsingService.convertToRawNumericSubsets(pureSubsetDimensions);
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.createCoverage(CONDENSER_TEMP_NAME, numericSubsets);

        String rasqlDomain = rasqlTranslationService.constructRasqlDomain(metadata.getAxes(), axisIteratorSubsetDimensions, axisIteratorAliasRegistry);
        String template = TEMPLATE.replace("$operation", operation)
                                  .replace("$iter", rasqlAliasName)
                                  .replace("$intervals", rasqlDomain)
                                  .replace("$using", using.getRasql());

        if (whereClause != null) {
            template = template.replace("$whereClause", whereClause.getRasql());
        } else {
            template = template.replace("$whereClause", "");
        }
        
        return new WcpsResult(using.getMetadata(), template);
    }

    private final static String CONDENSER_TEMP_NAME = "CONDENSE_TEMP";
    private final static String TEMPLATE = "CONDENSE $operation OVER $iter in [$intervals] $whereClause USING $using";
}
