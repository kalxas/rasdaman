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
import java.util.List;
import java.util.Map;

import org.rasdaman.domain.cis.NilValue;
import petascope.exceptions.PetascopeException;
import petascope.core.Pair;
import petascope.exceptions.WCSException;
import petascope.util.StringUtil;
import petascope.util.ras.TypeRegistry.TypeRegistryEntry;

/**
 * Utilities for determining the rasdaman collection type required for creating collections.
 */
public class TypeResolverUtil {

    /**
     * Guesses the rasdaman collection type from a file.
     *
     * @param collectionName
     * @param filePath path to the file
     * @param dimension
     * @param nullValues
     * @return the rasdaman collection type
     * @throws IOException
     */
    public static String guessCollectionTypeFromFile(String collectionName, String filePath, int dimension, List<NilValue> nullValues) throws IOException, PetascopeException {
        Pair<Integer, ArrayList<String>> dimTypes = Gdalinfo.getDimensionAndTypes(filePath);
        
        return guessCollectionType(collectionName, dimension, dimTypes.snd, nullValues);
    }

    /**
     * Guesses the collection type when band type is not available. It assumes
     * band type char.
     *
     * @param collectionName     name of creating collection (coverage Id)
     * @param numberOfBands      how many band the dataset has
     * @param numberOfDimensions how many dimensions the dataset has
     * @param nullValues
     * @param pixelDataType      the pixel data type, if not given assumed Float32
     * @return pair containing the collection type and cell type (e.g. <"GreySet", "c">)
     */
    public static Pair<String, String> guessCollectionType(String collectionName, Integer numberOfBands, Integer numberOfDimensions, List<NilValue> nullValues, String pixelDataType) throws PetascopeException {
        if (pixelDataType == null) {
            pixelDataType = GDT_Float32;
        }
        if (!GDAL_TYPES_TO_RAS_TYPES.containsKey(pixelDataType)) {
            throw new WCSException("Unknown pixel data type: " + pixelDataType);
        }
        //assume band type char on every band
        ArrayList<String> bandTypes = new ArrayList<>();
        for (Integer i = 0; i < numberOfBands; i++) {
            bandTypes.add(pixelDataType);
        }
        return Pair.of(guessCollectionType(collectionName, numberOfDimensions, bandTypes, nullValues), RAS_TYPES_TO_ABBREVIATION.get(GDAL_TYPES_TO_RAS_TYPES.get(pixelDataType)));
    }
    
