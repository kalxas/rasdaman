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

import javax.persistence.*;
import java.util.List;

/**
 * CIS 1.1
 *
 * Except for an index axis (which is a bare array grid), coordinates in an axis
 * are expressed in some geodetic CRS or similar. Correspondingly, the grid
 * limits in the CIS::Axis structure contains information about the grid
 * boundaries in the coverage’s CRS.
 *
 * In addition, the limits of the underlying array are given by the
 * CIS::gridLimits component. This structure is optional because it is not
 * needed when all coverage axes are of type CIS::indexAxis , in which case the
 * boundary information is redundant.
 */
@Entity
@Table(name = GridLimits.TABLE_NAME)
public class GridLimits {

    public static final String TABLE_NAME = "grid_limits";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    public long id;

    @Column(name = "srs_name", length = 1000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String srsName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = GridLimits.COLUMN_ID)
    @OrderColumn(name = "index_axes_order")
    // all axes of the Index CRS referenced in srsName, in proper sequence
    private List<IndexAxis> indexAxes;

    public GridLimits() {

    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    public List<IndexAxis> getIndexAxes() {
        return indexAxes;
    }

    public void setIndexAxes(List<IndexAxis> indexAxes) {
        this.indexAxes = indexAxes;
    }
    
    // helper methods
    /**
     * Get IndexAxis (grid domain) from list of Index Axes
     * 
     * @param axisLabel
     * @return 
     */
    public IndexAxis getIndexAxisByLabel(String axisLabel) {
        for (IndexAxis indexAxis:this.indexAxes) {
            if (indexAxis.getAxisLabel().equals(axisLabel)) {
                return indexAxis;
            }
        }
        
        return null;
    }
}
