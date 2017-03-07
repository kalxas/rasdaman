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

import java.math.BigDecimal;
import petascope.exceptions.PetascopeException;
import petascope.util.TimeUtil;
import petascope.wcps2.error.managed.processing.InvalidCalculatedBoundsSubsettingException;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.ParsedSubset;

/**
 * Class which convert slicing point in date time (e.g: "1950-01-01" to numeric in grid coordinate (e.g: 0))
 * depends on the type of axis (regular / irregular)
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class TimeConversionService {

    /**
     * Point is a valid date time and it will translate the value in date time stamp (e.g: "2008-01-01") to grid coordinate (e.g: 0)
     * @param point the value in date time stamp
     * @param axis
     * @return the value in grid point
     */
    public static BigDecimal getTimeInGridPointForRegularAxis(Axis axis, String point) {
        String axisName = axis.getLabel();
        String axisUoM = axis.getAxisUoM();
        String datumOrigin = axis.getCrsDefinition().getDatumOrigin();

        BigDecimal result;

        try {
            // Need to convert timestamps to TemporalCRS numeric coordinates
            result = new BigDecimal(TimeUtil.countOffsets(datumOrigin, point, axisUoM, BigDecimal.ONE).toString()); // do not normalize by vector here:
        } catch (PetascopeException e) {
            throw new InvalidCalculatedBoundsSubsettingException(axisName, new ParsedSubset<String>(point));
        }

        return result;
    }

    /**
     * Point is a valid date time and it will translate the value in date time stamp (e.g: "2008-01-01") to grid coordinate (e.g: 0)
     * @param axis
     * @param point
     * @return
     */
    public static BigDecimal getTimeInGridPointForIrregularAxis(Axis axis, String point) {
        String axisName = axis.getLabel();
        String axisUoM = axis.getAxisUoM();
        String datumOrigin = axis.getCrsDefinition().getDatumOrigin();
        BigDecimal scalarResolution = axis.getScalarResolution();

        BigDecimal result;
        try {
            result = new BigDecimal(TimeUtil.countOffsets(datumOrigin, point, axisUoM, scalarResolution).toString());
        } catch (PetascopeException e) {
            throw new InvalidCalculatedBoundsSubsettingException(axisName, new ParsedSubset<String>(point));
        }

        // NOTE: if scalarResolution is equal 1 (e.g: 1 day then it does not have problem with the translated coordinate)
        // e:g irr_cube_2 with offset vector time is 2 days (slicing point (2008-01-01) will return 148654 / 2 which is out of geo-bound for this axis).
        result = result.multiply(scalarResolution);

        return result;
    }
}
