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
 * Java class for ows:ServiceProvider elements.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class ServiceProvider {

    private String providerName;
    private String providerSite;
    private ServiceContact serviceContact;

    // Constructors
    public ServiceProvider (String name) {
        providerName = name;
        serviceContact = new ServiceContact(); // mandatory but can be empty
    }
    public ServiceProvider (String name, String site) {
        this(name);
        providerSite = site;
    }

    // Getters/Setters
    public String getName() {
        return providerName;
    }
    //
    public String getSite() {
        return (null == providerSite) ? "" : providerSite;
    }
    public void setSite(String site) {
        providerSite = site;
    }
    //
    public ServiceContact getContact() {
        return serviceContact;
    }
} //~ ServiceProvider
