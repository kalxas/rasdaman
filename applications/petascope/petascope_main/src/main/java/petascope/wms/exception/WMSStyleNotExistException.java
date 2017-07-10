/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wms.exception;

import org.jetbrains.annotations.NotNull;
import petascope.exceptions.WMSException;

/**
 * Exception to be thrown when a WMS style of a layer does not exist from
 * persistent database.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WMSStyleNotExistException extends WMSException {

    /**
     * Constructor for the class
     *
     * @param styleName
     * @param layerName
     */
    public WMSStyleNotExistException(String styleName, String layerName) {
        super(ERROR_MESSAGE.replace("$styleName", styleName).replace("$layerName", layerName));
    }

    @NotNull
    @Override
    public String getExceptionCode() {
        return EXCEPTION_CODE;
    }

    private final static String ERROR_MESSAGE = "The given style name: $styleName of a layer: $layerName does not exist in database.";
    private final static String EXCEPTION_CODE = "StyleNotExist";
}
