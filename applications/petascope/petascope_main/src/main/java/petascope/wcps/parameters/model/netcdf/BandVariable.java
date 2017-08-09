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
package petascope.wcps.parameters.model.netcdf;

/**
 * This class represents a netcdf variable that corresponds to a band.
 *
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class BandVariable implements Variable {

    // e.g: double, float32
    private String type;
    private String name;
    private BandVariableMetadata metadata;    

    public BandVariable(String dataType, String name, BandVariableMetadata metadata) {
        this.type = dataType;
        this.name = name;
        this.metadata = metadata;        
    }

    public String getType() {
        return type;
    }

    public BandVariableMetadata getMetadata() {
        return metadata;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMetadata(BandVariableMetadata metadata) {
        this.metadata = metadata;
    }

    public void setName(String name) {
        this.name = name;
    }
}
