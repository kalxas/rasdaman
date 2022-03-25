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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * OGC 08-005
 *
 * Information for contacting service provider (CI_ResponsibleParty)
 *
 * Example:
 *
 * <ServiceContact>
 * <IndividualName>Jeff Smith, Server Administrator</IndividualName>
 * <PositionName>Computer Scientist</PositionName>
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
 * <ows:OnlineResource xlink:href="http://rasdaman.org"/>
 * <ows:Role>string</ows:Role>
 * </ServiceContact>
 *
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = ServiceContact.TABLE_NAME)
public class ServiceContact {

    public static final String TABLE_NAME = "service_contact";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "individual_name")
    // One, mandatory
    private String individualName;

    @Column(name = "position_name")
    // One, mandatory
    private String positionName;

    @Column(name = "role")
    // One, mandatory
    private String role;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = ContactInfo.COLUMN_ID)
    // One, mandatory
    private ContactInfo contactInfo;

    public ServiceContact() {

    }

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

}
