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
package petascope.wcps.encodeparameters.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
//import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import petascope.wcps.encodeparameters.model.CoverageMetadata;
import petascope.wcps.exception.processing.InvalidCoverageMetadataToDeserializeException;

/**
 *
 * Build extra metadata as Map<String, String>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class ExtraMetadataService {

    private static final Logger log = LoggerFactory.getLogger(ExtraMetadataService.class);
    
    public ExtraMetadataService() {
        
    }  
    
    
    /**
     * Deserialize the coverage's metadata in XML/JSON format of gmlcov:metadata element
     * to and object to manipulate.
     * 
     * @param metadata
     * @return 
     */
    public CoverageMetadata deserializeCoverageMetadata(String metadata) {
        CoverageMetadata coverageMetadata = new CoverageMetadata();
        
        XmlMapper xmlMapper = new XmlMapper();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //remove the slices and the gmlcov:metadata closing tag if it exists
        metadata = removeMetadataSlices(metadata).replace("<gmlcov:metadata />", "");
        //convert to object
        try {
            //find out the type
            if (metadata.startsWith("<")) {
                //xml
                //the contents that the xmlMapper can read into a map must currently come from inside an outer tag, which is ignored
                //so we are just adding them
                coverageMetadata = xmlMapper.readValue(OUTER_TAG_START + metadata + OUTER_TAG_END, CoverageMetadata.class);
            } else if (metadata.startsWith("{")) {
                //json
                coverageMetadata = objectMapper.readValue(metadata, CoverageMetadata.class);
            }
        } catch (IOException e) {
            log.error("Cannot deserialize WCPS coverage's metadata in XML/JSON by Jackson", e);
            throw new InvalidCoverageMetadataToDeserializeException(e.getMessage());
        }
        
        return coverageMetadata;
    }    

    /**
     * remove the slices and the gmlcov:metadata closing tag if it exists from
     * extraMetadata
     *
     * @param extraMetadata
     * @return
     */
    private String removeMetadataSlices(String extraMetadata) {
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
