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
package org.rasdaman.domain.owsmetadata;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Example:
 *
 * <ContactInfo>
 * <Phone>
 * <Voice>+1 301 555-1212</Voice>
 * <Facsimile>+1 301 555-1212</Facsimile>
 * </Phone>
 * <Address>
 * <DeliveryPoint>NASA Goddard Space Flight Center</DeliveryPoint>
 * <City>Greenbelt</City>
 * <AdministrativeArea>MD</AdministrativeArea>
 * <PostalCode>20771</PostalCode>
 * <Country>USA</Country>
 *
 * <ElectronicMailAddress>user@host.com</ElectronicMailAddress>
 * </Address>
 * </ContactInfo>
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = ContactInfo.TABLE_NAME)
public class ContactInfo {

    public static final String TABLE_NAME = "contact_info";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // Zero or one, optional
    @Column(name = "hours_of_service")
    private String hoursOfService;

    // Zero or one, optional
    @Column(name = "contact_instructions", length = 10000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String contactInstructions;

    // Zero or one, optional
    @Column(name = "online_resource", length = 1000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    // e.g: <ows:OnlineResource xlink:href="http://geoserver.org"/>
    private String onlineResource;

    // Zero or one, optional
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = Address.COLUMN_ID)
    private Address address;

    // Zero or one, optional
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = Phone.COLUMN_ID)
    private Phone phone;

    public ContactInfo() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHoursOfService() {
        return hoursOfService;
    }

    public void setHoursOfService(String hoursOfService) {
        this.hoursOfService = hoursOfService;
    }

    public String getContactInstructions() {
        return contactInstructions;
    }

    public void setContactInstructions(String contactInstructions) {
        this.contactInstructions = contactInstructions;
    }

    public String getOnlineResource() {
        return onlineResource;
    }

    public void setOnlineResource(String onlineResource) {
        this.onlineResource = onlineResource;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

}
