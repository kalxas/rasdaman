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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.metadata.service;

import petascope.exceptions.PetascopeException;
import petascope.util.BigDecimalUtil;
import petascope.wcps2.metadata.model.ParsedSubset;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Translate the coordinates from geo bound to grid bound for trimming/slicing and vice versa if using CRS:1 in trimming/slicing
 * i.e: Lat(0:20) ->
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class CoordinateTranslationService {

    private final CoverageRegistry coverageRegistry;

    public CoordinateTranslationService(CoverageRegistry coverageRegistry) {
        this.coverageRegistry = coverageRegistry;
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
    public ParsedSubset<BigInteger> geoToGridForRegularAxis(ParsedSubset<BigDecimal> numericSubset, BigDecimal geoDomainMin,
            BigDecimal geoDomainMax, BigDecimal resolution, BigDecimal gridDomainMin) {
        boolean zeroIsMin = resolution.compareTo(BigDecimal.ZERO) > 0;

        BigDecimal returnLowerLimit, returnUpperLimit;
        if (zeroIsMin) {
            // closed interval on the lower limit, open on the upper limit - use floor and ceil - 1 repsectively
            // e.g: Long(0:20) -> c[0:50]
            returnLowerLimit = BigDecimalUtil.divide(numericSubset.getLowerLimit().subtract(geoDomainMin), resolution)
                               .setScale(0, RoundingMode.FLOOR).add(gridDomainMin);
            returnUpperLimit = BigDecimalUtil.divide(numericSubset.getUpperLimit().subtract(geoDomainMin), resolution)
                               .setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE).add(gridDomainMin);

            //because we use ceil - 1, when values are close (less than 1 resolution dif), the upper will be pushed below the lower
            if (returnUpperLimit.compareTo(returnLowerLimit) < 0) {
                returnUpperLimit = returnLowerLimit;
            }
            // NOTE: the if a slice equals the upper bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
            if ((geoDomainMax.compareTo(geoDomainMin) != 0) && numericSubset.getLowerLimit().equals(numericSubset.getUpperLimit()) && numericSubset.getUpperLimit().equals(geoDomainMax)) {
                returnLowerLimit = returnLowerLimit.subtract(BigDecimal.ONE);
                returnUpperLimit = returnLowerLimit;
            }
        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            // e.g: Lat(0:20) -> c[0:50]
            returnLowerLimit = BigDecimalUtil.divide(numericSubset.getUpperLimit().subtract(geoDomainMax), resolution)
                               .setScale(0, RoundingMode.CEILING).add(gridDomainMin);
            returnUpperLimit = BigDecimalUtil.divide(numericSubset.getLowerLimit().subtract(geoDomainMax), resolution)
                               .setScale(0, RoundingMode.FLOOR).subtract(BigDecimal.ONE).add(gridDomainMin);

            if (returnUpperLimit.compareTo(returnLowerLimit) < 0) {
                returnUpperLimit = returnLowerLimit;
            }
        }
        return new ParsedSubset(returnLowerLimit.toBigInteger(), returnUpperLimit.toBigInteger());
    }

    public ParsedSubset<BigInteger> gridToGeoForRegularAxis(ParsedSubset<BigDecimal> numericSubset, BigDecimal geoDomainMin,
            BigDecimal geoDomainMax, BigDecimal resolution, BigDecimal gridDomainMin) {
        boolean zeroIsMin = resolution.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal returnLowerLimit, returnUpperLimit;
        if (zeroIsMin) {
            // e.g: Long:"http://.../Index2D"(0:50) -> Long(0:20)
            returnLowerLimit = BigDecimalUtil.multiple(numericSubset.getLowerLimit().subtract(geoDomainMin), resolution)
                               .setScale(0, RoundingMode.FLOOR).add(gridDomainMin);
            returnUpperLimit = BigDecimalUtil.multiple(numericSubset.getUpperLimit().subtract(geoDomainMin), resolution)
                               .setScale(0, RoundingMode.CEILING).add(gridDomainMin);

            // because we use ceil - 1, when values are close (less than 1 resolution dif), the upper will be pushed below the lower
            if (returnUpperLimit.compareTo(returnLowerLimit) < 0) {
                returnUpperLimit = returnLowerLimit;
            }
            // NOTE: the if a slice equals the upper bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
            if ((geoDomainMax.compareTo(geoDomainMin) != 0) && numericSubset.getLowerLimit().equals(numericSubset.getUpperLimit()) && numericSubset.getUpperLimit().equals(geoDomainMax)) {
                returnLowerLimit = returnLowerLimit.subtract(BigDecimal.ONE);
                returnUpperLimit = returnLowerLimit;
            }
        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            // e.g: Lat:"http://.../Index2D"(0:50) -> Lat(0:20)
            returnLowerLimit = BigDecimalUtil.multiple(numericSubset.getUpperLimit().subtract(geoDomainMax), resolution)
                               .setScale(0, RoundingMode.CEILING).add(gridDomainMin);
            returnUpperLimit = BigDecimalUtil.multiple(numericSubset.getLowerLimit().subtract(geoDomainMax), resolution)
                               .setScale(0, RoundingMode.FLOOR).add(gridDomainMin);

            if (returnUpperLimit.compareTo(returnLowerLimit) < 0) {
                returnUpperLimit = returnLowerLimit;
            }
        }
        return new ParsedSubset(returnLowerLimit.toBigInteger(), returnUpperLimit.toBigInteger());
    }

    /**
     * Returns the translated subset if the coverage has an irregular axis
     * This needs to be further refactored: the correct coefficients must be added in the WcpsCoverageMetadata object when a subset is done
     * on it, and the min and max coefficients should be passed to this method.
     *
     * @param numericSubset    the subset to be translated
     * @param scalarResolution
     * @param coverageName
     * @param axisOrder
     * @param gridDomainMin
     * @param gridDomainMax
     * @param geoDomainMin
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public ParsedSubset<BigInteger> geoToGridForIrregularAxes(
        ParsedSubset<BigDecimal> numericSubset, BigDecimal scalarResolution, String coverageName,
        int axisOrder, BigDecimal gridDomainMin,
        BigDecimal gridDomainMax, BigDecimal geoDomainMin) throws PetascopeException {
        // Need to query the database (IRRSERIES table) to get the extents
        // Retrieve correspondent cell indexes (unique method for numerical/timestamp values)
        // TODO: I need to extract all the values, not just the extremes
        // coefficients are relative to the origin, but subsets are not.
        BigDecimal lowerCoefficient = null;
        BigDecimal upperCoefficient = null;

        // e.g: t(148654) in irr_cube_2
        lowerCoefficient = ((numericSubset.getLowerLimit()).subtract(geoDomainMin)).divide(scalarResolution);
        upperCoefficient = ((numericSubset.getUpperLimit()).subtract(geoDomainMin)).divide(scalarResolution);

        long[] indexes = coverageRegistry.getMetadataSource().getIndexesFromIrregularRectilinearAxis(
                             coverageName,
                             axisOrder, // i-order of axis
                             lowerCoefficient,
                             upperCoefficient,
                             Long.valueOf(gridDomainMin.toBigInteger().toString()), Long.valueOf(gridDomainMax.toBigInteger().toString()));

        // Add sdom lower bound
        BigInteger lowerBound = BigDecimal.valueOf(indexes[0]).add(gridDomainMin).toBigInteger();
        BigInteger upperbound = BigDecimal.valueOf(indexes[1]).toBigInteger();

        return new ParsedSubset(lowerBound, upperbound);
    }
}
