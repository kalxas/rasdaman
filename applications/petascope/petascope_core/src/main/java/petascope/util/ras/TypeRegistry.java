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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rasdaman.domain.cis.NilValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.config.ConfigManager;
import petascope.core.Pair;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.ListUtil;
import petascope.util.StringUtil;

/**
 * Keeps track of the types that exist in the tracked rasdaman instance.
 *
 * @author <a href="alex@flanche.net">Alex Dumitru</a>
 * @author <a href="vlad@flanche.net">Vlad Merticariu</a>
 */
public class TypeRegistry {
    private static TypeRegistry instance;

    /**
     * Returns the instance to the singleton @link{TypeRegistry}
     *
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public static TypeRegistry getInstance() throws PetascopeException {
        if (instance != null)
            return instance;
        
        // instance is null, initialize
        TypeRegistry tmp = new TypeRegistry();
        tmp.initializeRegistry();
        instance = tmp;
        return instance;
    }

    private TypeRegistry() {
    }

    /**
     * Returns the type entry for a give type name.
     * If the type is not in the database, an exception will be thrown.
     *
     * @param typeName the name of the type for which the entry is required
     * @return the type entry for the given type name
     */
    public TypeRegistryEntry getTypeEntry(String typeName) throws PetascopeException {
        TypeRegistryEntry type = typeRegistry.get(typeName);
        if (type == null) {
            throw new PetascopeException(ExceptionCode.RuntimeError, "Could not find the requested type: " + typeName + " from type regitries.");
        }
        return type;
    }

    public Map<String, TypeRegistryEntry> getTypeRegistry() {
        return typeRegistry;
    }

    /**
     * Create a struct type content, e.g: band0 float, band1 float, band2 short.
     */
    private String generateStructStructure(List<String> bandBaseTypes) {
        List<String> bands = new ArrayList<>();
        int i = 0;
        for (String bandBaseType : bandBaseTypes) {
            String band = ("band" + i) + " " + bandBaseType;
            bands.add(band);
            
            i++;
        }
        
        String result = ListUtil.join(bands, ", ");
        return result;
    }

    private String generateNullValuesRepresentation(List<List<NilValue>> nullValues) {
        String result = "";
        if (!nullValues.isEmpty()) {
            
            // all bands have same null value
            Set<String> set = new LinkedHashSet<>();
            // e.g. [ 1,3,4,5 ]
            for (List<NilValue> bandValues : nullValues) {
                // [1,2]               
                for (NilValue nilValue : bandValues) {
                    if (!nilValue.getValue().trim().isEmpty()) {
                        set.add(nilValue.getValue());
                    }
                }
            }
            
            if (set.isEmpty()) {
                // no null values
                return result;
            }
            
            String value = ListUtil.join(Arrays.asList(set), ",");
            // e.g. result =  [ "1", "2", "3", "6" ]
            result = NULL_VALUES_TEMPLATE +  value;
        }
        
        return result;
    }

    private String expandDimensions(int dimNo) {
        StringBuilder result = new StringBuilder();
        for (int i = 0 ; i < dimNo; i++) {
            if (i != 0) {
                result.append(",");
            }
            result.append("D" + String.valueOf(i));
        }

        return result.toString();
    }

