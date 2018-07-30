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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.parameters.model.netcdf;

import java.util.List;
import java.util.Map;

/**
 * This class represents the netcdf parameters understandable by the rasdaman encode (export). decode (import) function.
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class NetCDFExtraParams {

    private List<String> dimensions;
    //@JsonSerialize(using = VariablesJsonSerializer.class)
    // NOTE: Using custom JSON serializer as we want a list of objects in JSON not an array of objects in JSON
    private Map<String, Variable> variables;    


    public NetCDFExtraParams(List<String> dimensions, Map<String, Variable> variables) {
        this.dimensions = dimensions;
        this.variables = variables;
    }

    public List<String> getDimensions() {
        return dimensions;
    }

    public Map<String, Variable> getVariables() {
        return variables;
    }
    
    public void setDimensions(List<String> dimensions) {
        this.dimensions = dimensions;
    }

    public void setVariables(Map<String, Variable> variables) {
        this.variables = variables;
    }
}
