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
import javax.persistence.Table;
import static org.rasdaman.domain.wms.Layer.TABLE_PREFIX;

/**
 * Class to represent the Attribution field of a layer in WMS 1.3.0. According
 * to the standard, its definition is:
 * <p/>
 * The optional <Attribution> element provides a way to identify the source of
 * the geographic information used in Layer or collection of Layers. Attribution
 * encloses several optional elements: <OnlineResource> states the data
 * provider’s URL; <Title> is a human-readable string naming the data provider;
 * <LogoURL> is the URL of a logo image. Client applications may choose to
 * display one or more of these items. A <Format> element in LogoURL indicates
 * the MIME type of the logo image, and the attributes width and height state
 * the size of the image in pixels.
 *
 * e.g: <!-- Optional Title, URL and logo image of data provider. -->
 * <Attribution>
 * <Title>State College University</Title>
 * <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple"
 * xlink:href="http://www.university.edu/" />
 * <LogoURL width="100" height="100">
 * <Format>image/gif</Format>
 * <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
 * xlink:type="simple"
 * xlink:href="http://www.university.edu/icons/logo.gif" />
 * </LogoURL>
 * </Attribution>
 *
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = Attribution.TABLE_NAME)
public class Attribution implements Serializable {

    public static final String TABLE_NAME = TABLE_PREFIX + "_attribution";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    public Attribution() {

    }

    @Column(name = "title")
    private String title;

    @Column(name = "online_resource_url", length = 1000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String onlineResourceURL;

    @Column(name = "logo_url_format")
    private String logoURLFormat;

    @Column(name = "logo_url_width")
    private String logoURLWidth;

    @Column(name = "logo_url_height")
    private String logoURLHeight;

    @Column(name = "logo_url_online_resource_url", length = 1000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String logoURLOnlineResourceURL;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOnlineResourceURL() {
        return onlineResourceURL;
    }

    public void setOnlineResourceURL(String onlineResourceURL) {
        this.onlineResourceURL = onlineResourceURL;
    }

    public String getLogoURLFormat() {
        return logoURLFormat;
    }

    public void setLogoURLFormat(String logoURLFormat) {
        this.logoURLFormat = logoURLFormat;
    }

    public String getLogoURLWidth() {
        return logoURLWidth;
    }

    public void setLogoURLWidth(String logoURLWidth) {
        this.logoURLWidth = logoURLWidth;
    }

    public String getLogoURLHeight() {
        return logoURLHeight;
    }

    public void setLogoURLHeight(String logoURLHeight) {
        this.logoURLHeight = logoURLHeight;
    }

    public String getLogoURLOnlineResourceURL() {
        return logoURLOnlineResourceURL;
    }

    public void setLogoURLOnlineResourceURL(String logoURLOnlineResourceURL) {
        this.logoURLOnlineResourceURL = logoURLOnlineResourceURL;
    }

    
}
