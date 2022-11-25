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
package petascope.wcps.subset_axis.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import petascope.util.CrsUtil;
import petascope.wcps.result.ParameterResult;

/**
 * Class to translate trimming or slicing operations to rasql  <code>
 * Lat:"http://.../4326"(4.56:2.32)
 * </code> translates to  <code>
 * 4.56:2.32
 * with axisName is: Lat
 * CRS is: 4326
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// As Jackson needs to know the concrete subclass when deserializing string to object
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public abstract class WcpsSubsetDimension extends ParameterResult {
    
    public WcpsSubsetDimension() {
        
    }
    
    /**
    * Constructor for the trimming of class
    *
     * @param axisName the name of the axis on which the trim operation is made
     * @param crs the crs of the subset
     */
    public WcpsSubsetDimension(String axisName, String crs) {
        this.axisName = axisName;
        if (crs != null) {
            this.crs = CrsUtil.replaceOldURLWithNewURL(crs);
        }
    }

    /**
     * Returns the axis on which the trim interval is being done
     *
     * @return
     */
    public String getAxisName() {
        return axisName;
    }

    public void setAxisName(String axisName) {
        this.axisName = axisName;
    }

    /**
     * Returns the crs of the subset interval
     *
     * @return
     */
    public String getCrs() {
        return crs;
    }

    public void setCrs(String crsUri) {
        this.crs = CrsUtil.replaceOldURLWithNewURL(crsUri);
    }

    public boolean isTemporal() {
        return temporal;
    }

    public void setTemporal(boolean temporal) {
        this.temporal = temporal;
    }

    public abstract String getStringBounds();
    
    public abstract String toString();
    
    public abstract String toStringWithoutCRS();
    
    private String axisName;
    private String crs;
    private boolean temporal = false;
    public static final String AXIS_ITERATOR_DOLLAR_SIGN = "$";
}