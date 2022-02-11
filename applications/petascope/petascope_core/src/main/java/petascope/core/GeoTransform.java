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
package petascope.core;

import java.math.BigDecimal;
import org.gdal.gdal.Dataset;

/**
 * A class that contains the necessary values for a 2D geo-referenced coverage in a source CRS which
 * can be used to estimate the geo, grid domains and XY axes' resolutions in a target CRS.
 * 
 * e.g: test_mean_summer_airtemp 
 * (geo domains: Lat(-8.975, -44.525), Long(111.975, 156.275), grid domains: (0:885, 0:710), Lat_resolution: -0.05, Long_resolution: 0.05, CRS: EPSG:4326)
 * 
 * Then, the GeoTransform object will contain (NOTE: gdal doesn't care about the CRS axes order, e.g: EPSG:4326 is always long, lat not lat, long):
 *  - EPSG code: 4326
 *  - upperLeftGeoX: 111.975
 *  - upperLeftGeoY: -8.975
 *  - width: 886
 *  - height: 711
 *  - geoXResolution: 0.05
 *  - geoYResolution: -0.05
 * 
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class GeoTransform {
    
    private int epsgCode;
    private BigDecimal upperLeftGeoX;
    private BigDecimal upperLeftGeoY;
    private int gridWidth;
    private int gridHeight;
    private BigDecimal geoXResolution;
    private BigDecimal geoYResolution;

    public GeoTransform() {
        
    }
    
    public GeoTransform(int epsgCode, BigDecimal upperLeftGeoX, BigDecimal upperLeftGeoY, int gridWidth, int gridHeight, BigDecimal geoXResolution, BigDecimal geoYResolution) {
        this.epsgCode = epsgCode;
        this.upperLeftGeoX = upperLeftGeoX;
        this.upperLeftGeoY = upperLeftGeoY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.geoXResolution = geoXResolution;
        this.geoYResolution = geoYResolution;
    }
    
    /**
     * Create a GeoTransform object from a Gdal dataset
     */
    public GeoTransform(Dataset dataset) {
        // upperLeftGeoX, resX, 0, upperLeftGeoY, 0, resY
        double[] gdalValues = dataset.GetGeoTransform();
        int width = dataset.GetRasterXSize();
        int height = dataset.GetRasterYSize();
        this.gridWidth = width;
        this.gridHeight = height;
        
        this.upperLeftGeoX = new BigDecimal(String.valueOf(gdalValues[0]));
        this.upperLeftGeoY = new BigDecimal(String.valueOf(gdalValues[3]));
        
        this.geoXResolution = new BigDecimal(String.valueOf(gdalValues[1]));
        this.geoYResolution = new BigDecimal(String.valueOf(gdalValues[5]));
    }        

    public int getEPSGCode() {
        return epsgCode;
    }

    public void setEPSGCode(int epsgCode) {
        this.epsgCode = epsgCode;
    }

    public BigDecimal getUpperLeftGeoX() {
        return upperLeftGeoX;
    }
    
    public BigDecimal getUpperLeftGeoXDecimal() {
        return new BigDecimal(String.valueOf(this.upperLeftGeoX));
    }
    
    public BigDecimal getLowerRightGeoYDecimal() {
        return new BigDecimal(String.valueOf(this.getLowerRightGeoY()));
    }

    public void setUpperLeftGeoX(BigDecimal upperLeftGeoX) {
        this.upperLeftGeoX = upperLeftGeoX;
    }
    public void setUpperLeftGeoX(double upperLeftGeoX) {
        this.upperLeftGeoX = new BigDecimal(String.valueOf(upperLeftGeoX));
    }

    public BigDecimal getUpperLeftGeoY() {
        return upperLeftGeoY;
    }

    public void setUpperLeftGeoY(BigDecimal upperLeftGeoY) {
        this.upperLeftGeoY = upperLeftGeoY;
    }
    public void setUpperLeftGeoY(double upperLeftGeoY) {
        this.upperLeftGeoY = new BigDecimal(String.valueOf(upperLeftGeoY));
    }
    

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    public BigDecimal getGeoXResolution() {
        return geoXResolution;
    }
    
    public void setGeoXResolution(BigDecimal geoXResolution) {
        this.geoXResolution = geoXResolution;
    }
    public void setGeoXResolution(double geoXResolution) {
        this.geoXResolution = new BigDecimal(String.valueOf(geoXResolution));
    }

    public BigDecimal getGeoYResolution() {
        return geoYResolution;
    }
    
    public BigDecimal getGeoYResolutionDecimal() {
        return new BigDecimal(String.valueOf(this.geoYResolution));
    }

    public void setGeoYResolution(BigDecimal geoYResolution) {
        this.geoYResolution = geoYResolution;
    }
    public void setGeoYResolution(double geoYResolution) {
        this.geoYResolution = new BigDecimal(String.valueOf(geoYResolution));
    }    
    
    /**
     * Return the GDAL format for geo transform:
     * UpplerLeftX, resX, 0, UpperLeftY, 0, resY.
     * e.g: 699960.0, 60.0, 0.0, 5900040.0, 0.0, -60.0
     */
    public String toGdalString() {
        return upperLeftGeoX + ", " + geoXResolution + ", 0, " + upperLeftGeoY + ", 0, " + geoYResolution;
    }
    
    public BigDecimal getLowerRightGeoX() {
        // xmax = xmin + width * geoXResolution
        BigDecimal result = this.upperLeftGeoX.add(new BigDecimal(gridWidth).multiply(geoXResolution));
        return result;
    }
    
    public BigDecimal getLowerRightGeoY() {
        // ymin = ymax + height * geoYResolution (geoY is negative)
        BigDecimal result = this.upperLeftGeoY.add(new BigDecimal(gridHeight).multiply(geoYResolution));
        return result;
    }
    
    @Override
    public String toString() {
        String output = "epsgCode:" + epsgCode + ", xMin:" + upperLeftGeoX  + ", yMin: " + this.getLowerRightGeoY()
                + ", xMax:" + this.getLowerRightGeoX() + ", yMax: " + upperLeftGeoY + ", width:" + gridWidth
                + ", height:" + gridHeight + ", geoXResolution:" + geoXResolution + ", geoYResolution:" + geoYResolution;
        return output;
    }
    
    public BoundingBox toBBox() {
        BigDecimal xMin = new BigDecimal(String.valueOf(this.upperLeftGeoX));
        BigDecimal yMin = this.getLowerRightGeoY();
        
        BigDecimal xMax = this.getLowerRightGeoX();
        BigDecimal yMax = new BigDecimal(String.valueOf(this.upperLeftGeoY));
        
        BoundingBox bbox = new BoundingBox(xMin, yMin, xMax, yMax);
        return bbox;
    }
}
