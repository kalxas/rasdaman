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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * CIS 1.1 (Annex C)
 * 
 * Only GeneralGridCoverage has domainSet which contains GeneralGrid.
 * 
 */
@Entity
@Table(name = GeneralGridDomainSet.TABLE_NAME)
@PrimaryKeyJoinColumn(name = GeneralGridDomainSet.COLUMN_ID, referencedColumnName = DomainSet.COLUMN_ID)
public class GeneralGridDomainSet extends DomainSet implements Serializable {
    
    public static final String TABLE_NAME = "general_grid_domain_set";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    @OneToOne (cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn (name = GeneralGrid.COLUMN_ID)
    private GeneralGrid generalGrid;
    
    @Column(name = "coverage_origin")
    // Origin is the center of a pixel with the formula 
    // (positive: min + 0.5 * abs(resolution), negative, min - 0.5 * abs(resolution)
    // e.g: min Long is: -180, resolution is 1, then origin of Long axis is: -180 + 0.5 = -179.5
    //      min Lat is: 90 (as it is negative axis), then origin of Lat axis is: 90 - 0.5 = 89.5 
    private String origin;
    
    public GeneralGridDomainSet() {
        
    }
    
    public GeneralGridDomainSet(GeneralGrid generalGrid) {
        this.generalGrid = generalGrid;
    }

    public GeneralGrid getGeneralGrid() {
        return generalGrid;
    }
    
    public String getOrigin() {
        return this.origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setGeneralGrid(GeneralGrid generalGrid) {
        this.generalGrid = generalGrid;
    }
}
