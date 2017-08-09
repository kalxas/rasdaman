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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.util.JSONUtil;
import petascope.util.MIMEUtil;
import petascope.wcps.encodeparameters.model.CoverageMetadata;
import petascope.wcps.encodeparameters.model.Dimensions;
import petascope.wcps.encodeparameters.model.GeoReference;
import petascope.wcps.encodeparameters.model.JsonExtraParams;
import petascope.wcps.encodeparameters.model.NoData;
import petascope.wcps.encodeparameters.model.Variables;
import petascope.wcps.exception.processing.DeserializationExtraParamsInJsonExcception;
import petascope.wcps.handler.EncodeCoverageHandler;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.parameters.model.netcdf.NetCDFExtraParams;

/**
 *
 * Build encoding object then serialize it to JSON string.
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class SerializationEncodingService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(SerializationEncodingService.class);
    
    @Autowired
    private EncodeCoverageHandler encodeCoverageHandler;
    @Autowired
    private ExtraMetadataService extraMetadataService;

    public SerializationEncodingService() {
        
    }

    /**
     * Generate Rasql extra parameters in Json string from *old style* extra params of WCPS query (e.g: "nodata=0,1,2,3")
     * @param rasqlFormat
     * @param metadata
     * @param netCDFExtraParams
     * @param geoReference
     * @return
     * @throws com.fasterxml.jackson.core.JsonProcessingException
     */
    public String serializeExtraParamsToJson(String rasqlFormat, WcpsCoverageMetadata metadata,
                                            NetCDFExtraParams netCDFExtraParams, GeoReference geoReference) throws JsonProcessingException {
        JsonExtraParams jsonExtraParams = new JsonExtraParams();
        if (netCDFExtraParams != null) {
            jsonExtraParams.setDimensions(new Dimensions(netCDFExtraParams.getDimensions()));
            jsonExtraParams.setVariables(new Variables(netCDFExtraParams.getVariables()));
        }
        
        jsonExtraParams.setNoData(new NoData(metadata.getNodata()));        
        // e.g: netCDF some global metadata (Project = "This is another test file" ; Title = "This is a test file" ; jsonExtraParams.setMetadata(new Metadata(metadata.getMetadata()));)
        
        if (metadata.getMetadata() != null) {
            // Extra metadata of coverage
            jsonExtraParams.setMetadata(extraMetadataService.deserializeCoverageMetadata(metadata.getMetadata()).getGlobalAttributesMap());
        }
        
        jsonExtraParams.setGeoReference(geoReference);
        // NOTE: (JP2OpenJPEG) jpeg2000 will need to add "codec":"jp2" or it will not have geo-reference metadata in output
        if (rasqlFormat.equalsIgnoreCase(MIMEUtil.FORMAT_ID_OPENJP2)) {
            jsonExtraParams.getFormatParameters().put(MIMEUtil.CODEC, MIMEUtil.CODEC_JP2);
        }

        String jsonOutput = JSONUtil.serializeObjectToJSONString(jsonExtraParams);
        return jsonOutput;
    }


    /**
     * Generate Rasql extra parameters in Json string from *new style* extra params of WCPS query (e.g: "{...\"nodata\": [0,1,2,3]...}"
     * @param rasqlFormat
     * @param extraParams
     * @param metadata
     * @param netCDFExtraParams
     * @param geoReference
     * @return
     * @throws JsonProcessingException
     */
    public String serializeExtraParamsToJson(String rasqlFormat, String extraParams, WcpsCoverageMetadata metadata,
                                             NetCDFExtraParams netCDFExtraParams, GeoReference geoReference) throws JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();        
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        
        JsonExtraParams jsonExtraParams;
                
        try {
            jsonExtraParams = objectMapper.readValue(extraParams, JsonExtraParams.class);
        } catch (IOException ex) {
            log.error("Could not deserialize extra parameters in JSON format", ex);
            throw new DeserializationExtraParamsInJsonExcception();
        }

        // update each range of coverage with value from passing nodata_values
        encodeCoverageHandler.updateNoDataInRangeFileds(jsonExtraParams.getNoData().getNilValues(), metadata);        
        // parse the input global metadata if exists
        Map<String, String> inputGlobalMetadataMap = extraMetadataService.deserializeCoverageMetadata(metadata.getMetadata()).getGlobalAttributesMap();
        
        // e.g: netCDF some global metadata (Project = "This is another test file" ; Title = "This is a test file" ; jsonExtraParams.setMetadata(new Metadata(metadata.getMetadata()));)
        if (jsonExtraParams.getMetadata()== null) {
            jsonExtraParams.setMetadata(inputGlobalMetadataMap);
        } else {
            // merge global coverage's metadata with input extra metadata from WCPS query in JSON if exists
            if (metadata.getMetadata() != null && !metadata.getMetadata().isEmpty()) {
                for (Map.Entry<String, String> entry: inputGlobalMetadataMap.entrySet()) {
                    jsonExtraParams.getMetadata().put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        if (netCDFExtraParams != null) {
            jsonExtraParams.setDimensions(new Dimensions(netCDFExtraParams.getDimensions()));
            jsonExtraParams.setVariables(new Variables(netCDFExtraParams.getVariables()));
        }

        // NOTE: must consider important properties such as geoReference (crs, bbox)
        // (i.e: if crs and bbox are not passed from WCPS, use the metadata from coverage result)
        if (jsonExtraParams.getGeoReference() == null) {
            jsonExtraParams.setGeoReference(geoReference);
        } else {
            if (jsonExtraParams.getGeoReference().getCrs() == null) {
                jsonExtraParams.getGeoReference().setCrs(geoReference.getCrs());
            }
            if (jsonExtraParams.getGeoReference().getBoundingBox() == null) {
                jsonExtraParams.getGeoReference().setBoundingBox(geoReference.getBoundingBox());
            }
        }

        // NOTE: (JP2OpenJPEG) jpeg2000 will need to add "codec":"jp2" or it will not have geo-reference metadata in output
        if (rasqlFormat.equalsIgnoreCase(MIMEUtil.FORMAT_ID_OPENJP2)) {
            jsonExtraParams.getFormatParameters().put(MIMEUtil.CODEC, MIMEUtil.CODEC_JP2);
        }

        String jsonOutput = JSONUtil.serializeObjectToJSONString(jsonExtraParams);
        return jsonOutput;
    }
}
