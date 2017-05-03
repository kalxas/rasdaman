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

package petascope.wms2.rasdaman;

import petascope.exceptions.PetascopeException;
import petascope.exceptions.rasdaman.RasdamanException;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;
import petascope.wms2.service.exception.error.WMSDataStoreException;
import petascope.wms2.service.exception.error.WMSInternalException;

/**
 * Class to represent a rasdaman service
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RasdamanService {

    /**
     * Constructor for the class
     *
     * @param config the rasdaman service configuration
     */
    public RasdamanService(RasdamanServiceConfig config) {
        this.config = config;
    }

    /**
     * Executes a rasdaman query an returns the result as a byte array
     *
     * @param query the query to be executed
     * @return the result of the query as a byte array
     * @throws RasdamanException
     * @throws WMSDataStoreException
     */
    public byte[] executeQuery(String query) throws WMSDataStoreException, WMSInternalException, PetascopeException {
        //TODO Once the new protocol is stable replace this with a pool of RasImplementation
        //The current code uses the RasUtil implementation which is suboptimal for a WMS service
        //as it reconnects to rasdaman each time a query is executed. Ideally we should establish a number of
        //connections and use them so that we eliminate any overhead of the connection
        try {
            RasQueryResult result = new RasQueryResult(RasUtil.executeRasqlQuery(query, config.getRasdamanUser(), config.getRasdamanPassword()));

            if (result.getMdds() == null || result.getMdds().size() == 0) {
                throw new WMSDataStoreException();
            }
            return result.getMdds().get(0);
        } catch (RasdamanException e) {
            throw new WMSInternalException(e);
        }
    }

    private final RasdamanServiceConfig config;

}
