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

package petascope.wms2.service.base;

import com.sun.istack.Nullable;
import org.jetbrains.annotations.NotNull;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInvalidServiceException;
import petascope.wms2.service.exception.error.WMSInvalidVersionException;

/**
 * Basic class for validation of requests to a WMS service.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class ServiceValidator implements Validator<Request> {

    /**
     * Constructor for the class
     *
     * @param version     the version of the service for which we are validating
     * @param serviceName the name of the service for which we are validating
     */
    public ServiceValidator(String version, String serviceName) {
        this.version = version;
        this.serviceName = serviceName;
    }

    /**
     * Validates the request and throws the corresponding WMS exception if invalid
     *
     * @throws WMSException
     */
    public void validate(Request request) throws WMSException {
        validateService(request);
        validateVersion(request);

    }

    /**
     * Validates the service parameter and throws the corresponding WMS exception if invalid
     *
     * @throws WMSInvalidServiceException
     */
    private void validateService(Request request) throws WMSInvalidServiceException {
        if (isNullOrNotEqual(request.getService(), serviceName)) {
            throw new WMSInvalidServiceException(request.getService());
        }
    }

    /**
     * Validates the service parameter and throws the corresponding WMS exception if invalid
     */
    private void validateVersion(Request request){
        String clientVersion = request.getVersion();
        if (clientVersion == null) {
            //THe standard does not require anymore a validation of the version
            //We should return the results even if the version is smaller or larger
        }
    }

    /**
     * Returns true if the request parameter given is null or if it is different from the config parameter
     *
     * @param requestParameter the request parameter
     * @param configParameter  the config parameter
     * @return a boolean indicator
     */
    private static boolean isNullOrNotEqual(@Nullable String requestParameter, @NotNull String configParameter) {
        return requestParameter == null || !requestParameter.equalsIgnoreCase(configParameter);
    }

    private final String version;
    private final String serviceName;
}
