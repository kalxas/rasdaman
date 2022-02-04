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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.exception.processing;

import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;

/**
 * Exception that is thrown when handling clipping expression. 
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class ClipExpressionException extends WCPSException {
    
    public ClipExpressionException(String errorMessage, Exception cause) {
        super(ExceptionCode.InternalComponentError, ERROR_TEMPLATE.replace("$errorMessage", errorMessage), cause);
    }
    
    private static final String ERROR_TEMPLATE = "Error processing clip() operator expression. Reason: $errorMessage";
    
}
