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
package petascope.wcps2.encodeparameters.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

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
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
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

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @JsonUnwrapped
    public Metadata getMetadata() {
        return this.metadata;
    }

    public void setTranspose(Transpose transpose) {
        this.transpose = transpose;
    }

    @JsonUnwrapped
    public Transpose getTranspose() {
        return this.transpose;
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

    public void setColorPalette(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
    }

    @JsonUnwrapped
    public ColorPalette getColorPalette() {
        return this.colorPalette;
    }

    public void setFormatParameters(FormatParameters formatParameters) {
        this.formatParameters = formatParameters;
    }

    @JsonUnwrapped
    public FormatParameters getFormatParameters() {
        return this.formatParameters;
    }

    public void setConfigOptions(ConfigOptions configOptions) {
        this.configOptions = configOptions;
    }

    @JsonUnwrapped
    public ConfigOptions getConfigOptions() {
        return this.configOptions;
    }

    private Dimensions dimensions;
    private Variables variables;
    private Metadata metadata;
    private Transpose transpose;
    private GeoReference geoReference;
    private NoData nodata;
    private ColorPalette colorPalette;
    private FormatParameters formatParameters;
    private ConfigOptions configOptions;
}
