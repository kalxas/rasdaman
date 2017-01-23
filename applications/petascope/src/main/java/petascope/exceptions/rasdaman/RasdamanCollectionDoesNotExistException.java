package petascope.exceptions.rasdaman;

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
import petascope.exceptions.ExceptionCode;

/**
 *
 * Exception is thrown when collection does not exist
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 * 
 */
public class RasdamanCollectionDoesNotExistException extends RasdamanException {

    public RasdamanCollectionDoesNotExistException(ExceptionCode exceptionCode, String query, Exception ex) {
        super(exceptionCode, EXCEPTION_TEXT.replace("$query", query), ex);
    }

    public static final String EXCEPTION_TEXT = "Error: collection name does not exist in rasql query: $query";

}
