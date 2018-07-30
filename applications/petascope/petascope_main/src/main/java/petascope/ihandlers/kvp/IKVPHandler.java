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
package petascope.ihandlers.kvp;

import petascope.core.response.Response;
import java.util.Map;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WMSException;

/**
 * Interface for all KVP request handlers
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public interface IKVPHandler {

    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException;
    /**
     * Validate the KVP parameters passing to service class handlers
     * @param kvpParameters
     * @throws PetascopeException
     * @throws SecoreException
     * @throws WMSException 
     */
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException;
}
