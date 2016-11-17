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
package petascope.wcs2.handlers.wcst;

import petascope.exceptions.rasdaman.RasdamanException;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.*;
import petascope.exceptions.wcst.WCSTInvalidUpdateOnIrregularAxisException;
import petascope.util.*;
import petascope.util.ras.TypeResolverUtil;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.CoverageInfo;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.util.CrsComputer;
import petascope.wcs2.handlers.AbstractRequestHandler;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.handlers.wcst.helpers.decodeparameters.RangeParametersConvertor;
import petascope.wcs2.handlers.wcst.helpers.decodeparameters.RangeParametersConvertorFactory;
import petascope.wcs2.handlers.wcst.helpers.update.RasdamanUpdater;
import petascope.wcs2.handlers.wcst.helpers.update.RasdamanUpdaterFactory;
import petascope.wcs2.handlers.wcst.helpers.validator.SubsetValidator;
import petascope.wcs2.handlers.wcst.helpers.validator.UpdateCoverageValidator;
import petascope.wcs2.helpers.wcst.RemoteCoverageUtil;
import petascope.wcs2.parsers.subsets.DimensionSlice;
import petascope.wcs2.parsers.subsets.DimensionSubset;
import petascope.wcs2.parsers.subsets.DimensionTrim;
import petascope.wcs2.parsers.wcst.UpdateCoverageRequest;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import petascope.exceptions.wcst.WCSTCoverageNotFound;
import petascope.exceptions.wcst.WCSTInvalidXML;
import petascope.wcps2.metadata.legacy.Coverage;
import petascope.wcps2.metadata.legacy.CoverageRegistry;
import petascope.wcps2.metadata.model.ParsedSubset;


