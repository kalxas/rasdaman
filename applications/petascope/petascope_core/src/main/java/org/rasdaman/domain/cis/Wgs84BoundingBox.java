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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.cis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import petascope.util.BigDecimalUtil;

/**
 * If coverage is geo-referenced, it needs to have a geo XY bounding box in EPSG:4326 to show the footprint
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */

@Entity
@Table(name = Wgs84BoundingBox.TABLE_NAME)
public class Wgs84BoundingBox implements Serializable {
    
    public static final String TABLE_NAME = "wgs84_bounding_box";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    @Id
    @JsonIgnore
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @Column(name = "min_long")
    private String minLong;
    
    @Column(name = "min_lat")
    private String minLat;
    
    @Column(name = "max_long")
    private String maxLong;
    
    @Column(name = "max_lat")
    private String maxLat;

    public Wgs84BoundingBox() {
    }

    public Wgs84BoundingBox(BigDecimal minLong, BigDecimal minLat, BigDecimal maxLong, BigDecimal maxLat) {
        this.minLong = BigDecimalUtil.stripDecimalZeros(minLong).toPlainString();
        this.minLat = BigDecimalUtil.stripDecimalZeros(minLat).toPlainString();
        this.maxLong = BigDecimalUtil.stripDecimalZeros(maxLong).toPlainString();
        this.maxLat = BigDecimalUtil.stripDecimalZeros(maxLat).toPlainString();
    }

    public long getId() {
        return id;
    }

    public BigDecimal getMinLong() {
        return new BigDecimal(minLong);
    }

    public BigDecimal getMinLat() {
        return new BigDecimal(minLat);
    }

    public BigDecimal getMaxLong() {
        return new BigDecimal(maxLong);
    }

    public BigDecimal getMaxLat() {
        return new BigDecimal(maxLat);
    }

    public void set(Wgs84BoundingBox wgs84BoundingBox) {
        this.minLong = wgs84BoundingBox.getMinLong().toString();
        this.minLat = wgs84BoundingBox.getMinLat().toString();
        this.maxLong = wgs84BoundingBox.getMaxLong().toString();
        this.maxLat = wgs84BoundingBox.getMaxLat().toString();
    }
    
    /**
     * Return this BBOX as WKT polygon in WGS84 CRS (Long Lat order)
     */
    @JsonIgnore
    public String getWKTPolygon() {
        
        // NOTE: postgis does not allow full world's map (-180 -90 180 90)
        BigDecimal minX = new BigDecimal(this.minLong);
        if (minX.compareTo(new BigDecimal("-180")) <= 0) {
            minX = new BigDecimal("-179.999");
        }
        BigDecimal minY = new BigDecimal(this.minLat);
        if (minY.compareTo(new BigDecimal("-90")) <= 0) {
            minY = new BigDecimal("-89.999");
        }
        
        BigDecimal maxX = new BigDecimal(this.maxLong);
        if (maxX.compareTo(new BigDecimal("180")) >= 0) {
            maxX = new BigDecimal("179.999");
        }
        BigDecimal maxY = new BigDecimal(this.maxLat);
        if (maxY.compareTo(new BigDecimal("90")) >= 0) {
            maxY = new BigDecimal("89.999");
        }
        
        String wkt = "POLYGON((";
        wkt += minX + " " + minY + ","
            + minX + " " + maxY + ","
            + maxX + " " + maxY + ","
            + maxX + " " + minY + ","
            + minX + " " + minY + "))";
        
        return wkt;
    }
    
    @Override
    public String toString() {
        return "BBox(Long, Lat): " + this.minLong + ", " + this.minLat + ", " + this.maxLong + ", " + this.maxLat;
    }
    
    
}
