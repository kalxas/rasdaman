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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
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

import petascope.core.Pair;
import petascope.core.XMLSymbols;
import petascope.core.gml.GMLParserService;
import petascope.util.CrsUtil;
import petascope.exceptions.WCSException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Elements;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.Axis;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.gml.GeneralGridCoverageGMLService;

import petascope.wcst.exceptions.WCSTCoverageParameterNotFound;
import petascope.wcst.exceptions.WCSTInvalidXML;
import petascope.wcps.metadata.model.ParsedSubset;

@Service
public class UpdateCoverageHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UpdateCoverageHandler.class);

    @Autowired
    private CoverageRepostioryService persistedCoverageService;
    @Autowired
    private GeneralGridCoverageGMLService generalGridCoverageGMLService;
    @Autowired
    private RasdamanUpdaterFactory rasdamanUpdaterFactory;
    @Autowired
    private RangeParametersConvertorFactory rangeParametersConvertorFactory;
    @Autowired
    private GridDomainsValidator gridDomainsValidator;

    /**
     * Handles the update of an existing WCS coverage.
     *
     * @param request the update coverage request.
     * @return empty response.
     * @throws petascope.wcst.exceptions.WCSTCoverageParameterNotFound
     * @throws petascope.wcst.exceptions.WCSTInvalidXML
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     */
    public Response handle(UpdateCoverageRequest request)
            throws WCSTCoverageParameterNotFound, WCSTInvalidXML, PetascopeException, SecoreException {
        log.debug("Handling coverage update...");
        // persisted coverage
        Coverage currentCoverage = persistedCoverageService.readCoverageByIdFromDatabase(request.getCoverageId());
        String affectedCollectionOid = currentCoverage.getRasdamanRangeSet().getOid().toString();
        String affectedCollectionName = currentCoverage.getRasdamanRangeSet().getCollectionName();

        String gmlInputCoverage = getGmlCoverageFromRequest(request);
        List<AbstractSubsetDimension> dimensionSubsets = request.getSubsets();

        try {
            Document gmlInputCoverageDocument = XMLUtil.buildDocument(null, gmlInputCoverage);
            // Build input Coverage object from GML document (each slice is a input coverage) to update the persisted coverage
            Coverage inputCoverage = generalGridCoverageGMLService.buildCoverage(gmlInputCoverageDocument);
            //validation
            UpdateCoverageValidator updateCoverageValidator = new UpdateCoverageValidator(currentCoverage, inputCoverage,
                    dimensionSubsets, request.getRangeComponent());
            updateCoverageValidator.validate();

            //handle subset coefficients if necessary for coverage with irregular axis
            handleSubsetCoefficients(currentCoverage, dimensionSubsets, inputCoverage);

            //handle cell values
            Element rangeSet = GMLParserService.parseRangeSet(gmlInputCoverageDocument.getRootElement());

            Map<Integer, String> pixelIndices = getPixelIndicesByCoordinate(currentCoverage, dimensionSubsets);
            String affectedDomain = getAffectedDomain(currentCoverage, dimensionSubsets, pixelIndices);

            // Only support GeneralGridCoverage now
            List<IndexAxis> inputIndexAxes = ((GeneralGridCoverage) inputCoverage).getIndexAxes();

            // NOTE: need to validate the grid domains from input coverage slice 
            // and the output grid domains which is calculated from the subsets with exising coverage
            gridDomainsValidator.validate(inputIndexAxes, affectedDomain);

            String shiftDomain = getShiftDomain(inputCoverage, currentCoverage, pixelIndices);
            RasdamanUpdater updater;

            Elements dataBlockElements = rangeSet.getChildElements(XMLSymbols.LABEL_DATABLOCK,
                    XMLSymbols.NAMESPACE_GML);
            if (dataBlockElements.size() != 0) {
                //tuple list given explicitly
                String values = getReplacementValuesFromTupleList(currentCoverage, rangeSet, request.getPixelDataType());
                updater = rasdamanUpdaterFactory.getUpdater(affectedCollectionName, affectedCollectionOid, affectedDomain, values, shiftDomain);                
                updater.update();                
            } else {
                //tuple list given as file
                //retrieve the file, if needed
                boolean isLocal = false;
                File valuesFile;
                String fileUrl = GMLParserService.parseFilePath(rangeSet);
                if (fileUrl.startsWith("file://")) {
                    fileUrl = fileUrl.replace("file://", "");
                }
                if (fileUrl.startsWith("/")) {
                    isLocal = true;
                    valuesFile = new File(fileUrl);
                } else {
                    //remote file, get it
                    valuesFile = getReplacementValuesFromFile(rangeSet);
                }
                String mimetype = GMLParserService.parseMimeType(rangeSet);
                // e.g: netCDF test_eobstest: "{"variables": ["tg"]}",
                String rangeParameters = GMLParserService.parseRangeParameters(rangeSet);

                //process the range parameters
                RangeParametersConvertor convertor = rangeParametersConvertorFactory.getConvertor(mimetype, rangeParameters, currentCoverage);
                String decodeParameters = convertor.toRasdamanDecodeParameters();

                updater = rasdamanUpdaterFactory.getUpdater(affectedCollectionName, affectedCollectionOid,
                        affectedDomain, valuesFile, mimetype, shiftDomain,
                        decodeParameters);                
                updater.update();                

                //delete the file
                if (!isLocal) {
                    valuesFile.delete();
                }
            }

            // After updating rasdaman collection, we need to update current coverage with new geo, grid domains
            updateGeoDomains(currentCoverage, inputCoverage, dimensionSubsets);
            updateAxisExtents(currentCoverage);
            updateGridDomains(currentCoverage, pixelIndices);
            // Now, we can persist the updated current coverage from input slice
            persistedCoverageService.save(currentCoverage);
        } catch (IOException e) {
            Logger.getLogger(UpdateCoverageHandler.class.getName()).log(Level.SEVERE, null, e);
            throw new WCSTCoverageParameterNotFound();
        } catch (ParsingException e) {
            Logger.getLogger(UpdateCoverageHandler.class.getName()).log(Level.SEVERE, null, e);
            throw new WCSTInvalidXML(e.getMessage());
        } catch (PetascopeException e) {
            throw e;
        }

        return new Response();
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
    private void handleSubsetCoefficients(Coverage currentCoverage, List<AbstractSubsetDimension> dimensionSubsets,
            Coverage inputCoverage) throws PetascopeException, SecoreException {

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
                    unnormalizedCoefficients.addAll(inputIrregularAxis.getDirectPositionsNumber());
                }

                // NOTE: Coefficients of input axis are calculated with the input domain lowerBound which is not current domain lowerBound
                // so need to calculate the offset between the input axis lowerBound and current axis lowerBound first
                BigDecimal coefficientOffset = computeCoefficientOffset(currentIrregularAxis, dimensionSubset);
                for (BigDecimal unnormalizedCoefficient : unnormalizedCoefficients) {
                    // Normalized input coefficient with offset from current axis lowerBound
                    BigDecimal normalizedCoefficient = unnormalizedCoefficient.add(coefficientOffset);

                    // Check if this normalizedCoefficient is not in current axis list of directPositions then add it
                    boolean isExist = currentIrregularAxis.validateCoefficient(normalizedCoefficient);

                    // Coefficient does not exist in the list of direct positions and it > highest coefficient, so add it into the list
                    // NOTE: no support to add new coefficient betweens coefficients (e.g: 0 2 3 and add 1)
                    if (!isExist) {
                        currentIrregularAxis.getDirectPositions().add(normalizedCoefficient.toPlainString());
                    }
                }
            }
        }
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
        if (subset instanceof TrimmingSubsetDimension) {
            TrimmingSubsetDimension trimSubset = (TrimmingSubsetDimension) subset;
            point = trimSubset.getLowerBound();
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

        // The geo value of lowest directPositions (it is not the lowest coefficient which is 0 in most cases)
        BigDecimal lowerBoundNumber = currentIrregularAxis.getLowerBoundNumber();
        BigDecimal normalizedDomMin = BigDecimalUtil.divide(lowerBoundNumber, resolution);

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
            affectedDomain += "[";
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
                        affectedDomain += ":" + currentIndexAxis.getUpperBound();
                    }
                }
                //add separator between dimensions if not at the last dimension
                if (i < currentIndexAxes.size() - 1) {
                    affectedDomain += ",";
                }
            }
            affectedDomain += "]";
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
        String shiftDomain = "[";
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
                if (pixelIndices.get(correspondingAxisOrder).contains(":")) {
                    shift = pixelIndices.get(correspondingAxisOrder).split(":")[0];
                } else {
                    shift = pixelIndices.get(correspondingAxisOrder);
                }
            }
            shiftDomain += shift;
            if (i != inputCoverage.getNumberOfDimensions() - 1) {
                shiftDomain += ",";
            }
        }
        shiftDomain += "]";
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
    private Map<Integer, String> getPixelIndicesByCoordinate(Coverage currentCoverage,
            List<AbstractSubsetDimension> dimensionSubsets) throws WCSException, PetascopeException, SecoreException {
        Map<Integer, String> result = new HashMap<>();

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

            Long gridLowerBound = pixelIndices.getLowerLimit();
            Long gridUpperBound = pixelIndices.getUpperLimit();

            // For irregular axis data bound is false (slicing)
            if (gridLowerBound == null) {
                gridLowerBound = gridUpperBound;
            }
            if (gridUpperBound == null) {
                gridUpperBound = gridLowerBound;
            }

            String resultingDomain;
            if (dimensionSubset instanceof SlicingSubsetDimension) {
                resultingDomain = gridLowerBound.toString();
            } else {
                resultingDomain = gridLowerBound.toString() + ":" + gridUpperBound.toString();
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

                // update when possible (currentMin > inputMin, currentMax < inputMax), but set it with dateTime format
                if (currentLowerBound.compareTo(inputLowerBound) > 0) {
                    currentGeoAxis.setLowerBound(inputLowerBound.toPlainString());
                }
                if (currentUpperBound.compareTo(inputUpperBound) < 0) {
                    currentGeoAxis.setUpperBound(inputUpperBound.toPlainString());
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
    private void updateGridDomains(Coverage currentCoverage, Map<Integer, String> pixelIndices) {

        for (Map.Entry<Integer, String> entry : pixelIndices.entrySet()) {
            Long gridLowerBound = null, gridUpperBound = null;
            Integer geoAxisOrder = entry.getKey();
            // e.g: 0:20 or 5
            String[] gridDomains = entry.getValue().split(":");
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
        }
    }

    /**
     * Gets the GML coverage representation from an update request.
     *
     * @param request the request object.
     * @return the GML coverage representation.
     * @throws WCSException
     */
    private String getGmlCoverageFromRequest(UpdateCoverageRequest request) throws WCSException {
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
        Element dataBlock = GMLParserService.parseDataBlock(rangeSet);
        String collectionName = coverage.getCoverageId().replace("-", "_");
        Pair<String, String> collectionType = TypeResolverUtil.guessCollectionType(collectionName, coverage.getNumberOfBands(), coverage.getNumberOfDimensions(),
                coverage.getAllUniqueNullValues(), pixelDataType);

        // Only support GeneralGridCoverage now
        List<IndexAxis> indexAxes = ((GeneralGridCoverage) coverage).getIndexAxes();
        String values = GMLParserService.parseGMLTupleList(dataBlock, indexAxes, collectionType.snd);

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
    private File getReplacementValuesFromFile(Element rangeSet) throws IOException, WCSException {
        //tuple list given as file
        String fileUrl = GMLParserService.parseFilePath(rangeSet);
        //save in a temporary file to pass to gdal and rasdaman
        File tmpFile = RemoteCoverageUtil.copyFileLocally(fileUrl);

        return tmpFile;
    }
}