public class UpdateCoverageHandler extends AbstractRequestHandler<UpdateCoverageRequest> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UpdateCoverageHandler.class);

    private final RasdamanUpdaterFactory rasdamanUpdaterFactory;
    private final RangeParametersConvertorFactory parametersConvertorFactory;
    private final SubsetValidator subsetValidator;

    public UpdateCoverageHandler(DbMetadataSource meta) {
        super(meta);
        rasdamanUpdaterFactory = new RasdamanUpdaterFactory();
        parametersConvertorFactory = new RangeParametersConvertorFactory();
        subsetValidator = new SubsetValidator();
    }

    /**
     * Handles the update of an existing WCS coverage.
     *
     * @param request the update coverage request.
     * @return empty response.
     * @throws petascope.exceptions.wcst.WCSTCoverageNotFound
     * @throws petascope.exceptions.wcst.WCSTInvalidXML
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     */
    @Override
    public Response handle(UpdateCoverageRequest request)
            throws WCSTCoverageNotFound, WCSTInvalidXML, PetascopeException, SecoreException {
        log.info("Handling coverage update...");
        CoverageMetadata currentCoverage = this.meta.read(request.getCoverageId());
        String affectedCollectionName = getCurrentCollectionName(currentCoverage);
        String affectedCollectionOid = getCurrentCollectionOid(currentCoverage);

        CoverageMetadata inputCoverage;
        String gmlInputCoverage = getGmlCoverageFromRequest(request);

        try {
            Document xmlInputCoverage = XMLUtil.buildDocument(null, gmlInputCoverage);
            inputCoverage = CoverageMetadata.fromGML(xmlInputCoverage);
            //validation
            UpdateCoverageValidator validator = new UpdateCoverageValidator(currentCoverage, inputCoverage,
                                                                            request.getSubsets(), request.getRangeComponent());
            validator.validate();

            //handle subset coefficients if necessary
            //the transaction is not commited unless everything goes well.
            handleSubsetCoefficients(currentCoverage, request.getSubsets(), inputCoverage);

            //handle cell values
            Element rangeSet = GMLParserUtil.parseRangeSet(xmlInputCoverage.getRootElement());

            Map<Integer, String> pixelIndices = getPixelIndicesByCoordinate(currentCoverage, request.getSubsets());
            String affectedDomain = getAffectedDomain(currentCoverage, request.getSubsets(), pixelIndices);                        
            subsetValidator.validate(inputCoverage.getCellDomainList(), affectedDomain);
            String shiftDomain = getShiftDomain(inputCoverage, currentCoverage, pixelIndices);
            RasdamanUpdater updater;

            if (rangeSet.getChildElements(XMLSymbols.LABEL_DATABLOCK,
                    XMLSymbols.NAMESPACE_GML).size() != 0) {
                //tuple list given explicitly
                String values = getReplacementValuesFromTupleList(currentCoverage, rangeSet, request.getPixelDataType());
                updater = rasdamanUpdaterFactory.getUpdater(affectedCollectionName, affectedCollectionOid, affectedDomain, values, shiftDomain);
                try {
                    updater.update();
                } catch (RasdamanException e) {
                    this.meta.abortAndClose();
                    throw e;
                }
            } else {
                //tuple list given as file
                //retrieve the file, if needed
                boolean isLocal = false;
                File valuesFile;
                String fileUrl = GMLParserUtil.parseFilePath(rangeSet);
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
                String mimetype = GMLParserUtil.parseMimeType(rangeSet);
                String rangeParameters = GMLParserUtil.parseRangeParameters(rangeSet);

                //process the range parameters
                RangeParametersConvertor convertor = parametersConvertorFactory.getConvertor(mimetype, rangeParameters, currentCoverage, meta);
                String decodeParameters = convertor.toRasdamanDecodeParameters();


                updater = rasdamanUpdaterFactory.getUpdater(affectedCollectionName, affectedCollectionOid,
                        affectedDomain, valuesFile, mimetype, shiftDomain, decodeParameters);
                try {
                    updater.update();
                } catch (RasdamanException e) {
                    this.meta.abortAndClose();
                    throw e;
                }

                //delete the file
                if (!isLocal) {
                    valuesFile.delete();
                }
            }

            //All went well, commit the metadata transaction, if there were any metadata changes.
            this.meta.commitAndClose();
        } catch (IOException e) {
            Logger.getLogger(UpdateCoverageHandler.class.getName()).log(Level.SEVERE, null, e);
            abortTransaction();
            throw new WCSTCoverageNotFound();
        } catch (ParsingException e) {
            Logger.getLogger(UpdateCoverageHandler.class.getName()).log(Level.SEVERE, null, e);
            abortTransaction();
            throw new WCSTInvalidXML(e.getMessage());
        } catch (SQLException e) {
            Logger.getLogger(UpdateCoverageHandler.class.getName()).log(Level.SEVERE, null, e);
            abortTransaction();
            throw new PetascopeException(ExceptionCode.InternalSqlError);
        } catch (PetascopeException e) {
            abortTransaction();
            throw e;
        }

        return new Response(new String[]{""});
    }

    /**
     * Handles the updating of the coefficients for the current coverage. It looks at the subset list and, for the
     * targeted irregular axes it computes the coefficient offset (the coefficient corresponding to the lower bound of
     * the subset). It then takes the coefficients of the input coverage for the axis (if they exist), adjusts them by
     * the offset and persists them. If the irregular axis is not contained in the list of axis of the input coverage,
     * a non-data bound update is done (updating a 3D coverage using  2D coverage as input), case in which the offset
     * coefficient (which is the one corresponding to the added slice) is saved.
     * @param currentCoverageMetadata the coverage object targeted by the update op.
     * @param subsets the list of subsets indicating where the coverage should be updated.
     * @param inputCoverage the coverage that replaces the above part in the target coverage.
     * @throws PetascopeException
     */
    private void handleSubsetCoefficients(CoverageMetadata currentCoverageMetadata, List<DimensionSubset> subsets,
                                          CoverageMetadata inputCoverage) throws PetascopeException {
        for (DimensionSubset subset : subsets) {
            //check if axis is regular or irregular
            DomainElement currentDom = currentCoverageMetadata.getDomainByName(subset.getDimension());
            if (currentDom.isIrregular()) {
                try {
                    //update coefficients corresponding to the subsets
                    int axisId;
                    axisId = meta.getGridAxisId(currentCoverageMetadata.getCoverageId(), currentDom.getOrder());
                    //inputDom might be null in case the slice is not data bound (e.g. updating a timeslice using a 2D cov)
                    DomainElement inputDom = inputCoverage.getDomainByName(subset.getDimension());
                    List<BigDecimal> coefficientList = new ArrayList<BigDecimal>();
                    if (inputDom == null) {
                        //non data bound, coefficient is 0 for the new slice
                        coefficientList.add(BigDecimal.ZERO);
                    } else {
                        //data bound, coefficients provided in the input coverage
                        coefficientList.addAll(inputDom.getCoefficients());
                    }
                    //update the collected coefficients
                    BigDecimal coefficientOffset = computeCoefficientOffset(currentDom, subset);
                    for (BigDecimal coefficient : coefficientList) {
                        BigDecimal translatedCoefficient = coefficient.add(coefficientOffset);
                        int coefficientOrder = computeCoefficientOrder(currentCoverageMetadata, currentDom, translatedCoefficient, subset);
                        meta.updateAxisCoefficient(axisId, translatedCoefficient, coefficientOrder);
                    }
                } catch (SQLException e) {
                    log.error(e.getMessage());
                    throw new PetascopeException(ExceptionCode.InternalSqlError);
                }
            }
        }
    }

    /**
     * Computes the order of a coefficient and increases the domain of the coverage accordingly.
     * @param currentCoverageMetadata the coverage object targeted by the update operation.
     * @param currentDomain the updated domain element.
     * @param coefficient the value of the coefficient corresponding to the slice to be added.
     * @param subset the place in the target coverage where the slices are added.
     * @return the total number of slices that the coverage contains on this axes, after the coefficient is added.
     * @throws PetascopeException
     */
    private int computeCoefficientOrder(CoverageMetadata currentCoverageMetadata, DomainElement currentDomain, BigDecimal coefficient, DimensionSubset subset) throws PetascopeException {
        List<BigDecimal> allCoefficients = meta.getAllCoefficients(currentCoverageMetadata.getCoverageName(), currentDomain.getOrder());
        int currentPosition = BigDecimalUtil.listContains(allCoefficients, coefficient);
        //for now, we only support adding slices on top or updating existing slices
        if (currentPosition == -1 && (coefficient.compareTo(allCoefficients.get(allCoefficients.size() - 1)) == -1)) {
            //it means that the coefficient is not in the list and is smaller than the largest one
            //this would mean adding a slice in between 2 other slices, not supported
            throw new WCSTInvalidUpdateOnIrregularAxisException();
        } else if (currentPosition != -1) {
            //coefficient already exist
            return currentPosition;
        } else {
            //increase the cell domain of the coverage by 1
            int currentDomainHi = currentCoverageMetadata.getCellDomainByOrder(currentDomain.getOrder()).getHiInt();
            currentDomainHi++;
            currentCoverageMetadata.getCellDomain(currentDomain.getOrder()).setHi(String.valueOf(currentDomainHi));
            //increase the pixel domain
            if (subset.isNumeric()) {
                if (subset instanceof DimensionSlice) {
                    currentDomain.setMaxValue(new BigDecimal(((DimensionSlice) subset).getSlicePoint()));
                } else {
                    currentDomain.setMaxValue(new BigDecimal(((DimensionTrim) subset).getTrimHigh()));
                }
            }
            //coefficient is corresponding to a new slice, added on top
            return allCoefficients.size();
        }
    }

    /**
     * Computes the offset in coefficients for a domain extensions of the coverage.
     * @param currentDom the current domain of the coverage
     * @param subset the subset with which the coverage is extended
     * @return the coefficient corresponding to the first slice of the subset
     * @throws PetascopeException
     */
    private BigDecimal computeCoefficientOffset(DomainElement currentDom, DimensionSubset subset) throws PetascopeException {
        String point = "";
        if (subset instanceof DimensionTrim) {
            DimensionTrim trimSubset = (DimensionTrim) subset;
                point = trimSubset.getTrimLow();
        } else if (subset instanceof DimensionSlice) {
            point = ((DimensionSlice) subset).getSlicePoint();
        }
        BigDecimal normalizedSlicePoint;
        if (subset.isNumeric()) {
            normalizedSlicePoint = BigDecimalUtil.divide(new BigDecimal(point), currentDom.getScalarResolution());
        } else {
            //time
            String datumOrigin = currentDom.getCrsDef().getDatumOrigin();
            String axisUoM = currentDom.getUom();
            normalizedSlicePoint = new BigDecimal(TimeUtil.countOffsets(datumOrigin, point, axisUoM, currentDom.getScalarResolution().doubleValue()));
        }
        BigDecimal normalizedDomMin = BigDecimalUtil.divide(currentDom.getMinValue(), currentDom.getScalarResolution());
        BigDecimal coefficient = (normalizedSlicePoint.subtract(normalizedDomMin)).multiply(currentDom.getDirectionalResolution());
        return coefficient;
    }

    /**
     * Computes the rasdaman domain that is affected by the update operation.
     *
     * @param currentCoverage the coverage targeted by the update operation.
     * @param subsets         the list of subsets given as parameters to the update operation.
     * @param pixelIndices    the pixel indices corresponding to each subset.
     * @return the string representation of the rasdaman domain affected by the update op.
     * @throws PetascopeException
     */
    private String getAffectedDomain(CoverageMetadata currentCoverage, List<DimensionSubset> subsets, Map<Integer, String> pixelIndices) throws PetascopeException {
        String ret = "";
        if (!subsets.isEmpty()) {
            //construct the rasdaman domain starting from cellDomains and replace the values where subsets are given
            ret += "[";
            for (int i = 0; i < currentCoverage.getDimension(); i++) {
                CellDomainElement currentCellDomain = currentCoverage.getCellDomainByOrder(i);
                //if a given subset for this cell domain iss give, use that
                if (pixelIndices.containsKey(i)) {
                    ret += pixelIndices.get(i);
                }
                //otherwise, use the entire domain
                else {
                    ret += currentCellDomain.getLo();
                    //only add upper bound if not equal lower
                    if (currentCellDomain.getHiInt() != currentCellDomain.getLoInt()) {
                        ret += ":" + currentCellDomain.getHi();
                    }
                }
                //add separator between dimensions if not at the last dimension
                if (i < currentCoverage.getDimension() - 1) {
                    ret += ",";
                }
            }
            ret += "]";
        }
        return ret;
    }

    /**
 * Computes the domain with which the array in the values clause must be shifted.
 *
 * @param inputCoverage the coverage providing the cell values for replacement.
 * @param targetCoverage the coverage where the update is being made.
 * @param pixelIndices  the list of pixel indices corresponding to each subset indicated in the request.
 * @return the string representation of the rasdaman domain with which the array must be shifted.
 */
