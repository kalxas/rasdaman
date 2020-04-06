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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcst.helpers.update;

import org.rasdaman.config.ConfigManager;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.ras.RasUtil;

/**
 * Class for updating when values are received as tuple list.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class RasdamanValuesUpdater extends RasdamanUpdater {

    String affectedCollectionName;
    String affectedDomain;
    String values;
    String shiftDomain;
    
    public RasdamanValuesUpdater() {
        
    }

    /**
     * Class constructor.
     * @param affectedCollectionName the name of the rasdaman collection corresponding to the coverage.
     * @param affectedDomain the rasdaman domain over which the update is executed.
     * @param values the values clause in the rasdaman update operation.
     * @param shiftDomain the domain with which the rasdaman array in the values clause must be shifted.
     */
    public RasdamanValuesUpdater(String affectedCollectionName, String affectedDomain, String values, String shiftDomain, String username, String password) {
        this.affectedCollectionName = affectedCollectionName;
        this.affectedDomain = affectedDomain;
        this.values = values;
        this.shiftDomain = shiftDomain;
        
        this.username = username;
        this.password = password;
    }

    @Override
    public void updateWithFile() throws RasdamanException, PetascopeException {
        String queryString = UPDATE_TEMPLATE_VALUES.replace("$collection", affectedCollectionName)
                             .replace("$domain", affectedDomain)
                             .replace("$values", values)
                             .replace("$shiftDomain", shiftDomain);
        RasUtil.executeRasqlQuery(queryString, this.username, this.password, true);
    }

    private static final String UPDATE_TEMPLATE_VALUES = "UPDATE $collection SET $collection$domain ASSIGN shift($values, $shiftDomain)";

    @Override
    public void updateWithBytes(byte[] bytes) throws PetascopeException {
    }
}
