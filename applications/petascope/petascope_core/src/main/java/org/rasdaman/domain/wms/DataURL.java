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
 * A server may use DataURL to offer a link to the underlying data represented
 * by a particular layer. The enclosed Format element indicates the file format
 * MIME type of the data file.
 *
 * e.g: <DataURL type="FGDC:1998">
 * <Format>text/plain</Format>
 * <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
 * xlink:type="simple"
 * xlink:href="http://www.university.edu/metadata/roads.txt" />
 * </DataURL>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = DataURL.TABLE_NAME)
public class DataURL implements Serializable {

    public static final String TABLE_NAME = TABLE_PREFIX + "_data_url";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    public DataURL() {

    }

    @Column(name = "format")
    private String format;

    @Column(name = "online_resource_url", length = 1000)
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String onlineResourceURL;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getOnlineResourceURL() {
        return onlineResourceURL;
    }

    public void setOnlineResourceURL(String onlineResourceURL) {
        this.onlineResourceURL = onlineResourceURL;
    }

}
