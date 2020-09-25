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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.PetascopeException;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.util.StringUtil;
import petascope.util.ras.TypeRegistry.TypeRegistryEntry;

/**
 * Utilities for determining the rasdaman collection type required for creating collections.
 */
public class TypeResolverUtil {
    
    private static final Logger log = LoggerFactory.getLogger(TypeResolverUtil.class);

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
    public static String guessCollectionTypeFromFile(String collectionName, String filePath, int dimension, List<NilValue> nullValues) throws PetascopeException {
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
     * @param inputPixelDataType      the pixel data type, if not given assumed Float32
     * @return pair containing the collection type and a list of cell types suffixes (e.g. <"GreySet", ["c"]>)
     */
    public static Pair<String, List<String>> guessCollectionType(String collectionName, Integer numberOfBands, Integer numberOfDimensions, List<NilValue> nullValues, String inputPixelDataType) throws PetascopeException {
        if (inputPixelDataType == null) {
            inputPixelDataType = GDT_Float32;
        }
        
        // Normally, pixelDataType is 1 type (e.g: Float32). However, netCDF can contain different types for bands (e.g: Float32,Int16,Float32).
        String[] pixelDataTypes = inputPixelDataType.split(",");
        int numberOfBandTypes = pixelDataTypes.length;
        
        if (numberOfBandTypes > 1 && numberOfBandTypes != numberOfBands) {
            // e.g: 3 bands but only 2 band types 
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Number of bands is different from number of data types."
                                  + " Given: " + numberOfBandTypes + " and " + numberOfBandTypes + " respectively.");
        }
        
        // Validate each pixel data type
        for (String pixelDataType : pixelDataTypes) {
            if (!GDAL_TYPES_TO_RAS_TYPES.containsKey(pixelDataType)) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Unknown pixel data type: " + pixelDataType);
            }
        }
        
        List<String> bandTypes = new ArrayList<>();
        List<String> typeSuffixes = new ArrayList<>();
        for (Integer i = 0; i < numberOfBands; i++) {
            String dataType = (numberOfBandTypes > 1 ? pixelDataTypes[i] : pixelDataTypes[0]);
            bandTypes.add(dataType);
            
            // e.g: char -> c
            String typeSuffix = RAS_TYPES_TO_ABBREVIATION.get(GDAL_TYPES_TO_RAS_TYPES.get(dataType));
            typeSuffixes.add(typeSuffix);
        }
        
        String result = guessCollectionType(collectionName, numberOfDimensions, bandTypes, nullValues);
        
        return Pair.of(result, typeSuffixes);
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
     * Return the default value for an input data type, e.g: char -> 0c, complexd -> complex(0.0d, 0.0d), boolean -> false
     */
    private static String getDefaultValueForCellDataType(String cellDataType) throws PetascopeException {
        String initValue = "0";
        cellDataType = cellDataType.trim();
        
        if (cellDataType.equals(R_CInt16)) {
            // CInt16 -> short
            return R_CInt16 + "(0s, 0s)";
        } else if (cellDataType.equals(R_CInt32)) {
            // CInt32 -> long
            return R_CInt32 + "(0l, 0l)";
        } else if (cellDataType.equals(R_CFloat64)) {
            // CFloat64 -> double
            return R_CFloat64 + "(0.0d, 0.0d)";
        } else if (cellDataType.equals(R_CFloat32)) {
            // CFloat32 -> float
            return R_CFloat32 + "(0.0f, 0.0f)";
        } else if (cellDataType.equals(R_Boolean)) {
            // boolean -> false
            return "false";
        } else {
            // char -> c
            String result = RAS_TYPES_TO_ABBREVIATION.get(cellDataType);
            if (result == null) {
                throw new PetascopeException(ExceptionCode.NoApplicableCode, "Cannot find abbreviation for cell data type '" + cellDataType + "' from registry.");
            }
            
            return initValue + result;
        }
    }
     
