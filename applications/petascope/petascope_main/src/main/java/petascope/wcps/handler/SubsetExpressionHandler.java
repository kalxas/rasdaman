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

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.StringUtil;
import petascope.wcps.exception.processing.CoverageAxisNotFoundExeption;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.DimensionIntervalList;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.metadata.service.CollectionAliasRegistry;
import petascope.wcps.metadata.service.CoverageAliasRegistry;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wcps.subset_axis.model.AxisIterator;

/**
 * Translation class for slice/trim expression in wcps.  <code>
 * $c[x(0:10),y(0:100)]
 * </code> translates to  <code>
 * c[0:10,0:100]
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class SubsetExpressionHandler extends AbstractOperatorHandler {

    @Autowired
    private WcpsCoverageMetadataGeneralService wcpsCoverageMetadataService;
    @Autowired
    private SubsetParsingService subsetParsingService;
    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    @Autowired
    private CoverageAliasRegistry coverageAliasRegistry;    
    @Autowired
    private CollectionAliasRegistry collectionAliasRegistry;
    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    @Autowired
    private AxisIteratorAliasRegistry axisIteratorAliasRegistry;
    
    
    public static final String OPERATOR = "domain subset";
    
    public WcpsResult handle(WcpsResult coverageExpression, DimensionIntervalList dimensionIntervalList) throws PetascopeException {
        WcpsResult wcpsResult = this.handle(coverageExpression, dimensionIntervalList, true);
        return wcpsResult;
    }

    public WcpsResult handle(WcpsResult coverageExpression, DimensionIntervalList dimensionIntervalList, boolean checkBounds) throws PetascopeException {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR); 

        String rasql = coverageExpression.getRasql();

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        List<WcpsSubsetDimension> subsetDimensions = dimensionIntervalList.getIntervals();

        String beforeCoverageId = metadata.getCoverageName();
        String aliasTmp = this.coverageAliasRegistry.getAliasByCoverageName(beforeCoverageId);
        if (aliasTmp != null) {
            aliasTmp = StringUtil.stripDollarSign(aliasTmp);
        }

        // c0 -> Pair<collectionName,coverageId>
        this.collectionAliasRegistry.add(aliasTmp, metadata.getRasdamanCollectionName(), metadata.getCoverageName());

        // Validate axis name before doing other processes.
        validateSubsets(metadata, subsetDimensions);

        // subset dimensions with numeric or timestamp bounds (Lat(0:4) or ansi("2006-01-01")).
        List<WcpsSubsetDimension> terminalSubsetDimensions = subsetParsingService.getTerminalSubsetDimensions(subsetDimensions);
        // subset dimension containing expressions as bounds (Lat($px)).
        List<WcpsSubsetDimension> expressionSubsetDimensions = subsetParsingService.getExpressionSubsetDimensions(subsetDimensions);

        // Only apply subsets if subset dimensions have numeric bounds.
        List<Subset> numericSubsets = subsetParsingService.convertToNumericSubsets(terminalSubsetDimensions, metadata.getAxes());
        
        String rasqlResult = "";
        
        // In case, condenser is used and axis iterators exist
        // for the case of virtual coverage, after axis iterator, then time axis must be trimmed and it can return in proper domains in USING expression
        // e.g: OVER $pt t (imageCrsdomain(c[time("2015":"2018")], time))
        for (Map.Entry<String, AxisIterator> entry : this.axisIteratorAliasRegistry.getAliasAxisIteratorMap().entrySet()) {
            // e.g: $pt[0]
            String alias = entry.getKey();
            AxisIterator axisIterator = entry.getValue();
            
            String bounds = axisIterator.getSubsetDimension().getStringBounds();
            String firstBound = bounds.split(":")[0];
            String secondsBound = bounds.split(":")[1];
            
            if (BigDecimalUtil.isNumber(firstBound) && BigDecimalUtil.isNumber(secondsBound)) {
                BigDecimal lowerBound = new BigDecimal(bounds.split(":")[0]);
                BigDecimal upperBound = new BigDecimal(bounds.split(":")[1]);
                NumericTrimming numericTrimming = new NumericTrimming(lowerBound, upperBound);

                String axisName = axisIterator.getSubsetDimension().getAxisName();
                String crs = axisIterator.getSubsetDimension().getCrs();

                Subset subset = new Subset(numericTrimming, crs, axisName);

                boolean exists = false;
                for (Subset subsetTmp : numericSubsets) {
                    if (CrsUtil.axisLabelsMatch(subsetTmp.getAxisName(), subset.getAxisName())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    for (Axis axis : metadata.getAxes()) {
                        if (CrsUtil.axisLabelsMatch(axis.getLabel(), subset.getAxisName()) 
                            && subset.getCrs() != null
                            && subset.getCrs().equals(CrsUtil.GRID_CRS)) {
                            numericSubsets.add(subset);
                            break;
                        }
                    }                
                }
            }
        }
        
        // Update the coverage expression metadata with the new subsets
        wcpsCoverageMetadataService.applySubsets(checkBounds, metadata, subsetDimensions, numericSubsets);
        
        if (!checkBounds) {
            // As WMS it can query with out of bounds for XY axes domains (e.g: request BBOX only intersects with a corner)
            wcpsCoverageMetadataService.adjustXYGeoGridBounds(metadata);
        }

        // now the metadata contains the correct geo and rasdaman subsets
        // NOTE: if subset dimension has "$" as axis iterator, just keep it and don't translate it to numeric as numeric subset.
        String dimensionIntervals = rasqlTranslationService.constructRasqlDomain(metadata.getSortedAxesByGridOrder(),
                                                                             expressionSubsetDimensions);
        String temp = TEMPLATE.replace("$covExp", rasql);
        rasqlResult = temp.replace("$dimensionIntervalList", dimensionIntervals);
        
        // NOTE: DimensionIntervalList with Trim expression can contain slicing as well (e.g: c[t(0), Lat(0:20), Long(30)])
        // then the slicing axis also need to be removed from coverage metadata.
        wcpsCoverageMetadataService.stripSlicingAxes(metadata, subsetDimensions);
        
        // NOTE: also filter local metadata child from input subsets to not contain all local metadata child list when encoding
        wcpsCoverageMetadataService.filterCoverageLocalMetadata(metadata, numericSubsets);

        // Fit to sample space for grid and geo bound of X-Y axes
        // NOTE: only fit if the axis is mentioned in the subsets (e.g: if coverage has 2 axes: Lat, Long), then c[Lat(20.5)] will only fit for Lat axis
        // don't change anything on Long axis
        subsetParsingService.fitToSampleSpaceRegularAxes(numericSubsets, metadata);
        
        // Update coverag's native CRS after subsetting (e.g: 3D -> 2D, then CRS=compound?time&4326 -> 4326)
        metadata.updateCrsUri();
        
        WcpsResult result = new WcpsResult(metadata, rasqlResult, this.collectionAliasRegistry);
        return result;
    }

    /**
     * Validate axis name from subset e.g: c[LAT(12)] is not valid and throw
     * exception
     *
     * @param subsetDimensions
     */
    private void validateSubsets(WcpsCoverageMetadata metadata, List<WcpsSubsetDimension> subsetDimensions) {
        String axisName = null;
        Iterator<WcpsSubsetDimension> iterator = subsetDimensions.iterator();
        
        while (iterator.hasNext()) {
            WcpsSubsetDimension wcpsSubsetDimension = iterator.next();
            axisName = wcpsSubsetDimension.getAxisName();
            
            boolean exist = false;
            
            for (Axis axis : metadata.getAxes()) {
                if (CrsUtil.axisLabelsMatch(axis.getLabel(), axisName)) {
                    exist = true;
                    break;
                }
            }

            if (!exist) {
                // subset does not contains valid axis name
                throw new CoverageAxisNotFoundExeption(axisName);
            }
        }
    }
    
    private final String TEMPLATE = "$covExp[$dimensionIntervalList]";
}
