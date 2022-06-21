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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.metadata.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import static petascope.core.CrsDefinition.LONGITUDE_AXIS_LABEL_EPGS_VERSION_0;
import petascope.util.CrsUtil;
import petascope.util.StringUtil;

/**
 * Store the alias only for the sorted axis from SORT operator.
 * 
 * e.g. SORT $c ALONG time BY coverageExpression
 * 
 * then sorted axis label: time is added to this registry.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SortedAxisIteratorAliasRegistry {
    
    // e.g. time -> i0[0], Lat -> i1[0], ...
    private Map<String, String> registriesMap = new LinkedHashMap<>();
    
    public void add(String axisLabel) {
        if (!this.registriesMap.containsKey(axisLabel)) {
            int size = this.registriesMap.size();
            // e.g. i,j,k
            this.registriesMap.put(this.getUnifiedLongAxisLabelIfAny(axisLabel), StringUtil.getIteratorLabel(size));
        }
    }
    
    /**
     * e.g. axisLabel: time -> i
     *                 Lat -> j
     */
    public String getIteratorLabel(String axisLabel) {
        String result = this.registriesMap.get(this.getUnifiedLongAxisLabelIfAny(axisLabel));
        return result;
    }
    
    /**
     * Return the axis iterator label representation in rasql queries.
     * e.g. time -> i[0]
     *      Lat -> j[0]
     */
    public String getIteratorLabelRepresentation(String axisLabel) {
        // e.g. i
        String iteratorLabel = this.getIteratorLabel(axisLabel);
        String result = null;
        if (iteratorLabel != null) {
            result = iteratorLabel + "[0]";
        }
        
        return result;
    }
    
    public void remove(String axisLabel) {
        this.registriesMap.remove(this.getUnifiedLongAxisLabelIfAny(axisLabel));
    }
    
    /**
     * e.g. Long or Lon -> longitude axis
     */
    public String getUnifiedLongAxisLabelIfAny(String axisLabel) {
        if (CrsUtil.isLongitudeAxis(axisLabel, axisLabel)) {
            return LONGITUDE_AXIS_LABEL_EPGS_VERSION_0;
        }
        return axisLabel;
    }
}
