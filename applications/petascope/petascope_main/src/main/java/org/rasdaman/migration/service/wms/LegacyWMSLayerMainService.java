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
package org.rasdaman.migration.service.wms;

import java.sql.SQLException;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.migration.domain.legacy.LegacyWMSLayer;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WMSException;

/**
 * Class which translates the legacy WMS 1.3.0 metadata to new one and persist
 * to database
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class LegacyWMSLayerMainService {

    private static final Logger log = LoggerFactory.getLogger(LegacyWMSLayerMainService.class);

    @Autowired
    private WMSLayerTranslatingService wmsTranslatingService;

    @Autowired
    private WMSRepostioryService wmsRepostioryService;

    public LegacyWMSLayerMainService() {

    }

    /**
     * Convert the legacy WMS layer's metadata to new WMS Metadata.
     *
     * @return
     */
    private Layer convertToInsert(LegacyWMSLayer legacyWMSLayer) throws SQLException, PetascopeException, SecoreException, SecoreException, WMSException {
        Layer layer = this.wmsTranslatingService.create(legacyWMSLayer);

        return layer;
    }

    /**
     * Check if a WMS legacy layer already migrated to new database
     *
     * @param legacyLayerName
     * @return
     */
    public boolean layerNameExist(String legacyLayerName) {
        return this.wmsRepostioryService.layerNameExist(legacyLayerName);
    }

    /**
     * Create WMS 1.3 layer from the legacy layer
     *
     * @param legacyWMSLayer
     * @throws java.sql.SQLException
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     * @throws petascope.exceptions.WMSException
     */
    public void persist(LegacyWMSLayer legacyWMSLayer) throws SQLException, PetascopeException, SecoreException, WMSException {
        Layer layer = this.convertToInsert(legacyWMSLayer);
        // NOTE: if a coverage's metadata is not migrated yet, so WMS layer should not be migrated
        // or a coverage's metadata does not exist, this WMS layer should not exist.
        if (layer != null) {
            // persist the translated WMS 1.3 layers's metadata        
            this.wmsRepostioryService.saveLayer(layer);
            log.debug("WMS layer '" + layer.getName() + "' is persisted to database.");
        }
    }
}
