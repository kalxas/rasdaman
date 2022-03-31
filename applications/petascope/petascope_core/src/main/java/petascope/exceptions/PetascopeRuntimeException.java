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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.exceptions;

import org.rasdaman.config.VersionManager;

/**
 * Wrapper class, used only inside inner class
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class PetascopeRuntimeException extends RuntimeException {
    private Exception exception;
    
    // default is 2.0.1
    private String version = VersionManager.WCS_VERSION_20;

    public PetascopeRuntimeException(String version, Exception exception) {
        if (version != null) {
            this.version = version;
        }
        this.exception = exception;
    }

    public String getVersion() {
        return version;
    }

    public Exception getException() {
        return exception;
    }
}
