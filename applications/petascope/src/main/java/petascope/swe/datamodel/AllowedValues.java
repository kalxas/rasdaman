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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nu.xom.Element;
import petascope.util.BigDecimalUtil;
import petascope.util.MiscUtil;
import static petascope.util.XMLSymbols.LABEL_ALLOWED_VALUES;
import static petascope.util.XMLSymbols.LABEL_INTERVAL;
import static petascope.util.XMLSymbols.NAMESPACE_SWE;
import static petascope.util.XMLSymbols.PREFIX_SWE;

/**
 * Defines the permitted values for the component as an enumerated list and/or a list of inclusive ranges.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class AllowedValues {

    // fields
    /**
     * List of values which constrain the feature space of an SWE Count or Quantity component.
     * The numbers shall be expressed in the same unit as the parent data component.
     * swe:AllowedValuesPropertyType : [0..*] value ((type="double"))
     * Not supported.
     */
    private final List<Double> values = new ArrayList<Double>(0);
    /**
     * List of inclusive intervals which constrain the feature space of an SWE Count or Quantity component.
     * The numbers in the interval(s) shall be expressed in the same unit as the parent data component.
     * swe:AllowedValuesPropertyType : [0..*] value ((type="RealPair"))
     */
    private final List<RealPair> intervals;
    /**
     * SIGNIFICANT FIGURES.
     * Examples (from Sec.7.2.18 of SWE Common Data Model Encoding Standard -- OGC 08-094r1):
     *   - All non-zero digits are considered significant. 123.45 has five significant figures: 1, 2, 3, 4 and 5;
     *   - Zeros between two non-zero digits are significant. 101.12 has five significant figures: 1, 0, 1, 1 and 2;
     *   - Leading zeros are not significant. 0.00052 has two significant figures: 5 and 2 and is equivalent to 5.2x10-4 and
     *     would be valid even if the number of significant figures is restricted to 2;
     *   - Trailing zeros are significant. 12.2300 has six significant figures: 1, 2, 2, 3, 0 and 0 and would thus be invalid if the
     *     number of significant figures is restricted to 4.
     * NOTE: The number of significant figures and/or an interval constraint (i.e. min/max
     * values) can help a software implementation choosing the best data type to use (i.e. `float'
     * or `double', `short', `int' or `long') to store values associated to a given data component.
     * @todo Implement significant figures.
     * swe:AllowedValuesPropertyType : [0..1] value ((type="integer"))
     *
     */
    private final Integer significantFigures;

    // constructor
    public AllowedValues(List<RealPair> intervalList) {
        significantFigures = null;
        intervals = new ArrayList<RealPair>(intervalList.size());
        for (RealPair interval : intervalList) {
            intervals.add(interval);
        }
    }
    // public AllowedValues(List<RealPair> intervalList, Integer figures) { ... }

    // access
    /**
     * Getter method for the allowed intervals.
     * @return The intervals that define the allowed values of an SWE component (Count or Quantity).
     */
    public Iterator<RealPair> getIntervalIterator() {
        return intervals.iterator();
    }
    // public Integer getSignificantFigures() { ... }

    // methods
    public Element toGML() {
        // <swe:AllowedValues>
        //   <swe:interval>-180 0</swe:interval>
        // </swe:AllowedValues>
        Element allowedValues = new Element(PREFIX_SWE + ":" + LABEL_ALLOWED_VALUES, NAMESPACE_SWE);
        Element interval;
        Iterator<RealPair> intervalIt = getIntervalIterator();
        RealPair pair;

        if (intervalIt.hasNext()) {
            while (intervalIt.hasNext()) {
                interval = new Element(PREFIX_SWE + ":" + LABEL_INTERVAL, NAMESPACE_SWE);
                pair = intervalIt.next();
                interval.appendChild(BigDecimalUtil.stripDecimalZeros(pair.getMin()) + " " + BigDecimalUtil.stripDecimalZeros(pair.getMax()));
                allowedValues.appendChild(interval);
            }
            return allowedValues;
        } else {
            return null;
        }
    }
}
