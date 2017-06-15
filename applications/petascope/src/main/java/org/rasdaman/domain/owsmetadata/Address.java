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

import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * Example:
 *
 * <ows:Address>
 * <ows:DeliveryPoint>1352 Lighthouse Ave.</ows:DeliveryPoint>
 * <ows:City>Pacific Grove</ows:City>
 * <ows:AdministrativeArea>CA</ows:AdministrativeArea>
 * <ows:PostalCode>93950</ows:PostalCode>
 * <ows:Country>USA</ows:Country>
 * <ows:ElectronicMailAddress>bob.simons@noaa.gov</ows:ElectronicMailAddress>
 * </ows:Address>
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = Address.TABLE_NAME)
public class Address {

    public static final String TABLE_NAME = "address";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    // Zero or more, optional
    private List<String> deliveryPoints;

    // Zero or one, optional
    @Column(name = "city")
    private String city;

    // Zero or one, optional
    @Column(name = "administrative_area")
    private String administrativeArea;

    @Column(name = "postal_code")
    // Zero or One (validation schema), optional
    private String postalCode;

    
    @Column(name = "country")
    // Zero or One (validation schema), optional
    private String country;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    // Zero ore more, optional
    private List<String> electronicMailAddresses;

    public Address() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getDeliveryPoints() {
        return deliveryPoints;
    }

    public void setDeliveryPoints(List<String> deliveryPoints) {
        this.deliveryPoints = deliveryPoints;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAdministrativeArea() {
        return administrativeArea;
    }

    public void setAdministrativeArea(String administrativeArea) {
        this.administrativeArea = administrativeArea;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getElectronicMailAddresses() {
        return electronicMailAddresses;
    }

    public void setElectronicMailAddresses(List<String> electronicMailAddresses) {
        this.electronicMailAddresses = electronicMailAddresses;
    }
}
