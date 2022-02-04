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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.oapi.handlers.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.oapi.handlers.model.Collection;
import petascope.oapi.handlers.model.Extent;
import petascope.oapi.handlers.model.Link;
import petascope.oapi.handlers.model.Spatial;
import petascope.oapi.handlers.model.Temporal;
import petascope.util.CrsUtil;
import petascope.util.StringUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Service class to build OAPI collection object
 * 
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class OapiCollectionService {
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(OapiCollectionService.class);
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    
    /**
     * Build Collection object from a Coverage object
     */
    public Collection buildCollectionFromCoverage(WcpsCoverageMetadata coverage, String urlPrefix) throws Exception {
        String id = coverage.getCoverageName();
        String title = coverage.getCoverageName();
        String description = StringUtils.EMPTY;
        Extent extent = this.buildExtent(coverage);
        List<String> crss = this.getCrsList(coverage);
        
        if (extent.getSpatial() != null) {
            crss.add(0, Spatial.DEFAULT_SPATIAL_CRS);
        }
        
        // https://github.com/opengeospatial/ogc_api_coverages/blob/master/standard/openapi/schemas/common/collectionInfo.yaml#L32
        if (extent.getTemporal() != null) {
            if (crss.get(0).equals(Spatial.DEFAULT_SPATIAL_CRS)) {
                crss.add(1, Temporal.DEFAULT_TEMPORAL_CRS);
            } else {
                crss.add(0, Temporal.DEFAULT_TEMPORAL_CRS);
            }            
        }
        
        Link domainLink = Link.getDomainLink(urlPrefix, coverage.getCoverageName());
        Link rangeTypeLink = Link.getRangeTypeLink(urlPrefix, coverage.getCoverageName());
        List<Link> links = Link.getCollectionInformationLinks(urlPrefix, coverage.getCoverageName());
        
        return new Collection(id, title, description, extent, crss,                              
                              domainLink, rangeTypeLink, links);
    }

    /**
     * Build Extent object for GetCollectionInformation request
     */
    private Extent buildExtent(WcpsCoverageMetadata coverage) throws Exception {
        Spatial spatial = this.buildSpatial(coverage);
        Temporal temporal = this.buildTemporal(coverage);

        return new Extent(spatial, temporal);
    }
    
    /**
     * Build Spatial object for Extent object
     */
    private Spatial buildSpatial(WcpsCoverageMetadata wcpsCoverage) throws PetascopeException {
        String coverageId = wcpsCoverage.getCoverageName();
        Spatial result = null;

        if (wcpsCoverage.hasXYAxes() && CrsUtil.isValidTransform(wcpsCoverage.getXYAxes().get(0).getNativeCrsUri())) {
            List<List<BigDecimal>> bboxValues = new ArrayList<>();
            
            Coverage coverage = this.coverageRepositoryService.readCoverageBasicMetadataByIdFromCache(coverageId);
            if (coverage != null) {
                Wgs84BoundingBox wgs84BBox = coverage.getEnvelope().getEnvelopeByAxis().getWgs84BBox();

                if (wgs84BBox != null) {
                    // coverage is geo-referenced
                    List<BigDecimal> values = Arrays.asList(wgs84BBox.getMinLong(),
                                                            wgs84BBox.getMinLat(),
                                                            wgs84BBox.getMaxLong(),
                                                            wgs84BBox.getMaxLat());

                    bboxValues.add(values);
                    result = new Spatial(bboxValues);                
                }
            }
            
        }
        
        return result;
    }
    
    /**
     * Build Temporal object for Extent object
     */
    private Temporal buildTemporal(WcpsCoverageMetadata coverage) throws PetascopeException {
        Temporal result = null;
        
        List<List<String>> temporalValues = new ArrayList<>();
        for (Axis axis : coverage.getAxes()) {
            if (axis.isTimeAxis()) {
                String lowerBound = StringUtil.stripFirstAndLastQuotes(axis.getLowerGeoBoundRepresentation());
                String upperBound = StringUtil.stripFirstAndLastQuotes(axis.getUpperGeoBoundRepresentation());
                
                List<String> values = Arrays.asList(lowerBound, upperBound);
                
                temporalValues.add(values);
            }
        }
        
        if (!temporalValues.isEmpty()) {
            result = new Temporal(temporalValues);
        }
        
        return result;
    }

    /**
     * Get the list of CRSs of a coverage
     */
    private List<String> getCrsList(WcpsCoverageMetadata coverageMetadata) {
        List<String> result = new ArrayList<>();
        for (Axis axis : coverageMetadata.getAxes()) {
            if (!result.contains(axis.getNativeCrsUri())) {
                String crs = CrsUtil.createOpenGisUrl(axis.getNativeCrsUri());
                result.add(crs);
            }
        }
        
        return result;
    }
    
}
