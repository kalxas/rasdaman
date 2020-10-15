/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.wms;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import nu.xom.Element;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import static org.rasdaman.domain.wms.Layer.TABLE_PREFIX;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.LABEL_WMS_EAST_BOUND_LONGITUDE;
import static petascope.core.XMLSymbols.LABEL_WMS_NORTH_BOUND_LATITUDE;
import static petascope.core.XMLSymbols.LABEL_WMS_SOUTH_BOUND_LATITUDE;
import static petascope.core.XMLSymbols.LABEL_WMS_WEST_BOUND_LONGITUDE;
import petascope.util.BigDecimalUtil;

/**
 * Every named Layer shall have exactly one <EX_GeographicBoundingBox> element
 * that is either stated explicitly or inherited from a parent Layer.
 * EX_GeographicBoundingBox states, via the elements westBoundLongitude,
 * eastBoundLongitude, southBoundLatitude, and northBoundLatitude, the minimum
 * bounding rectangle in decimal degrees of the area covered by the Layer.
 * EX_GeographicBoundingBox shall be supplied regardless of what CRS the map
 * server may support, but it may be approximate if the data are not natively in
 * geographic coordinates. The purpose of EX_GeographicBoundingBox is to
 * facilitate geographic searches without requiring coordinate transformations
 * by the search engine.
 *
 * e.g: <EX_GeographicBoundingBox>
 * <westBoundLongitude>-180</westBoundLongitude>
 * <eastBoundLongitude>180</eastBoundLongitude>
 * <southBoundLatitude>-90</southBoundLatitude>
 * <northBoundLatitude>90</northBoundLatitude>
 * </EX_GeographicBoundingBox>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = EXGeographicBoundingBox.TABLE_NAME)
public class EXGeographicBoundingBox implements Serializable {

    public static final String TABLE_NAME = TABLE_PREFIX + "_ex_geographic_bounding_box";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    public EXGeographicBoundingBox() {

    }

    // Min, Max Long and Min, Max Lat
    public EXGeographicBoundingBox(String westBoundLongitude, String eastBoundLongitude, String southBoundLatitude, String northBoundLatitude) {
        this.westBoundLongitude = westBoundLongitude;
        this.eastBoundLongitude = eastBoundLongitude;
        this.southBoundLatitude = southBoundLatitude;
        this.northBoundLatitude = northBoundLatitude;
    }
    
    public EXGeographicBoundingBox(Wgs84BoundingBox wgs84BoundingBox) {
        this.westBoundLongitude = wgs84BoundingBox.getMinLong().toPlainString();
        this.eastBoundLongitude = wgs84BoundingBox.getMaxLong().toPlainString();
        this.southBoundLatitude = wgs84BoundingBox.getMinLat().toPlainString();
        this.northBoundLatitude = wgs84BoundingBox.getMaxLat().toPlainString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column
    private String westBoundLongitude;

    @Column
    private String eastBoundLongitude;

    @Column
    private String southBoundLatitude;

    @Column
    private String northBoundLatitude;

    public BigDecimal getWestBoundLongitude() {
        return new BigDecimal(westBoundLongitude);
    }

    public void setWestBoundLongitude(BigDecimal westBoundLongitude) {
        this.westBoundLongitude = BigDecimalUtil.stripDecimalZeros(westBoundLongitude).toPlainString();
    }

    public BigDecimal getEastBoundLongitude() {
        return new BigDecimal(eastBoundLongitude);
    }

    public void setEastBoundLongitude(BigDecimal eastBoundLongitude) {
        this.eastBoundLongitude = BigDecimalUtil.stripDecimalZeros(eastBoundLongitude).toPlainString();
    }

    public BigDecimal getSouthBoundLatitude() {
        return new BigDecimal(southBoundLatitude);
    }

    public void setSouthBoundLatitude(BigDecimal southBoundLatitude) {
        this.southBoundLatitude = BigDecimalUtil.stripDecimalZeros(southBoundLatitude).toPlainString();
    }

    public BigDecimal getNorthBoundLatitude() {
        return new BigDecimal(northBoundLatitude);
    }

    public void setNorthBoundLatitude(BigDecimal northBoundLatitude) {
        this.northBoundLatitude = BigDecimalUtil.stripDecimalZeros(northBoundLatitude).toPlainString();
    }

    @Override
    public String toString() {
        return "minLong=" + this.westBoundLongitude + ", maxLong=" + this.eastBoundLongitude
                + "minLat=" + this.southBoundLatitude + ", maxLat=" + this.northBoundLatitude;
    }

    /**
     * Return the values of min, max of 2 axes to put inside
     * EX_GeographicBoundingBox XML element.
     *
     * @return
     */
    public String getReprenstation() {
        Element exBBoxElement = new Element(XMLSymbols.LABEL_WMS_EX_BBOX);

        Element westBoundLongitudeElement = new Element(LABEL_WMS_WEST_BOUND_LONGITUDE);
        westBoundLongitudeElement.appendChild(westBoundLongitude);
        Element eastBoundLongitudeElement = new Element(LABEL_WMS_EAST_BOUND_LONGITUDE);
        eastBoundLongitudeElement.appendChild(eastBoundLongitude);
        Element southBoundLatitudeElement = new Element(LABEL_WMS_SOUTH_BOUND_LATITUDE);
        southBoundLatitudeElement.appendChild(southBoundLatitude);
        Element northBoundLatitudeElement = new Element(LABEL_WMS_NORTH_BOUND_LATITUDE);
        northBoundLatitudeElement.appendChild(northBoundLatitude);

        exBBoxElement.appendChild(westBoundLongitudeElement);
        exBBoxElement.appendChild(eastBoundLongitudeElement);
        exBBoxElement.appendChild(southBoundLatitudeElement);
        exBBoxElement.appendChild(northBoundLatitudeElement);

        return exBBoxElement.toXML();
    }

}
