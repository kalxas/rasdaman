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
package petascope.util.ras;

import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.WCPSConstants;
import static petascope.util.ras.TypeResolverUtil.R_Boolean;
import static petascope.util.ras.TypeResolverUtil.R_CFloat32;
import static petascope.util.ras.TypeResolverUtil.R_CFloat64;
import static petascope.util.ras.TypeResolverUtil.R_CInt16;
import static petascope.util.ras.TypeResolverUtil.R_CInt32;
import static petascope.util.ras.TypeResolverUtil.R_Char;
import static petascope.util.ras.TypeResolverUtil.R_Double;
import static petascope.util.ras.TypeResolverUtil.R_Float;
import static petascope.util.ras.TypeResolverUtil.R_Long;
import static petascope.util.ras.TypeResolverUtil.R_Octet;
import static petascope.util.ras.TypeResolverUtil.R_Short;
import static petascope.util.ras.TypeResolverUtil.R_ULong;
import static petascope.util.ras.TypeResolverUtil.R_UnsignedLong;
import static petascope.util.ras.TypeResolverUtil.R_UnsignedShort;

/**
 * Utility class to convert a petascope data type to a rasdaman data type
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CastDataTypeConverter {

    /**
     * This is only a static class so make sure no one instantiates it
     */
    private CastDataTypeConverter() {
        throw new AssertionError("This class should not be instantiated");
    }

    /**
     * Converts a petascope data type to a rasdaman data type
     *
     * @param dataTypeToBeConverted the data type to be converted
     * @return the rasdaman data type
     */
    public static String convert(String dataTypeToBeConverted) throws PetascopeException {
        String result = dataTypeToBeConverted.toLowerCase();
        if (result.equals(WCPSConstants.MSG_BOOLEAN)) {
            result = R_Boolean;
        } else if (result.equals(WCPSConstants.MSG_CHAR)) {
            result = R_Octet;
        } else if (result.equals(WCPSConstants.MSG_UNSIGNED_CHAR)) {
            result = R_Char;
            
        } else if (result.equals(WCPSConstants.MSG_SHORT)) {
            result = R_Short;
        } else if (result.equals(WCPSConstants.MSG_UNSIGNED_SHORT)) {
            result = R_UnsignedShort;
            
        } else if (result.equals(WCPSConstants.MSG_INT)) {
            result = R_Long;
        } else if (result.equals(WCPSConstants.MSG_UNSIGNED_INT)) {
            result = R_UnsignedLong;
        
        } else if (result.equals(WCPSConstants.MSG_LONG)) {
            result = R_Long;
        } else if (result.equals(WCPSConstants.MSG_UNSIGNED_LONG)) {
            result = R_UnsignedLong;
            
        } else if (result.equals(WCPSConstants.MSG_FLOAT)) {
            result = R_Float;
        } else if (result.equals(WCPSConstants.MSG_DOUBLE)) {
            result = R_Double;
        
        } else if (result.equals(WCPSConstants.MSG_COMPLEX_INT16)) {
            result = R_CInt16;
        } else if (result.equals(WCPSConstants.MSG_COMPLEX_INT32)) {
            result = R_CInt32;
            
        } else if (result.equals(WCPSConstants.MSG_COMPLEX)) {
            result = R_CFloat32;
        } else if (result.equals(WCPSConstants.MSG_COMPLEX2)) {
            result = R_CFloat64;
        } else {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Unknown WCPS base type '" + result + "' for casting.");
        }
        //short, unsigned short and complex have identity mapping
        return result;
    }
}
