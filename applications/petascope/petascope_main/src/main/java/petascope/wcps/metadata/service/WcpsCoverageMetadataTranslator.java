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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.metadata.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.rasdaman.domain.cis.*;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.core.Pair;
import org.rasdaman.admin.pyramid.service.PyramidService;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import static petascope.wcps.handler.GeneralCondenserHandler.USING;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * This class translates different types of metadata into WcpsCoverageMetadata.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class WcpsCoverageMetadataTranslator {

    @Autowired
    private CoverageRepositoryService persistedCoverageService;
    @Autowired
    private PyramidService pyramidService;
    @Autowired
    private CoordinateTranslationService coordinateTranslationService;
    @Autowired
    private CoverageAliasRegistry coverageAliasRegistry;
    @Autowired
    private CollectionAliasRegistry collectionAliasRegistry;
    
    @Autowired
    private RasqlTranslationService rasqlTranslationService;
    
    @Autowired
    private UsingCondenseRegistry usingCondenseRegistry;
    
    public WcpsCoverageMetadataTranslator() {

    }
    
    /**
     * Create a WCPS coverage metadata object from a Coverage CIS 1.1 model in
     * database.
     *
     * @param coverageId
     * @return
     * @throws PetascopeException
     * @throws SecoreException
     */
    public WcpsCoverageMetadata create(String coverageId) throws PetascopeException {
        // Only supports GeneralGridCoverage now
        
        Coverage coverage = this.persistedCoverageService.readCoverageFullMetadataByIdFromCache(coverageId);
        
        List<GeoAxis> geoAxes = ((GeneralGridCoverage) coverage).getGeoAxes();
        List<IndexAxis> indexAxes = ((GeneralGridCoverage) coverage).getIndexAxes();

        // wcpsCoverageMetadata axis
        String coverageCRS = coverage.getEnvelope().getEnvelopeByAxis().getSrsName();
        List<Axis> axes = buildAxes(coverageCRS, geoAxes, indexAxes);
        List<Axis> originalAxes = buildAxes(coverageCRS, geoAxes, indexAxes);
        List<RangeField> rangeFields = buildRangeFields(coverage.getRangeType().getDataRecord().getFields());
        // parse extra metadata of coverage to map
        String extraMetadata = coverage.getMetadata();
        List<List<NilValue>> nilValues = coverage.getNilValues();
        
        String rasdamanCollectionName = coverage.getRasdamanRangeSet().getCollectionName();
        
        List<String> axisCrsUris = new ArrayList<>();
        for (Axis axis : axes) {
            axisCrsUris.add(axis.getNativeCrsUri());
        }
        
        String crsUri = CrsUtil.CrsUri.createCompound(axisCrsUris);

        WcpsCoverageMetadata wcpsCoverageMetadata = new WcpsCoverageMetadata(coverageId, rasdamanCollectionName,
                                                        coverage.getCoverageType(), axes, crsUri, 
                                                        rangeFields, nilValues, extraMetadata, originalAxes);

	wcpsCoverageMetadata.setDecodedFilePath(coverage.getRasdamanRangeSet().getDecodeExpression());
        return wcpsCoverageMetadata;
    }

    /**
     * Translate a persisted coverage in database by coverageId to a
     * WcpsCoverageMetadata. NOTE: if a WcpsCoverageMetadata is already
     * translated, it will get from cache.
     *
     * @param coverageId
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public WcpsCoverageMetadata translate(String coverageId) throws PetascopeException {
        // NOTE: cannot cache a translated WCPS coverage metadata as a WCPS request can slice, trim to the coverage metadata which is stored in cache
        // and the next request will not have the full metadata as the previous one which is big error.
        WcpsCoverageMetadata wcpsCoverageMetadata = this.create(coverageId);

        return wcpsCoverageMetadata;
    }
    
    /**
     * If a 2D coverage imported with downscaled collections, then it can scale the grid domains on the first parameter of scale()
     * by the value of selected downscale collection and update coverage name in FROM clause to downscaled collection name.
     * Based on width and height, calculate the downscaledLevel should be applied on grid XY bounds of original WCPS coverage object
     * e.g: Lat axis with grid bound (0:10) will change to (0/2:10/2) = (0:5)
     */
    public WcpsCoverageMetadata applyDownscaledLevelOnXYGridAxesForScale(WcpsResult coverageExpression, 
                                                         WcpsCoverageMetadata wcpsCoverageMetadataBase, List<Subset> numericSubsets) throws PetascopeException {
        // @TODO: NOTE: it needs to find out which pyramid member should be selected before subsetting in scale() operator
        // e.g. for c in (base) return encode(
        //      scale(c[ansi("2016-01-01":"2020-01-01")], {Lat:"CRS:1"(0:30), Long:"CRS:1"(0:20)}), "csv")
        // Here, with ANTLR4, c[ansi("2016-01-01":"2020-01-01")] will be processed first by using base coverage, 
        // instead of coverage pyramid pyramid_2 with the given target grid domain for Lat and Long axes.
        // This requires ticket: https://projects.rasdaman.com/ticket/299
        
        WcpsCoverageMetadata result = wcpsCoverageMetadataBase;
        
        if (wcpsCoverageMetadataBase.hasXYAxes()) {
            
            List<Axis> xyAxes = wcpsCoverageMetadataBase.getXYAxes();
            Axis subsettedAxisX = xyAxes.get(0);
            Axis subsettedAxisY = xyAxes.get(1);
            
            Pair<BigDecimal, BigDecimal> geoSubsetX = new Pair<>(subsettedAxisX.getGeoBounds().getLowerLimit(), subsettedAxisX.getGeoBounds().getUpperLimit());
            Pair<BigDecimal, BigDecimal> geoSubsetY = new Pair<>(subsettedAxisY.getGeoBounds().getLowerLimit(), subsettedAxisY.getGeoBounds().getUpperLimit());
            
            int width = subsettedAxisX.getGridBounds().getUpperLimit().intValue() - subsettedAxisY.getGridBounds().getLowerLimit().intValue() + 1;
            int height = subsettedAxisY.getGridBounds().getUpperLimit().intValue() - subsettedAxisY.getGridBounds().getLowerLimit().intValue() + 1;
            
            List<WcpsSubsetDimension> nonXYSubsetDimensions = new ArrayList<>();
            List<Axis> nonXYAxes = wcpsCoverageMetadataBase.getNonXYAxes();

            for (Subset numericSubset : numericSubsets) {
                // In case X or Y axis is speficied in the target scaling of scale() operator
                if (CrsUtil.axisLabelsMatch(subsettedAxisX.getLabel(), numericSubset.getAxisName())) {
                    width = numericSubsets.get(0).getNumericSubset().getUpperLimit().toBigInteger().intValue() - numericSubsets.get(0).getNumericSubset().getLowerLimit().toBigInteger().intValue();
                }
                
                if (CrsUtil.axisLabelsMatch(subsettedAxisY.getLabel(), numericSubset.getAxisName())) {
                    height = numericSubsets.get(1).getNumericSubset().getUpperLimit().toBigInteger().intValue() - numericSubsets.get(1).getNumericSubset().getLowerLimit().toBigInteger().intValue();
                }
                
                for (Axis nonXYAxis : nonXYAxes) {
                    String axisLabel = nonXYAxis.getLabel();
                    
                    if (CrsUtil.axisLabelsMatch(axisLabel, numericSubset.getAxisName())) {
                        String geoLowerBound = numericSubset.getNumericSubset().getLowerLimit().toPlainString();
                        String geoUpperBound = numericSubset.getNumericSubset().getUpperLimit().toPlainString();
                        
                        WcpsSubsetDimension nonXYSubsetDimension = new WcpsTrimSubsetDimension(axisLabel, nonXYAxis.getNativeCrsUri(), geoLowerBound, geoUpperBound);
                        nonXYSubsetDimensions.add(nonXYSubsetDimension);
                        break;
                    }
                }
            }
            
            List<Subset> trimmingSubsets = new ArrayList<>();
            for (Axis axis : wcpsCoverageMetadataBase.getAxes()) {
                NumericTrimming numericTrimming = new NumericTrimming(axis.getGeoBounds().getLowerLimit(), axis.getGeoBounds().getUpperLimit());
                trimmingSubsets.add(new Subset(numericTrimming, axis.getNativeCrsUri(), axis.getLabel()));
            }
            
            // NOTE: only support scale() with pyramid on a single coverage expression
            // @TODO: https://projects.rasdaman.com/ticket/308 to support scale on pyramid member of a virtual coverage // -- rasdaman enterprise
                
            if (wcpsCoverageMetadataBase.isSingleCoverageExpression()) {
                String contributingCoverageId = wcpsCoverageMetadataBase.getCoverageName();

                GeneralGridCoverage baseCoverage = (GeneralGridCoverage) this.persistedCoverageService.readCoverageFullMetadataByIdFromCache(contributingCoverageId);
                CoveragePyramid coveragePyramid = this.pyramidService.getSuitableCoveragePyramidForScaling(baseCoverage, geoSubsetX, geoSubsetY,
                                                                                                        subsettedAxisX, subsettedAxisY,
                                                                                                        width, height, nonXYSubsetDimensions);

                // e.g. test_pyramid_8
                WcpsCoverageMetadata wcpsCoverageMetadataPyramid = this.translate(coveragePyramid.getPyramidMemberCoverageId());
                result = wcpsCoverageMetadataPyramid;
                
                // Remove any stripped axes of input coverage in pyramid member coverage (e.g: slicing in time axis of a virtual coverage, 
                // then the pyramid member of it also needs to remove this time axis)
                this.applyGeoSubsetOnPyramidCoverage(wcpsCoverageMetadataBase, wcpsCoverageMetadataPyramid);
  
                if (!wcpsCoverageMetadataPyramid.getCoverageName().equals(wcpsCoverageMetadataBase.getCoverageName())) {
                    // remove any collection alias (from base coverages before scaling) which don't exist in the generated rasql
                    Iterator<Pair<String,String>> iterator = this.collectionAliasRegistry.getAliasMap().values().iterator();
                    while (iterator.hasNext()) {
                        // collectionName, coverageId
                        Pair<String, String> pair = iterator.next();
                        if (pair.snd.equals(wcpsCoverageMetadataBase.getCoverageName())) {
                            iterator.remove();
                        }
                    }
                }

            }
        }
        
        return result;
    }
    
    /**
     * For example, metadata object is sliced / trimmed over the axes, while the metadataPyramid is just read from cache with the full geo extents
     */
    private void applyGeoSubsetOnPyramidCoverage(WcpsCoverageMetadata metadata, WcpsCoverageMetadata metadataPyramid) {
        Iterator<Axis> axesPyramidIterator = metadataPyramid.getAxes().iterator();
        while (axesPyramidIterator.hasNext()) {
            Axis axisPyramid = axesPyramidIterator.next();
            String axisPyramidLalbel = axisPyramid.getLabel();

            if (!metadata.axisExists(axisPyramidLalbel)) {
                axesPyramidIterator.remove();
            } else {
                Axis axisBase = metadata.getAxisByName(axisPyramidLalbel);
                axisPyramid.setGeoBounds(axisBase.getGeoBounds());
                axisPyramid.setOrigin(axisBase.getOrigin());                
                
                if (axisPyramid instanceof IrregularAxis) {
                    List<BigDecimal> baseDirections = ((IrregularAxis)axisBase).getDirectPositions();
                    ((IrregularAxis)axisPyramid).setDirectPositions(baseDirections);
                }
            }
        }
    }
    
    /**
     * For example scale(c[Lat(20:30), Long(40:50), {Lat:"CRS:1"(0:300), Long:"CRS:1"(400:500)})
     * c is a coverage with pyramid levels 1 and level 8
     * before scale(), rasql is c[0:3000, 0:5000] as it doesn't know about scale() and uses level 1 grid domains
     * after scale(), based on the second parameter of scale(), it knows, c should be used with level 8 instead
     * Hence, the grid domains must be updated accordingly c[0:3000, 0:5000] -> c[0:375, 0:625]
     */
    private String updateGridDomainsForNormalCoverage(String rasql, GeneralGridCoverage pyramidMemberCoverage, CoveragePyramid coveragePyramid) {
        
        // axis labels + scale factor for axis
        List<Pair<String, BigDecimal>> scaleFactorsByGridOder = this.pyramidService.sortScaleFactorsByGridOderWithAxisLabel(pyramidMemberCoverage, coveragePyramid.getScaleFactorsList());

        // In case of using condenser as first paramter of scale(), it should not downscale domain of iterators
        int indexOfUsing = rasql.indexOf(USING);
        String result = "";
        for (int i = 0; i < rasql.length(); i++){
            char c = rasql.charAt(i);
            String value = String.valueOf(c);
            int numberOfOpenBrackets = 0;
            if (c == '[') {
                value = "";
                numberOfOpenBrackets++;
                for (int j = i + 1; j < rasql.length(); j++) {
                    char d = rasql.charAt(j);
                    if (d == '[') {
                        numberOfOpenBrackets++;
                    }
                    if (d == ']') {
                        numberOfOpenBrackets--;
                        i = j;
                        if (numberOfOpenBrackets == 0) {
                            break;
                        }
                    }
                    value += d;
                }

                // e.g: CONDENSE min OVER ts in [0:5]  USING c[ts[0],0:99,0:99], only update grid domains after USING (not [0:5])
                if (indexOfUsing < i) {
                    List<String> boundsList = new ArrayList<>();
                    String[] parts = value.split(",");
                    for (int j = 0; j < parts.length; j++) {
                        String part = parts[j];
                        String updatedPart = part;
                        if (part.contains(":")) {
                            String[] bounds = part.split(":");
                            BigDecimal lowerBound = new BigDecimal(bounds[0].trim());
                            BigDecimal upperBound = new BigDecimal(bounds[1].trim());

                            BigDecimal scaleFactor = scaleFactorsByGridOder.get(j).snd;
                            String appliedLowerBound = BigDecimalUtil.divide(lowerBound, scaleFactor).toBigInteger().toString();
                            String appliedUpperBound = BigDecimalUtil.divide(upperBound, scaleFactor).toBigInteger().toString();
                            updatedPart = appliedLowerBound + ":" + appliedUpperBound;
                        }
                        boundsList.add(updatedPart);
                    }
                    value = ListUtil.join(boundsList, ",");
                }
                value = "[" + value + "]";
            }
            result += value;
        }
        
        return result;
    }
    
    /**
     * Create a WcpsCoverageMetadata metadata object depending on the input GeoXY axes domains. 
     * This method will check which Rasdaman downscaled collection the metadata object should contain and also the XY grid domains accordingly.
     * 
     * NOTE: It needs to select a suitable downscaled collection based on geo XY subsets to help reduce the time to process Rasql on lower resolution collection.
     */
    public WcpsCoverageMetadata createForDownscaledLevelByGeoXYSubsets(WcpsCoverageMetadata metadata, 
            Pair<BigDecimal, BigDecimal> geoSubsetX, Pair<BigDecimal, BigDecimal> geoSubsetY, Integer width, Integer height, 
            List<WcpsSubsetDimension> nonXYSubsetDimensions) throws PetascopeException {
        
        GeneralGridCoverage baseCoverage = (GeneralGridCoverage)this.persistedCoverageService.readCoverageFullMetadataByIdFromCache(metadata.getCoverageName());
        
        // Depend on the geo XY axes subsets, select a suitable pyramid member coverage (it must be the lowest level which is valid for both X and Y axes).
        CoveragePyramid coveragePyramid = this.pyramidService.getSuitableCoveragePyramidForScaling(baseCoverage, geoSubsetX, geoSubsetY,
                                                            null, null,
                                                            width, height, nonXYSubsetDimensions);
        GeneralGridCoverage pyramidMemeberCoverage = (GeneralGridCoverage)this.persistedCoverageService.readCoverageFullMetadataByIdFromCache(coveragePyramid.getPyramidMemberCoverageId());
        WcpsCoverageMetadata newMetadata = this.translate(pyramidMemeberCoverage.getCoverageId());
        
        // If a downscaled collection is selected, metadata object should use this one for other processes.
        newMetadata.setCoveragePyramid(coveragePyramid);
        
        return newMetadata;
    }
    

    /**
     * Build list of RangeField for WcpsCoverageMetadata object
     *
     * @param fields
     * @return
     */
    private List<RangeField> buildRangeFields(List<Field> fields) {
        List<RangeField> rangeFields = new ArrayList<>();
        // each field contains one quantity
        for (Field field : fields) {
            Quantity quantity = field.getQuantity();
            RangeField rangeField = new RangeField();
            // Data type for each band of coverage
            rangeField.setDataType(quantity.getDataType());
            rangeField.setName(field.getName());
            rangeField.setDescription(quantity.getDescription());
            rangeField.setDefinition(quantity.getDefinition());
            rangeField.setNodata(quantity.getNilValues());
            rangeField.setUomCode(quantity.getUom().getCode());
            rangeField.setAllowedValues(quantity.getAllowedValues());

            rangeFields.add(rangeField);
        }

        return rangeFields;
    }

    /**
     * Build list of axes for WcpsCoverageMetadata from the coverage's axes
     *
     * @param geoDomains
     * @param gridDomains
     * @return
     */
    private List<Axis> buildAxes(String coverageCRS, List<GeoAxis> geoAxes, List<IndexAxis> indexAxes) throws PetascopeException {
        List<Axis> result = new ArrayList();
        
        for (int i = 0; i < geoAxes.size(); i++) {
            GeoAxis geoAxis = geoAxes.get(i);
            String axisLabel = geoAxis.getAxisLabel();

            // NOTE: the order of geo CRS axes could be different from the index axes
            // e.g: CRS is EPSG:4326&Ansidate so the geo order is: Lat, Long, t, but in rasdaman, grid stored as: t, Lat, Long
            IndexAxis indexAxis = null;
            for (int j = 0; j < indexAxes.size(); j++) {
                indexAxis = indexAxes.get(j);
                String indexAxisLabelTmp = indexAxis.getAxisLabel();
                if (axisLabel.equals(indexAxisLabelTmp)) {
                    break;
                }
            }

            // geoBounds is the geo bounds of axis in the coverage (but can be modified later by subsets)
            NumericSubset originalGeoBounds = new NumericTrimming(geoAxis.getLowerBoundNumber(), geoAxis.getUpperBoundNumber());
            NumericSubset geoBounds = new NumericTrimming(geoAxis.getLowerBoundNumber(), geoAxis.getUpperBoundNumber());            
            NumericSubset originalGridBounds = new NumericTrimming(new BigDecimal(indexAxis.getLowerBound()), new BigDecimal(indexAxis.getUpperBound()));
            NumericSubset gridBounds = new NumericTrimming(new BigDecimal(indexAxis.getLowerBound()), new BigDecimal(indexAxis.getUpperBound()));
            
            String crsUri = geoAxis.getSrsName();

            CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(crsUri);
            // x, y, t,...
            String axisType = CrsUtil.getAxisTypeByIndex(coverageCRS, i);

            // Get the metadata of CRS (needed when using TimeCrs)
            String axisUoM = geoAxis.getUomLabel();
            // the order of geo axis stored in rasdaman as grid axis (e.g: CRS order EPSG:4326 Lat, Long, but in grid order is: Long, Lat)
            int gridAxisOrder = indexAxis.getAxisOrder();

            // NOTE: this needs the "sign" of offset vector as well
            BigDecimal scalarResolution = geoAxis.getResolution();
            BigDecimal originNumber = this.getOriginNumber(geoAxis);

            // Check domainElement's type
            if (geoAxis.isIrregular()) {
                // All stored coefficients for irregular axis in coverage
                List<BigDecimal> directPositions = ((org.rasdaman.domain.cis.IrregularAxis) geoAxis).getDirectPositionsAsNumbers();
                result.add(new IrregularAxis(axisLabel, geoBounds, originalGridBounds, gridBounds,
                        crsUri, crsDefinition, axisType, axisUoM, gridAxisOrder,
                        originNumber, scalarResolution, directPositions, originalGeoBounds));
            } else {

                result.add(new RegularAxis(axisLabel, geoBounds, originalGridBounds, gridBounds,
                        crsUri, crsDefinition, axisType, axisUoM, gridAxisOrder,
                        originNumber, scalarResolution, originalGeoBounds));
            }            
        }
        return result;
    }

    private BigDecimal getOriginNumber(GeoAxis geoAxis) throws PetascopeException {
        BigDecimal origin;

        if (geoAxis.isIrregular()) {
            // Only supports irregular with positive resolution
            origin = geoAxis.getLowerBoundNumber();
        } else {
            BigDecimal resolution = geoAxis.getResolution();
            BigDecimal lowerBound = geoAxis.getLowerBoundNumber();
            BigDecimal upperBound = geoAxis.getUpperBoundNumber();
            //if axis is regular positive axis we apply formula: origin = (geoMinValue + 0.5) * resolution (> 0)
            if (resolution.compareTo(BigDecimal.ZERO) > 0) {
                origin = lowerBound.add(BigDecimal.valueOf(1.0 / 2)
                        .multiply(resolution)).stripTrailingZeros();
            } else {
                // if axis is regular negative axis, origin = (geoMaxValue + 0.5) * resolution (< 0)
                origin = upperBound.add(BigDecimal.valueOf(1.0 / 2)
                        .multiply(resolution)).stripTrailingZeros();
            }
        }

        return origin;
    }
}
