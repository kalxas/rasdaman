package rasj;

import rasj.*;
import rasj.global.*;

import java.util.*;

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
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/** ***********************************************************
 * <pre>
 *
 * PURPOSE:
 * This class represents all user defined structured types in the ODMG conformant
 * representation of the RasDaMan type system.
 * @version $Revision: 1.8 $
 *
 *
 *
 * COMMENTS:
 *
 * </pre>
 *********************************************************** */

public class RasStructureType extends RasBaseType {
    
    private RasBaseType[] baseTypes;
    private String[] attributes;

    public RasStructureType() {
        super();
        baseTypes = null;
        attributes = null;
    }

    public RasStructureType(String name, RasBaseType[] btyp, String[] attr) {
        super(name, 0);
        baseTypes = btyp;
        attributes = attr;

        for (int i = 0; i < baseTypes.length; i++) {
            typeSize = typeSize + baseTypes[i].getSize();
        }
        typeID = RasGlobalDefs.RAS_STRUCTURE;
    }

    public int getTypeID() {
        return RasGlobalDefs.RAS_STRUCTURE;
    }

    public RasBaseType[] getBaseTypes() {
        return baseTypes;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public boolean isStructType() {
        return true;
    }

    public String toString() {
        String s = super.toString() + "struct " + super.typeName + "\n{\n";
        for (int i = 0; i < attributes.length - 1; i++) {
            s = s + "  " + baseTypes[i] + " " + attributes[i] + ", \n";

            if (i == attributes.length - 2) {
                s = s + baseTypes[i + 1] + " " + attributes[i + 1] + "\n}\n";
            }

        }
        return s;
    }

    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof RasStructureType) {
            RasStructureType struct = (RasStructureType) obj;
            boolean stillEqual = getAttributes().length == struct.getAttributes().length &&
                                 getBaseTypes().length == struct.getBaseTypes().length &&
                                 (getName() == null ? struct.getName() == null : getName().equals(struct.getName()));
            for (int i = 0; i < getAttributes().length && stillEqual; ++i) {
                String thisAttr = getAttributes()[i];
                String structAttr = struct.getAttributes()[i];
                RasBaseType thisType = getBaseTypes()[i];
                RasBaseType structType = struct.getBaseTypes()[i];
                stillEqual = (thisAttr == null ? structAttr == null : thisAttr.equals(structAttr)) &&
                             (thisType == null ? structType == null : thisType.equals(structType));
            }
            result = stillEqual;
        }
        return result;
    }
}
