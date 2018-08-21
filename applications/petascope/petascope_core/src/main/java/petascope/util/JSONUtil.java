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
import org.apache.commons.lang3.StringUtils;

/**
 * JSON ultilities
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class JSONUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Serialize an object to JSON string with indentation (human readable)
     */
    public static String serializeObjectToJSONString(Object obj) throws JsonProcessingException {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(obj);
        return json;
    }
    
    /**
     * Serialize an object to JSON string without indentation (e.g: rasql encode(extra_params_json_inline))
     */
    public static String serializeObjectToJSONStringNoIndentation(Object obj) throws JsonProcessingException {
        objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(obj);
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
    
    public static final String EMPTY_ROOT_NODE = "{}";
}
