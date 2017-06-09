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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.rasdaman.migration.legacy.LegacyAddress;
import org.rasdaman.migration.legacy.LegacyContactInfo;
import org.rasdaman.migration.legacy.LegacyServiceContact;
import org.rasdaman.migration.legacy.LegacyServiceProvider;
import org.rasdaman.domain.owsmetadata.Address;
import org.rasdaman.domain.owsmetadata.ContactInfo;
import org.rasdaman.domain.owsmetadata.ServiceContact;
import org.rasdaman.domain.owsmetadata.ServiceProvider;
import org.rasdaman.domain.owsmetadata.Phone;
import org.springframework.stereotype.Service;

/**
 * Class which translates the legacy OWS ServiceIdentification to new one and
 * persist to database
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class ServiceProviderTranslatingService {

    public ServiceProviderTranslatingService() {

    }

    /**
     * Create a new ServiceIdenfitication from the legacy
     *
     * @param legacy
     * @return
     */
    public ServiceProvider create(LegacyServiceProvider legacy) {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProviderName(legacy.getName());
        serviceProvider.setProviderSite(legacy.getSite());

        // Contact name, position, role, Address, Telephones
        ServiceContact serviceContact = this.createServiceContact(legacy.getContact());
        serviceProvider.setServiceContact(serviceContact);

        return serviceProvider;
    }

    /**
     * Create the object ServiceContact from legacy
     *
     * @param contact
     * @return
     */
    private ServiceContact createServiceContact(LegacyServiceContact contact) {
        ServiceContact serviceContact = new ServiceContact();

        serviceContact.setIndividualName(contact.getIndividualName());
        serviceContact.setPositionName(contact.getPositionName());
        serviceContact.setRole(contact.getRole());

        ContactInfo contactInfo = this.createContactInfo(contact.getContactInfo());
        serviceContact.setContactInfo(contactInfo);

        return serviceContact;
    }

    /**
     * Create ContactInfo object from legacy
     *
     * @param contactInfo
     * @return
     */
    private ContactInfo createContactInfo(LegacyContactInfo legacy) {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setHoursOfService(legacy.getHoursOfService());
        contactInfo.setContactInstructions(legacy.getInstructions());
        // As this information does not exist in legacy WCS GetCapabilities
        contactInfo.setOnlineResource(null);

        Address address = this.createAddress(legacy.getAddress());
        contactInfo.setAddress(address);

        Phone telephone = this.createTelephone(legacy);
        contactInfo.setPhone(telephone);

        return contactInfo;
    }

    /**
     * Create Address object from legacy
     *
     * @param contactInfo
     * @return
     */
    private Address createAddress(LegacyAddress legacy) {
        Address address = new Address();
        address.setAdministrativeArea(legacy.getAdministrativeArea());
        address.setCity(legacy.getCity());
        address.setCountry(legacy.getCountry());

        List<String> deliveryPoints = new ArrayList<>();
        deliveryPoints.addAll(legacy.getDeliveryPoints());
        address.setDeliveryPoints(deliveryPoints);
        if (address.getDeliveryPoints().isEmpty()) {
            // NOTE: Make up this address from legacy WMS Address value as it is needed for WMS and does not exist in legacy OWS Service Metadata
            address.setDeliveryPoints(Arrays.asList(ServiceProvider.DEFAULT_DELIVERY_POINT));
        }

        List<String> emailAddresses = new ArrayList<>();
        emailAddresses.addAll(legacy.getEmailAddresses());
        address.setElectronicMailAddresses(emailAddresses);

        address.setPostalCode(legacy.getPostalCode());

        return address;
    }

    /**
     * Create Telephone object from legacy
     *
     * @param legacy
     * @return
     */
    private Phone createTelephone(LegacyContactInfo legacy) {
        Phone telephone = new Phone();
        telephone.setVoicePhones(legacy.getVoicePhones());
        if (telephone.getVoicePhones().isEmpty()) {
            // Use the default voice phone
            telephone.setVoicePhones(Arrays.asList(Phone.DEFAULT_VOICE_PHONE));
        }
        telephone.setFacsimilePhone(legacy.getFacsimilePhones());

        return telephone;
    }
}
