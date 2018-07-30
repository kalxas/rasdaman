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
package petascope.wcs2.parsers.subsets;

import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.TimeUtil;

/**
* Representation of a trim subset in a WCS request.
*/
public class TrimmingSubsetDimension extends AbstractSubsetDimension {

    private String lowerBound;
    private String upperBound;

    public TrimmingSubsetDimension(String dimension, String lowerBound, String upperBound) {
        this(dimension, null, lowerBound, upperBound);
    }

    public TrimmingSubsetDimension(String dimension, String crs, String lowerBound, String upperBound) {
        super(dimension, crs);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        isNumeric = !lowerBound.matches(QUOTED_SUBSET) && !upperBound.matches(QUOTED_SUBSET);
    }

    public String getUpperBound() {
        return upperBound;
    }

    public String getLowerBound() {
        return lowerBound;
    }

    /**
     * @param value Set new lower bound to 1D domain.
     */
    public void setLowerBound(String value) {
        lowerBound = value;
        isNumeric = !lowerBound.matches(QUOTED_SUBSET);
    }

    /**
     * @param value Set new upper bound to 1D domain.
     */
    public void setUpperBound(String value) {
        upperBound = value;
        isNumeric = !upperBound.matches(QUOTED_SUBSET);
    }


    /**
     * Integrity of time subsets (quoted subsets): valid and ordered bounds.
     * @throws petascope.exceptions.WCSException
     */
    public void timestampSubsetCheck() throws WCSException {
        if (null != getLowerBound() && getLowerBound().matches(QUOTED_SUBSET)) {
            if (!TimeUtil.isValidTimestamp(getLowerBound())) {
                throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getLowerBound() + "\" is not valid or supported.");
            }
        }
        if (null != getUpperBound() && getUpperBound().matches(QUOTED_SUBSET)) {
            if (!TimeUtil.isValidTimestamp(getUpperBound())) {
                throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getUpperBound() + "\" is not valid or supported.");
            }
        }
        if (null != getLowerBound() && null != getUpperBound() && getLowerBound().matches(QUOTED_SUBSET) && getUpperBound().matches(QUOTED_SUBSET)) {
            // Check low<high
            if (!TimeUtil.isOrderedTimeSubset(getLowerBound(), getUpperBound())) {
                throw new WCSException(ExceptionCode.InvalidParameterValue, "Temporal subset \"" + getLowerBound() + ":" + getUpperBound() + "\" is invalid: check order.");
            }
        }
    }
    
    @Override
    public String getSubsetBoundsRepresentationWCS() {
        return "(" + lowerBound + "," + upperBound + ")";
    }
    
    @Override
    public String getSubsetBoundsRepresentationWCPS() {
        return "(" + lowerBound + ":" + upperBound + ")";
    }
}
