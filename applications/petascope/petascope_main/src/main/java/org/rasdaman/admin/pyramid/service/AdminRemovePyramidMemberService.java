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
package org.rasdaman.admin.pyramid.service;

import com.rasdaman.admin.service.AbstractAdminService;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.repository.service.CoveragePyramidRepositoryService;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.rasdaman.repository.service.WMTSRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.controller.AbstractController.getValueByKey;
import static petascope.core.KVPSymbols.KEY_COVERAGE_ID;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.SetUtil;
import static petascope.core.KVPSymbols.KEY_MEMBERS;
import petascope.util.CrsUtil;
import petascope.wmts.handlers.service.WMTSGetCapabilitiesService;

/**
 * Class to handle admin request to remove a pyramid member coverage
 * 
 * API: /rasdaman/admin?
        REQUEST=RemovePyramidMember
        & BASE={base-coverage-id}
        & MEMBERS={pyramid-element-coverage-id}
 
 * Example:
  /rasdaman/admin?
    REQUEST=RemovePyramidMember
  & BASE=Sentinel2_10m
  & MEMBERS=Sentinel2_10m
         
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class AdminRemovePyramidMemberService extends AbstractAdminService {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AdminRemovePyramidMemberService.class);
    
    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_COVERAGE_ID, KEY_MEMBERS);
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private CoveragePyramidRepositoryService coveragePyramidRepositoryService;
    @Autowired
    private WMTSRepositoryService wmtsRepositoryService;
    
    private void validate(Map<String, String[]> kvpParameters) throws PetascopeException {       
        this.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);
    }
    
    private String parseBaseCoverageId(Map<String, String[]> kvpParameters) throws PetascopeException {
        String baseCoverageId = getValueByKey(kvpParameters, KEY_COVERAGE_ID);
        if (!this.coverageRepositoryService.isInLocalCache(baseCoverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Base coverage '" + baseCoverageId + "' does not exist in local database.");
        }
        
        return baseCoverageId;
    }
    
    private String[] parsePyramidMemberCoverageIds(Map<String, String[]> kvpParameters, GeneralGridCoverage baseCoverage) throws PetascopeException {
        String[] memberIds = getValueByKey(kvpParameters, KEY_MEMBERS).split(",");
        
        for (String pyramidMemberCoverageId : memberIds) {
            if (!baseCoverage.hasPyramidMember(pyramidMemberCoverageId)) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                            "Base coverage '" + baseCoverage.getCoverageId() + "' does not contain pyramid member coverage '" + pyramidMemberCoverageId + "'.");
            }
        }
        
        return memberIds;
    }
    

    @Override
    public Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, IOException {
        this.validate(kvpParameters);
        
        // e.g: cov4
        String baseCoverageId = this.parseBaseCoverageId(kvpParameters);
        GeneralGridCoverage baseCoverage = ((GeneralGridCoverage)this.coverageRepositoryService.readCoverageByIdFromDatabase(baseCoverageId));
        
        // e.g: cov8,cov16
        String[] pyramidMemberCoverageIds = this.parsePyramidMemberCoverageIds(kvpParameters, baseCoverage);        
        
        for (String pyramidMemberCoverageId : pyramidMemberCoverageIds) {
            // remove cov8 from cov4's pyramid
            this.removePyramidMemberCoverage(baseCoverage, pyramidMemberCoverageId);

            // get all coverage contains cov4 (e.g. cov1 and cov2), and remove cov8 from their containing coverages' pyramids
            List<GeneralGridCoverage> containingCoverages = this.coveragePyramidRepositoryService.getCoveragesContainingPyramidMemberCoverageId(baseCoverageId);
            for (GeneralGridCoverage containingCoverage : containingCoverages) {
                this.removePyramidMemberCoverage(containingCoverage, pyramidMemberCoverageId);
            }
        }
        
        Response result = new Response();
        return result;
    }
    
    /**
     * Remove the pyramid member coverage from the base coverage
     * and all other coverages containing the base coverage
     * 
     * For example, cov2 contains cov4, then remove cov4 from cov2's pyramid
     */
    public void removePyramidMemberCoverage(GeneralGridCoverage baseCoverage, String pyramidMemberCoverageId) throws PetascopeException {
        int index = -1;
        for (int i = 0; i < baseCoverage.getPyramid().size(); i++) {
            CoveragePyramid coveragePyramid = baseCoverage.getPyramid().get(i);
            if (coveragePyramid.getPyramidMemberCoverageId().equals(pyramidMemberCoverageId)) {
                index = i;
                break;
            }
        }
        
        if (index >= 0) {
            baseCoverage.getPyramid().remove(index);
            this.coverageRepositoryService.save(baseCoverage);
            
            // e.g. EPSG:4326
            String epsgCode = CrsUtil.getAuthorityCode(baseCoverage.getEnvelope().getEnvelopeByAxis().getGeoXYCrs());
            
            // remove TileMatrix (WMTS) exists in an existing TileMatrixSet
            this.wmtsRepositoryService.removeTileMatrixFromLocalCache(baseCoverage.getCoverageId(), pyramidMemberCoverageId, epsgCode);
            
            log.info("Removed pyramid member coverage '"  + pyramidMemberCoverageId + "' from base coverage '" + baseCoverage.getCoverageId() + "'.");
        }
        
    }
    
}
