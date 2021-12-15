package rasj;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.lang.*;
import org.odmg.DList;
import rasj.odmg.*;
import rasj.global.RasGlobalDefs;
import static rasj.global.RasGlobalDefs.*;

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
/**
 **
 * *********************************************************
 * <pre>
 *
 * PURPOSE:
 * This class represents an struct datatype.
 *
 *
 *
 * COMMENTS:
 *
 * </pre> **********************************************************
 */
public class RasStructure {

    private DList elements;
    private RasStructureType type;

    /**
     * Constructor getting the type and input stream from which to read the
     * values.
       *
     */
    public RasStructure(RasStructureType type, DataInputStream dis)
    throws IOException, RasResultIsNoIntervalException {
        this.type = type;
        this.elements = new RasList();

        RasBaseType[] bts = type.getBaseTypes();
        for (RasBaseType bt : bts) {
            if (bt != null) {
                elements.add(getElement(dis, bt, null));
            }
        }
    }

    public static Object getElement(DataInputStream dis, RasType et, byte[] binData) throws IOException, RasResultIsNoIntervalException {
        Object ret = null;
        switch (et.getTypeID()) {
        case RasGlobalDefs.RAS_MINTERVAL:
            ret = new RasMInterval(new String(binData));
            break;
        case RasGlobalDefs.RAS_SINTERVAL:
            ret = new RasSInterval(new String(binData));
            break;
        case RasGlobalDefs.RAS_POINT:
            ret = new RasPoint(new String(binData));
            break;
        case RasGlobalDefs.RAS_OID:
            ret = new RasOID(new String(binData));
            break;
        case RAS_BOOLEAN:
        case RAS_CHAR:
            ret = dis.readUnsignedByte();
            break;
        case RAS_BYTE:
            ret = dis.readByte();
            break;
        case RAS_DOUBLE:
            double d = dis.readDouble();
            ret = new Double(d);
            break;
        case RAS_FLOAT:
            float f = dis.readFloat();
            ret = new Float(f);
            break;
        case RAS_ULONG:
            byte[] bu = new byte[8];
            bu[0] = 0;
            bu[1] = 0;
            bu[2] = 0;
            bu[3] = 0;
            bu[4] = dis.readByte();
            bu[5] = dis.readByte();
            bu[6] = dis.readByte();
            bu[7] = dis.readByte();
            ByteArrayInputStream bis2 = new ByteArrayInputStream(bu);
            DataInputStream dis2 = new DataInputStream(bis2);
            long ul = dis2.readLong();
            ret = new Long(ul);
            break;
        case RAS_LONG:
        case RAS_INT:
            int i = dis.readInt();
            ret = new Integer(i);
            break;
        case RAS_USHORT:
            int j = dis.readUnsignedShort();
            ret = new Integer(j);
            break;
        case RAS_SHORT:
            ret = new Short(dis.readShort());
            break;
        case RAS_STRUCTURE:
        case RAS_RGB:
            RasStructureType st = (RasStructureType) et;
            ret = new RasStructure(st, dis);
            break;
        default:
            throw new RasTypeNotSupportedException(et + " as ElementType ");
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("{");
        int i = 0;
        for (Object element : elements) {
            if (element == null) {
                continue;
            }
            if (i > 0) {
                ret.append(",");
            }
            ret.append(" ").append(element.toString());
            i = 1;
        }
        ret.append(" }");
        return ret.toString();
    }

    public java.util.List getElements() {
        return elements;
    }

    public RasStructureType getType() {
        return type;
    }
}
