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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.result;

import petascope.util.MIMEUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.subset_axis.model.DimensionIntervalList;

/**
 * Class that encapsulates the result of the evaluation of a WCPS node.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WcpsResult extends VisitorResult {

    //the rasql string resulting from the evaluation
    private String rasql;
    
    private DimensionIntervalList dimensionIntervalList;
    
    // if result of encode(clip(c, linestring()) with coordinates, "csv/json") then needs to translate
    // grid to geo coordinates from result of rasql ("grid_x1 grid_y1 value1", "grid_x2 grid_y2 value2", ...)
    private boolean withCoordinates;
    
    public WcpsResult(WcpsCoverageMetadata metadata, String rasql) {
        this.rasql = rasql;
        this.metadata = metadata;
    }
    
    public WcpsResult(DimensionIntervalList dimensionIntervalList) {
        this.dimensionIntervalList = dimensionIntervalList;
    }

    // Used when create a new rasql query for multipart purpose
    public void setRasql(String rasql) {
        this.rasql = rasql;
    }

    public String getRasql() {
        return rasql;
    }

    public WcpsCoverageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(WcpsCoverageMetadata metadata) {
        this.metadata = metadata;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public DimensionIntervalList getDimensionIntervalList() {
        return dimensionIntervalList;
    }

    public void setDimensionIntervalList(DimensionIntervalList dimensionIntervalList) {
        this.dimensionIntervalList = dimensionIntervalList;
    }

    // rasql query can be encode in multiple types (e.g: tiff, png, csv,...)
    // then need to get the correct MIME for each type
    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
        // e.g: dem() is not MIME then will return null
        if (this.mimeType == null) {            
            this.mimeType = MIMEUtil.MIME_XML;
        }
    }

    public boolean withCoordinates() {
        return withCoordinates;
    }

    public void setWithCoordinates(boolean withCoordinates) {
        this.withCoordinates = withCoordinates;
    }
}