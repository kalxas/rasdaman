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
package petascope.core.gml.metadata.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to represents local metadata object from each updated coverage slice.
 * <slice>
 * <local_attribute>value_1</local_attribute>
 * ...
 * <boundedBy> ... </boundedBy>
 * </slice>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class LocalMetadataChild {
    
    public static final String LOCAL_METADATA_TAG = "slice";

    private Map<String, String> localMetadataAttributesMap;
    
    private BoundedBy boundedBy;

    @JsonAnySetter
    // NOTE: To map an unknown list of properties, must use this annotation
    public void addKeyValue(String key, String value) {
        this.localMetadataAttributesMap.put(key, value);
    }

    
    public LocalMetadataChild() {
        this.localMetadataAttributesMap = new LinkedHashMap<>();
        this.boundedBy = new BoundedBy();
    }

    @JsonAnyGetter
    // NOTE: to unwrap the "map" from { "map": { "key": "value" } }, only keep { "key": "value" }
    public Map<String, String> getLocalMetadataAttributesMap() {
        return localMetadataAttributesMap;
    }
    
    public BoundedBy getBoundedBy() {
        return this.boundedBy;
    }

    public void setBoundedBy(BoundedBy boundedBy) {
        this.boundedBy = boundedBy;
    }

    public LocalMetadataChild(Map<String, String> localMetadataAttributesMap, BoundedBy boundedBy) {
        this.localMetadataAttributesMap = localMetadataAttributesMap;
        this.boundedBy = boundedBy;
    }
}
