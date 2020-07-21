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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wms.handlers.model;

import java.util.ArrayList;
import java.util.List;
import petascope.wcps.metadata.model.ParsedSubset;

/**
 * A model object to store the translated grid dimension (of an axis)
 * for WMS GetMap service.
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class TranslatedGridDimensionSubset {
    
    private String axisLabel;
    
    // List of translated grid domains as slicing points (e.g: 0,1,2,3 for interval [0:3])
    private final List<String> gridBounds = new ArrayList<>();
    
    public TranslatedGridDimensionSubset(String axisLabel, List<ParsedSubset<Long>> gridBounds) {
        this.axisLabel = axisLabel;
        this.createGridBounds(gridBounds);
    }
    
    /**
     * Create a list of grid bounds which needs to regard nonXY axes properly.
     * Trimming needs to be separated to multiple slicing values (e.g: [0:3] -> 0,1,2,3
     */
    private void createGridBounds(List<ParsedSubset<Long>> inputGridBounds) {
        for (ParsedSubset<Long> parsedSubset : inputGridBounds) {
            String value;
            // e.g: time, dim_pressure axis...
            if (parsedSubset.getLowerLimit().equals(parsedSubset.getUpperLimit())) {
                // slicing
                value = parsedSubset.getLowerLimit().toString();
                this.gridBounds.add(value);
            } else {
                for (Long i = parsedSubset.getLowerLimit(); i <= parsedSubset.getUpperLimit(); i++) {
                    // also slicing
                    value = i.toString();
                    this.gridBounds.add(value);
                }
            }
        }
    }


    public String getAxisLabel() {
        return axisLabel;
    }
    public List<String> getGridBounds() {
        return gridBounds;
    }
}
