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
package petascope.wcs2.handlers.kvp.service;

import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import static petascope.core.KVPSymbols.KEY_INTERPOLATION;
import petascope.wcs2.handlers.kvp.KVPWCSGetCapabilitiesHandler;

/**
 * Service class for Interpolation handler of GetCoverageKVP class
 *
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWCSGetCoverageInterpolationService {

    public KVPWCSGetCoverageInterpolationService() {

    }

    /**
     * If request with interpolation parameter, check if it is a supported
     * interpolation method
     *
     * @param interpolations
     */
    public void handleInterpolation(String[] interpolations) throws WCSException {
        if (interpolations.length > 1) {
            throw new WCSException(ExceptionCode.InvalidRequest,
                    "Multiple \"" + KEY_INTERPOLATION + "\" parameters in the request: must be unique.");
        } // if set, it must be a supported one:
        else if (!KVPWCSGetCapabilitiesHandler.supportedInterpolations.contains(interpolations[0])) {
            throw new WCSException(ExceptionCode.InterpolationMethodNotSupported, "Received interpolation URL: " + interpolations[0] + " is not supported.");
        }
    }
}
