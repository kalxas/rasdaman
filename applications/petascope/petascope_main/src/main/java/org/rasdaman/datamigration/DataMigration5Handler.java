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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.datamigration;
import java.util.List;
import java.util.Map;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;

/**
 * Class to handle data migration version number 5
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class DataMigration5Handler extends AbstractDataMigrationHandler {
    
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    
    public DataMigration5Handler() {
        // NOTE: update this by one for new handler class
        this.migrationVersion = 5;
        this.handlerId = "d96d9e26-9fbb-11ec-887a-509a4cb4e064";
    }

    @Override
    public void migrate() throws PetascopeException, SecoreException {
        
        List<Layer> layers = this.wmsRepostioryService.readAllLocalLayers();
        for (Layer layer : layers) {
            if (layer.getStyles().size() > 0) {
                // NOTE: v10, if a layer has at least one style, then this style is set as default style
                layer.getStyles().get(0).setDefaultStyle(true);
                wmsRepostioryService.saveLayer(layer);
            }
        }
        
    }
    
}