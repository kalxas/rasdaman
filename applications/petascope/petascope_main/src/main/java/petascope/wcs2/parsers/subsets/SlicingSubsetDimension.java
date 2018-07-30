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
* Representation of a slice subset in a WCS request.
*/
public class SlicingSubsetDimension extends AbstractSubsetDimension {

    private String bound;

    public SlicingSubsetDimension(String dimension, String bound) {
        this(dimension, null, bound);
    }

    public SlicingSubsetDimension(String dimension, String crs, String bound) {
        super(dimension, crs);
        this.bound = bound;
        isNumeric = !bound.matches(QUOTED_SUBSET);
    }

    public String getBound() {
        return bound;
    }

    public void setBound(String bound) {
        this.bound = bound;
        isNumeric = !bound.matches(QUOTED_SUBSET);
    }

    public void setSlicePoint(Double value) {
        setBound(value.toString());
        isNumeric = true;
    }

    /**
     * Integrity of time subsets (quoted subsets): valid and ordered bounds.
     * @throws petascope.exceptions.WCSException
     */
    public void timestampSubsetCheck() throws WCSException {
        if (null != getBound() && getBound().matches(QUOTED_SUBSET)) {
            if (!TimeUtil.isValidTimestamp(getBound())) {
                throw new WCSException(ExceptionCode.InvalidParameterValue, "Timestamp \"" + getBound() + "\" is not valid or supported.");
            }
        }
    }

    @Override
    public String getSubsetBoundsRepresentationWCS() {
        return "(" + bound + ")";
    }
    
    @Override
    public String getSubsetBoundsRepresentationWCPS() {
        return "(" + bound + ")";
    }
}
