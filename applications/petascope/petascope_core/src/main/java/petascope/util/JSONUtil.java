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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/
package petascope.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * JSON ultilities
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class JSONUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Escape quote " to \"
     */
    public static String escapeQuote(String text) {
        text = text.replace("\"", "\\\"");
        return text;
    }
    
    /**
     * Serialize an object to JSON string with indentation (human readable)
     */
    public static String serializeObjectToJSONString(Object obj) throws PetascopeException {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String result = serializeObjectToString(obj);
        
        return result;
    }
    
    /**
     * Serialize an object to JSON string without indentation (e.g: rasql encode(extra_params_json_inline))
     */
    public static String serializeObjectToJSONStringNoIndentation(Object obj) throws PetascopeException {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        String result = serializeObjectToString(obj);
        
        return result;
    }
    
    /**
     * Deserialize a JSON string to an object
     */
    public static Object deserialize(String json, Class c) throws PetascopeException {
        Object result = null;
        try {
            result = objectMapper.readValue(json, c);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                    "Cannot deserialize JSON string to object '" + c.getName() + "'. Reason: " + ex.getMessage(), ex);
        }
        
        return result;
    }
    
    /**
     * Serialize a JSON object to a string.
     */
    private static String serializeObjectToString(Object obj) throws PetascopeException {
        String json;
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, "Cannot serialize object to JSON string. Reason: " + ex.getMessage(), ex);
        }
        return json;
    }

    /**
     * Check if an input JSON string is valid
     * @param jsonInString
     * @return
     */
    public static boolean isJsonValid(String jsonInString) {
        if (StringUtils.isEmpty(jsonInString)) {
            return false;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonInString);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    /**
     * Clone an input object by serializing it to JSON string and deserializing it back to a new object
     */
    public static Object clone(Object inputObject) throws PetascopeException {
        if (inputObject == null) {
            return null;
        }
        
        String json = serializeObjectToJSONString(inputObject);
        Object result = null;
        
        try {
            result = objectMapper.readValue(json, inputObject.getClass());
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Cannot clone object. Reason: " + ex.getMessage());
        }
        
        return result;
    }
    
    public static final String EMPTY_ROOT_NODE = "{}";
}
