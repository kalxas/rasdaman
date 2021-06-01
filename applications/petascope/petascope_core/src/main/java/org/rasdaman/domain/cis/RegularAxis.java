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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.cis;

import java.io.Serializable;
import javax.persistence.*;

/**
 * CIS 1.1
 * 
 * Axis type CIS::RegularAxis has no restriction on the CRS used; as it is regularly spaced
it contains the common distance, i.e.: resolution, as a part of the axis definition.
* 
* Note: The type is string to accommodate any potential resolution specification, such as “100” for
degrees or meters, “2015-07-30T23Z” for a 1-hour duration in Gregorian calendar, and potential future
calendar types.
* 
* In a coverage using the grid-regular scheme, the resolution value in a
CIS::RegularAxis shall be a nonzero, *positive* value expressed in the units of measure of
this axis as defined in the CRS identified in the srsName item of the envelope.
* 
* The positive integer number of direct position coordinates along regular axis:
* 
* gridPoints = Math.floor( (gridUpperbound - gridLowerBound + 1) / resolution)
* 
* 
 */
@Entity
@Table(name = RegularAxis.TABLE_NAME)
@PrimaryKeyJoinColumn(name = RegularAxis.COLUMN_ID, referencedColumnName = GeoAxis.COLUMN_ID)
public class RegularAxis extends GeoAxis implements Serializable {
    
    public static final String TABLE_NAME = "regular_axis";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    public RegularAxis() {
        
    }

    public RegularAxis(String axisLabel, String uomLabel, String srsName, String lowerBound, String upperBound, String resolution, String axisType) {
        super(axisLabel, uomLabel, srsName, lowerBound, upperBound, resolution, axisType);
    }
    
}
