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
package petascope.wcst.handlers;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeMap;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.service.PyramidService;
import petascope.wcst.parsers.InsertScaleLevelRequest;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;

/**
 * Class to handle InsertScaleLevel request from WCST_Import to create a
 * corresponding downscaled collection based on the input scale level.
 *
 * e.g: http://localhost:8080/rasdaman/ows?serivce=WCS&version=2.0.1&request=InsertScaleLevel&level=2&coverageId=test_mr
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class InsertScaleLevelHandler {

    @Autowired
    private PyramidService pyramidService;
    
    @Autowired
    private CoverageRepostioryService coverageRepostioryService;
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(InsertScaleLevelHandler.class);

    /**
     * Handle the InsertScaleLevel request from client. 
     * NOTE: - If coverage  exists, it will create a downscaled collection ****with data****. 
     *       - If coverage does not exists, it will create a downscaled collection ****with no data****.
     */
    public Response handle(InsertScaleLevelRequest request) throws PetascopeException, SecoreException {
        log.debug("Handling coverage's scale level insertion...");
        
        String coverageId = request.getCoverageId();
        BigDecimal level = request.getLevel();
        
        // First, create an empty rasdaman downscaled collection
        this.pyramidService.insertScaleLevel(coverageId, level);
        
        try {
            // Then, populate the data for it (it does not matter coverage has full data or just 1 MDD point data)
            Coverage coverage = this.coverageRepostioryService.readCoverageByIdFromDatabase(coverageId);
            
            TreeMap<Integer, Pair<Boolean, String>> gridDomainsPairsMap = new TreeMap<>();
            
            List<GeoAxis> geoAxes = ((GeneralGridCoverage) coverage).getGeoAxes();
            for (GeoAxis geoAxis : geoAxes) {
                IndexAxis indexAxis = ((GeneralGridCoverage) coverage).getIndexAxisByName(geoAxis.getAxisLabel());
                String gridDomain = indexAxis.getLowerBound() + RASQL_BOUND_SEPARATION + indexAxis.getUpperBound();
                int gridAxisOrder = indexAxis.getAxisOrder();
                
                if (geoAxis.isXYAxis()) {
                    gridDomainsPairsMap.put(gridAxisOrder, new Pair(true, gridDomain));
                } else {
                    gridDomainsPairsMap.put(gridAxisOrder, new Pair(false, gridDomain));
                }
            }
            
            this.pyramidService.updateScaleLevel(coverageId, level, gridDomainsPairsMap);
        } catch (Exception ex) {
            log.error("Error updating scale level for coverage '" + coverageId + "' with level '" + level + "'. Reason: " + ex.getMessage(), ex);
            // If error occurred when updating data to downscaled collection, delete this collection.
            this.pyramidService.deleteScaleLevel(coverageId, level);
        }
        
        Response response = new Response();
        
        return response;
    }
}
