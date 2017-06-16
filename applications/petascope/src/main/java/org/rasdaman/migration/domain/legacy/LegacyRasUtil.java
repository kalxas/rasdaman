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
package org.rasdaman.migration.domain.legacy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.odmg.Database;
import org.odmg.ODMGException;
import org.odmg.OQLQuery;
import org.odmg.QueryException;
import org.odmg.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.migration.domain.legacy.LegacyConfigManager;
import static org.rasdaman.migration.domain.legacy.LegacyRasConstants.RASQL_VERSION;
import org.rasdaman.config.ConfigManager;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanCollectionDoesNotExistException;
import petascope.rasdaman.exceptions.RasdamanCollectionExistsException;
import petascope.rasdaman.exceptions.RasdamanException;
import rasj.RasClientInternalException;
import rasj.RasConnectionFailedException;
import rasj.RasImplementation;
import rasj.odmg.RasBag;
import org.rasdaman.migration.domain.legacy.LegacyWcpsConstants;

/**
 * Rasdaman utility classes - execute queries, etc.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class LegacyRasUtil {

    private static final Logger log = LoggerFactory.getLogger(LegacyRasUtil.class);

    //Default time between re-connect attempts in seconds (If setting not found)
    private static final int DEFAULT_TIMEOUT = 5;

    //Default number of re-connect attempts  (If setting not fount)
    private static final int DEFAULT_RECONNECT_ATTEMPTS = 3;

    // Useful patterns to extract data from the ``--out string'' RasQL output
    private static final String VERSION_PATTERN = "rasdaman (\\S+)-\\S+ .*$"; // group _1_ is version

    /**
     * Execute a RasQL query with configured credentials.
     *
     * @param query
     * @throws RasdamanException
     */
    public static Object executeRasqlQuery(String query) throws Exception {
        return executeRasqlQuery(query, LegacyConfigManager.RASDAMAN_USER, LegacyConfigManager.RASDAMAN_PASS);
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
    public static Object executeRasqlQuery(String query, String username, String password, Boolean isWriteTransaction) throws Exception {
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
                    //log.trace("Executing query {}", query);
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
    public static Object executeRasqlQuery(String query, String username, String password) throws Exception {
        return executeRasqlQuery(query, username, password, false);
    }

//    /**
//     * Convert WCPS query in abstract syntax to a rasql query. This is done as
//     * abstract -> XML -> rasql conversion.
//     *
//     * @param query WCPS query in abstract syntax
//     * @param wcps WCPS engine
//     * @return the corresponding rasql query
//     * @throws WCPSException
//     */
//    public static String abstractWCPSToRasql(String query, Wcps wcps) throws Exception {
//        if (query == null) {
//            throw new Exception("Can't convert null query");
//        }
//        log.trace("Converting abstract WCPS query\n{}", query);
//        String xmlQuery = abstractWCPStoXML(query);
//        try {
//            String rasql = xmlWCPSToRasql(xmlQuery, wcps);
//            log.debug("rasql: " + rasql);
//            return rasql;
//            //return xmlWCPSToRasql(xmlQuery, wcps);
//        } catch (Exception ex) {
//            throw ex;
//        }
//    }
//    /**
//     * Convert abstract WCPS query to XML syntax.
//     *
//     * @param query WCPS query in abstract syntax
//     * @return the same query in XML
//     * @throws WCPSException in case of error during the parsing/translation
//     */
//    public static String abstractWCPStoXML(String query) throws WCPSException {
//        String ret = null;
//        WCPSRequest request = null;
//        try {
//            CharStream cs = new ANTLRStringStream(query);
//            wcpsLexer lexer = new wcpsLexer(cs);
//            CommonTokenStream tokens = new CommonTokenStream();
//            tokens.setTokenSource(lexer);
//            wcpsParser parser = new wcpsParser(tokens);
//
//            log.trace("Parsing abstract WCPS query...");
//            wcpsParser.wcpsRequest_return rrequest = parser.wcpsRequest();
//            request = rrequest.value;
//        } catch (RecognitionException ex) {
//            throw new WCPSException(ExceptionCode.SyntaxError,
//                                    "Error parsing abstract WCPS query.", ex);
//        }
//
//        try {
//            log.trace("Converting parsed request to XML...");
//            ret = request.toXML();
//            log.debug("Done, xml query: " + ret);
//        } catch (Exception ex) {
//            throw new WCPSException(ExceptionCode.SyntaxError,
//                                    "Error translating parsed abstract WCPS query to XML format.", ex);
//        }
//        return ret;
//    }
//    /**
//     * Convert WCPS query in XML syntax to a rasql query.
//     *
//     * @param query WCPS query in XML syntax
//     * @param wcps WCPS engine
//     * @return the corresponding rasql query
//     * @throws WCPSException
//     */
//    public static String xmlWCPSToRasql(String query, Wcps wcps) throws WCPSException {
//        if (query == null) {
//            throw new WCPSException(ExceptionCode.InvalidParameterValue, "Can't convert null query");
//        }
//        log.trace("Converting XML WCPS query\n{}", query);
//        ProcessCoveragesRequest pcReq;
//        try {
//            pcReq = wcps.pcPrepare(ConfigManager.RASDAMAN_URL,
//                                   ConfigManager.RASDAMAN_DATABASE, IOUtils.toInputStream(query));
//        } catch (WCPSException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            throw new WCPSException(ExceptionCode.InternalComponentError,
//                                    "Error translating XML WCPS query to rasql - " + ex.getMessage(), ex);
//        }
//        log.trace("Resulting RasQL query: [{}] {}", pcReq.getMime(), pcReq.getRasqlQuery());
//        String ret = pcReq.getRasqlQuery();
//        return ret;
//    }
//    /**
//     * Execute a WCPS query given in abstract or XML syntax.
//     *
//     * @param query a WCPS query given in abstract syntax
//     * @param wcps WCPS engine
//     * @return result from executing query
//     * @throws WCPSException
//     * @throws RasdamanException
//     */
//    public static Object executeWcpsQuery(String query, Wcps wcps) throws WCPSException, RasdamanException {
//        if (query == null) {
//            throw new WCPSException(ExceptionCode.InvalidParameterValue, "Can't execute null query");
//        }
//        query = query.trim();
//        log.trace("Executing WCPS query: {}", query);
//        if (query.startsWith("<")) {
//            return executeXmlWcpsQuery(query, wcps);
//        } else {
//            return executeAbstractWcpsQuery(query, wcps);
//        }
//    }
//
//    /**
//     * Execute a WCPS query given in abstract syntax.
//     *
//     * @param query a WCPS query given in abstract syntax
//     * @param wcps WCPS engine
//     * @return result from executing query
//     * @throws WCPSException
//     * @throws RasdamanException
//     */
//    public static Object executeAbstractWcpsQuery(String query, Wcps wcps) throws WCPSException, RasdamanException {
//        if (query == null) {
//            throw new WCPSException(ExceptionCode.InvalidParameterValue, "Can't execute null query");
//        }
//        log.trace("Executing abstract WCPS query");
//        String rasquery = abstractWCPSToRasql(query, wcps);
//        // Check if it is a rasql query
//        if (rasquery != null && rasquery.startsWith(WcpsConstants.MSG_SELECT)) {
//            return executeRasqlQuery(abstractWCPSToRasql(query, wcps));
//        }
//        return rasquery;
//
//    }
//
//    /**
//     * Execute a WCPS query given in XML syntax.
//     *
//     * @param query a WCPS query given in XML syntax
//     * @param wcps WCPS engine
//     * @return the result from executing query
//     * @throws WCPSException
//     * @throws RasdamanException
//     */
//    public static Object executeXmlWcpsQuery(String query, Wcps wcps) throws WCPSException, RasdamanException {
//        if (query == null) {
//            throw new WCPSException(ExceptionCode.InvalidParameterValue, "Can't execute null query");
//        }
//        log.trace("Executing XML WCPS query");
//        return executeRasqlQuery(xmlWCPSToRasql(query, wcps));
//    }
    /**
     * Fetch rasdaman version by parsing RasQL ``version()'' output.
     *
     * @return The rasdaman version
     * @throws RasdamanException
     */
    public static String getRasdamanVersion() throws Exception {

        String version = "";
        Object tmpResult = null;
        try {
            tmpResult = LegacyRasUtil.executeRasqlQuery("select " + RASQL_VERSION + "()");
        } catch (Exception ex) {
            log.warn("Failed retrieving rasdaman version", ex);
            version = "9.0";
        }

        if (null != tmpResult) {
            LegacyRasQueryResult queryResult = new LegacyRasQueryResult(tmpResult);

            // Some regexp to extract the version from the whole verbose output
            Pattern p = Pattern.compile(VERSION_PATTERN);
            Matcher m = p.matcher(queryResult.toString());

            if (m.find()) {
                version = m.group(1);
            }
        }

        log.debug("Read rasdaman version: \"" + version + "\"");
        return version;
    }

    /**
     * Deletes an array from rasdaman.
     *
     * @param oid
     * @param collectionName
     * @throws RasdamanException
     */
    public static void deleteFromRasdaman(BigInteger oid, String collectionName) throws Exception {
        String query = TEMPLATE_DELETE.replaceAll(TOKEN_COLLECTION_NAME, collectionName).replace(TOKEN_OID, oid.toString());
        executeRasqlQuery(query, LegacyConfigManager.RASDAMAN_ADMIN_USER, LegacyConfigManager.RASDAMAN_ADMIN_PASS, true);
        //check if there are other objects left in the collection
        log.info("Checking the number of objects left in collection " + collectionName);
        RasBag result = (RasBag) executeRasqlQuery(TEMPLATE_SDOM.replace(TOKEN_COLLECTION_NAME, collectionName));
        log.info("Result size is: " + String.valueOf(result.size()));
        if (result.isEmpty()) {
            //no object left, delete the collection so that the name can be reused in the future
            log.info("No objects left in the collection, dropping the collection so the name can be reused in the future.");
            executeRasqlQuery(TEMPLATE_DROP_COLLECTION.replace(TOKEN_COLLECTION_NAME, collectionName), LegacyConfigManager.RASDAMAN_ADMIN_USER, LegacyConfigManager.RASDAMAN_ADMIN_PASS, true);
        }
    }

    /**
     * Creates a collection in rasdaman.
     *
     * @param collectionName
     * @param collectionType
     * @throws RasdamanException
     */
    public static void createRasdamanCollection(String collectionName, String collectionType) throws Exception {
        String query = TEMPLATE_CREATE_COLLECTION.replace(TOKEN_COLLECTION_NAME, collectionName)
                .replace(TOKEN_COLLECTION_TYPE, collectionType);
        executeRasqlQuery(query, LegacyConfigManager.RASDAMAN_ADMIN_USER, LegacyConfigManager.RASDAMAN_ADMIN_PASS, true);
    }

    /**
     * Inserts a set of values given as an array constant in rasdaman.
     *
     * @param collectionName
     * @param values
     * @return the oid of the newly inserted object
     * @throws RasdamanException
     */
    public static BigInteger executeInsertValuesStatement(String collectionName, String values, String tiling) throws Exception {
        BigInteger oid = null;
        String tilingClause = (tiling == null || tiling.isEmpty()) ? "" : TILING_KEYWORD + " " + tiling;
        String query = TEMPLATE_INSERT_VALUES.replace(TOKEN_COLLECTION_NAME, collectionName)
                .replace(TOKEN_VALUES, values).replace(TOKEN_TILING, tilingClause);
        executeRasqlQuery(query, LegacyConfigManager.RASDAMAN_ADMIN_USER, LegacyConfigManager.RASDAMAN_ADMIN_PASS, true);
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
            oid = BigDecimal.valueOf((Double) resultInstance).toBigInteger();
        }
        return oid;
    }

    /**
     * Insert an image to an existing collection by decoding file
     *
     * @param collectionName
     * @param filePath
     * @param mime
     * @param username
     * @param tiling
     * @param password
     * @return
     * @throws petascope.rasdaman.exceptions.RasdamanException
     * @throws java.io.IOException
     */
    public static BigInteger executeInsertFileStatement(String collectionName, String filePath, String mime,
            String username, String password, String tiling) throws Exception, IOException {
        BigInteger oid = new BigInteger("0");
        String query;
        String tilingClause = (tiling == null || tiling.isEmpty()) ? "" : TILING_KEYWORD + " " + tiling;

        query = LegacyConfigManager.RASDAMAN_BIN_PATH + RASQL + " --user " + username + " --passwd " + password + " -q "
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
            throw new Exception(response);
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
            oid = BigDecimal.valueOf((Double) resultInstance).toBigInteger();
        }
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
    public static void executeInsertUpdateFileStatement(String orgQuery, String filePath, String username, String password) throws Exception, IOException {
        String query;

        query = LegacyConfigManager.RASDAMAN_BIN_PATH + RASQL + " --user " + username + " --passwd " + password + " -q "
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
            throw new Exception(response);
        }
    }

    public static void executeUpdateFileStatement(String query, String filePath, String username, String password) throws IOException, Exception {
        String rasql = LegacyConfigManager.RASDAMAN_BIN_PATH + RASQL + " --user " + username + " --passwd " + password + " -q "
                + "'" + query + "' --file '" + filePath + "'";
        log.info("Executing " + rasql);
        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", rasql});
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        String response = "";
        while ((s = stdError.readLine()) != null) {
            response += s + "\n";
        }
        //check if an error exist in the response
        if (!response.isEmpty()) {
            throw new Exception(response);
        }
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

        String patternTemplate = "(%s)(.*?)(%s)(.*?)";
        String patternStr = String.format(patternTemplate, LegacyRasConstants.RASQL_SELECT.toLowerCase(), LegacyRasConstants.RASQL_FROM.toLowerCase());

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
     * Check if the query contains decode() or inv_* to read data from file
     *
     * @param query
     * @return
     */
    public static boolean isDecodeQuery(String query) {
        query = query.toLowerCase().trim();
        // e.g: insert into (mr) values decode($1)

        String patternTemplate = "(%s|%s)(.*?)(%s|%s(.*?))(.*?)";
        String patternStr = String.format(patternTemplate, LegacyRasConstants.RASQL_INSERT.toLowerCase(), LegacyRasConstants.RASQL_UPDATE.toLowerCase(),
                LegacyRasConstants.RASQL_DECODE.toLowerCase(), LegacyRasConstants.RASQL_INV.toLowerCase());

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
