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
package petascope.wcs2.handlers.wcst;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.RasdamanException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.GMLParserUtil;
import petascope.util.Pair;
import petascope.util.XMLSymbols;
import petascope.util.XMLUtil;
import petascope.util.ras.RasUtil;
import petascope.util.ras.TypeResolverUtil;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.handlers.AbstractRequestHandler;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.helpers.wcst.RemoteCoverageUtil;
import petascope.wcs2.parsers.wcst.InsertCoverageRequest;
import petascope.wcs2.templates.Templates;
import rasj.RasResultIsNoIntervalException;
/**
 * Handles the insertion of coverages into petascope, according to the WCS-T
 * specs.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class InsertCoverageHandler extends AbstractRequestHandler<InsertCoverageRequest> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(InsertCoverageHandler.class);

    /**
     * Class constructor
     *
     * @param meta
     */
    public InsertCoverageHandler(DbMetadataSource meta) {
        super(meta);
    }

    /**
     * Handles the InsertCoverage request
     *
     * @param request an InsertCoverage request
     * @return if the ingestion if successful, a response containing the added
     * coverage id is returned.
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     */
    public Response handle(InsertCoverageRequest request) throws PetascopeException, WCSException, SecoreException {
        if (request.getGMLCoverage() != null) {
            return handleGMLCoverageInsert(request);
        } else {
            return handleRemoteCoverageIsert(request);
        }
    }

    private Response handleGMLCoverageInsert(InsertCoverageRequest request) throws WCSException, PetascopeException, SecoreException {
        return new Response(null, insertGMLCoverage(request.getGMLCoverage(), request.isUseNewId(),
                request.getPixelDataType(), request.getTiling()), FormatExtension.MIME_XML);
    }

    /**
     * Only works with GML remote coverages for now
     *
     * @param request
     * @return
     */
    private Response handleRemoteCoverageIsert(InsertCoverageRequest request) throws PetascopeException, SecoreException {
        //get the remote coverage
        String coverage = RemoteCoverageUtil.getRemoteGMLCoverage(request.getCoverageURL());
        //if it is not GML, make it GML
        //for now assuming only GML
        if (false) {

        }
        //finally process it
        return new Response(null, insertGMLCoverage(coverage, request.isUseNewId(), request.getPixelDataType(), request.getTiling()), FormatExtension.MIME_XML);
    }

    /**
     * Handles the insertion of a coverage in GML format.
     *
     * @param GMLCoverage: a coverage in GML format
     * @return if the ingestion if successful, a response containing the added
     * coverage id is returned.
     * @throws WCSException
     * @throws PetascopeException
     * @throws SecoreException
     */
    private String insertGMLCoverage(String GMLCoverage, Boolean generateId, String pixelDataType, String tiling) throws PetascopeException, SecoreException {
        Document xmlCoverage;
        CoverageMetadata coverage;
        String result = null;
        String outputA = "";
        try {
            xmlCoverage = XMLUtil.buildDocument(null, GMLCoverage);
            //parse the gml
            coverage = CoverageMetadata.fromGML(xmlCoverage);
            //add coverage id
            if (generateId) {
                coverage.setCoverageName(generateCoverageName());
            }
            //use a generated collection name to avoid conflicts
            String collectionName =  generateCollectionName();
            BigInteger oid;
            Element rangeSet = GMLParserUtil.parseRangeSet(xmlCoverage.getRootElement());
            String rasCollectionType;
            if (rangeSet.getChildElements(XMLSymbols.LABEL_DATABLOCK,
                    XMLSymbols.NAMESPACE_GML).size() != 0) {
                //tuple list given explicitly
                //get the pixel values as rasdaman constant
                Element dataBlock = GMLParserUtil.parseDataBlock(rangeSet);
                org.apache.commons.lang3.tuple.Pair<String, Character> collectionType =
                        TypeResolverUtil.guessCollectionType(coverage.getNumberOfBands(), coverage.getDimension(), pixelDataType);
                rasCollectionType = collectionType.getKey();
                String rasdamanValues = GMLParserUtil.parseGMLTupleList(dataBlock, coverage.getCellDomainList(), collectionType.getValue().toString());
                oid = insertValuesIntoRasdaman(collectionName, rasCollectionType, rasdamanValues, tiling);
            } else {
                //tuple list given as file
                String fileUrl = GMLParserUtil.parseFilePath(rangeSet);
                String mimetype = GMLParserUtil.parseMimeType(rangeSet);
                //save in a temporary file to pass to gdal and rasdaman
                File tmpFile = RemoteCoverageUtil.copyFileLocally(fileUrl);
                //pass it to gdal to get the collection type
                rasCollectionType = TypeResolverUtil.guessCollectionTypeFromFile(tmpFile.getAbsolutePath(), coverage.getDimension());
                //insert it into rasdaman
                oid = insertFileIntoRasdaman(collectionName, rasCollectionType, tmpFile.getAbsolutePath(), mimetype, tiling);
                //delete the temporary file
                tmpFile.delete();
            }

            coverage.setRasdamanCollection(new Pair<BigInteger, String>(oid, collectionName));
            coverage.setRasdamanCollectionType(TypeResolverUtil.getMddTypeForCollectionType(rasCollectionType) + ":" + rasCollectionType);
            meta.insertNewCoverageMetadata(coverage, true);

            result = Templates.getTemplate(Templates.INSERT_COVERAGE_RESPONSE, new Pair<String, String>(
                    Templates.KEY_COVERAGE_ID, coverage.getCoverageName()));

        } catch (IOException ex) {
            Logger.getLogger(InsertCoverageHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new PetascopeException(ExceptionCode.WCSTCoverageNotFound);
        } catch (ParsingException ex) {
            Logger.getLogger(InsertCoverageHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new PetascopeException(ExceptionCode.WCSTInvalidXML, ex.getMessage());
        }
        return result;
    }

    /**
     * Generates a unique coverage name.
     * @return
     */
    private String generateCoverageName() {
        return (WCST_AUTOGENERATED_PREFIX + java.util.UUID.randomUUID().toString()).replace("-", "_");
    }

    /**
     * Generates a unique collection name.
     * @return
     */
    private String generateCollectionName() {
        return (DEFAULT_COL_PREFIX + java.util.UUID.randomUUID().toString()).replace("-", "_");
    }

    private BigInteger insertFileIntoRasdaman(String collectionName, String collectionType, String filePath, String mimetype, String tiling) throws PetascopeException, IOException {
        log.info("Creating rasdaman collection " + collectionName + ".");
        //create the collection
        RasUtil.createRasdamanCollection(collectionName, collectionType);
        log.info("Collection created.");
        BigInteger oid = RasUtil.executeInsertFileStatement(collectionName, filePath,mimetype,
                ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, tiling);

        return oid;
    }

    /**
     * Creates the rasdaman collection, if necessary, and inserts the coverage
     * pixels.
     *
     * @param collectionName
     * @param collectionType
     * @param values
     * @return the oid of the newly inserted object
     */
    private BigInteger insertValuesIntoRasdaman(String collectionName, String collectionType, String values, String tiling) throws PetascopeException {
        BigInteger oid = null;
        log.info("Creating rasdaman collection " + collectionName + ".");
        //create the collection
        RasUtil.createRasdamanCollection(collectionName, collectionType);
        log.info("Collection created.");

        try {
            //insert the values
            oid = RasUtil.executeInsertValuesStatement(collectionName, values, tiling);
        } catch (RasdamanException ex) {
            log.error("Rasdaman error when inserting into collection " + collectionName + ". Error message: " + ex.getMessage());
            throw ex;
        }
        return oid;
    }

    private static final String WCST_AUTOGENERATED_PREFIX = "WCST_";
    private static final String DEFAULT_COL_PREFIX = "WCST_";
}
