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

/**
 * Legacy class for WMS 1.3 EX_GeographicBoundingBox
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class LegacyWMSEXGeographicBoundingBox {

    // Primary key
    private int id;
    private String westBoundLongitude;
    private String eastBoundLongitude;
    private String southBoundLatitude;
    private String northBoundLatitude;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWestBoundLongitude() {
        return westBoundLongitude;
    }

    public void setWestBoundLongitude(String westBoundLongitude) {
        this.westBoundLongitude = westBoundLongitude;
    }

    public String getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    public void setEastBoundLongitude(String eastBoundLongitude) {
        this.eastBoundLongitude = eastBoundLongitude;
    }

    public String getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    public void setSouthBoundLatitude(String southBoundLatitude) {
        this.southBoundLatitude = southBoundLatitude;
    }

    public String getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    public void setNorthBoundLatitude(String northBoundLatitude) {
        this.northBoundLatitude = northBoundLatitude;
    }

}
