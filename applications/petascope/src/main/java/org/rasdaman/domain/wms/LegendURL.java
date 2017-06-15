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

/**
 * A Map Server may use zero or more LegendURL elements to provide an image(s)
 * of a legend relevant to each Style of a Layer. The Format element indicates
 * the MIME type of the legend. Width and height attributes may be provided to
 * assist client applications in laying out space to display the legend.
 *
 * e.g: <LegendURL width="20" height="20">
 * <Format>image/png</Format>
 * <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://access.planetserver.eu:8083/geoserver/ows?service=WMS&request=GetLegendGraphic&format=image%2Fpng&width=20&height=20&layer=global_dem%3Atiles_cut"/>
 * </LegendURL>
 *
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = LegendURL.TABLE_NAME)
public class LegendURL {

    public static final String TABLE_NAME = TABLE_PREFIX + "_legend_url";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = COLUMN_ID)
    private long id;

    public LegendURL() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // MIME type of logo (e.g: image/png)
    @Column(name = "format")
    private String logoFormat;

    @Column(name = "online_resource_url", length = 1000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String onlineResourceURL;

    @Column(name = "width")
    private int width;

    @Column(name = "logo_height")
    private int height;

    public String getLogoFormat() {
        return logoFormat;
    }

    public void setLogoFormat(String logoFormat) {
        this.logoFormat = logoFormat;
    }

    public String getOnlineResourceURL() {
        return onlineResourceURL;
    }

    public void setOnlineResourceURL(String onlineResourceURL) {
        this.onlineResourceURL = onlineResourceURL;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    /**
     * Return the representation of a LegendURL XML element in string
     * @return 
     */
    public String getRepresentation() {
        Element legendURLElement = new Element(XMLSymbols.LABEL_WMS_LEGEND_URL);
        Attribute widthAttribute = new Attribute(XMLSymbols.ATT_WMS_WIDTH, String.valueOf(this.width));
        Attribute heightAttribute = new Attribute(XMLSymbols.ATT_WMS_WIDTH, String.valueOf(this.height));
        legendURLElement.addAttribute(widthAttribute);
        legendURLElement.addAttribute(heightAttribute);
        
        // Format
        Element formatElement = new Element(XMLSymbols.LABEL_WMS_FORMAT);
        formatElement.appendChild(this.logoFormat);
        legendURLElement.appendChild(formatElement);
        
        // OnlineResource
        Element onlineResourceElement = new Element(XMLSymbols.LABEL_WMS_ONLINE_RESOURCE);
        Attribute hrefAttribute = new Attribute(XMLSymbols.PREFIX_XLINK + ":" + XMLSymbols.ATT_HREF, this.getOnlineResourceURL());
        onlineResourceElement.addAttribute(hrefAttribute);
        legendURLElement.appendChild(onlineResourceElement);
        
        return legendURLElement.toXML();
    }

}
