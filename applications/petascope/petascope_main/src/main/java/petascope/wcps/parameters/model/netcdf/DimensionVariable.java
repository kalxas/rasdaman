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

/**
 * This class represents a netcdf variable that corresponds to a dimesnion.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class DimensionVariable<T> implements Variable {

    private String type;
    private List<T> data;
    private String name;
    private DimensionVariableMetadata metadata;

    public DimensionVariable(String type, List<T> data, String name, DimensionVariableMetadata metadata) {
        this.type = type;
        this.data = data;
        this.name = name;
        this.metadata = metadata;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DimensionVariableMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DimensionVariableMetadata metadata) {
        this.metadata = metadata;
    }
}
