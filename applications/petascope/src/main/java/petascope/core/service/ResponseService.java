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
package petascope.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.exceptions.WMSException;
import petascope.util.MIMEUtil;
import static petascope.wcs2.handlers.kvp.KVPWCSGetCoverageHandler.ENCODE_FORMAT;
import petascope.wcs2.handlers.kvp.KVPWCSProcessCoverageHandler;

/**
 * Service class to build the response from input request to client
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class ResponseService {
    
    private static final Logger log = LoggerFactory.getLogger(ResponseService.class);

    @Autowired
    KVPWCSProcessCoverageHandler processCoverageHandler;
    
    public ResponseService() {

    }

    /**
     * Return the output response to the client
     *
     * @param responses
     * @return
     */
    public Response buildResponse(List<Response> responses) {
        // If only 1 response so just return this one
        if (responses.size() == 1) {
            return responses.get(0);
        } else {
            // If it has multiple responses so they are multipart WCS (e.g: mediaType=multipart/related) or WCPS (e.g: coverageIds=test_mr,test_mr1)
            // NOTE: multipart result does not have mimeType and a specified coverageId (e.g: test_mr.png)
            Response response = new Response();
            List<byte[]> datas = new ArrayList<>();
            for (Response res : responses) {
                datas.addAll(res.getDatas());
            }
            response.setDatas(datas);

            return response;
        }
    }
    
    /**
     * Depend on if the request is WCS multipart request, it should add a WCPS
     * return in GML first and a WCPS return in formatType later
     *
     * wcpsQuery is a not complete query with the missing encodeFormat
     *
     * @param kvpParameters
     * @param wcpsQuery
     * @param encodeFormat
     */
    public Response handleWCPSResponse(Map<String, String[]> kvpParameters, String wcpsQuery, String encodeFormat) throws PetascopeException, WCSException, SecoreException, WMSException {
        
        Response response = null;
        // e.g: mediaType=multipart/related        
        boolean isMultipart = false;
        if (kvpParameters.get(KVPSymbols.KEY_MEDIATYPE) != null) {
            if (!kvpParameters.get(KVPSymbols.KEY_MEDIATYPE)[0].equals(KVPSymbols.VALUE_MULTIPART_RELATED)) {
                throw new WCSException(ExceptionCode.NoSuchMediaType,
                        "Mediatype value is not valid, given: " + kvpParameters.get(KVPSymbols.KEY_MEDIATYPE)[0]);
            } else {
                if (kvpParameters.get(KVPSymbols.KEY_FORMAT) != null) {
                    if (!kvpParameters.get(KVPSymbols.KEY_FORMAT)[0].equals(MIMEUtil.MIME_GML)) {
                        // NOTE: format=application/gml+xml&mediaType=multipart/related is considered as a WCS request in GML format only                
                        isMultipart = true;
                    }
                }
            }
        }

        String firstWcpsQuery, secondWcpsQuery;
        if (isMultipart == true) {
            firstWcpsQuery = wcpsQuery.replace(ENCODE_FORMAT, MIMEUtil.ENCODE_GML);
            secondWcpsQuery = wcpsQuery.replace(ENCODE_FORMAT, encodeFormat);
            log.debug("Generated a mutlipart WCPS query from WCS request: " + firstWcpsQuery);
            Response firstResponse = processCoverageHandler.processQuery(firstWcpsQuery);
            Response secondResponse = processCoverageHandler.processQuery(secondWcpsQuery);

            List<byte[]> datas = new ArrayList<>();
            datas.addAll(firstResponse.getDatas());
            datas.addAll(secondResponse.getDatas());

            // Then need to combine the byte[] data from first and second response
            response = new Response(datas, secondResponse.getFormatType(), secondResponse.getCoverageID());
        } else {
            firstWcpsQuery = wcpsQuery.replace(ENCODE_FORMAT, encodeFormat);
            log.debug("Generated a single WCPS query from WCS request: " + firstWcpsQuery);
            response = processCoverageHandler.processQuery(firstWcpsQuery);
        }

        return response;
    }
}

