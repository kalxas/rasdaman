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
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import petascope.util.StringUtil;

/**
 * Class creating the correct RasdamanUpdater object.
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class RasdamanUpdaterFactory {
    
    public static final int NO_EXPAND_DIMENSION = -1;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public RasdamanUpdaterFactory() {
    }

    public RasdamanUpdater getUpdater(String collectionName, String domain, String values, String shiftDomain, String username, String password) {
        return new RasdamanValuesUpdater(collectionName, domain, values, shiftDomain, username, password);
    }

    public RasdamanUpdater getUpdater(String collectionName, String domain, String filePath, String mimeType,
                                      String shiftDomain, String rangeParameters, String username, String password,
                                      boolean updateFilePath,
                                      Integer overviewIndex
                                    ) throws IOException {

        if (rangeParameters.isEmpty()) {
            rangeParameters = "{}";
        }
        
        // Add the filePaths to the rangeParameters json string
        if (updateFilePath) {
            rangeParameters = this.updateFilePathsInRangeParameters(rangeParameters, filePath, overviewIndex);
        }

        // else, not insitu
        if (mimeType != null && mimeType.toLowerCase().contains(IOUtil.GRIB_MIMETYPE)) {
            return new RasdamanGribUpdater(collectionName, domain, rangeParameters, shiftDomain, username, password);
        } else if (mimeType != null && mimeType.toLowerCase().contains(IOUtil.NETCDF_MIMETYPE)) {
            return new RasdamanNetcdfUpdater(collectionName, domain, shiftDomain, rangeParameters, username, password);
        } else {
            return new RasdamanGdalDecodeUpdater(collectionName, domain, shiftDomain, rangeParameters, username, password);
        }
    }

    private String getInsituMime(String mimeType) {
        if (mimeType.contains(IOUtil.GRIB_MIMETYPE)) {
            return IOUtil.GRIB_MIMETYPE;
        } else if (mimeType.contains(IOUtil.NETCDF_MIMETYPE)) {
            return IOUtil.NETCDF_MIMETYPE;
        } else {
            return IOUtil.GDAL_MIMETYPE;
        }
    }

    // -- rasdaman enterprise ends

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
    private String updateFilePathsInRangeParameters(String rangeParameters, String filePath, Integer overviewIndex) throws IOException {        
        ObjectNode rootNode = (ObjectNode) this.objectMapper.readTree(rangeParameters);
        ArrayNode filePathsNode = rootNode.putArray("filePaths");
        filePathsNode.add(filePath);
        
        rootNode = this.createOpenOptionsNode(rootNode, rangeParameters, overviewIndex);
        
        // e.g: "...{\"filePaths\":[\"PATH/test.png\"]}..."
        return StringUtil.escapeQuotesJSON(rootNode.toString());
    }
    
    /**
     * If overviewIndex is not null, then add OpenOptions element
     */
    private ObjectNode createOpenOptionsNode(ObjectNode rootNode, String rangeParameters, Integer overviewIndex) throws IOException {
        ObjectNode rootNodeResult = rootNode;
        
        if (rootNode == null) {
            rootNodeResult = (ObjectNode) this.objectMapper.readTree(rangeParameters);
        }
        
        if (overviewIndex != null) {
            // Use overview from the input file to update to rasdaman downscaled collection
            
            // \"openOptions\": { \"OVERVIEW_LEVEL\": 3}
            ObjectNode openOptionsNode = rootNodeResult.putObject("openOptions");
            openOptionsNode.put("OVERVIEW_LEVEL", overviewIndex);            
        }
        
        return rootNodeResult;        
    }
}
