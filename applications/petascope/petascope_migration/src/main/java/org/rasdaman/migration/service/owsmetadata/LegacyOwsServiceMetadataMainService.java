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
package org.rasdaman.migration.service.owsmetadata;

import org.rasdaman.migration.domain.legacy.LegacyServiceMetadata;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.domain.owsmetadata.ServiceIdentification;
import org.rasdaman.domain.owsmetadata.ServiceProvider;
import org.rasdaman.migration.legacy.readdatabase.ReadLegacyOwsServiceMetadataService;
import org.rasdaman.repository.service.OWSMetadataRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Class is used to translate the legacy OWS Service metadata
 * (ServiceIndentification, ServiceProvider) from WCS GetCapabilities to
 * database.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class LegacyOwsServiceMetadataMainService {

    private static final Logger log = LoggerFactory.getLogger(LegacyOwsServiceMetadataMainService.class);

    @Autowired
    private ReadLegacyOwsServiceMetadataService readLegacyOwsServiceMetadataService;

    @Autowired
    private ServiceIdentificationTranslatingService serviceIdentificationTranslatingService;
    @Autowired
    private ServiceProviderTranslatingService serviceProviderTranslatingService;

    @Autowired
    private OWSMetadataRepostioryService owsMetadataRepostioryService;

    /**
     * Build a OwsServiceMetadata object from legacy OWS tables
     *
     * @return OwsServiceMetadata
     */
    private OwsServiceMetadata convertToInsert() {
        OwsServiceMetadata serviceMetadata = new OwsServiceMetadata();
        // Read the legacy metadata from old DbMetadataSource
        LegacyServiceMetadata legacyMetadata = readLegacyOwsServiceMetadataService.read();

        // Build ServiceIndentification
        ServiceIdentification serviceIdentification = serviceIdentificationTranslatingService.create(legacyMetadata.getIdentification());
        serviceMetadata.setServiceIdentification(serviceIdentification);

        // Build ServiceProvider
        ServiceProvider serviceProvider = serviceProviderTranslatingService.create(legacyMetadata.getProvider());
        serviceMetadata.setServiceProvider(serviceProvider);

        return serviceMetadata;
    }

    public void persist() {
        // NOTE: if OwsServiceMetadata was persisted, remove it and insert a new one to be consistent
        this.owsMetadataRepostioryService.deleteAll();
        OwsServiceMetadata serviceMetadata = this.convertToInsert();

        // persist the serviceMetadata
        this.owsMetadataRepostioryService.save(serviceMetadata);
        log.debug("OWS Service Metadata is persisted in database.");
    }
}
