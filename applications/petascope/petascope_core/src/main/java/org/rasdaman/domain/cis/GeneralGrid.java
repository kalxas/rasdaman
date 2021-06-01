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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * CIS 1.1
 * 
 * Grided coverages have a grid as their domain set describing the direct positions in multi-
dimensional coordinate space, depending on the type of grid.
 */
@Entity
@Table(name = GeneralGrid.TABLE_NAME)
public class GeneralGrid implements Serializable {
    
    public static final String TABLE_NAME = "general_grid";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;
        
    @Column(name = "srs_name", length = 1000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    // Compound CRSs of all geo axes    
    private String srsName;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = GeneralGrid.COLUMN_ID)
    @OrderColumn(name = "geo_axes_order")
    // GeoAxis means it could be RegularAxis or IrregularAxis
    private List<GeoAxis> geoAxes;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = GridLimits.COLUMN_ID)
    // not needed in case grid consists of index axes only
    private GridLimits gridLimits;

    public GeneralGrid() {
        
    }

    public GeneralGrid(String srsName, List<GeoAxis> geoAxes) {        
        this(srsName, geoAxes, null);
    }
    
    public GeneralGrid(String srsName, List<GeoAxis> geoAxes, GridLimits gridLimits) {
        this.srsName = srsName;
        this.geoAxes = geoAxes;
        this.gridLimits = gridLimits;
    }

    public List<GeoAxis> getGeoAxes() {
        return geoAxes;
    }

    public void setGeoAxes(List<GeoAxis> geoAxes) {
        this.geoAxes = geoAxes;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    public GridLimits getGridLimits() {
        return gridLimits;
    }

    public void setGridLimits(GridLimits gridLimits) {
        this.gridLimits = gridLimits;
    }
}
