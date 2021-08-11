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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.Field;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.repository.service.CoveragePyramidRepositoryService;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.controller.AbstractController.getValueByKey;
import static petascope.controller.AbstractController.getValueByKeyAllowNull;
import static petascope.core.KVPSymbols.KEY_BASE;
import static petascope.core.KVPSymbols.KEY_MEMBER;
import static petascope.core.KVPSymbols.KEY_REQUEST;
import petascope.core.Pair;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import petascope.util.SetUtil;
import petascope.util.StringUtil;
import static petascope.core.KVPSymbols.KEY_PYRAMID_HARVESTING;
import static petascope.core.KVPSymbols.VALUE_ADD_PYRAMID_MEMBER;

/**
 * Class to handle admin request to add a pyramid member coverage (and all nested pyramid members of it
 * to a base coverage)
 * 
 * API: /rasdaman/admin?
        REQUEST=AddPyramidMember
        & BASE={base-coverage-id}
        & MEMBER={pyramid-element-coverage-id}
 
 * Example:
  /rasdaman/admin?
    REQUEST=AddPyramidMember
  & BASE=Sentinel2_10m
  & MEMBER=Sentinel2_10m
  * 
  * 
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class AddPyramidMemberService extends AbstractAdminService {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AddPyramidMemberService.class);
    
    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_REQUEST, KEY_BASE, KEY_MEMBER, KEY_PYRAMID_HARVESTING);
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private PyramidService pyramidService;
    @Autowired
    private CoveragePyramidRepositoryService coveragePyramidRepositoryService;
    
    public AddPyramidMemberService() {
        this.service = VALUE_ADD_PYRAMID_MEMBER;
        this.request = VALUE_ADD_PYRAMID_MEMBER;
    }
    
    private void validate(Map<String, String[]> kvpParameters) throws PetascopeException {
        this.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);
    }
    
    private String parseBaseCoverageId(Map<String, String[]> kvpParameters) throws PetascopeException {
        String baseCoverageId = getValueByKey(kvpParameters, KEY_BASE);
        if (!this.coverageRepositoryService.isInLocalCache(baseCoverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Base coverage '" + baseCoverageId + "' does not exist in local database.");
        }
        
        return baseCoverageId;
    }
    
    private String parseMemberCoverageId(Map<String, String[]> kvpParameters, GeneralGridCoverage baseCoverage) throws PetascopeException {
        // NOTE: this coverage id must not exist in local and remote caches
        String memberCoverageId = getValueByKey(kvpParameters, KEY_MEMBER);
        if (baseCoverage.hasPyramidMember(memberCoverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                        "Coverage '" + memberCoverageId + "' already exist in the pyramid of base coverage '" + baseCoverage.getCoverageId() + "'.");
        }
        
        
        return memberCoverageId;
    }
    
    /**
     * Check if pyramid member coverage has the same structure types for baseCoverage
     */
    private void validate(GeneralGridCoverage baseCoverage, GeneralGridCoverage pyramidMemberCoverage) throws PetascopeException {
        String baseCoverageId = baseCoverage.getCoverageId();
        String pyramidMemeberCoverageId = pyramidMemberCoverage.getCoverageId();
        
        int numberOfBaseAxes = baseCoverage.getNumberOfDimensions();
        int numberOfPyramidMemberAxes = pyramidMemberCoverage.getNumberOfDimensions();
        
        if (numberOfBaseAxes != numberOfPyramidMemberAxes) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Number of axes are different. Given '" + numberOfBaseAxes + "' in base coverage '" + baseCoverageId + "' "
                    + "and '" + numberOfPyramidMemberAxes + "' axes in pyramid member coverage '" + pyramidMemeberCoverageId  + "'.");
        }
        
        int numberOfBaseBands = baseCoverage.getNumberOfBands();
        int numberOfPyramidMemberBands = pyramidMemberCoverage.getNumberOfBands();
        
        if (numberOfBaseBands != numberOfPyramidMemberBands) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Number of bands are different. Given '" + numberOfBaseBands + "' in base coverage '" + baseCoverageId + "' "
                    + "and '" + numberOfPyramidMemberBands + "' bands in pyramid member coverage '" + pyramidMemeberCoverageId  + "'.");
        }
        
        for (GeoAxis baseGeoAxis : baseCoverage.getGeoAxes()) {
            String axisLabel = baseGeoAxis.getAxisLabel();
            
            GeoAxis pyramidGeoAxis = pyramidMemberCoverage.getGeoAxisByName(axisLabel);
            if (pyramidGeoAxis == null) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, 
                        "Pyramid member coverage '" + pyramidMemeberCoverageId + "' does not contain axis '" + axisLabel + "' "
                        + "as in base coverage '" + baseCoverageId + "'.");
            }
            
            String baseAxisCRS = baseGeoAxis.getSrsName();
            String pyramidAxisCRS = pyramidGeoAxis.getSrsName();
            
            if (!CrsUtil.CrsUri.toDbRepresentation(baseAxisCRS).equals(CrsUtil.CrsUri.toDbRepresentation(pyramidAxisCRS))) {
                throw new PetascopeException(ExceptionCode.InvalidRequest,
                        "CRS is different for axis '" + axisLabel + "'. Given '" + baseAxisCRS + "' in base coverage '" + baseCoverageId + "'"
                        + " and '" + pyramidAxisCRS + "' in pyramid member coverage '" + pyramidMemeberCoverageId + "'.");
            }
        }
        
        for (int i = 0; i < baseCoverage.getRangeType().getDataRecord().getFields().size(); i++) {
            Field fieldBase = baseCoverage.getRangeType().getDataRecord().getFields().get(i);
            Field fieldPyramidMember = pyramidMemberCoverage.getRangeType().getDataRecord().getFields().get(i);
            
            if (!fieldBase.getName().equals(fieldPyramidMember.getName())) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, 
                        "Pyramid member coverage '" + pyramidMemeberCoverageId + "' does not contain band '" + fieldBase.getName() + "' "
                        + "as in base coverage '" + baseCoverageId + "'.");
            }
        }
    }
    

    @Override
    public Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, IOException {
        this.validate(kvpParameters);
        
        // e.g. cov2
        String baseCoverageId = this.parseBaseCoverageId(kvpParameters);
        GeneralGridCoverage baseCoverage = ((GeneralGridCoverage)this.coverageRepositoryService.readCoverageByIdFromDatabase(baseCoverageId));
        
        // e.g: cov4
        String pyramidMemberCoverageId = this.parseMemberCoverageId(kvpParameters, baseCoverage);
        GeneralGridCoverage pyramidMemberCoverage = ((GeneralGridCoverage)this.coverageRepositoryService.readCoverageFullMetadataByIdFromCache(pyramidMemberCoverageId));
        
        
        String tmp = getValueByKeyAllowNull(kvpParameters, KEY_PYRAMID_HARVESTING);
        boolean pyramidHarvesting = false;
        if (tmp != null && tmp.equalsIgnoreCase(StringUtil.TRUE_VALUE)) {
            pyramidHarvesting = true;
        }
         
        this.validate(baseCoverage, pyramidMemberCoverage);
        // first, add cov4 and its nested pyramid member coverages (e.g. cov8 and cov16) to cov2
        this.addPyramidMemberCoverageToBaseCoverage(baseCoverage, pyramidMemberCoverage, pyramidHarvesting);
        
        // then, find all coverages (e.g. cov1) containing base coverage cov2
        // then add cov4 (and its nested pyramid member coverages) to cov1
        
        this.addPyramidMemeberCoverageToAllContainingCoverages(baseCoverage, pyramidMemberCoverage, pyramidHarvesting);
                
        // Persist the newly added pyramid member of base coverages to database
        this.coverageRepositoryService.save(baseCoverage);
        
        Response result = new Response();
        return result;
    }
    
    /**
     * Add the pyramidMemberCoverage (covA) to the base coverage (covB) as as suitable scale levels.
     * NOTE: 
     * - pyramidMemberCoverage can be federated coverage
     * - any pyramid members of covA (recursively for any pyramid member of pyramid members of cov A and so on...) will be added to covB
     */
    public void addPyramidMemberCoverageToBaseCoverage(GeneralGridCoverage baseCoverage, GeneralGridCoverage pyramidMemberCoverage, boolean pyramidHarvesting) throws PetascopeException {
        
        // 1. Get list recursively all the pyramid members of this pyramid member coverage
        // e.g. cov2 has cov4 and cov4 has cov8 as pyramid members, then return [cov2, cov4, cov8]       
        List<GeneralGridCoverage> nestedPyramidMemberCoverages = new ArrayList<>();
        if (pyramidHarvesting) {
            this.pyramidService.getListOfNestedPyramidMemberCoverages(pyramidMemberCoverage, nestedPyramidMemberCoverages);
        }
        
        nestedPyramidMemberCoverages.add(0, pyramidMemberCoverage);
        
        // 2. Get the sorted map by scalefactors for each coverage pyramid member and add them to base coverage if they don't exist
        TreeMap<BigDecimal, Pair<GeneralGridCoverage, List<String>>> sortedMap = this.pyramidService.getSortedPyramidMemberCoveragesByScaleFactors(baseCoverage, nestedPyramidMemberCoverages);
        for (Entry<BigDecimal, Pair<GeneralGridCoverage, List<String>>> entry : sortedMap.entrySet()) {
            Pair<GeneralGridCoverage, List<String>> pair = entry.getValue();
            Coverage coverageTmp = pair.fst;
            
            // e.g: 1,2,4
            List<String> scaleFactorsTmp = pair.snd;
            String coverageIdTmp = coverageTmp.getCoverageId();
            
            CoveragePyramid coveragePyramidTmp = new CoveragePyramid(coverageIdTmp, scaleFactorsTmp, false);
            if (!baseCoverage.getPyramid().contains(coveragePyramidTmp)) {
                baseCoverage.getPyramid().add(coveragePyramidTmp);
            }
        }
        
        // NOTE: sort all pyramid members by scale factors order (smaller -> larger)
        // before persisting to database with this order
        List<CoveragePyramid> sortedCoveragePyramids = this.pyramidService.sortByScaleFactors(baseCoverage.getPyramid());
        baseCoverage.setPyramid(sortedCoveragePyramids);
    }
    
    /**
     * Find all coverages containing a pyramid member coverage id,
     * then add this pyramid member coverage to all these containing coverages.
     */
    public void addPyramidMemeberCoverageToAllContainingCoverages(GeneralGridCoverage baseCoverage, GeneralGridCoverage pyramidMemberCoverage, boolean pyramidHarvesting) 
                                                                 throws PetascopeException {
        // NOTE: if base coverage covA is a pyramid member of other containing coverages, then add all nested pyramid members of covA to these coverages
        // e.g. cov1's pyramid has cov2
        //      cov4's pyramid has cov8
        //      now, request to add cov4 (member) to cov2's pyramid (base is cov2)
        //      then, cov2's pyramid has: cov4 and cov8
        //            cov1's phramid has: cov2, cov4 and cov8
        
        String baseCoverageId = baseCoverage.getCoverageId();
        
        List<GeneralGridCoverage> containingCoverages = this.coveragePyramidRepositoryService.getCoveragesContainingPyramidMemberCoverageId(baseCoverageId);
        for (GeneralGridCoverage containingCoverage : containingCoverages) {
            // e.g. add cov4 to cov1
            this.addPyramidMemberCoverageToBaseCoverage(containingCoverage, pyramidMemberCoverage, pyramidHarvesting);
            
            this.coverageRepositoryService.save(containingCoverage);
        }
    }
    
}
