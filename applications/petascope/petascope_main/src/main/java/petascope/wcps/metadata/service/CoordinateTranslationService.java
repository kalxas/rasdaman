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

import petascope.util.BigDecimalUtil;
import petascope.wcps.metadata.model.ParsedSubset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import petascope.core.BoundingBox;
import petascope.core.GeoTransform;
import petascope.core.Pair;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.core.service.CrsComputerService;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.wcps.exception.processing.IrreguarAxisCoefficientNotFoundException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * Translate the coordinates from geo bound to grid bound for trimming/slicing and vice versa if using CRS:1 in trimming/slicing
 * i.e: Lat(0:20) ->
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class CoordinateTranslationService {
    
    private final BigDecimal GDAL_EPSILON_MIN = new BigDecimal("0.001");
    private final BigDecimal GDAL_EPSILON_MAX = new BigDecimal("0.5");
    
    /**
     * From a geo bounds for X axis (e.g: lon1:lon2]
     * return the grid bound and number of grid pixel
     */
    private Pair<BigDecimal, BigDecimal> calculateGridXBounds(GeoTransform adfGeoTransform, BigDecimal geoXMin, BigDecimal geoXMax) {
        BigDecimal gridXMin = BigDecimalUtil.divide(geoXMin.subtract(adfGeoTransform.getUpperLeftGeoXDecimal()), adfGeoTransform.getGeoXResolutionDecimal());
        BigDecimal numberOfXPixels = BigDecimalUtil.divide(geoXMax.subtract(geoXMin), adfGeoTransform.getGeoXResolutionDecimal());
        
        // Default nearest neighbor
        gridXMin = gridXMin.add(GDAL_EPSILON_MIN).setScale(0, RoundingMode.FLOOR);
        numberOfXPixels = numberOfXPixels.add(GDAL_EPSILON_MAX).setScale(0, RoundingMode.FLOOR);
        
        return new Pair<>(gridXMin, numberOfXPixels);
    }
    
    /**
     * From a geo bounds for Y axis (e.g: lat1:lat2]
     * return the grid bound and number of grid pixel
     */
    private Pair<BigDecimal, BigDecimal> calculateGridYBounds(GeoTransform adfGeoTransform, BigDecimal geoYMin, BigDecimal geoYMax) {
        BigDecimal gridYMin = BigDecimalUtil.divide(geoYMax.subtract(adfGeoTransform.getUpperLeftGeoYDecimal()), adfGeoTransform.getGeoYResolutionDecimal());
        BigDecimal numberOfYPixels = BigDecimalUtil.divide(geoYMin.subtract(geoYMax), adfGeoTransform.getGeoYResolutionDecimal());
        
        // Default nearest neighbor
        gridYMin = gridYMin.add(GDAL_EPSILON_MIN).setScale(0, RoundingMode.FLOOR);
        numberOfYPixels = numberOfYPixels.add(GDAL_EPSILON_MAX).setScale(0, RoundingMode.FLOOR);
        
        return new Pair<>(gridYMin, numberOfYPixels);
    }
    
    /**
     * Get a pair of lower:upper bounds for geo and grid by geo trimming subset of X axis
     * as gdal_translate -projwin
     */
    public Pair<ParsedSubset<BigDecimal>, ParsedSubset<Long>> calculateGeoGridXBounds(Axis axisX, GeoTransform adfGeoTransform, 
                                                                                      BigDecimal geoXMin, BigDecimal geoXMax) {
        Pair<BigDecimal, BigDecimal> gridPair = this.calculateGridXBounds(adfGeoTransform, geoXMin, geoXMax);
        
        BigDecimal dfOXSize = gridPair.snd.subtract(GDAL_EPSILON_MIN).setScale(0, RoundingMode.CEILING);
        BigDecimal gridXMin = gridPair.fst;
        BigDecimal gridXMax = gridXMin.add(dfOXSize).subtract(BigDecimal.ONE);
        
        gridXMin = gridXMin.add(axisX.getOriginalGridBounds().getLowerLimit());
        gridXMax = gridXMax.add(axisX.getOriginalGridBounds().getLowerLimit());
        
        BigDecimal updatedGeoXMin = adfGeoTransform.getUpperLeftGeoXDecimal().add(gridPair.fst.multiply(adfGeoTransform.getGeoXResolutionDecimal()));
        BigDecimal updatedGeoXMax = updatedGeoXMin.add(adfGeoTransform.getGeoXResolutionDecimal().multiply(dfOXSize));
        
        ParsedSubset<Long> gridSubset = new ParsedSubset<>(gridXMin.longValue(), gridXMax.longValue());
        ParsedSubset<BigDecimal> geoSubset = new ParsedSubset<>(updatedGeoXMin, updatedGeoXMax);
        
        return new Pair<>(geoSubset, gridSubset);
    }
    
    /**
     * Get a pair of lower:upper bounds for geo and grid by geo trimming subset of Y axis
     * as gdal_translate -projwin
     */
    public Pair<ParsedSubset<BigDecimal>, ParsedSubset<Long>> calculateGeoGridYBounds(Axis axisY, GeoTransform adfGeoTransform, 
                                                                                      BigDecimal geoYMin, BigDecimal geoYMax) {
        Pair<BigDecimal, BigDecimal> gridPair = this.calculateGridYBounds(adfGeoTransform, geoYMin, geoYMax);
        
        BigDecimal dfOYSize = gridPair.snd.subtract(GDAL_EPSILON_MIN).setScale(0, RoundingMode.CEILING);
        BigDecimal gridYMin = gridPair.fst;
        BigDecimal gridYMax = gridYMin.add(dfOYSize).subtract(BigDecimal.ONE);
        
        gridYMin = gridYMin.add(axisY.getOriginalGridBounds().getLowerLimit());
        gridYMax = gridYMax.add(axisY.getOriginalGridBounds().getLowerLimit());
        
        BigDecimal updatedGeoYMax = adfGeoTransform.getUpperLeftGeoYDecimal().add(gridPair.fst.multiply(adfGeoTransform.getGeoYResolutionDecimal()));
        BigDecimal updatedGeoYMin = updatedGeoYMax.add(adfGeoTransform.getGeoYResolutionDecimal().multiply(dfOYSize));
        
        ParsedSubset<Long> gridSubset = new ParsedSubset<>(gridYMin.longValue(), gridYMax.longValue());
        ParsedSubset<BigDecimal> geoSubset = new ParsedSubset<>(updatedGeoYMin, updatedGeoYMax);
        
        return new Pair<>(geoSubset, gridSubset);
    }
    
     /**
     * From a geo XY bounding box with geo bounds calculates a grid XY bounding box with grid bounds.
     */
    public BoundingBox calculageGridXYBoundingBox(Axis axisX, Axis axisY, BoundingBox intersectGeoBBoxNativeCRS) throws PetascopeException {
        
        int epsgCode = CrsUtil.getEpsgCodeAsInt(axisX.getNativeCrsUri());
        
        // axis X
        int gridWidth = axisX.getTotalNumberOfGridPixels();

        GeoTransform adfGeoTransformX = new GeoTransform(epsgCode, axisX.getGeoBounds().getLowerLimit().doubleValue(), 0,
                                                         gridWidth, 0, axisX.getResolution().doubleValue(), 0);
        Pair<ParsedSubset<BigDecimal>, ParsedSubset<Long>> pairX = this.calculateGeoGridXBounds(axisX, adfGeoTransformX,
                                                                                                intersectGeoBBoxNativeCRS.getXMin(), intersectGeoBBoxNativeCRS.getXMax());
        // axis Y
        int gridHeight = axisY.getTotalNumberOfGridPixels();

        GeoTransform adfGeoTransformY = new GeoTransform(epsgCode, 0, axisY.getGeoBounds().getUpperLimit().doubleValue(),
                                                         0, gridHeight, 0, axisY.getResolution().doubleValue());
        Pair<ParsedSubset<BigDecimal>, ParsedSubset<Long>> pairY = this.calculateGeoGridYBounds(axisY, adfGeoTransformY,
                                                                                                intersectGeoBBoxNativeCRS.getYMin(), intersectGeoBBoxNativeCRS.getYMax());
        
        BoundingBox gridBBox = new BoundingBox(new BigDecimal(pairX.snd.getLowerLimit().toString()), new BigDecimal(pairY.snd.getLowerLimit().toString()),
                                               new BigDecimal(pairX.snd.getUpperLimit().toString()), new BigDecimal(pairY.snd.getUpperLimit().toString()));
        
        return gridBBox;
        
    }
    
    /**
     * Translate a geo subset on an axis to grid subset accordingly.
     * e.g: Lat(0:20) -> c[10:15]
     */
    public ParsedSubset<Long> geoToGridSpatialDomain(Axis axis, WcpsSubsetDimension subsetDimension, ParsedSubset<BigDecimal> parsedGeoSubset) throws PetascopeException {
        ParsedSubset<Long> parsedGridSubset;
        if (axis instanceof RegularAxis) {
            parsedGridSubset = this.geoToGridForRegularAxis(parsedGeoSubset, axis.getGeoBounds().getLowerLimit(),
                                                            axis.getGeoBounds().getUpperLimit(), axis.getResolution(), axis.getGridBounds().getLowerLimit());
        } else {
            parsedGridSubset = this.geoToGridForIrregularAxes(subsetDimension, parsedGeoSubset, axis.getResolution(), axis.getGridBounds().getLowerLimit(), 
                                                            axis.getGridBounds().getUpperLimit(), axis.getGeoBounds().getLowerLimit(), (IrregularAxis)axis);
        }
        
        return parsedGridSubset;
    }
    
    
    /**
     * Computes the pixel indices for a subset on a regular axis.
     *
     * @param numericSubset:     the geo subset to be converted to pixel indices.
     * @param geoDomainMin:      the geo minimum on the axis.
     * @param geoDomainMax:      the geo maximum on the axis.
     * @param resolution:        the signed cell width (negative if the axis is linear negative)
     * @param gridDomainMin:     the grid coordinate of the first pixel of the axis
     * @return the pair of grid coordinates corresponding to the given geo subset.
     */
    public ParsedSubset<Long> geoToGridForRegularAxis(ParsedSubset<BigDecimal> numericSubset, BigDecimal geoDomainMin,
        BigDecimal geoDomainMax, BigDecimal resolution, BigDecimal gridDomainMin) {
        boolean zeroIsMin = resolution.compareTo(BigDecimal.ZERO) > 0;
        
        BigDecimal lowerBound = numericSubset.getLowerLimit();
        BigDecimal upperBound = numericSubset.getUpperLimit();
        
        if (numericSubset.isSlicing()) {
            lowerBound = numericSubset.getSlicingCoordinate();
            upperBound = numericSubset.getSlicingCoordinate();
        }

        BigDecimal returnLowerLimit, returnUpperLimit;
        if (zeroIsMin) {
            // closed interval on the lower limit, open on the upper limit - use floor and ceil - 1 repsectively
            // e.g: Long(0:20) -> c[0:50]
            BigDecimal lowerLimit = BigDecimalUtil.divide(lowerBound.subtract(geoDomainMin), resolution);
            lowerLimit = CrsComputerService.shiftToNearestGridPointWCPS(lowerLimit);
            returnLowerLimit = lowerLimit.setScale(0, RoundingMode.FLOOR).add(gridDomainMin);
            
            BigDecimal upperLimit = BigDecimalUtil.divide(upperBound.subtract(geoDomainMin), resolution);            
            upperLimit = CrsComputerService.shiftToNearestGridPointWCPS(upperLimit);
            returnUpperLimit = upperLimit.setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE).add(gridDomainMin);

        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            // e.g: axis with 4 pixels in rasdaman, geo limits are 80 and 0, res = -20.
            // ras:    0   1   2   3
            //        --- --- --- ---
            // geo:  80  60  40  20  0
            // user subset 58: count how many resolution-sized interval are between 80 and 58 (1.1), and floor it to get 1
            BigDecimal lowerLimit = BigDecimalUtil.divide(upperBound.subtract(geoDomainMax), resolution);
            lowerLimit = CrsComputerService.shiftToNearestGridPointWCPS(lowerLimit);
            returnLowerLimit = lowerLimit.setScale(0, RoundingMode.FLOOR).add(gridDomainMin);
            
            BigDecimal upperLimit = BigDecimalUtil.divide(lowerBound.subtract(geoDomainMax), resolution);
            upperLimit = CrsComputerService.shiftToNearestGridPointWCPS(upperLimit);
            returnUpperLimit = upperLimit.setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE).add(gridDomainMin);
        }
        
        //because we use ceil - 1, when values are close (less than 1 resolution dif), the upper will be pushed below the lower            
        if (returnUpperLimit.add(BigDecimal.ONE).equals(returnLowerLimit)) {
            if (returnUpperLimit.compareTo(gridDomainMin) < 0) {
                returnUpperLimit = gridDomainMin;
            }
            returnLowerLimit = returnUpperLimit;
            
        }            
        
        return new ParsedSubset(returnLowerLimit.longValue(), returnUpperLimit.longValue());
    }

    /**
     * Translate the  grid subset with grid CRS (i.e: CRS:1) to geo subset
     * e.g: Long:"CRS:1"(0:50) -> Long(0.5:20.5)
     * NOTE: no rounding for geo bounds as they should be not integer values
     * @param numericSubset
     * @param gridDomainMin
     * @param gridDomainMax
     * @param resolution
     * @param geoDomainMin
     * @return 
     */
    public static ParsedSubset<BigDecimal> gridToGeoForRegularAxis(ParsedSubset<BigDecimal> numericSubset, BigDecimal gridDomainMin,
            BigDecimal gridDomainMax, BigDecimal resolution, BigDecimal geoDomainMin) {
        boolean zeroIsMin = resolution.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal returnLowerLimit, returnUpperLimit;
        if (zeroIsMin) {
            // e.g: Long:"CRS:1"(0:50) -> Long(0.5:20.5)
            returnLowerLimit = BigDecimalUtil.multiple(numericSubset.getLowerLimit().subtract(gridDomainMin), resolution)
                               .add(geoDomainMin);
            returnUpperLimit = BigDecimalUtil.multiple(numericSubset.getUpperLimit().subtract(gridDomainMin), resolution)
                               .add(geoDomainMin);

            // because we use ceil - 1, when values are close (less than 1 resolution dif), the upper will be pushed below the lower
            if (returnUpperLimit.compareTo(returnLowerLimit) < 0) {
                returnUpperLimit = returnLowerLimit;
            }
        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            // e.g: Lat:"CRS:"(0:50) -> Lat(0.23:20.23)
            // (input grid - total pixels) / resolution + geoDomain, NOTE: total pixels + 1 (e.g: 0:710 then max is not: 0 but 711)
            returnLowerLimit = BigDecimalUtil.multiple(numericSubset.getUpperLimit().subtract(gridDomainMax.add(BigDecimal.ONE)), resolution)
                               .add(geoDomainMin);
            returnUpperLimit = BigDecimalUtil.multiple(numericSubset.getLowerLimit().subtract(gridDomainMax.add(BigDecimal.ONE)), resolution)
                               .add(geoDomainMin);

            if (returnUpperLimit.compareTo(returnLowerLimit) < 0) {
                returnUpperLimit = returnLowerLimit;
            }
        }
        return new ParsedSubset(returnLowerLimit, returnUpperLimit);
    }

    /**
     * Returns the translated subset if the coverage has an irregular axis
     * This needs to be further refactored: the correct coefficients must be added in the WcpsCoverageMetadata object when a subset is done
     * on it, and the min and max coefficients should be passed to this method.
     *
     */
    public ParsedSubset<Long> geoToGridForIrregularAxes(WcpsSubsetDimension subsetDimension,
        ParsedSubset<BigDecimal> numericSubset, BigDecimal scalarResolution, BigDecimal gridDomainMin,
        BigDecimal gridDomainMax, BigDecimal geoDomainMin, IrregularAxis irregularAxis) throws PetascopeException {
        
        BigDecimal lowerLimit = null;
        BigDecimal upperLimit = null;
        String originalLowerBound = null;
        String originalUpperBound = null;
        
        if (numericSubset.isSlicing()) {
            lowerLimit = numericSubset.getSlicingCoordinate();
            upperLimit = numericSubset.getSlicingCoordinate();
            originalLowerBound = ((WcpsSliceSubsetDimension)subsetDimension).getBound();
            originalUpperBound = ((WcpsSliceSubsetDimension)subsetDimension).getBound();
        } else {
            lowerLimit = numericSubset.getLowerLimit();
            upperLimit = numericSubset.getUpperLimit();
            originalLowerBound = ((WcpsTrimSubsetDimension)subsetDimension).getLowerBound();
            originalUpperBound = ((WcpsTrimSubsetDimension)subsetDimension).getUpperBound();
        }

        // e.g: t(148654) in irr_cube_2
        BigDecimal lowerCoefficient = (lowerLimit.subtract(geoDomainMin)).divide(scalarResolution);
        BigDecimal upperCoefficient = (upperLimit.subtract(geoDomainMin)).divide(scalarResolution);
        
        lowerCoefficient = lowerCoefficient.add(irregularAxis.getFirstCoefficient());
        upperCoefficient = upperCoefficient.add(irregularAxis.getFirstCoefficient());
        
        if (numericSubset.isSlicing()) {
            // e.g: irregular date axis has values "2015-01", "2016-01", "2018-01" and request "2017-01"
            if (irregularAxis.getIndexOfCoefficient(lowerCoefficient) < 0) {
                throw new IrreguarAxisCoefficientNotFoundException(irregularAxis.getLabel(), originalLowerBound);
            }
        }
        
        // Return the grid indices of the lower and upper coefficients in an irregular axis
        Pair<Long, Long> gridIndicePair = irregularAxis.getGridIndices(lowerCoefficient, upperCoefficient);
        Pair<Long, Long> gridBoundsPair = irregularAxis.calculateGridBoundsByZeroCoefficientIndex(gridIndicePair.fst, gridIndicePair.snd);

        return new ParsedSubset(gridBoundsPair.fst, gridBoundsPair.snd);
    }
    
    /**
     * Translate grid subset to geo subset for irregular axis (e.g: time).
     * 1 grid coordinate is attached to 1 geo coefficient of irregular axis.
     */
    public static ParsedSubset<BigDecimal> gridToGeoForIrregularAxes(ParsedSubset<BigDecimal> numericSubset, IrregularAxis irregularAxis) {
        BigDecimal lowerBoundCoefficient = irregularAxis.getDirectPositions().get(numericSubset.getLowerLimit().intValue());
        BigDecimal geoLowerBound = irregularAxis.getOrigin().add(lowerBoundCoefficient);
        
        BigDecimal upperBoundCoefficient = irregularAxis.getDirectPositions().get(numericSubset.getUpperLimit().intValue());
        BigDecimal geoUpperBound = irregularAxis.getOrigin().add(upperBoundCoefficient);
        
        return new ParsedSubset(geoLowerBound, geoUpperBound);
    }
}
