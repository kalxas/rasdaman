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

/**
 * Abstract class for all other types of axis (IndexAxis, RegularAxis, IrregularAxis, DistoredAxis,...)
 *  
 */
@Entity
@Table(name = Axis.TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Axis implements Serializable {
    
    public static final String TABLE_NAME = "axis";
    public static final String COLUMN_ID = TABLE_NAME + "_id";   
    
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "axis_label")
    private String axisLabel;
    
    @Column(name = "uom_label")
    private String uomLabel;
    
    @Column(name = "srs_name", length = 1000)   
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String srsName;
    
    public Axis() {
        
    }

    public Axis(String axisLabel, String uomLabel, String srsName) {
        this.axisLabel = axisLabel;
        this.uomLabel = uomLabel;
        this.srsName = srsName;
    }
    
    public String getAxisLabel() {
        return axisLabel;
    }

    public void setAxisLabel(String axisLabel) {
        this.axisLabel = axisLabel;
    }
    
    public String getUomLabel() {
        return uomLabel;
    }

    public void setUomLabel(String uomLabel) {
        this.uomLabel = uomLabel;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }
}
