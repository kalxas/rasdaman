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
package org.rasdaman.domain.wmts;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Model class for WMTS TileMatrixSet
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class TileMatrixSet {
    
    private String name;
    // e.g. test_wms_2 -> TileMatrix
    private Map<String, TileMatrix> tileMatrixMap = new LinkedHashMap<>();

    public TileMatrixSet(String name, Map<String, TileMatrix> tileMatrixMap) {
        this.name = name;
        this.tileMatrixMap = tileMatrixMap;
    }

    public String getName() {
        return name;
    }

    public Map<String, TileMatrix> getTileMatrixMap() {
        return tileMatrixMap;
    }
    
}
