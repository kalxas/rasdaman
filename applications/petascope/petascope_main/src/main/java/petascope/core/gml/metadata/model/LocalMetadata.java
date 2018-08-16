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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent the local metadata object inside coveage's extra metadata. It contains
 * list of local metadata child objects from each updated coverage slice.
 * 
 * When serializing in XML, it will be:
 * <slices>
 *    <slice>
 *     ...
 *    </slice>
 *    <slice>
 *    ...
 *    </slice>
 * </slices>
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class LocalMetadata {
    
    public static final String LOCAL_METADATATAG = "slices";
    
    @JsonProperty(value = LocalMetadataChild.LOCAL_METADATA_TAG)
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<LocalMetadataChild> localMetadataList;

    public LocalMetadata(List<LocalMetadataChild> localMetadataList) {
        this.localMetadataList = localMetadataList;
    }

    public LocalMetadata() {
        this.localMetadataList = new ArrayList<>();
    }

    public List<LocalMetadataChild> getLocalMetadataList() {
        return localMetadataList;
    }

    public void setLocalMetadataList(List<LocalMetadataChild> localMetadataList) {
        this.localMetadataList = localMetadataList;
    }
}
