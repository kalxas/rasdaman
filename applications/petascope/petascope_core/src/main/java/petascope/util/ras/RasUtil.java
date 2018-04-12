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
import org.odmg.QueryException;
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
import static petascope.util.ras.RasConstants.RASQL_VERSION;
import rasj.RasClientInternalException;
import rasj.RasConnectionFailedException;
import rasj.RasImplementation;
import rasj.odmg.RasBag;

/**
 * Rasdaman utility classes - execute queries, etc.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class RasUtil {

    private static final Logger log = LoggerFactory.getLogger(RasUtil.class);

    //Default time between re-connect attempts in seconds (If setting not found)
    private static final int DEFAULT_TIMEOUT = 5;

    //Default number of re-connect attempts  (If setting not fount)
    private static final int DEFAULT_RECONNECT_ATTEMPTS = 3;

    /**
     * Execute a RasQL query with configured credentials.
     *
     * @param query
     * @throws RasdamanException
     */
    public static Object executeRasqlQuery(String query) throws PetascopeException {
        return executeRasqlQuery(query, ConfigManager.RASDAMAN_USER, ConfigManager.RASDAMAN_PASS);
    }
    
    /**
     * Executes a RasQL query, allowing write transactions by setting the flag.
     *
     * @param query
     * @param username
     * @param password
     * @param isWriteTransaction
     * @return
     * @throws RasdamanException
     */
    public static Object executeRasqlQuery(String query, String username, String password, Boolean isWriteTransaction) throws PetascopeException {
        long start = System.currentTimeMillis();        
        log.debug("Executing rasql query: " + query);

        RasImplementation impl = new RasImplementation(ConfigManager.RASDAMAN_URL);
        impl.setUserIdentification(username, password);
        Database db = impl.newDatabase();
        int maxAttempts, timeout, attempts = 0;

        //The result of the query will be assigned to ret
        //Should allways return a result (empty result possible)
        //since a RasdamanException will be thrown in case of error
        Object ret = null;

        try {
            timeout = Integer.parseInt(ConfigManager.RASDAMAN_RETRY_TIMEOUT) * 1000;
        } catch (NumberFormatException ex) {
            timeout = DEFAULT_TIMEOUT * 1000;
            log.warn("The setting " + ConfigManager.RASDAMAN_RETRY_TIMEOUT + " is ill-defined. Assuming " + DEFAULT_TIMEOUT + " seconds between re-connect attemtps to a rasdaman server.");
            ConfigManager.RASDAMAN_RETRY_TIMEOUT = String.valueOf(DEFAULT_TIMEOUT);
        }

        try {
            maxAttempts = Integer.parseInt(ConfigManager.RASDAMAN_RETRY_ATTEMPTS);
        } catch (NumberFormatException ex) {
            maxAttempts = DEFAULT_RECONNECT_ATTEMPTS;
            log.warn("The setting " + ConfigManager.RASDAMAN_RETRY_ATTEMPTS + " is ill-defined. Assuming " + DEFAULT_RECONNECT_ATTEMPTS + " attempts to connect to a rasdaman server.");
            ConfigManager.RASDAMAN_RETRY_ATTEMPTS = String.valueOf(DEFAULT_RECONNECT_ATTEMPTS);
        }

        Transaction tr;

        //Try to connect until the maximum number of attempts is reached
        //This loop handles connection attempts to a saturated rasdaman
        //complex which will refuse the connection until a server becomes
        //available.
        boolean queryCompleted = false, dbOpened = false;
        while (!queryCompleted) {

            //Try to obtain a free rasdaman server
            try {
                int openFlag = isWriteTransaction ? Database.OPEN_READ_WRITE : Database.OPEN_READ_ONLY;
                db.open(ConfigManager.RASDAMAN_DATABASE, openFlag);
                dbOpened = true;
                tr = impl.newTransaction();
                tr.begin();
                OQLQuery q = impl.newOQLQuery();

                //A free rasdaman server was obtain, executing query
                try {
                    q.create(query);                    
                    ret = q.execute();
                    tr.commit();
                    queryCompleted = true;
                } catch (QueryException ex) {

                    //Executing a rasdaman query failed
                    tr.abort();

                    // Check if collection name exist then throw another exception
                    if (ex.getMessage().contains("CREATE: Collection name exists already.")) {
                        throw new RasdamanCollectionExistsException(ExceptionCode.CollectionExists, query, ex);
                    } else if (ex.getMessage().contains("Collection name is unknown.")) {
                        throw new RasdamanCollectionDoesNotExistException(ExceptionCode.CollectionDoesNotExist, query, ex);
                    } else {
                        throw new RasdamanException(ExceptionCode.RasdamanRequestFailed,
                                "Error evaluating rasdaman query: '" + query + "'", ex);
                    }
                } catch (Error ex) {
                    tr.abort();
                    log.error("Critical error", ex);
                    if (ex instanceof OutOfMemoryError) {
                        throw new PetascopeException(ExceptionCode.InternalComponentError, "Requested more data than the server can handle at once. "
                                + "Try increasing the maximum memory allowed for Tomcat (-Xmx JVM option).");
                    } else {
                        throw new RasdamanException(ExceptionCode.RasdamanRequestFailed, ex.getMessage());
                    }
                } finally {

                    //Done connection with rasdaman, closing database.
                    try {
                        db.close();
                    } catch (ODMGException ex) {
                        log.warn("Error closing database connection: ", ex);
                    }
                }
            } catch (RasConnectionFailedException ex) {

                //A connection with a Rasdaman server could not be established
                //retry shortly unless connection attpempts exceded the maximum
                //possible connection attempts.
                attempts++;
                if (dbOpened) {
                    try {
                        db.close();
                    } catch (ODMGException e) {
                        log.warn("Error closing database connection: ", e);
                    }
                }
                dbOpened = false;
                if (!(attempts < maxAttempts)) //Throw a RasConnectionFailedException if the connection
                //attempts exceeds the maximum connection attempts.
                {
                    throw ex;
                }

                //Sleep before trying to open another connection
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    log.error("Thread " + Thread.currentThread().getName()
                            + " was interrupted while searching a free server.");
                    throw new RasdamanException(ExceptionCode.RasdamanUnavailable,
                            "Unable to get a free rasdaman server.");
                }
            } catch (ODMGException ex) {

                //The maximum ammount of connection attempts was exceded
                //and a connection could not be established. Return
                //an exception indicating Rasdaman is unavailable.
                log.error("A Rasdaman request could not be fullfilled since no "
                        + "free Rasdaman server were available. Consider adjusting "
                        + "the values of rasdaman_retry_attempts and rasdaman_retry_timeout "
                        + "or adding more Rasdaman servers.", ex);

                throw new RasdamanException(ExceptionCode.RasdamanUnavailable,
                        "Unable to get a free rasdaman server.");
            } catch (RasClientInternalException ex) {
                //when no rasdaman servers are started, rasj throws this type of exception
                throw new RasdamanException(ExceptionCode.RasdamanUnavailable,
                        "Unable to get a free rasdaman server.");
            }

        }

        long end = System.currentTimeMillis();
        long totalTime = end - start;
        log.debug("rasql query executed in " + String.valueOf(totalTime) + " ms.");

        return ret;
    }

    /**
     * Execute a RasQL query with specified credentials, allowing only read
     * transactions.
     *
     * @param query
     * @param username
     * @param password
     * @throws RasdamanException
     */
    // FIXME - should return just String?
    public static Object executeRasqlQuery(String query, String username, String password) throws PetascopeException {
        return executeRasqlQuery(query, username, password, false);
    }

    /**
     * Fetch rasdaman version by parsing RasQL ``version()'' output.
     *
     * @return The rasdaman version
     * @throws RasdamanException
     */
    public static String getRasdamanVersion() throws RasdamanException {
        long start = System.currentTimeMillis();

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
            // rasdaman 9.4.0 on x86_64-redhat-linux ...
            version = result.split(" ")[1];
        }

        log.debug("Read rasdaman version: \"" + version + "\"");

        long end = System.currentTimeMillis();
        long totalTime = end - start;
        log.debug("rasql query executed in " + String.valueOf(totalTime) + " ms.");

        return version;
    }

    /**
     * Deletes an array from rasdaman.
     *
     * @param oid
     * @param collectionName
     * @throws RasdamanException
     */
    public static void deleteFromRasdaman(Long oid, String collectionName) throws RasdamanException, PetascopeException {
        long start = System.currentTimeMillis();

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

        long end = System.currentTimeMillis();
        long totalTime = end - start;
        log.debug("rasql query executed in " + String.valueOf(totalTime) + " ms.");
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
     * Inserts a set of values given as an array constant in rasdaman. e.g:
     * "INSERT INTO PM10_2 VALUES <[0:0,0:0,0:0] 0f> TILING ALIGNED [0:366,
     * 0:500, 0:500]"
     *
     * @param collectionName
     * @param values
     * @param tiling
     * @return the oid of the newly inserted object
     * @throws RasdamanException
     */
    public static Long executeInsertValuesStatement(String collectionName, String values, String tiling) throws RasdamanException, PetascopeException {
        long start = System.currentTimeMillis();

        Long oid = null;
        String tilingClause = (tiling == null || tiling.isEmpty()) ? "" : TILING_KEYWORD + " " + tiling;
        String query = TEMPLATE_INSERT_VALUES.replace(TOKEN_COLLECTION_NAME, collectionName)
                .replace(TOKEN_VALUES, values).replace(TOKEN_TILING, tilingClause);
        executeRasqlQuery(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
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
            BigDecimal tmp = BigDecimalUtil.stripDecimalZeros(new BigDecimal(resultInstance.toString()));
            oid = tmp.longValue();
        }

        long end = System.currentTimeMillis();
        long totalTime = end - start;
        log.debug("rasql query executed in " + String.valueOf(totalTime) + " ms.");

        return oid;
    }

    /**
     * Insert an image to an existing collection by decoding file
     *
     * @param collectionName
     * @param filePath
     * @param mime
     * @param tiling
     * @return
     * @throws petascope.rasdaman.exceptions.RasdamanException
     * @throws java.io.IOException
     */
    public static Long executeInsertFileStatement(String collectionName, String filePath, String mime,
            String tiling) throws RasdamanException, IOException, PetascopeException {
        long start = System.currentTimeMillis();
                
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
        
        long end = System.currentTimeMillis();
        long totalTime = end - start;
        log.debug("rasql query executed in " + String.valueOf(totalTime) + " ms.");
        
        return oid;
    }

    /**
     * Insert/Update an image to an existing collection by using the posted
     * rasql query e.g: rasql -q 'insert into float_3d values inv_netcdf($1,
     * "vars=values")' -f "float_3d.nc" 'update mr_test1 set mr_test1 assign
     * decode($1)'
     *
     * @param orgQuery
     * @param filePath
     * @param username
     * @param password
     * @throws petascope.rasdaman.exceptions.RasdamanException
     * @throws java.io.IOException
     */
    public static void executeInsertUpdateFileStatement(String orgQuery, String filePath, String username, String password) throws RasdamanException, IOException {
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

        long start = System.currentTimeMillis();
        // This needs to run with open transaction and rasadmin permission
        executeRasqlQuery(query, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        long end = System.currentTimeMillis();

        log.debug("Time for rasql to update collection: " + String.valueOf(end - start));
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
     *
     * @param query
     * @return
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
     *
     * @param domainIntervals
     * @return
     */
    public static List<Pair<Long, Long>> parseDomainIntervals(String domainIntervals) {
        List<Pair<Long, Long>> results = new ArrayList<Pair<Long, Long>>();
        String extracted = domainIntervals.substring(domainIntervals.indexOf("[") + 1, domainIntervals.indexOf("]"));
        String[] values = extracted.split(",");
        for (String value : values) {
            String[] lowerUpperBounds = value.split(":");
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
     *
     * @param rasqlQuery
     * @return
     * @throws petascope.rasdaman.exceptions.RasdamanException
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

    private static final String TOKEN_COLLECTION_NAME = "%collectionName%";
    private static final String TOKEN_COLLECTION_TYPE = "%collectionType%";
    private static final String TEMPLATE_CREATE_COLLECTION = "CREATE COLLECTION " + TOKEN_COLLECTION_NAME + " " + TOKEN_COLLECTION_TYPE;
    private static final String TOKEN_VALUES = "%values%";
    private static final String TOKEN_TILING = "%tiling%";
    private static final String TILING_KEYWORD = "TILING";
    private static final String TEMPLATE_INSERT_VALUES = "INSERT INTO " + TOKEN_COLLECTION_NAME + " VALUES " + TOKEN_VALUES + " " + TOKEN_TILING;
    private static final String TEMPLATE_SELECT_OID = "SELECT oid(" + TOKEN_COLLECTION_NAME + ") FROM " + TOKEN_COLLECTION_NAME;
    private static final String TOKEN_OID = "%oid%";
    private static final String TEMPLATE_DELETE = "DELETE FROM " + TOKEN_COLLECTION_NAME + " WHERE oid(" + TOKEN_COLLECTION_NAME + ")=" + TOKEN_OID;
    private static final String TEMPLATE_INSERT_DECODE_FILE = "INSERT INTO " + TOKEN_COLLECTION_NAME + " VALUES decode($1)" + " " + TOKEN_TILING;
    private static final String RASQL = "rasql";
    private static final String TEMPLATE_SDOM = "SELECT sdom(m) FROM " + TOKEN_COLLECTION_NAME + " m";
    private static final String TEMPLATE_DROP_COLLECTION = "DROP COLLECTION " + TOKEN_COLLECTION_NAME;
}
