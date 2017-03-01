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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.handler;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import petascope.util.CrsUtil;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;

/**
 * Translator class for CrsSet of the coverage (e.g list all coverage's axis CRS
 * from crsSet($c))
 *
 * for c in (mr), d in (rgb) return crsSet(c)
 * return:
 * i:http://localhost:8080/def/crs/OGC/0/Index2D CRS:1,
 * j:http://localhost:8080/def/crs/OGC/0/Index2D CRS:1
 *
 * for c in (mean_summer_airtemp) return crsSet(c)
 * return:
 *
 * Long:http://localhost:8080/def/crs/EPSG/0/4326 CRS:1,
 * Lat:http://localhost:8080/def/crs/EPSG/0/4326 CRS:1
 *
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class CoverageCrsSetHandler {

    public static WcpsMetadataResult handle(WcpsResult coverageExpression) {
        String result = "";
        List<String> list = new ArrayList<String>();
        String tmp = "";
        for (Axis axis : coverageExpression.getMetadata().getAxes()) {
            // nativeCrs (e.g: mr: Index2D, mean_summer_airtemp: EPSG:4326)
            tmp = axis.getLabel() + ":" + axis.getCrsUri();
            // gridCrs (CRS:1)
            tmp = tmp + " " + CrsUtil.GRID_CRS;

            list.add(tmp);
        }
        result = StringUtils.join(list, ",");

        return new WcpsMetadataResult(coverageExpression.getMetadata(), result.trim());
    }
}