    /**
     * From collection type, return the TypeRegistryEntry objects contains information about MDD, Cell types
     */
    public static TypeRegistryEntry getTypeRegistryEntry(String collectionType) throws PetascopeException {
        TypeRegistry typeRegistry = TypeRegistry.getInstance();
        TypeRegistryEntry entry = typeRegistry.getTypeEntry(collectionType);
        return entry;
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
    private static String guessCollectionType(String collectionName, Integer numberOfDimensions, ArrayList<String> gdalBandTypes, List<NilValue> nilValues) throws PetascopeException {
        String result = "";

        //get the type registry
        TypeRegistry typeRegistry = TypeRegistry.getInstance();
        
        for (Map.Entry<String, TypeRegistryEntry> entry : typeRegistry.getTypeRegistry().entrySet()) {
            //filter by dimensionality (e.g: struct { char band0, char band1, char band2 }, 2)
            String mdArrayType = entry.getValue().getMDArrayType();
            String mdArrayDimensions = mdArrayType.substring(mdArrayType.lastIndexOf(",") + 1);
            // existing MDD type has same dimension as input coverage (2D, 3D,...)
            if (mdArrayDimensions.equals(numberOfDimensions.toString())) {
                //get the structured base type
                String cellType;
                if (mdArrayType.contains("struct")) {
                    //more bands, check for each of them
                    String cellTypeArr = mdArrayType.split("}")[0];
                    cellType = cellTypeArr.split("\\{")[1];
                    //we are left with something like char red, char green, char blue
                    String[] cellTypeByBand = cellType.split(",");
                    //filter by number of bands
                    if (cellTypeByBand.length == gdalBandTypes.size()) {
                        Boolean allBandsMatch = true;
                        //compare band by band
                        for (int j = 0; j < cellTypeByBand.length; j++) {
                            //fix rasdaman type bug, where ushort and ulong are printed by rasdl, but not accepted as input.
                            // Instead rasdl wants "unsigned short" or "unsigned long"
                            if (cellTypeByBand[j].contains("ushort")) {
                                cellTypeByBand[j] = "unsigned short";
                            }
                            if (cellTypeByBand[j].contains("ulong")) {
                                cellTypeByBand[j] = "unsigned long";
                            }                            
                            if (!cellTypeByBand[j].contains(GDAL_TYPES_TO_RAS_TYPES.get(gdalBandTypes.get(j)))) {
                                allBandsMatch = false;
                            }
                        }
                        
                        // if band types are the same then last check for nilValues
                        if (allBandsMatch) {
                            // all nil values are the same then the existing set type is good for this coverage, no need to create new cell/array/set types
                            if (allNilValuesMatch(entry.getValue().getNilValues(), nilValues)) {
                                return entry.getKey();                            
                            }
                        }
                    }
                } else {
                    //1 band
                    cellType = entry.getValue().getCellType();
                    if (gdalBandTypes.size() == 1 && cellType.equals(GDAL_TYPES_TO_RAS_TYPES.get(gdalBandTypes.get(0)))) {
                        // all nil values are the same then the existing set type is good for this coverage, no need to create new cell/array/set types
                        if (allNilValuesMatch(entry.getValue().getNilValues(), nilValues)) {
                            return entry.getKey();                            
                        }
                    }
                }
            }     
            
            // NOTE: if an existing set type doesn't match with coverage new set type, then the new one will need to have different name
            // to avoid duplicate creating type error
            if (entry.getKey().equals(collectionName + TypeRegistry.SET_TYPE_SUFFIX)) {
                collectionName = StringUtil.addDateTimeSuffix(collectionName);
            }
        }
        // no existing set type can be used for the coverage, so create the new one
        result = typeRegistry.createNewType(collectionName, numberOfDimensions, translateTypes(gdalBandTypes), nilValues);
        return result;
    }
    
    /**
     * Check if both 2 input lists have same scalar nilValues for each band
     * @return 
     */
    private static boolean allNilValuesMatch(List<NilValue> oldNilValues, List<NilValue> newNilValues) {
        // NOTE: Only check null values (scalar) not null values object as it can contain different null reason but the values are the same
        if (oldNilValues.size() == newNilValues.size()) {
            int i = 0;                                        
            for (NilValue newNilValue : newNilValues) {
                if (!newNilValue.getValue().equals(oldNilValues.get(i).getValue())) {                    
                    return false;
                }
                i++;
            }

            // The new coverage's data type existed in rasdaman's collection type, so don't create a new type            
            return true;            
        }
        
        return false;
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
    private static final String R_UShort = "unsigned short";
    private static final String R_Short = "short";
    private static final String R_ULong = "unsigned long";
    private static final String R_Long = "long";
    private static final String R_Float = "float";
    private static final String R_Double = "double";

    //rasdaman abbreviations
    private static final String R_Abb_Char = "c";
    private static final String R_Abb_UShort = "us";
    private static final String R_Abb_Short = "s";
    private static final String R_Abb_ULong = "ul";
    private static final String R_Abb_Long = "l";
    private static final String R_Abb_Float = "f";
    private static final String R_Abb_Double = "d";

    //gdal base types
    private static final String GDT_Byte = "Byte";
    private static final String GDT_UInt16 = "UInt16";
    private static final String GDT_Int16 = "Int16";
    private static final String GDT_UInt32 = "UInt32";
    private static final String GDT_Int32 = "Int32";
    private static final String GDT_Float32 = "Float32";
    private static final String GDT_Float64 = "Float64";

    private static final HashMap<String, String> GDAL_TYPES_TO_RAS_TYPES = new HashMap<String, String>();
    private static final HashMap<String, String> RAS_TYPES_TO_ABBREVIATION = new HashMap<String, String>();

    static {
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Byte, R_Char);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_UInt16, R_UShort);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Int16, R_Short);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_UInt32, R_ULong);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Int32, R_Long);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Float32, R_Float);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Float64, R_Double);

        RAS_TYPES_TO_ABBREVIATION.put(R_Char, R_Abb_Char);
        RAS_TYPES_TO_ABBREVIATION.put(R_UShort, R_Abb_UShort);
        RAS_TYPES_TO_ABBREVIATION.put(R_Short, R_Abb_Short);
        RAS_TYPES_TO_ABBREVIATION.put(R_ULong, R_Abb_ULong);
        RAS_TYPES_TO_ABBREVIATION.put(R_Long, R_Abb_Long);
        RAS_TYPES_TO_ABBREVIATION.put(R_Float, R_Abb_Float);
        RAS_TYPES_TO_ABBREVIATION.put(R_Double, R_Abb_Double);
    }
}
