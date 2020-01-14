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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import petascope.util.IOUtil;

import java.io.IOException;
import org.springframework.stereotype.Service;

/**
 * Class creating the correct RasdamanUpdater object.
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class RasdamanUpdaterFactory {
    
    public static final int NO_EXPAND_DIMENSION = -1;

    public RasdamanUpdaterFactory() {
    }

    public RasdamanUpdater getUpdater(String collectionName, String domain, String values, String shiftDomain) {
        return new RasdamanValuesUpdater(collectionName, domain, values, shiftDomain);
    }

    public RasdamanUpdater getUpdater(String collectionName, String domain, String filePath, String mimeType,
                                      String shiftDomain, String rangeParameters, boolean updateFilePath) throws IOException {
        
        // Add the filePaths to the rangeParameters json string
        if (updateFilePath) {
            rangeParameters = this.updateFilePathsInRangeParameters(rangeParameters, filePath);
        }
        
        if (mimeType != null && mimeType.toLowerCase().contains(IOUtil.GRIB_MIMETYPE)) {            
            return new RasdamanGribUpdater(collectionName, domain, rangeParameters, shiftDomain);
        } else if (mimeType != null && mimeType.toLowerCase().contains(IOUtil.NETCDF_MIMETYPE)) {
            return new RasdamanNetcdfUpdater(collectionName, domain, shiftDomain, rangeParameters);
        } else {
            return new RasdamanGdalDecodeUpdater(collectionName, domain, shiftDomain, rangeParameters);
        }
    }
    
    
    /**
     * To improves ingestion performance if the data is on the same machine as the rasdaman server, as the network transport is bypassed 
     * we add the filePaths parameter into RangeElement strings
     * 
     * before, we use: --file PATH_TO_FILE:
     * rasql -q 'UPDATE A SET A[0:0,0:4318,0:8640] 
     * ASSIGN shift(decode($1, "NetCDF", "{\"variables\": [\"MERIS_nobs_sum\", \"chlor_a\"]}"), [0,0,0]) 
     * WHERE oid(test_ansidate_different_crs_origin_5) = 1025' --file 'PATH/test.nc'
     * 
     * after, we add it in rangeParameter string:
     * rasql -q 'UPDATE A SET A[0:0,0:4318,0:8640] 
     * ASSIGN shift(decode($1, "NetCDF", "{\"variables\": [\"MERIS_nobs_sum\", \"chlor_a\", 
     * \"filePaths\": [\"PATH/test.nc\"] }"), [0,0,0]) 
     * WHERE oid(test_ansidate_different_crs_origin_5) = 1025'
     * 
     * @return 
     */
    private String updateFilePathsInRangeParameters(String rangeParameters, String filePath) throws IOException {
        if (rangeParameters.isEmpty()) {
            rangeParameters = "{}";
        }
        ObjectNode root = (ObjectNode) new ObjectMapper().readTree(rangeParameters);
        ArrayNode filePathsNode = root.putArray("filePaths");
        filePathsNode.add(filePath);
        // e.g: "...{\"filePaths\":[\"PATH/test.png\"]}..."
        return root.toString().replace("\"", "\\\"");       
    }
}
