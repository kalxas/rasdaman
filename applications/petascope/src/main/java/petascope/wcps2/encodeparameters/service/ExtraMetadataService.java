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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/
package petascope.wcps2.encodeparameters.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Build extra metadata as Map<String, String>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class ExtraMetadataService {
     /**
     * Convert extra metadata of coverage from String to Map<String, String> to encode
     * @param extraMetadata
     * @return 
     */
    public static Map<String, String> convertExtraMetadata(String extraMetadata) {
        XmlMapper xmlMapper = new XmlMapper();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> convertedMetadata = null;
        //remove the slices and the gmlcov:metadata closing tag if it exists
        extraMetadata = removeMetadataSlices(extraMetadata).replace("<gmlcov:metadata />", "");
        //convert to object
        try {
            //find out the type
            if (extraMetadata.startsWith("<")) {
                //xml
                //the contents that the xmlMapper can read into a map must currently come from inside an outer tag, which is ignored
                //so we are just adding them
                convertedMetadata = xmlMapper.readValue(OUTER_TAG_START + extraMetadata + OUTER_TAG_END, Map.class);
            } else if (extraMetadata.startsWith("{")) {
                //json
                convertedMetadata = objectMapper.readValue(extraMetadata, new TypeReference<Map<String, String>>() {});
            }
        } catch (IOException e) {
            //failed, just give it as string
            convertedMetadata = new HashMap<String, String>();
            convertedMetadata.put(METADATA_STRING_KEY, extraMetadata);
        }
        return convertedMetadata;
    }
    
    /**
     * remove the slices and the gmlcov:metadata closing tag if it exists from extraMetadata
     * @param extraMetadata
     * @return 
     */
    private static String removeMetadataSlices(String extraMetadata) {
        String result = "";
        //remove all \n and double spaces
        extraMetadata = extraMetadata.replaceAll("\\s+", " ");
        if (extraMetadata.contains("<slices>") && extraMetadata.contains("</slices>")) {
            //xml
            String[] extraMetadataParts = extraMetadata.split("<slices>");
            String begin = extraMetadataParts[0];
            String[] endParts = extraMetadataParts[1].split("</slices>");
            String end = endParts[endParts.length - 1];
            result = begin + end;
        } else if (extraMetadata.contains("<slices />")) {
            //xml, no slices
            result = extraMetadata.replace("<slices />", "");
        } else if (extraMetadata.contains("{ \"slices\":")) {
            //json with slices as first element
            String[] extraMetadataParts = extraMetadata.split("\"slices\":");
            String begin = extraMetadataParts[0];
            String[] endParts = extraMetadataParts[1].split("],");
            String end = endParts[1];
            result = begin + end;
        } else if (extraMetadata.contains(", \"slices\"")) {
            //json, slices not first
            String[] extraMetadataParts = extraMetadata.split(", \"slices\":");
            String begin = extraMetadataParts[0];
            String[] endParts = extraMetadataParts[1].split("]");
            String end = endParts[1];
            result = begin + end;
        } else {
            //default
            result = extraMetadata;
        }
        return result;
    }
        
    private final static String OUTER_TAG_START = "<metadata>";
    private final static String OUTER_TAG_END = "</metadata>";
    private final static String METADATA_STRING_KEY = "metadata";
}
