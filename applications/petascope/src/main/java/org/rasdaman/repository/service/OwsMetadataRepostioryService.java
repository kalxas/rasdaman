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
package org.rasdaman.repository.service;

import java.util.List;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.repository.interfaces.OwsServiceMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import petascope.exceptions.WCSException;

/**
 *
 * Service class to access to OwsServiceMetadata repository (data access class)
 * in database
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
@Transactional
public class OwsMetadataRepostioryService {

    @Autowired
    private OwsServiceMetadataRepository owsServiceMetadataRepository;

    private static final Logger log = LoggerFactory.getLogger(OwsMetadataRepostioryService.class);

    // cache the Ows Service metadata
    private static OwsServiceMetadata owsServiceMetadataCache;

    /**
     *
     * Read persisted OwsServiceMetadata from database
     *
     * @return
     * @throws petascope.exceptions.WCSException
     */
    public OwsServiceMetadata read() throws WCSException {

        OwsServiceMetadata owsServiceMetadata = new OwsServiceMetadata();
        if (owsServiceMetadataCache != null) {
            // Read from the cache
            owsServiceMetadata = owsServiceMetadataCache;
        } else {
            // This should contain only 1 object for all services
            List<OwsServiceMetadata> metadatas = (List<OwsServiceMetadata>) owsServiceMetadataRepository.findAll();
            if (metadatas.isEmpty()) {
                //throw new WCSException(ExceptionCode.InternalComponentError, "There is no OWS Service metadata persisted in database, please migrate it first.");
                // If database is empty and not migrate from old database, yet, then create a default object and persist it.
                owsServiceMetadata = owsServiceMetadata.createDefaultOWSMetadataService();
                this.save(owsServiceMetadata);
                log.info("Creating default OWS Service Metadata for new database.");
            } else {
                log.debug("Read OWS Service Metadata from database.");
                owsServiceMetadata = metadatas.get(0);
            }
            
            // Then keep it in cache
            owsServiceMetadataCache = owsServiceMetadata;
        }

        return owsServiceMetadata;
    }

    /**
     * Save a new ows service metadata to database NOTE: only have 1 OWS
     * metadata, so remove all the entries in cache map (it should only have 1)
     *
     * @param owsServiceMetadata
     * @return
     */
    public OwsServiceMetadata save(OwsServiceMetadata owsServiceMetadata) {
        this.owsServiceMetadataRepository.save(owsServiceMetadata);
        owsServiceMetadataCache = owsServiceMetadata;

        return owsServiceMetadata;
    }

    /**
     * Delete all the persisted ows service metadata (should be only 1)
     */
    public void deleteAll() {
        this.owsServiceMetadataRepository.deleteAll();
        owsServiceMetadataCache = null;
    }
}
