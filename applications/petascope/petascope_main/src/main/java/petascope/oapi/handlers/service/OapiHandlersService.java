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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.rasdaman.config.VersionManager.WCS_VERSION_21;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.core.KVPSymbols.KEY_COVERAGEID;
import static petascope.core.KVPSymbols.KEY_FORMAT;
import static petascope.core.KVPSymbols.KEY_OUTPUT_TYPE;
import static petascope.core.KVPSymbols.KEY_REQUEST;
import static petascope.core.KVPSymbols.KEY_SERVICE;
import static petascope.core.KVPSymbols.KEY_SUBSET;
import static petascope.core.KVPSymbols.KEY_VERSION;
import static petascope.core.KVPSymbols.VALUE_GENERAL_GRID_COVERAGE;
import static petascope.core.KVPSymbols.VALUE_GET_COVERAGE;
import static petascope.core.KVPSymbols.WCS_SERVICE;
import petascope.core.json.cis11.JSONCoreCIS11;
import petascope.core.json.cis11.JSONCoreCIS11Builder;
import petascope.core.json.cis11.model.rangeset.DataBlock;
import petascope.core.json.cis11.model.rangeset.RangeSet;
import petascope.core.response.Response;
import petascope.oapi.handlers.model.Bbox;
import petascope.oapi.handlers.model.Collection;
import petascope.oapi.handlers.model.Collections;
import petascope.oapi.handlers.model.LandingPage;
import petascope.oapi.handlers.model.Link;
import petascope.util.MIMEUtil;
import static petascope.util.MIMEUtil.MIME_JSON;
import petascope.util.TimeUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wcps.result.executor.WcpsRasqlExecutor;
import petascope.wcs2.handlers.kvp.KVPWCSGetCoverageHandler;
import petascope.wcs2.handlers.kvp.KVPWCSProcessCoverageHandler;