    /**
     * Return the default values of an MDD type for a given collection type.
     * 
     * e.g: MDD type: char -> 0c
     *      MDD type: struct {red char blue float red short} -> char,float,short -> 0c,0f,0s
     */
    public static List<String> getDefaultBandValues(String collectionType) throws PetascopeException, TypeRegistryEntryMissingException {
        
        List<String> results = new ArrayList<>();
        TypeRegistry typeRegistry = TypeRegistry.getInstance();
        TypeRegistryEntry entry = typeRegistry.getTypeEntry(collectionType);
        String cellType = entry.getCellType();
        
        if (!cellType.contains("{")) {
            // cell type is not a struct
            // e.g: float -> 0f
            String result = getDefaultValueForCellDataType(cellType);
            results.add(result);
        } else {
            // cell type is a struct, e.g: struct { band0 unsigned short ,band1 unsigned short ,band2 unsigned short ,band3 unsigned short  }
            String structContent = cellType.substring(cellType.indexOf("{") + 1, cellType.length() - 1).trim();
            // e.g: band0 unsigned short
            String[] tempArray = structContent.split(",");
            for (int i = 0; i < tempArray.length; i++) {
                String text = tempArray[i].trim();
                // e.g: band0
                String cellName = text.substring(0, text.indexOf(" "));

                // e.g: unsigned short
                String cellDataType = text.substring(text.indexOf(" ") + 1, text.length());
                
                // e.g: char -> 0c, unsigned short -> 0us
                String result = getDefaultValueForCellDataType(cellDataType);
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * Guesses the collection type. If no type is found, a new one is created.
     */
    private static String guessCollectionType(String collectionName, Integer numberOfDimensions, 
                                              List<String> gdalBandTypes, List<NilValue> nilValues) throws PetascopeException {
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
                if (mdArrayType.contains(STRUCT)) {
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
        try {
            result = typeRegistry.createNewType(collectionName, numberOfDimensions, translateTypes(gdalBandTypes), nilValues);
        } catch (PetascopeException ex) {
            // In case, one creates rasql types manually, then petascope cannot see them and it should create new ones to avoid duplicate names.
            if (ex.getMessage().toLowerCase().contains("type already exists")) {
                log.warn("Type names for '" + collectionName + "' already exists. Creating new ones...");
                collectionName = StringUtil.addDateTimeSuffix(collectionName);
                result = typeRegistry.createNewType(collectionName, numberOfDimensions, translateTypes(gdalBandTypes), nilValues);
            } else {
                throw ex;
            }
        }
        
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
    private static List<String> translateTypes(List<String> gdalTypes) {
        List<String> rasTypes = new ArrayList<>();
        for (String i : gdalTypes) {
            rasTypes.add(GDAL_TYPES_TO_RAS_TYPES.get(i));
        }
        return rasTypes;
    }
    
    public static final String STRUCT = "struct";

    //rasdaman base types
    public static final String R_Char = "char";
    public static final String R_Octet = "octet";
    public static final String R_UnsignedShort = "unsigned short";
    public static final String R_UShort = "ushort";
    public static final String R_Short = "short";    
    public static final String R_UnsignedLong = "unsigned long";
    public static final String R_ULong = "ulong";
    public static final String R_Long = "long";
    public static final String R_Float = "float";
    public static final String R_Double = "double";
    
    // 32 bit | complex of 16 bit signed integers
    public static final String R_CInt16 = "CInt16";
    // 64 bit | complex of 32 bit signed integers
    public static final String R_CInt32 = "CInt32";
    // 64 bit | single precision floating point complex
    public static final String R_CFloat32 = "CFloat32";
    public static final String R_COMPLEX = "complex";
    // 128 bit | single precision floating point complex
    public static final String R_CFloat64 = "CFloat64";
    public static final String R_COMPLEXD = "complexd";
    public static final String R_Boolean = "bool";

    //rasdaman abbreviations
    public static final String R_Abb_Char = "c";
    public static final String R_Abb_Octet = "o";
    public static final String R_Abb_UShort = "us";
    public static final String R_Abb_Short = "s";
    public static final String R_Abb_ULong = "ul";
    public static final String R_Abb_Long = "l";
    public static final String R_Abb_Float = "f";
    public static final String R_Abb_Double = "d";
    // For complex number
    public static final String R_Abb_CInt16 = "s,s";
    public static final String R_Abb_CInt32 = "l,l";
    public static final String R_Abb_CFloat32 = "f,f";
    public static final String R_Abb_CFloat64 = "d,d";
    

    //gdal base types
    public static final String GDT_Byte = "Byte";
    public static final String GDT_Signed_Byte = "SignedByte";
    public static final String GDT_UInt16 = "UInt16";
    public static final String GDT_Int16 = "Int16";
    public static final String GDT_CInt16 = "CInt16";
    public static final String GDT_UInt32 = "UInt32";
    public static final String GDT_Int32 = "Int32";
    public static final String GDT_CInt32 = "CInt32";
    public static final String GDT_Float32 = "Float32";
    public static final String GDT_CFloat32 = "CFloat32";
    public static final String GDT_Float64 = "Float64";
    public static final String GDT_CFloat64 = "CFloat64";
    
    // opengis base types (http://www.opengis.net/def/dataType/OGC/0/)
    public static final String OPENGIS_BOOLEAN = "unsignedByte";
    public static final String OPENGIS_OCTET = "signedByte";
    public static final String OPENGIS_CHAR = "unsignedByte";
    public static final String OPENGIS_SHORT = "signedShort";
    public static final String OPENGIS_USHORT = "unsignedShort";
    public static final String OPENGIS_LONG = "signedInt";
    public static final String OPENGIS_ULONG = "unsignedInt";
    public static final String OPENGIS_FLOAT = "float32";
    public static final String OPENGIS_DOUBLE = "float64";
    public static final String OPENGIS_COMPLEX_Int16 = "cint16";
    public static final String OPENGIS_COMPLEX_Int32 = "cint32";
    public static final String OPENGIS_COMPLEX_Float32 = "cfloat32";
    public static final String OPENGIS_COMPLEX_Float64 = "cfloat64";

    public static final HashMap<String, String> GDAL_TYPES_TO_RAS_TYPES = new HashMap<String, String>();
    private static final HashMap<String, String> RAS_TYPES_TO_ABBREVIATION = new HashMap<String, String>();
    public static final Map<String, Byte> RAS_TYPES_TO_NUMBER_OF_BYTES = new HashMap<String, Byte>();
    
    // Convert rasdaman data types to opengis types (e.g: char -> unsignedByte)
    public static final Map<String, String> RAS_TYPES_TO_OPENGIS_TYPES = new HashMap<>();

    static {
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Byte, R_Char);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Signed_Byte, R_Octet);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_UInt16, R_UShort);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Int16, R_Short);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_UInt32, R_ULong);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Int32, R_Long);        
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Float32, R_Float);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_Float64, R_Double);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_CInt16, R_CInt16);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_CInt32, R_CInt32);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_CFloat32, R_CFloat32);
        GDAL_TYPES_TO_RAS_TYPES.put(GDT_CFloat64, R_CFloat64);

        RAS_TYPES_TO_ABBREVIATION.put(R_Char, R_Abb_Char);
        RAS_TYPES_TO_ABBREVIATION.put(R_Octet, R_Abb_Octet);
        RAS_TYPES_TO_ABBREVIATION.put(R_UnsignedShort, R_Abb_UShort);
        RAS_TYPES_TO_ABBREVIATION.put(R_UShort, R_Abb_UShort);
        RAS_TYPES_TO_ABBREVIATION.put(R_Short, R_Abb_Short);
        RAS_TYPES_TO_ABBREVIATION.put(R_UnsignedLong, R_Abb_ULong);
        RAS_TYPES_TO_ABBREVIATION.put(R_ULong, R_Abb_ULong);
        RAS_TYPES_TO_ABBREVIATION.put(R_Long, R_Abb_Long);
        RAS_TYPES_TO_ABBREVIATION.put(R_Float, R_Abb_Float);
        RAS_TYPES_TO_ABBREVIATION.put(R_Double, R_Abb_Double);
        RAS_TYPES_TO_ABBREVIATION.put(R_CInt16, R_Abb_CInt16);
        RAS_TYPES_TO_ABBREVIATION.put(R_CInt32, R_Abb_CInt32);
        RAS_TYPES_TO_ABBREVIATION.put(R_CFloat32, R_Abb_CFloat32);
        RAS_TYPES_TO_ABBREVIATION.put(R_CFloat64, R_Abb_CFloat64);
        
        // band type -> bits (e.g: char -> 1 byte)
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_Boolean, (byte)1);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_Octet, (byte)1);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_Char, (byte)1);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_Short, (byte)2);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_UShort, (byte)2);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_UnsignedShort, (byte)2);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_Long, (byte)4);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_ULong, (byte)4);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_UnsignedLong, (byte)4);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_Float, (byte)4);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_Double, (byte)8);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_CInt16.toLowerCase(), (byte)4);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_CInt32.toLowerCase(), (byte)8);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_CFloat32.toLowerCase(), (byte)8);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_COMPLEX, (byte)8);
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_CFloat64.toLowerCase(), (byte)16);                
        RAS_TYPES_TO_NUMBER_OF_BYTES.put(R_COMPLEXD, (byte)16);

        RAS_TYPES_TO_OPENGIS_TYPES.put(R_Boolean, OPENGIS_BOOLEAN);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_Octet, OPENGIS_OCTET);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_Char, OPENGIS_CHAR);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_Short, OPENGIS_SHORT);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_UShort, OPENGIS_USHORT);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_UnsignedShort, OPENGIS_USHORT);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_Long, OPENGIS_LONG);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_ULong, OPENGIS_ULONG);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_UnsignedLong, OPENGIS_ULONG);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_Float, OPENGIS_FLOAT);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_Double, OPENGIS_DOUBLE);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_CInt16, OPENGIS_COMPLEX_Int16);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_CInt32, OPENGIS_COMPLEX_Int32);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_CFloat32, OPENGIS_COMPLEX_Float32);
        RAS_TYPES_TO_OPENGIS_TYPES.put(R_CFloat64, OPENGIS_COMPLEX_Float64);
    }
}
