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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.server.core;


//A single component of a coverage's range. See the WCPS standard for more information.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.WCPSConstants;

public class RangeElement implements Cloneable {
    
    private static Logger log = LoggerFactory.getLogger(RangeElement.class);

    private String name;
    private String type;
    private String uom;

    public RangeElement(String name, String type, String uom) throws WCPSException {
        if ((name == null) || (type == null)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    WCPSConstants.ERRTXT_INVALID_RANGE_TYPE_P1 + 
                    WCPSConstants.ERRTXT_INVALID_RANGE_TYPE_P2);
        }

        if (name.equals("")) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    WCPSConstants.ERRTXT_INVALID_RANGE_TYPE_P1 + 
                    WCPSConstants.ERRTXT_INVALID_RANGE_TYPE_P3);
        }

        if (!(type.equals(WCPSConstants.MSG_BOOLEAN) || type.equals(WCPSConstants.MSG_CHAR) || type.equals("unsigned char")
                || type.equals(WCPSConstants.MSG_SHORT) || type.equals(WCPSConstants.MSG_UNSIGNED_SHORT) || type.equals("int")
                || type.equals(WCPSConstants.MSG_UNSIGNED_INT) || type.equals(WCPSConstants.MSG_LONG)
                || type.equals(WCPSConstants.MSG_UNSIGNED_LONG) || type.equals(WCPSConstants.MSG_FLOAT)
                || type.equals(WCPSConstants.MSG_DOUBLE) || type.equals(WCPSConstants.MSG_COMPLEX) || type.equals(WCPSConstants.MSG_COMPLEX + "2"))) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    WCPSConstants.ERRTXT_INVALID_RANGE_TYPE_P1 + 
                    WCPSConstants.ERRTXT_INVALID_RANGE_TYPE_P4 + type);
        }

        this.name = name;
        this.type = type;
        this.uom = uom;
        
        log.trace(toString());
    }

    public String getUom() {
        return uom;
    }

    public RangeElement clone() {
        try {
            return new RangeElement(new String(name), new String(type), new String(uom));
        } catch (WCPSException ime) {
            throw new RuntimeException(
                    WCPSConstants.ERRTXT_INVALID_METADATA_CLONING_RAN,
                    ime);
        }

    }

    public boolean equals(RangeElement re) {
        return name.equals(re.type);

    }

    public String getName() {
        return name;

    }

    public String getType() {
        return type;

    }

    public boolean isBoolean() {
        return type.equals(WCPSConstants.MSG_BOOLEAN);

    }

    public static boolean isBoolean(String type) {
        return type.equals(WCPSConstants.MSG_BOOLEAN);

    }

    public boolean isComplex() {
        return type.equals(WCPSConstants.MSG_COMPLEX) || type.equals(WCPSConstants.MSG_COMPLEX + "2");

    }

    public static boolean isComplex(String type) {
        return type.equals(WCPSConstants.MSG_COMPLEX) || type.equals(WCPSConstants.MSG_COMPLEX+ "2");

    }

    public boolean isIntegral() {
        return type.equals(WCPSConstants.MSG_CHAR) || type.equals(WCPSConstants.MSG_SHORT) || type.equals(WCPSConstants.MSG_UNSIGNED_SHORT)
                || type.equals(WCPSConstants.MSG_INT) || type.equals(WCPSConstants.MSG_UNSIGNED_INT) || type.equals(WCPSConstants.MSG_LONG)
                || type.equals(WCPSConstants.MSG_UNSIGNED_LONG);

    }

    public static boolean isIntegral(String type) {
        return type.equals(WCPSConstants.MSG_CHAR) || type.equals(WCPSConstants.MSG_SHORT) || type.equals(WCPSConstants.MSG_UNSIGNED_SHORT)
                || type.equals(WCPSConstants.MSG_INT) || type.equals(WCPSConstants.MSG_UNSIGNED_INT) || type.equals(WCPSConstants.MSG_LONG)
                || type.equals(WCPSConstants.MSG_UNSIGNED_LONG);

    }

    public boolean isFloating() {
        return type.equals(WCPSConstants.MSG_FLOAT) || type.equals(WCPSConstants.MSG_DOUBLE);

    }

    public static boolean isFloating(String type) {
        return type.equals(WCPSConstants.MSG_FLOAT) || type.equals(WCPSConstants.MSG_DOUBLE);

    }

    public boolean isNumeric() {
        return type.equals(WCPSConstants.MSG_CHAR) || type.equals(WCPSConstants.MSG_SHORT) || type.equals(WCPSConstants.MSG_UNSIGNED_SHORT)
                || type.equals(WCPSConstants.MSG_INT) || type.equals(WCPSConstants.MSG_UNSIGNED_INT) || type.equals(WCPSConstants.MSG_LONG)
                || type.equals(WCPSConstants.MSG_UNSIGNED_LONG) || type.equals(WCPSConstants.MSG_FLOAT)
                || type.equals(WCPSConstants.MSG_DOUBLE) || type.equals(WCPSConstants.MSG_COMPLEX)
                || type.equals(WCPSConstants.MSG_COMPLEX + "2");

    }

    public static boolean isNumeric(String type) {
        return type.equals(WCPSConstants.MSG_CHAR) || type.equals(WCPSConstants.MSG_SHORT) || type.equals(WCPSConstants.MSG_UNSIGNED_SHORT)
                || type.equals(WCPSConstants.MSG_INT) || type.equals(WCPSConstants.MSG_UNSIGNED_INT) || type.equals(WCPSConstants.MSG_LONG)
                || type.equals(WCPSConstants.MSG_UNSIGNED_LONG) || type.equals(WCPSConstants.MSG_FLOAT)
                || type.equals(WCPSConstants.MSG_DOUBLE) || type.equals(WCPSConstants.MSG_COMPLEX)
                || type.equals(WCPSConstants.MSG_COMPLEX + "2");

    }

    public void setType(String type) throws WCPSException {
        if (!(type.equals(WCPSConstants.MSG_BOOLEAN) || type.equals(WCPSConstants.MSG_CHAR) || type.equals("unsigned char")
                || type.equals(WCPSConstants.MSG_SHORT) || type.equals(WCPSConstants.MSG_UNSIGNED_SHORT) || type.equals("int")
                || type.equals(WCPSConstants.MSG_UNSIGNED_INT) || type.equals(WCPSConstants.MSG_LONG)
                || type.equals(WCPSConstants.MSG_UNSIGNED_LONG) || type.equals(WCPSConstants.MSG_FLOAT)
                || type.equals(WCPSConstants.MSG_DOUBLE) || type.equals(WCPSConstants.MSG_COMPLEX) || type.equals(WCPSConstants.MSG_COMPLEX + "2"))) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    WCPSConstants.ERRTXT_INVALID_RANGE_TYPE_P1 + 
                    WCPSConstants.ERRTXT_INVALID_RANGE_TYPE_P4 + type);
        }

        this.type = type;

    }

    public String toString() {
        String r = WCPSConstants.MSG_RANGE_ELEMENT + " { " + WCPSConstants.MSG_NAME + " '" + name + "', " + WCPSConstants.MSG_TYPE + " '" + type + "'}";
        return r;
    }
}
