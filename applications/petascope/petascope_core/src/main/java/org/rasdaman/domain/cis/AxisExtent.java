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
import java.math.BigDecimal;
import javax.persistence.*;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.TimeUtil;

/**
 * CIS 1.1
 *
 * If present, the envelope of a coverage instantiating class coverage shall
 * consist of a CIS::EnvelopeByAxis element (NOTE: Only used when the optional
 * CIS::Envelope element exists)
 *
 * Sequence of extents of the grid along a specific axis, exactly one for each
 * axis defined in the srsName CRS. for each axis in envelope there is exactly
 * one matching CRS axis with axisLabel = CRS axisAbbrev for this axis and
 * uomLabel = unit of measure for this axis.
 *
 */
@Entity
@Table(name = AxisExtent.TABLE_NAME)
public class AxisExtent implements Serializable {

    public static final String TABLE_NAME = "axis_extent";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "axis_label")
    private String axisLabel;

    @Column(name = "lower_bound")
    private String lowerBound;

    @Column(name = "upper_bound")
    private String upperBound;

    @Column(name = "uom_label")
    private String uomLabel;

    @Column(name = "resolution")
    private BigDecimal resolution;
    
    @Column(name = "srs_name", length=1000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    // The CRS which contains axis
    private String srsName;
    
    @Column(name = "axis_type")
    // e.g: X, Y, T,..
    private String axisType;

    public AxisExtent() {

    }
    
    public AxisExtent(String axisLabel, String srsName, String uomLabel, String lowerBound, String upperBound, BigDecimal resolution, String axisType) {
        this.axisLabel = axisLabel;
        this.srsName = srsName;
        this.uomLabel = uomLabel;
        this.lowerBound = lowerBound;        
        this.upperBound = upperBound;
        this.resolution = resolution;
        this.axisType = axisType;
    }

    public String getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(String upperBound) {
        this.upperBound = upperBound;
    }

    public String getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(String lowerBound) {
        this.lowerBound = lowerBound;
    }

    public String getUomLabel() {
        return uomLabel;
    }

    public void setUomLabel(String uomLabel) {
        this.uomLabel = uomLabel;
    }

    public String getAxisLabel() {
        return axisLabel;
    }

    public void setAxisLabel(String axisLabel) {
        this.axisLabel = axisLabel;
    }

    public BigDecimal getResolution() {
        return resolution;
    }

    public void setResolution(BigDecimal resolution) {
        this.resolution = resolution;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    public String getAxisType() {
        return axisType;
    }

    public void setAxisType(String axisType) {
        this.axisType = axisType;
    }
    
     /**
     * Return the geo lower bound in numbers (as they could be in Datetime
     * string also)
     *
     * @return
     * @throws PetascopeException
     * @throws SecoreException
     */
    @JsonIgnore
    public BigDecimal getLowerBoundNumber() throws PetascopeException {
        BigDecimal number = null;
        if (this.lowerBound.contains("\"")) {
            String axisUoM = this.getUomLabel();
            String datumOrigin = CrsUtil.getDatumOrigin(this.getSrsName());
            number = TimeUtil.countOffsets(datumOrigin, this.lowerBound, axisUoM, BigDecimal.ONE);
            return number;
        } else {
            return BigDecimalUtil.stripDecimalZeros(new BigDecimal(this.lowerBound));
        }
    }
    
    /**
     * Return the geo uppwer bound in numbers (as they could be in DateTtime
     * string also)
     *
     * @return
     * @throws PetascopeException
     * @throws SecoreException
     */
    @JsonIgnore
    public BigDecimal getUpperBoundNumber() throws PetascopeException {
        BigDecimal number = null;
        if (this.upperBound.contains("\"")) {
            String axisUoM = this.getUomLabel();
            String datumOrigin = CrsUtil.getDatumOrigin(this.getSrsName());
            number = TimeUtil.countOffsets(datumOrigin, this.upperBound, axisUoM, BigDecimal.ONE);
            return number;
        } else {
            return BigDecimalUtil.stripDecimalZeros(new BigDecimal(this.upperBound));
        }
    }
}
