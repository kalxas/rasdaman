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
package petascope.wcps.subset_axis.model;

import java.util.ArrayList;
import java.util.List;
import petascope.util.ListUtil;
import petascope.wcps.result.ParameterResult;

/**
 * Class to represent WKT coordinates (e.g: 20 30, 40 50, 60 70)
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WKTCompoundPoints extends ParameterResult {

    private List<WKTCompoundPoint> wktCompoundPoints;

    public WKTCompoundPoints(List<WKTCompoundPoint> wktPoints) {
        this.wktCompoundPoints = wktPoints;
    }

    public List<WKTCompoundPoint> getWKTCompoundPoints() {
        return wktCompoundPoints;
    }

    public void setWKTCompoundPoints(List<WKTCompoundPoint> wktCompoundPoints) {
        this.wktCompoundPoints = wktCompoundPoints;
    }

    public int getNumberOfDimensions() {
        return this.wktCompoundPoints.get(0).getNumberOfDimensions();
    }

    public String getStringRepresentation() {
        List<String> points = new ArrayList<>();
        for (int i = 0; i < this.wktCompoundPoints.size(); i++) {
            String point = "(" + this.wktCompoundPoints.get(i).getStringRepresentation() + ")";
            // e.g: (20 30,30 40)
            points.add(point);
        }

        // e.g: (20 30,30 40), (40 50,60 70)
        return ListUtil.join(points, ",");
    }
}
