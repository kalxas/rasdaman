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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package com.rasdaman.admin.coverage.service;

import com.rasdaman.admin.service.AbstractAdminService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.repository.service.CoveragePyramidRepositoryService;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMSRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_COVERAGE_ID;
import static petascope.core.KVPSymbols.KEY_METADATA;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.MIMEUtil;
import petascope.util.SetUtil;
import static petascope.core.KVPSymbols.KEY_NEW_COVERAGE_ID;
import petascope.util.XMLUtil;

/**
 * Service to handle update coverage (id and metadata).
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class AdminUpdateCoverageService extends AbstractAdminService {

    private static Logger log = LoggerFactory.getLogger(AdminUpdateCoverageService.class);

    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_COVERAGE_ID, KVPSymbols.KEY_NEW_COVERAGE_ID, KEY_METADATA);

    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private CoveragePyramidRepositoryService coveragePyramidRepositoryService;

    public AdminUpdateCoverageService() {

    }

    private void validate(Map<String, String[]> kvpParameters) throws PetascopeException {
        
        this.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);

        for (Map.Entry<String, String[]> entry : kvpParameters.entrySet()) {
            String key = entry.getKey();
            if (!VALID_PARAMETERS.contains(key.toLowerCase())) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Key parameter '" + key + "' is not valid.");
            }
        }

        String currentCoverageId = AbstractController.getValueByKey(kvpParameters, KEY_COVERAGE_ID);
        String newCoverageId = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_NEW_COVERAGE_ID);

        if (!this.coverageRepositoryService.isInLocalCache(currentCoverageId)) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Coverage '" + currentCoverageId + "' does not exist in local database.");
        }
        if (newCoverageId != null && this.coverageRepositoryService.isInLocalCache(newCoverageId)) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "New coverage id '" + newCoverageId + "' already exists in local database.");
        }
    }

    @Override
    public Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {

        this.validate(kvpParameters);

        String currentCoverageId = AbstractController.getValueByKey(kvpParameters, KEY_COVERAGE_ID);
        String newCoverageId = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_NEW_COVERAGE_ID);
        String metadata = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_METADATA);
        if (metadata != null) {
            metadata = XMLUtil.stripXMLDeclaration(metadata);
        }

        if (newCoverageId != null) {
            // admin wants to rename an existing local coverage

            // rename the associated layer if the layer exists
            if (this.wmsRepostioryService.isInLocalCache(currentCoverageId)) {
                this.wmsRepostioryService.updateLayerName(currentCoverageId, newCoverageId);
            }
            
            // - list all coverages containing this current coverage id and update their's pyramids with the new name
            List<GeneralGridCoverage> localBaseCoverages = this.coveragePyramidRepositoryService.getCoveragesContainingPyramidMemberCoverageId(currentCoverageId);
            for (Coverage baseCoverage : localBaseCoverages) {
                // e.g. Sentinel2_10m has a pyramid memeber Sentinel2_60m
                for (CoveragePyramid coveragePyramid : baseCoverage.getPyramid()) {
                    // now Sentinel2_60m changed to Sentinel2_60m_new
                    // then Sentinel2_10m has pyramid memeber Sentinel2_60m_new instead
                    if (coveragePyramid.getPyramidMemberCoverageId().equals(currentCoverageId)) {
                        coveragePyramid.setPyramidMemberCoverageId(newCoverageId);
                        break;
                    }
                }
                
                this.coverageRepositoryService.save(baseCoverage);
            }

        }
        
        // update new coverageId and metadata for the coverage if needed
        this.coverageRepositoryService.updateCoverage(currentCoverageId, newCoverageId, metadata);
        
        Response result = new Response(Arrays.asList("".getBytes()), MIMEUtil.MIME_TEXT);
        return result;
    }

}
