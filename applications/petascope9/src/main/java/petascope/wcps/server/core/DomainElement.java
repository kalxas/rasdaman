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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.server.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.CrsDefinition;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.util.WCPSConstants;
import petascope.util.TimeUtil;

/**
 * This is an axis in geographic coordinates. See the WCPS standard.
 */
public class DomainElement implements Cloneable {
    
    private static Logger log = LoggerFactory.getLogger(DomainElement.class);

    private String crs;
    private String name;
    private String maxValue;
    private String minValue;
    private String type;
    private String uom;
    private int iOrder;
    private CrsDefinition.Axis axisDef;
    private String resolution;
    private int DIM;
    private boolean isIrregular;

    // Overload
    public DomainElement(String min, String max, CrsDefinition.Axis axis, String crsUri, int order, int dim, boolean isIrr) 
            throws WCPSException {
        this(min, max, axis.getAbbreviation(), axis.getType(), crsUri, order, dim, isIrr);
        name = axis.getAbbreviation();
        uom  = axis.getUoM();
        
        axisDef = axis;
    }

    public DomainElement(String min, String max, String axisName, String axisType, String crsUri, int order, int dim, boolean isIrr) 
            throws WCPSException {

        if ((axisName == null) || (axisType == null)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    WCPSConstants.ERRTXT_INVALID_DOMAIN_ELEMENT_NULL);
        }

        name = axisName;
        DIM = dim;
        isIrregular = isIrr;
        iOrder = order;
        type = axisType;
        if (!type.equals(AxisTypes.X_AXIS) 
                && !type.equals(AxisTypes.Y_AXIS) 
                && !type.equals(AxisTypes.T_AXIS) 
                && !type.equals(AxisTypes.ELEV_AXIS)
                && !type.equals(AxisTypes.OTHER)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    WCPSConstants.ERRTXT_INVALID_DOMAIN_ELEMENT_EMPTY + type);
        }

        if ((min != null) && (max != null)) {
            minValue = min;
            maxValue = max;
        } else {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    WCPSConstants.ERRTXT_INVALID_DOMAIN_ELEMENT_EMPTY);
        }

        if ((crsUri == null) || crsUri.equals(CrsUtil.GRID_CRS)) {
               crs = CrsUtil.GRID_CRS;
        } else crs = crsUri;
        
        // Compute resolution (with irregular axes a resolution cannot be defined)
        if (!isIrregular) {
            // Consistency checks
            if (Double.parseDouble(maxValue) < Double.parseDouble(minValue)) {
                throw new WCPSException(ExceptionCode.InvalidMetadata,
                        WCPSConstants.ERRTXT_INVALID_DOM_BOUNDS);
            }
            
            // Use BigDecimals to avoid finite arithemtic rounding issues of Doubles
            BigDecimal maxBD = BigDecimal.valueOf(Double.parseDouble(maxValue));
            BigDecimal minBD = BigDecimal.valueOf(Double.parseDouble(minValue));
            BigDecimal dimBD = BigDecimal.valueOf(DIM);
            BigDecimal diffBD = maxBD.subtract(minBD);
            BigDecimal resBD = diffBD.divide(dimBD, RoundingMode.UP);
            
            resolution = resBD.toString();
        }
        
        log.trace(toString());
    }

    //@Override
    public DomainElement clone() {
        
        try {
            String newMin = minValue == null ? null : minValue.toString();
            String newMax = maxValue == null ? null : maxValue.toString();
            String newCrs = crs      == null ? null : crs.toString();
            int order     = new Integer(iOrder);
            boolean isIrr = isIrregular ? true : false;
            return new DomainElement(newMin, newMax, axisDef.clone(), newCrs, order, DIM, isIrr);
        } catch (Exception ime) {
            throw new RuntimeException(
                    WCPSConstants.ERRTXT_INVALID_METADAT_WHILE_CLONE,
                    ime);
        }

    }

    public boolean equals(DomainElement de) {
        return minValue.equals(de.minValue) && maxValue.equals(maxValue)
                && name.equals(de.name) && type.equals(de.type);
    }
    
    public CrsDefinition.Axis getAxisDef() {
        return axisDef;
    }
    
    public String getName() {
        return name;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public String getMinValue() {
        return minValue;
    }
    
    public String getResolution() {
        return resolution;
    }

    public String getType() {
        return type;
    }

    public String getCrs() {
        return crs;
    }

    public String getUom() {
        return uom;
    }
    
    public int getOrder() {
       return iOrder;
    }
    
    public boolean isIrregular() {
        return isIrregular;
    }
    
    @Override
    public String toString() {
        String d = WCPSConstants.MSG_DOMAIN_CAMEL + "#" + iOrder + " {"  
                + "Name:" + name 
                + " | Type:" + type
                + " | UoM:" + uom
                + " | [" + minValue 
                + ","    + maxValue + "]"
                + " | CRS:" + crs + "'}";
        return d;
    }
}
