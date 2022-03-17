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
import java.nio.charset.Charset;
import java.util.ArrayList;
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
import petascope.core.Pair;
import petascope.util.ListUtil;
import petascope.util.MIMEUtil;
import rasj.RasImplementation;
import rasj.odmg.RasBag;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.util.ras.RasConstants.RASQL_OPEN_SUBSETS;
import static petascope.util.ras.RasConstants.RASQL_CLOSE_SUBSETS;
import rasj.RasGMArray;
import rasj.RasMInterval;
import rasj.RasResultIsNoIntervalException;

/**
 * Rasdaman utility classes - execute queries, etc.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class RasUtil {

    private static final Logger log = LoggerFactory.getLogger(RasUtil.class);

    private static void closeDB(Database db) throws RasdamanException {
        if (db != null) {
            try {
                db.close();
            } catch (Exception ex) {
                throw new RasdamanException("Failed closing rasdaman db connection. Reason: " + ex.getMessage(), ex);
            }
        }
    }
    
    private static void abortTR(Transaction tr) throws RasdamanException {
        if (tr != null) {
            try {
                tr.abort();
            } catch (Exception ex) {
                throw new RasdamanException("Failed closing rasdaman transaction. Reason: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Create a RasGMArray from an array of bytes;
     */
    private static RasGMArray createRasGMArray(byte[] bytes) throws RasResultIsNoIntervalException {
        
        String mIntervals = "[0:" + (bytes.length - 1) + "]";
        // with cell length = 1 (byte)
        RasGMArray rasGMArray = new RasGMArray(new RasMInterval(mIntervals), 1);
        rasGMArray.setArray(bytes);
        rasGMArray.setObjectTypeName("GreyString");
        
        return rasGMArray;
    }
    
    /**
     * Executes a rasql query and returns result.
     */
    public static Object executeRasqlQuery(String query, String username, String password, boolean rw) throws PetascopeException {
        Object ret = executeRasqlQuery(query, username, password, rw, null);
        return ret;
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
    public static Object executeRasqlQuery(String query, String username, String password, boolean rw, RasGMArray rasGMArray) throws PetascopeException {
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
            log.error("Failed opening " + (rw ? "rw" : "ro") + " database connection to rasdaman. Reason: " + ex.getMessage());
            throw new RasdamanException(ExceptionCode.RasdamanUnavailable, ex, query);
        }

        Transaction tr = null;
        try {
            // open transaction
            tr = impl.newTransaction();
            tr.begin();
        } catch (Exception ex) {
            log.error("Failed opening " + (rw ? "rw" : "ro") + " transaction to rasdaman. Reason: " + ex.getMessage());
            closeDB(db);
            throw new RasdamanException(ExceptionCode.RasdamanUnavailable, ex, query);
        }
        
        OQLQuery q = null;
        try {
            q = impl.newOQLQuery();
            q.create(query);
            if (rasGMArray != null) {
                q.bind(rasGMArray);
            }
        } catch (Exception ex) {
            // not really supposed to ever throw an exception
            log.error("Failed creating query object: " + ex.getMessage());
            abortTR(tr);
            closeDB(db);
            throw new RasdamanException(ExceptionCode.InternalComponentError, ex, query);
        }
        
        Object ret = null;
        try {
            ret = q.execute();
            tr.commit();
        } catch (ODMGException ex) {
            abortTR(tr);
            if (ex.getMessage().contains("CREATE: Collection name exists already.") || ex.getMessage().contains("Collection already exists")) {
                throw new RasdamanCollectionExistsException(ExceptionCode.CollectionExists, query, ex);
            } else if (ex.getMessage().contains("Collection name is unknown.")) {
                throw new RasdamanCollectionDoesNotExistException(ExceptionCode.CollectionDoesNotExist, query, ex);
            } else {
                throw new RasdamanException(ExceptionCode.RasdamanRequestFailed,
                        ex.getMessage(), ex, query);
            }
        } catch (OutOfMemoryError ex) {
            abortTR(tr);
            throw new PetascopeException(ExceptionCode.OutOfMemory, "Requested more data than the server can handle at once. "
                    + "Try increasing the maximum memory allowed for Tomcat (-Xmx JVM option).");
        } catch (Exception ex) {
            abortTR(tr);
            if (ex.getMessage().contains("GRPC Exception")) {
                log.warn("Lost connection to rasdaman server.");
            } else {
                throw new RasdamanException(ExceptionCode.RasdamanRequestFailed, 
                    ex.getMessage(), ex, query);
            }
        } finally {
            closeDB(db);
        }

        final long end = System.currentTimeMillis();
        final long totalTime = end - start;
        log.info("Rasql query executed in " + String.valueOf(totalTime) + " ms.");

        return ret;
    }

    /**
     * Deletes an array from rasdaman.
     */
    public static void deleteFromRasdaman(String collectionName, String username, String password) throws RasdamanException, PetascopeException {
        String query = TEMPLATE_DELETE.replaceAll(TOKEN_COLLECTION_NAME, collectionName);
        executeRasqlQuery(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        //check if there are other objects left in the collection
        log.info("Checking the number of objects left in collection " + collectionName);
        RasBag result = (RasBag) executeRasqlQuery(TEMPLATE_SDOM.replace(TOKEN_COLLECTION_NAME, collectionName), username, password, false);
        log.info("Result size is: " + String.valueOf(result.size()));
        if (result.isEmpty()) {
            //no object left, delete the collection so that the name can be reused in the future
            log.info("No objects left in the collection, dropping the collection so the name can be reused in the future.");
            executeRasqlQuery(TEMPLATE_DROP_COLLECTION.replace(TOKEN_COLLECTION_NAME, collectionName), 
                              username, password, true);
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
    public static void initializeMDD(int numberOfDimensions, int numberOfBands, 
            String collectionType, String tileSetting, String collectionName, String username, String password) throws PetascopeException {

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
        RasUtil.executeInsertStatement(collectionName, values, tileSetting, username, password);
    }
    
    /**
     * Update data from a source Rasdaman collection with grid subsets on a downscaled Rasdaman collection with grid subets.
     */
    public static void updateDownscaledCollectionFromSourceCollection(String sourceAffectedDomain, 
            String targetAffectedDomain, String sourceCollectionName, String targetDownscaledCollectionName, String username, String password) throws PetascopeException {
        
        // e.g: update test_mr1 as c set c[*:*,*:*] assign scale(d[*:*,*:*], [0:20,0:30]) from test_mr as d
        String rasqlQuery = "UPDATE " + targetDownscaledCollectionName + " as d SET d" + targetAffectedDomain 
                          + " ASSIGN SCALE(c" + sourceAffectedDomain + ", " + targetAffectedDomain + ")"
                          + " FROM " + sourceCollectionName + " as c";
        RasUtil.executeRasqlQuery(rasqlQuery, username, password, Boolean.TRUE);
    }

    /**
     * Inserts a set of values given as an array constant in rasdaman. e.g:
     * "INSERT INTO PM10_2 VALUES <[0:0,0:0,0:0] 0f> TILING ALIGNED [0:366,
     * 0:500, 0:500]"
     *
     */
    public static void executeInsertStatement(String collectionName, String values, String tiling, String username, String password) throws RasdamanException, PetascopeException {
        String tilingClause = (tiling == null || tiling.isEmpty()) ? "" : TILING_KEYWORD + " " + tiling;
        String query = TEMPLATE_INSERT_VALUES.replace(TOKEN_COLLECTION_NAME, collectionName)
                                              .replace(TOKEN_VALUES, values).replace(TOKEN_TILING, tilingClause);
        executeRasqlQuery(query, username, password, true);
    }

    /**
     * Insert an image to an existing collection by decoding file
     */
    public static void executeInsertFileStatement(String collectionName, String filePath, String mime,
                                                  String tiling, String username, String password) throws RasdamanException, PetascopeException {
        String query;
        String tilingClause = (tiling == null || tiling.isEmpty()) ? "" : TILING_KEYWORD + " " + tiling;

        query = ConfigManager.RASDAMAN_BIN_PATH + RASQL
                + " --user " + username + " --passwd " + password + " -q "
                + "'" + TEMPLATE_INSERT_DECODE_FILE.replace(TOKEN_COLLECTION_NAME, collectionName).replace(TOKEN_TILING, tilingClause) + "' --file " + filePath;
        log.info("Executing " + query);

        Process p;
        try {
            p = Runtime.getRuntime().exec(new String[] {"bash", "-c", query});
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, "Failed to run rasql query '" + query + "' with bash. Reason: " + ex.getMessage());
        }
        
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        String errorMessage = "";
        try {
            while ((s = stdError.readLine()) != null) {
                errorMessage += s + "\n";
            }
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, 
                    "Failed to get error message from InputStreamReader of rasql '" + query + "' with bash. Reason: " + ex.getMessage());
        }
        if (!errorMessage.isEmpty()) {
            //error occured
            throw new RasdamanException(ExceptionCode.InternalComponentError, errorMessage, query);
        }
    }

    /**
     * Insert/Update an image to an existing collection by using the posted
     * rasql query e.g: rasql -q 'insert into float_3d values inv_netcdf($1,
     * "vars=values")' -f "float_3d.nc" 'update mr_test1 set mr_test1 assign
     * decode($1)'
     */
    public static void executeInsertUpdateFileStatement(String orgQuery, String filePath, String username, String password) 
            throws RasdamanException, PetascopeException {
        long start = System.currentTimeMillis();
        
        String query = ConfigManager.RASDAMAN_BIN_PATH + RASQL + " --user " + username + " --passwd " + password + " -q "
                + "'"
                + orgQuery
                + "' --file " + filePath;
        log.info("Executing " + query);

        Process p;
        try {
            p = Runtime.getRuntime().exec(new String[] {"bash", "-c", query});
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, "Failed to run rasql query '" + query + "' with bash. Reason: " + ex.getMessage());
        }
        
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        String errorMessage = "";
        try {
            while ((s = stdError.readLine()) != null) {
                errorMessage += s + "\n";
            }
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, 
                    "Failed to get error message from InputStreamReader of rasql '" + query + "' with bash. Reason: " + ex.getMessage());
        }
        
        if (!errorMessage.isEmpty()) {
            //error occured
            throw new RasdamanException(ExceptionCode.InternalComponentError, errorMessage, query);
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
    public static void executeUpdateFileStatement(String query, String username, String password) throws PetascopeException {
        // This needs to run with open transaction and rasadmin permission
        executeRasqlQuery(query, username, password, true);
    }
    
    public static void executeUpdateBytesStatement(String query, byte[] bytes, String username, String password) throws PetascopeException {
        RasGMArray rasGMArray;
        try {
            rasGMArray = createRasGMArray(bytes);
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError,
                                         "Cannot create RasGMArray from an array of bytes. Reason: " + ex.getMessage(), ex);
        }
        
        executeRasqlQuery(query, username, password, true, rasGMArray);
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
    public static byte[] getRasqlResultAsBytes(String rasqlQuery, String username, String password) throws RasdamanException, PetascopeException {
        byte[] result = new byte[0];
        RasQueryResult res;

        Object object = RasUtil.executeRasqlQuery(rasqlQuery, username, password, false);
        res = new RasQueryResult(object);

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
    
    /**
     * Execute a rasql query which will return a string
     */
    public static String executeQueryToReturnString(String query, String username, String password) throws PetascopeException {
        String result = new RasQueryResult(executeRasqlQuery(query, username, password, false)).toString();
        
        return result;
    }
    
     /* Get the tiling information from rasql query of a collection.
     */
    public static String retrieveTilingInfo(String collectionName) throws PetascopeException {
        String query = "select dbinfo(c) from " + collectionName + " as c";
        String dbinfoResult = new RasQueryResult(RasUtil.executeRasqlQuery(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, false)).toString();
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
    
    /**
     * Get the sdom information of a rasdaman collection
     * e.g: return 0:20,0:30
     */
    public static String retrieveSdomInfo(String collectionName) throws PetascopeException {
        String query = "select sdom(c) from " + collectionName + " as c";
        String sdomResult = new RasQueryResult(RasUtil.executeRasqlQuery(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, false)).toString().replace("[", "").replace("]", "");
        
        return sdomResult;
    }
    
    /**
     * If query contains encode() operator, then, extract the MIME type from it
     * e.g: ... encode(c, "png") -> "png"
     */
    public static String getMimeInEncode(String query) throws PetascopeException {
        query = query.toLowerCase();
        
        String fromClause = " " + RasConstants.RASQL_FROM + " ";
        String encode = RasConstants.RASQL_ENCODE + "(";
        
        if (query.contains(fromClause) && query.replaceAll("\\s+", "").contains(encode)) {                       

            int indexOfFrom = query.indexOf(fromClause);

            String subQuery = query.substring(0, indexOfFrom).replaceAll("\\s+", "");

            String firstCharacter = "";
            String secondCharacter = "";

            String encodeFormat = "";

            if (!subQuery.contains("\"{")) {
                // Old style format
                String[] tmps = subQuery.split(",\"");

                encodeFormat = tmps[0];
                if (tmps.length >= 3) {
                    if (tmps[tmps.length - 1].contains("=")) {
                        // e.g: "nodata=0"
                        encodeFormat = tmps[tmps.length - 2];
                    } else {
                        encodeFormat = tmps[tmps.length - 1];
                    }
                } else if (tmps.length == 2) {
                    encodeFormat = tmps[1];
                }
            } else {
                // new style extra format JSON
                int index = indexOfFrom;
                for (int i = subQuery.length() - 1; i >= 0; i--) {
                    firstCharacter = String.valueOf(subQuery.charAt(i - 1));
                    secondCharacter = String.valueOf(subQuery.charAt(i));

                    String combination = firstCharacter + secondCharacter;

                    if ("\"{".equals(combination)) {
                        String newSubQuery = subQuery.substring(0, index - 3);
                        String[] tmps = newSubQuery.split(",");

                        encodeFormat = tmps[tmps.length - 1];
                        break;
                    }

                    index = i;
                }

            }

            encodeFormat = encodeFormat.replace("\"", "").replace(")", "");
            String mimeType = MIMEUtil.getMimeType(encodeFormat);
            return mimeType;
        }
        
        return null;
    }


    
    /**
     * Send a simple rasql query to rasdaman to check if username and passWord are correct.
     */
    public static boolean checkValidUserCredentials(String username, String inputPassword) throws RasdamanException {
        RasImplementation impl = new RasImplementation(ConfigManager.RASDAMAN_URL);
        try {
            impl.setUserIdentification(username, inputPassword);
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();
            boolean userNotExist = errorMessage.contains("user") && errorMessage.contains("does not exist");
            boolean passwordInCorrect = errorMessage.contains("password") && errorMessage.contains("is invalid");
            if (userNotExist || passwordInCorrect) {
                return false;
            } else {
                throw new RasdamanException("Could not check if credentials for user '" + username + "' are valid in rasdaman. Reason: " + ex.getMessage(), ex);
            }
        }
        
        return true;
    }
    
    /**
     * Get all collection names by input credentials
     */
    public static List<String> getCollectionnames(String username, String password) throws PetascopeException {
        List<String> collectionnames = new ArrayList<>();
        
        try {
            Object rasqlResult = executeRasqlQuery("LIST COLLECTIONS", username, password, false);
            RasQueryResult queryResult = new RasQueryResult(rasqlResult);              
            collectionnames = ListUtil.stol(queryResult.toString(), ",");
        } catch(Exception ex) {
            log.warn("Cannot execute LIST COLLECTIONS query. Reason: " + ex.getMessage());
            // in case LIST COLLECTIONS doesn't exist
            String query = "SELECT c from RAS_COLLECTIONNAMES as c";
            Object rasjResult = executeRasqlQuery(query, username, password, false);
            RasQueryResult queryResult = new RasQueryResult(rasjResult);
            
            for (int i = 0; i < queryResult.getMdds().size(); i++) {
                collectionnames.add(new String(queryResult.getMdds().get(i)));
            }
        }
        
        return collectionnames;
    }
    
    /**
     * For example, number of dimensions is 2, number of bands is 2 and, first band's null value is 30 and data type is character
     * and scale's target domain is [0:30,0:40]
     * returns SCALE(<[0:0,0:0] {30c,30c}>, [0:30,0:40])
     */
    public static String getScaleNullGridDomains(int numberOfDimensions, int numberOfBands, String firstBandNullValue, 
                                                 String firstBandDataType, String scaleTargetGridDomains) {
        String defaultGridDomain = "0:0";
        List<String> gridDomains = new ArrayList<>();
        for (int i = 0; i < numberOfDimensions; i++) {
            gridDomains.add(defaultGridDomain);
        }
        
        List<String> bandNullValues = new ArrayList<>();
        for (int i = 0; i < numberOfBands; i++) {
            // e.g: float -> f
            String typeSuffix = TypeResolverUtil.getTypeSuffix(firstBandDataType);
            
            // e.g: 0.3f
            bandNullValues.add(firstBandNullValue + typeSuffix);
        }
        
        String result = "SCALE( <[" + ListUtil.join(gridDomains, ",") + "] {" + ListUtil.join(bandNullValues, ",") + "}>, " + scaleTargetGridDomains + ")";
        return result;       
    }


    private static final String TOKEN_COLLECTION_NAME = "%collectionName%";
    private static final String TOKEN_COLLECTION_TYPE = "%collectionType%";
    private static final String TEMPLATE_CREATE_COLLECTION = "CREATE COLLECTION " + TOKEN_COLLECTION_NAME + " " + TOKEN_COLLECTION_TYPE;
    private static final String TOKEN_VALUES = "%values%";
    private static final String TOKEN_TILING = "%tiling%";
    private static final String TILING_KEYWORD = "TILING";
    private static final String RASDAMAN_TYPE = "%TYPE%";
    private static final String TEMPLATE_INSERT_VALUES = "INSERT INTO " + TOKEN_COLLECTION_NAME + " VALUES " + TOKEN_VALUES + " " + TOKEN_TILING;
    private static final String TEMPLATE_DELETE = "DELETE FROM " + TOKEN_COLLECTION_NAME;
    private static final String TEMPLATE_INSERT_DECODE_FILE = "INSERT INTO " + TOKEN_COLLECTION_NAME + " VALUES decode($1)" + " " + TOKEN_TILING;
    private static final String RASQL = "rasql";
    private static final String TEMPLATE_SDOM = "SELECT sdom(m) FROM " + TOKEN_COLLECTION_NAME + " m";
    private static final String TEMPLATE_DROP_COLLECTION = "DROP COLLECTION " + TOKEN_COLLECTION_NAME;
    private static final String TEMPLATE_DROP_RASDAMAN_TYPE = "DROP TYPE " + RASDAMAN_TYPE;
}
