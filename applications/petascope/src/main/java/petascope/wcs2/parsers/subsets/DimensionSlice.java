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
package petascope.wcs2.parsers.subsets;

import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.TimeUtil;
import petascope.wcs2.parsers.GetCoverageRequest;

/**
* Representation of a slice subset in a WCS request.
*/
public class DimensionSlice extends DimensionSubset {

    private String slicePoint;

    public DimensionSlice(String dimension, String slicePoint) {
        this(dimension, null, slicePoint);
    }

    public DimensionSlice(String dimension, String crs, String slicePoint) {
        super(dimension, crs);
        this.slicePoint = slicePoint;
        isNumeric = !slicePoint.matches(QUOTED_SUBSET);
    }

    public String getSlicePoint() {
        return slicePoint;
    }

    public void setSlicePoint(String value) {
        slicePoint = value;
        isNumeric = !slicePoint.matches(QUOTED_SUBSET);
    }

    public void setSlicePoint(Double value) {
        setSlicePoint(value.toString());
        isNumeric = true;
    }

    /**
     * Integrity of time subsets (quoted subsets): valid and ordered bounds.
     * @throws petascope.exceptions.WCSException
     */
    public void timestampSubsetCheck() throws WCSException {
        if (null != getSlicePoint() && getSlicePoint().matches(QUOTED_SUBSET)) {
            if (!TimeUtil.isValidTimestamp(getSlicePoint())) {
                throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getSlicePoint() + "\" is not valid or supported.");
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + "(" + slicePoint + ")";
    }
}