/**
 * Class to return the objects for OAPI requests
 * 
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class OapiHandlersService {
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(OapiHandlersService.class);
    
    @Autowired
    private OapiCollectionService oapiCollectionService;
    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    @Autowired
    private JSONCoreCIS11Builder jsonBuilder;
    @Autowired
    private KVPWCSGetCoverageHandler getCoverageHandler;
    @Autowired
    private KVPWCSProcessCoverageHandler processCoverageHandler;
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private WcpsRasqlExecutor wcpsRasqlExecutor;
    
    /**
     *  https://oapi.rasdaman.org/rasdaman/oapi (7.3.1. API landing page), see: Landing Page Response Schema
    */
    public LandingPage getLandingPageResult(String urlPrefix) {
        String title = "rasdaman OGC API - Coverages";
        String description = "Prototype implementation of the OGC API - Coverages standard.";
        
        Link selfLink = Link.getSelfLink(urlPrefix);
        Link dataLink = Link.getDataLink(urlPrefix);
        Link wcpsLink = Link.getProcessLink(urlPrefix);
        
        List<Link> links = Arrays.asList(selfLink, dataLink, wcpsLink);
        
        return new LandingPage(title, description, links);
    }
    
    /**
     * Execute a WCPS query and return result
     */
    public Response executeWcpsQuery(Map<String, String[]> kvpParameters) throws Exception {
        return processCoverageHandler.handle(kvpParameters);
    }
    
    /**
     * 
     * The Collections operation returns a set of metadata which describes the collections (coverages) available from this API
     * e.g: https://oapi.rasdaman.org/rasdaman/oapi/collections (7.4.1. Collections -> 7.4.1.2. Response)
     * 
     * The collections array property contains the list of object (e.g: https://oapi.rasdaman.org/rasdaman/oapi/collections/mean_summer_airtemp)
     */
    public Collections getCollectionsResult(String urlPrefix, String bbox, String datetime) throws Exception {
        Link dataLink = Link.getDataLink(urlPrefix);

        // each coverage is a collection, we just need the ids to create links to them
        List<String> coverageIds = coverageRepositoryService.readAllLocalCoverageIds();
        // apply coverages filters if necessary
        if (bbox != null || datetime != null) {
            coverageIds = getIntersectingCoveragesResult(coverageIds, bbox, datetime);
        }
        
        List<Collection> collections = new ArrayList<>();
        for (String coverageId : coverageIds) {
            Collection collection = this.getCollectionInformationResult(coverageId, urlPrefix);
            collections.add(collection);
        }

        return new Collections(collections, Arrays.asList(dataLink));
    }
    
    /**
     * NOTE: it also allows to filter the coverages by temporal (datetime parameter) and spatial (bbox parameter) 
     * e.g: https://oapi.rasdaman.org/rasdaman/oapi/collections?bbox=-175,-80,180,90&datetime="01-01-2015"
     */
    private List<String> getIntersectingCoveragesResult(List<String> coverageIds, String bbox, String dateTime) throws Exception {
        Bbox inputBbox = null;
        if (bbox != null) {
            inputBbox = Bbox.fromString(bbox);
        }
        
        List<String> result = new ArrayList<>();
        for (String coverageId : coverageIds) {
            WcpsCoverageMetadata wcpsCoverage = wcpsCoverageMetadataTranslator.translate(coverageId);
            
            boolean dateFilterPassed = false;
            if (dateTime != null) {
                for (Axis axis : wcpsCoverage.getAxes()) {
                    if (axis.isTimeAxis()) {
                        if (TimeUtil.isInTimeInterval(dateTime, axis.getLowerGeoBoundRepresentation(), axis.getUpperGeoBoundRepresentation())) {
                            dateFilterPassed = true;
                            break;
                        }
                    }
                }
            }
            
            if (inputBbox != null) {
                if (dateTime == null || dateFilterPassed) {
                    Coverage coverage = this.coverageRepositoryService.readCoverageBasicMetadataByIdFromCache(coverageId);
                    if (coverage != null) {
                        Wgs84BoundingBox wgs84BBox = coverage.getEnvelope().getEnvelopeByAxis().getWgs84BBox();

                        // For only geo-referenced coverages which can be projected bbox to EPSG:4326 
                        if (wgs84BBox != null && inputBbox.intersects(Bbox.fromWgs84BoundingBox(wgs84BBox))) {
                            result.add(coverageId);
                        }
                    }
                }
            }
        }

        return result;
    }
    
    /**
     * Collection Information is the set of metadata which describes a single collection, or in the the case of API-Coverages, a single Coverage.
     * It is comparable to a **WCS DescribeCoverage**.
     * e,g: https://oapi.rasdaman.org/rasdaman/oapi/collections/mean_summer_airtemp (7.4.2. Collection Information -> Collection Information Resource Example)
     */
    public Collection getCollectionInformationResult(String coverageId, String urlPrefix) throws Exception {
        Collection collection = this.oapiCollectionService.buildCollectionFromCoverage(wcpsCoverageMetadataTranslator.translate(coverageId), urlPrefix);
        return collection;
    }

    /**
     * Return the JSON core object in CIS 1.1
     */
    public JSONCoreCIS11 getJSONCoreCIS11Result(String coverageId) throws Exception {
        WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(coverageId);
        JSONCoreCIS11 result = jsonBuilder.build(wcpsCoverageMetadata);
        
        return result;
    }

    /**
     * Returns the coverage including all of its components (domain set, range type, range set and metadata).
     * It is comparable to a **WCS GetCoverage**.
     * 
     * e.g: https://oapi.rasdaman.org/rasdaman/oapi/collections/S2_FALSE_COLOR_84/coverage?subset=Lat(51.9:52.1),Long(-4.1:-3.9),ansi("2018-11-14")&f=gml
     */
    public Response getCoverageSubsetResult(String coverageId, String[] subsets, String outputFormat) throws Exception {
        Map<String, String[]> kvpParams = new HashMap<>();
        
        String[] service = {WCS_SERVICE};
        String[] version = {WCS_VERSION_21};
        String[] request = {VALUE_GET_COVERAGE};
        String[] format = {outputFormat};
        String[] coverageIds = {coverageId};

        kvpParams.put(KEY_SERVICE, service);
        kvpParams.put(KEY_VERSION, version);
        kvpParams.put(KEY_REQUEST, request);
        
        if (subsets != null) {
            kvpParams.put(KEY_SUBSET, subsets);
        }
        if (outputFormat != null) {
            kvpParams.put(KEY_FORMAT, format);
        } else {
            // NOTE: default is JSON in CIS 1.1 if output format is not specified
            kvpParams.put(KEY_FORMAT, new String[] { MIME_JSON });
            kvpParams.put(KEY_OUTPUT_TYPE, new String[] { VALUE_GENERAL_GRID_COVERAGE });
        }
        
        kvpParams.put(KEY_COVERAGEID, coverageIds);

        return getCoverageHandler.handle(kvpParams);
    }

    public RangeSet getCoverageRangeSetResult(String coverageId, String[] subsets) throws Exception {
        Response response = getCoverageSubsetResult(coverageId, subsets, MIMEUtil.MIME_JSON);
        byte[] bytes = null;
        if (!response.getDatas().isEmpty()) {
            bytes = response.getDatas().get(0);
        }
        
        List<Object> pixelValues = this.wcpsRasqlExecutor.getJsonPixelValues(bytes);
        
        return new RangeSet(new DataBlock(pixelValues)); 
    }

}
