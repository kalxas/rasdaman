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
package petascope.wcps.metadata.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.wcps.exception.processing.CannotFindAxistIteratorException;
import petascope.wcps.exception.processing.InvalidRedefineAxisIteratorException;
import petascope.wcps.subset_axis.model.AxisIterator;

/**
 * This class has the purpose of keeping information about axis iterator aliases
 * inside 1 query e.g: for c in (mr) return encode(coverage cov $px x(0:20), $py
 * y(20:50) values c[i($px), j($py), "png") means that $px is an alias for
 * x(0:20) and $px is an alias for y(20:50) in this query.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AxisIteratorAliasRegistry {

    // NOTE: axis iterator alias can only be unique for a subset dimension (e.g: $px x(0:20))
    private final LinkedHashMap<String, AxisIterator> axisIteratorMappings = new LinkedHashMap<String, AxisIterator>();

    // maintains a list of rasql subsets that represent axis iterators.
    private final List<String> rasqlAxisIterators = new ArrayList<>();

    public AxisIteratorAliasRegistry() {

    }

    public void addAxisIteratorAliasMapping(String axisIteratorAlias, AxisIterator axisIterator) {
        AxisIterator value = axisIteratorMappings.get(axisIteratorAlias);
        if (value != null) {
            // throw an exception when redefine the axis iterator alias
            throw new InvalidRedefineAxisIteratorException(axisIteratorAlias, axisIterator.getSubsetDimension());
        } else {
            // if key does not exist then need to add key first then add value for this key
            axisIteratorMappings.put(axisIteratorAlias, axisIterator);
        }
    }

    public AxisIterator getAxisIterator(String axisIteratorAlias) {
        AxisIterator axisIterator = null;
        if (axisIteratorMappings.get(axisIteratorAlias) != null) {
            axisIterator = axisIteratorMappings.get(axisIteratorAlias);
        }

        if (axisIterator == null) {
            throw new CannotFindAxistIteratorException(axisIteratorAlias);
        }

        return axisIterator;
    }

    public List<String> getRasqlAxisIterators() {
        return rasqlAxisIterators;
    }
    
    /**
     * e.g: $pt -> time axis with lowerBound "2016" and upperBound "2017"
     */
    public LinkedHashMap<String, AxisIterator> getAliasAxisIteratorMap() {
        return this.axisIteratorMappings;
    }

    /**
     * Adds a subset to the list of maintained rasql subsets representing axis iterator aliases.
     * @param rasqlSubset
     */
    public void addRasqlAxisIterator(String rasqlSubset){
        rasqlAxisIterators.add(rasqlSubset);
    }

}
