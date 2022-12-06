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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

/**
 * 
 * NOTE: used only for WMS style from WCPS query fragment.
 * It keeps the subsets per layer from the map registries.
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WMSSubsetDimensionsRegistry {

    // test_wms_4326 -> [ansi("2015-01-01"), plev(30)]
    private HashMap<String, List<WcpsSubsetDimension>> map = new LinkedHashMap<>();

    public WMSSubsetDimensionsRegistry() {
        
    }

    public HashMap<String, List<WcpsSubsetDimension>> getMap() {
        return map;
    }

    public void add(String layerName,List<WcpsSubsetDimension> wcpsSubsetDimensions) {
        map.put(layerName, wcpsSubsetDimensions);
    }

    public List<WcpsSubsetDimension> getSubsetDimensions(String layerName) {
        return map.get(layerName);
    }
}
