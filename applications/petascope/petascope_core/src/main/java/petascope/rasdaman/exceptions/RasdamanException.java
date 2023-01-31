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
package petascope.rasdaman.exceptions;

import org.rasdaman.config.ConfigManager;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

public class RasdamanException extends PetascopeException {
    
    private String query;

    public RasdamanException(String exceptionText, String query) {
        this(ExceptionCode.RasdamanError, exceptionText, null, query);
    }
    
    public RasdamanException(String exceptionText, Exception ex) {
        this(ExceptionCode.RasdamanError, exceptionText, ex, null);
    }

    public RasdamanException(String exceptionText, Exception ex, String query) {
        this(ExceptionCode.RasdamanError, exceptionText, ex, query);
    }

    public RasdamanException(ExceptionCode exceptionCode, String query) {
        this(exceptionCode, null, null, query);
    }

    public RasdamanException(ExceptionCode exceptionCode, Exception ex, String query) {
        this(exceptionCode, ex.getLocalizedMessage(), ex, query);
    }

    public RasdamanException(ExceptionCode exceptionCode, String exceptionText, String query) {
        this(exceptionCode, exceptionText, null, query);
    }

    public RasdamanException(ExceptionCode exceptionCode, String exceptionText, Exception ex, String query) {
        super(exceptionCode, exceptionText, ex, ConfigManager.LANGUAGE);
        this.query = query;
    }

    @Override
    public String getMessage() {
        String ret = "Failed internal rasql query";
        if (query != null)
            ret += ": " + query;
        return ret;
    }

    public String getQuery() {
        return query;
    }
    
}
