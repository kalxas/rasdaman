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
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.util.TimeUtil;
import petascope.wcs2.parsers.GetCoverageRequest;

/**
* Representation of a trim subset in a WCS request.
*/
public class DimensionTrim extends DimensionSubset {

    //private final String trimLow;
    //private final String trimHigh;
    private String trimLow;
    private String trimHigh;

    public DimensionTrim(String dimension, String trimLow, String trimHigh) {
        this(dimension, null, trimLow, trimHigh);
    }

    public DimensionTrim(String dimension, String crs, String trimLow, String trimHigh) {
        super(dimension, crs);
        this.trimLow = trimLow;
        this.trimHigh = trimHigh;
        isNumeric = !trimLow.matches(QUOTED_SUBSET) && !trimHigh.matches(QUOTED_SUBSET);
    }

    public String getTrimHigh() {
        return trimHigh;
    }

    public String getTrimLow() {
        return trimLow;
    }

    /**
     * @param value Set new lower bound to 1D domain.
     */
    public void setTrimLow(String value) {
        trimLow = value;
        isNumeric = !trimLow.matches(QUOTED_SUBSET);
    }

    public void setTrimLow(Double value) {
        setTrimLow(value.toString());
        isNumeric = true;
    }

    public void setTrimLow(Integer value) {
        setTrimLow(value.toString());
        isNumeric = true;
    }

    /**
     * @param value Set new upper bound to 1D domain.
     */
    public void setTrimHigh(String value) {
        trimHigh = value;
        isNumeric = !trimHigh.matches(QUOTED_SUBSET);
    }

    public void setTrimHigh(Double value) {
        setTrimHigh(value.toString());
        isNumeric = true;
    }

    public void setTrimHigh(Integer value) {
        setTrimHigh(value.toString());
        isNumeric = true;
    }

    /**
     * Integrity of time subsets (quoted subsets): valid and ordered bounds.
     * @throws petascope.exceptions.WCSException
     */
    public void timestampSubsetCheck() throws WCSException {
        if (null != getTrimLow() && getTrimLow().matches(QUOTED_SUBSET)) {
            if (!TimeUtil.isValidTimestamp(getTrimLow())) {
                throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getTrimLow() + "\" is not valid or supported.");
            }
        }
        if (null != getTrimHigh() && getTrimHigh().matches(QUOTED_SUBSET)) {
            if (!TimeUtil.isValidTimestamp(getTrimHigh())) {
                throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getTrimHigh() + "\" is not valid or supported.");
            }
        }
        if (null != getTrimLow() && null != getTrimHigh() && getTrimLow().matches(QUOTED_SUBSET) && getTrimHigh().matches(QUOTED_SUBSET)) {
            // Check low<high
            try {
                if (!TimeUtil.isOrderedTimeSubset(getTrimLow(), getTrimHigh())) {
                    throw new WCSException(ExceptionCode.InvalidParameterValue, "Temporal subset \"" + getTrimLow() + ":" + getTrimHigh() + "\" is invalid: check order.");
                }
            } catch (PetascopeException ex) {
                throw new WCSException(ex.getExceptionCode(), ex);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + "(" + trimLow + "," + trimHigh + ")";
    }
}
