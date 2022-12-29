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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.CoverageAxisNotFoundExeption;
import petascope.wcps.metadata.model.Axis;
import petascope.core.BoundingBox;
import petascope.core.Pair;
import petascope.util.StringUtil;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.RasqlTranslationService;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.metadata.service.WcpsCoverageMetadataGeneralService;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.DimensionIntervalList;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps.metadata.service.CollectionAliasRegistry;
import petascope.wcps.metadata.service.CoverageAliasRegistry;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.subset_axis.model.AxisIterator;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;
import petascope.wcps.metadata.service.WMSSubsetDimensionsRegistry;
import petascope.wms.handlers.kvp.KVPWMSGetMapHandler;
import static petascope.wms.handlers.service.WMSGetMapStyleService.WMS_VIRTUAL_LAYER_EXPECTED_BBOX;
import static petascope.wms.handlers.service.WMSGetMapStyleService.WMS_VIRTUAL_LAYER_EXPECTED_HEIGHT;
import static petascope.wms.handlers.service.WMSGetMapStyleService.WMS_VIRTUAL_LAYER_EXPECTED_OUTPUT_CRS;
import static petascope.wms.handlers.service.WMSGetMapStyleService.WMS_VIRTUAL_LAYER_EXPECTED_WIDTH;

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
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SubsetExpressionHandler extends AbstractOperatorHandler {
    
    private static Logger log = LoggerFactory.getLogger(SubsetExpressionHandler.class);

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
    @Autowired
    private WMSSubsetDimensionsRegistry wmsSubsetDimensionsRegistry;
    
    
    public static final String OPERATOR = "domain subset";
    
    public WcpsResult handle(WcpsResult coverageExpression, DimensionIntervalList dimensionIntervalList) throws PetascopeException {
        WcpsResult wcpsResult = this.handle(coverageExpression, dimensionIntervalList, true);
        return wcpsResult;
    }

    public WcpsResult handle(WcpsResult coverageExpression, DimensionIntervalList dimensionIntervalList, boolean checkBounds) throws PetascopeException {
        
        checkOperandIsCoverage(coverageExpression, OPERATOR); 

        String rasql = coverageExpression.getRasql();

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        
        List<WcpsSubsetDimension> subsetDimensions = new ArrayList<>();
        if (dimensionIntervalList != null) {
            subsetDimensions = dimensionIntervalList.getIntervals();
        }
        
        if (!this.wmsSubsetDimensionsRegistry.getMap().isEmpty()) {
            log.info("#### I have some values");
            String layerName = metadata.getCoverageName();
            List<WcpsSubsetDimension> wmsLayerSubsetDimensions = this.wmsSubsetDimensionsRegistry.getSubsetDimensions(layerName);
            
            if (wmsLayerSubsetDimensions != null) {
            
                for (WcpsSubsetDimension wmsLayerSubsetDimension : wmsLayerSubsetDimensions) {
                    boolean axisExists = false;
                    String wmsLayerAxisLabel = wmsLayerSubsetDimension.getAxisName();

                    for (WcpsSubsetDimension fixedSubsetDimension : subsetDimensions) {
                        String fixedSubsetDimensionAxisLabel = fixedSubsetDimension.getAxisName();

                        if (wmsLayerAxisLabel.equals(fixedSubsetDimensionAxisLabel)) {
                            axisExists = true;
                            break;
                        }
                    }

                    if (!axisExists) {
                        subsetDimensions.add(wmsLayerSubsetDimension);
                    }
                }   
                
            }
            
        }
        
        
        Pair<Integer, Integer> wmsExpectedXYGridDomains = this.parseExpectedOutputForWMSVirtualLayer(coverageExpression.getMetadata(), subsetDimensions);
        if (wmsExpectedXYGridDomains != null) {

            String beforeCoverageId = metadata.getCoverageName();
            BoundingBox expectedOutputBBox = null;
            metadata = selectDownscaledCollectionForWMS(metadata, subsetDimensions, wmsExpectedXYGridDomains);
            
            String afterCoverageId = metadata.getCoverageName();
            
            // e.g before it is covearge id: downscaled level 1, after it is coverage id: downscaled level 8 due to scale() is used
            rasql = rasql.replace(beforeCoverageId, afterCoverageId); 
            
            String aliasTmp = this.coverageAliasRegistry.getAliasByCoverageName(beforeCoverageId);
	    if (aliasTmp != null) {
	        aliasTmp = StringUtil.stripDollarSign(aliasTmp);
   	    }

            // e.g. c0 Important (!)
            rasql = aliasTmp;
            
            WcpsCoverageMetadata pyramidMemberCoverageMetadata = this.wcpsCoverageMetadataTranslator.translate(afterCoverageId);
            for (WcpsSubsetDimension dimension : subsetDimensions) {
                String axisLabel = dimension.getAxisName();
                Axis axis = pyramidMemberCoverageMetadata.getAxisByName(axisLabel);
                
                if (axis.isXAxis() || axis.isYAxis()) {
                    WcpsTrimSubsetDimension trimDimension = (WcpsTrimSubsetDimension) dimension;
                    
                    BigDecimal adjustedLowerBound = null, adjustedUpperBound = null;
  
                    if (expectedOutputBBox != null) {
                        if (axis.isXAxis()) {
                            adjustedLowerBound = expectedOutputBBox.getXMin();
                            adjustedUpperBound = expectedOutputBBox.getXMax();
                        } else {
                            adjustedLowerBound = expectedOutputBBox.getYMin();
                            adjustedUpperBound = expectedOutputBBox.getYMax();
                        }
                    } else {
                        adjustedLowerBound = new BigDecimal(trimDimension.getLowerBound());
                        adjustedUpperBound = new BigDecimal(trimDimension.getUpperBound());
                    }
                    
                    if (new BigDecimal(trimDimension.getLowerBound()).compareTo(axis.getGeoBounds().getLowerLimit()) < 0) {
                        adjustedLowerBound = axis.getGeoBounds().getLowerLimit();
                    }

                    if (new BigDecimal(trimDimension.getUpperBound()).compareTo(axis.getGeoBounds().getUpperLimit()) > 0) {
                        adjustedUpperBound = axis.getGeoBounds().getUpperLimit();
                    }

                    trimDimension.setLowerBound(adjustedLowerBound.toPlainString());
                    trimDimension.setUpperBound(adjustedUpperBound.toPlainString());
                }
            }
            
            // c0 -> Pair<collectionName,coverageId>
            this.collectionAliasRegistry.add(aliasTmp, metadata.getRasdamanCollectionName(), metadata.getCoverageName());
        }

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
                            && (subset.getCrs().equals(CrsUtil.GRID_CRS))
                            ) {
                            numericSubsets.add(subset);
                            break;
                        }
                    }                
                }
            }
        }
        
        // Update the coverage expression metadata with the new subsets
        wcpsCoverageMetadataService.applySubsets(checkBounds, checkBounds, metadata, subsetDimensions, numericSubsets);
        
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
    
   
    /**
     * This is used in case of WMS with WCPS style, a WcpsCoverageMetadata needs to adjust accordingly the downscaled collection is used
     * underneath which depends on the given XY geo domains.
     */
    private WcpsCoverageMetadata selectDownscaledCollectionForWMS(WcpsCoverageMetadata metadata, List<WcpsSubsetDimension> wcpsSubsetDimensions,
                                               Pair<Integer, Integer> wmsExpectedXYGridDomainsPair) throws PetascopeException {
        Pair<BigDecimal, BigDecimal> geoSubsetX = null;
        Pair<BigDecimal, BigDecimal> geoSubsetY = null;
        List<Axis> xyAxes = metadata.getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);
        
        for (WcpsSubsetDimension wcpsSubsetDimension : wcpsSubsetDimensions) {
            if (CrsUtil.axisLabelsMatch(wcpsSubsetDimension.getAxisName(), axisX.getLabel())) {
                if (wcpsSubsetDimension instanceof WcpsTrimSubsetDimension) {
                    BigDecimal lowerBound = new BigDecimal(((WcpsTrimSubsetDimension) wcpsSubsetDimension).getLowerBound());
                    BigDecimal upperBound = new BigDecimal(((WcpsTrimSubsetDimension) wcpsSubsetDimension).getUpperBound());
                    geoSubsetX = new Pair<>(lowerBound, upperBound);
                }
            } else if (CrsUtil.axisLabelsMatch(wcpsSubsetDimension.getAxisName(), axisY.getLabel())) {
                if (wcpsSubsetDimension instanceof WcpsTrimSubsetDimension) {
                    BigDecimal lowerBound = new BigDecimal(((WcpsTrimSubsetDimension) wcpsSubsetDimension).getLowerBound());
                    BigDecimal upperBound = new BigDecimal(((WcpsTrimSubsetDimension) wcpsSubsetDimension).getUpperBound());
                    geoSubsetY = new Pair<>(lowerBound, upperBound);
                }
            }
        }
        
        List<WcpsSubsetDimension> nonXYSubsetDimensions = new ArrayList<>();
        WcpsCoverageMetadata updateMetadata = this.wcpsCoverageMetadataTranslator.createForDownscaledLevelByGeoXYSubsets(metadata, geoSubsetX, geoSubsetY,
                                                                                              wmsExpectedXYGridDomainsPair.fst, wmsExpectedXYGridDomainsPair.snd, nonXYSubsetDimensions);
        return updateMetadata;
    }
    
    /**
     * It is used only for WMS with WCPS fragment style and for virtual layer.
     * In this case, the expected BBOX and width and height are pre-set by WMS style handler
     * and WCPS subset handler for virtual coverage should just use these defined values.
     */
    private Pair<Integer, Integer> parseExpectedOutputForWMSVirtualLayer(WcpsCoverageMetadata metadata, List<WcpsSubsetDimension> wcpsSubsetDimensions) {
        Iterator<WcpsSubsetDimension> iterator = wcpsSubsetDimensions.iterator();
        
        int width = 0;
        int height = 0;
        
        BoundingBox expectedBBox = null;
        
        while (iterator.hasNext()) {
            WcpsSubsetDimension wcpsSubsetDimension = iterator.next();
            String axisLabel = wcpsSubsetDimension.getAxisName();
            
            if (axisLabel.equals(WMS_VIRTUAL_LAYER_EXPECTED_BBOX)) {
                expectedBBox = BoundingBox.parse(wcpsSubsetDimension.getStringBounds());
                iterator.remove();
            } else if (axisLabel.equals(WMS_VIRTUAL_LAYER_EXPECTED_WIDTH)) {
                width = Integer.parseInt(wcpsSubsetDimension.getStringBounds());
                iterator.remove();
            } else if (axisLabel.equals(WMS_VIRTUAL_LAYER_EXPECTED_HEIGHT)) {
                height = Integer.parseInt(wcpsSubsetDimension.getStringBounds());
                iterator.remove();
            }
        }
        if (width > 0 && height > 0) {
            return new Pair<>(width, height);
        }
        
        return null;
        
    }
    
    private final String TEMPLATE = "$covExp[$dimensionIntervalList]";
}
