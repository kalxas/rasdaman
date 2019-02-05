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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.cis;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import petascope.core.AxisTypes;
import petascope.core.CrsDefinition;
import petascope.core.Pair;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;

/**
 *
 * CIS 1.1
 *
 * With the introduction of the CIS GeneralGridCoverage type and its unified
 * modeling of all grid types, the grid types of GMLCOV 1.0 , GML 3.3 , and
 * ReferenceableGrid- Coverage Extension are likely to get deprecated in future.
 */
@Entity
@Table(name = GeneralGridCoverage.TABLE_NAME)
@PrimaryKeyJoinColumn(name = GeneralGridCoverage.COLUMN_ID, referencedColumnName = Coverage.COLUMN_ID)
public class GeneralGridCoverage extends Coverage implements Serializable {

    public static final String TABLE_NAME = "general_grid_coverage";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    public GeneralGridCoverage() {

    }

    // DomainSet here is GeneralGridDomainSet
    // Helper Methods
    /**
     * Return the list of geo axes from current coverage
     *
     * @return
     */
    public List<GeoAxis> getGeoAxes() {
        List<GeoAxis> geoAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGeoAxes();

        return geoAxes;
    }

    /**
     *
     * Return the list IndexAxis of coverage's grid domain (cellDomainElements)
     *
     * @return
     */
    public List<IndexAxis> getIndexAxes() {
        List<IndexAxis> indexAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGridLimits().getIndexAxes();

        return indexAxes;
    }

    /**
     * Return the IndexAxis from the list of index axes by the grid axisOrder
     *
     * @param axisOrder
     * @return
     */
    public IndexAxis getIndexAxisByOrder(int axisOrder) {
        List<IndexAxis> indexAxes = this.getIndexAxes();

        for (IndexAxis indexAxis : indexAxes) {
            if (indexAxis.getAxisOrder() == axisOrder) {
                return indexAxis;
            }
        }

        return null;
    }

    /**
     * Return the IndexAxis from the list of index axes by the grid axis name as
     * grid index axis has the same name as geo axis, but the grid axes order
     * could be different with geo CRS axes order
     */
    public IndexAxis getIndexAxisByName(String axisLabel) {
        for (IndexAxis indexAxis : this.getIndexAxes()) {
            if (indexAxis.getAxisLabel().equals(axisLabel)) {
                return indexAxis;
            }
        }
        return null;
    }
    
    /**
     *
     * Return the geoAxis's CRS order from the list of geo axes by axis label
     * e.g: the list of geo axes is: Lat, Long, time, then Long's order is 1
     *
     * @param axisLabel
     * @return
     */
    public Integer getGeoAxisOrderByName(String axisLabel) {
        List<GeoAxis> geoAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGeoAxes();
        int i = 0;
        for (GeoAxis geoAxis : geoAxes) {
            if (geoAxis.getAxisLabel().equals(axisLabel)) {
                return i;
            }

            i++;
        }

        return null;
    }

    /**
     * Return the geo axis by it's name from the list of geo axes
     *
     * @param axisLabel
     * @return
     */
    public GeoAxis getGeoAxisByName(String axisLabel) {
        List<GeoAxis> geoAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGeoAxes();
        for (GeoAxis geoAxis : geoAxes) {
            if (geoAxis.getAxisLabel().equals(axisLabel)) {
                return geoAxis;
            }
        }

        return null;
    }
    
    /**
     * Return the XY geo axes of a coverage if possible
     * @return 
     */
    public Pair<GeoAxis, GeoAxis> getXYGeoAxes() throws PetascopeException, SecoreException {
        GeoAxis geoAxisX = null, geoAxisY = null;
        
        String coverageCRS = this.getEnvelope().getEnvelopeByAxis().getSrsName();
        List<GeoAxis> geoAxes = ((GeneralGridDomainSet) this.getDomainSet()).getGeneralGrid().getGeoAxes();
        
        int i = 0;
        for (GeoAxis geoAxis : geoAxes) {
            // x, y, t,...
            String axisType = CrsUtil.getAxisTypeByIndex(coverageCRS, i);

            if (axisType.equals(AxisTypes.X_AXIS)) {
                geoAxisX = geoAxis;
            } else if (axisType.equals(AxisTypes.Y_AXIS)) {
                geoAxisY = geoAxis;
            }
            
            i++;
        }
        
        // Coverage has XY geo axes
        if (geoAxisX != null && geoAxisY != null) {
            return new Pair<>(geoAxisX, geoAxisY);
        }
        
        return null;        
    }
}
