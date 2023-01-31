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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.handlers.kvp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
import petascope.controller.PetascopeController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_CLIP;
import static petascope.core.KVPSymbols.KEY_COVERAGEID;
import static petascope.core.KVPSymbols.KEY_FORMAT;
import static petascope.core.KVPSymbols.KEY_INTERNAL_WCPS_FROM_WCS_GET_COVERAGE;
import static petascope.core.KVPSymbols.KEY_INTERPOLATION;
import static petascope.core.KVPSymbols.KEY_MEDIATYPE;
import static petascope.core.KVPSymbols.KEY_OUTPUT_CRS;
import static petascope.core.KVPSymbols.KEY_OUTPUT_TYPE;
import static petascope.core.KVPSymbols.KEY_RANGESUBSET;
import static petascope.core.KVPSymbols.KEY_REQUEST;
import static petascope.core.KVPSymbols.KEY_SCALEAXES;
import static petascope.core.KVPSymbols.KEY_SCALEEXTENT;
import static petascope.core.KVPSymbols.KEY_SCALEFACTOR;
import static petascope.core.KVPSymbols.KEY_SCALESIZE;
import static petascope.core.KVPSymbols.KEY_SERVICE;
import static petascope.core.KVPSymbols.KEY_SUBSET;
import static petascope.core.KVPSymbols.KEY_SUBSETTING_CRS;
import static petascope.core.KVPSymbols.KEY_VERSION;
import static petascope.core.KVPSymbols.VALUE_GENERAL_GRID_COVERAGE;
import petascope.core.response.Response;
import petascope.core.service.ResponseService;
import petascope.exceptions.WMSException;
import petascope.util.CrsUtil;
import petascope.util.JSONUtil;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import petascope.util.SetUtil;
import petascope.util.StringUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;
import petascope.wcs2.handlers.kvp.service.KVPWCSGetCoverageInterpolationService;
import petascope.wcs2.handlers.kvp.service.KVPWCSGetCoverageRangeSubsetService;
import petascope.wcs2.handlers.kvp.service.KVPWCSGetCoverageScalingService;
import petascope.wcs2.handlers.kvp.service.KVPWCSGetCoverageSubsetDimensionService;
import petascope.wcs2.handlers.kvp.service.KVPWCSGetcoverageClipService;
import petascope.wcs2.parsers.subsets.AbstractSubsetDimension;
import static petascope.util.ras.RasConstants.RASQL_OPEN_SUBSETS;
import static petascope.util.ras.RasConstants.RASQL_CLOSE_SUBSETS;

