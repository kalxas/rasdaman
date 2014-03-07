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
package petascope.ows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import petascope.util.Pair;
import petascope.util.XMLSymbols;

/**
 * Java class for ows:Address elements.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class Address {

    private Set<String> deliveryPoints;
    private String city;
    private String administrativeArea;
    private String postalCode;
    private String country;
    private Set<String> emailAddresses;

    // Constructor
    Address () {
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
        return (null == city) ? "" : city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    //
    public String getAdministrativeArea() {
        return (null == administrativeArea) ? "" : administrativeArea;
    }
    public void setAdministrativeArea(String area) {
        administrativeArea = area;
    }
    //
    public String getPostalCode() {
        return (null == postalCode) ? "" : postalCode;
    }
    public void setPostalCode(String code) {
        postalCode = code;
    }
    //
    public String getCountry() {
        return (null == country) ? "" : country;
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
     * @return
     */
    public List<Pair<String, String>> getAddressMetadata() {
        List<Pair<String, String>> availableInfo = new ArrayList<Pair<String, String>>();

        // Keep XML order
        for (String deliveryPoint : deliveryPoints) {
            availableInfo.add(Pair.of(XMLSymbols.LABEL_DELIVERY_POINT, deliveryPoint));
        }
        if (null != city) {
            availableInfo.add(Pair.of(XMLSymbols.LABEL_CITY, city));
        }
        if (null != administrativeArea) {
            availableInfo.add(Pair.of(XMLSymbols.LABEL_ADMINISTRATIVE_AREA, administrativeArea));
        }
        if (null != postalCode) {
            availableInfo.add(Pair.of(XMLSymbols.LABEL_POSTAL_CODE, postalCode));
        }
        if (null != country) {
            availableInfo.add(Pair.of(XMLSymbols.LABEL_COUNTRY, country));
        }
        for (String emailAddress : emailAddresses) {
            availableInfo.add(Pair.of(XMLSymbols.LABEL_EMAIL_ADDRESS, emailAddress));
        }

        return availableInfo;
    }
}

