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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import nu.xom.Attribute;
import nu.xom.Element;
import org.rasdaman.config.ConfigManager;
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
 * <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://localhost:8080/rasdaman/ows?service=WMS&request=GetLegendGraphic&format=image/png&layer=cov1&style=color"/>
 * </LegendURL>
 *
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = LegendURL.TABLE_NAME)
public class LegendURL implements Serializable {

    public static final String TABLE_NAME = TABLE_PREFIX + "_legend_url";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    // MIME type of logo (e.g: image/png)
    @Column(name = "format")
    private String format;

    @Column(name = "online_resource_url", length = 1000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String onlineResourceURL;

    @Column(name = "width")
    private int width;

    @Column(name = "logo_height")
    private int height;
    
    @Column(name = "legend_graphic_base64")
    @Lob
    private String legendGraphicBase64;
    
    public LegendURL() {

    }
    
    public LegendURL(String format, String legendGraphicBase64, String onlineResourceURL) {
        this.format = format;
        this.legendGraphicBase64 = legendGraphicBase64;
        this.onlineResourceURL = onlineResourceURL;
    }
    
    public LegendURL(String format, String onlineResourceURL) {
        this(format, null, onlineResourceURL);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * e.g. http://localhost:8080/rasdaman/ows?service=WMS&request=GetLegendGraphic&format=image%2Fpng&width=20&height=20&layer=ne%3Ane
     */
    public String getOnlineResourceURL() {
        if (onlineResourceURL != null) {
            if (!onlineResourceURL.contains("?")) {
                return ConfigManager.PETASCOPE_ENDPOINT_URL + "?" + onlineResourceURL;
            } else {
                return onlineResourceURL;
            }
        }
        
        return null;
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

    public String getLegendGraphicBase64() {
        return legendGraphicBase64;
    }

    public void setLegendGraphicBase64(String legendGraphicBase64) {
        this.legendGraphicBase64 = legendGraphicBase64;
    }
}