/**
 * Class which handles the WCS GetCoverage request and translates to a WCPS
 * request. So WCS will not actually do anything for processing coverage.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWCSGetCoverageHandler extends KVPWCSAbstractHandler {

    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;

    // Services for GetCoverageKVP
    @Autowired
    private ResponseService responseService;
    @Autowired
    private KVPWCSGetCoverageSubsetDimensionService kvpGetCoverageSubsetDimensionService;
    @Autowired
    private KVPWCSGetCoverageRangeSubsetService kvpGetCoverageRangeSubsetService;
    @Autowired
    private KVPWCSGetCoverageInterpolationService kvpGetCoverageInterpolationService;
    @Autowired
    private KVPWCSGetCoverageScalingService kvpGetCoverageScalingService;
    @Autowired
    private KVPWCSGetcoverageClipService kvpGetCoverageClipService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private PetascopeController petascopeController;
    
    private static final Logger log = LoggerFactory.getLogger(KVPWCSGetCoverageHandler.class);
    
    protected static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_SERVICE, KEY_VERSION, KEY_REQUEST, KEY_COVERAGEID, KEY_SUBSET,
                                                                      KEY_SCALEAXES, KEY_SCALEEXTENT, KEY_SCALEFACTOR, KEY_SCALESIZE, 
                                                                      KEY_RANGESUBSET, KEY_INTERPOLATION,
                                                                      KEY_SUBSETTING_CRS, KEY_OUTPUT_CRS,
                                                                      KEY_FORMAT, KEY_CLIP,
                                                                      KEY_MEDIATYPE,
                                                                      KEY_OUTPUT_TYPE);
    protected static Set<String> UNIQUE_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_SERVICE, KEY_VERSION, KEY_REQUEST, KEY_COVERAGEID,
                                                                                    KEY_SUBSETTING_CRS, KEY_OUTPUT_CRS,
                                                                                    KEY_INTERPOLATION,
                                                                                    KEY_FORMAT, KEY_CLIP,
                                                                                    KEY_MEDIATYPE,
                                                                                    KEY_OUTPUT_TYPE);
    
    public static final String ENCODE_FORMAT = "$encodeFormat";
    public static final String RANGE_NAME = ".$rangeName";
    public static final String EXTRA_OPTIONS = "$extra_options";

    // e.g: for c in (test_mr) return encode(c[i(0:10), j(0:20)], "png")
    private static final String WCPS_QUERY_TEMPLATE = "for c in ($coverageId) return encode($queryContent, \"" + ENCODE_FORMAT + "\", \"" + EXTRA_OPTIONS + "\")";
    
    private static final String WCPS_COVERAGE_ALIAS = "c";

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        // GetCoverage can contain multiple coverageIds (e.g: coverageId=test_mr,test_irr_cube_2)
        // NOTE: in case of requests with multipart/related so the result is 1 DescribeCoverage in GML and 1 GetCoverage in requested format (e.g: tiff)                
        if (kvpParameters.get(KVPSymbols.KEY_COVERAGEID) == null) {
            throw new WCSException(ExceptionCode.InvalidRequest, "A GetCoverage request must specify at least one " + KVPSymbols.KEY_COVERAGEID + ".");
        }
        
        this.validateParameters(kvpParameters, VALID_PARAMETERS);        
        this.validateCoverageConversionCIS11(kvpParameters);
        this.validateUniqueParameters(kvpParameters, UNIQUE_PARAMETERS);
        
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, WCSException, SecoreException, WMSException, Exception {
        // Validate before handling the request
        this.validate(kvpParameters);
        
        String[] coverageIds = kvpParameters.get(KVPSymbols.KEY_COVERAGEID)[0].split(",");
        
        // Store the extra params from WCS which can be added to WCPS's one
        Map<String, String> extraOptions = new LinkedHashMap<>();

        // As GetCoverage can contain multiple coverageIds
        List<Response> responses = new ArrayList<>();

        // Translated WCPS query from WCS request
        String wcpsQuery = "";
        for (String coverageId : coverageIds) {
            // Build a WcpsCoverageMetadata from persisted coverage to handle the WCS GetCoverage request
            WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(coverageId);

            // Interpolation extension
            String interpolationType = null;
          
            if (kvpParameters.get(KVPSymbols.KEY_INTERPOLATION) != null) {
                String[] interpolations = kvpParameters.get(KVPSymbols.KEY_INTERPOLATION);
                interpolationType = kvpGetCoverageInterpolationService.handleInterpolation(interpolations);
            }

            // Subset extension
            // e.g: subset=i(10,20)&subset=j(10,20)
            // or with the subsettingCRS for this axis: subset=E,http://www.opengis.net/def/crs/EPSG/0/3857(-1.3637472939075228E7,-1.3636585328807762E7)
            List<AbstractSubsetDimension> subsetDimensions = kvpGetCoverageSubsetDimensionService.parseSubsets(kvpParameters, wcpsCoverageMetadata);

            // RangeSubset extension
            // e.g: b1:b5,b3,b7,b10
            String rangeSubsets = null;
            if (kvpParameters.get(KVPSymbols.KEY_RANGESUBSET) != null) {
                rangeSubsets = kvpParameters.get(KVPSymbols.KEY_RANGESUBSET)[0];
            }

            // RangeSubset extension handlers
            if (rangeSubsets != null) {
                kvpGetCoverageRangeSubsetService.handleRangeSubsets(wcpsCoverageMetadata, rangeSubsets.trim().split(","));
            }

            // Generate the WCPS query from the translated WcpsCoverageMetadata
            String generateCoverageExpression = this.generateCoverageExpression(kvpParameters,
                    wcpsCoverageMetadata, subsetDimensions, interpolationType);

            // The main content of WCPS query
            String queryContent;
            // Range constructor handlers for singleband and multibands coverages

            // e.g: test_mr covearge with only 1 band
            queryContent = kvpGetCoverageRangeSubsetService.generateRangeConstructorWCPS(wcpsCoverageMetadata,
                    generateCoverageExpression, rangeSubsets);

            // Scale extension
            queryContent = kvpGetCoverageScalingService.handleScaleExtension(queryContent, kvpParameters);

            // Output format
            String requestedMime = MIMEUtil.MIME_GML;
            if (kvpParameters.get(KVPSymbols.KEY_FORMAT) != null) {
                // e.g: image/png if it exists in the request
                requestedMime = kvpParameters.get(KVPSymbols.KEY_FORMAT)[0];
            }
            
            String outputType = this.getKVPValue(kvpParameters, KEY_OUTPUT_TYPE);
            if (outputType != null && outputType.equalsIgnoreCase(VALUE_GENERAL_GRID_COVERAGE)) {
                extraOptions.put(KEY_OUTPUT_TYPE, VALUE_GENERAL_GRID_COVERAGE);
            }
            
            String options = "";
            if (extraOptions.size() > 0) {
                options = JSONUtil.serializeObjectToJSONStringNoIndentation(extraOptions);
                options = JSONUtil.escapeQuote(options);
            }
            
            wcpsQuery = WCPS_QUERY_TEMPLATE.replace("$coverageId", coverageId)
                                           .replace("$queryContent", queryContent)
                                           .replace(EXTRA_OPTIONS, options);
            
            StringUtil.putKeyToKVPMaps(kvpParameters, KEY_INTERNAL_WCPS_FROM_WCS_GET_COVERAGE, Boolean.TRUE.toString());

            // Handle multipart for WCS (WCPS) request if any or non multipart            
            Response responseTmp = responseService.handleWCPSResponse(kvpParameters, wcpsQuery, requestedMime);
            responses.add(responseTmp);
        }

        return responseService.buildResponse(responses);
    }

    /**
     * Generate a coverage expression (e.g: c[i(0:20)])
     *
     * @param wcpsCoverageMetadata
     * @return
     */
    private String generateCoverageExpression(Map<String, String[]> kvpParameters,
            WcpsCoverageMetadata wcpsCoverageMetadata, List<AbstractSubsetDimension> subsetDimensions, String interpolationType) throws WCSException {

        // Crs Extension: Translate from the input CRS (subsettingCrs) to native CRS (XYAxes's Crs)
        String subsettingCrs = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_SUBSETTING_CRS);
        // Translate from 2D geo-referenced coverage nativeCRS to outputCrs
        String outputCrs = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_OUTPUT_CRS);
        
        // Then validate if these CRSs are good
        try {
            if (subsettingCrs != null) {
                CrsUtil.getCrsDefinition(subsettingCrs);
            }
        } catch (PetascopeException ex) {
            throw new WCSException(ExceptionCode.SubsettingCRSNotSupported,
                                   "Failed get CRS defintion from subsettingCRS '" + subsettingCrs + "'. Reason: " + ex.getMessage(), ex);
        }
        try {
            if (outputCrs != null) {
                CrsUtil.getCrsDefinition(outputCrs);
            }
        } catch (PetascopeException ex) {
            throw new WCSException(ExceptionCode.OutputCRSNotSupported,
                                   "Failed get CRS defintion from outputCRS '" + outputCrs + "'. Reason: " + ex.getMessage(), ex);
        }

        List<String> intervals = new ArrayList<>();
        for (AbstractSubsetDimension subsetDimension : subsetDimensions) {
            Axis axis = wcpsCoverageMetadata.getAxisByName(subsetDimension.getDimensionName());
            // Only add the axis which is requested with subset parameter
            if (axis != null) {
                String crsAxis = subsetDimension.getCrs();
                String axisDimension = "";
                if (crsAxis == null) {
                    // e.g: Lat(0:20)
                    axisDimension = axis.getLabel() + subsetDimension.getSubsetBoundsRepresentationWCPS();
                } else {
                    // e.g: Lat,http://....3857(350000,50000)
                    // it is equivalent to subssetingCRS but for only current axis
                    axisDimension = axis.getLabel() + ":" + "\"" + crsAxis + "\"" + subsetDimension.getSubsetBoundsRepresentationWCPS();
                }
                if (subsettingCrs != null) {
                    // NOTE: if subsettingCrs is not null then X and Y must be in the subset parameters or they are not and it will use the outputCrs for them
                    if (axis.isXYGeoreferencedAxis()) {
                        // subsettingCRS parameter with input interval will be translated to interval in nativeCRS
                        // e.g: Lat(350000:450000)&subsettingCrs=http://...3857
                        // NOTE: only apply on the XY geo-referenced axes, as it is not valid to add EPSG:4326 to timeAxis
                        axisDimension = axis.getLabel() + ":" + "\"" + subsettingCrs + "\"" + subsetDimension.getSubsetBoundsRepresentationWCPS();
                    }
                }

                // Now, can add the axis dimension to the coverage expression
                intervals.add(axisDimension);
            }
        }

        // e.g: c[i(0:20)] or c[i(0:20)].red and depend on the imported coverage has 1 or multibands (!)
        // as: test_mr (1 band) cannot have this query: c[i(0:20)].0 but test_rgb (3 bands) can have: c[i(0:20)].0
        String coverageExpression;
        if (intervals.isEmpty()) {
            // e.g: no subsets
            coverageExpression = WCPS_COVERAGE_ALIAS + RANGE_NAME;
        } else {
            // e.g: subset=Lat(20:30)
            coverageExpression = WCPS_COVERAGE_ALIAS + RASQL_OPEN_SUBSETS + ListUtil.join(intervals, ", ") + RASQL_CLOSE_SUBSETS + RANGE_NAME;
        }        

        // NOTE: if subsettingCrs exists but outputCrs is null, then outputCrs is subsettingCrs
        if (subsettingCrs != null && outputCrs == null) {
            outputCrs = subsettingCrs;
        }
        
        // Handle for WCS WKT clipping extension if necessary (i.e: when clip parameter exists in the request)
        coverageExpression = this.kvpGetCoverageClipService.handle(kvpParameters, coverageExpression, subsettingCrs);

        if (outputCrs != null) {
            // Generate the CrsTransform expression
            coverageExpression = "crsTransform(" + coverageExpression;
            List<String> transformAxes = new ArrayList<>();
            for (Axis axis : wcpsCoverageMetadata.getAxes()) {
                if (axis.isXYGeoreferencedAxis()) {
                    transformAxes.add(axis.getLabel() + ":" + "\"" + outputCrs + "\"");
                }
            }
            
            String interpolationExpression = "{}";
            if (interpolationType != null) {
                interpolationExpression = "{" + interpolationType + "}";
            }
            coverageExpression += ", { " + ListUtil.join(transformAxes, ", ") + " }, " + interpolationExpression + " )";
        }

        return coverageExpression;
    }

}
