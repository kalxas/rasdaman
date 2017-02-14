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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import petascope.util.JsonUtil;
import petascope.wcps2.encodeparameters.model.Dimensions;
import petascope.wcps2.encodeparameters.model.GeoReference;
import petascope.wcps2.encodeparameters.model.JsonExtraParams;
import petascope.wcps2.encodeparameters.model.NoData;
import petascope.wcps2.encodeparameters.model.Variables;
import petascope.wcps2.handler.EncodeCoverageHandler;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.parameters.model.netcdf.NetCDFExtraParams;
import petascope.wcs2.extensions.FormatExtension;

/**
 *
 * Build encoding object then serialize it to JSON string.
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class SerializationEncodingService {

    private ExtraMetadataService extraMetadataService;

    public SerializationEncodingService(ExtraMetadataService extraMetadataService) {
        this.extraMetadataService = extraMetadataService;
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
        if (metadata != null) {
            jsonExtraParams.setNoData(new NoData(metadata.getNodata()));        
            // e.g: netCDF some global metadata (Project = "This is another test file" ; Title = "This is a test file" ; jsonExtraParams.setMetadata(new Metadata(metadata.getMetadata()));)
            if (metadata.getMetadata() != null) {
                jsonExtraParams.setMetadata(extraMetadataService.convertExtraMetadata(metadata.getMetadata()));
            }
        }
        jsonExtraParams.setGeoReference(geoReference);
        // NOTE: (JP2OpenJPEG) jpeg2000 will need to add "codec":"jp2" or it will not have geo-reference metadata in output
        if (rasqlFormat.equalsIgnoreCase(FormatExtension.FORMAT_ID_OPENJP2)) {
            jsonExtraParams.getFormatParameters().put(FormatExtension.CODEC, FormatExtension.CODEC_JP2);
        }

        String jsonOutput = JsonUtil.serializeToPojoJson(jsonExtraParams);
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
        JsonExtraParams jsonExtraParams = objectMapper.readValue(extraParams, JsonExtraParams.class);

        // update each range of coverage with value from passing nodata_values
        EncodeCoverageHandler.updateNoDataInRangeFileds(jsonExtraParams.getNoData().getNilValues(), metadata);
        Map<String, String> extraMetadata = extraMetadataService.convertExtraMetadata(metadata.getMetadata());
        
        // e.g: netCDF some global metadata (Project = "This is another test file" ; Title = "This is a test file" ; jsonExtraParams.setMetadata(new Metadata(metadata.getMetadata()));)
        if (jsonExtraParams.getMetadata() == null) {
            jsonExtraParams.setMetadata(extraMetadata);
        } else {
            // merge coverage extraMetadata with input extra metadata in JSON
            if (metadata.getMetadata() != null && !metadata.getMetadata().isEmpty()) {
                for (Map.Entry<String, String> entry: extraMetadata.entrySet()) {
                    jsonExtraParams.getMetadata().put(entry.getKey(), entry.getValue());
                }
            }
        }
        jsonExtraParams.setGeoReference(geoReference);

        if (netCDFExtraParams != null) {
            jsonExtraParams.setDimensions(new Dimensions(netCDFExtraParams.getDimensions()));
            jsonExtraParams.setVariables(new Variables(netCDFExtraParams.getVariables()));
        }

        // NOTE: must consider important properties such as geoReference (crs, bbox)
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
        if (rasqlFormat.equalsIgnoreCase(FormatExtension.FORMAT_ID_OPENJP2)) {
            jsonExtraParams.getFormatParameters().put(FormatExtension.CODEC, FormatExtension.CODEC_JP2);
        }

        String jsonOutput = JsonUtil.serializeToPojoJson(jsonExtraParams);
        return jsonOutput;
    }
}
