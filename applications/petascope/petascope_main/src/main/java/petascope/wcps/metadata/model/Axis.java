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
package petascope.wcps.metadata.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.core.AxisTypes;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.TimeUtil;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @param <T>
 */
// As Jackson needs to know the concrete subclass when deserializing string to object
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Axis<T> {

    private String label;
    private NumericSubset geoBounds;
    private NumericSubset gridBounds;
    
    // This is the persisted grid domain for axis in database
    private NumericSubset originalGridBounds;
    private NumericSubset originalGeoBounds;
    private BigDecimal origin;
    // this is the CRS of axis in coverage
    private String nativeCrsUri;
    private CrsDefinition crsDefinition;
    // e.g: x, y, t, ...
    private String axisType;
    private String axisUoM;
    private int rasdamanOrder;
    private BigDecimal resolution;
    private boolean slicing = false;
    
    // If this axis was created by subsetting by "CRS:1" -> true
    // NOTE: this is used only when considering if a pyramid member can be used for scaling
    private boolean transatedGridToGeoBounds = false;
    
    public Axis() {
        
    }
    
    public Axis(String label, NumericSubset geoBounds, NumericSubset originalGridBounds, NumericSubset gridBounds,
            String crsUri, CrsDefinition crsDefinition,
            String axisType, String axisUoM, int rasdamanOrder, BigDecimal origin, BigDecimal resolution) {        
        this(label, geoBounds, originalGridBounds, gridBounds, crsUri, crsDefinition, axisType, axisUoM, rasdamanOrder, origin, resolution, null);
    }

    public Axis(String label, NumericSubset geoBounds, NumericSubset originalGridBounds, NumericSubset gridBounds,
            String crsUri, CrsDefinition crsDefinition,
            String axisType, String axisUoM, int rasdamanOrder, BigDecimal origin, BigDecimal resolution, NumericSubset originalGeoBounds) {
        this.label = label;
        this.geoBounds = geoBounds;
        this.originalGeoBounds = originalGeoBounds;
        this.originalGridBounds = originalGridBounds;
        this.gridBounds = gridBounds;
        this.nativeCrsUri = crsUri;
        this.crsDefinition = crsDefinition;
        this.axisType = axisType;
        this.axisUoM = axisUoM;
        this.rasdamanOrder = rasdamanOrder;
        this.origin = BigDecimalUtil.stripDecimalZeros(origin);
        this.resolution = resolution;
    }

    public BigDecimal getResolution() {
        return BigDecimalUtil.stripDecimalZeros(resolution);
    }

    public void setResolution(BigDecimal resolution) {
        this.resolution = resolution;
    }

    public void setNativeCrsUri(String crsUri) {
        this.nativeCrsUri = crsUri;
    }

    public String getNativeCrsUri() {
        return nativeCrsUri;
    }

    public CrsDefinition getCrsDefinition() {
        return crsDefinition;
    }

    public NumericSubset getGeoBounds() {
        return geoBounds;
    }

    public void setGeoBounds(NumericSubset geoBounds) {
        this.geoBounds = geoBounds;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public NumericSubset getGridBounds() {
        return gridBounds;
    }

    public void setGridBounds(NumericSubset gridBounds) {
        this.gridBounds = gridBounds;
    }

    public NumericSubset getOriginalGridBounds() {
        return originalGridBounds;
    }

    public void setOriginalGridBounds(NumericSubset originalGridBounds) {
        this.originalGridBounds = originalGridBounds;
    }

    public NumericSubset getOriginalGeoBounds() {
        return originalGeoBounds;
    }

    public void setOriginalGeoBounds(NumericSubset originalGeoBounds) {
        this.originalGeoBounds = originalGeoBounds;
    }
    
    public String getAxisType() {
        return axisType;
    }

    public String getAxisUoM() {
        return axisUoM;
    }

    public int getRasdamanOrder() {
        return rasdamanOrder;
    }

    public void setOrigin(BigDecimal origin) {
        this.origin = BigDecimalUtil.stripDecimalZeros(origin);
    }

    /**
     * Return the original origin of the axis without trimming/slicing from coverage's metadata
     *
     * @return
     */
    public BigDecimal getOriginalOrigin() {
        return this.origin;
    }

    /**
     * Return the raw origin in number (e.g: 4.5000 -> 4.5, 3.20001 -> 3.20001)
     *
     * @return
     */
    public BigDecimal getOrigin() {
        // Re calculate origin when subsets were applied (but it is not original origin of coverage)
        BigDecimal axisOrigin = null;
        if (this instanceof IrregularAxis) {
            axisOrigin = geoBounds.getLowerLimit();
        } else {
            BigDecimal halfPixel = BigDecimal.valueOf(1.0 / 2).multiply(resolution).abs();
            if (resolution.compareTo(BigDecimal.ZERO) > 0) {
                // origin = (geoMinValue + 0.5 * resolution) when resolution > 0 (e.g: Longitude axis)
                axisOrigin = geoBounds.getLowerLimit().add(halfPixel).stripTrailingZeros();
            } else {
                // origin = (geoMaxValue - 0.5 * resolution) when resolution < 0 (e.g: Latitude axis)                
                axisOrigin = geoBounds.getUpperLimit().subtract(halfPixel).stripTrailingZeros();
            }
        }

        return BigDecimalUtil.stripDecimalZeros(axisOrigin);
    }

    /**
     * Return the translated origin for the axis in String with dateTime axis is
     * in dateTime format and non dateTime axis is raw value.
     *
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public String getOriginRepresentation() throws PetascopeException {
        if (CrsUtil.isTimeAxis(this.axisType)) {
            // Translate the origin in number to dateTime format            
            return TimeUtil.valueToISODateTime(BigDecimal.ZERO, this.getOrigin(), crsDefinition);
        } else {
            return this.getOrigin().toPlainString();
        }
    }
    
     /**
     * Return the translated original origin for the axis in String with dateTime axis is
     * in dateTime format and non dateTime axis is raw value.
     *
    */
    public String getOriginalOriginRepresentation() throws PetascopeException {
        if (CrsUtil.isTimeAxis(this.axisType)) {
            // Translate the origin in number to dateTime format            
            return TimeUtil.valueToISODateTime(BigDecimal.ZERO, this.getOriginalOrigin(), crsDefinition);
        } else {
            return this.getOriginalOrigin().toPlainString();
        }
    }

    /**
     * Return the translated lower geo bound for the axis in String with
     * dateTime axis is in dateTime format and non dateTime axis is raw value
     *
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public String getLowerGeoBoundRepresentation() throws PetascopeException {
        if (this.axisType.equals(AxisTypes.T_AXIS)) {
            // Translate the lower geo bound in number to dateTime format            
            return TimeUtil.valueToISODateTime(BigDecimal.ZERO, this.getGeoBounds().getLowerLimit(), crsDefinition);
        } else {
            return this.getGeoBounds().getLowerLimit().toPlainString();
        }
    }

    /**
     * Return the translated lower geo bound for the axis in String with
     * dateTime axis is in dateTime format and non dateTime axis is raw value
     *
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    public String getUpperGeoBoundRepresentation() throws PetascopeException {
        if (this.axisType.equals(AxisTypes.T_AXIS)) {
            // Translate the upper geo bound in number to dateTime format            
            return TimeUtil.valueToISODateTime(BigDecimal.ZERO, this.getGeoBounds().getUpperLimit(), crsDefinition);
        } else {
            return this.getGeoBounds().getUpperLimit().toPlainString();
        }
    }

    /**
     * Check if axis is X, Y geoferenced axis (e.g: Lat, Long, E, N,...) and it
     * is not gridCRS (CRS:1) or IndexNDCRS
     *
     * @return
     */
    public boolean isXYGeoreferencedAxis() {
        if (this.axisType.equals(AxisTypes.X_AXIS) || this.axisType.equals(AxisTypes.Y_AXIS)) {
            if (!(CrsUtil.isGridCrs(this.nativeCrsUri) || CrsUtil.isIndexCrs(this.nativeCrsUri))) {
                return true;
            }
        }

        return false;
    }

    public boolean isXAxis() {
        return this.axisType.equals(AxisTypes.X_AXIS);
    }

    public boolean isYAxis() {
        return this.axisType.equals(AxisTypes.Y_AXIS);
    }    
    
    public boolean isNonXYAxis() {
        return !(this.isXAxis() || this.isYAxis());
    }
    
    public boolean isTimeAxis() {
        return this.axisType.equals(AxisTypes.T_AXIS);
    }
    
    public boolean isElevationAxis() {
        return this.axisType.equals(AxisTypes.HEIGHT_AXIS) 
            || this.axisType.equals(AxisTypes.DEPTH_AXIS);
    }
    
    /**
     * Return the total pixels by: grid_max - grid_min + 1
     * e.g: [0:20] -> 21 pixels
     */
    public int getTotalNumberOfGridPixels() {
        int result = this.gridBounds.getUpperLimit().subtract(this.gridBounds.getLowerLimit()).add(BigDecimal.ONE).intValue();
        return result;
    }
    
    /**
     * Return an axis's label by input index starts from 0.
     */
    public static String createAxisLabelByIndex(int index) {
        // NOTE: axis labels come from IndexND CRS.
        String axisName = Character.toString((char)('i' + index));
        return axisName;
    }
    
    public void setSlicing() {
        this.slicing = true;
    }
    
    public boolean isSlicing() {
        return slicing == true;
    }

    public boolean isTransatedGridToGeoBounds() {
        return transatedGridToGeoBounds;
    }

    public void setTransatedGridToGeoBounds(boolean transatedGridToGeoBounds) {
        this.transatedGridToGeoBounds = transatedGridToGeoBounds;
    }
    
    public String toString() {
        String result = "Geo bounds: " + this.getGeoBounds().getStringRepresentation() + " - Grid bounds: " + this.getGridBounds().getStringRepresentation();
        return result;
    }
}
