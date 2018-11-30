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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * This class represents the metadata for a variable representing a band in netcdf.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class BandVariableMetadata {

    private String description;

    private String units;

    private String definition;
    
    private Map<String, String> metadataMap;

    public BandVariableMetadata(String description, String units, String definition, Map<String, String> metadataMap) {
        this.description = description;
        this.units = units;
        this.definition = definition;
        this.metadataMap = metadataMap;
    }

    public BandVariableMetadata() {
    }

    public String getDescription() {
        return description;
    }

    public String getUnits() {
        return units;
    }

    public String getDefinition() {
        return definition;
    }

    @JsonAnyGetter
    // Unwrap the map to list of keys, values, e.g: "map:" {"a":"b"} serializes to "a":"b"
    public Map<String, String> getMetadata() {
        return metadataMap;
    }
}
