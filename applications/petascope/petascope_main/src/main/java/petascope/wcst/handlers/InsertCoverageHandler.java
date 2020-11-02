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
package petascope.wcst.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nu.xom.Document;
import nu.xom.Element;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.Field;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.NilValue;
import org.rasdaman.domain.cis.Quantity;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.rasdaman.exceptions.RasdamanCollectionExistsException;
import petascope.wcst.exceptions.WCSTCoverageParameterNotFound;
import petascope.wcst.exceptions.WCSTInvalidXML;
import petascope.core.gml.cis10.GMLCIS10ParserService;
import petascope.util.MIMEUtil;
import petascope.core.Pair;
import petascope.util.StringUtil;
import petascope.core.XMLSymbols;
import petascope.util.XMLUtil;
import petascope.util.ras.TypeResolverUtil;
import petascope.core.response.Response;
import petascope.core.Templates;
import petascope.core.gml.cis.service.GMLCISParserService;
import petascope.exceptions.ExceptionCode;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.ListUtil;
import petascope.util.ras.RasUtil;
import static petascope.util.ras.TypeResolverUtil.GDT_Float32;
import petascope.wcst.exceptions.WCSTCoverageIdNotValid;
import petascope.wcst.exceptions.WCSTDuplicatedCoverageId;
import petascope.wcst.helpers.insert.RasdamanCollectionCreator;
import petascope.wcst.helpers.insert.RasdamanDefaultCollectionCreator;
import petascope.wcst.helpers.insert.AbstractRasdamanInserter;
import petascope.wcst.helpers.insert.RasdamanValuesInserter;
import petascope.wcst.helpers.insert.RasdamanFileInserter;
import petascope.wcst.helpers.RemoteCoverageUtil;
import petascope.wcst.parsers.InsertCoverageRequest;

