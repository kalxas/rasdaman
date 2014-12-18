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
package petascope.util.ras;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import petascope.exceptions.PetascopeException;
import petascope.util.ras.TypeRegistry.TypeRegistryEntry;

/**
 * Utilities for determining the rasdaman collection type required for creating collections.
 */
public class TypeResolverUtil {

    /**
     * Guesses the rasdaman collection type from a file.
     *
     * @param filePath path to the file
     * @return the rasdaman collection type
     * @throws IOException
     */
    public static String guessCollectionTypeFromFile(String filePath) throws IOException, PetascopeException {
        Pair<Integer, ArrayList<String>> dimTypes = Gdalinfo.getDimensionAndTypes(filePath);
        return guessCollectionType(dimTypes.getKey(), dimTypes.getValue());
    }

    /**
     * Guesses the collection type when band type is not available. It assumes
     * band type char.
     *
     * @param numberOfBands      how many band the dataset has
     * @param numberOfDimensions how many dimensions the dataset has
     * @return pair containing the collection type and cell type (e.g. <"GreySet", "c">)
     */
    public static Pair<String, Character> guessCollectionType(Integer numberOfBands, Integer numberOfDimensions) throws PetascopeException {
        String assumedType = GDT_Byte;
        //assume band type char on every band
        ArrayList<String> bandTypes = new ArrayList<String>();
        for (Integer i = 0; i < numberOfBands; i++) {
            bandTypes.add(assumedType);
        }
        return Pair.of(guessCollectionType(numberOfDimensions, bandTypes), GDAL_TYPES_TO_RAS_TYPES.get(assumedType).charAt(0));
    }

    /**
     * Returns the mdd type for a give collection type.
     *
     * @param collectionType the collection type.
     * @return the mdd type, empty if nothing is found.
     */
    public static String getMddTypeForCollectionType(String collectionType) throws PetascopeException {
        String ret = "";
        TypeRegistry typeRegistry = TypeRegistry.getInstance();
        ret = typeRegistry.getMddTypeForCollectionType(collectionType);
        return ret;
    }


    /**
     * Guesses the collection type. If no type is found, a new one is created.
     *
     * @param numberOfDimensions
     * @param gdalBandTypes
     * @return
     */
    private static String guessCollectionType(Integer numberOfDimensions, ArrayList<String> gdalBandTypes) throws PetascopeException {
        String result = "";

        //get the type registry
        TypeRegistry typeRegistry = TypeRegistry.getInstance();
        for (Map.Entry<String, TypeRegistryEntry> i : typeRegistry.getTypeRegistry().entrySet()) {
            //filter by dimensionality
            String domainType = i.getValue().getDomainType();
            if (domainType.contains(numberOfDimensions.toString())) {
                //get the structured base type
                String baseType;
                if (domainType.contains("struct")) {
                    //more bands, check for each of them
                    String baseTypeArr = domainType.split("}")[0];
                    baseType = baseTypeArr.split("\\{")[1];
                    //we are left with something like char red, char green, char blue
                    String[] baseTypeByBand = baseType.split(",");
                    //filter by number of bands
                    if (baseTypeByBand.length == gdalBandTypes.size()) {
                        Boolean allBandsMatch = true;
                        //compare band by band
                        for (Integer j = 0; j < baseTypeByBand.length; j++) {
                            if (!baseTypeByBand[j].contains(GDAL_TYPES_TO_RAS_TYPES.get(gdalBandTypes.get(j)))) {
                                allBandsMatch = false;
                            }
                        }
                        //if all good return result
                        if (allBandsMatch) {
                            return i.getKey();
                        }
                    }
                } else {
                    //1 band
                    baseType = i.getValue().getBaseType();
                    if (baseType.equals(GDAL_TYPES_TO_RAS_TYPES.get(gdalBandTypes.get(0)))) {
                        return i.getKey();
                    }
                }
            }
        }
        //nothing has been found, so the type must be created
        result = typeRegistry.createNewType(numberOfDimensions, translateTypes(gdalBandTypes));
        return result;
    }

    /**
     * Translates an array of gdal types into rasdaman types.
     *
     * @param gdalTypes
     * @return
     */
    private static ArrayList<String> translateTypes(ArrayList<String> gdalTypes) {
        ArrayList<String> rasTypes = new ArrayList<String>();
        for (String i : gdalTypes) {
            rasTypes.add(GDAL_TYPES_TO_RAS_TYPES.get(i));
        }
        return rasTypes;
    }

    //rasdaman base types
    private static final String R_Char = "char";
    private static final String R_UShort = "ushort";
    private static final String R_Short = "short";
    private static final String R_ULong = "ulong";
    private static final String R_Long = "long";
    private static final String R_Float = "float";
    private static final String R_Double = "double";

    //gdal base types
    private static final String GDT_Byte = "Byte";
    private static final String GDT_UInt16 = "UInt16";
    private static final String GDT_Int16 = "Int16";
    private static final String GDT_UInt32 = "UInt32";
    private static final String GDT_Int32 = "Int32";
    private static final String GDT_Float32 = "Float32";
    private static final String GDT_Float64 = "Float64";

    private static final HashMap<String, String> GDAL_TYPES_TO_RAS_TYPES = new HashMap<String, String>();

    static {
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Byte, R_Char);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_UInt16, R_UShort);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Int16, R_Short);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_UInt32, R_ULong);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Int32, R_Long);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Float32, R_Float);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Float64, R_Double);
    }
}
