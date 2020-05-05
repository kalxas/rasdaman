/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.gml.metadata.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import petascope.core.XMLSymbols;
import petascope.wcps.encodeparameters.model.AxesMetadata;
import petascope.wcps.encodeparameters.model.BandsMetadata;
/**
 * A class to represent the coverage's metadata from gmlcov:metadata element
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class CoverageMetadata {

    private Map<String, String> globalMetadataAttributesMap;
    
    public static final String XML_ROOT_OPEN_TAG = "<CoverageMetadata>";
    public static final String XML_ROOT_CLOSE_TAG = "</CoverageMetadata>";
    
    @JsonProperty(value = BandsMetadata.BANDS_METADATA_ROOT_TAG)
    private BandsMetadata bandsMetadata;
    
    @JsonProperty(value = AxesMetadata.AXES_METADATA_ROOT_TAG)
    private AxesMetadata axesMetadata;
    
    // <slices> <slice> ... </slice> <slice> ... </slice> </slices>
    @JsonProperty(value = LocalMetadata.LOCAL_METADATATAG)    
    private LocalMetadata localMetadata;
    
    @JsonAnySetter
    // NOTE: To map an unknown list of properties, must use this annotation
    public void addKeyValue(String key, String value) {
        this.globalMetadataAttributesMap.put(key, value);
    }

    public CoverageMetadata() {
        this.globalMetadataAttributesMap = new LinkedHashMap<>();
        this.bandsMetadata = new BandsMetadata();
        this.axesMetadata = new AxesMetadata();
        this.localMetadata = new LocalMetadata();
    }
    
    /**
     * If this object contains empty properties objects, then they should be stripped
     * to not show in the result if serialization
     */
    public void stripEmptyProperties() {
        if (this.axesMetadata != null && this.axesMetadata.getAxesAttributesMap().isEmpty()) {
            this.setAxesMetadata(null);
        }
        if (this.bandsMetadata != null && this.bandsMetadata.getBandsAttributesMap().isEmpty()) {
            this.setBandsMetadata(null);
        }
        if (this.localMetadata != null && this.localMetadata.getLocalMetadataChildList().isEmpty()) {
            this.setLocalMetadata(null);
        }
    }

    @JsonAnyGetter
    // NOTE: to unwrap the "map" from { "map": { "key": "value" } }, only keep { "key": "value" }
    public Map<String, String> getGlobalAttributesMap() {
        return globalMetadataAttributesMap;
    }

    public BandsMetadata getBandsMetadata() {
        return bandsMetadata;
    }

    public void setBandsMetadata(BandsMetadata bandsMetadata) {
        this.bandsMetadata = bandsMetadata;
    }

    public AxesMetadata getAxesMetadata() {
        return axesMetadata;
    }

    public void setAxesMetadata(AxesMetadata axesMetadata) {
        this.axesMetadata = axesMetadata;
    }

    public LocalMetadata getLocalMetadata() {
        return localMetadata;
    }

    public void setLocalMetadata(LocalMetadata localMetadata) {
        this.localMetadata = localMetadata;
    }

    /**
    * If coverage's metadata is not deserializable by Jackson, then 
    * result of DescribeCoverage or GetCoverage in GML should use the persisted metadata string in database
    * instead of empty
    */
    @JsonIgnore
    public boolean isIsNotDeserializable() {
        if (this.globalMetadataAttributesMap.isEmpty() && 
            this.axesMetadata == null && this.bandsMetadata == null && this.localMetadata == null) {
            return true;
        } else {
            if (this.axesMetadata != null && this.bandsMetadata != null && this.localMetadata != null) {
                if (this.axesMetadata.getAxesAttributesMap().isEmpty() && this.bandsMetadata.getBandsAttributesMap().isEmpty()
                    && this.localMetadata.getLocalMetadataChildList().isEmpty() && this.globalMetadataAttributesMap.isEmpty()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
    Check if input localMetadata already exists in list by checking <envelope>.
    */
    public boolean containLocalMetadataInList(LocalMetadataChild localMetadata) {
        boolean elementExist = false;
        if (this.localMetadata != null) {
            for (LocalMetadataChild element : this.localMetadata.getLocalMetadataChildList()) {
                if (element.getBoundedBy().getEnvelope().equals(localMetadata.getBoundedBy().getEnvelope())) {
                    elementExist = true;
                    break;
                }
            }
        }
        
        return elementExist;
    }
    
    /**
     * Add a new localMetadata to list of local metadata root.
     */
    public void addLocalMetadataToList(LocalMetadataChild localMetadata) {
        if (this.localMetadata != null) {
            this.localMetadata.getLocalMetadataChildList().add(localMetadata);
        }
    }
    
    /**
     * If global metadata (a map of string:string), nothing to do.
     * If local metadata (a list of LocalMetadataChild), aggregate by keys and concatenate values
     * from this list to 1 map of keys:concatenated values (concatenated values are comma separated values,
     * e.g: "fileReferenceHistory": "file_path_1,file_path_2,file_path_3" from list of LocalMetadataChild with 3 elements.
     */
    public Map<String, String> flattenMetadataMap() {
        
        Map<String, String> resultMap = new LinkedHashMap<>();
        resultMap.putAll(this.globalMetadataAttributesMap);
        
        if (this.localMetadata != null) {
            // Iterate keys, values in LocalMetadataChild list to aggreate them by keys
            for (LocalMetadataChild localMetadataChild : this.localMetadata.getLocalMetadataChildList()) {
                for (Entry<String, String> entry : localMetadataChild.getLocalMetadataAttributesMap().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    if (!resultMap.containsKey(key)) {
                        resultMap.put(key, value);
                    } else {
                        // Concatenate values to same key
                        String concatedValue = resultMap.get(key);
                        concatedValue = concatedValue + "," + value;
                        resultMap.put(key, concatedValue);
                    }
                }
            }
        }
        
        return resultMap;
    }
}
