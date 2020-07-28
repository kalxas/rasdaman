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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.core.XMLSymbols.LABEL_GENERAL_GRID_COVERAGE;
import petascope.exceptions.PetascopeException;
import petascope.util.JSONUtil;
import petascope.util.MIMEUtil;
import petascope.core.gml.metadata.model.CoverageMetadata;
import petascope.exceptions.ExceptionCode;
import petascope.wcps.encodeparameters.model.ColorPalette;
import static petascope.wcps.encodeparameters.model.ColorPalette.COLOR_PALETTE_TABLE_COVERAGE_METADATA;
import static petascope.wcps.encodeparameters.model.ColorPalette.COLOR_PALETTE_TABLE_SIZE;
import petascope.wcps.encodeparameters.model.Dimensions;
import petascope.wcps.encodeparameters.model.GeoReference;
import petascope.wcps.encodeparameters.model.JsonExtraParams;
import petascope.wcps.encodeparameters.model.NoData;
import petascope.wcps.encodeparameters.model.Variables;
import petascope.wcps.exception.processing.DeserializationExtraParamsInJsonExcception;
import petascope.wcps.exception.processing.InvalidCoverageMetadataToDeserializeException;
import petascope.wcps.handler.EncodeCoverageHandler;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.parameters.model.netcdf.NetCDFExtraParams;

