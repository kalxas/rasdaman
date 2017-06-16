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
package petascope.wcps2.metadata.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.core.AxisTypes.AxisDirection;
import petascope.util.ListUtil;
import petascope.core.Pair;
import petascope.util.TimeUtil;
import org.rasdaman.migration.domain.legacy.LegacyAxisTypes;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class IrregularAxis extends Axis {

    // list of coefficients for irregular axis
    private List<BigDecimal> directPositions;

    public IrregularAxis(String label, NumericSubset geoBounds, NumericSubset gridBounds,
            AxisDirection direction, String crsUri, CrsDefinition crsDefinition,
            String axisType, String axisUoM,
            int rasdamanOrder, BigDecimal origin, BigDecimal resolution, List<BigDecimal> directPositions) {
        super(label, geoBounds, gridBounds, direction, crsUri, crsDefinition, axisType, axisUoM, rasdamanOrder, origin, resolution);

        this.directPositions = directPositions;

    }

    public List<BigDecimal> getDirectPositions() {
        return directPositions;
    }

    public void setDirectPositions(List<BigDecimal> directPositions) {
        this.directPositions = directPositions;
    }

    /**
     *
     * Return the grid indices of input geo min and geo max values for irregular
     * axis e.g: 0 10 20 50 70 and input: min is 30, max is 60 then the first
     * value which is selected is 50 (and grid index is: 3) and the second value
     * which is selected is 70 (and grid index is: 4)
     *
     * return [3, 4]
     *
     * @param minInput
     * @param maxInput
     * @return
     */
    public Pair<Long, Long> getGridIndices(BigDecimal minInput, BigDecimal maxInput) {

        Long minIndex = null;
        Long maxIndex = null;
        boolean foundMinIndex = false;

        Long i = Long.valueOf("0");

        // coefficient in numbers for legacy coverages
        for (BigDecimal coefficient : directPositions) {
            // find the min number which >= minInput
            if (!foundMinIndex && coefficient.compareTo(minInput) >= 0) {
                minIndex = i;
                foundMinIndex = true;
            }
            // find the max number which <= maxInput (as it is ascending list, so don't stop until coefficent > maxInput
            if (coefficient.compareTo(maxInput) <= 0) {
                maxIndex = i;
            }
            // stop as it should find the minIndex and maxIndex already
            if (coefficient.compareTo(maxInput) >= 0) {
                break;
            }

            i++;
        }
        
        if (minIndex == null || maxIndex == null) {
           
        }

        return new Pair(minIndex, maxIndex);
    }

    /**
     * Get all the coefficients from the list of directPositions which greater
     * than minInput and less than maxInput
     *
     * @param minInput
     * @param maxInput
     * @return
     */
    public List<BigDecimal> getAllCoefficientsInInterval(BigDecimal minInput, BigDecimal maxInput) {
        // Find the min and max grid incides in the List of directPositions
        Pair<Long, Long> gridIndices = this.getGridIndices(minInput, maxInput);
        List<BigDecimal> coefficients = new ArrayList<>();
        for (Long i = gridIndices.fst; i <= gridIndices.snd; i++) {
            BigDecimal coefficient = this.directPositions.get(i.intValue());
            coefficients.add(coefficient);
        }

        return coefficients;
    }

    /**
     * Return the list of raw coefficients in a String, raw means dateTime no
     * transform to dateTime format e.g: 0 3 5 8
     *
     * @return
     */
    public String getRawCoefficients() {
        String coefficients = ListUtil.join(directPositions, " ");

        return coefficients;
    }

    /**
     * Return the list of translated coefficients (DateTime axis) or raw
     * coefficients (non-datetime axis)
     *
     * @return
     * @throws PetascopeException
     */
    public String getRepresentationCoefficients() throws PetascopeException {
        String coefficients = "";

        // date time axis, need to translate from raw coefficients to datetime format based on CRS origin
        if (this.getAxisType().equals(LegacyAxisTypes.T_AXIS)) {
            List<String> translatedCoefficients = TimeUtil.listValuesToISODateTime(this.getGeoBounds().getLowerLimit(),
                    directPositions, this.getCrsDefinition());
            coefficients = ListUtil.join(translatedCoefficients, " ");
        } else {
            // non date time axis
            coefficients = this.getRawCoefficients();
        }

        return coefficients;
    }

    @Override
    public IrregularAxis clone() {
        return new IrregularAxis(getLabel(), getGeoBounds(), getGridBounds(), getDirection(),
                getNativeCrsUri(), getCrsDefinition(), getAxisType(), getAxisUoM(),
                getRasdamanOrder(), getOriginalOrigin(), getResolution(), getDirectPositions());
    }

}
