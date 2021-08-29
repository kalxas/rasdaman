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
package petascope.wcps.encodeparameters.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Model class to store all the objects of extra params to serialize into JSON string.
 * 
 * e.g: for c in (test_mean_summer_airtemp) return encode(c, "tiff", "nodata=0")
 * translates to rasql
 * SELECT encode(c[*:*,*:*], "GTiff" ,
 *                "{\"nodata\": [0], \"geoReference\": { \"bbox\": { \"xmin\":-40.50, \"ymin\":25.00, \"xmax\":75.50, \"ymax\":75.50 },
 *                \"crs\": \"EPSG:4326\" } }" )
 * FROM test_eobstest AS c
 * 
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
// Don't return empty array e.g. "transpose": [] to rasql as this is an error
public class JsonExtraParams {
    
    public JsonExtraParams() {        
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    @JsonUnwrapped
    public Dimensions getDimensions() {
        return this.dimensions;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    @JsonUnwrapped
    public Variables getVariables() {
        return this.variables;
    }

    public void setGeoReference(GeoReference geoReference) {
        this.geoReference = geoReference;
    }
    
    public GeoReference getGeoReference() {
        return this.geoReference;
    }

    public void setNoData(NoData nodata) {
        this.nodata = nodata;
    }

    @JsonUnwrapped    
    public NoData getNoData() {
        return this.nodata;
    }
        
    public void setTranspose(List<Integer> transpose) {
        this.transpose = transpose;
    }

    @JsonUnwrapped    
    public List<Integer> getTranspose() {
        return this.transpose;
    }    

    public void setColorPalette(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
    }

    public ColorPalette getColorPalette() {
        return this.colorPalette;
    }

    public ColorMap getColorMap() {
        return colorMap;
    }

    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }
    
    public void setFormatParameters(Map<String, String> formatParameters) {
        this.formatParameters = formatParameters;
    }

    public Map<String, String> getFormatParameters() {
        return this.formatParameters;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, String> getMetadata() {
        return this.metadata;
    }
    
    public void setConfigOptions(Map<String, String> configOptions) {
        this.configOptions = configOptions;
    }

    public Map<String, String> getConfigOptions() {
        return this.configOptions;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    private Dimensions dimensions;
    private Variables variables;
    private GeoReference geoReference;
    private NoData nodata;
    // GDAL color format
    private ColorPalette colorPalette;
    // Rasdaman color format
    private ColorMap colorMap;
    private String outputType;
        
    private List<Integer> transpose = new ArrayList<>();    
    private Map<String, String> formatParameters = new HashMap<>();
    private Map<String, String> metadata = new HashMap<>();
    private Map<String, String> configOptions = new HashMap<>();
    
}