/**
 * Handles the insertion of coverages into petascope, according to the WCS-T
 * specs.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class InsertCoverageHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(InsertCoverageHandler.class);
    private String coverageId;

    @Autowired
    private GMLCISParserService gmlCISParserService;
    @Autowired
    private CoverageRepositoryService persistedCoverageService;

    /**
     * Handles the InsertCoverage request
     *
     * @param request an InsertCoverage request
     * @return if the ingestion if successful, a response containing the added
     * coverage id is returned.
     */
    public Response handle(InsertCoverageRequest request) throws PetascopeException, SecoreException {
        log.debug("Handling coverage insertion...");
        if (request.getGMLCoverage() != null) {
            return handleGMLCoverageInsert(request);
        } else {
            return handleRemoteCoverageInsert(request);
        }
    }

    private Response handleGMLCoverageInsert(InsertCoverageRequest request) throws PetascopeException, SecoreException {
        String result = this.insertGMLCoverage(request.getGMLCoverage(), request);
        return new Response(Arrays.asList(result.getBytes()), MIMEUtil.MIME_GML, null);
    }

    /**
     * Only works with GML remote coverages for now
     *
     * @param request the insertCoverage request.
     * @return InsertCoverage response.
     */
    private Response handleRemoteCoverageInsert(InsertCoverageRequest request) throws PetascopeException, SecoreException {
        //get the remote coverage
        String gmlCoverage = RemoteCoverageUtil.getRemoteGMLCoverage(request.getCoverageURL());
        String result = insertGMLCoverage(gmlCoverage, request);
        //finally process it
        return new Response(Arrays.asList(result.getBytes()), MIMEUtil.MIME_GML, this.coverageId);
    }

    /**
     * Handles the insertion of a coverage in GML format.
     *
     * @param GMLCoverage: a coverage in GML format
     * @return if the ingestion if successful, a response containing the added
     * coverage id is returned.
     */
    private String insertGMLCoverage(String GMLCoverage, InsertCoverageRequest request)
            throws WCSTCoverageParameterNotFound, WCSTInvalidXML, PetascopeException, SecoreException, RasdamanException {

        Document gmlCoverageDocument;
        Coverage coverage;
        String result;
        
            try {
                // Build the XML document
                gmlCoverageDocument = XMLUtil.buildDocument(null, GMLCoverage);
            } catch (Exception ex) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                            "Cannot parse the input GML from InsertCoverage request to XML document. Reason: " + ex.getMessage(), ex);
            }
             
            // Parse the gml to Coverage
            coverage = gmlCISParserService.parseDocumentToCoverage(gmlCoverageDocument);
            // Check if coverage already existed first
            try {
                Coverage tempCoverage = persistedCoverageService.readCoverageByIdFromDatabase(coverage.getCoverageId());
                if (tempCoverage != null) {
                    throw new WCSTDuplicatedCoverageId(coverage.getCoverageId());
                }
            } catch (PetascopeException ex) {
                if (ex.getExceptionCode().equals(ExceptionCode.NoSuchCoverage)) {
                    // Coverage does not exist, good to insert new one
                } else {
                    throw ex;
                }
            }

            try {
                this.handleInsertCoverageRequest(request, coverage, gmlCoverageDocument);
            } catch (IOException ex) {
                throw new PetascopeException(ExceptionCode.InternalComponentError,
                                             "Cannot handle insert coverage request. Reason: " + ex.getMessage(), ex);
            }
            
            // Now can finish the coverage build and persist to database            
            persistedCoverageService.save(coverage);
            
            result = Templates.getTemplate(Templates.WCS2_WCST_INSERT_COVERAGE_RESPONSE,
                                           new Pair<>(Templates.WCS2_WCST_INSERT_COVERAGE_COVERAGE_ID, coverage.getCoverageId()));
            
        return result;
    }
    
    /**
     * Handle InsertCoverage request to create rasdaman collection
     */
    private void handleInsertCoverageRequest(InsertCoverageRequest request, Coverage coverage, Document gmlCoverageDocument)
            throws PetascopeException, RasdamanException, IOException, SecoreException {
        
        String username = ConfigManager.RASDAMAN_ADMIN_USER;
        String password = ConfigManager.RASDAMAN_ADMIN_PASS;
        
        Boolean generateId = request.isUseNewId();
        String pixelDataType = request.getPixelDataType();
        String tiling = request.getTiling();
        
        // List of unique nullValues for all bands (quantities) to create rasdaman range set
        List<NilValue> nullValues = coverage.getAllUniqueNullValues();

        //add coverage id
        if (generateId) {
            coverage.setCoverageId(generateCoverageName());
        }

        this.coverageId = coverage.getCoverageId();

        // use the same collection name as the coverage name 
        // (NOTE: rasdaman does not support "-" in collection name)
        String collectionName = coverage.getCoverageId();
        if (collectionName.contains("-")) {
            throw new WCSTCoverageIdNotValid(collectionName);
        }

        AbstractRasdamanInserter rasdamanInserter;
        RasdamanCollectionCreator rasdamanCollectionCreator;
        Element rangeSet = GMLCIS10ParserService.parseRangeSet(gmlCoverageDocument.getRootElement());

        int numberOfDimensions = coverage.getNumberOfDimensions();
        int numberOfBands = coverage.getNumberOfBands();
        // rasdaman set type
        String rasCollectionType;
        

        if (rangeSet.getChildElements(XMLSymbols.LABEL_DATABLOCK,
                XMLSymbols.NAMESPACE_GML).size() != 0) {
            //tuple list given explicitly
            //get the pixel values as rasdaman constant
            Element dataBlock = GMLCIS10ParserService.parseDataBlock(rangeSet);

            long start = System.currentTimeMillis();
            Pair<String, List<String>> collectionTypePair
                    = TypeResolverUtil.guessCollectionType(collectionName, numberOfBands, numberOfDimensions, nullValues, pixelDataType);
            rasCollectionType = collectionTypePair.fst;
            long end = System.currentTimeMillis();
            log.debug("Time for guessing collection type: " + String.valueOf(end - start) + " ms.");

            // Right now, only support GeneralGridCoverage
            List<IndexAxis> indexAxes = ((GeneralGridCoverage) coverage).getIndexAxes();

            // e.g: us, d, f,...
            List<String> typeSuffixes = collectionTypePair.snd;
            String rasdamanValues = GMLCIS10ParserService.parseGMLTupleList(dataBlock, indexAxes, typeSuffixes);
            start = System.currentTimeMillis();
            rasdamanCollectionCreator = new RasdamanDefaultCollectionCreator(collectionName, rasCollectionType);

            // NOTE: collectionName could be exist in rasdaman db, then must check if it does exist -> rename it with collectionName_datetime
            try {
                rasdamanCollectionCreator.createCollection();
            } catch (RasdamanCollectionExistsException e) {
                collectionName = StringUtil.addDateTimeSuffix(collectionName);
                // Retry to create collection with new collection name
                rasdamanCollectionCreator = new RasdamanDefaultCollectionCreator(collectionName, rasCollectionType);
                rasdamanCollectionCreator.createCollection();
            }
            rasdamanInserter = new RasdamanValuesInserter(collectionName, rasCollectionType, rasdamanValues, tiling, username, password);
            rasdamanInserter.insert();
            end = System.currentTimeMillis();
            log.debug("Time for creating collection: " + String.valueOf(end - start));
        } else {
            //tuple list given as file
            String fileUrl = GMLCIS10ParserService.parseFilePath(rangeSet);
            if (fileUrl.startsWith("file://")) {
                fileUrl = fileUrl.replace("file://", "");
            }
            File tmpFile;
            boolean fileIsLocal;
            if (fileUrl.startsWith("/")) {
                //local file
                tmpFile = new File(fileUrl);
                fileIsLocal = true;
            } else {
                try {
                    tmpFile = RemoteCoverageUtil.copyFileLocally(fileUrl);
                } catch (IOException ex) {
                    throw new PetascopeException(ExceptionCode.IOConnectionError, "Cannot copy file from '" + fileUrl + "' to local. Reason: " + ex.getMessage(), ex);
                }
                fileIsLocal = false;
            }
            String mimetype = GMLCIS10ParserService.parseMimeType(rangeSet);
            //pass it to gdal to get the collection type
            if (pixelDataType != null) {
                rasCollectionType = TypeResolverUtil.guessCollectionType(collectionName, numberOfBands, numberOfDimensions, nullValues, pixelDataType).fst;
            } else {
                //read it from file
                rasCollectionType = TypeResolverUtil.guessCollectionTypeFromFile(collectionName, tmpFile.getAbsolutePath(), numberOfDimensions, nullValues);
            }
            //insert it into rasdaman
            rasdamanCollectionCreator = new RasdamanDefaultCollectionCreator(collectionName, rasCollectionType);
            rasdamanInserter = new RasdamanFileInserter(collectionName, tmpFile.getAbsolutePath(), mimetype, tiling, username, password);

            rasdamanCollectionCreator.createCollection();
            rasdamanInserter.insert();
            // delete the temporary file
            if (!fileIsLocal) {
                tmpFile.delete();
            }
        }

        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        rasdamanRangeSet.setCollectionName(collectionName);
        // set Type
        rasdamanRangeSet.setCollectionType(rasCollectionType);
        String mddType = TypeResolverUtil.getMddTypeForCollectionType(rasCollectionType);
        rasdamanRangeSet.setMddType(mddType);
        rasdamanRangeSet.setTiling(tiling);

        // rasdaman collection was created add this to coverage
        coverage.setRasdamanRangeSet(rasdamanRangeSet);

        this.updateRasdamanDataTypesForRangeQuantities(coverage, pixelDataType);
    }
    
    /**
     * Given a temp coverage object, insert it as a temp rasdaman collection
     * and update the collection from a file path.
     * 
     * @decodeExpression: e.g: decode(<[0:0] 1c>, "GDAL", "{\"filePaths\":[\"/home/rasdaman/test.TIFF\"]}")
     */
    public void insertTempCoverage(Coverage coverage, String decodeExpression) throws PetascopeException {
        String collectionName = coverage.getCoverageId();
        int numberOfDimensions = coverage.getNumberOfDimensions();
        int numberOfBands = coverage.getNumberOfBands();
        List<NilValue> bandNullValues = coverage.getAllNullValues();
        
        // e.g: Byte (all bands have same data type) or Float32,Int16 (each band has different data type)
        String pixelDataType = coverage.getPixelDataType();
        
        Pair<String, List<String>> collectionTypePair
                    = TypeResolverUtil.guessCollectionType(coverageId, numberOfBands, numberOfDimensions, bandNullValues, pixelDataType);
        String rasCollectionType = collectionTypePair.fst;
        // e.g: c, d, f,...
        List<String> typeSuffixes = collectionTypePair.snd;
        
        // Then, create a temp rasdaman collection for this coverage
        RasdamanCollectionCreator rasdamanCollectionCreator = new RasdamanDefaultCollectionCreator(collectionName, rasCollectionType);
        rasdamanCollectionCreator.createCollection();
        
        // e.g: <[0:0,0:0,0:0] {9999f,9999f,9999f,9999f,9999f,9999f,9999f,9999f,9999f}>
        String rasdamanValues = this.createRasdamanInsertNullValuesExpression(numberOfDimensions, bandNullValues, typeSuffixes);
        RasdamanValuesInserter rasdamanInserter = new RasdamanValuesInserter(collectionName, rasCollectionType, rasdamanValues, null, 
                                                                             ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS);
        rasdamanInserter.insert();
        
        // Then, update this test collection from the given file path
        this.updateTempCollection(coverage, decodeExpression);
    }
    
    /**
     * Update a temp collection with a decode expression
     * 
     * @decodeExpression: e.g: decode(<[0:0] 1c>, "GDAL", "{\"filePaths\":[\"/home/rasdaman/test.TIFF\"]}")
     */
    private void updateTempCollection(Coverage coverage, String decodeExpression) throws PetascopeException {
        final String COLLECTION_NAME = "$COLLECTION_NAME";
        final String GRID_DOMAINS = "$GRID_DOMAINS";
        final String TEMPLATE = "UPDATE " + COLLECTION_NAME + " SET " + COLLECTION_NAME + "[" + GRID_DOMAINS + "] ASSIGN " + decodeExpression;
        
        List<String> gridDomainsTmp = new ArrayList<>();
        List<IndexAxis> indexAxes = ((GeneralGridCoverage)coverage).getIndexAxes();
        for (IndexAxis indexAxis : indexAxes) {
            String gridDomain = indexAxis.getLowerBound() + ":" + indexAxis.getUpperBound();
            gridDomainsTmp.add(gridDomain);
        }
        
        String gridDomains = ListUtil.join(gridDomainsTmp, ",");
        String query = TEMPLATE.replace(COLLECTION_NAME, coverage.getCoverageId())
                                .replace(GRID_DOMAINS, gridDomains);
        RasUtil.executeUpdateFileStatement(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS);        
    }
    
    /**
     * Return the INSERT domain with null values and data typ expression
     * e.g:  <[0:0,0:0,0:0] {-999f,-999f,0s}> for a coverage with 3 axes and 3 bands with different data types and null values
     * or  <[0:0,0:0] {0c,0c,0c}> for a coverage with 2 axes and 3 bands and same null values
     */
    private String createRasdamanInsertNullValuesExpression(int numberOfDimensions, List<NilValue> bandNullValues, List<String> typeSuffixes) {        
        final String GRID_DOMAINS = "$GRID_DOMAINS";
        final String DEFAULT_GRID_DOMAIN = "0:0";
        
        final String NULL_VALUES = "$NULL_VALUES";
        final String DEFAUL_NULL_VALUE = "0";
        final String FLOAT_ZERO_SUFFIX = ".0";
        final String TEMPLATE = "<[" + GRID_DOMAINS + "] {" + NULL_VALUES + "}>";
        
        List<String> gridDomainsTmp = new ArrayList<>();
        for (int i = 0; i < numberOfDimensions; i++) {
            gridDomainsTmp.add(DEFAULT_GRID_DOMAIN);
        }
        
        List<String> nullValuesTmp = new ArrayList<>();
        for (int i = 0; i < typeSuffixes.size(); i++) {
            String nullValue = DEFAUL_NULL_VALUE;
            // e.g: c
            String suffix = typeSuffixes.get(i);
            if (!bandNullValues.isEmpty()) {
                nullValue = bandNullValues.get(i).getValue();
                if (nullValue.endsWith(FLOAT_ZERO_SUFFIX)) {
                    nullValue = nullValue.replace(FLOAT_ZERO_SUFFIX, "");
                }
            }
            
            String value = nullValue + suffix;
            nullValuesTmp.add(value);
        }
        
        String gridDomains = ListUtil.join(gridDomainsTmp, ",");
        String nullValues = ListUtil.join(nullValuesTmp, ",");
        
        String result = TEMPLATE.replace(GRID_DOMAINS, gridDomains)
                                .replace(NULL_VALUES, nullValues);
        return result;
    }
    
    /**
     * Update coverage's range quantities' rasdaman types from request parameter pixelDataType.
     */
    private void updateRasdamanDataTypesForRangeQuantities(Coverage coverage, String pixelDataType) {
        if (pixelDataType == null) {
            pixelDataType = TypeResolverUtil.GDAL_TYPES_TO_RAS_TYPES.get(GDT_Float32);
        }
        // e.g: Float32,Float32,Int16
        String[] gdalDataTypes = pixelDataType.split(",");
        int i = 0;
        for (Field field : coverage.getRangeType().getDataRecord().getFields()) {
            Quantity quantity = field.getQuantity();
            
            String gdalDataType = gdalDataTypes[0];
            if (gdalDataTypes.length > 1) {
                gdalDataType = gdalDataTypes[i];
            }
            
            // e.g: gdal Byte -> rasdaman char
            String rasdamanDataType = TypeResolverUtil.GDAL_TYPES_TO_RAS_TYPES.get(gdalDataType);            
            quantity.setDataType(rasdamanDataType);
            
            i++;
        }        
    }

    /**
     * Generates a unique coverage name.
     *
     * @return a unique coverage name.
     */
    private String generateCoverageName() {
        return (WCST_AUTOGENERATED_PREFIX + java.util.UUID.randomUUID().toString()).replace("-", "_");
    }

    private static final String WCST_AUTOGENERATED_PREFIX = "WCST_";
}
