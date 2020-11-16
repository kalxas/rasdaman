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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.oapi.handlers.service;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.oapi.handlers.model.HttpErrorResponse;
import petascope.util.JSONUtil;

/**
 * Class to return the response entity for OAPI requests
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class OapiResultService {
    
    private static org.slf4j.Logger log = LoggerFactory.getLogger(OapiResultService.class);
    
    private final HttpHeaders httpHeaders = new HttpHeaders();
    
    /**
     * Return the result in JSON format
     */
    public ResponseEntity getJsonResponse(Object object) throws PetascopeException {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String objectRepresentation = JSONUtil.serializeObjectToJSONString(object);
        return new ResponseEntity(objectRepresentation, httpHeaders, HttpStatus.OK);
    }
    
    /**
     * Return the error result in JSON format
     */
    public ResponseEntity getJsonErrorResponse(HttpStatus httpStatus, String errorMessage) throws PetascopeException {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpErrorResponse httpErrorResponse = new HttpErrorResponse(httpStatus, errorMessage);
        String objectRepresentation = JSONUtil.serializeObjectToJSONString(httpErrorResponse);
        return new ResponseEntity(objectRepresentation, httpHeaders, HttpStatus.OK);
    }
    
    /**
     * Return the result in text / binary format
     */
    public ResponseEntity getDataResponse(Response response) {
        if (response.getDatas().size() == 0) {
            // empty response
            return ResponseEntity.ok("");
        } else if (response.getDatas().size() == 1) {
            // 1 response
            final HttpHeaders httpHeaders = new HttpHeaders();
            String mediaType = "text/plain";
            if (!response.getFormatType().isEmpty()) {
                mediaType = response.getFormatType();
            }
            
            httpHeaders.setContentType(MediaType.parseMediaType(mediaType));
            return new ResponseEntity(response.getDatas().get(0), httpHeaders, HttpStatus.OK);
        } else {
            //multipart, not supported currently
            return new ResponseEntity("Multipart not yet supported", HttpStatus.NOT_IMPLEMENTED);
        }
    }
    
}
