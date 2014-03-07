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

/**
 * Java class for ows:ContactInfo elements.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class ContactInfo {

    private String phone;
    private Address contactAddress;
    private String hoursOfService;
    private String contactInstructions;

    // Constructor
    ContactInfo () {
        contactAddress = new Address();
    }

    // Getters/Setters
    public String getPhone() {
        return (null == phone) ? "" : phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    //
    public Address getAddress() {
        return contactAddress;
    }
    //
    public String getHoursOfService() {
        return (null == hoursOfService) ? "" : hoursOfService;
    }
    public void setHoursOfService(String hours) {
        hoursOfService = hours;
    }
    //
    public String getInstructions() {
        return (null == contactInstructions) ? "" : contactInstructions;
    }
    public void setInstructions(String instructions) {
        contactInstructions = instructions;
    }

    // Methods
    public boolean isEmpty() {
        return getPhone().isEmpty() &&
                getAddress().getAddressMetadata().isEmpty() &&
                getHoursOfService().isEmpty() &&
                getInstructions().isEmpty();
    }
}
