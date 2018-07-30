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
package org.rasdaman.migration.domain.legacy;


//A single component of a coverage's range. See the WCPS standard for more information.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.migration.domain.legacy.LegacyWcpsConstants;

public class LegacyRangeElement implements Cloneable {

    private static Logger log = LoggerFactory.getLogger(LegacyRangeElement.class);

    private String name;
    private String type;
    private String uom;

    // Overload
    public LegacyRangeElement(String name, String type, String uom) throws Exception {
        if ((name == null) || (type == null)) {
            throw new Exception("Invalid range element: element name and type cannot be null.");
        }

        if (name.equals("")) {
            throw new Exception("Invalid range element: element name cannot be empty.");
        }

        if (!(type.equals(LegacyWcpsConstants.MSG_BOOLEAN) || type.equals(LegacyWcpsConstants.MSG_CHAR) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_CHAR)
                || type.equals(LegacyWcpsConstants.MSG_SHORT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_SHORT) || type.equals(LegacyWcpsConstants.MSG_INT)
                || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_INT) || type.equals(LegacyWcpsConstants.MSG_LONG)
                || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_LONG) || type.equals(LegacyWcpsConstants.MSG_FLOAT)
                || type.equals(LegacyWcpsConstants.MSG_DOUBLE) || type.equals(LegacyWcpsConstants.MSG_COMPLEX) || type.equals(LegacyWcpsConstants.MSG_COMPLEX + "2"))) {
            throw new Exception("Invalid range element: invalid element type:" + type);
        }

        this.name = name;
        this.type = type;
        this.uom = uom;

        //log.trace(toString());
    }


    public LegacyRangeElement clone() {
        try {
            return new LegacyRangeElement(
                       new String(name),
                       new String(type),
                       new String(uom));
        } catch (Exception ime) {
            throw new RuntimeException("Invalid metadata while cloning RangeElement. This is a software bug in WCPS.", ime);
        }
    }


    public String getUom() {
        return uom;
    }

    public boolean equals(LegacyRangeElement re) {
        return name.equals(re.type);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isBoolean() {
        return type.equals(LegacyWcpsConstants.MSG_BOOLEAN);

    }

    public static boolean isBoolean(String type) {
        return type.equals(LegacyWcpsConstants.MSG_BOOLEAN);

    }

    public boolean isComplex() {
        return type.equals(LegacyWcpsConstants.MSG_COMPLEX) || type.equals(LegacyWcpsConstants.MSG_COMPLEX + "2");

    }

    public static boolean isComplex(String type) {
        return type.equals(LegacyWcpsConstants.MSG_COMPLEX) || type.equals(LegacyWcpsConstants.MSG_COMPLEX + "2");

    }

    public boolean isIntegral() {
        return type.equals(LegacyWcpsConstants.MSG_CHAR) || type.equals(LegacyWcpsConstants.MSG_SHORT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_SHORT)
               || type.equals(LegacyWcpsConstants.MSG_INT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_INT) || type.equals(LegacyWcpsConstants.MSG_LONG)
               || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_LONG);

    }

    public static boolean isIntegral(String type) {
        return type.equals(LegacyWcpsConstants.MSG_CHAR) || type.equals(LegacyWcpsConstants.MSG_SHORT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_SHORT)
               || type.equals(LegacyWcpsConstants.MSG_INT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_INT) || type.equals(LegacyWcpsConstants.MSG_LONG)
               || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_LONG);

    }

    public boolean isFloating() {
        return type.equals(LegacyWcpsConstants.MSG_FLOAT) || type.equals(LegacyWcpsConstants.MSG_DOUBLE);

    }

    public static boolean isFloating(String type) {
        return type.equals(LegacyWcpsConstants.MSG_FLOAT) || type.equals(LegacyWcpsConstants.MSG_DOUBLE);

    }

    public boolean isNumeric() {
        return type.equals(LegacyWcpsConstants.MSG_CHAR) || type.equals(LegacyWcpsConstants.MSG_SHORT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_SHORT)
               || type.equals(LegacyWcpsConstants.MSG_INT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_INT) || type.equals(LegacyWcpsConstants.MSG_LONG)
               || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_LONG) || type.equals(LegacyWcpsConstants.MSG_FLOAT)
               || type.equals(LegacyWcpsConstants.MSG_DOUBLE) || type.equals(LegacyWcpsConstants.MSG_COMPLEX)
               || type.equals(LegacyWcpsConstants.MSG_COMPLEX + "2");

    }

    public static boolean isNumeric(String type) {
        return type.equals(LegacyWcpsConstants.MSG_CHAR) || type.equals(LegacyWcpsConstants.MSG_SHORT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_SHORT)
               || type.equals(LegacyWcpsConstants.MSG_INT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_INT) || type.equals(LegacyWcpsConstants.MSG_LONG)
               || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_LONG) || type.equals(LegacyWcpsConstants.MSG_FLOAT)
               || type.equals(LegacyWcpsConstants.MSG_DOUBLE) || type.equals(LegacyWcpsConstants.MSG_COMPLEX)
               || type.equals(LegacyWcpsConstants.MSG_COMPLEX + "2");

    }

    public void setType(String type) throws Exception {
        if (!(type.equals(LegacyWcpsConstants.MSG_BOOLEAN) || type.equals(LegacyWcpsConstants.MSG_CHAR) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_CHAR)
                || type.equals(LegacyWcpsConstants.MSG_SHORT) || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_SHORT) || type.equals(LegacyWcpsConstants.MSG_INT)
                || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_INT) || type.equals(LegacyWcpsConstants.MSG_LONG)
                || type.equals(LegacyWcpsConstants.MSG_UNSIGNED_LONG) || type.equals(LegacyWcpsConstants.MSG_FLOAT)
                || type.equals(LegacyWcpsConstants.MSG_DOUBLE) || type.equals(LegacyWcpsConstants.MSG_COMPLEX) || type.equals(LegacyWcpsConstants.MSG_COMPLEX + "2"))) {
            throw new Exception("Invalid range element: invalid element type:" + type);
        }

        this.type = type;

    }

    public String toString() {
        String r = "Range Element { name '" + name + "', type '" + type + "'}";
        return r;
    }
}
