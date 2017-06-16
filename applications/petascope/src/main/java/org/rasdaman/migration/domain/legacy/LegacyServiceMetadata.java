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
package org.rasdaman.migration.domain.legacy;

import java.util.List;
import org.rasdaman.migration.domain.legacy.LegacyServiceIdentification;
import org.rasdaman.migration.domain.legacy.LegacyServiceProvider;

/**
 * A place to store OWS service capabilities.
 * Contents are read from petascopedb tables (petascopedb=# \dt ps*_service_*).
 * Nested classes address the different (sub)components of a service:
 * identification, provider, contact, keywords, etc.
 * Some information is not customizable in the db tables, and is wither defined
 * in the templates (petascope.wcs2.templates) -- eg ows:serviceTypeVersion --
 * or plugged-in on-the-fly by the GetCapabilities handler -- eg OWS profiles.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class LegacyServiceMetadata {

    private LegacyServiceIdentification serviceIdentification;
    private LegacyServiceProvider       serviceProvider;
    // OperationsMetadata are set on-the-fly depending on the development status.

    // Getters
    // NOTE: service identification and provider are not mandatory in a capabilities document.
    public LegacyServiceIdentification getIdentification() {
        return serviceIdentification;
    }
    public LegacyServiceProvider getProvider() {
        return serviceProvider;
    }

    // Methods
    public void addServiceIdentification(String type, List<String> typeVersions) {
        serviceIdentification = new LegacyServiceIdentification(type, typeVersions);
    }
    public void addServiceIdentification(String type, String codespace, List<String> typeVersions) {
        serviceIdentification = new LegacyServiceIdentification(type, codespace, typeVersions);
    }
    public void addServiceProvider(String name) {
        serviceProvider = new LegacyServiceProvider(name);
    }
    public void addServiceProvider(String name, String site) {
        serviceProvider = new LegacyServiceProvider(name, site);
    }
}
