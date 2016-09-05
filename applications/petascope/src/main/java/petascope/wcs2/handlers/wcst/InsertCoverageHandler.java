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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.slf4j.LoggerFactory;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.rasdaman.RasdamanException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.exceptions.rasdaman.RasdamanCollectionExistsException;
import petascope.exceptions.wcst.WCSTCoverageNotFound;
import petascope.exceptions.wcst.WCSTInvalidXML;
import petascope.util.GMLParserUtil;
import petascope.util.Pair;
import petascope.util.XMLSymbols;
import petascope.util.XMLUtil;
import petascope.util.ras.RasUtil;
import petascope.util.ras.TypeResolverUtil;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.handlers.AbstractRequestHandler;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.handlers.wcst.helpers.insert.RasdamanCollectionCreator;
import petascope.wcs2.handlers.wcst.helpers.insert.RasdamanDefaultCollectionCreator;
import petascope.wcs2.handlers.wcst.helpers.insert.RasdamanInserter;
import petascope.wcs2.handlers.wcst.helpers.insert.RasdamanValuesInserter;
import petascope.wcs2.handlers.wcst.helpers.insert.RasdamanFileInserter;
import petascope.wcs2.helpers.wcst.RemoteCoverageUtil;
import petascope.wcs2.parsers.wcst.InsertCoverageRequest;
import petascope.wcs2.templates.Templates;

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
     * @param meta the dbmetadata source
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
    public Response handle(InsertCoverageRequest request) throws PetascopeException, SecoreException {
        log.info("Handling coverage insertion...");
        if (request.getGMLCoverage() != null) {
            return handleGMLCoverageInsert(request);
        } else {
            return handleRemoteCoverageIsert(request);
        }
    }

    private Response handleGMLCoverageInsert(InsertCoverageRequest request) throws PetascopeException, SecoreException {
        return new Response(null, new String[] { insertGMLCoverage(request.getGMLCoverage(), request) }, FormatExtension.MIME_XML);
    }

    /**
     * Only works with GML remote coverages for now
     *
     * @param request the insertCoverage request.
     * @return InsertCoverage response.
     */
    private Response handleRemoteCoverageIsert(InsertCoverageRequest request) throws PetascopeException, SecoreException {
        //get the remote coverage
        String coverage = RemoteCoverageUtil.getRemoteGMLCoverage(request.getCoverageURL());
        //if it is not GML, make it GML
        //for now assuming only GML

        //finally process it
        return new Response(null, new String[] { insertGMLCoverage(coverage, request) }, FormatExtension.MIME_XML);
    }

    /**
     * Handles the insertion of a coverage in GML format.
     *
     * @param GMLCoverage: a coverage in GML format
     * @return if the ingestion if successful, a response containing the added
     * coverage id is returned.
     * @throws WCSTCoverageNotFound
     * @throws WCSTInvalidXML
     * @throws WCSException
     * @throws PetascopeException
     * @throws SecoreException
     */
    private String insertGMLCoverage(String GMLCoverage, InsertCoverageRequest request)
            throws WCSTCoverageNotFound, WCSTInvalidXML, PetascopeException, SecoreException {
        Boolean generateId = request.isUseNewId();
        String pixelDataType = request.getPixelDataType();
        String tiling = request.getTiling();
        Document xmlCoverage;
        CoverageMetadata coverage;
        String result;
        try {
            xmlCoverage = XMLUtil.buildDocument(null, GMLCoverage);
            //parse the gml
            coverage = CoverageMetadata.fromGML(xmlCoverage);
            ArrayList<String> nullValues = coverage.getAllUniqueNullValues();
            //add coverage id
            if (generateId) {
                coverage.setCoverageName(generateCoverageName());
            }
            //use the same collection name as the coverage name (NOTE: rasdaman does not support "-" in collection name, then replace it)
            String collectionName = coverage.getCoverageName().replace("-", "_");

            BigInteger oid;
            RasdamanInserter rasdamanInserter;
            RasdamanCollectionCreator rasdamanCollectionCreator;
            Element rangeSet = GMLParserUtil.parseRangeSet(xmlCoverage.getRootElement());
            String rasCollectionType;
            if (rangeSet.getChildElements(XMLSymbols.LABEL_DATABLOCK,
                    XMLSymbols.NAMESPACE_GML).size() != 0) {
                //tuple list given explicitly
                //get the pixel values as rasdaman constant
                Element dataBlock = GMLParserUtil.parseDataBlock(rangeSet);
                org.apache.commons.lang3.tuple.Pair<String, String> collectionType
                        = TypeResolverUtil.guessCollectionType(coverage.getNumberOfBands(), coverage.getDimension(), nullValues, pixelDataType);
                rasCollectionType = collectionType.getKey();
                String rasdamanValues = GMLParserUtil.parseGMLTupleList(dataBlock, coverage.getCellDomainList(), collectionType.getValue());
                rasdamanCollectionCreator = new RasdamanDefaultCollectionCreator(collectionName, rasCollectionType);

                 // NOTE: collectionName could be exist in rasdaman db, then must check if it does exist -> rename it with collectionName_datetime
                try {
                    rasdamanCollectionCreator.createCollection();
                } catch (RasdamanCollectionExistsException e) {
                        String dateTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
                        collectionName = collectionName + "_" + dateTime;
                        // Retry to create collection with new collection name
                        rasdamanCollectionCreator = new RasdamanDefaultCollectionCreator(collectionName, rasCollectionType);
                        rasdamanCollectionCreator.createCollection();
                }
                rasdamanInserter = new RasdamanValuesInserter(collectionName, rasCollectionType, rasdamanValues, tiling);
                oid = rasdamanInserter.insert();
            } else {
                //tuple list given as file
                String fileUrl = GMLParserUtil.parseFilePath(rangeSet);
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
                    tmpFile = RemoteCoverageUtil.copyFileLocally(fileUrl);
                    fileIsLocal = false;
                }
                String mimetype = GMLParserUtil.parseMimeType(rangeSet);
                //pass it to gdal to get the collection type
                if(pixelDataType != null){
                    rasCollectionType = TypeResolverUtil.guessCollectionType(coverage.getNumberOfBands(), coverage.getDimension(), nullValues, pixelDataType).getKey();
                }
                else {
                  //read it from file
                  rasCollectionType = TypeResolverUtil.guessCollectionTypeFromFile(tmpFile.getAbsolutePath(), coverage.getDimension(), nullValues);
                }
                //insert it into rasdaman
                rasdamanCollectionCreator = new RasdamanDefaultCollectionCreator(collectionName, rasCollectionType);
                rasdamanInserter = new RasdamanFileInserter(collectionName, tmpFile.getAbsolutePath(), mimetype, tiling);

                rasdamanCollectionCreator.createCollection();
                oid = rasdamanInserter.insert();
                //delete the temporary file
                if (!fileIsLocal) {
                    tmpFile.delete();
                }
            }

            coverage.setRasdamanCollection(new Pair<BigInteger, String>(oid, collectionName));
            coverage.setRasdamanCollectionType(TypeResolverUtil.getMddTypeForCollectionType(rasCollectionType) + ":" + rasCollectionType);
            meta.insertNewCoverageMetadata(coverage, true);

            result = Templates.getTemplate(Templates.INSERT_COVERAGE_RESPONSE, new Pair<String, String>(
                    Templates.KEY_COVERAGE_ID, coverage.getCoverageName()));

        } catch (IOException ex) {
            Logger.getLogger(InsertCoverageHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new WCSTCoverageNotFound();
        } catch (ParsingException ex) {
            Logger.getLogger(InsertCoverageHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new WCSTInvalidXML(ex.getMessage());
        }
        return result;
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
