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
package org.rasdaman.migration.service.owsmetadata;

import java.util.ArrayList;
import org.rasdaman.migration.domain.legacy.LegacyServiceIdentification;
import org.rasdaman.domain.owsmetadata.ServiceIdentification;
import org.springframework.stereotype.Service;

/**
 * Class which translates the legacy OWS ServiceIdentification to new one and
 * persist to database
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class ServiceIdentificationTranslatingService {

    public ServiceIdentificationTranslatingService() {

    }

    /**
     * Create a new ServiceIdenfitication from the legacy
     *
     * @param legacy
     * @return
     */
    public ServiceIdentification create(LegacyServiceIdentification legacy) {
        ServiceIdentification serviceIdentification = new ServiceIdentification();

        serviceIdentification.setServiceType(legacy.getType());
        serviceIdentification.setServiceTypeVersions(legacy.getTypeVersions());
        serviceIdentification.setServiceTitle(legacy.getDescription().getTitles().get(0));
        serviceIdentification.setServiceAbstract(legacy.getDescription().getAbstracts().get(0));

        // As this information does not exist in GetCapabilities in legacy
        serviceIdentification.setKeywords(new ArrayList<String>());
        serviceIdentification.setFees(legacy.getFees());
        serviceIdentification.setAccessConstraints(legacy.getAccessConstraints());

        return serviceIdentification;
    }
}
