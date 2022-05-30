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
/**
 * Handles update of coverages into petascope, according to the WCS-T
 * specs.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
package petascope.wcst.handlers;

import com.rasdaman.accesscontrol.service.AuthenticationService;
import com.rasdaman.admin.layer.service.AdminCreateOrUpdateLayerService;

import petascope.core.Pair;
import petascope.core.XMLSymbols;
import petascope.core.gml.cis10.GMLCIS10ParserService;
import petascope.util.CrsUtil;
import petascope.exceptions.WCSException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.PetascopeException;
import nu.xom.Document;
import nu.xom.Element;
import org.slf4j.LoggerFactory;
import petascope.util.*;
import petascope.util.ras.TypeResolverUtil;
import petascope.core.service.CrsComputerService;
import petascope.core.response.Response;
import petascope.wcst.helpers.decodeparameters.RangeParametersConvertor;
import petascope.wcst.helpers.decodeparameters.RangeParametersConvertorFactory;
import petascope.wcst.helpers.update.RasdamanUpdater;
import petascope.wcst.helpers.update.RasdamanUpdaterFactory;
import petascope.wcst.helpers.validator.GridDomainsValidator;
import petascope.wcst.helpers.validator.UpdateCoverageValidator;
import petascope.wcst.helpers.RemoteCoverageUtil;
import petascope.wcs2.parsers.subsets.SlicingSubsetDimension;
import petascope.wcs2.parsers.subsets.AbstractSubsetDimension;
import petascope.wcs2.parsers.subsets.TrimmingSubsetDimension;
import petascope.wcst.parsers.UpdateCoverageRequest;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import nu.xom.Elements;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.Axis;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.domain.cis.IrregularAxis.CoefficientStatus;
import org.rasdaman.domain.cis.RasdamanDownscaledCollection;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.CrsDefinition;
import petascope.core.gml.cis.service.GMLCISParserService;
import petascope.core.gml.metadata.model.CoverageMetadata;
import petascope.core.gml.metadata.model.LocalMetadataChild;
import petascope.core.gml.metadata.service.CoverageMetadataService;
import static petascope.core.service.CrsComputerService.GRID_POINT_EPSILON_WCPS;
import petascope.exceptions.ExceptionCode;
import org.rasdaman.admin.pyramid.service.PyramidService;

import petascope.wcst.exceptions.WCSTCoverageParameterNotFound;
import petascope.wcst.exceptions.WCSTInvalidXML;
import petascope.wcps.metadata.model.ParsedSubset;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.util.ras.RasConstants.RASQL_OPEN_SUBSETS;
import static petascope.util.ras.RasConstants.RASQL_CLOSE_SUBSETS;

import org.rasdaman.repository.service.WMSRepostioryService;
import petascope.controller.PetascopeController;

@Service
public class UpdateCoverageHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UpdateCoverageHandler.class);
    
    @Autowired
    private CoverageRepositoryService persistedCoverageService;
    @Autowired
    private WMSRepostioryService wmsRepostioryService;
    @Autowired
    private GMLCISParserService gmlCISParserService;
    @Autowired
    private RasdamanUpdaterFactory rasdamanUpdaterFactory;
    @Autowired
    private RangeParametersConvertorFactory rangeParametersConvertorFactory;
    @Autowired
    private GridDomainsValidator gridDomainsValidator;
    @Autowired
    private PyramidService pyramidService;
    @Autowired
    private CoverageMetadataService coverageMetadataService;
    @Autowired
    private AdminCreateOrUpdateLayerService createOrUpdateLayerService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private PetascopeController petascopeController;

    @Autowired
    private RemoteCoverageUtil remoteCoverageUtil;
    private static final String FILE_PROTOCOL = "file://";

    /**
     * Handles the update of an existing WCS coverage.
     *
     * @param request the update coverage request.
     * @return empty response.
     */
    public Response handle(UpdateCoverageRequest request)
            throws WCSTCoverageParameterNotFound, WCSTInvalidXML, PetascopeException, SecoreException, Exception {
        log.debug("Handling coverage update...");
        
        this.petascopeController.validateWriteRequestFromIP(httpServletRequest);
        
        // persisted coverage
        Coverage currentCoverage = persistedCoverageService.readCoverageByIdFromDatabase(request.getCoverageId());
        String coverageId = request.getCoverageId();

        String gmlInputCoverage = getGmlCoverageFromRequest(request);
        Document gmlInputCoverageDocument;
        try {
            gmlInputCoverageDocument = XMLUtil.buildDocument(null, gmlInputCoverage);
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                            "Cannot parse the input GML from UpdateCoverage request to XML document. Reason: " + ex.getMessage(), ex);
        }
        // Build input Coverage object from GML document (each slice is a input coverage) to update the persisted coverage
        Coverage inputCoverage = gmlCISParserService.parseDocumentToCoverage(gmlInputCoverageDocument);
        inputCoverage.setId(currentCoverage.getId());

	this.handleUpdateCoverageRequest(request, currentCoverage, inputCoverage, gmlInputCoverageDocument);
        
        persistedCoverageService.createCoverageExtent(currentCoverage);
        
        // Now, we can persist the updated current coverage from input slice
        persistedCoverageService.save(currentCoverage);
        
        // If this coverage has an existing associated WMS layer, then update the coverage -> update the layer as well
        if (this.wmsRepostioryService.isInLocalCache(coverageId)) {
            this.createOrUpdateLayerService.save(coverageId, null);
        }

        Response response = new Response();
        response.setCoverageID(coverageId);
        
        return response;
    }
    
    /**
     * Handle update coverage request from GML to rasdaman collection
     */
    private void handleUpdateCoverageRequest(UpdateCoverageRequest request, 
                                             Coverage currentCoverage, 
                                             Coverage inputCoverage, Document gmlInputCoverageDocument) 
            throws PetascopeException, SecoreException, IOException {
        
        String affectedCollectionName = currentCoverage.getRasdamanRangeSet().getCollectionName();
        
        List<AbstractSubsetDimension> dimensionSubsets = request.getSubsets();

        // Shift the domains for regular axes if necessary in case they are overlapping 
        // with current imported domains by an odd float numbers (i.e: they are not aligned).
        this.shiftRegularAxesByOffsets(currentCoverage, dimensionSubsets, inputCoverage);

        //validation
        UpdateCoverageValidator updateCoverageValidator = new UpdateCoverageValidator(currentCoverage, inputCoverage,
                dimensionSubsets, request.getRangeComponent());
        updateCoverageValidator.validate();

        //handle subset coefficients if necessary for coverage with irregular axis
        Pair<String, Integer> expandedAxisDimensionPair = handleSubsetCoefficients(request, currentCoverage, dimensionSubsets, inputCoverage);

        //handle cell values
        Element rangeSet = GMLCIS10ParserService.parseRangeSet(gmlInputCoverageDocument.getRootElement());

        TreeMap<Integer, String> gridDomainsPairsMap = getPixelIndicesByCoordinate(currentCoverage, dimensionSubsets);
        String affectedDomain = getAffectedDomain(currentCoverage, dimensionSubsets, gridDomainsPairsMap);

        // Only support GeneralGridCoverage now
        List<IndexAxis> inputIndexAxes = ((GeneralGridCoverage) inputCoverage).getIndexAxes();

        // NOTE: need to validate the grid domains from input coverage slice 
        // and the output grid domains which is calculated from the subsets with exising coverage
        gridDomainsValidator.validate(inputIndexAxes, affectedDomain);

        String shiftDomain = getShiftDomain(inputCoverage, currentCoverage, gridDomainsPairsMap);
        RasdamanUpdater updater;
        
        String username = ConfigManager.RASDAMAN_ADMIN_USER;
        String password = ConfigManager.RASDAMAN_ADMIN_PASS;

        Elements dataBlockElements = rangeSet.getChildElements(XMLSymbols.LABEL_DATABLOCK,
                XMLSymbols.NAMESPACE_GML);
        if (dataBlockElements.size() != 0) {
            //tuple list given explicitly
            String values = getReplacementValuesFromTupleList(currentCoverage, rangeSet, request.getPixelDataType());
            updater = rasdamanUpdaterFactory.getUpdater(affectedCollectionName, affectedDomain, values, shiftDomain, username, password);                
            updater.updateWithFile();                
        } else {
            //tuple list given as file
            //retrieve the file, if needed
            boolean isLocal = false;
            byte[] bytes = null;
            String fileUrl = GMLCIS10ParserService.parseFilePath(rangeSet);
            if (isLocalFile(fileUrl)) {
                fileUrl = fileUrl.replace(FILE_PROTOCOL, "");
                isLocal = true;
                
                // NOTE: In case the input file has been uploaded to the local server
                // then use this file path instead of the client file path created inside the GML.
                if (request.getUploadedFilePath() != null) {
                    fileUrl = request.getUploadedFilePath();
                }
            } else {
                // remote file, get it as bytes
                bytes = getReplacementValuesFromFileAsBytes(rangeSet);
            }
            String mimetype = GMLCIS10ParserService.parseMimeType(rangeSet);
            Integer overviewIndex = GMLCIS10ParserService.parseOverviewIndex(rangeSet);
            
            // e.g: netCDF test_eobstest: "{"variables": ["tg"]}",
            String rangeParameters = GMLCIS10ParserService.parseRangeParameters(rangeSet);

            //process the range parameters
            RangeParametersConvertor convertor = rangeParametersConvertorFactory.getConvertor(mimetype, rangeParameters, currentCoverage);
            String decodeParameters = convertor.toRasdamanDecodeParameters();

            updater = rasdamanUpdaterFactory.getUpdater(affectedCollectionName,
                    affectedDomain, fileUrl, mimetype, shiftDomain, decodeParameters, username, password,
                    isLocal, overviewIndex);
            if (isLocal) {
                updater.updateWithFile();                
                if (request.getUploadedFilePath() != null) {
                    // Remove the uploaded file after it has been updated to rasdaman
                    Files.deleteIfExists(Paths.get(fileUrl));
                }
            } else {
                updater.updateWithBytes(bytes);                    
            }
        }

        // After updating rasdaman collection, we need to update current coverage with new geo, grid domains
        updateGeoDomains(currentCoverage, inputCoverage, dimensionSubsets);
        updateAxisExtents(currentCoverage);
        updateGridDomains(currentCoverage, gridDomainsPairsMap, expandedAxisDimensionPair);

        for (CoveragePyramid coveragePyramid : currentCoverage.getPyramid()) {
            // If coverage has pyramid members and some of them need to be synced,
            // then update these pyramid member coverages from current input data by subsets
            // don't update the default pyramid level with level 1 for all axes
            if (coveragePyramid.isSynced() && !coveragePyramid.getPyramidMemberCoverageId().equals(currentCoverage.getCoverageId())) {                
                Coverage pyramidMemberCoverage = this.persistedCoverageService.readCoverageFullMetadataByIdFromCache(coveragePyramid.getPyramidMemberCoverageId());
                String downscaledCollectionName = pyramidMemberCoverage.getRasdamanRangeSet().getCollectionName();
                
                // then, update the pyramid member's coverage grid domains as well
                this.pyramidService.updateGridAndGeoDomainsOnDownscaledLevelCoverage((GeneralGridCoverage)currentCoverage, 
                                                                               (GeneralGridCoverage)pyramidMemberCoverage,
                                                                               coveragePyramid.getScaleFactorsList());
                
                // then, create downscaled collection
                this.pyramidService.updateDownscaledLevelCollection((GeneralGridCoverage)currentCoverage, 
                                                                    (GeneralGridCoverage)pyramidMemberCoverage,
                                                                    downscaledCollectionName,
                                                                    coveragePyramid, 
                                                                    new ArrayList<>(gridDomainsPairsMap.values()), username, password);
            }
        }

        // Since version 9.7, WCST_Import can add local metadata from slice (input file) to coverage's metadata in Petascope.
        this.addLocalMetadataToCoverageMetadata(inputCoverage, currentCoverage);
    }
    
    /**
     * Check if a file path is a local file (local server) or remote file (remote server).
     */
    private boolean isLocalFile(String filePath) {
        return filePath.startsWith(FILE_PROTOCOL) || !filePath.matches("\\w+://.*");        
    }
    
    /**
    * NOTE: Since version 9.7, WCST_Import supports local_metadata in general recipes, then
    * inputCoverage from GML will contain local metadata as its global metadata. This metadata needs to be appended
    * inside current coverage under XML element: <localMetadata> </localMetadata> or JSON array of objects: localMetadata[{...}, {...}, ...]
    */
    private void addLocalMetadataToCoverageMetadata(Coverage inputCoverage, Coverage currentCoverage) throws PetascopeException {
        String localMetadata = inputCoverage.getMetadata().trim();
        
        if (!localMetadata.isEmpty()) {
            // Only update current coverage's metadata if input coverage has metadata to be added            
            LocalMetadataChild inputLocalMetadata = null;
            inputLocalMetadata = this.coverageMetadataService.deserializeLocalMetadata(inputCoverage.getMetadata());
            
            CoverageMetadata currentCoverageMetadata = this.coverageMetadataService.deserializeCoverageMetadata(currentCoverage.getMetadata());
            
            // Only add local meta from input coverage if current coverage does not contain it inside coverage's metadata
            if (inputLocalMetadata != null && !currentCoverageMetadata.containLocalMetadataInList(inputLocalMetadata)) {
                currentCoverageMetadata.addLocalMetadataToList(inputLocalMetadata);
            }    
            
            // After adding new local metadata child, serialize coverage's metadata to the imported original format (XML/JSON).
            String updatedCurrentCoverageMetadataStr = "";
            if (XMLUtil.containsXMLContent(currentCoverage.getMetadata())) {
                // XML format
                updatedCurrentCoverageMetadataStr = this.coverageMetadataService.serializeCoverageMetadataInXML(currentCoverageMetadata);
            } else {
                // JSON format
                updatedCurrentCoverageMetadataStr = this.coverageMetadataService.serializeCoverageMetadataInJSON(currentCoverageMetadata);
            }
            
            //  After that, a new local metadata root from input coverage is added and persisted to database of current coverage's medata
            currentCoverage.setMetadata(updatedCurrentCoverageMetadataStr);            
        }
    }
    
    /**
     * Same as behavior from gdal_merge.py to shift an odd number to the nearest rounded number.
     * NOTE: it is very important to shift like this or the grid/geo domains from coveraged imported by WCS-T 
     * will be inconsistent from gdal_merge.py.
     * 
     * e.g: 3.2 -> 3, 3.5 -> 4
     */
    private BigDecimal shiftToRoundedNumber(BigDecimal number) {
        // NOTE: Add an epsilon before shifting in case the result is double but wrapped as BigDecimal with losing precision (e.g: for DateTime axis)
        number = number.add(GRID_POINT_EPSILON_WCPS);
        return number.add(new BigDecimal("0.5")).setScale(0, RoundingMode.FLOOR);
    }
    
    /**
     * NOTE: When input slice is overlapping with current domains of regular axes (e.g: Lat, Long) and they are not match
     * in grid domains (i.e: the difference between grid lower bound of current axis and grid lower bound of input slice is float).
     * Then, the geo domain of this axis from input slice needs to be shifted by an offset value and this input slice could be imported
     * to Petascope instead of throwing exception from validator because grid domains don't match.
     */
    private void shiftRegularAxesByOffsets(Coverage currentCoverage, List<AbstractSubsetDimension> inputDimensionSubsets, Coverage inputCoverage) throws PetascopeException, SecoreException {
        
        for (AbstractSubsetDimension inputDimensionSubset : inputDimensionSubsets) {
            
            if (inputDimensionSubset instanceof SlicingSubsetDimension) {
                continue;
            }
            
            String axisLabel = inputDimensionSubset.getDimensionName();
            GeoAxis geoAxis = ((GeneralGridCoverage) currentCoverage).getGeoAxisByName(axisLabel);
            
            if (geoAxis instanceof org.rasdaman.domain.cis.RegularAxis) {
                ParsedSubset<BigDecimal> subsetDomain = CrsComputerService.parseSubsetDimensionToNumbers(geoAxis.getSrsName(), geoAxis.getUomLabel(), inputDimensionSubset);
                // NOTE: Only support this feature if axis is regular
                BigDecimal currentGeoLowerBound = geoAxis.getLowerBoundNumber();
                BigDecimal currentGeoUpperBound = geoAxis.getUpperBoundNumber();
                
                BigDecimal inputGeoLowerBound = subsetDomain.getLowerLimit();
                BigDecimal inputGeoUpperBound = subsetDomain.getUpperLimit();
                
                BigDecimal axisResolution = geoAxis.getResolution();                
                BigDecimal lowerGeoOffSet = BigDecimal.ZERO;
                BigDecimal upperGeoOffSet = BigDecimal.ZERO;
                
                boolean positiveAxisDirection = true;  
                
                // Check axis direction (e.g: Long origin at lowerBound, Lat origin at upperBound)
                if (axisResolution.compareTo(BigDecimal.ZERO) < 0) {
                    positiveAxisDirection = false;
                }
                
                GeoAxis currentGeoAxis = ((GeneralGridCoverage) currentCoverage).getGeoAxisByName(axisLabel);
                GeoAxis inputGeoAxis = ((GeneralGridCoverage) inputCoverage).getGeoAxisByName(axisLabel);
                
                BigDecimal gridLowerDistance = BigDecimal.ZERO;
                BigDecimal gridUpperDistance = BigDecimal.ZERO;
                
                BigDecimal origin = BigDecimal.ZERO;
                
                
                if (positiveAxisDirection) {
                    // e.g: Long axis with positive direction
                    
                    // NOTE: don't need to shift anything if origin from current imported coverage's geo domain is the same as from input geo domain
                    if (inputGeoLowerBound.compareTo(currentGeoLowerBound) == 0) {
                        continue;
                    }
                    
                    // First, find the new origin between input subset and current subet (for positive axis, lowerBound is origin)
                    if (inputGeoLowerBound.compareTo(currentGeoLowerBound) < 0) {
                        // In this case, ***coverage's domain*** must be shifted by an offset as the origin belongs to the input subset    
                        gridLowerDistance = (currentGeoLowerBound.subtract(inputGeoLowerBound)).divide(axisResolution, MathContext.DECIMAL64);
                        origin = inputGeoLowerBound;
                        
                        BigDecimal shiftedGridLowerDistance = this.shiftToRoundedNumber(gridLowerDistance);
                        lowerGeoOffSet = (shiftedGridLowerDistance.subtract(gridLowerDistance)).multiply(axisResolution);

                        currentGeoAxis.setLowerBound(currentGeoLowerBound.add(lowerGeoOffSet).toPlainString());
                    } else {
                        // In this case, ***input subset domain*** must be shifted by tan offset as the origin belongs to the coverage's domain
                        gridLowerDistance = (inputGeoLowerBound.subtract(currentGeoLowerBound)).divide(axisResolution.abs(), MathContext.DECIMAL64);     
                        origin = currentGeoLowerBound;
                        
                        BigDecimal shiftedGridLowerDistance = this.shiftToRoundedNumber(gridLowerDistance);
                        lowerGeoOffSet = (shiftedGridLowerDistance.subtract(gridLowerDistance)).multiply(axisResolution);

                        if (inputDimensionSubset instanceof TrimmingSubsetDimension) {
                            ((TrimmingSubsetDimension)inputDimensionSubset).setLowerBound(inputGeoLowerBound.add(lowerGeoOffSet).toPlainString());
                        } else {
                            ((SlicingSubsetDimension)inputDimensionSubset).setBound(inputGeoLowerBound.add(lowerGeoOffSet).toPlainString());
                        }

                        if (inputGeoAxis != null) {
                            inputGeoAxis.setLowerBound(inputGeoLowerBound.add(lowerGeoOffSet).toPlainString());
                        }
                    }
                    
                    if (!origin.equals(currentGeoLowerBound)) {
                         // In this case, ***coverage's domain*** must be shifted by an offset as the origin belongs to the input subset 
                        gridUpperDistance = (currentGeoUpperBound.subtract(origin)).divide(axisResolution, MathContext.DECIMAL64);
                        if (!BigDecimalUtil.integer(gridUpperDistance)) {
                            BigDecimal shiftedGridUpperDistance = this.shiftToRoundedNumber(gridUpperDistance);
                            upperGeoOffSet = (shiftedGridUpperDistance.subtract(gridUpperDistance)).multiply(axisResolution);
                            
                            currentGeoAxis.setUpperBound(currentGeoUpperBound.add(upperGeoOffSet).toPlainString());
                        }
                    } else {
                        // In this case, ***input subset domain*** must be shifted by tan offset as the origin belongs to the coverage's domain
                        gridUpperDistance = (inputGeoUpperBound.subtract(origin)).divide(axisResolution, MathContext.DECIMAL64);
                        BigDecimal shiftedGridUpperDistance = this.shiftToRoundedNumber(gridUpperDistance);
                        upperGeoOffSet = (shiftedGridUpperDistance.subtract(gridUpperDistance)).multiply(axisResolution);

                        if (inputDimensionSubset instanceof TrimmingSubsetDimension) {
                            ((TrimmingSubsetDimension)inputDimensionSubset).setUpperBound(inputGeoUpperBound.add(upperGeoOffSet).toPlainString());
                        }

                        if (inputGeoAxis != null) {
                            inputGeoAxis.setUpperBound(inputGeoUpperBound.add(upperGeoOffSet).toPlainString());
                        }
                    }
                } else {
                    // e.g: Lat Axis with nagative direction
                    
                    // NOTE: don't need to shift anything if origin from current imported coverage's geo domain is the same as from input geo domain
                    if (inputGeoUpperBound.compareTo(currentGeoUpperBound) == 0) {
                        continue;
                    }
                    
                    // First, find the new origin between input subset and current subet (for positive axis, lowerBound is origin)
                    if (inputGeoUpperBound.compareTo(currentGeoUpperBound) > 0) {
                        // In this case, ***coverage's domain*** must be shifted by an offset as the origin belongs to the input subset
                        gridUpperDistance = (currentGeoUpperBound.subtract(inputGeoUpperBound)).divide(axisResolution, MathContext.DECIMAL64);  
                        origin = inputGeoUpperBound;
                        
                        BigDecimal shiftedGridUpperDistance = this.shiftToRoundedNumber(gridUpperDistance);                                            
                        upperGeoOffSet = (shiftedGridUpperDistance.subtract(gridUpperDistance)).multiply(axisResolution);

                        currentGeoAxis.setUpperBound(currentGeoUpperBound.add(upperGeoOffSet).toPlainString());
                    } else {
                        // In this case, ***input subset domain*** must be shifted by tan offset as the origin belongs to the coverage's domain
                        gridUpperDistance = (inputGeoUpperBound.subtract(currentGeoUpperBound)).divide(axisResolution, MathContext.DECIMAL64);     
                        origin = currentGeoUpperBound;
                        
                        BigDecimal shiftedGridUpperDistance = this.shiftToRoundedNumber(gridUpperDistance);
                        upperGeoOffSet = (shiftedGridUpperDistance.subtract(gridUpperDistance)).multiply(axisResolution);

                        if (inputDimensionSubset instanceof TrimmingSubsetDimension) {
                            ((TrimmingSubsetDimension)inputDimensionSubset).setUpperBound(inputGeoUpperBound.add(upperGeoOffSet).toPlainString());
                        } else {
                            ((SlicingSubsetDimension)inputDimensionSubset).setBound(inputGeoUpperBound.add(upperGeoOffSet).toPlainString());
                        }

                        if (inputGeoAxis != null) {
                            inputGeoAxis.setUpperBound(inputGeoUpperBound.add(upperGeoOffSet).toPlainString());
                        }
                    }
                    
                    if (!origin.equals(currentGeoUpperBound)) {
                        // In this case, ***coverage's domain*** must be shifted by an offset as the origin belongs to the input subset
                        gridLowerDistance = (currentGeoLowerBound.subtract(origin)).divide(axisResolution, MathContext.DECIMAL64);
                        BigDecimal shiftedGridLowerDistance = this.shiftToRoundedNumber(gridLowerDistance);
                        lowerGeoOffSet = (shiftedGridLowerDistance.subtract(gridLowerDistance)).multiply(axisResolution);

                        currentGeoAxis.setLowerBound(currentGeoLowerBound.add(lowerGeoOffSet).toPlainString());
                    } else {
                        // In this case, ***input subset domain*** must be shifted by an offset as the origin belongs to the coverage's domain
                        gridLowerDistance = (inputGeoLowerBound.subtract(origin)).divide(axisResolution, MathContext.DECIMAL64);
                        BigDecimal shiftedGridLowerDistance = this.shiftToRoundedNumber(gridLowerDistance);
                        lowerGeoOffSet = (shiftedGridLowerDistance.subtract(gridLowerDistance)).multiply(axisResolution);

                        if (inputDimensionSubset instanceof TrimmingSubsetDimension) {
                            ((TrimmingSubsetDimension)inputDimensionSubset).setLowerBound(inputGeoLowerBound.add(lowerGeoOffSet).toPlainString());
                        }

                        if (inputGeoAxis != null) {
                            inputGeoAxis.setLowerBound(inputGeoLowerBound.add(lowerGeoOffSet).toPlainString());
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles the updating of the coefficients for the current coverage. It
     * looks at the subset list and, for the targeted irregular axes it computes
     * the coefficient offset (the coefficient corresponding to the lower bound
     * of the subset). It then takes the coefficients of the input coverage for
     * the axis (if they exist), adjusts them by the offset and persists them.
     * If the irregular axis is not contained in the list of axis of the input
     * coverage, a non-data bound update is done (updating a 3D coverage using
     * 2D coverage as input), case in which the offset coefficient (which is the
     * one corresponding to the added slice) is saved.
     *
     * @param currentCoverage the coverage object targeted by the update op.
     * @param dimensionSubsets the list of subsets indicating where the coverage
     * should be updated.
     * @param inputCoverage the coverage that replaces the above part in the
     * target coverage.
     * @throws PetascopeException
     */
    private Pair<String, Integer> handleSubsetCoefficients(UpdateCoverageRequest request, Coverage currentCoverage, List<AbstractSubsetDimension> dimensionSubsets,
            Coverage inputCoverage) throws PetascopeException, SecoreException {
        
        int expandedDimension = RasdamanUpdaterFactory.NO_EXPAND_DIMENSION;
        
        Pair<String, Integer> expandedAxisDimensionPair = new Pair<>("", expandedDimension);

        for (AbstractSubsetDimension dimensionSubset : dimensionSubsets) {
            String axisLabel = dimensionSubset.getDimensionName();
            // Only support GeneralGridCoverage noew
            Axis currentGeoAxis = ((GeneralGridCoverage) currentCoverage).getGeoAxisByName(axisLabel);
             
            // check if geo axis (domainElement) is irregular
            if (currentGeoAxis.getClass().equals(IrregularAxis.class)) {
                // update the collected coefficients from input subset to the current irregular axis's directPositions (coefficients list)
                IrregularAxis currentIrregularAxis = ((IrregularAxis) currentGeoAxis);

                // update coefficients of current irregular axis corresponding to the subsets
                // NOTE: input irregular geo axis might be null in case the slice is not data bound (e.g. updating a 3D coverage using a 2D slice)
                // Only support GeneralGridCoverage now
                Axis inputGeoAxis = ((GeneralGridCoverage) inputCoverage).getGeoAxisByName(axisLabel);

                // or also called directPositions
                // NOTE: this coefficient is normalized in inputCoverage but not with currentCoverage
                List<BigDecimal> unnormalizedCoefficients = new ArrayList<>();

                if (inputGeoAxis == null) {
                    // non data bound, coefficient is calculated from the input subset dimension (slicing) and first lower bound of irregular axis
                    unnormalizedCoefficients.add(BigDecimal.ZERO);
                } else {
                    // data bound, coefficients provided in the input coverage
                    // This axis should be irregular in inputCoverage as well
                    // NOTE: all the coefficients from input irregular axis were normalized in wcst_import
                    // with the lowerBound of current subset
                    // e.g: This axis contains 4 coefficients, then the values are 0, 3, 5, 8                    
                    IrregularAxis inputIrregularAxis = ((IrregularAxis) inputGeoAxis);
                    // unformalized: for 4 days (lowerBound: 2008-01-01, 2008-01-04, 2008-01-06, upperBound: 2008-01-09)
                    unnormalizedCoefficients.addAll(inputIrregularAxis.getDirectPositionsAsNumbers());
                }

                // NOTE: Coefficients of input axis are calculated with the input domain lowerBound which is not current domain lowerBound
                // so need to calculate the offset between the input axis lowerBound and current axis lowerBound first
                BigDecimal coefficientOffset = computeCoefficientOffset(currentIrregularAxis, dimensionSubset);
                for (BigDecimal unnormalizedCoefficient : unnormalizedCoefficients) {
                    // Normalized input coefficient with offset from current axis lowerBound
                    BigDecimal normalizedCoefficient = unnormalizedCoefficient.add(coefficientOffset);

                    // Check if this normalizedCoefficient is not in current axis list of directPositions then add it
                    boolean isInsitu = false;
                    CoefficientStatus coefficientStatus = currentIrregularAxis.validateCoefficient(isInsitu, normalizedCoefficient);

                    if (coefficientStatus == coefficientStatus.APPEND_TO_TOP) {
                        // add coefficient to top
                        currentIrregularAxis.getDirectPositions().add(normalizedCoefficient.toPlainString());
                    } else if (coefficientStatus == coefficientStatus.APPEND_TO_BOTTOM) {
                        // add coefficient to bottom
                        currentIrregularAxis.getDirectPositions().add(0, normalizedCoefficient.toPlainString());
                    }
                }
            }
        }
        
        return expandedAxisDimensionPair;
    }
    
    /**
     * Computes the offset in coefficients for a domain extensions of the
     * coverage.
     *
     * @param currentDom the current domain of the coverage
     * @param subset the subset with which the coverage is extended
     * @return the coefficient corresponding to the first slice of the subset
     * @throws PetascopeException
     */
    private BigDecimal computeCoefficientOffset(IrregularAxis currentIrregularAxis, AbstractSubsetDimension subset) throws PetascopeException, SecoreException {
        String point = "";
        boolean isTrimming = false;
        if (subset instanceof TrimmingSubsetDimension) {
            TrimmingSubsetDimension trimSubset = (TrimmingSubsetDimension) subset;
            point = trimSubset.getLowerBound();
            isTrimming = true;
        } else if (subset instanceof SlicingSubsetDimension) {
            point = ((SlicingSubsetDimension) subset).getBound();
        }

        // The value of coefficient which is calculated with resolution and lowerBound of irregular axis
        BigDecimal normalizedSlicePoint;
        // by default resolution is 1 for IrregularAxis
        BigDecimal resolution = currentIrregularAxis.getResolution();

        if (subset.isNumeric()) {
            normalizedSlicePoint = BigDecimalUtil.divide(new BigDecimal(point), resolution);
        } else {
            // datetime format which needs to be translated to number first
            String axisCRS = currentIrregularAxis.getSrsName();
            String axisUoM = currentIrregularAxis.getUomLabel();

            String datumOrigin = CrsUtil.getDatumOrigin(axisCRS);
            normalizedSlicePoint = TimeUtil.countOffsets(datumOrigin, point, axisUoM, resolution);
        }

        // The geo value of lowest directPositions
        // NOTE: need to normalize based on the first coverage slice (coefficient zero)
        BigDecimal coefficientZeroBoundNumber = currentIrregularAxis.getCoefficientZeroBoundNumber();
        
        if (isTrimming && coefficientZeroBoundNumber.equals(currentIrregularAxis.getUpperBoundNumber())) {
            // In case coverage was imported with reversed coefficients (e.g: 10000 7500 5000)
            coefficientZeroBoundNumber = currentIrregularAxis.getLowerBoundNumber();
        }
        BigDecimal normalizedDomMin = BigDecimalUtil.divide(coefficientZeroBoundNumber, resolution);

        // Coefficient is the normalized value with lowerBound
        // e.g: AnsiDate time is: 1601-01-01, and the coverage starts with first time slice in "2005-01-01"
        // then, this first time slice's coefficient is 0
        // next time slice is "2005-01-03", then the coefficient 2 (days)
        // next time slice is "2005-01-07", then the coefficient is 6 (days)
        // NOTE: each coefficient value maps to 1 grid pixel in the grid dimension of irregular axis
        BigDecimal coefficient = (normalizedSlicePoint.subtract(normalizedDomMin)).multiply(resolution);

        return coefficient;
    }

    /**
     * Computes the rasdaman domain that is affected by the update operation.
     *
     * @param currentCoverage the coverage targeted by the update operation.
     * @param subsets the list of subsets given as parameters to the update
     * operation.
     * @param pixelIndices the pixel indices corresponding to each subset.
     * @return the string representation of the rasdaman domain affected by the
     * update op.
     * @throws PetascopeException
     */
    private String getAffectedDomain(Coverage currentCoverage, List<AbstractSubsetDimension> subsets,
            Map<Integer, String> pixelIndices) throws PetascopeException {
        String affectedDomain = "";

        // Only support GeneralGridCoverage now
        List<IndexAxis> currentIndexAxes = ((GeneralGridCoverage) currentCoverage).getIndexAxes();

        if (!subsets.isEmpty()) {
            //construct the rasdaman domain starting from cellDomains and replace the values where subsets are given
            affectedDomain += RASQL_OPEN_SUBSETS;
            for (int i = 0; i < currentIndexAxes.size(); i++) {

                IndexAxis currentIndexAxis = ((GeneralGridCoverage) currentCoverage).getIndexAxisByOrder(i);

                //if a given subset for this cell domain is given, use that
                if (pixelIndices.containsKey(i)) {
                    affectedDomain += pixelIndices.get(i);
                } //otherwise, use the entire domain
                else {
                    affectedDomain += currentIndexAxis.getLowerBound();
                    //only add upper bound if greater than equal lower
                    if (currentIndexAxis.getUpperBound() > currentIndexAxis.getLowerBound()) {
                        affectedDomain += RASQL_BOUND_SEPARATION + currentIndexAxis.getUpperBound();
                    }
                }
                //add separator between dimensions if not at the last dimension
                if (i < currentIndexAxes.size() - 1) {
                    affectedDomain += ",";
                }
            }
            affectedDomain += RASQL_CLOSE_SUBSETS;
        }
        return affectedDomain;
    }

    /**
     * Computes the domain with which the array in the values clause must be
     * shifted.
     *
     * @param inputCoverage the coverage providing the cell values for
     * replacement.
     * @param currentCoverage the coverage where the update is being made.
     * @param pixelIndices the list of pixel indices corresponding to each
     * subset indicated in the request.
     * @return the string representation of the rasdaman domain with which the
     * array must be shifted.
     */
    private String getShiftDomain(Coverage inputCoverage, Coverage currentCoverage, Map<Integer, String> pixelIndices) {
        String shiftDomain = RASQL_OPEN_SUBSETS;
        List<AxisExtent> inputAxesExtent = inputCoverage.getEnvelope().getEnvelopeByAxis().getAxisExtents();
        for (int i = 0; i < inputAxesExtent.size(); i++) {
            String shift = "0";
            //get the axis extent
            AxisExtent inputAxisExtent = inputAxesExtent.get(i);
            //get the order of the axis with the same name from the target coverage
            String inputAxisLabel = inputAxisExtent.getAxisLabel();

            int correspondingAxisOrder = ((GeneralGridCoverage) currentCoverage).getGeoAxisOrderByName(inputAxisLabel);
            if (pixelIndices.containsKey(correspondingAxisOrder)) {
                //add the lower limit
                if (pixelIndices.get(correspondingAxisOrder).contains(RASQL_BOUND_SEPARATION)) {
                    shift = pixelIndices.get(correspondingAxisOrder).split(RASQL_BOUND_SEPARATION)[0];
                } else {
                    shift = pixelIndices.get(correspondingAxisOrder);
                }
            }
            shiftDomain += shift;
            if (i != inputCoverage.getNumberOfDimensions() - 1) {
                shiftDomain += ",";
            }
        }
        shiftDomain += RASQL_CLOSE_SUBSETS;
        return shiftDomain;
    }

    /**
     * Computes the grid pixel indices corresponding to each subset given as
     * parameter to the request.
     *
     * map: geo axis order -> grid domain for the input subset
     *
     * @param currentCoverage the coverage targeted by the update operation.
     * @param dimensionSubsets the list of subsets indicated in the update
     * coverage request.
     * @return map indicating the pixel indices for each dimension.
     */
    private TreeMap<Integer, String> getPixelIndicesByCoordinate(Coverage currentCoverage,
            List<AbstractSubsetDimension> dimensionSubsets) throws WCSException, PetascopeException, SecoreException {
        TreeMap<Integer, String> result = new TreeMap<>();

        for (AbstractSubsetDimension dimensionSubset : dimensionSubsets) {
            String low = "";
            String high = "";
            String inputAxisLabel = dimensionSubset.getDimensionName();

            if (dimensionSubset instanceof TrimmingSubsetDimension) {
                low = ((TrimmingSubsetDimension) dimensionSubset).getLowerBound();
                high = ((TrimmingSubsetDimension) dimensionSubset).getUpperBound();
            } else if (dimensionSubset instanceof SlicingSubsetDimension) {
                low = ((SlicingSubsetDimension) dimensionSubset).getBound();
                high = ((SlicingSubsetDimension) dimensionSubset).getBound();
            }

            ParsedSubset<String> parsedSubset = new ParsedSubset<>(low, high);
            // Only support GeneralGridCoverage now
            String crs = ((GeneralGridCoverage) currentCoverage).getGeoAxisByName(inputAxisLabel).getSrsName();
            // Calculate the geo domain to grid pixel domain
            // e.g: Lat(0:20) -> 0:10
            CrsComputerService crsComputer = new CrsComputerService(dimensionSubset.getDimensionName(), crs, parsedSubset, currentCoverage);
            ParsedSubset<Long> pixelIndices = crsComputer.getPixelIndices();

            String resultingDomain;
            if (dimensionSubset instanceof SlicingSubsetDimension) {
                resultingDomain = pixelIndices.getLowerLimit().toString();
            } else {
                resultingDomain = pixelIndices.getLowerLimit().toString() + RASQL_BOUND_SEPARATION + pixelIndices.getUpperLimit().toString();
            }

            // Only supports GeneralGridCoverage now
            // NOTE: the order of CRS could be different from the order of grid CRS (e.g: Lat, Long but in rasdaman it is stored as Long, Lat as row-major order)
            int correspondingAxisOrder = ((GeneralGridCoverage) currentCoverage).getIndexAxisByName(inputAxisLabel).getAxisOrder();
            result.put(correspondingAxisOrder, resultingDomain);
        }

        return result;
    }

    /**
     * Using the list of updated geo axes by list of subset dimensions to update
     * the list of axis extends from coverage's envelope
     *
     * @param currentCoverage
     */
    private void updateAxisExtents(Coverage currentCoverage) {
        // Only supports GeneralGridCoverage now
        List<GeoAxis> geoAxes = ((GeneralGridCoverage) currentCoverage).getGeoAxes();
        for (GeoAxis geoAxis : geoAxes) {
            String axisLabel = geoAxis.getAxisLabel();
            AxisExtent axisExtent = currentCoverage.getEnvelope().getEnvelopeByAxis().getAxisExtentByLabel(axisLabel);
            axisExtent.setLowerBound(geoAxis.getLowerBound());
            axisExtent.setUpperBound(geoAxis.getUpperBound());
        }
    }

    /**
     * Using the list of DimensionSubset from wcst_import request, update the
     * geo domain lowerBound, upperBound of current geo axes of persisted
     * coverage
     *
     */
    private void updateGeoDomains(Coverage currentCoverage, Coverage inputCoverage, List<AbstractSubsetDimension> dimensionSubsets)
            throws PetascopeException, SecoreException {

        // NOTE: subset_correction:true makes the bounds incorrect with the bounds from GML file of WCST_Import
        // so only use the geo bounds inside the GML file and if the axis does not belong to the GML (dataBound: false), then get the value from the subsetDimension.                
        for (AbstractSubsetDimension dimensionSubset : dimensionSubsets) {
            String low = "";
            String high = "";
            String inputAxisLabel = dimensionSubset.getDimensionName();

            // Only supports GeneralGridCoverage now
            GeoAxis currentGeoAxis = ((GeneralGridCoverage) currentCoverage).getGeoAxisByName(inputAxisLabel);
            GeoAxis inputGeoAxis = ((GeneralGridCoverage) inputCoverage).getGeoAxisByName(inputAxisLabel);

            if (inputGeoAxis == null) {
                // Axis does not belong in input coverage (dataBound: false, e.g: time value from file name)
                if (dimensionSubset instanceof TrimmingSubsetDimension) {
                    low = ((TrimmingSubsetDimension) dimensionSubset).getLowerBound();
                    high = ((TrimmingSubsetDimension) dimensionSubset).getUpperBound();
                } else if (dimensionSubset instanceof SlicingSubsetDimension) {
                    low = ((SlicingSubsetDimension) dimensionSubset).getBound();
                    high = ((SlicingSubsetDimension) dimensionSubset).getBound();
                }
            } else {
                // Axis belong in input coverage
                low = inputGeoAxis.getLowerBound();
                high = inputGeoAxis.getUpperBound();
            }

            BigDecimal inputLowerBound, inputUpperBound, currentLowerBound, currentUpperBound;
            
            currentLowerBound = currentGeoAxis.getLowerBoundNumber();
            currentUpperBound = currentGeoAxis.getUpperBoundNumber();

            // not datetime format
            if (dimensionSubset.isNumeric()) {
                inputLowerBound = new BigDecimal(low);
                inputUpperBound = new BigDecimal(high);

                // update when possible (currentMin > inputMin, currentMax < inputMax)                
                if (currentLowerBound.compareTo(inputLowerBound) > 0) {
                    currentGeoAxis.setLowerBound(inputLowerBound.toPlainString());
                }
                if (currentUpperBound.compareTo(inputUpperBound) < 0) {
                    currentGeoAxis.setUpperBound(inputUpperBound.toPlainString());
                }
            } else {
                // dateTime format, compares with offset from origin
                String axisCRS = currentGeoAxis.getSrsName();
                String datumOrigin = CrsUtil.getDatumOrigin(axisCRS);
                String uom = currentGeoAxis.getUomLabel();

                // By default resolution is 1 for TimeAxis
                inputLowerBound = TimeUtil.countOffsets(datumOrigin, low, uom, BigDecimal.ONE);
                inputUpperBound = TimeUtil.countOffsets(datumOrigin, high, uom, BigDecimal.ONE);

                // NOTE: If time axis is regular (e.g: dataBound is false as time from file name, then the origin is the value from the file name, but the bounds will act as another regular geo bounds)
                // InputLowerBound = InputUperBound when only 1 Time value is sent as slice
                if (!currentGeoAxis.isIrregular() && inputLowerBound.equals(inputUpperBound)) {
                    inputLowerBound = inputLowerBound.subtract(currentGeoAxis.getResolution().multiply(new BigDecimal("0.5")));
                    inputUpperBound = inputUpperBound.add(currentGeoAxis.getResolution().multiply(new BigDecimal("0.5")));
                }
                
                CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(axisCRS);

                // update when possible (currentMin > inputMin, currentMax < inputMax), but set it with dateTime format
                if (currentLowerBound.compareTo(inputLowerBound) > 0) {
                    String timeLowerBound = TimeUtil.valueToISODateTime(BigDecimal.ZERO, inputLowerBound, crsDefinition);
                    currentGeoAxis.setLowerBound(timeLowerBound);
                }
                if (currentUpperBound.compareTo(inputUpperBound) < 0) {
                    String timeUpperBound = TimeUtil.valueToISODateTime(BigDecimal.ZERO, inputUpperBound, crsDefinition);
                    currentGeoAxis.setUpperBound(timeUpperBound);
                }
            }
        }
    }

    /**
     * Using the geo axis Order -> grid domains map then parse it to update the
     * grid lowerBound, upperBound of current index axes of persisted coverage
     *
     * @param coverage
     * @param pixelIndices
     */
    private void updateGridDomains(Coverage currentCoverage, Map<Integer, String> pixelIndices, Pair<String, Integer> expandedAxisDimensionPair) {

        for (Map.Entry<Integer, String> entry : pixelIndices.entrySet()) {
            Long gridLowerBound = null, gridUpperBound = null;
            Integer geoAxisOrder = entry.getKey();
            // e.g: 0:20 or 5
            String[] gridDomains = entry.getValue().split(RASQL_BOUND_SEPARATION);
            if (gridDomains.length == 1) {
                gridLowerBound = new Long(gridDomains[0]);
                gridUpperBound = new Long(gridDomains[0]);
            } else {
                gridLowerBound = new Long(gridDomains[0]);
                gridUpperBound = new Long(gridDomains[1]);
            }

            IndexAxis currentIndexAxis = ((GeneralGridCoverage) currentCoverage).getIndexAxisByOrder(geoAxisOrder);
            // Update current grid domain lower bound, upper bound when necessary
            if (currentIndexAxis.getLowerBound() > gridLowerBound) {
                currentIndexAxis.setLowerBound(gridLowerBound);
            }
            if (currentIndexAxis.getUpperBound() < gridUpperBound) {
                currentIndexAxis.setUpperBound(gridUpperBound);
            }
            
            if (CrsUtil.axisLabelsMatch(currentIndexAxis.getAxisLabel(), expandedAxisDimensionPair.fst) 
               && expandedAxisDimensionPair.snd != RasdamanUpdaterFactory.NO_EXPAND_DIMENSION) {
                Long expandedUpperBound = currentIndexAxis.getUpperBound() + 1;
                currentIndexAxis.setUpperBound(expandedUpperBound);
            }
        }
    }

    /**
     * Gets the GML coverage representation from an update request.
     *
     * @param request the request object.
     * @return the GML coverage representation.
     * @throws WCSException
     */
    private String getGmlCoverageFromRequest(UpdateCoverageRequest request) throws PetascopeException {
        String gmlCoverage = "";
        if (request.getInputCoverage() != null) {
            gmlCoverage = request.getInputCoverage();
        } else if (request.getInputCoverageRef() != null) {
            gmlCoverage = RemoteCoverageUtil.getRemoteGMLCoverage(request.getInputCoverageRef());
        }
        return gmlCoverage;
    }

    /* Gets the array in the values clause to be used in a rasdaman update
     * query, when values are given as tuple list.
     *
     * @param coverage the coverage providing the values.
     * @param rangeSet the rangeSet element.
     * @return string representations of the values clause, as rasdaman array
     * constant.
     * @throws PetascopeException
     */
    private String getReplacementValuesFromTupleList(Coverage coverage, Element rangeSet, String pixelDataType) throws PetascopeException {
        Element dataBlock = GMLCIS10ParserService.parseDataBlock(rangeSet);
        String collectionName = coverage.getCoverageId();
        Pair<String, List<String>> collectionType = TypeResolverUtil.guessCollectionType(collectionName, coverage.getNumberOfBands(), coverage.getNumberOfDimensions(),
                coverage.getNilValues(), pixelDataType);

        // Only support GeneralGridCoverage now
        List<IndexAxis> indexAxes = ((GeneralGridCoverage) coverage).getIndexAxes();
        String values = GMLCIS10ParserService.parseGMLTupleList(dataBlock, indexAxes, collectionType.snd);

        return values;
    }

    /**
     * Gets the file to be used in the values clause of the rasdaman update
     * query, when the values are given as file ref.
     *
     * @param rangeSet the rangeSet element.
     * @return file containing values for update.
     * @throws IOException
     * @throws WCSException
     */
    private File getReplacementValuesFromFile(Element rangeSet) throws IOException, WCSException, PetascopeException {
        //tuple list given as file
        String fileUrl = GMLCIS10ParserService.parseFilePath(rangeSet);
        //save in a temporary file to pass to gdal and rasdaman
        File tmpFile = RemoteCoverageUtil.copyFileLocally(fileUrl);

        return tmpFile;
    }
    
    
    /**
     * Get the bytes array of input file to be used to update a rasdaman collection
     */
    private byte[] getReplacementValuesFromFileAsBytes(Element rangeSet) throws IOException, WCSException, PetascopeException {
        //tuple list given as file
        String fileUrl = GMLCIS10ParserService.parseFilePath(rangeSet);
        //save in a temporary file to pass to gdal and rasdaman
        byte[] bytes = this.remoteCoverageUtil.getBytesFromRemoteFile(fileUrl);

        return bytes;
    }
}
