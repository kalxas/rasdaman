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
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.Axis;

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
    private List<LocalMetadataChild> localMetadataChildList;

    public LocalMetadata(List<LocalMetadataChild> localMetadataList) {
        this.localMetadataChildList = localMetadataList;
    }

    public LocalMetadata() {
        this.localMetadataChildList = new ArrayList<>();
    }

    public List<LocalMetadataChild> getLocalMetadataChildList() {
        return localMetadataChildList;
    }

    public void setLocalMetadataChildList(List<LocalMetadataChild> localMetadataChildList) {
        this.localMetadataChildList = localMetadataChildList;
    }
    
    /**
     * Iterate all envelopes of local metadata child list and build envelope subsets list for them.
     * As string lowerCorner/uperCorner is not usable to calculate.
     */
    public void buildEnvelopeSubsetsForChildList(List<Axis> axes) throws PetascopeException {
        
        if (this.localMetadataChildList.size() > 0) {
            Envelope firstEnvelope = this.localMetadataChildList.get(0).getBoundedBy().getEnvelope();
            if (firstEnvelope.getNumerOfAxes() != axes.size()) {
                return;
            }
        }
        
        for (LocalMetadataChild localMetadataChild : this.localMetadataChildList) {
            Envelope envelope = localMetadataChild.getBoundedBy().getEnvelope();
            envelope.buildNumericSubsets(axes);
        }
    }
}
