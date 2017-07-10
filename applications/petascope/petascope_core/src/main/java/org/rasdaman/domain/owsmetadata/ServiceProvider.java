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
 *
 * OWS 05-008
 *
 * The ServiceProvider section of a service metadata document contains metadata
 * about the organization operating this server.
 *
 * Detail: 7.4.4 ServiceProvider section contents
 *
 * Example:
 *
 * <ows:ServiceProvider>
 * <ows:ProviderName>NOAA Environmental Research Division</ows:ProviderName>
 * <ows:ProviderSite xlink:href="http://127.0.0.1:8080/cwexperimental"/>
 * <ows:ServiceContact>
 * <ows:IndividualName>Bob Simons</ows:IndividualName>
 * <ows:ContactInfo>
 * <ows:Phone>
 * <ows:Voice>831-658-3205</ows:Voice>
 * </ows:Phone>
 * <ows:Address>
 * <ows:DeliveryPoint>1352 Lighthouse Ave.</ows:DeliveryPoint>
 * <ows:City>Pacific Grove</ows:City>
 * <ows:AdministrativeArea>CA</ows:AdministrativeArea>
 * <ows:PostalCode>93950</ows:PostalCode>
 * <ows:Country>USA</ows:Country>
 * <ows:ElectronicMailAddress>bob.simons@noaa.gov</ows:ElectronicMailAddress>
 * </ows:Address>
 * </ows:ContactInfo>
 * </ows:ServiceContact>
 * </ows:ServiceProvider>
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = ServiceProvider.TABLE_NAME)
public class ServiceProvider {

    public static final String TABLE_NAME = "service_provider";
    public static final String COLUMN_ID = TABLE_NAME + "_id";    
    
    // NOTE: This value does not exist from legacy OWS Service metadata for WCS and it is needed for WMS Addres
    public static final String DEFAULT_DELIVERY_POINT = "Campus Ring 1";
    
    @Id
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "provider_name")
    // One, mandatory    
    private String providerName;

    @Column(name = "provider_site")
    // Zero or one, optional    
    private String providerSite;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = ServiceContact.COLUMN_ID)
    // One, mandatory
    private ServiceContact serviceContact;

    public ServiceProvider() {
        
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderSite() {
        return providerSite;
    }

    public void setProviderSite(String providerSite) {
        this.providerSite = providerSite;
    }

    public ServiceContact getServiceContact() {
        return serviceContact;
    }

    public void setServiceContact(ServiceContact serviceContact) {
        this.serviceContact = serviceContact;
    }
    
}
