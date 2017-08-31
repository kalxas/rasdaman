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
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.rasdaman.domain.cis.NilValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.config.ConfigManager;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;

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
        if (instance == null) {
            instance = new TypeRegistry();
            instance.initializeRegistry();
        }
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
     * @throws petascope.util.ras.TypeRegistryEntryMissingException
     */
    public TypeRegistryEntry getTypeEntry(String typeName) throws TypeRegistryEntryMissingException {
        TypeRegistryEntry type = typeRegistry.get(typeName);
        if (type == null) {
            throw new TypeRegistryEntryMissingException("Could not find the requested type: " + typeName);
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
            int count = 0;
            for (NilValue nullValue : nullValues) {
                String val = nullValue.getValue();
                //check if i is an interval, if not (and is a single value), make it an interval, as there is a bug in
                // rasdaman which prevents adding single values as null values
                if (val.contains(":")) {
                    values += val;
                } else {
                    values += val + ":" + val;
                }
                //add "," on all but last dim
                if (count < nullValues.size() - 1) {
                    values += ",";
                }
                count++;
            }
            result = NULL_VALUES_TEMPLATE.replace("$values", values);
        }
        return result;
    }

    private String expandDimensions(int numberOfDimensions) {
        StringBuilder result = new StringBuilder();
        for (int i = 0 ; i < numberOfDimensions; i++) {
            result.append("D" + String.valueOf(i));
            if (i < numberOfDimensions - 1) {
                result.append(",");
            }
        }

        return result.toString();
    }

    public String createNewType(String collectionName, Integer numberOfDimensions, List<String> bandBaseTypes, List<NilValue> nullValues) throws PetascopeException {
        log.info("Creating new type.");
        String cellName = collectionName + "_Cell";
        String marrayName = collectionName + "_Array";
        String setName = collectionName + "_Set";
        
        if (bandBaseTypes.size() == 1) {
            //simple types
            String queryMarray = QUERY_CREATE_MARRAY_TYPE.replace("$typeName", marrayName)
                                 .replace("$typeStructure", bandBaseTypes.get(0))
                                 .replace("$dimensions", expandDimensions(numberOfDimensions));
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
                                 .replace("$dimensions", expandDimensions(numberOfDimensions));
            //create it
            RasUtil.executeRasqlQuery(queryMarray, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
        }

        String querySet = QUERY_CREATE_SET_TYPE.replace("$typeName", setName)
                          .replace("$marrayTypeName", marrayName)
                          .replace("$nullValues", generateNullValuesRepresentation(nullValues));
        //create it
        RasUtil.executeRasqlQuery(querySet, ConfigManager.RASDAMAN_ADMIN_USER, ConfigManager.RASDAMAN_ADMIN_PASS, true);
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
            if (collectionType.equals(i.getKey())) {
                mddType = i.getValue();
                break;
            }
        }
        return mddType;
    }
    
    /**
     * Collects the types from rasdl and inserts them into an internal registry
     */
    private void initializeRegistry() throws PetascopeException {
        try {
            log.trace("Initializing the type registry");
            initializeStructRegistry();
            initializeMarrayTypes();
            initializeSetTypes();
            this.buildRegistry();
            log.info("Succesfully initiated the type registry. Contents: {}", typeRegistry.toString());
        } catch (RasdamanException e) {
            //log.error(MessageFormat.format("Could not read the rasdaman type registry. Tried with {0}rasdl -p", ConfigManager.RASDAMAN_BIN_PATH));
            throw e;
        }
    }

    private void initializeSetTypes() throws RasdamanException, PetascopeException {
        Object result = RasUtil.executeRasqlQuery(QUERY_SET_TYPES);
        RasQueryResult queryResult = new RasQueryResult(result);
        String[] fullStringResult = queryResult.toString().split("\0");
        for (String setLine : fullStringResult) {
            String setName = parseSetName(setLine);
            String marrayName = parseSetMarrayName(setLine);
            String nilValues = parseSetNullValues(setLine);
            setTypeDefinitions.add(Pair.of(setName, marrayName));
            setTypeNullValues.put(setName, nilValues);
        }
    }

    private String parseSetNullValues(String setLine) {
        String[] parts = setLine.split("NULL VALUES \\[");
        if (parts.length < 2) { //no nil values
            return "";
        }
        String[] nilParts = parts[1].split("]");
        if (nilParts.length < 1) { //invalid line
            return "";
        }
        return nilParts[0].trim();
    }

    private String parseSetName(String setLine) {
        String[] parts = setLine.split("CREATE TYPE ");
        if (parts.length < 2) { //invalid line
            return "";
        }
        String[] setNameParts = parts[1].split(" ");
        if (setNameParts.length < 1) { //invalid line
            return "";
        }
        return setNameParts[0].trim();
    }

    private String parseSetMarrayName(String setLine) {
        String result;
        String[] parts = setLine.split("AS SET \\(");
        if (parts.length < 2) { //invalid line
            return "";
        }
        String[] marrayNameParts = parts[1].split("\\)");
        if (parts.length < 1) { //invalid line
            return "";
        }
        if (marrayNameParts[0].contains("NULL VALUES")) {
            result = marrayNameParts[0].split("NULL VALUES")[0].trim();
        } else {
            result = marrayNameParts[0].trim();
        }
        return result;
    }

    private void initializeMarrayTypes() throws RasdamanException, PetascopeException {
        Object result = RasUtil.executeRasqlQuery(QUERY_MARRAY_TYPES);
        RasQueryResult queryResult = new RasQueryResult(result);
        String[] fullStringResult = queryResult.toString().split("\0");
        for (String marrayLine : fullStringResult) {
            String marrayName = parseMarrayName(marrayLine);
            String marrayStructure = parseMarrayStructure(marrayLine);
            marrayTypeDefinitions.put(marrayName, marrayStructure);
        }
    }

    private void initializeStructRegistry() throws RasdamanException, PetascopeException {
        Object result = RasUtil.executeRasqlQuery(QUERY_STRUCT_TYPES);
        RasQueryResult queryResult = new RasQueryResult(result);
        String[] fullStringResult = queryResult.toString().split("\0");
        for (String i : fullStringResult) {
            String[] parts = i.split("CREATE TYPE ");
            String typeName = "";
            String typeStructure = "";
            if (parts.length > 1) {
                String[] nameParts = parts[1].split(" ");
                if (nameParts.length > 0) {
                    typeName = nameParts[0].trim();
                }
            }
            String[] structParts = i.split("AS");
            if (structParts.length > 1) {
                typeStructure = "struct " + structParts[1].trim().replace("(", "{").replace(")", "}");
            }
            structTypeDefinitions.put(typeName, typeStructure);
        }
    }

    private String parseMarrayName(String marrayLine) {
        String[] parts = marrayLine.split("CREATE TYPE ");
        if (parts.length < 2) { //invalid line
            return "";
        }
        String[] marrayParts = parts[1].split(" ");
        if (marrayParts.length < 1) { //invalid line
            return "";
        }
        return marrayParts[0];
    }

    private String parseMarrayStructure(String marrayLine) throws RasdamanException {
        String[] parts = marrayLine.split("AS");
        if (parts.length < 2) { //invalid line
            return "";
        }
        String marrayStructure = parts[1].trim();
        String[] marrayStructureParts = marrayStructure.split("MDARRAY");
        if (marrayStructureParts.length < 2) { //invalid line
            return "";
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
     * @deprectaed
     * Parses one rasdl line retrieving the base type and the domain type. We differentiate each line based on its contents
     * into two cases:
     * Marray definition: typedef marray <struct { char red, char green, char blue }, 2> RGBImage;
     * Set definition:    typedef set <GreyImage> GreySet;
     *
     * @param line
     */
    private void parseRasdlLine(String line) {
        if (line.startsWith(RASDL_MARRAY_IDENTIFIER)) {
            String[] marrayParts = line.substring(RASDL_MARRAY_IDENTIFIER.length()).split(">");
            if (marrayParts.length < 2) { //invalid line
                return;
            }
            String marrayName = marrayParts[1].replace(";", "").trim();
            String marrayDef = marrayParts[0].replace("<", "").trim();
            marrayTypeDefinitions.put(marrayName, marrayDef);
        } else if (line.startsWith(RASDL_SET_IDENTIFIER)) {
            String[] setParts = line.substring(RASDL_SET_IDENTIFIER.length()).split(">");
            if (setParts.length < 2) {
                return;
            }
            String[] setNameWithNullValuesParts = setParts[1].replace(";", "").trim().split(" ");
            String setName = setNameWithNullValuesParts[setNameWithNullValuesParts.length - 1];
            String marrayTypeName = setParts[0].replace(">", "").replace("<", "").trim();
            setTypeDefinitions.add(Pair.of(setName, marrayTypeName));
        }
    }

    private void reinitialize() throws PetascopeException {
        structTypeDefinitions.clear();
        setTypeDefinitions.clear();
        marrayTypeDefinitions.clear();
        typeRegistry.clear();
        initializeRegistry();
    }

    /**
     * Builds the registry from the collected types gathered by parsing the rasdl output
     */
    private void buildRegistry() {
        for (Pair<String, String> entry : setTypeDefinitions) {
            String domainType = marrayTypeDefinitions.get(entry.getValue());
            if (domainType != null) {
                String[] domainTypeParts = domainType.split(",");
                if (domainTypeParts.length >= 2) {
                    String[] baseTypeParts = ArrayUtils.remove(domainTypeParts, domainTypeParts.length - 1);
                    String baseType = StringUtils.join(baseTypeParts, "");
                    String[] nullParts = setTypeNullValues.get(entry.getKey()).split(",");
                    List<NilValue> nullValues = new ArrayList<NilValue>();
                    for (String val : nullParts) {
                        if (!val.isEmpty()) {
                            //if the value that is parsed is an interval with the same limits (e.g. 5:5), add only 1
                            //value. This is needed because currently there is a bug when creating types via rasql,
                            //which doesn't allow single values to be specified. However, petascope needs to display single
                            //values when presenting the output to the user.
                            if (val.contains(":")) {
                                String[] parts = val.split(":");
                                if (parts.length == 2 & parts[0].equals(parts[1])) {
                                    val = parts[0];
                                }
                            }
                            NilValue nullValue = new NilValue(val, "");
                            nullValues.add(nullValue);
                        }
                    }
                    typeRegistry.put(entry.getKey(), new TypeRegistryEntry(baseType, domainType, nullValues));
                }
            }
        }
    }


    /**
     * Internal class for keeping track of the type entries
     */
    public class TypeRegistryEntry {

        private TypeRegistryEntry(String baseType, String domainType, List<NilValue> nullValues) {
            this.baseType = baseType;
            this.domainType = domainType;
            this.nullValues = nullValues;
        }

        /**
         * Returns the base type of this type entry
         *
         * @return
         */
        public String getBaseType() {
            return baseType;
        }

        /**
         * Returns the domain type of this type entry
         *
         * @return
         */
        public String getDomainType() {
            return domainType;
        }

        @Override
        public String toString() {
            return "TypeRegistryEntry{" +
                   "baseType='" + baseType + '\'' +
                   ", domainType='" + domainType + '\'' +
                   ", nullValues='" + nullValues.toString() + "'" +
                   '}';
        }

        public Boolean equals(TypeRegistryEntry another) {
            return another.getBaseType().equals(this.baseType) &&
                   another.getDomainType().equals(this.domainType);
        }

        public List<NilValue> getNullValues() {
            return nullValues;
        }

        private String baseType;
        private String domainType;
        private List<NilValue> nullValues;
    }

    private final HashMap<String, TypeRegistryEntry> typeRegistry = new HashMap<String, TypeRegistryEntry>();
    private final HashMap<String, String> marrayTypeDefinitions = new HashMap<String, String>();
    private final ArrayList<Pair<String, String>> setTypeDefinitions = new ArrayList<Pair<String, String>>();
    private HashMap<String, String> setTypeNullValues = new HashMap<String, String>();
    private HashMap<String, String> structTypeDefinitions = new HashMap<String, String>();
    private final String RASDL_MARRAY_IDENTIFIER = "typedef marray";
    private final String RASDL_SET_IDENTIFIER = "typedef set";
    private final Logger log = LoggerFactory.getLogger(TypeRegistry.class);
    private final static Integer GENERATED_TYPE_NAME_LENGTH = 30;
    private final static String QUERY_MARRAY_TYPES = "SELECT a FROM RAS_MARRAY_TYPES a";
    private final static String QUERY_STRUCT_TYPES = "SELECT a FROM RAS_STRUCT_TYPES a";
    private final static String QUERY_SET_TYPES = "SELECT a FROM RAS_SET_TYPES a";
    private final static String QUERY_CREATE_MARRAY_TYPE = "CREATE TYPE $typeName AS $typeStructure MDARRAY [$dimensions]";
    private final static String QUERY_CREATE_SET_TYPE = "CREATE TYPE $typeName AS SET ($marrayTypeName $nullValues)";
    private final static String NULL_VALUES_TEMPLATE = "NULL VALUES [$values]";
    private final static String QUERY_CREATE_STRUCT_TYPE = "CREATE TYPE $structTypeName AS ( $structStructure )";
}
