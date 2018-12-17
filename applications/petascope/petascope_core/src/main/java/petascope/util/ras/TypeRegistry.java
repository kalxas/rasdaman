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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
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
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;

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
            throw new PetascopeException(ExceptionCode.RuntimeError, "Could not find the requested type: " + typeName);
        }
        return type;
    }

    public HashMap<String, TypeRegistryEntry> getTypeRegistry() {
        return typeRegistry;
    }

    private String generateStructStructure(List<String> bandBaseTypes) {
        String output = "";
        int count = 0;
        for (String bandBaseType : bandBaseTypes) {
            output += ("band" + count) + " " + bandBaseType + " ";
            if (count < bandBaseTypes.size() - 1) {
                output += ",";
            }
            count++;
        }
        return output;
    }

    private String generateNullValuesRepresentation(List<NilValue> nullValues) {
        String result = "";
        if (!nullValues.isEmpty()) {
            String values = "";
            for (NilValue nullValue : nullValues) {
                if (!values.isEmpty())
                    values += ",";
                values += nullValue.getValue();
            }
            result = NULL_VALUES_TEMPLATE.replace("$values", values);
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
    public String createNewType(String collectionName, Integer dimNo, List<String> bandBaseTypes, List<NilValue> nullValues) throws PetascopeException {
        log.debug("Creating new type for collection '" + collectionName + "' of dimension " + dimNo + 
                  ", base types: " + bandBaseTypes.toString() + ", with null values: " + (!nullValues.isEmpty()) + ".");
        String cellName = collectionName + CELL_TYPE_SUFFIX;
        String marrayName = collectionName + ARRAY_TYPE_SUFFIX;
        String setName = collectionName + SET_TYPE_SUFFIX;
        
        if (bandBaseTypes.size() == 1) {
            //simple types
            String queryMarray = QUERY_CREATE_MARRAY_TYPE.replace("$typeName", marrayName)
                                 .replace("$typeStructure", bandBaseTypes.get(0))
                                 .replace("$dimensions", expandDimensions(dimNo));
            //create the marray type
            RasUtil.executeRasqlQuery(queryMarray, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        } else {
            //struct types
            String queryStruct = QUERY_CREATE_STRUCT_TYPE.replace("$structTypeName", cellName)
                                 .replace("$structStructure", generateStructStructure(bandBaseTypes));
            //create the struct type
            RasUtil.executeRasqlQuery(queryStruct, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
            //marray type
            String queryMarray = QUERY_CREATE_MARRAY_TYPE.replace("$typeName", marrayName)
                                 .replace("$typeStructure", cellName)
                                 .replace("$dimensions", expandDimensions(dimNo));
            //create it
            RasUtil.executeRasqlQuery(queryMarray, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        }

        String querySet = QUERY_CREATE_SET_TYPE.replace("$typeName", setName)
                          .replace("$marrayTypeName", marrayName)
                          .replace("$nullValues", generateNullValuesRepresentation(nullValues));
        //create it
        RasUtil.executeRasqlQuery(querySet, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        
        // TODO: shouldn't reinitialize from scratch but just add the new types to the registry
        this.reinitialize();
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

    private void reinitialize() throws PetascopeException {
        structTypeDefinitions.clear();
        setTypeDefinitions.clear();
        marrayTypeDefinitions.clear();
        typeRegistry.clear();
        initializeRegistry();
    }

    private void initializeSetTypes() throws RasdamanException, PetascopeException {
        Object result = RasUtil.executeRasqlQuery(QUERY_SET_TYPES);
        RasQueryResult queryResult = new RasQueryResult(result);
        
        for (byte[] bytes : queryResult.getMdds()) {
            String setLine = new String(bytes);
            try {
                String setName = parseSetName(setLine);
                String marrayName = parseSetMarrayName(setLine);
                String nilValues = parseSetNullValues(setLine);
                setTypeDefinitions.add(Pair.of(setName, marrayName));
                setTypeNullValues.put(setName, nilValues);
            } catch (Exception ex) {
                log.warn(ex.getMessage());
            }
        }
    }

    private String parseSetNullValues(String setLine) throws RasdamanException {
        String[] parts = setLine.split("NULL VALUES \\[");
        if (parts.length < 2) { //no nil values
            return "";
        }
        String[] nilParts = parts[1].split("]");
        if (nilParts.length < 1) { //invalid line
            throw new RasdamanException(ExceptionCode.RuntimeError, "Null values cannot be parsed from rasdaman result: '" + setLine + "'.");
        }
        return nilParts[0].trim();
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

    private void initializeMarrayTypes() throws PetascopeException {
        Object result = RasUtil.executeRasqlQuery(QUERY_MARRAY_TYPES);
        RasQueryResult queryResult = new RasQueryResult(result);
        String[] fullStringResult = queryResult.toString().split("\0");
        for (String marrayLine : fullStringResult) {
            try {
                String marrayName = parseMarrayName(marrayLine);
                String marrayStructure = parseMarrayStructure(marrayLine);
                marrayTypeDefinitions.put(marrayName, marrayStructure);
            } catch (RasdamanException ex) {
                log.warn(ex.getExceptionText());
            }
        }
    }

    private void initializeStructRegistry() throws PetascopeException {
        Object result = RasUtil.executeRasqlQuery(QUERY_STRUCT_TYPES);
        RasQueryResult queryResult = new RasQueryResult(result);
        for (byte[] bytes : queryResult.getMdds()) {
            // e.g: CREATE TYPE RGBPixel AS (red char, green char, blue char)
            String str = new String(bytes);
            String[] parts = str.split(AS);
            if (parts.length != 2) {
                log.warn("Struct type cannot be parsed from rasdaman result: '" + str + "'.");
                continue;
            }

            String[] nameParts = parts[0].split(" ");
            if (nameParts.length != 3) {
                log.warn("Struct type cannot be parsed from rasdaman result: '" + parts[0] + "'.");
                continue;
            }
            String typeName = nameParts[2];

            String typeStructure = "struct " + parts[1].trim().replace("(", "{").replace(")", "}");
            structTypeDefinitions.put(typeName, typeStructure);
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
     * Builds the registry from the collected types gathered by parsing the rasdl output
     */
    private void buildRegistry() {
        for (Pair<String, String> entry : setTypeDefinitions) {
            String domainType = marrayTypeDefinitions.get(entry.snd);
            if (domainType != null) {
                String[] domainTypeParts = domainType.split(",");
                if (domainTypeParts.length >= 2) {
                    String[] baseTypeParts = ArrayUtils.remove(domainTypeParts, domainTypeParts.length - 1);
                    String baseType = StringUtils.join(baseTypeParts, "");
                    String[] nullParts = setTypeNullValues.get(entry.fst).split(",");
                    List<NilValue> nullValues = new ArrayList<>();
                    for (String val : nullParts) {
                        if (!val.isEmpty()) {
                            //if the value that is parsed is an interval with the same limits (e.g. 5:5), add only 1
                            //value. This is needed because currently there is a bug when creating types via rasql,
                            //which doesn't allow single values to be specified. However, petascope needs to display single
                            //values when presenting the output to the user.
                            if (val.contains(RASQL_BOUND_SEPARATION)) {
                                String[] parts = val.split(RASQL_BOUND_SEPARATION);
                                if (parts.length == 2 & parts[0].equals(parts[1])) {
                                    val = parts[0];
                                }
                            }
                            NilValue nullValue = new NilValue(val, "");
                            nullValues.add(nullValue);
                        }
                    }
                    TypeRegistryEntry typeRegistryEntry = new TypeRegistryEntry(baseType, domainType, nullValues);
                    typeRegistry.put(entry.fst, typeRegistryEntry);
                }
            }
        }
    }


    /**
     * Internal class for keeping track of the type entries
     */
    public class TypeRegistryEntry {

        private TypeRegistryEntry(String cellType, String mddArrayType, List<NilValue> nilValues) {
            this.cellType = cellType;
            this.mdArrayType = mddArrayType;
            this.nilValues = nilValues;
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

        public List<NilValue> getNilValues() {
            return nilValues;
        }

        private String cellType;
        private String mdArrayType;
        private List<NilValue> nilValues;
    }
    
    public static final String SET_TYPE_SUFFIX = "_Set";
    public static final String ARRAY_TYPE_SUFFIX = "_Array";
    public static final String CELL_TYPE_SUFFIX = "_Cell";
    
    private static final String AS = " AS ";

    private final HashMap<String, TypeRegistryEntry> typeRegistry = new HashMap<String, TypeRegistryEntry>();
    private final HashMap<String, String> marrayTypeDefinitions = new HashMap<String, String>();
    private final ArrayList<Pair<String, String>> setTypeDefinitions = new ArrayList<Pair<String, String>>();
    private HashMap<String, String> setTypeNullValues = new HashMap<String, String>();
    private HashMap<String, String> structTypeDefinitions = new HashMap<String, String>();
    private final String RASDL_MARRAY_IDENTIFIER = "typedef marray";
    private final String RASDL_SET_IDENTIFIER = "typedef set";
    private final Logger log = LoggerFactory.getLogger(TypeRegistry.class);
    private final static String QUERY_MARRAY_TYPES = "SELECT a FROM RAS_MARRAY_TYPES a";
    private final static String QUERY_STRUCT_TYPES = "SELECT a FROM RAS_STRUCT_TYPES a";
    private final static String QUERY_SET_TYPES = "SELECT a FROM RAS_SET_TYPES a";
    private final static String QUERY_CREATE_MARRAY_TYPE = "CREATE TYPE $typeName AS $typeStructure MDARRAY [$dimensions]";
    private final static String QUERY_CREATE_SET_TYPE = "CREATE TYPE $typeName AS SET ($marrayTypeName $nullValues)";
    private final static String NULL_VALUES_TEMPLATE = "NULL VALUES [$values]";
    private final static String QUERY_CREATE_STRUCT_TYPE = "CREATE TYPE $structTypeName AS ( $structStructure )";
}
