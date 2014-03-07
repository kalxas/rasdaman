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
package petascope.wcs2.helpers.rangesubsetting;

import java.util.ArrayList;
import java.util.List;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.Pair;
import petascope.wcs2.parsers.GetCoverageMetadata.RangeField;

/**
 * This class represents a Range Subset type as described in OGC-12-040
 * standard, Requirement2.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public class RangeSubset {

    /**
     * Empty constructor
     */
    public RangeSubset() {
        items = new ArrayList<RangeItem>();
        coverageComponents = new ArrayList<String>();
    }

    /**
     * Adds a RangeItem to be taken into consideration when the selected ranges
     * are returned
     *
     * @param item the item to be selected
     */
    public void addRangeItem(RangeItem item) throws WCSException {
        if (!this.items.contains(item)) {
            this.items.add(item);
        }
    }

    /**
     * @todo What happens when the index of end smaller index of begin e.g.
     * green:red Returns the selected components from the range subset request
     * right now they are ignored
     * @return an array of strings representing the names of the range e.g
     * ([red, green, blue]
     */
    public ArrayList<String> getSelectedComponents() throws WCSException {
        ArrayList<String> ret = new ArrayList<String>(coverageComponents.size());
        for (RangeItem rangeItem : items) {
            Pair<String, String> interval = rangeItem.getInterval();
            if (!this.coverageComponents.contains(interval.fst) || !this.coverageComponents.contains(interval.snd)) {
                throw new WCSException(ExceptionCode.NoSuchField);
            }
            int begin = this.coverageComponents.indexOf(interval.fst);
            int end = this.coverageComponents.indexOf(interval.snd);
            if (begin == end) {
                ret.add(this.coverageComponents.get(begin));
            } else {
                for (int i = begin; i <= end; i++) {
                    ret.add(this.coverageComponents.get(i));
                }
            }
        }
        return ret;
    }

    /**
     * String representation of the class
     *
     * @return
     */
    @Override
    public String toString() {
        return "RangeSubset{" + "items=" + items + '}';
    }

    /**
     * Checks if any rangeItem exists in the container
     *
     * @return true if none, false otherwise
     */
    public Boolean isEmpty() {
        return this.items.isEmpty();
    }

    /**
     * Set the coverage ranges for the coverage upon which the request is being
     * made.
     *
     * @param coverageComponents the coverage ranges
     */
    public void setCoverageComponents(List<RangeField> coverageComponents) {
        for (RangeField rf : coverageComponents) {
            this.coverageComponents.add(rf.getFieldName());
        }
    }
    private ArrayList<String> coverageComponents;
    private ArrayList<RangeItem> items;
}
