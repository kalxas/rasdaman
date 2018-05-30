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
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class TranslatedGridDimensionSubset {
    
    // Rasdaman axis order
    private final int gridAxisOrder;
    // For X,Y axis, e.g: [10:20, 0:5] means [10:20, 0:5], but for other dimension (e.g: [*:*, *:*, 0:5]) means
    // [*:*, *:*, 0] overlay [*:*, *:*, 1] overlay [*:*, *:*, 2] overlay [*:*, *:*, 3] overlay [*:*, *:*, 4] overlay [*:*, *:*, 5]
    private final boolean isNonXYAxis;
    
    // List of translated grid domains on axis (e.g: 0:2, 3:5, 6, 10:12)
    private final List<String> gridBounds = new ArrayList<>();
    
    public TranslatedGridDimensionSubset(int gridAxisOrder, boolean isNonXYAxis, List<ParsedSubset<Long>> gridBounds) {
        this.gridAxisOrder = gridAxisOrder;
        this.isNonXYAxis = isNonXYAxis;
        this.createGridBounds(gridBounds);
    }
    
    /**
     * Create a list of grid bounds which needs to regard nonXY axes properly.
     * Trimming needs to be separated to multiple slicing values (e.g: [0:3] -> 0,1,2,3
     */
    private void createGridBounds(List<ParsedSubset<Long>> inputGridBounds) {
        for (ParsedSubset<Long> parsedSubset : inputGridBounds) {
            String value;
            if (isNonXYAxis) {
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
            } else {
                // X, Y axes, it is always trimming
                value = parsedSubset.getLowerLimit() + ":" + parsedSubset.getUpperLimit();
                this.gridBounds.add(value);
            }
        }
    }

    public int getGridAxisOrder() {
        return gridAxisOrder;
    }

    public boolean isIsNonXYAxis() {
        return isNonXYAxis;
    }
    
    public List<String> getGridBounds() {
        return gridBounds;
    }
}
