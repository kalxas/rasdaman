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
package petascope.wcps2.handler;

import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.result.WcpsResult;
import petascope.wcps2.subset_axis.model.AxisIterator;
import petascope.wcps2.subset_axis.model.WcpsSubsetDimension;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.wcps2.metadata.model.Subset;
import petascope.wcps2.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps2.metadata.service.RasqlTranslationService;
import petascope.wcps2.metadata.service.SubsetParsingService;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataGeneralService;

/**
 * Translation node from wcps coverage list to rasql for the coverage
 * constructor Example:  <code>
 * COVERAGE myCoverage
 * OVER x x(0:100)
 * VALUES 200
 * </code> translates to  <code>
 * MARRAY x in [0:100]
 * VALUES 200
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class CoverageConstructorHandler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    @Autowired
    private SubsetParsingService subsetParsingService;
    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    @Autowired
    private AxisIteratorAliasRegistry axisIteratorAliasRegistry;

    public WcpsResult handle(String coverageName, List<AxisIterator> axisIterators, WcpsResult values) {

        // contains subset dimension without "$"
        List<WcpsSubsetDimension> pureSubsetDimensions = new ArrayList<WcpsSubsetDimension>();
        // contains subset dimension with "$"
        List<WcpsSubsetDimension> axisIteratorSubsetDimensions = new ArrayList<WcpsSubsetDimension>();

        // All of the axis iterators uses the same rasql alias name (e.g: px)
        String rasqlAliasName = "";

        for (AxisIterator i : axisIterators) {
            String alias = i.getAliasName();
            WcpsSubsetDimension subsetDimension = i.getSubsetDimension();

            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = alias.replace(WcpsSubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN, "");
            }
            // Check if axis iterator's subset dimension which has the "$"
            if (i.getSubsetDimension().getStringRepresentation().contains(WcpsSubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN)) {
                axisIteratorSubsetDimensions.add(subsetDimension);
            } else {
                pureSubsetDimensions.add(subsetDimension);
            }
        }

        List<Subset> numericSubsets = subsetParsingService.convertToRawNumericSubsets(pureSubsetDimensions);
        WcpsCoverageMetadata metadata = wcpsCoverageMetadataService.createCoverage(coverageName, numericSubsets);

        String rasqlDomain = rasqlTranslationService.constructRasqlDomain(metadata.getSortedAxesByGridOrder(), axisIteratorSubsetDimensions, axisIteratorAliasRegistry);
        String template = TEMPLATE.replace("$iter", rasqlAliasName)
                .replace("$intervals", rasqlDomain)
                .replace("$values", values.getRasql());
        return new WcpsResult(metadata, template);
    }

    private final String TEMPLATE = "MARRAY $iter in [$intervals] VALUES $values";
}
