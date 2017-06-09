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
import static org.rasdaman.domain.wms.Layer.TABLE_PREFIX;

/**
 * WMS 1.3 7.2.4.7 Layer attributes
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = LayerAttribute.TABLE_NAME)
public class LayerAttribute {

    public static final String TABLE_NAME = TABLE_PREFIX + "_layer_attribute";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    public static final String QUERYABLE = "queryable";
    public static final String CASCADED = "cascaded";
    public static final String OPAQUE = "opaque";
    public static final String NO_SUBSETS = "noSubsets";
    public static final String FIXED_WIDTH = "fixedWidth";
    public static final String FIXED_HEIGHT = "fixedHeight";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    public LayerAttribute() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // Whether the server supports the GetFeatureInfo operation on that Layer (not supported in Rasdaman)
    // false (0): not queryable, true (1): queryable
    @Column
    private int queryable = 0;

    // A Layer is said to have been “cascaded” if it was obtained from an originating server and then included in the
    // service metadata of a different server.
    // If a WMS cascades the content of another WMS, then it shall increment by 1 the value of the cascaded attribute
    // for the affected layers. If that attribute is missing from the originating server’s service metadata, then the
    // Cascading WMS shall insert the attribute and set it to 1. 
    @Column
    private int cascaded = 0;

    // 7.2.4.7.4 Opaque vs. transparent layers
    // the server shall send an image with a transparent background if and only if the client requests
    // TRANSPARENT=TRUE and a picture FORMAT that supports transparency.
    // Default set to true (1) as it is not transparent without the transparent=true
    @Column
    private int opaque = 1;

    // false (0): WMS can map a subset of full bounding box, true (1): cannot do subset
    @Column
    private int noSubsets = 0;

    // 0: WMS can produce map of arbitrary width nonzero, nonzero: value is fixed for the width.
    @Column
    private int fixedWidth = 0;

    // 0: WMS can produce map of arbitrary height nonzero, nonzero: value is fixed for the height.
    @Column
    private int fixedHeight = 0;

    public int getQueryable() {
        return queryable;
    }

    public void setQueryable(int queryable) {
        this.queryable = queryable;
    }

    public int getCascaded() {
        return cascaded;
    }

    public void setCascaded(int cascaded) {
        this.cascaded = cascaded;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public int getNoSubsets() {
        return noSubsets;
    }

    public void setNoSubsets(int noSubsets) {
        this.noSubsets = noSubsets;
    }

    public int getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(int fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    public int getFixedHeight() {
        return fixedHeight;
    }

    public void setFixedHeight(int fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

}
