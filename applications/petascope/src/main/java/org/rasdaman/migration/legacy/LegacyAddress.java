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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.legacy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Java class for ows:Address elements.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class LegacyAddress {

    private Set<String> deliveryPoints;
    private String city;
    private String administrativeArea;
    private String postalCode;
    private String country;
    private Set<String> emailAddresses;

    // Constructor
    LegacyAddress() {
        deliveryPoints = new HashSet<String>();
        emailAddresses = new HashSet<String>();
    }

    // Getters/Setters
    public Set<String> getDeliveryPoints() {
        return deliveryPoints;
    }
    public void addDeliveryPoint(String point) {
        if (null != point && !point.isEmpty()) {
            deliveryPoints.add(point);
        }
    }
    //
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    //
    public String getAdministrativeArea() {
        return administrativeArea;
    }
    public void setAdministrativeArea(String area) {
        administrativeArea = area;
    }
    //
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String code) {
        postalCode = code;
    }
    //
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    //
    public Set<String> getEmailAddresses() {
        return emailAddresses;
    }
    public void addEmailAddress(String email) {
        if (null != email && !email.isEmpty()) {
            emailAddresses.add(email);
        }
    }

    // Methods
    /**
     * Return an ordered dictionary of OWS GML types and associated values
     * of the contact information that has been filled.
     */
    public List<LegacyPair<String, String>> getAddressMetadata() {
        List<LegacyPair<String, String>> availableInfo = new ArrayList<LegacyPair<String, String>>();

        // Keep XML order
        for (String deliveryPoint : deliveryPoints) {
            availableInfo.add(LegacyPair.of(LegacyXMLSymbols.LABEL_DELIVERY_POINT, deliveryPoint));
        }
        if (null != city) {
            availableInfo.add(LegacyPair.of(LegacyXMLSymbols.LABEL_CITY, city));
        }
        if (null != administrativeArea) {
            availableInfo.add(LegacyPair.of(LegacyXMLSymbols.LABEL_ADMINISTRATIVE_AREA, administrativeArea));
        }
        if (null != postalCode) {
            availableInfo.add(LegacyPair.of(LegacyXMLSymbols.LABEL_POSTAL_CODE, postalCode));
        }
        if (null != country) {
            availableInfo.add(LegacyPair.of(LegacyXMLSymbols.LABEL_COUNTRY, country));
        }
        for (String emailAddress : emailAddresses) {
            availableInfo.add(LegacyPair.of(LegacyXMLSymbols.LABEL_EMAIL_ADDRESS, emailAddress));
        }

        return availableInfo;
    }
}

