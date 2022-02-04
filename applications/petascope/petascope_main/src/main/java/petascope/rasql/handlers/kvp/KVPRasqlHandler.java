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
import java.util.ArrayList;
import java.util.List;
import petascope.core.response.Response;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import petascope.controller.AbstractController;
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
 * Class to handle KVP rasql requests of the format:
 * 
 * <code>query=<q>&username=<u>&password=<p></code>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPRasqlHandler implements IKVPHandler {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(KVPRasqlHandler.class);

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        checkRequiredParameter(kvpParameters, KEY_USERNAME);
        checkRequiredParameter(kvpParameters, KEY_PASSWORD);
        checkRequiredParameter(kvpParameters, KEY_QUERY);
    }

    private void checkRequiredParameter(Map<String, String[]> kvpParameters, String key) throws PetascopeException {
        String[] value = kvpParameters.get(key);
        if (value == null)
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Missing required parameter: " + key + ".");
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        // Validate before handling request
        this.validate(kvpParameters);

        String userName = AbstractController.getValueByKey(kvpParameters, KEY_USERNAME);
        String password = AbstractController.getValueByKey(kvpParameters, KEY_PASSWORD);
        String query = AbstractController.getValueByKey(kvpParameters, KEY_QUERY);

        // check if user wants to upload file to server by find decode() or inv_*() in the requested query
        String filePath = AbstractController.getValueByKeyAllowNull(kvpParameters, KEY_UPLOADED_FILE_VALUE);

        // select, delete, update without decode()
        Response response = this.executeQuery(userName, password, query, filePath);

        return response;
    }

    /**
     * Execute rasql query and return response.
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
            if (filePath == null) {
                boolean rw = !RasUtil.isSelectQuery(query);
                Object rasjResult = RasUtil.executeRasqlQuery(query, username, password, rw, null);
                response.setDatas(getResultDatas(rasjResult));
                
                String mimeType = RasUtil.getMimeInEncode(query);
                response.setFormatType(mimeType);
            } else {
                // decode() or inv_*() in rasql query, no result returned
                RasUtil.executeInsertUpdateFileStatement(query, filePath, username, password);
            }
        } finally {
            removeUploadedFile(filePath);
        }

        log.debug("Rasql query finished successfully.");
        return response;
    }
    
    private List<byte[]> getResultDatas(Object rasjResult) {
        RasQueryResult result = new RasQueryResult(rasjResult);
        if (!result.getScalars().isEmpty()) {
            List<byte[]> ret = new ArrayList<>();
            for (String res: result.getScalars())
                ret.add(res.getBytes());
            return ret;
        } else if (!result.getMdds().isEmpty()) {
            return result.getMdds();
        } else {
            return null;
        }
    }
    
    /**
     * Remove the uploaded filePath after insert/update to collection
     */
    private void removeUploadedFile(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if (file.delete()) {
                log.debug("Removed uploaded file: " + filePath);
            } else {
                log.error("Failed removing uploaded file: " + filePath);
            }
        }
    }

}
