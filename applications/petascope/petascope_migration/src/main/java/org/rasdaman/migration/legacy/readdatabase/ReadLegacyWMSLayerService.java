/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.legacy.readdatabase;

import java.sql.SQLException;
import java.util.List;
import org.rasdaman.migration.domain.legacy.LegacyDbMetadataSource;
import org.rasdaman.migration.domain.legacy.LegacyWMSLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Read the legacy WMS 1.3 layer to a LegacyWMSLayer object
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class ReadLegacyWMSLayerService {

    @Autowired
    private LegacyDbMetadataSource meta;

    /**
     * Read the legacy WMS layer from database
     *
     * @param legacyWMSLayerName
     * @return
     * @throws java.sql.SQLException
     */
    public LegacyWMSLayer read(String legacyWMSLayerName) throws SQLException {
        LegacyWMSLayer legacyWMSLayer = meta.getLegacyWMSLayer(legacyWMSLayerName);
        return legacyWMSLayer;
    }

    /**
     * Read all the layers in legacy WMS 1.3 table
     *
     * @return
     * @throws java.lang.Exception
     */
    public List<String> readlAllLayerNames() throws Exception {
        List<String> layerNames = meta.layers();

        return layerNames;
    }
}
