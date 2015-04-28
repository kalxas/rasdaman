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

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.*;
import petascope.exceptions.wcst.WCSTInvalidTrimSubsetOnIrregularAxisException;
import petascope.exceptions.wcst.WCSTInvalidUpdateOnIrregularAxisException;
import petascope.util.*;
import petascope.util.ras.RasUtil;
import petascope.util.ras.TypeResolverUtil;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.CoverageInfo;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.metadata.Coverage;
import petascope.wcps2.metadata.CoverageRegistry;
import petascope.wcps2.metadata.Interval;
import petascope.wcps2.util.CrsComputer;
import petascope.wcs2.handlers.AbstractRequestHandler;
import petascope.wcs2.handlers.Response;
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

public class UpdateCoverageHandler extends AbstractRequestHandler<UpdateCoverageRequest> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UpdateCoverageHandler.class);

    public UpdateCoverageHandler(DbMetadataSource meta) {
        super(meta);
    }

    /**
     * Handles the update of an existing WCS coverage.
     * @param request the update coverage request.
     * @return empty response.
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     */
    @Override
    public Response handle(UpdateCoverageRequest request) throws PetascopeException, WCSException, SecoreException {
        CoverageMetadata currentCoverage = this.meta.read(request.getCoverageId());
        String affectedCollectionName = getCurrentCollectionName(currentCoverage);
        String affectedCollectionOid = getCurrentCollectionOid(currentCoverage);

        CoverageMetadata inputCoverage = null;
        String gmlInputCoverage = getGmlCoverageFromRequest(request);

        try {
            Document xmlInputCoverage = XMLUtil.buildDocument(null, gmlInputCoverage);
            inputCoverage = CoverageMetadata.fromGML(xmlInputCoverage);
            //validation
            UpdateCoverageValidator validator = new UpdateCoverageValidator(currentCoverage, inputCoverage,
                    request.getSubsets(), request.getRangeComponent());
            validator.validate();

            //handle subset coefficients if necessary
            handleSubsetCoefficients(currentCoverage, request.getSubsets());

            //handle cell values
            Element rangeSet = GMLParserUtil.parseRangeSet(xmlInputCoverage.getRootElement());

            Map<Integer, Interval<Long>> pixelIndices = getPixelIndicesByCoordinate(currentCoverage, request.getSubsets());
            String affectedDomain = getAffectedDomain(currentCoverage, request.getSubsets(), pixelIndices);
            String shiftDomain = getShiftDomain(inputCoverage, pixelIndices);

            if (rangeSet.getChildElements(XMLSymbols.LABEL_DATABLOCK,
                    XMLSymbols.NAMESPACE_GML).size() != 0) {
                //tuple list given explicitly
                String values = getReplacementValuesFromTupleList(inputCoverage, rangeSet, request.getPixelDataType());
                handleUpdateWithValuesFromTupleList(affectedCollectionName, affectedCollectionOid, affectedDomain, values, shiftDomain);
            } else {
                //tuple list given as file
                //retrieve the file
                File valuesFile = getReplacementValuesFromFile(rangeSet);
                String mimetype = GMLParserUtil.parseMimeType(rangeSet);
                handleUpdateWithValuesFromFile(affectedCollectionName, affectedCollectionOid, affectedDomain, valuesFile, mimetype, shiftDomain);
                //delete the file
                valuesFile.delete();
            }
        } catch (IOException e) {
            Logger.getLogger(UpdateCoverageHandler.class.getName()).log(Level.SEVERE, null, e);
            throw new PetascopeException(ExceptionCode.WCSTCoverageNotFound);
        } catch (ParsingException e) {
            Logger.getLogger(InsertCoverageHandler.class.getName()).log(Level.SEVERE, null, e);
            throw new PetascopeException(ExceptionCode.WCSTInvalidXML, e.getMessage());
        }

        return new Response("");
    }

    /**
     * Handles the update when the values are given as tuple list.
     * @param affectedCollectionName the name of the rasdaman collection corresponding to the coverage.
     * @param affectedCollectionOid the oid of the rasdaman array corresponding to the coverage.
     * @param affectedDomain the rasdaman domain over which the update is executed.
     * @param values the values clause in the rasdaman update operation.
     * @param shiftDomain the domain with which the rasdaman array in the values clause must be shifted.
     * @throws RasdamanException
     */
    private void handleUpdateWithValuesFromTupleList(String affectedCollectionName, String affectedCollectionOid, String affectedDomain,
                                                     String values, String shiftDomain) throws RasdamanException {
        String queryString = UPDATE_TEMPLATE_VALUES.replace("$collection", affectedCollectionName)
                .replace("$domain", affectedDomain)
                .replace("$oid", affectedCollectionOid)
                .replace("$values", values)
                .replace("$shiftDomain", shiftDomain);
        RasUtil.executeRasqlQuery(queryString, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
    }

    /**
     * Handles the update when the values are given as file.
     * @param affectedCollectionName the name of the rasdaman collection corresponding to the coverage.
     * @param affectedCollectionOid the oid of the rasdaman array corresponding to the coverage.
     * @param affectedDomain the rasdaman domain over which the update is executed.
     * @param valuesFile the file where the cell values are stored.
     * @param mimetype the mime type of the file.
     * @param shiftDomain the domain with which the array stored in the file must be shifted.
     * @throws IOException
     * @throws RasdamanException
     */
    private void handleUpdateWithValuesFromFile(String affectedCollectionName, String affectedCollectionOid, String affectedDomain,
                                                File valuesFile, String mimetype, String shiftDomain) throws IOException, RasdamanException {
        String queryString = UPDATE_TEMPLATE_FILE.replace("$collection", affectedCollectionName)
                .replace("$domain", affectedDomain)
                .replace("$oid", affectedCollectionOid)
                .replace("$shiftDomain", shiftDomain);
        RasUtil.executeUpdateFileStatement(queryString, valuesFile.getAbsolutePath(), mimetype,
                ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS);
    }

    private void handleSubsetCoefficients(CoverageMetadata currentCoverageMetadata, List<DimensionSubset> subsets) throws PetascopeException {
        for(DimensionSubset subset : subsets){
            //check if axis is irregular is irregular
            if(currentCoverageMetadata.getDomainByName(subset.getDimension()).isIrregular()){
                //update coefficient corresponding to this slice
                DomainElement currentDom = currentCoverageMetadata.getDomainByName(subset.getDimension());
                int axisId;
                try {
                    axisId = meta.getGridAxisId(currentCoverageMetadata.getCoverageId(), currentDom.getOrder());
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new PetascopeException(ExceptionCode.InternalSqlError);
                }
                BigDecimal currentCoefficient = computeSubsetCoefficient(currentDom, subset);
                int coefficientOrder = computeCoefficientOrder(currentCoverageMetadata, currentDom, currentCoefficient, (DimensionSlice)subset);
                try {
                    meta.updateAxisCoefficient(axisId, currentCoefficient, coefficientOrder);
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new PetascopeException(ExceptionCode.InternalSqlError);
                }
            }
        }
    }

    private int computeCoefficientOrder(CoverageMetadata currentCoverageMetadata, DomainElement currentDomain, BigDecimal coefficient, DimensionSlice subset) throws PetascopeException {
        List<BigDecimal> allCoefficients = meta.getAllCoefficients(currentCoverageMetadata.getCoverageName(), currentDomain.getOrder());
        int currentPosition = BigDecimalUtil.listContains(allCoefficients, coefficient);
        //for now, we only support adding slices on top or updating existing slices
        if(currentPosition == -1 && (coefficient.compareTo(allCoefficients.get(allCoefficients.size() - 1)) == -1)){
            //it means that the coefficient is not in the list and is smaller than the largest one
            //this would mean adding a slice in between 2 other slices, not supported
            throw new WCSTInvalidUpdateOnIrregularAxisException();
        }
        else if (currentPosition != -1){
            //coefficient already exist
            return currentPosition;
        }
        else {
            //increase the cell domain of the coverage by 1
            int currentDomainHi = currentCoverageMetadata.getCellDomainByOrder(currentDomain.getOrder()).getHiInt();
            currentDomainHi++;
            currentCoverageMetadata.getCellDomain(currentDomain.getOrder()).setHi(String.valueOf(currentDomainHi));
            //increase the pixel domain
            if(subset.isNumeric()){
                currentDomain.setMaxValue(new BigDecimal(subset.getSlicePoint()));
            }
            //coefficient is corresponding to a new slice, added on top
            return allCoefficients.size();
        }
    }

    private BigDecimal computeSubsetCoefficient(DomainElement currentDom, DimensionSubset subset) throws PetascopeException {
        if(subset instanceof DimensionSlice){
            String point = ((DimensionSlice) subset).getSlicePoint();
            BigDecimal normalizedSlicePoint;
            if(subset.isNumeric()){
                normalizedSlicePoint = BigDecimalUtil.divide(new BigDecimal(point), currentDom.getScalarResolution());
            }
            else {
                //time
                String datumOrigin = currentDom.getCrsDef().getDatumOrigin();
                String axisUoM = currentDom.getUom();
                normalizedSlicePoint = new BigDecimal(TimeUtil.countOffsets(datumOrigin, point, axisUoM, currentDom.getScalarResolution().doubleValue()));
            }
            BigDecimal normalizedDomMin = BigDecimalUtil.divide(currentDom.getMinValue(), currentDom.getScalarResolution());
            BigDecimal coefficient = (normalizedSlicePoint.subtract(normalizedDomMin)).multiply(currentDom.getDirectionalResolution());
            return coefficient;
        } else {
            throw new WCSTInvalidTrimSubsetOnIrregularAxisException(subset.toString(), currentDom.getLabel());
        }

    }

    /**
     * Computes the rasdaman domain that is affected by the update operation.
     * @param currentCoverage the coverage targeted by the update operation.
     * @param subsets the list of subsets given as parameters to the update operation.
     * @param pixelIndices the pixel indices corresponding to each subset.
     * @return the string representation of the rasdaman domain affected by the update op.
     * @throws PetascopeException
     */
    private String getAffectedDomain(CoverageMetadata currentCoverage, List<DimensionSubset> subsets, Map<Integer, Interval<Long>> pixelIndices) throws PetascopeException {
        String ret = "";
        if (!subsets.isEmpty()) {
            //construct the rasdaman domain starting from cellDomains and replace the values where subsets are given
            ret += "[";
            for (int i = 0; i < currentCoverage.getDimension(); i++) {
                CellDomainElement currentCellDomain = currentCoverage.getCellDomainByOrder(i);
                //if a given subset for this cell domain iss give, use that
                if (pixelIndices.containsKey(i)) {
                    ret += pixelIndices.get(i).getLowerLimit().toString();
                    //only add upper bound if not equal lower
                    if(!pixelIndices.get(i).getUpperLimit().equals(pixelIndices.get(i).getLowerLimit())){
                        ret += ":" + pixelIndices.get(i).getUpperLimit().toString();
                    }
                }
                //otherwise, use the entire domain
                else {
                    ret += currentCellDomain.getLo();
                    //only add upper bound if not equal lower
                    if(currentCellDomain.getHiInt() != currentCellDomain.getLoInt()) {
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
     * @param inputCoverage the coverage providing the cell values for replacement.
     * @param pixelIndices the list of pixel indices corresponding to each subset indicated in the request.
     * @return the string representation of the rasdaman domain with which the array must be shifted.
     */
    private String getShiftDomain(CoverageMetadata inputCoverage, Map<Integer, Interval<Long>> pixelIndices) {
        String shiftDomain = "[";
        for (int i = 0; i < inputCoverage.getDimension(); i++) {
            Long shift = Long.valueOf(0);
            if (pixelIndices.containsKey(i)) {
                shift = pixelIndices.get(i).getLowerLimit();
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
     * @param currentCoverage the coverage targeted by the update operation.
     * @param subsets the list of subsets indicated in the update coverage request.
     * @return map indicating the pixel indices for each dimension.
     */
    private Map<Integer, Interval<Long>> getPixelIndicesByCoordinate(CoverageMetadata currentCoverage, List<DimensionSubset> subsets) {
        CoverageInfo coverageInfo = new CoverageInfo(currentCoverage);
        Coverage currentWcpsCoverage = new Coverage(currentCoverage.getCoverageName(), coverageInfo, currentCoverage);
        CoverageRegistry coverageRegistry = new CoverageRegistry(meta);
        Map<Integer, Interval<Long>> result = new HashMap<Integer, Interval<Long>>();
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
            Interval<String> currentInterval = new Interval<String>(low, high);
            String crs = i.getCrs() != null ? i.getCrs() : currentWcpsCoverage.getCoverageMetadata().getDomainByName(i.getDimension()).getNativeCrs();
            CrsComputer crsComputer = new CrsComputer(i.getDimension(), crs, currentInterval, currentWcpsCoverage, coverageRegistry);
            Interval<Long> pixelIndices = crsComputer.getPixelIndices(true);
            result.put(currentWcpsCoverage.getCoverageMetadata().getDomainIndexByName(i.getDimension()), pixelIndices);
        }
        return result;
    }

    /**
     * Gets the GML coverage representation from an update request.
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
     * @param currentCoverage the coverage targeted by the update operation.
     * @return rasdaman collection name.
     */
    private String getCurrentCollectionName(CoverageMetadata currentCoverage) {
        return currentCoverage.getRasdamanCollection().snd;
    }

    /**
     * Gets the oid of the array corresponding to the coverage targeted by the update operation.
     * @param currentCoverage the coverage targeted by the update operation.
     * @return the string representation of the oid.
     */
    private String getCurrentCollectionOid(CoverageMetadata currentCoverage) {
        return currentCoverage.getRasdamanCollection().fst.toString();
    }

    /**
     * Gets the array in the values clause to be used in a rasdaman update query, when values are given as tuple list.
     * @param coverage the coverage providing the values.
     * @param rangeSet the rangeSet element.
     * @return string representations of the values clause, as rasdaman array constant.
     * @throws PetascopeException
     */
    private String getReplacementValuesFromTupleList(CoverageMetadata coverage, Element rangeSet, String pixelDataType) throws PetascopeException {
        Element dataBlock = GMLParserUtil.parseDataBlock(rangeSet);
        Pair<String, Character> collectionType = TypeResolverUtil.guessCollectionType(coverage.getNumberOfBands(), coverage.getDimension(), pixelDataType);
        String values = GMLParserUtil.parseGMLTupleList(dataBlock, coverage.getCellDomainList(), collectionType.getValue().toString());
        return values;
    }

    /**
     * Gets the file to be used in the values clause of the rasdaman update query, when the values are given as file ref.
     * @param rangeSet the rangeSet element.
     * @return file containing values for update.
     * @throws IOException
     * @throws WCSException
     */
    private File getReplacementValuesFromFile(Element rangeSet) throws IOException, WCSException {
        //tuple list given as file
        String fileUrl = GMLParserUtil.parseFilePath(rangeSet);
        String mimetype = GMLParserUtil.parseMimeType(rangeSet);
        //save in a temporary file to pass to gdal and rasdaman
        File tmpFile = RemoteCoverageUtil.copyFileLocally(fileUrl);
        return tmpFile;
    }

    private static final String UPDATE_TEMPLATE_VALUES = "UPDATE $collection SET $collection$domain ASSIGN shift($values, $shiftDomain) WHERE oid($collection) = $oid";
    private static final String UPDATE_TEMPLATE_FILE = "UPDATE $collection SET $collection$domain ASSIGN shift(decode($1), $shiftDomain) WHERE oid($collection) = $oid";

}
