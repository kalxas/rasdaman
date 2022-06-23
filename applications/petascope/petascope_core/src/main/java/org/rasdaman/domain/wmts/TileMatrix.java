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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.wmts;

import java.math.BigDecimal;

/**
 * Model class contains some basic properties for a WMTS TileMatrix
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class TileMatrix {
    
    // fixed number of pixels for width and height of a Tile
    public static final int GRID_SIZE = 256;
    
    private String name; 
    
    private BigDecimal scaleDenominator;
    private String topLeftCorner;
    private int tileWidth = GRID_SIZE;
    private int tileHeight = GRID_SIZE;
    // Width of the matrix (number of tiles in width)
    private long matrixWidth;
    // Height of the matrix (number of tiles in matrix)
    private long matrixHeight;
    
    private String epsgCode;
    
    private BigDecimal geoLowerBoundX;
    private BigDecimal geoUpperBoundX;
    private BigDecimal geoLowerBoundY;
    private BigDecimal geoUpperBoundY;
    
    private BigDecimal geoResolutionX;
    private BigDecimal geoResolutionY;
    
    // grid domains of the X axis
    private long gridWidth;
    // grid domains of the Y axis
    private long gridHeight;

    public TileMatrix(String name,
            BigDecimal scaleDenominator, String topLeftCorner, 
            long tileWidth, long tileHeight,
            long matrixWidth, long matrixHeight,
            String epsgCode,
            BigDecimal geoLowerBoundX, BigDecimal geoUpperBoundX,
            BigDecimal geoLowerBoundY, BigDecimal geoUpperBoundY,
            BigDecimal geoResolutionX, BigDecimal geoResolutionY,
            long gridWith, long gridHeight) {
        
        this.name = name;
        
        this.scaleDenominator = scaleDenominator;
        this.topLeftCorner = topLeftCorner;
        
        this.tileWidth = (int) tileWidth;
        this.tileHeight = (int) tileHeight;
        
        this.matrixWidth = matrixWidth;
        this.matrixHeight = matrixHeight;
        
        this.epsgCode = epsgCode;
        
        this.geoLowerBoundX = geoLowerBoundX;
        this.geoUpperBoundX = geoUpperBoundX;
        this.geoLowerBoundY = geoLowerBoundY;
        this.geoUpperBoundY = geoUpperBoundY;
        
        this.geoResolutionX = geoResolutionX;
        this.geoResolutionY = geoResolutionY;
        
        this.gridWidth = gridWith;
        this.gridHeight = gridHeight;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getScaleDenominator() {
        return scaleDenominator;
    }

    public String getTopLeftCorner() {
        return topLeftCorner;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public long getMatrixWidth() {
        return matrixWidth;
    }

    public long getMatrixHeight() {
        return matrixHeight;
    }

    public String getEpsgCode() {
        return epsgCode;
    }

    public BigDecimal getGeoLowerBoundX() {
        return geoLowerBoundX;
    }

    public BigDecimal getGeoUpperBoundX() {
        return geoUpperBoundX;
    }

    public BigDecimal getGeoLowerBoundY() {
        return geoLowerBoundY;
    }

    public BigDecimal getGeoUpperBoundY() {
        return geoUpperBoundY;
    }

    public BigDecimal getGeoResolutionX() {
        return geoResolutionX;
    }

    public BigDecimal getGeoResolutionY() {
        return geoResolutionY;
    }

    public long getGridWidth() {
        return gridWidth;
    }

    public long getGridHeight() {
        return gridHeight;
    }
    
    
    
}
