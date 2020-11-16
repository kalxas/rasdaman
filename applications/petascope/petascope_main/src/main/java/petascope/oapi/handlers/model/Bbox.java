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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.oapi.handlers.model;

import java.math.BigDecimal;
import org.rasdaman.domain.cis.Wgs84BoundingBox;

/**
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Bbox {
    BigDecimal minLon;
    BigDecimal minLat;
    BigDecimal maxLon;
    BigDecimal maxLat;

    public Bbox(BigDecimal minLon, BigDecimal minLat, BigDecimal maxLon, BigDecimal maxLat) {
        this.minLon = minLon;
        this.minLat = minLat;
        this.maxLon = maxLon;
        this.maxLat = maxLat;
    }

    public static Bbox fromString(String bbox) {
        String[] corners = bbox.split(",");
        if (corners.length != 4) {
            throw new RuntimeException("Bbox must have 4 corners.");
        }
        return new Bbox(new BigDecimal(corners[0]), new BigDecimal(corners[1]), new BigDecimal(corners[2]), new BigDecimal(corners[3]));
    }

    public static Bbox fromWgs84BoundingBox(Wgs84BoundingBox wgs84BBox) {
        return new Bbox(wgs84BBox.getMinLong(), wgs84BBox.getMinLat(), wgs84BBox.getMaxLong(), wgs84BBox.getMaxLat());
    }

    public boolean intersects(Bbox other) {
        boolean longOverlap = valueInRange(minLon, other.minLon, other.maxLon) ||
                valueInRange(other.minLon, minLon, maxLon);
        boolean latOverlap = valueInRange(minLat, other.minLat, other.maxLat) ||
                valueInRange(other.minLat, minLat, maxLat);
        return longOverlap && latOverlap;
    }

    private boolean valueInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }
}
