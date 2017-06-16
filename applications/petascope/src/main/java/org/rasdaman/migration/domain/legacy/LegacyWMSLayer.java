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
package org.rasdaman.migration.domain.legacy;

import java.util.List;

/**
 * Legacy WMS 1.3 layers
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class LegacyWMSLayer {

    // Primary key
    private String name;
    private String title;
    private String layerAbstract;
    // Each layer has 1 of this EXGeographicBoundingBox
    private LegacyWMSEXGeographicBoundingBox exBBox;
    // And multiple styles
    private List<LegacyWMSStyle> styles;
    private final int queryable = 0;
    private final int cascaded = 0;
    private final int opaque = 1;
    private final int noSubsets = 0;
    private final int fixedWidth = 0;
    private final int fixedHieght = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLayerAbstract() {
        return layerAbstract;
    }

    public void setLayerAbstract(String layerAbstract) {
        this.layerAbstract = layerAbstract;
    }

    public LegacyWMSEXGeographicBoundingBox getExBBox() {
        return exBBox;
    }

    public void setExBBox(LegacyWMSEXGeographicBoundingBox exBBox) {
        this.exBBox = exBBox;
    }

    public List<LegacyWMSStyle> getStyles() {
        return styles;
    }

    public void setStyles(List<LegacyWMSStyle> styles) {
        this.styles = styles;
    }

    public int getQueryable() {
        return queryable;
    }

    public int getCascaded() {
        return cascaded;
    }

    public int getOpaque() {
        return opaque;
    }

    public int getNoSubsets() {
        return noSubsets;
    }

    public int getFixedWidth() {
        return fixedWidth;
    }

    public int getFixedHieght() {
        return fixedHieght;
    }
}