/**
 *
 * Build encoding object then serialize it to JSON string.
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class SerializationEncodingService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(SerializationEncodingService.class);
    
    @Autowired
    private EncodeCoverageHandler encodeCoverageHandler;
    @Autowired
    private CoverageRepositoryService coverageRepostioryService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public SerializationEncodingService() {
        
    }
    
    /**
     * Depends on encoding (with PNG or TIFF) then colorPallete should be added as JSON extra params.
     */
    public static void addColorPalleteToJSONExtraParamIfPossible(String rasqlFormat, CoverageMetadata coverageMetadata, JsonExtraParams jsonExtraParams) throws PetascopeException {
        ColorPalette colorPalette = null;
        
        boolean colorPaletteTableInMetadata = true;
        
        if (jsonExtraParams.getColorPalette() != null) {
            // If input query contains colorPaletteTable then uses it
            colorPalette = jsonExtraParams.getColorPalette();
            colorPaletteTableInMetadata = false;
        } else {
            // Add colorPaleteTable for colorPalette if it exists in coverage's global metadata implicitly
            String colorPaleteTableValue = coverageMetadata.getGlobalAttributesMap().get(COLOR_PALETTE_TABLE_COVERAGE_METADATA);
            // Only try to deserialize if it is a JSON string
            if ( colorPaleteTableValue != null && colorPaleteTableValue.trim().startsWith("{")) {
                try {
                    // e.g:  "colorPaletteTable": "{'colorTable': [[0, 0, 0, 255], [216, 31, 30, 255], ...}"
                    colorPalette = objectMapper.readValue(colorPaleteTableValue.replace("'", "\""), ColorPalette.class);
                } catch (IOException ex) {
                    throw new InvalidCoverageMetadataToDeserializeException(ex.getMessage(), ex);
                }
            }
        }
                
        if (colorPalette != null) {
            if (colorPalette.getColorTable().size() != COLOR_PALETTE_TABLE_SIZE) {
                // Coverage metadata does not contains 256 RGB values for color table, just ignore it
                String errorMessage = "ColorTable does not contain exactly " + COLOR_PALETTE_TABLE_SIZE + " values. "
                                             + " Given: "  + colorPalette.getColorTable().size() + " values";
                if (!colorPaletteTableInMetadata) {
                    throw new PetascopeException(ExceptionCode.InvalidRequest, errorMessage);
                } else {
                    log.warn(errorMessage);
                }
            }
        
            if (rasqlFormat.equalsIgnoreCase(MIMEUtil.ENCODE_PNG) ||
                rasqlFormat.equalsIgnoreCase(MIMEUtil.MIME_PNG) ||
                rasqlFormat.equalsIgnoreCase(MIMEUtil.ENCODE_TIFF) ||
                rasqlFormat.equalsIgnoreCase(MIMEUtil.MIME_TIFF)) {
                jsonExtraParams.setColorPalette(colorPalette);
            }
        }
    }

    /**
     * Generate Rasql extra parameters in Json string from *old style* extra params of WCPS query (e.g: "nodata=0,1,2,3")
     * @param rasqlFormat
     * @param wcpsCoverageMetadata
     * @param netCDFExtraParams
     * @param geoReference
     * @return
     * @throws com.fasterxml.jackson.core.JsonProcessingException
     */
    public String serializeOldStyleExtraParamsToJson(String rasqlFormat, WcpsCoverageMetadata wcpsCoverageMetadata,
                                            NetCDFExtraParams netCDFExtraParams, GeoReference geoReference, boolean hasNoData) throws JsonProcessingException, PetascopeException {
        JsonExtraParams jsonExtraParams = new JsonExtraParams();
        if (netCDFExtraParams != null) {
            jsonExtraParams.setDimensions(new Dimensions(netCDFExtraParams.getDimensions()));
            jsonExtraParams.setVariables(new Variables(netCDFExtraParams.getVariables()));
        }

        jsonExtraParams.setNoData(new NoData(wcpsCoverageMetadata.getNodata()));
        
        // Extra metadata of coverage
        CoverageMetadata coverageMetadata = wcpsCoverageMetadata.getCoverageMetadata();
        jsonExtraParams.setMetadata(coverageMetadata.flattenMetadataMap());        
        
        jsonExtraParams.setGeoReference(geoReference);
        // NOTE: (JP2OpenJPEG) jpeg2000 will need to add "codec":"jp2" or it will not have geo-reference metadata in output
        if (rasqlFormat.equalsIgnoreCase(MIMEUtil.FORMAT_ID_OPENJP2)) {
            jsonExtraParams.getFormatParameters().put(MIMEUtil.CODEC, MIMEUtil.CODEC_JP2);
        }
        
        if (this.outputNeedsTranspose(rasqlFormat, jsonExtraParams, wcpsCoverageMetadata)) {
            // Automatically swap coverage imported YX grid axes order to XY grid axes order when the result is 2D XY
            this.addTransposeToExtraParams(wcpsCoverageMetadata, jsonExtraParams);
        }
        
        this.addColorPalleteToJSONExtraParamIfPossible(rasqlFormat, coverageMetadata, jsonExtraParams);
        
        String jsonOutput = JSONUtil.serializeObjectToJSONStringNoIndentation(jsonExtraParams);
        return jsonOutput;
    }


    /**
     * Generate Rasql extra parameters in Json string from *new style* extra params of WCPS query (e.g: "{...\"nodata\": [0,1,2,3]...}"
     * @param rasqlFormat
     * @param extraParams
     * @param wcpsCoverageMetadata
     * @param netCDFExtraParams
     * @param geoReference
     * @return
     * @throws JsonProcessingException
     */
    public String serializeNewStyleExtraParamsToJson(String rasqlFormat, String extraParams, WcpsCoverageMetadata wcpsCoverageMetadata,
                                             NetCDFExtraParams netCDFExtraParams, GeoReference geoReference) throws JsonProcessingException, IOException, PetascopeException {
        ObjectMapper objectMapper = new ObjectMapper();        
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        
        JsonExtraParams jsonExtraParams;
                
        try {
            // Deserialize encode extra params in JSON string to an object
            jsonExtraParams = objectMapper.readValue(extraParams, JsonExtraParams.class);
        } catch (IOException ex) {
            throw new DeserializationExtraParamsInJsonExcception(ex);
        }
        
        // CIS 1.1
        if (jsonExtraParams.getOutputType() != null && jsonExtraParams.getOutputType().equals(LABEL_GENERAL_GRID_COVERAGE)) {
            wcpsCoverageMetadata.setCoverageType(LABEL_GENERAL_GRID_COVERAGE);
        }

        // update each range of coverage with value from passing nodata_values
        encodeCoverageHandler.updateNoDataInRangeFileds(jsonExtraParams.getNoData().getNilValues(), wcpsCoverageMetadata);      
        // parse coverage's metadata XML/JSON string to a CoverageMetadata object
        CoverageMetadata coverageMetadata = wcpsCoverageMetadata.getCoverageMetadata();
        Map<String, String> metadataMap = coverageMetadata.flattenMetadataMap();

        if (jsonExtraParams.getMetadata().isEmpty()) {
            // If there is no metadata in extra params of encode() then metadata of encode() comes from coverage's metadata.
            jsonExtraParams.setMetadata(metadataMap);
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
        
        if (this.outputNeedsTranspose(rasqlFormat, jsonExtraParams, wcpsCoverageMetadata)) {
            // Automatically swap coverage imported YX grid axes order to XY grid axes order when the result is 2D XY
            this.addTransposeToExtraParams(wcpsCoverageMetadata, jsonExtraParams);
        }
        
        this.addColorPalleteToJSONExtraParamIfPossible(rasqlFormat, coverageMetadata, jsonExtraParams);

        String jsonOutput = JSONUtil.serializeObjectToJSONStringNoIndentation(jsonExtraParams);
        return jsonOutput;
    }
    
    /**
     * Check if output should add transpose internally in case of output is 2D YX grid axes order
     * and can be displayable (2D image).
     */
    private Boolean outputNeedsTranspose(String rasqlFormat, JsonExtraParams jsonExtraParams, WcpsCoverageMetadata metadata) {
        if (jsonExtraParams.getTranspose().isEmpty() && metadata.getAxes().size() == 2 
                && !metadata.isXYOrder() 
                && MIMEUtil.displayableMIME(rasqlFormat)) {
            // YX output grid axes and displayable image
            return true;
        }
        return false;
    }
    
    /**
     * Automatically add { \"transpose\": [0,1] }" when coverage result is 2D and with YX grid axes order.
     */
    private void addTransposeToExtraParams(WcpsCoverageMetadata metadata, JsonExtraParams jsonExtraParams) throws PetascopeException {
        String coverageId = metadata.getCoverageName();
        // NOTE: with coverage constructor inside WCPS query, it doesn't have rasdaman collection, so do nothing in this case.
        if (this.coverageRepostioryService.isInCache(coverageId)) {
            // e.g: geo order is Lat, Long check if grid order is also Lat, Long. If it is then need to add transpose
            Axis firstAxis = metadata.getAxes().get(0);
            int firstGridAxisOrder = firstAxis.getRasdamanOrder();
            Axis secondAxis = metadata.getAxes().get(1);
            int secondGridAxisOrder = secondAxis.getRasdamanOrder();
            if (firstGridAxisOrder < secondGridAxisOrder) {
                // equivalent to \"transpose\": [0,1]
                List<Integer> transposeList = new ArrayList<>();
                transposeList.add(0);
                transposeList.add(1);
                jsonExtraParams.setTranspose(transposeList);
            }
            
        }
    }
}
