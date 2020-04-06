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
import javax.persistence.*;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * CIS 1.1
 *
 * Axis type CIS::IndexAxis requires an Index CRS as its CRS, as defined in the
 * OGC Name Type Specification for Index CRSs.
 *
 * An Index CRS allows only integer coordinates with spacing (“resolution”) of
 * 1, hence resembling Cartesian coordinates; therefore, there is no resolution
 * value.
 *
 * A grid coverage containing exclusively axes of type IndexAxis technically
 * corresponds to a GMLCOV::GridCoverage, however, with a slightly differing
 * schema.
 *
 * An Index CRS allows only integer coordinates with spacing (“resolution”) of
 * 1, hence resembling Cartesian coordinates; therefore, there is no resolution
 * value.
 */
@Entity
@Table(name = IndexAxis.TABLE_NAME)
@PrimaryKeyJoinColumn(name = IndexAxis.COLUMN_ID, referencedColumnName = Axis.COLUMN_ID)
public class IndexAxis extends Axis implements Serializable {

    public static final String TABLE_NAME = "index_axis";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    // Index Axis uom is fixed for all coverages 
    public static final String UOM_LABEL = "GridSpacing";

    @Column(name = "lower_bound")
    private Long lowerBound;

    @Column(name = "upper_bound")
    private Long upperBound;
    
    // to know the rasdaman order of a grid axis as they could be different from GeoAxis
    // e.g: geo axis: EPSG: 4326 order is Lat (0), Long (1), but grid order is: Long (1), Lat (0)
    @Column(name = "grid_axis_order")
    private int axisOrder;

    public IndexAxis() {

    }

    public IndexAxis(String axisLabel, String srsName, String uomLabel, Long lowerBound, Long upperBound, int axisOrder) {
        super(axisLabel, uomLabel, srsName);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.axisOrder = axisOrder;
    }
    
    


    public Long getLowerBound() {
        return lowerBound;
    }
    
    public void setLowerBound(Long lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Long getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(Long upperBound) {
        this.upperBound = upperBound;
    }

    public int getAxisOrder() {
        return axisOrder;
    }

    public void setAxisOrder(int axisOrder) {
        this.axisOrder = axisOrder;
    }
}
