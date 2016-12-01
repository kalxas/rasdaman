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
package petascope.wcps2.decodeparameters.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import petascope.wcps2.decodeparameters.service.VariablesJsonSerializer;

import java.util.List;
import java.util.Map;

/**
 * This class represents the netcdf parameters understandable by the rasdaman decode function.
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class NetCDFExtraParams {

    private List<String> dimensions;
    @JsonSerialize(using = VariablesJsonSerializer.class)
    private List<Variable> variables;
    private List<Double> nodata;
    private Map<String, String> metadata;

    public GeoReference getGeoReference() {
        return geoReference;
    }

    public void setGeoReference(GeoReference geoReference) {
        this.geoReference = geoReference;
    }

    private GeoReference geoReference;


    public NetCDFExtraParams(List<String> dimensions, List<Variable> variables, List<Double> nodata, Map<String, String> metadata, GeoReference geoReference) {
        this.dimensions = dimensions;
        this.variables = variables;
        this.nodata = nodata;
        this.metadata = metadata;
        this.geoReference = geoReference;
    }

    public List<String> getDimensions() {
        return dimensions;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public List<Double> getNodata() {
        return nodata;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setDimensions(List<String> dimensions) {
        this.dimensions = dimensions;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public void setNodata(List<Double> nodata) {
        this.nodata = nodata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
