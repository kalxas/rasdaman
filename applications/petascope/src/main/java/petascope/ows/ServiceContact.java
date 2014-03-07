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
 * Java class for ows:ServiceContact elements.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
// NOTE: ServiceContact element should not be used to reference a web site of the service provider.
public class ServiceContact {

    private String individualName;
    private String positionName;
    private ContactInfo contactInfo;
    private String role;

    // Constructor
    ServiceContact () {
        contactInfo = new ContactInfo();
    }

    // Getters/Setters
    public String getIndividualName() {
        return (null == individualName) ? "" : individualName;
    }
    public void setIndividualName(String name) {
        individualName = name;
    }
    //
    public String getPositionName() {
        return (null == positionName) ? "" : positionName;
    }
    public void setPositionName(String position) {
        positionName = position;
    }
    //
    public ContactInfo getContactInfo() {
        return contactInfo;
    }
    //
    public String getRole() {
        return (null == role) ? "" : role;
    }
    public void setRole(String role) {
        this.role = role;
    }
} //~ ServiceProvider.ServiceContact
