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
package petascope.wcps.server.core;


//A single component of a coverage's range. See the WCPS standard for more information.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.WcpsConstants;

public class RangeElement implements Cloneable {

    private static Logger log = LoggerFactory.getLogger(RangeElement.class);

    private String name;
    private String type;
    private String uom;

    // Overload
    public RangeElement(String name, String type, String uom) throws WCPSException {
        if ((name == null) || (type == null)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    "Invalid range element: element name and type cannot be null.");
        }

        if (name.equals("")) {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    "Invalid range element: element name cannot be empty.");
        }

        if (!(type.equals(WcpsConstants.MSG_BOOLEAN) || type.equals(WcpsConstants.MSG_CHAR) || type.equals("unsigned char")
                || type.equals(WcpsConstants.MSG_SHORT) || type.equals(WcpsConstants.MSG_UNSIGNED_SHORT) || type.equals("int")
                || type.equals(WcpsConstants.MSG_UNSIGNED_INT) || type.equals(WcpsConstants.MSG_LONG)
                || type.equals(WcpsConstants.MSG_UNSIGNED_LONG) || type.equals(WcpsConstants.MSG_FLOAT)
                || type.equals(WcpsConstants.MSG_DOUBLE) || type.equals(WcpsConstants.MSG_COMPLEX) || type.equals(WcpsConstants.MSG_COMPLEX + "2"))) {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    "Invalid range element: invalid element type:" + type);
        }

        this.name = name;
        this.type = type;
        this.uom = uom;

        log.trace(toString());
    }


    public RangeElement clone() {
        try {
            return new RangeElement(
                    new String(name),
                    new String(type),
                    new String(uom));
        } catch (WCPSException ime) {
            throw new RuntimeException("Invalid metadata while cloning RangeElement. This is a software bug in WCPS.", ime);
        }
    }


    public String getUom() {
        return uom;
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
        return type.equals(WcpsConstants.MSG_BOOLEAN);

    }

    public static boolean isBoolean(String type) {
        return type.equals(WcpsConstants.MSG_BOOLEAN);

    }

    public boolean isComplex() {
        return type.equals(WcpsConstants.MSG_COMPLEX) || type.equals(WcpsConstants.MSG_COMPLEX + "2");

    }

    public static boolean isComplex(String type) {
        return type.equals(WcpsConstants.MSG_COMPLEX) || type.equals(WcpsConstants.MSG_COMPLEX+ "2");

    }

    public boolean isIntegral() {
        return type.equals(WcpsConstants.MSG_CHAR) || type.equals(WcpsConstants.MSG_SHORT) || type.equals(WcpsConstants.MSG_UNSIGNED_SHORT)
                || type.equals(WcpsConstants.MSG_INT) || type.equals(WcpsConstants.MSG_UNSIGNED_INT) || type.equals(WcpsConstants.MSG_LONG)
                || type.equals(WcpsConstants.MSG_UNSIGNED_LONG);

    }

    public static boolean isIntegral(String type) {
        return type.equals(WcpsConstants.MSG_CHAR) || type.equals(WcpsConstants.MSG_SHORT) || type.equals(WcpsConstants.MSG_UNSIGNED_SHORT)
                || type.equals(WcpsConstants.MSG_INT) || type.equals(WcpsConstants.MSG_UNSIGNED_INT) || type.equals(WcpsConstants.MSG_LONG)
                || type.equals(WcpsConstants.MSG_UNSIGNED_LONG);

    }

    public boolean isFloating() {
        return type.equals(WcpsConstants.MSG_FLOAT) || type.equals(WcpsConstants.MSG_DOUBLE);

    }

    public static boolean isFloating(String type) {
        return type.equals(WcpsConstants.MSG_FLOAT) || type.equals(WcpsConstants.MSG_DOUBLE);

    }

    public boolean isNumeric() {
        return type.equals(WcpsConstants.MSG_CHAR) || type.equals(WcpsConstants.MSG_SHORT) || type.equals(WcpsConstants.MSG_UNSIGNED_SHORT)
                || type.equals(WcpsConstants.MSG_INT) || type.equals(WcpsConstants.MSG_UNSIGNED_INT) || type.equals(WcpsConstants.MSG_LONG)
                || type.equals(WcpsConstants.MSG_UNSIGNED_LONG) || type.equals(WcpsConstants.MSG_FLOAT)
                || type.equals(WcpsConstants.MSG_DOUBLE) || type.equals(WcpsConstants.MSG_COMPLEX)
                || type.equals(WcpsConstants.MSG_COMPLEX + "2");

    }

    public static boolean isNumeric(String type) {
        return type.equals(WcpsConstants.MSG_CHAR) || type.equals(WcpsConstants.MSG_SHORT) || type.equals(WcpsConstants.MSG_UNSIGNED_SHORT)
                || type.equals(WcpsConstants.MSG_INT) || type.equals(WcpsConstants.MSG_UNSIGNED_INT) || type.equals(WcpsConstants.MSG_LONG)
                || type.equals(WcpsConstants.MSG_UNSIGNED_LONG) || type.equals(WcpsConstants.MSG_FLOAT)
                || type.equals(WcpsConstants.MSG_DOUBLE) || type.equals(WcpsConstants.MSG_COMPLEX)
                || type.equals(WcpsConstants.MSG_COMPLEX + "2");

    }

    public void setType(String type) throws WCPSException {
        if (!(type.equals(WcpsConstants.MSG_BOOLEAN) || type.equals(WcpsConstants.MSG_CHAR) || type.equals("unsigned char")
                || type.equals(WcpsConstants.MSG_SHORT) || type.equals(WcpsConstants.MSG_UNSIGNED_SHORT) || type.equals("int")
                || type.equals(WcpsConstants.MSG_UNSIGNED_INT) || type.equals(WcpsConstants.MSG_LONG)
                || type.equals(WcpsConstants.MSG_UNSIGNED_LONG) || type.equals(WcpsConstants.MSG_FLOAT)
                || type.equals(WcpsConstants.MSG_DOUBLE) || type.equals(WcpsConstants.MSG_COMPLEX) || type.equals(WcpsConstants.MSG_COMPLEX + "2"))) {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    "Invalid range element: invalid element type:" + type);
        }

        this.type = type;

    }

    public String toString() {
        String r = "Range Element { name '" + name + "', type '" + type + "'}";
        return r;
    }
}
