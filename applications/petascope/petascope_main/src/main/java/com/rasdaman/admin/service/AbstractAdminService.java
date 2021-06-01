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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package com.rasdaman.admin.service;

import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 *
 * Abstract handler class for all requests in KVP or XML
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public abstract class AbstractAdminService {
        
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AbstractAdminService.class);
    
    // A Handler can only handle 1 kind of service (rascontrol / statistics,...)
    protected String service;
    protected String request;
    
    @Autowired
    public HttpServletRequest httpServletRequest;
    
    protected void validateRequiredParameters(Map<String, String[]> kvpParameters, Set<String> validParameters) throws PetascopeException {
        for (String key : kvpParameters.keySet()) {
            if (!validParameters.contains(key.toLowerCase())) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Parameter '" + key + "' is not valid in request.");
            }
        }
    }

    public boolean canHandle(String service, String request) {
        // Handler could handle a service (rascontrol / statistic,...)
        if ((this.service != null && this.service.equals(service)) 
            || (this.request != null && this.request.equals(request))) {
            log.debug("Found the request handler: " + this.getClass().getCanonicalName());
            return true;
        }
        
        return false;
    }

    public abstract Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception;
    
}

