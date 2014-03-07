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

package petascope.swe.datamodel;

import java.math.BigDecimal;
import petascope.util.Pair;

/**
 * A pair of doubles: used to define [min,max] intervals (e.g. allowed values).
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class RealPair {

    // fields
    /**
     * [min,max] pair.
     * <simpleType name="RealPair">
     *   <restriction>
     *     <simpleType>
     *       <list itemType="double"/>
     *     </simpleType>
     *     <length value="2"/>
     *   </restriction>
     * </simpleType>
     */
    private final Pair<BigDecimal,BigDecimal> interval;

    // constructor
    /**
     * New SWE RealPair with specified min and max values.
     * @param min
     * @param max
     */
    public RealPair(BigDecimal min, BigDecimal max) {
        interval = Pair.of(min, max);
    }

    // access
    /**
     * Getter method for the minimum value of the interval.
     * @return The minimum bound of this pair.
     */
    public BigDecimal getMin() {
        return interval.fst;
    }
    /**
     * Getter method for the maximum value of the interval.
     * @return The maximum bound of this pair.
     */
    public BigDecimal getMax() {
        return interval.snd;
    }
}
