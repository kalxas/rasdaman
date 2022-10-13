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
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import static org.rasdaman.domain.cis.IrregularAxis.DEFAULT_RESOLUTION;
import org.rasdaman.domain.cis.RasdamanDownscaledCollection;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.controller.AbstractController.getValueByKey;
import static petascope.controller.AbstractController.getValueByKeyAllowNull;
import static petascope.core.KVPSymbols.KEY_COVERAGE_ID;
import static petascope.core.KVPSymbols.KEY_INTERPOLATION;
import static petascope.core.KVPSymbols.KEY_MEMBER;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.BigDecimalUtil;
import petascope.util.JSONUtil;
import petascope.util.ListUtil;
import petascope.util.SetUtil;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.wcs2.handlers.kvp.service.KVPWCSGetCoverageScalingService.NEAREST_INTERPOLATION;
import static petascope.wcs2.handlers.kvp.service.KVPWCSGetCoverageScalingService.SUPPORTED_SCALING_AXIS_INTERPOLATIONS;
import static petascope.core.KVPSymbols.KEY_SCALE_VECTOR;
import petascope.wmts.handlers.service.WMTSGetCapabilitiesService;

/**
 * Class to handle admin request to create a pyramid member coverage by scalefactors
 * (before it was InsertScaleLevel request).
 * 
 * API: /rasdaman/admin?
        REQUEST=CreatePyramidMember
        & BASE={base-coverage-id}
        & MEMBER={pyramid-element-coverage-id}
        & SCALEFACTOR={scale-vector} - comma-separated list of positive float values for each axis
        & INTERPOLATION={interpolation-vector} - vector of interpolation methods used, one per axis (comma-separated list of interpolation identifiers
 
 * Example:
  /rasdaman/admin?
    REQUEST=CreatePyramidMember
  & BASE=S2
  & MEMBER=S2_4
  & SCALEFACTOR=4,4,1
  & INTERPOLATION=linear,linear,nearest
         
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class AdminCreatePyramidMemberService extends AbstractAdminService {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AdminCreatePyramidMemberService.class);
    
    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_COVERAGE_ID, KEY_MEMBER, 
                                                                                KEY_SCALE_VECTOR, KEY_INTERPOLATION);
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private PyramidService pyramidService;
    @Autowired
    private AdminAddPyramidMemberService addPyramidMemberService;
    
    private void validate(Map<String, String[]> kvpParameters) throws PetascopeException {
        this.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);
    }
    
    private String parseBaseCoverageId(Map<String, String[]> kvpParameters) throws PetascopeException {
        String baseCoverageId = getValueByKey(kvpParameters, KEY_COVERAGE_ID);
        if (!this.coverageRepositoryService.isInLocalCache(baseCoverageId)) {
            throw new PetascopeException(ExceptionCode.NoSuchCoverage, "Base coverage '" + baseCoverageId + "' does not exist in local database.");
        }
        
        return baseCoverageId;
    }
    
    private String parseMemberCoverageId(Map<String, String[]> kvpParameters, GeneralGridCoverage baseCoverage) throws PetascopeException {
        // NOTE: this coverage id must not exist in local and remote caches
        String memberCoverageId = getValueByKey(kvpParameters, KEY_MEMBER);
        if (this.coverageRepositoryService.isInCache(memberCoverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Coverage '" + memberCoverageId + "' already exists.");
        } else if (baseCoverage.hasPyramidMember(memberCoverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                        "Coverage '" + memberCoverageId + "' already exist in the pyramid of base coverage '" + baseCoverage.getCoverageId() + "'.");
        }
        
        
        return memberCoverageId;
    }
    
    private List<BigDecimal> parseScaleFactor(Map<String, String[]> kvpParameters, GeneralGridCoverage baseCoverage) throws PetascopeException, SecoreException {
        String scaleFactor = getValueByKey(kvpParameters, KEY_SCALE_VECTOR);
        
        List<BigDecimal> results = new ArrayList<>();
        
        String[] tmpValues = scaleFactor.split(",");
        int numberOfScaleFactors = tmpValues.length;
        
        int numberOfAxes = baseCoverage.getNumberOfDimensions();
        if (numberOfAxes != tmpValues.length) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                         "Number of scale factors must match with coverage's number of axes. Given '" + numberOfScaleFactors + "' and '" + numberOfAxes + "' respectively.");
        }
        
        List<GeoAxis> geoAxes = baseCoverage.getGeoAxes();
        
        for (int i = 0; i < tmpValues.length; i++) {
            String value = tmpValues[i].trim();
            if (!BigDecimalUtil.isNumber(value)) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Scale factor value must be a number. Given '" + value + "'.");
            }
            
            BigDecimal number = new BigDecimal(value);
            if (number.compareTo(BigDecimal.ONE) < 0) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Scale factor value must be a number and >= 1. Given '" + value + "'.");
            }
            
            GeoAxis geoAxis = geoAxes.get(i);
            // NOTE: only allow downscale on XY axes (e.g: regular Lat Long)
            if ((!geoAxis.isXYAxis() && number.compareTo(BigDecimal.ONE) > 0)) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, 
                        "Scale factor value (> 1) can only be applied on geo-spatial axes with types X or Y. "
                                + "Given factor '" + number + "' for axis '" + geoAxis.getAxisLabel() + "' with type '" + geoAxis.getAxisType() + "'.");
            }                  
            
            results.add(number);
        }
        
        return results;
    }
    
    private List<String> parseInterpolation(Map<String, String[]> kvpParameters, GeneralGridCoverage baseCoverage) throws PetascopeException {
        String interpolation = getValueByKeyAllowNull(kvpParameters, KEY_INTERPOLATION);
        
        int numberOfAxes = baseCoverage.getNumberOfDimensions();
        
        List<String> results = new ArrayList<>();
        if (interpolation != null) {
            String[] tmpValues = interpolation.split(",");
            int numberOfInterpolations = tmpValues.length;
            
            // interpolation=nearest is ok, all axes are applied the same value
            if (tmpValues.length > 1 && tmpValues.length != numberOfAxes) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                         "Number of interpolations must match with coverage's number of axes. Given '" + numberOfInterpolations + "' and '" + numberOfAxes + "' respectively.");
            }
            
            for (String value : tmpValues) {
                value = value.trim();
                if (!SUPPORTED_SCALING_AXIS_INTERPOLATIONS.contains(value)) {
                    throw new PetascopeException(ExceptionCode.NoApplicableCode, "Interpolation method is not supported. Given '" + value + "'.");
                }
            }
        } else {
            results.add(NEAREST_INTERPOLATION);
        }
        
        return results;
    }
    

    @Override
    public Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, IOException {
        this.validate(kvpParameters);
        
        String baseCoverageId = this.parseBaseCoverageId(kvpParameters);
        GeneralGridCoverage baseCoverage = ((GeneralGridCoverage)this.coverageRepositoryService.readCoverageByIdFromDatabase(baseCoverageId));
        
        String pyramidMemberCoverageId = this.parseMemberCoverageId(kvpParameters, baseCoverage);
        
        // e.g: 1,1.5,2
        List<BigDecimal> scaleFactors = this.parseScaleFactor(kvpParameters, baseCoverage);
        
        // e.g: nearest,cubic,linear (default is nearest) - optimal
        List<String> interpolations = this.parseInterpolation(kvpParameters, baseCoverage);
        
        this.createDownscaledCoverage(baseCoverage, pyramidMemberCoverageId, scaleFactors, interpolations);
        
        Response result = new Response();
        return result;
    }
    
    /**
     * Create a downscaled level coverage based on input scale factors
     */
    private void createDownscaledCoverage(GeneralGridCoverage baseCoverage, 
                                          String pyramidMemberCoverageId,
                                          List<BigDecimal> scaleFactors, List<String> interpolations) throws PetascopeException, SecoreException, IOException {
        
        GeneralGridCoverage downscaledLevelCoverage = (GeneralGridCoverage) JSONUtil.clone(baseCoverage);
        downscaledLevelCoverage.setPyramid(new ArrayList<>());
        downscaledLevelCoverage.setCoverageId(pyramidMemberCoverageId);
        downscaledLevelCoverage.getRasdamanRangeSet().setRasdamanDownscaledCollections(new ArrayList<RasdamanDownscaledCollection>());
        
        CoveragePyramid coveragePyramid = new CoveragePyramid(pyramidMemberCoverageId, BigDecimalUtil.toStringList(scaleFactors), true);
        
        // create a rasdaman downscaled collection for this pyramid member coverage
        this.createDownscaledLevelCollection(baseCoverage, downscaledLevelCoverage, coveragePyramid, scaleFactors);
        
        // and persit both coverages to databases
        this.coverageRepositoryService.save(downscaledLevelCoverage);
        
        // then, add this pyramid member coverage to the base coverage's pyramid set
        baseCoverage.getPyramid().add(coveragePyramid);
        this.coverageRepositoryService.save(baseCoverage);  
        
        // Recreate WMTS TileMatrixSet for this base layer
        WMTSGetCapabilitiesService.localUpdatedLayerNames.add(baseCoverage.getCoverageId());
        
        // for example: request to create with base coverage: cov4 and pyramid member: cov8
        // first, find all coverages (e.g. cov1 and cov2) containing base coverage cov4
        // then, add newly created cov8 to these cov1 and cov2 coverages
        this.addPyramidMemberService.addPyramidMemeberCoverageToAllContainingCoverages(baseCoverage, downscaledLevelCoverage, false);        
        
        log.debug("Created pyramid member coverage '"  + pyramidMemberCoverageId + "' of base coverage '" + baseCoverage.getCoverageId() + "'.");
    }
    
    /**
     * Create a rasdaman downscaled level collection based on input scale factors
     */
    private void createDownscaledLevelCollection(GeneralGridCoverage baseCoverage, 
                                                 GeneralGridCoverage downscaledLevelCoverage,
                                                 CoveragePyramid coveragePyramid, List<BigDecimal> scaleFactors)
                                                throws PetascopeException, SecoreException {
        
        String pyramidMemberCoverageId = downscaledLevelCoverage.getCoverageId();
        String username = ConfigManager.RASDAMAN_ADMIN_USER;
        String password = ConfigManager.RASDAMAN_ADMIN_PASS;
        
        // First, initialize an empty rasdaman downscaled collection with all requirement data types and tilings
        this.pyramidService.initDownscaledLevelCollection(downscaledLevelCoverage, scaleFactors, username, password);
        String downscaledLevelCollectionName = downscaledLevelCoverage.getRasdamanRangeSet().getCollectionName();

        boolean runUpdateQuery = false;
        for (IndexAxis indexAxis : baseCoverage.getIndexAxes()) {
            if (indexAxis.getLowerBound() < indexAxis.getUpperBound()) {
                runUpdateQuery = true;
                break;
            }
        }
        
        if (!runUpdateQuery) {
            // In this case, the base coverage has no pixel -> no need to run an UPDATE query to pyramid member collection 
            return;
        }

        // scale factors are geo CRS order (e.g: time,lat,long) -> grid oder (time,long,lat)
        List<BigDecimal> targetScaleFactorsByGridOrder = this.pyramidService.sortScaleFactorsByGridOder(baseCoverage, scaleFactors);
        
        List<String> gridDomains = new ArrayList<>();

        for (int i = 0; i < baseCoverage.getIndexAxes().size(); i++) {
            BigDecimal scaleFactor = targetScaleFactorsByGridOrder.get(i);
            
            IndexAxis baseIndexAxis = baseCoverage.getIndexAxes().get(i);
            IndexAxis pyramidMemberIndexAxis = downscaledLevelCoverage.getIndexAxisByOrder(i);
            String axisLabel = baseIndexAxis.getAxisLabel();
            
            GeoAxis baseGeoAxis = baseCoverage.getGeoAxisByName(axisLabel);
            GeoAxis pyramidMemberGeoAxis = downscaledLevelCoverage.getGeoAxisByName(axisLabel);
            
            String gridDomain = baseIndexAxis.getLowerBound() + RASQL_BOUND_SEPARATION + baseIndexAxis.getUpperBound();
            gridDomains.add(gridDomain);
            
            if (!baseGeoAxis.isIrregular()) {
                // e.g: 0:49 -> 49
                long gridDistance = baseIndexAxis.getUpperBound() - baseIndexAxis.getLowerBound() + 1;

                // e.g: 20 / 2.5
                long numberOfPyramidMemberGridPixels = BigDecimalUtil.divideToLong(new BigDecimal(gridDistance), scaleFactor);
                long pyramidMemeberIndexAxisLowerBound =  BigDecimalUtil.divideToLong(new BigDecimal(baseIndexAxis.getLowerBound()), scaleFactor);
                
                // base 0:99 with scale factor = 2 will return 0:48
                long pyramidMemeberIndexAxisUpperBound = pyramidMemeberIndexAxisLowerBound + numberOfPyramidMemberGridPixels - 1;
                if (pyramidMemeberIndexAxisUpperBound < pyramidMemeberIndexAxisLowerBound) {
                    pyramidMemeberIndexAxisUpperBound = pyramidMemeberIndexAxisLowerBound;
                }
                pyramidMemberIndexAxis.setLowerBound(pyramidMemeberIndexAxisLowerBound);
                pyramidMemberIndexAxis.setUpperBound(pyramidMemeberIndexAxisUpperBound);
                
                if (numberOfPyramidMemberGridPixels > 0) {
                    // NOTE: a downscaled level coverage must have the same bounds for geo axes as in base coverage, 
                    // only different from grid bounds and axis resolution
                    BigDecimal pyramidMemberAxisResolution = BigDecimalUtil.divide(
                                                                pyramidMemberGeoAxis.getUpperBoundNumber().subtract(pyramidMemberGeoAxis.getLowerBoundNumber()),
                                                                new BigDecimal(numberOfPyramidMemberGridPixels));
                    if (baseGeoAxis.getResolution().compareTo(BigDecimal.ZERO) < 0) {
                        pyramidMemberAxisResolution = pyramidMemberAxisResolution.multiply(new BigDecimal("-1"));
                    }

                    pyramidMemberGeoAxis.setResolution(pyramidMemberAxisResolution);
                    downscaledLevelCoverage.getEnvelope().getEnvelopeByAxis().getAxisExtentByLabel(axisLabel).setResolution(pyramidMemberAxisResolution);
                }
            } else {
                // NOTE: for irregular axis, make it as same as base index axis
                pyramidMemberIndexAxis.setLowerBound(baseIndexAxis.getLowerBound());
                pyramidMemberIndexAxis.setUpperBound(baseIndexAxis.getUpperBound());
                pyramidMemberGeoAxis.setResolution(DEFAULT_RESOLUTION);
            }
        }
        
        try {            
            this.pyramidService.updateDownscaledLevelCollection(baseCoverage, downscaledLevelCoverage, 
                                                                downscaledLevelCollectionName, coveragePyramid, gridDomains, username, password);
        } catch (Exception ex) {
            String errorMessage = "Error updating downscaled level collection for coverage '" + pyramidMemberCoverageId 
                                + "' with scale factors '" + ListUtil.join(scaleFactors, ",") + "'. Reason: " + ex.getMessage();
            // If error occurred when updating data to downscaled collection, delete this collection.
            this.pyramidService.deleteDownscaledLevelCollection(downscaledLevelCollectionName, username, password);
            downscaledLevelCoverage.getRasdamanRangeSet().setCollectionName(null);
            
            throw new PetascopeException(ExceptionCode.InternalComponentError, errorMessage, ex);
        }        
    }
}
