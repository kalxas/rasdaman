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
package org.rasdaman.repository.interfaces;

import org.rasdaman.domain.owsmetadata.OwsServiceMetadata;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * OGC Web service provides the metadata about the ServiceIdentification,
 * ServiceProvider in GML from GetCapabilities request
 *
 * NOTE: The objects are defined for WCS standard, with WMS, the GML element
 * names are different, so build the GML representation accordingly the service type.
 *
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public interface OwsServiceMetadataRepository extends CrudRepository<OwsServiceMetadata, Long> {    

}
