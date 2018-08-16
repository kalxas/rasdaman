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

import java.util.Objects;

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
