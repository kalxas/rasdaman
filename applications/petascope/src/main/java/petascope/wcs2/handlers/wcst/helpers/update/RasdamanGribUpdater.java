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
package petascope.wcs2.handlers.wcst.helpers.update;

import petascope.ConfigManager;
import petascope.exceptions.rasdaman.RasdamanException;
import petascope.util.ras.RasUtil;

import java.io.File;
import java.io.IOException;

/**
 * Class for updating when values are received as file to be decoded using decode() rasdaman function.
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RasdamanGribUpdater implements RasdamanUpdater {

    String affectedCollectionName;
    String affectedCollectionOid;
    String affectedDomain;
    String rangeParameters;
    String shiftDomain;

    /**
     * Class constructor.
     * @param affectedCollectionName the name of the rasdaman collection corresponding to the coverage.
     * @param affectedCollectionOid the oid of the rasdaman array corresponding to the coverage.
     * @param affectedDomain the rasdaman domain over which the update is executed.
     * @param rangeParameters the parameters from which the grib messages can be computed.
     * @param shiftDomain the domain with which the array stored in the file must be shifted.
     */
    public RasdamanGribUpdater(String affectedCollectionName, String affectedCollectionOid, String affectedDomain,
                               String rangeParameters, String shiftDomain) {
        this.affectedCollectionName = affectedCollectionName;
        this.affectedCollectionOid = affectedCollectionOid;
        this.affectedDomain = affectedDomain;
        this.rangeParameters = rangeParameters;
        this.shiftDomain = shiftDomain;
    }

    @Override
    public void update() throws RasdamanException, IOException {
        String queryString = UPDATE_TEMPLATE_FILE.replace("$collection", affectedCollectionName)
                             .replace("$domain", affectedDomain)
                             .replace("$oid", affectedCollectionOid)
                             .replace("$shiftDomain", shiftDomain)
                             .replace("$gribMessages", rangeParameters);
        RasUtil.executeUpdateFileStatement(queryString);
    }

    // sample query
    // /home/rasdaman/install/bin/rasql --user rasadmin --passwd rasadmin 
    // -q 'UPDATE test_grib_irregular_1_message SET test_grib_irregular_1_message[0:0,2:2,0:287,0:144] 
    // ASSIGN shift(decode(<[0:0] 1c>, "GRIB", "{\"internalStructure\": 
    //        {\"messageDomains\":[{\"msgId\":1,\"domain\":\"[0:0,0:0,0:287,0:144]\"}]},
    //        \"filePaths\":[\"/home/rasdaman/output_20030107_20030107.grib2\"]}"), [0,2,0,0]) WHERE oid(test_grib_irregular_1_message) = 6657'
    private static final String UPDATE_TEMPLATE_FILE = "UPDATE $collection SET $collection$domain "
                                                     + "ASSIGN shift(decode(<[0:0] 1c>, "
                                                     + "\"GRIB\"" + ", \"$gribMessages\"), $shiftDomain) WHERE oid($collection) = $oid";            
            
}
