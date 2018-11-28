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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.rasql.handlers.kvp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import petascope.core.response.Response;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import static petascope.core.KVPSymbols.KEY_PASSWORD;
import static petascope.core.KVPSymbols.KEY_QUERY;
import static petascope.core.KVPSymbols.KEY_UPLOADED_FILE_VALUE;
import static petascope.core.KVPSymbols.KEY_USERNAME;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WMSException;
import petascope.ihandlers.kvp.IKVPHandler;
import petascope.util.MIMEUtil;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;

/**
 * Class which handle WCS 2.0.1 DescribeCoverage request NOTE: 1 coverage can
 * have multiple coverageIds e.g: coverageIds=test_mr,test_irr_cube_2 the XML
 * result is concatenated from both GML results.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPRasqlHandler implements IKVPHandler {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(KVPRasqlHandler.class);

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        String[] userName = kvpParameters.get(KVPSymbols.KEY_USERNAME);
        String[] password = kvpParameters.get(KVPSymbols.KEY_PASSWORD);
        String[] query = kvpParameters.get(KVPSymbols.KEY_QUERY);

        if (userName == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing required parameter: " + KEY_USERNAME + ".");
        } else if (password == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing required parameter: " + KEY_PASSWORD + ".");
        } else if (query == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing required parameter: " + KEY_QUERY + ".");
        }
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        // Validate before handling request
        this.validate(kvpParameters);

        String userName = kvpParameters.get(KEY_USERNAME)[0];
        String password = kvpParameters.get(KEY_PASSWORD)[0];
        String query = kvpParameters.get(KEY_QUERY)[0];

        // check if user wants to upload file to server by find decode() or inv_*() in the requested query
        String filePath = kvpParameters.get(KEY_UPLOADED_FILE_VALUE) == null ? null : kvpParameters.get(KEY_UPLOADED_FILE_VALUE)[0];

        // select, delete, update without decode()
        Response response = this.executeQuery(userName, password, query, filePath);

        return response;
    }

    /**
     * Return the value of the requested key
     *
     * @param key
     * @return
     */
    private String getValue(Map<String, String[]> kvpParameters, String key) throws PetascopeException {
        String values[] = kvpParameters.get(key);
        if (values == null) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Required KVP parameter: " + key + " missing from the request.");
        }

        return values[0];
    }

    /**
     * Execute rasql query.
     *
     * @param username rasdaman username
     * @param password rasdaman password
     * @param query rasql query to execute
     * @throws PetascopeException in case of error in query evaluation
     */
    private Response executeQuery(String username, String password, String query, String filePath) throws PetascopeException {
        Response response = new Response();
        response.setFormatType(MIMEUtil.MIME_TEXT);
        try {
            Object tmpResult = null;

            if (filePath == null) {
                // no decode() or inv_*() in rasql query
                if (RasUtil.isSelectQuery(query)) {
                    // no need to open transaction with "select" query
                    tmpResult = RasUtil.executeRasqlQuery(query, username, password, false);
                } else {
                    // drop, delete need to open transacation
                    tmpResult = RasUtil.executeRasqlQuery(query, username, password, true);
                }
            } else {
                // decode() or inv_*() in rasql query
                RasUtil.executeInsertUpdateFileStatement(query, filePath, username, password);
            }
            RasQueryResult queryResult = new RasQueryResult(tmpResult);

            byte[] bytes = null;
            if (!queryResult.getScalars().isEmpty()) {
                bytes = queryResult.getScalars().get(0).getBytes();
            } else if (!queryResult.getMdds().isEmpty()) {
                for (byte[] byteData : queryResult.getMdds()) {
                    bytes = ArrayUtils.addAll(bytes, byteData);
                }
            }
            
            // If no result is returned from rasdaman, nothing to write to client
            if (bytes != null) {
                response.setDatas(Arrays.asList(bytes));
            }
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.IOConnectionError,
                    "Failed writing result to output stream", ex);
        } finally {
            // remove the uploaded filePath after insert/update to collection
            if (filePath != null) {
                File file = new File(filePath);
                if (file.delete()) {
                    log.debug("Removed the uploaded file: " + filePath);
                } else {
                    log.error("Failed removing uploaded file: " + filePath);
                }
            }
        }

        log.debug("Rasql query finished successfully.");

        return response;
    }

}
