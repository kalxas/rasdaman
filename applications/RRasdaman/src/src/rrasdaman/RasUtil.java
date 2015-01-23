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
package rrasdaman;

import org.odmg.DBag;
import org.odmg.Database;
import org.odmg.OQLQuery;
import org.odmg.Transaction;
import rasj.*;
import rasj.global.RasGlobalDefs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RasUtil {
    public static Object[] parseArray(RasGMArray array) throws RasException {
        ByteBuffer buffer = ByteBuffer.wrap(array.getArray());
        buffer.order(ByteOrder.BIG_ENDIAN);
        RasBaseType type = array.getBaseTypeSchema();
        int size = (int) (array.getArraySize() / array.getTypeLength());
        return parseArray(buffer, size, type);
    }

    public static void main(String[] args) {
        /*
        try {
            RasImplementation impl = new RasImplementation("http://" + "localhost" + ":" + 7001);
            impl.setUserIdentification("rasadmin", "rasadmin");
            Database connection = impl.newDatabase();
            connection.open("RASBASE", Database.OPEN_READ_ONLY);
            Transaction transaction = impl.newTransaction();
            transaction.begin();

            OQLQuery q = impl.newOQLQuery();
            q.create("select x[0,0] from rgb as x");

            DBag db = (DBag) q.execute();
            for (Object o1: db) {
                RasStructure rs = (RasStructure) o1;
                RasStructureType rst = rs.getType();
                System.out.println("Structure size: " + rst.getSize());
                System.out.println("Number of attributes: " + rst.getAttributes().length);
                for (int i = 0; i < rst.getAttributes().length; ++i) {
                    System.out.println("Attribute " + i + ": " + rst.getAttributes()[i] + " "
                            + rst.getBaseTypes()[i]);
                }
            }

            transaction.abort();
            connection.close();
        } catch (Exception e) {

        }
        */
    }

    public static Object[] parseArray(ByteBuffer buffer, int size, RasBaseType type)
            throws  RasException {
        RasBaseType[] types;
        if (type.isStructType()) {
            types = ((RasStructureType) type).getBaseTypes();
        } else {
            types = new RasBaseType[1];
            types[0] = type;
        }

        int typeSize = types.length;
        Object[] result = new Object[typeSize];

        for (int i = 0; i < typeSize; ++i) {
            switch (types[i].getTypeID()) {
                case RasGlobalDefs.RAS_BYTE:
                case RasGlobalDefs.RAS_CHAR:
                    result[i] = new short[size];
                    break;
                case RasGlobalDefs.RAS_BOOLEAN:
                    result[i] = new boolean[size];
                    break;
                case RasGlobalDefs.RAS_INT:
                case RasGlobalDefs.RAS_LONG:
                case RasGlobalDefs.RAS_USHORT:
                    result[i] = new int[size];
                    break;
                case RasGlobalDefs.RAS_ULONG:
                    result[i] = new long[size];
                    break;
                case RasGlobalDefs.RAS_SHORT:
                    result[i] = new short[size];
                    break;
                case RasGlobalDefs.RAS_FLOAT:
                    result[i] = new float[size];
                    break;
                case RasGlobalDefs.RAS_DOUBLE:
                    result[i] = new double[size];
                    break;
                default:
                    throw new RasException("Unknown base type:\n" + type);
            }
        }

        for (int j = 0; j < size; ++j) {
            for (int i = 0; i < typeSize; ++i) {
                switch (types[i].getTypeID()) {
                    case RasGlobalDefs.RAS_BYTE:
                    case RasGlobalDefs.RAS_CHAR:
                        ((short[]) result[i])[j] = (short) signedToUnsigned(buffer.get(), 8);
                        break;
                    case RasGlobalDefs.RAS_BOOLEAN:
                        ((boolean[]) result[i])[j] = (buffer.get() != 0);
                        break;
                    case RasGlobalDefs.RAS_INT:
                    case RasGlobalDefs.RAS_LONG:
                        ((int[]) result[i])[j] = buffer.getInt();
                        break;
                    case RasGlobalDefs.RAS_ULONG:
                        ((long[]) result[i])[j] = signedToUnsigned(buffer.getInt(), 32);
                        break;
                    case RasGlobalDefs.RAS_SHORT:
                        ((short[]) result[i])[j] = buffer.getShort();
                        break;
                    case RasGlobalDefs.RAS_USHORT:
                        ((int[]) result[i])[j] = (int) signedToUnsigned(buffer.getShort(), 16);
                        break;
                    case RasGlobalDefs.RAS_FLOAT:
                        ((float[]) result[i])[j] = buffer.getFloat();
                        break;
                    case RasGlobalDefs.RAS_DOUBLE:
                        ((double[]) result[i])[j] = buffer.getDouble();
                        break;
                }
            }
        }

        return result;
    }

    public static RasGMArray createArray(Object[] data, int size, RasBaseType type)
            throws RasException {
        ByteBuffer buffer = ByteBuffer.allocate((int) (size * type.getSize()));
        RasBaseType[] types;
        if (type.isStructType()) {
            types = ((RasStructureType) type).getBaseTypes();
        } else {
            types = new RasBaseType[1];
            types[0] = type;
        }
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < types.length; ++j) {
                Object array = data[j];
                switch (types[j].getTypeID()) {
                    case RasGlobalDefs.RAS_BYTE:
                    case RasGlobalDefs.RAS_CHAR:
                    case RasGlobalDefs.RAS_BOOLEAN:
                        buffer.put(((byte[]) array)[i]);
                        break;
                    case RasGlobalDefs.RAS_INT:
                    case RasGlobalDefs.RAS_LONG:
                        buffer.putInt((int) ((long[]) array)[i]);
                        break;
                    case RasGlobalDefs.RAS_ULONG:
                        buffer.putInt((int) unsignedToSigned(((long[]) array)[i], 32));
                        break;
                    case RasGlobalDefs.RAS_SHORT:
                        buffer.putShort((short) ((int[]) array)[i]);
                        break;
                    case RasGlobalDefs.RAS_USHORT:
                        buffer.putShort((short) unsignedToSigned(((int[]) array)[i], 16));
                        break;
                    case RasGlobalDefs.RAS_FLOAT:
                        buffer.putFloat(((float[]) array)[i]);
                        break;
                    case RasGlobalDefs.RAS_DOUBLE:
                        buffer.putDouble(((double[]) array)[i]);
                        break;
                    default:
                        throw new RasException("Unknown base type:\n" + type);
                }
            }
        }
        RasGMArray array = new RasGMArray();
        array.setTypeLength(type.getSize());
        array.setArray(buffer.array());
        return array;
    }

    // Parse a single element of a type; needed for array.getCell(...) method
    public static Object parseElement(ByteBuffer buffer, RasBaseType type) throws RasException {
        if (type.isStructType()) {
            RasStructureType rst = (RasStructureType) type;
            RasBaseType[] types = rst.getBaseTypes();
            int size = types.length;
            Object[] result = new Object[size];
            for (int i = 0; i < size; ++i)
                result[i] = parseElement(buffer, types[i]);
            return result;

        } else {
            switch (type.getTypeID()) {
                case RasGlobalDefs.RAS_BYTE:
                case RasGlobalDefs.RAS_CHAR:
                    return signedToUnsigned(buffer.get(), 8);
                case RasGlobalDefs.RAS_BOOLEAN:
                    return buffer.get() != 0;
                case RasGlobalDefs.RAS_INT:
                case RasGlobalDefs.RAS_LONG:
                    return buffer.getInt();
                case RasGlobalDefs.RAS_ULONG:
                    return signedToUnsigned(buffer.getInt(), 32);
                case RasGlobalDefs.RAS_SHORT:
                    return buffer.getShort();
                case RasGlobalDefs.RAS_USHORT:
                    return signedToUnsigned(buffer.getShort(), 16);
                case RasGlobalDefs.RAS_FLOAT:
                    return buffer.getFloat();
                case RasGlobalDefs.RAS_DOUBLE:
                    return buffer.getDouble();
                default:
                    throw new RasException("Unknown base type:\n" + type);
            }
        }
    }

    public static long signedToUnsigned(long value, int size) {
        return (value < 0) ? (value + (1L << size)) : value;
    }

    public static long unsignedToSigned(long value, int size) {
        long signbit = value & (1L << (size - 1));
        return (signbit == 0) ? value : (value - (1L << size));
    }
}
