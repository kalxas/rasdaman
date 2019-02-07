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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.rasdaman.domain.owsmetadata.Address;
import org.rasdaman.domain.owsmetadata.ContactInfo;
import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.rasdaman.domain.owsmetadata.Phone;
import org.rasdaman.domain.owsmetadata.ServiceContact;
import org.rasdaman.domain.owsmetadata.ServiceIdentification;
import org.rasdaman.domain.owsmetadata.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import petascope.exceptions.WCSException;
import org.rasdaman.repository.interfaces.OWSServiceMetadataRepository;
import petascope.util.ListUtil;

/**
 *
 * Service class to access to OwsServiceMetadata repository (data access class)
 * in database
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
@Transactional
public class OWSMetadataRepostioryService {

    @Autowired
    private OWSServiceMetadataRepository owsServiceMetadataRepository;

    private static final Logger log = LoggerFactory.getLogger(OWSMetadataRepostioryService.class);

    // cache the Ows Service metadata
    private static OwsServiceMetadata owsServiceMetadataCache;
    
    @PersistenceContext
    EntityManager entityManager;

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
                owsServiceMetadata = this.createDefaultOWSMetadataService();
                this.save(owsServiceMetadata);
                log.info("Creating default OWS Service Metadata for new database.");
            } else {
                log.debug("Read OWS Service Metadata from database.");
                owsServiceMetadata = metadatas.get(0);
                entityManager.clear();
            }
            
            owsServiceMetadata.setServiceTypeVersions();
            
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
    
    /**
     * This is used when an empty database is created and does not have any OWS
     * metadata
     * @return 
     */
    public OwsServiceMetadata createDefaultOWSMetadataService() {
        OwsServiceMetadata owsServiceMetadata = new OwsServiceMetadata();

        ServiceIdentification serviceIdentification = new ServiceIdentification();
        owsServiceMetadata.setServiceIdentification(serviceIdentification);
        serviceIdentification.setServiceTitle("rasdaman");
        serviceIdentification.setServiceAbstract("rasdaman server - free download from www.rasdaman.org");
        serviceIdentification.setServiceType("OGC WCS");
        owsServiceMetadata.setServiceTypeVersions();

        ServiceProvider serviceProvider = new ServiceProvider();
        owsServiceMetadata.setServiceProvider(serviceProvider);
        serviceProvider.setProviderName("Jacobs University Bremen");
        serviceProvider.setProviderSite("http://rasdaman.org/");        

        ServiceContact serviceContact = new ServiceContact();
        serviceProvider.setServiceContact(serviceContact);
        serviceContact.setIndividualName("Prof. Dr. Peter Baumann");
        serviceContact.setPositionName("Project Leader");

        ContactInfo contactInfo = new ContactInfo();
        serviceContact.setContactInfo(contactInfo);
        serviceContact.setRole("pointOfContact");

        Address address = new Address();
        contactInfo.setAddress(address);
        address.setDeliveryPoints(ListUtil.valuesToList("Campus Ring 1"));
        address.setCity("Bremen");
        address.setPostalCode("28759");
        address.setCountry("Germany");
        address.setElectronicMailAddresses(ListUtil.valuesToList("p.baumann@jacobs-university.de"));

        Phone phone = new Phone();
        contactInfo.setPhone(phone);
        phone.setVoicePhones(ListUtil.valuesToList(Phone.DEFAULT_VOICE_PHONE));
        
        return owsServiceMetadata;
    }
}
