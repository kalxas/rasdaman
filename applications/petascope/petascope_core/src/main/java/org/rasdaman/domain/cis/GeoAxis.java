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

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import petascope.util.TimeUtil;

/**
 *
 * It does not exist in CIS 1.1 but for convenience, RegularAxis and
 * IrregularAxis of GeneralGridCoverage will extends it
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = GeoAxis.TABLE_NAME)
@PrimaryKeyJoinColumn(name = GeoAxis.COLUMN_ID, referencedColumnName = Axis.COLUMN_ID)
public class GeoAxis extends Axis {

    public static final String TABLE_NAME = "geo_axis";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Column(name = "lower_bound")
    // This is the lowest geo value, not the lowest coefficient which is 0 by default
    // Set as string because when reading the GML from wcst_import, it can be in datetime format (e.g: "2010-01-20T02:01:05Z")
    private String lowerBound;

    @Column(name = "upper_bound")
    // This is the highest geo value, not the highest coefficient
    private String upperBound;

    @Column(name = "resolution")
    private String resolution;

    public GeoAxis() {

    }

    public BigDecimal getResolution() {
        return new BigDecimal(resolution);
    }

    public void setResolution(BigDecimal resolution) {
        this.resolution = resolution.toPlainString();
    }

    public String getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(String lowerBound) {
        this.lowerBound = lowerBound;
    }

    public String getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(String upperBound) {
        this.upperBound = upperBound;
    }

    // Helpers Method
    /**
     * Return the geo lower bound in numbers (as they could be in Datetime
     * string also)
     *
     * @return
     * @throws PetascopeException
     * @throws SecoreException
     */
    public BigDecimal getLowerBoundNumber() throws PetascopeException, SecoreException {
        BigDecimal number = null;
        if (this.lowerBound.contains("\"")) {
            String axisUoM = this.getUomLabel();
            String datumOrigin = CrsUtil.getDatumOrigin(this.getSrsName());
            number = TimeUtil.countOffsets(datumOrigin, this.lowerBound, axisUoM, BigDecimal.ONE);
            return number;
        } else {
            return new BigDecimal(this.lowerBound);
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
    public BigDecimal getUpperBoundNumber() throws PetascopeException, SecoreException {
        BigDecimal number = null;
        if (this.upperBound.contains("\"")) {
            String axisUoM = this.getUomLabel();
            String datumOrigin = CrsUtil.getDatumOrigin(this.getSrsName());
            number = TimeUtil.countOffsets(datumOrigin, this.upperBound, axisUoM, BigDecimal.ONE);
            return number;
        } else {
            return new BigDecimal(this.upperBound);
        }
    }

    /**
     * Check if the geoAxis is IrregularAxis
     *
     * @return
     */
    public boolean isIrregular() {
        if (this.getClass().equals(IrregularAxis.class)) {
            return true;
        }
        return false;
    }
}
