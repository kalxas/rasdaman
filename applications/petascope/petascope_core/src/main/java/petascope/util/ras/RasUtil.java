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
package petascope.util.ras;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.odmg.Database;
import org.odmg.ODMGException;
import org.odmg.OQLQuery;
import org.odmg.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.config.ConfigManager;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.rasdaman.exceptions.RasdamanCollectionDoesNotExistException;
import petascope.rasdaman.exceptions.RasdamanCollectionExistsException;
import petascope.util.BigDecimalUtil;
import petascope.core.Pair;
import petascope.util.ListUtil;
import static petascope.util.ras.RasConstants.RASQL_VERSION;
import rasj.RasImplementation;
import rasj.odmg.RasBag;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.util.ras.RasConstants.RASQL_OPEN_SUBSETS;
import static petascope.util.ras.RasConstants.RASQL_CLOSE_SUBSETS;

/**
 * Rasdaman utility classes - execute queries, etc.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class RasUtil {

    private static final Logger log = LoggerFactory.getLogger(RasUtil.class);

    /**
     * Execute a RasQL query with configured credentials.
     */
    public static Object executeRasqlQuery(String query) throws PetascopeException {
        return executeRasqlQuery(query, ConfigManager.RASDAMAN_USER, ConfigManager.RASDAMAN_PASS, false);
    }
    
    private static void closeDB(Database db) {
        if (db != null) {
            try {
                db.close();
            } catch (Exception ex) {
                log.warn("Failed closing rasdaman db connection: " + ex.getMessage());
            }
        }
    }
    
    private static void abortTR(Transaction tr) {
        if (tr != null) {
            try {
                tr.abort();
            } catch (Exception ex) {
                log.warn("Failed closing rasdaman transaction: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Executes a rasql query and returns result.
     *
     * @param query rasql query string
     * @param username rasdaman user
     * @param password password for the user
     * @param rw true if query will do DB updates
     * @return result from query
     */
    public static Object executeRasqlQuery(String query, String username, String password, boolean rw) throws PetascopeException {
        final long start = System.currentTimeMillis();
        log.info("Executing rasql query: " + query);

        RasImplementation impl = new RasImplementation(ConfigManager.RASDAMAN_URL);
        impl.setUserIdentification(username, password);

        Database db = impl.newDatabase();
        try {
            // open db
            db.open(ConfigManager.RASDAMAN_DATABASE, 
                    rw ? Database.OPEN_READ_WRITE : Database.OPEN_READ_ONLY);
        } catch (Exception ex) {
            log.error("Failed opening " + (rw ? "rw" : "ro") + " database connection to rasdaman: " + ex.getMessage());
            throw new RasdamanException(ExceptionCode.RasdamanUnavailable, ex);
        }

        Transaction tr = null;
        try {
            // open transaction
            tr = impl.newTransaction();
            tr.begin();
        } catch (Exception ex) {
            log.error("Failed opening " + (rw ? "rw" : "ro") + " transaction to rasdaman: " + ex.getMessage());
            closeDB(db);
            throw new RasdamanException(ExceptionCode.RasdamanUnavailable, ex);
        }
        
        OQLQuery q = null;
        try {
            q = impl.newOQLQuery();
            q.create(query);
        } catch (Exception ex) {
            // not really supposed to ever throw an exception
            log.error("Failed creating query object: " + ex.getMessage());
            abortTR(tr);
            closeDB(db);
            throw new RasdamanException(ExceptionCode.InternalComponentError, ex);
        }
        
        Object ret = null;
        try {
            ret = q.execute();
            tr.commit();
        } catch (ODMGException ex) {
            abortTR(tr);
            if (ex.getMessage().contains("CREATE: Collection name exists already.")) {
                throw new RasdamanCollectionExistsException(ExceptionCode.CollectionExists, query, ex);
            } else if (ex.getMessage().contains("Collection name is unknown.")) {
                throw new RasdamanCollectionDoesNotExistException(ExceptionCode.CollectionDoesNotExist, query, ex);
            } else {
                throw new RasdamanException(ExceptionCode.RasdamanRequestFailed,
                        "Error evaluating rasdaman query: '" + query + "'. Reason: " + ex.getMessage(), ex);
            }
        } catch (OutOfMemoryError ex) {
            abortTR(tr);
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Requested more data than the server can handle at once. "
                    + "Try increasing the maximum memory allowed for Tomcat (-Xmx JVM option).");
        } catch (Exception ex) {
            abortTR(tr);
            throw new RasdamanException(ExceptionCode.RasdamanRequestFailed, 
                    "Error evaluating rasdaman query: '" + query + "'. Reason: " + ex.getMessage(), ex);
        } finally {
            closeDB(db);
        }

        final long end = System.currentTimeMillis();
        final long totalTime = end - start;
        log.info("Rasql query executed in " + String.valueOf(totalTime) + " ms.");

        return ret;
    }

    /**
     * Fetch rasdaman version by parsing RasQL ``version()'' output.
     *
     * @return The rasdaman version
     */
    public static String getRasdamanVersion() throws RasdamanException {
        String version = "";
        Object tmpResult = null;
        try {
            tmpResult = RasUtil.executeRasqlQuery("select " + RASQL_VERSION + "()");
        } catch (Exception ex) {
            log.error("Cannot retrieve rasdaman version", ex);
            throw new RasdamanException(ExceptionCode.RasdamanUnavailable, "Could not retrieve rasdaman version; is rasdaman started?", ex);
        }
        if (null != tmpResult) {
            RasQueryResult queryResult = new RasQueryResult(tmpResult);
            String result = queryResult.toString();
            if (result != null && !result.isEmpty()) {
                // rasdaman 9.4.0 on x86_64-redhat-linux ...
                version = result.split(" ")[1];
            } else {
                log.warn("Failed retrieving rasdaman version, got: " + result);
            }
        }
        return version;
    }

    /**
     * Deletes an array from rasdaman.
     */
    public static void deleteFromRasdaman(Long oid, String collectionName) throws RasdamanException, PetascopeException {
        String query = TEMPLATE_DELETE.replaceAll(TOKEN_COLLECTION_NAME, collectionName).replace(TOKEN_OID, oid.toString());
        executeRasqlQuery(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        //check if there are other objects left in the collection
        log.info("Checking the number of objects left in collection " + collectionName);
        RasBag result = (RasBag) executeRasqlQuery(TEMPLATE_SDOM.replace(TOKEN_COLLECTION_NAME, collectionName));
        log.info("Result size is: " + String.valueOf(result.size()));
        if (result.isEmpty()) {
            //no object left, delete the collection so that the name can be reused in the future
            log.info("No objects left in the collection, dropping the collection so the name can be reused in the future.");
            executeRasqlQuery(TEMPLATE_DROP_COLLECTION.replace(TOKEN_COLLECTION_NAME, collectionName), ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        }
    }

    /**
     * Creates a collection in rasdaman.
     *
     * @param collectionName
     * @param collectionType
     * @throws RasdamanException
     */
    public static void createRasdamanCollection(String collectionName, String collectionType) throws RasdamanException, PetascopeException {
        String query = TEMPLATE_CREATE_COLLECTION.replace(TOKEN_COLLECTION_NAME, collectionName)
                .replace(TOKEN_COLLECTION_TYPE, collectionType);
        executeRasqlQuery(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
    }
    
    /**
     * Insert one MDD point to a rasdaman collection, e.g: 3D coverages with 3 bands, base type is char (c)
     * 
     * INSERT INTO test_AverageTemperature_2 VALUES <[0:0,0:0,0:0] {0c, 0c, 0c}> TILING ALIGNED [0:1000,0:1000,0:2] tile size 4194304
     */
    public static Long initializeMDD(int numberOfDimensions, int numberOfBands, 
            String collectionType, String tileSetting, String collectionName) throws PetascopeException {

        List<String> domainsTmp = new ArrayList<>();
        
        for (int i = 0; i < numberOfDimensions; i++) {
            domainsTmp.add("0:0");
        }

        List<String> mddBandValues;
        try {
            mddBandValues = TypeResolverUtil.getDefaultBandValues(collectionType);
        } catch (TypeRegistryEntryMissingException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError,
                    "Cannot get the base type abbreviation for collection type '" + collectionType + "' from type registry. Reason: " + ex.getMessage());
        }
        String domainValue = ListUtil.join(domainsTmp, ",");
        String multibandValue = ListUtil.join(mddBandValues, ",");
        
        if (mddBandValues.size() > 1) {
            multibandValue = "{" + multibandValue + "}";
        }

        // e.g: 3 dimensions and 3 bands: <[0:0,0:0,0:0] {0c,0c,0c}>
        String values = "<[" + domainValue + "] " + multibandValue + ">";
        Long oid = RasUtil.executeInsertStatement(collectionName, values, tileSetting);
        
        return oid;
    }
    
    /**
     * Update data from a source Rasdaman collection with grid subsets on a downscaled Rasdaman collection with grid subets.
     */
    public static void updateDownscaledCollectionFromSourceCollection(Long oid, String sourceAffectedDomain, 
            String targetAffectedDomain, String sourceCollectionName, String targetDownscaledCollectionName) throws PetascopeException {
        
        // e.g: update test_mr1 as c set c[*:*,*:*] assign scale(d[*:*,*:*], [0:20,0:30]) from test_mr as d
        String rasqlQuery = "UPDATE " + targetDownscaledCollectionName + " as d SET d" + targetAffectedDomain 
                     + " ASSIGN SCALE(c" + sourceAffectedDomain + ", " + targetAffectedDomain + ")"
                     + " FROM " + sourceCollectionName + " as c WHERE oid(c) = " + oid;
        RasUtil.executeRasqlQuery(rasqlQuery, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, Boolean.TRUE);
    }

    /**
     * Inserts a set of values given as an array constant in rasdaman. e.g:
     * "INSERT INTO PM10_2 VALUES <[0:0,0:0,0:0] 0f> TILING ALIGNED [0:366,
     * 0:500, 0:500]"
     *
     * @return the oid of the newly inserted object
     * @throws RasdamanException
     */
    public static Long executeInsertStatement(String collectionName, String values, String tiling) throws RasdamanException, PetascopeException {
        String tilingClause = (tiling == null || tiling.isEmpty()) ? "" : TILING_KEYWORD + " " + tiling;
        String query = TEMPLATE_INSERT_VALUES.replace(TOKEN_COLLECTION_NAME, collectionName)
                .replace(TOKEN_VALUES, values).replace(TOKEN_TILING, tilingClause);
        Object rasjResult = executeRasqlQuery(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        Long oid = Long.parseLong(new RasQueryResult(rasjResult).getScalars().get(0));

        return oid;
    }

    /**
     * Insert an image to an existing collection by decoding file
     */
    public static Long executeInsertFileStatement(String collectionName, String filePath, String mime,
            String tiling) throws RasdamanException, IOException, PetascopeException {
        Long oid = new Long("0");
        String query;
        String tilingClause = (tiling == null || tiling.isEmpty()) ? "" : TILING_KEYWORD + " " + tiling;

        query = ConfigManager.RASDAMAN_BIN_PATH + RASQL
                + " --user " + ConfigManager.RASDAMAN_ADMIN_USER + " --passwd " + ConfigManager.RASDAMAN_ADMIN_PASS + " -q "
                + "'" + TEMPLATE_INSERT_DECODE_FILE.replace(TOKEN_COLLECTION_NAME, collectionName).replace(TOKEN_TILING, tilingClause) + "' --file " + filePath;
        log.info("Executing " + query);

        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", query});
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        String response = "";
        while ((s = stdError.readLine()) != null) {
            response += s + "\n";
        }
        if (!response.isEmpty()) {
            //error occured
            throw new RasdamanException(response);
        }
        //get the collection oid
        String oidQuery = TEMPLATE_SELECT_OID.replaceAll(TOKEN_COLLECTION_NAME, collectionName);
        RasBag result = (RasBag) executeRasqlQuery(oidQuery);
        Iterator resultIterator = result.iterator();
        Object resultInstance = null;
        //get the last available oid
        while (resultIterator.hasNext()) {
            resultInstance = resultIterator.next();
        }
        if (resultInstance != null) {
            oid = new Long(resultInstance.toString());
        }
        
        return oid;
    }

    /**
     * Insert/Update an image to an existing collection by using the posted
     * rasql query e.g: rasql -q 'insert into float_3d values inv_netcdf($1,
     * "vars=values")' -f "float_3d.nc" 'update mr_test1 set mr_test1 assign
     * decode($1)'
     */
    public static void executeInsertUpdateFileStatement(String orgQuery, String filePath, String username, String password) 
            throws RasdamanException, IOException {
        long start = System.currentTimeMillis();
        
        String query = ConfigManager.RASDAMAN_BIN_PATH + RASQL + " --user " + username + " --passwd " + password + " -q "
                + "'"
                + orgQuery
                + "' --file " + filePath;
        log.info("Executing " + query);

        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", query});
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        String response = "";
        while ((s = stdError.readLine()) != null) {
            response += s + "\n";
        }
        if (!response.isEmpty()) {
            //error occured
            throw new RasdamanException(response);
        }
        
        long end = System.currentTimeMillis();
        long totalTime = end - start;
        log.debug("rasql query executed in " + String.valueOf(totalTime) + " ms.");
    }

    /**
     * Update collection from file as slice by WCST
     *
     * @param query
     * @throws RasdamanException
     */
    public static void executeUpdateFileStatement(String query) throws PetascopeException {
        // This needs to run with open transaction and rasadmin permission
        executeRasqlQuery(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
    }

    /**
     * Check if a rasql query is "select" query
     *
     * @param query
     * @return
     */
    public static boolean isSelectQuery(String query) {
        query = query.toLowerCase().trim();
        // e.g: select dbinfo(c) from mr as c

        if (query.startsWith("select") && !query.contains(" into ")) {
            return true;
        }
        return false;
    }

    /**
     * Check if the query contains decode() or inv_* to read data from file
     */
    public static boolean isDecodeQuery(String query) {
        query = query.toLowerCase().trim();
        // e.g: insert into (mr) values decode($1)

        String patternTemplate = "(%s|%s)(.*?)(%s|%s(.*?))(.*?)";
        String patternStr = String.format(patternTemplate, RasConstants.RASQL_INSERT.toLowerCase(), RasConstants.RASQL_UPDATE.toLowerCase(),
                RasConstants.RASQL_DECODE.toLowerCase(), RasConstants.RASQL_INV.toLowerCase());

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(query);
        String value = null;

        while (matcher.find()) {
            value = matcher.group(1);
        }

        if (value != null) {
            return true;
        }
        return false;
    }

    /**
     * Parse the domainIntervals (e.g: [0:20,0:30,10] to list of pairs
     * (lowerBound, upperBound)
     */
    public static List<Pair<Long, Long>> parseDomainIntervals(String domainIntervals) {
        List<Pair<Long, Long>> results = new ArrayList<>();
        String extracted = domainIntervals.substring(domainIntervals.indexOf(RASQL_OPEN_SUBSETS) + 1,
                                                     domainIntervals.indexOf(RASQL_CLOSE_SUBSETS));
        String[] values = extracted.split(",");
        for (String value : values) {
            String[] lowerUpperBounds = value.split(RASQL_BOUND_SEPARATION);
            Long lowerBound = null, upperBound = null;
            // Slicing
            if (lowerUpperBounds.length == 1) {
                lowerBound = new Long(lowerUpperBounds[0]);
                upperBound = lowerBound;
            } else {
                // Trimming
                lowerBound = new Long(lowerUpperBounds[0]);
                upperBound = new Long(lowerUpperBounds[1]);
            }

            results.add(new Pair(lowerBound, upperBound));
        }

        return results;
    }

    /**
     * Run a rasql query and return results as array of bytes
     */
    public static byte[] getRasqlResultAsBytes(String rasqlQuery) throws RasdamanException, PetascopeException {
        byte[] result = new byte[0];
        RasQueryResult res;

        res = new RasQueryResult(RasUtil.executeRasqlQuery(rasqlQuery));
        if (!res.getMdds().isEmpty() || !res.getScalars().isEmpty()) {
            for (String s : res.getScalars()) {
                result = s.getBytes(Charset.forName("UTF-8"));
            }
            for (byte[] bs : res.getMdds()) {
                result = bs;
            }
        }

        return result;
    }
    
    /**
     * Drop set/mdd/cell type from Rasdaman (if input type is not used by other stored objects).
    */
    public static void dropRasdamanType(String type) throws PetascopeException {
        String rasqlQuery = TEMPLATE_DROP_RASDAMAN_TYPE.replace(RASDAMAN_TYPE, type);
        executeRasqlQuery(rasqlQuery, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
    }
    
     /* Get the tiling information from rasql query of a collection.
     */
    public static String retrieveTilingInfo(String collectionName, long oid) throws PetascopeException {
        String query = "select dbinfo(c) from " + collectionName + " as c where oid(c) = " + oid;
        String dbinfoResult = new RasQueryResult(RasUtil.executeRasqlQuery(query)).toString();
        // Parse the result for "tiling" value, e.g:  "tiling": { "tilingScheme": "aligned", "tileSize": "4194304", "tileConfiguration": "[0:500,0:500]" }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootObj;
        String tiling = "";
        try {
            rootObj = mapper.readTree(dbinfoResult);
            JsonNode tilingObj = rootObj.get("tiling");
            JsonNode tilingSchemeObj = tilingObj.get("tilingScheme");
            JsonNode tileSizeObj = tilingObj.get("tileSize");
            JsonNode tileConfigurationObj = tilingObj.get("tileConfiguration");

            tiling = tilingSchemeObj.asText().toUpperCase() + " " + tileConfigurationObj.asText() + " tile size " + tileSizeObj.asText();
        } catch (IOException ex) {
            log.warn("Cannot parse result of query '" + query + "' as JSON object from Rasdaman. Reason: " + ex.getMessage());
        }
        
        return tiling;
    }

    private static final String TOKEN_COLLECTION_NAME = "%collectionName%";
    private static final String TOKEN_COLLECTION_TYPE = "%collectionType%";
    private static final String TEMPLATE_CREATE_COLLECTION = "CREATE COLLECTION " + TOKEN_COLLECTION_NAME + " " + TOKEN_COLLECTION_TYPE;
    private static final String TOKEN_VALUES = "%values%";
    private static final String TOKEN_TILING = "%tiling%";
    private static final String TILING_KEYWORD = "TILING";
    private static final String RASDAMAN_TYPE = "%TYPE%";
    private static final String TEMPLATE_INSERT_VALUES = "INSERT INTO " + TOKEN_COLLECTION_NAME + " VALUES " + TOKEN_VALUES + " " + TOKEN_TILING;
    private static final String TEMPLATE_SELECT_OID = "SELECT oid(" + TOKEN_COLLECTION_NAME + ") FROM " + TOKEN_COLLECTION_NAME;
    private static final String TOKEN_OID = "%oid%";
    private static final String TEMPLATE_DELETE = "DELETE FROM " + TOKEN_COLLECTION_NAME + " WHERE oid(" + TOKEN_COLLECTION_NAME + ")=" + TOKEN_OID;
    private static final String TEMPLATE_INSERT_DECODE_FILE = "INSERT INTO " + TOKEN_COLLECTION_NAME + " VALUES decode($1)" + " " + TOKEN_TILING;
    private static final String RASQL = "rasql";
    private static final String TEMPLATE_SDOM = "SELECT sdom(m) FROM " + TOKEN_COLLECTION_NAME + " m";
    private static final String TEMPLATE_DROP_COLLECTION = "DROP COLLECTION " + TOKEN_COLLECTION_NAME;
    private static final String TEMPLATE_DROP_RASDAMAN_TYPE = "DROP TYPE " + RASDAMAN_TYPE;
}
