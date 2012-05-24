/*
 * This file is part of rasdaman community.
 *
 * Secore community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Secore community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.exceptions;

import petascope.ConfigManager;

public class SecoreException extends PetascopeException {

    public SecoreException(String exceptionText) {
        this(ExceptionCode.SecoreError, exceptionText);
    }

    public SecoreException(String exceptionText, Exception ex) {
        this(ExceptionCode.SecoreError, exceptionText, ex);
    }

    public SecoreException(ExceptionCode exceptionCode) {
        this(exceptionCode, null, null);
    }

    public SecoreException(ExceptionCode exceptionCode, Exception ex) {
        this(exceptionCode, ex.getLocalizedMessage(), ex);
    }

    public SecoreException(ExceptionCode exceptionCode, String exceptionText) {
        this(exceptionCode, exceptionText, null);
    }

    public SecoreException(ExceptionCode exceptionCode, String exceptionText, Exception ex) {
        super(exceptionCode, exceptionText, ex, ConfigManager.SECORE_VERSION, ConfigManager.SECORE_LANGUAGE);
    }
}
