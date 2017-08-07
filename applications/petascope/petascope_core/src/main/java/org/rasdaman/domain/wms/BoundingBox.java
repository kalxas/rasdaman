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

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import nu.xom.Attribute;
import nu.xom.Element;
import static org.rasdaman.domain.wms.Layer.TABLE_PREFIX;
import petascope.core.XMLSymbols;
import petascope.util.BigDecimalUtil;

/**
 * WMS service metadata shall declare one or more bounding boxes (as defined in
 * 6.7.4) for each Layer. A Bounding Box metadata element may either be stated
 * explicitly or may be inherited from a parent Layer. In XML, the <BoundingBox>
 * metadata element includes the following attributes: - CRS indicates the Layer
 * CRS that applies to this bounding box. - minx, miny, maxx, maxy indicate the
 * limits of the bounding box using the axis units and order of the specified
 * CRS. - resx and resy (optional) indicate the spatial resolution of the data
 * comprising the layer in those same units.
 *
 * NOTE: a layer can have multiple crss and each crs will need to create a
 * different bounding box, e.g:
 *
 * <CRS>EPSG:4326</CRS>
 * <CRS>CRS:84</CRS>
 *
 * The values of x,y depend on the CRS order so miny is min Longitude
 * (EPSG:4326).
 * <BoundingBox CRS="CRS:84" minx="-130.85168" miny="20.7052" maxx="-62.0054" maxy="54.1141"/>
 * <BoundingBox CRS="EPSG:4326" minx="20.7052" miny="-130.85168" maxx="54.1141" maxy="-62.0054"/>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = BoundingBox.TABLE_NAME)
public class BoundingBox {

    public static final String TABLE_NAME = TABLE_PREFIX + "_bounding_box";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    public BoundingBox() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column
    private String crs;

    @Column
    private String xmin;

    @Column
    private String ymin;

    @Column
    private String xmax;

    @Column
    private String ymax;

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public BigDecimal getXMin() {
        return new BigDecimal(xmin);
    }

    public void setXMin(BigDecimal xMin) {
        this.xmin = BigDecimalUtil.stripDecimalZeros(xMin).toPlainString();
    }

    public BigDecimal getYMin() {
        return new BigDecimal(ymin);
    }

    public void setYMin(BigDecimal ymin) {
        this.ymin = BigDecimalUtil.stripDecimalZeros(ymin).toPlainString();
    }

    public BigDecimal getXMax() {
        return new BigDecimal(xmax);
    }

    public void setXMax(BigDecimal xMax) {
        this.xmax = BigDecimalUtil.stripDecimalZeros(xMax).toPlainString();
    }

    public BigDecimal getYMax() {
        return new BigDecimal(ymax);
    }

    public void setYMax(BigDecimal ymax) {
        this.ymax = BigDecimalUtil.stripDecimalZeros(ymax).toPlainString();
    }

    @Override
    public String toString() {
        return "Bounding box: xmin=" + xmin + ", xmax=" + xmax
                + ", ymin=" + ymin + ", ymax=" + ymax;
    }

    /**
     * Create a BoundingBox XML element and get the XML string
     *
     * @return
     */
    public String getRepresentation() {
        
        Element bboxElement = new Element(XMLSymbols.LABEL_WMS_BOUNDING_BOX);
        Attribute crsAttribute = new Attribute(XMLSymbols.LABEL_WMS_CRS, this.getCrs());
        Attribute minxAttribute = new Attribute(XMLSymbols.ATT_WMS_MIN_X, xmin);
        Attribute minyAttribute = new Attribute(XMLSymbols.ATT_WMS_MIN_Y, ymin);
        Attribute maxxAttribute = new Attribute(XMLSymbols.ATT_WMS_MAX_X, xmax);
        Attribute maxyAttribute = new Attribute(XMLSymbols.ATT_WMS_MAX_Y, ymax);

        bboxElement.addAttribute(crsAttribute);
        bboxElement.addAttribute(minxAttribute);
        bboxElement.addAttribute(minyAttribute);
        bboxElement.addAttribute(maxxAttribute);
        bboxElement.addAttribute(maxyAttribute);

        return bboxElement.toXML();
    }
}
