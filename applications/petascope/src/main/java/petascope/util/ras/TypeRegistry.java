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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * Keeps track of the types that exist in the tracked rasdaman instance.
 *
 * @author <a href="alex@flanche.net">Alex Dumitru</a>
 * @author <a href="vlad@flanche.net">Vlad Merticariu</a>
 */
public class TypeRegistry {
    private static TypeRegistry Instance = new TypeRegistry();

    /**
     * Returns the instance to the singleton @link{TypeRegistry}
     *
     * @return
     */
    public static TypeRegistry getInstance() throws PetascopeException {
        if (!initialized) {
            Instance.initializeRegistry();
            initialized = true;
        }
        return Instance;
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

    public String createNewType(Integer numberOfDimensions, ArrayList<String> bandBaseTypes) throws PetascopeException {
        String output = "";
        String result;
        if (bandBaseTypes.size() == 1) {
            //simple types
            String marrayName = getRandomTypeName();
            output = MARRAY_TEMPLATE.replace(MARRAY_BASE_TYPE_KEY, bandBaseTypes.get(0))
                    .replace(MARRAY_DIM_KEY, numberOfDimensions.toString())
                    .replace(MARRAY_TYPE_NAME_KEY, marrayName) + "\n";
            String setName = getRandomTypeName();
            output += SET_TEMPLATE.replace(SET_BASE_TYPE_KEY, marrayName)
                    .replace(SET_TYPE_NAME, setName);
            result = setName;
        } else {
            //struct types
            String structName = getRandomTypeName();
            output = STRUCT_TEMPLATE.replace(STRUCT_NAME_KEY, structName)
                    .replace(STRUCT_CONTENTS_KEY, generateStructContents(bandBaseTypes)) + "\n";
            String marrayName = getRandomTypeName();
            output += MARRAY_TEMPLATE.replace(MARRAY_BASE_TYPE_KEY, structName)
                    .replace(MARRAY_DIM_KEY, numberOfDimensions.toString())
                    .replace(MARRAY_TYPE_NAME_KEY, marrayName) + "\n";
            String setName = getRandomTypeName();
            output += SET_TEMPLATE.replace(SET_BASE_TYPE_KEY, marrayName)
                    .replace(SET_TYPE_NAME, setName);
            result = setName;
        }
        try {
            //save the type
            saveType(output);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.WCSTTypeRegistryNotFound);
        }
        return result;
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
     * Saves a type into rasdl.
     *
     * @param contents
     * @throws IOException
     */
    private static void saveType(String contents) throws IOException {
        //generate the file path
        String filePath = GENERATED_TYPE_FILE_PREFIX + java.util.UUID.randomUUID().toString();
        File tmpTypeFile = new File(filePath);
        FileUtils.writeStringToFile(new File(filePath), contents);
        //add to rasdl
        Runtime.getRuntime().exec(ConfigManager.RASDAMAN_BIN_PATH + "rasdl -r " + filePath + " -i");
        //delete file
        tmpTypeFile.delete();
    }

    /**
     * Writes struct contents in asdl language.
     *
     * @param bandBaseTypes
     * @return
     */
    private static String generateStructContents(ArrayList<String> bandBaseTypes) {
        String output = "";
        for (String i : bandBaseTypes) {
            output += i + " " + getRandomTypeName() + "; ";
        }
        return output;
    }

    /**
     * Generates a random alphabetic string
     *
     * @return
     */
    private static String getRandomTypeName() {
        return RandomStringUtils.randomAlphabetic(GENERATED_TYPE_NAME_LENGTH);
    }

    /**
     * Collects the types from rasdl and inserts them into an internal registry
     */
    private void initializeRegistry() throws PetascopeException {
        try {
            log.trace("Initializing the type registry");
            Process process = Runtime.getRuntime().exec(ConfigManager.RASDAMAN_BIN_PATH + "rasdl -p");
            BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String currentLine;
            while ((currentLine = stdOutReader.readLine()) != null) {
                this.parseRasdlLine(currentLine);
            }
            this.buildRegistry();
            log.info("Succesfully initiated the type registry. Contents: {}", typeRegistry.toString());
        } catch (IOException e) {
            log.error(MessageFormat.format("Could not read the rasdaman type registry. Tried with {0}rasdl -p", ConfigManager.RASDAMAN_BIN_PATH));
            throw new PetascopeException(ExceptionCode.WCSTTypeRegistryNotFound);
        }
    }

    /**
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
                    typeRegistry.put(entry.getKey(), new TypeRegistryEntry(baseType, domainType));
                }
            }
        }
    }


    /**
     * Internal class for keeping track of the type entries
     */
    public class TypeRegistryEntry {

        private TypeRegistryEntry(String baseType, String domainType) {
            this.baseType = baseType;
            this.domainType = domainType;
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
                    '}';
        }

        public Boolean equals(TypeRegistryEntry another) {
            return another.getBaseType().equals(this.baseType) &&
                    another.getDomainType().equals(this.domainType);
        }

        private String baseType;
        private String domainType;
    }

    private final HashMap<String, TypeRegistryEntry> typeRegistry = new HashMap<String, TypeRegistryEntry>();
    private final HashMap<String, String> marrayTypeDefinitions = new HashMap<String, String>();
    private final ArrayList<Pair<String, String>> setTypeDefinitions = new ArrayList<Pair<String, String>>();
    private final String RASDL_MARRAY_IDENTIFIER = "typedef marray";
    private final String RASDL_SET_IDENTIFIER = "typedef set";
    private final Logger log = LoggerFactory.getLogger(TypeRegistry.class);
    private static boolean initialized = false;
    private static final String STRUCT_NAME_KEY = "$struct_name";
    private static final String STRUCT_CONTENTS_KEY = "$struct_contents";
    private final static String STRUCT_TEMPLATE = "struct " + STRUCT_NAME_KEY + "{" + STRUCT_CONTENTS_KEY + "};";
    private final static String MARRAY_BASE_TYPE_KEY = "$marray_base_type";
    private final static String MARRAY_DIM_KEY = "$marray_dim";
    private final static String MARRAY_TYPE_NAME_KEY = "$marray_type_name";
    private final static String MARRAY_TEMPLATE = "typedef marray <" + MARRAY_BASE_TYPE_KEY + "," + MARRAY_DIM_KEY + "> " + MARRAY_TYPE_NAME_KEY + ";";
    private final static String SET_BASE_TYPE_KEY = "$set_base_type";
    private final static String SET_TYPE_NAME = "$set_type_name";
    private final static String SET_TEMPLATE = "typedef set <" + SET_BASE_TYPE_KEY + "> " + SET_TYPE_NAME + ";";
    private final static String GENERATED_TYPE_FILE_PREFIX = "/tmp/WCSTType";
    private final static Integer GENERATED_TYPE_NAME_LENGTH = 30;
}
