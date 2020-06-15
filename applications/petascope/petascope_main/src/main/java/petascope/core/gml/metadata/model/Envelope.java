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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 * Class to represent <Envelope> element of a local metadata's slice of coverage's metadata.
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class Envelope {
   
    private String axisLabels;
    private int srsDimension;
    private String lowerCorner;
    private String upperCorner;
    
    // These properties are used internally for filtering local metadata by envelope only
    // by translating geo domains in string to list of numeric subsets (e.g: datetime string to BigDecimal)
    @JsonIgnore
    private List<Subset> envelopeSubsets;
    
    @JsonIgnore
    private SubsetParsingService subsetParsingService = new SubsetParsingService();
    

    public Envelope() {
        
    }
    
    public Envelope(String axisLabels, int srsDimension, String lowerCorner, String upperCorner) {
        this.axisLabels = axisLabels;
        this.srsDimension = srsDimension;
        this.lowerCorner = lowerCorner;
        this.upperCorner = upperCorner;
    }

    public String getAxisLabels() {
        return axisLabels;
    }

    public void setAxisLabels(String axisLabels) {
        this.axisLabels = axisLabels;
    }

    public int getSrsDimension() {
        return srsDimension;
    }

    public void setSrsDimension(int srsDimension) {
        this.srsDimension = srsDimension;
    }

    public String getLowerCorner() {
        return lowerCorner;
    }

    public void setLowerCorner(String lowerCorner) {
        this.lowerCorner = lowerCorner;
    }

    public String getUpperCorner() {
        return upperCorner;
    }

    public void setUpperCorner(String upperCorner) {
        this.upperCorner = upperCorner;
    }
    
    /**
     * Create list of numeric subsets based on lowerCorner and upperCorner to be used
     * internally from other classes.
     */
    public void buildNumericSubsets(List<Axis> axes) throws PetascopeException {
        String[] lowerBounds = lowerCorner.split(" ");
        String[] upperBounds = upperCorner.split(" ");
        
        this.envelopeSubsets = new ArrayList<>();
        
        List<WcpsSubsetDimension> wcpsSubsetDimensions = new ArrayList<>();
        
        for (int i = 0; i < axes.size(); i++) {
            Axis axis = axes.get(i);
            WcpsSubsetDimension wcpsSubsetDimension = new WcpsTrimSubsetDimension(axis.getLabel(), axis.getNativeCrsUri(), lowerBounds[i], upperBounds[i]);
            wcpsSubsetDimensions.add(wcpsSubsetDimension);
        }
        
        // Then, it can be converted to list of numeric subsets
        this.envelopeSubsets = this.subsetParsingService.convertToNumericSubsets(wcpsSubsetDimensions, axes);
    }

    public List<Subset> getEnvelopeSubsets() {
        return envelopeSubsets;
    }
    
    /**
     * Return the number of axes in coverage's metadata
     */
    @JsonIgnore
    public int getNumerOfAxes() {
        return this.axisLabels.split(" ").length;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Envelope other = (Envelope) obj;
        if (this.srsDimension != other.srsDimension) {
            return false;
        }
        if (!Objects.equals(this.axisLabels, other.axisLabels)) {
            return false;
        }
        if (!Objects.equals(this.lowerCorner, other.lowerCorner)) {
            return false;
        }
        if (!Objects.equals(this.upperCorner, other.upperCorner)) {
            return false;
        }
        return true;
    }
    
    
}