    /**
     * @return the name of the created set (collection) type.
     */
    public String createNewType(String collectionName, Integer dimNo, List<String> bandBaseTypes, List<List<NilValue>> nullValues) throws PetascopeException {
        log.debug("Creating new type for collection '" + collectionName + "' of dimension " + dimNo + 
                  ", base types: " + bandBaseTypes.toString() + ", with null values: " + (!nullValues.isEmpty()) + ".");
        String cellName = collectionName + CELL_TYPE_SUFFIX;
        String marrayName = collectionName + ARRAY_TYPE_SUFFIX;
        String setName = collectionName + SET_TYPE_SUFFIX;
        
        final String EXIST_TYPE_ERROR_MESSAGE = "already exists";
        
        if (bandBaseTypes.size() == 1) {
            //simple types
            String queryMarray = QUERY_CREATE_MARRAY_TYPE.replace("$typeName", marrayName)
                                 .replace("$typeStructure", bandBaseTypes.get(0))
                                 .replace("$dimensions", expandDimensions(dimNo));
            // create the marray type
            // e.g CREATE TYPE meris_lai_resolution_automatic AS float MDARRAY [D0,D1,D2]
            try {
                RasUtil.executeRasqlQuery(queryMarray, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
            } catch (RasdamanException ex) {
                if (!ex.getExceptionText().contains(EXIST_TYPE_ERROR_MESSAGE)) {
                    throw ex;
                }
            }
            
            this.parseMarrayType(queryMarray);
        } else {
            //struct types
            String queryStruct = QUERY_CREATE_STRUCT_TYPE.replace("$structTypeName", cellName)
                                 .replace("$structStructure", generateStructStructure(bandBaseTypes));
            //create the struct type
            try {
                RasUtil.executeRasqlQuery(queryStruct, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
            }  catch (RasdamanException ex) {
                if (!ex.getExceptionText().contains(EXIST_TYPE_ERROR_MESSAGE)) {
                    throw ex;
                }
            }
            
            //marray type
            String queryMarray = QUERY_CREATE_MARRAY_TYPE.replace("$typeName", marrayName)
                                 .replace("$typeStructure", cellName)
                                 .replace("$dimensions", expandDimensions(dimNo));
            //create it
            try {
                RasUtil.executeRasqlQuery(queryMarray, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
            } catch (RasdamanException ex) {
                if (!ex.getExceptionText().contains(EXIST_TYPE_ERROR_MESSAGE)) {
                    throw ex;
                }
            }
            
            this.parseStructType(queryStruct);
            this.parseMarrayType(queryMarray);
        }

        String querySet = QUERY_CREATE_SET_TYPE.replace("$typeName", setName)
                          .replace("$marrayTypeName", marrayName)
                          .replace("$nullValues", generateNullValuesRepresentation(nullValues));
        //create it
        try {
            RasUtil.executeRasqlQuery(querySet, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        }  catch (RasdamanException ex) {
            if (!ex.getExceptionText().contains(EXIST_TYPE_ERROR_MESSAGE)) {
                throw ex;
            }
        }
        
        Pair<String, String> setTypePair = this.parseSetType(querySet);
        TypeRegistryEntry typeRegistryEntry = this.createTypeRegistryEntry(setTypePair);
        if (typeRegistryEntry != null) {
            typeRegistry.put(setTypePair.fst, typeRegistryEntry);
        }
        
        return setName;
    }

    /**
     * Returns the mdd type for a given collection type.
     * @param collectionType the collection type.
     * @return the mdd type, empty if nothing is found.
     */
    public String getMddTypeForCollectionType(String collectionType) {
        String mddType = "";
        for (Pair<String, String> i : setTypeDefinitions) {
            if (collectionType.equals(i.fst)) {
                mddType = i.snd;
                break;
            }
        }
        return mddType;
    }
    
    
    /**
     * Collects the types from rasdaman and inserts them into an internal registry
     */
    private void initializeRegistry() throws PetascopeException {
        log.trace("Initializing the type registry");
        initializeStructRegistry();
        initializeMarrayTypes();
        initializeSetTypes();
        this.buildRegistry();
        log.info("Succesfully initialized the type registry.");
        log.trace("Type registry contents: {}", typeRegistry.toString());
    }

    /**
     * Parse a creating set type query to pair of set name and marray name.
     * e.g: CREATE TYPE test_rgb_Set AS SET (test_rgb_Array NULL VALUES [119.000000,208.000000,248.000000])
     * to pair of ("test_rgb_Set", "test_rgb_Array")
     */
    private Pair<String, String> parseSetType(String setTypeQuery) throws RasdamanException {
        String setName = parseSetName(setTypeQuery);
        String marrayName = parseSetMarrayName(setTypeQuery);   
                
        List<String> nilValues = parseSetNullValues(setTypeQuery);
        setTypeNullValues.put(setName, nilValues);
        
        Pair<String, String> setTypePair = new Pair(setName, marrayName);
        setTypeDefinitions.add(setTypePair);                
        
        return setTypePair;
    }

    private void initializeSetTypes() throws RasdamanException, PetascopeException {
        Object result = RasUtil.executeRasqlQuery(QUERY_SET_TYPES, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, false);
        RasQueryResult queryResult = new RasQueryResult(result);
        
        for (byte[] bytes : queryResult.getMdds()) {
            // e.g: CREATE TYPE test_rgb_Set AS SET (test_rgb_Array NULL VALUES [119.000000,208.000000,248.000000])
            String setTypeQuery = new String(bytes);
            try {                
                this.parseSetType(setTypeQuery);                
            } catch (Exception ex) {
                log.warn(ex.getMessage());
            }
        }
    }

    /**
     * NULL VALUES [-999.000000]
     *      return ["-999"]
     * 
     * or
     */
    private List<String> parseSetNullValues(String setLine) throws RasdamanException {
        List<String> results = StringUtil.extractStringsBetweenSquareBrackets(setLine);
        return results;
        
    }

    private String parseSetName(String setLine) throws RasdamanException {
        String[] parts = setLine.split("CREATE TYPE ");
        if (parts.length < 2) { //invalid line
            throw new RasdamanException(ExceptionCode.RuntimeError, "Set type name cannot be parsed from rasdaman result: '" + setLine + "'.");
        }
        String[] setNameParts = parts[1].split(" ");
        if (setNameParts.length < 1) { //invalid line
            throw new RasdamanException(ExceptionCode.RuntimeError, "Set type name cannot be parsed from rasdaman result: '" + setLine + "'.");
        }
        return setNameParts[0].trim();
    }

    private String parseSetMarrayName(String setLine) throws RasdamanException {
        String result;
        String[] parts = setLine.split("AS SET \\(");
        if (parts.length < 2) { //invalid line
            throw new RasdamanException(ExceptionCode.RuntimeError, "Set type cannot be parsed from rasdaman result: '" + setLine + "'.");
        }
        String[] marrayNameParts = parts[1].split("\\)");
        if (parts.length < 1) { //invalid line
            throw new RasdamanException(ExceptionCode.RuntimeError, "Set type cannot be parsed from rasdaman result: '" + setLine + "'.");
        }
        if (marrayNameParts[0].contains("NULL VALUES")) {
            result = marrayNameParts[0].split("NULL VALUES")[0].trim();
        } else {
            result = marrayNameParts[0].trim();
        }
        return result;
    }
    
    /**
     * Parse a creating marray type to pair of marray name and marray definition.
     * e.g:  CREATE TYPE test_wms_4d_ecmwf_fire_netcdf_Array AS float MDARRAY [a0,a1,a2,a3]
     * to Pair of ("test_wms_4d_ecmwf_fire_netcdf_Array", "float, 4")
     */
    private void parseMarrayType(String marrayTypeQuery) throws RasdamanException {
        String marrayName = parseMarrayName(marrayTypeQuery);
        String marrayStructure = parseMarrayStructure(marrayTypeQuery);
        
        marrayTypeDefinitions.put(marrayName, marrayStructure);
    }

    private void initializeMarrayTypes() throws PetascopeException {
        Object result = RasUtil.executeRasqlQuery(QUERY_MARRAY_TYPES, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, false);
        RasQueryResult queryResult = new RasQueryResult(result);
        for (byte[] bytes : queryResult.getMdds()) {
            String marrayTypeQuery = new String(bytes);
            // e.g:  CREATE TYPE test_wms_4d_ecmwf_fire_netcdf_Array AS float MDARRAY [a0,a1,a2,a3]
            try {
                this.parseMarrayType(marrayTypeQuery);
            } catch (RasdamanException ex) {
                log.warn(ex.getExceptionText());
            }
        }
    }
    
    /**
     * Parse a creating struct type query to pair of name and struct type definition.
     * e.g:  CREATE TYPE RGBPixel AS (red char, green char, blue char) 
     * returns Pair of (RGBPixel, struct {red char, green char, blue char})
     */
    private void parseStructType(String structTypeQuery) {
        String[] parts = structTypeQuery.split(AS);
        if (parts.length != 2) {
            log.warn("Struct type cannot be parsed from rasdaman result: '" + structTypeQuery + "'.");
            return;
        }

        String[] nameParts = parts[0].split(" ");
        if (nameParts.length != 3) {
            log.warn("Struct type cannot be parsed from rasdaman result: '" + parts[0] + "'.");
            return;
        }
        String typeName = nameParts[2];
        String typeStructure = "struct " + parts[1].trim().replace("(", "{").replace(")", "}");
        
        structTypeDefinitions.put(typeName, typeStructure);        
    }

    private void initializeStructRegistry() throws PetascopeException {
        Object result = RasUtil.executeRasqlQuery(QUERY_STRUCT_TYPES, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, false);
        RasQueryResult queryResult = new RasQueryResult(result);
        for (byte[] bytes : queryResult.getMdds()) {
            // e.g: CREATE TYPE RGBPixel AS (red char, green char, blue char)
            String structTypeQuery = new String(bytes);
            this.parseStructType(structTypeQuery);            
        }
    }

    private String parseMarrayName(String marrayLine) throws RasdamanException {
        // e.g: CREATE TYPE BoolString AS bool MDARRAY [a0]        
        String[] parts = marrayLine.split(" ");
        if (parts.length < 3) {
            throw new RasdamanException(ExceptionCode.RuntimeError, "Array name cannot be parsed from rasdaman result, given '" + marrayLine + "'.");
        }

        // e.g: BoolString
        String marrayName = parts[2];
        return marrayName; 
    }

    private String parseMarrayStructure(String marrayLine) throws RasdamanException {
        // e.g: CREATE TYPE S2_L2A_SOUTH_TYROL_RAS_new7_Array AS S2_L2A_SOUTH_TYROL_RAS_new7_Cell MDARRAY [a0,a1,a2]
        String[] parts = marrayLine.split(AS);
        if (parts.length < 2) {
            throw new RasdamanException(ExceptionCode.RuntimeError, "Array structure cannot be parsed from rasdaman result, given '" + marrayLine + "'.");
        }
        String marrayStructure = parts[1].trim();
        String[] marrayStructureParts = marrayStructure.split("MDARRAY");
        if (marrayStructureParts.length < 2) {
            throw new RasdamanException(ExceptionCode.RuntimeError, "Array structure cannot be parsed from rasdaman result, given '" + marrayLine + "'.");
        }
        //marrayStructureParts[0] is the type or structure name, marrayStructureParts[1] is the dimensionality
        return (expandStructureType(marrayStructureParts[0].trim()) + "," + marrayStructureParts[1].split(",").length);
    }

    private String expandStructureType(String typeName) throws RasdamanException {
        if (structTypeDefinitions.keySet().contains(typeName)) {
            return structTypeDefinitions.get(typeName);
        } else {
            return typeName;
        }
    }
    
    /**
     * Delete the input set type from stored set registries.
     * @param setType collection type
     */
    public boolean deleteSetTypeFromRegistry(String setType) {
        if (this.typeRegistry.containsKey(setType)) {
            this.typeRegistry.remove(setType);
            this.setTypeNullValues.remove(setType);

            for (Iterator<Pair<String, String>> iterator = this.setTypeDefinitions.iterator(); iterator.hasNext();) {
                Pair<String, String> pair = iterator.next();
                if (pair.fst.equals(setType)) {
                    // Remove the current element from the iterator and the list.
                    iterator.remove();
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Delete the input MDD type from stored MDD registry
     * @param mddType MDD type
     */
    public boolean deleteMDDTypeFromRegistry(String mddType) {
        return this.marrayTypeDefinitions.remove(mddType) != null;
    }
    
    /**
     * Delete the cell type from stored cell registry
     * @param cellType cell type
     */
    public boolean deleteCellTypeFromRegistry(String cellType) {
        return this.structTypeDefinitions.remove(cellType) != null;
    }
    
    /**
     * Create a new TypeRegistryEntry to be put in a cache.
     */
    private TypeRegistryEntry createTypeRegistryEntry(Pair<String, String> setTypePair) {
        String domainType = marrayTypeDefinitions.get(setTypePair.snd);
        if (domainType != null) {
            // e.g: struct {band0 char, band1 char, band2 char},2
            String[] domainTypeParts = domainType.split(","); 
            if (domainTypeParts.length >= 2) {
                String[] baseTypeParts = ArrayUtils.remove(domainTypeParts, domainTypeParts.length - 1);
                // e.g: struct {band0 char, band1 char, band2 char}
                String baseType = StringUtils.join(baseTypeParts, ",");
                
                // e.g. if all bands have same values ["1,2,3,4"] or null values per bands ["1,2", "3", "4,5"]
                List<String> nullParts = setTypeNullValues.get(setTypePair.fst);
                
                List<List<NilValue>> nilValues = new ArrayList<>();
                
                // each part is a set of null values per band OR a set of null values for all band
                for (String nullPart : nullParts) {
                    // e.g. 1,2
                    String[] values = nullPart.split(",");
                    
                    List<NilValue> nullValuesPerBand = new ArrayList<>();
                    for (String value : values) {
                        NilValue nilValue = new NilValue(value, null);
                        nullValuesPerBand.add(nilValue);
                    }
                    
                    nilValues.add(nullValuesPerBand);
                }
                
                TypeRegistryEntry typeRegistryEntry = new TypeRegistryEntry(baseType, domainType, nilValues);
                return typeRegistryEntry;
            }
        }
        
        return null;
    } 

    /**
     * Builds the registry from the collected types gathered by parsing the rasql output
     */
    private void buildRegistry() {
        for (Pair<String, String> setTypePair : setTypeDefinitions) {
            TypeRegistryEntry typeRegistryEntry = this.createTypeRegistryEntry(setTypePair);
            if (typeRegistryEntry != null) {
                typeRegistry.put(setTypePair.fst, typeRegistryEntry);
            }
        }
    }


    /**
     * Internal class for keeping track of the type entries
     */
    public class TypeRegistryEntry {

        private TypeRegistryEntry(String cellType, String mddArrayType, List<List<NilValue>> nilValues) {
            this.cellType = cellType;
            this.mdArrayType = mddArrayType;
            this.nilValues = nilValues;
        }
        
        /**
         * return a list of band types (e.g: cellType is char -> [char], cellType is struct { band0 char, band0 short } -> [char, short]
         */
        public List<String> getBandsTypes() {
            List<String> bandTypes = new ArrayList<>();
            
            if (this.cellType.contains(STRUCT)) {
                String structContent = this.cellType.substring(this.cellType.indexOf("{") + 1, this.cellType.indexOf("}"));
                String[] parts = structContent.trim().split(", ");
                for (String part : parts) {
                    // e.g: band0 unsigned short
                    String bandType = part.substring(part.indexOf(" ") + 1, part.length());
                    bandTypes.add(bandType);
                }
            } else {
                // e.g: char
                bandTypes.add(this.cellType);
            }
            
            return bandTypes;
        }
        
        /**
         * Return the list of number of bytes for list of bands types
         * e.g: [char, ushort] -> [1, 2] bytes
         */
        public List<Byte> getBandsSizesInBytes(List<String> bandsTypes) {
            List<Byte> bandsSizes = new ArrayList<>();
            
            for (String bandType : bandsTypes) {
                Byte bandSize = TypeResolverUtil.RAS_TYPES_TO_NUMBER_OF_BYTES.get(bandType);
                if (bandSize == null) {
                    log.error("Cannot get number of bytes for band type '" + bandType + "' from the type registry.");                    
                } else {
                    bandsSizes.add(bandSize);
                }                
            }
            
            return bandsSizes;
        }

        /**
         * Returns the cell type of this type entry
         *
         * @return
         */
        public String getCellType() {
            return cellType;
        }

        /**
         * Returns the MD array type of this type entry
         *
         * @return
         */
        public String getMDArrayType() {
            return mdArrayType;
        }

        @Override
        public String toString() {
            return "TypeRegistryEntry{" +
                   "cellType='" + cellType + '\'' +
                   ", mdArrayType='" + mdArrayType + '\'' +
                   ", nilValues='" + nilValues.toString() + "'" +
                   '}';
        }

        public Boolean equals(TypeRegistryEntry another) {
            return another.getCellType().equals(this.cellType) &&
                   another.getMDArrayType().equals(this.mdArrayType);
        }

        public List<List<NilValue>> getNilValues() {
            return nilValues;
        }

        private String cellType;
        private String mdArrayType;
        private List<List<NilValue>> nilValues;
    }
    
    public static final String SET_TYPE_SUFFIX = "_Set";
    public static final String ARRAY_TYPE_SUFFIX = "_Array";
    public static final String CELL_TYPE_SUFFIX = "_Cell";
    
    private static final String AS = " AS ";
    public static final String STRUCT = "struct";

    private final Map<String, TypeRegistryEntry> typeRegistry = new LinkedHashMap<>();
    private final Map<String, String> marrayTypeDefinitions = new LinkedHashMap<>();
    private final List<Pair<String, String>> setTypeDefinitions = new ArrayList<>();
    private Map<String, List<String>> setTypeNullValues = new LinkedHashMap<>();
    private Map<String, String> structTypeDefinitions = new LinkedHashMap();
    private final Logger log = LoggerFactory.getLogger(TypeRegistry.class);
    private final static String QUERY_MARRAY_TYPES = "SELECT a FROM RAS_MARRAY_TYPES a";
    private final static String QUERY_STRUCT_TYPES = "SELECT a FROM RAS_STRUCT_TYPES a";
    private final static String QUERY_SET_TYPES = "SELECT a FROM RAS_SET_TYPES a";
    private final static String QUERY_CREATE_MARRAY_TYPE = "CREATE TYPE $typeName AS $typeStructure MDARRAY [$dimensions]";
    private final static String QUERY_CREATE_SET_TYPE = "CREATE TYPE $typeName AS SET ($marrayTypeName $nullValues)";
    private final static String NULL_VALUES_TEMPLATE = "NULL VALUES ";
    // e.g. NULL VALUES { [1,3], [5], [8], [9] } // band1 has [1,3], band2 has [5], band3 has [8] and band4 has [9]
    private final static String QUERY_CREATE_STRUCT_TYPE = "CREATE TYPE $structTypeName AS ( $structStructure )";
}