private String getShiftDomain(CoverageMetadata inputCoverage, CoverageMetadata targetCoverage, Map<Integer, String> pixelIndices) {
    String shiftDomain = "[";
    for (int i = 0; i < inputCoverage.getDimension(); i++) {
        String shift = "0";
        //get the axis
        DomainElement inputCoverageAxis = inputCoverage.getDomainList().get(i);
        //get the order of the axis with the same name from the target coverage
        int correspondingAxisOrder = targetCoverage.getDomainIndexByName(inputCoverageAxis.getLabel());
        if (pixelIndices.containsKey(correspondingAxisOrder)) {
            //add the lower limit
            if (pixelIndices.get(correspondingAxisOrder).contains(":")) {
                shift = pixelIndices.get(correspondingAxisOrder).split(":")[0];
            } else {
                shift = pixelIndices.get(correspondingAxisOrder);
            }
        }
        shiftDomain += shift.toString();
        if (i != inputCoverage.getDimension() - 1) {
            shiftDomain += ",";
        }
    }
    shiftDomain += "]";
    return shiftDomain;
}

    /**
     * Computes the pixel indices corresponding to each subset given as parameter to the request.
     *
     * @param currentCoverage the coverage targeted by the update operation.
     * @param subsets         the list of subsets indicated in the update coverage request.
     * @return map indicating the pixel indices for each dimension.
     */
    private Map<Integer, String> getPixelIndicesByCoordinate(CoverageMetadata currentCoverage, List<DimensionSubset> subsets) throws WCSException {
        CoverageInfo coverageInfo = new CoverageInfo(currentCoverage);
        Coverage currentWcpsCoverage = new Coverage(currentCoverage.getCoverageName(), coverageInfo, currentCoverage);
        CoverageRegistry coverageRegistry = new CoverageRegistry(meta);
        Map<Integer, String> result = new HashMap<Integer, String>();
        for (DimensionSubset i : subsets) {
            String low = "";
            String high = "";
            if (i instanceof DimensionTrim) {
                low = ((DimensionTrim) i).getTrimLow();
                high = ((DimensionTrim) i).getTrimHigh();
            } else if (i instanceof DimensionSlice) {
                low = ((DimensionSlice) i).getSlicePoint();
                high = ((DimensionSlice) i).getSlicePoint();
            }
            ParsedSubset<String> currentInterval = new ParsedSubset<String>(low, high);
            String crs = i.getCrs() != null ? i.getCrs() : currentWcpsCoverage.getCoverageMetadata().getDomainByName(i.getDimension()).getNativeCrs();
            CrsComputer crsComputer = new CrsComputer(i.getDimension(), crs, currentInterval, currentWcpsCoverage, coverageRegistry);
            ParsedSubset<Long> pixelIndices = crsComputer.getPixelIndices(true);
            String resultingDomain;
            if (i instanceof DimensionSlice) {
                resultingDomain = pixelIndices.getLowerLimit().toString();
            } else {
                resultingDomain = pixelIndices.getLowerLimit().toString() + ":" + pixelIndices.getUpperLimit().toString();
            }
            result.put(currentWcpsCoverage.getCoverageMetadata().getDomainIndexByName(i.getDimension()), resultingDomain);
        }
        return result;
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

    /**
     * Gets the rasdaman collection name for the coverage targeted by the update operation.
     *
     * @param currentCoverage the coverage targeted by the update operation.
     * @return rasdaman collection name.
     */
    private String getCurrentCollectionName(CoverageMetadata currentCoverage) {
        return currentCoverage.getRasdamanCollection().snd;
    }

    /**
     * Gets the oid of the array corresponding to the coverage targeted by the update operation.
     *
     * @param currentCoverage the coverage targeted by the update operation.
     * @return the string representation of the oid.
     */
    private String getCurrentCollectionOid(CoverageMetadata currentCoverage) {
        return currentCoverage.getRasdamanCollection().fst.toString();
    }

    /**
     * Gets the array in the values clause to be used in a rasdaman update query, when values are given as tuple list.
     *
     * @param coverage the coverage providing the values.
     * @param rangeSet the rangeSet element.
     * @return string representations of the values clause, as rasdaman array constant.
     * @throws PetascopeException
     */
    private String getReplacementValuesFromTupleList(CoverageMetadata coverage, Element rangeSet, String pixelDataType) throws PetascopeException {
        Element dataBlock = GMLParserUtil.parseDataBlock(rangeSet);
        Pair<String, String> collectionType = TypeResolverUtil.guessCollectionType(coverage.getNumberOfBands(), coverage.getDimension(), coverage.getAllUniqueNullValues(), pixelDataType);
        String values = GMLParserUtil.parseGMLTupleList(dataBlock, coverage.getCellDomainList(), collectionType.getValue());
        return values;
    }

    /**
     * Gets the file to be used in the values clause of the rasdaman update query, when the values are given as file ref.
     *
     * @param rangeSet the rangeSet element.
     * @return file containing values for update.
     * @throws IOException
     * @throws WCSException
     */
    private File getReplacementValuesFromFile(Element rangeSet) throws IOException, WCSException {
        //tuple list given as file
        String fileUrl = GMLParserUtil.parseFilePath(rangeSet);
        //save in a temporary file to pass to gdal and rasdaman
        File tmpFile = RemoteCoverageUtil.copyFileLocally(fileUrl);
        return tmpFile;
    }

    private void abortTransaction() throws PetascopeException {
        try {
            this.meta.abortAndClose();
        } catch (SQLException e) {
            Logger.getLogger(UpdateCoverageHandler.class.getName()).log(Level.SEVERE, null, e);
            throw new PetascopeException(ExceptionCode.InternalSqlError);
        }
    }
}
