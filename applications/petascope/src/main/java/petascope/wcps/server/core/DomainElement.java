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
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.CrsDefinition;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.WcpsConstants;

/**
 * This is an axis in geographic coordinates. See the WCPS standard.
 */
public class DomainElement implements Cloneable {
    
    private static Logger log = LoggerFactory.getLogger(DomainElement.class);

    private String crs;
    private String label;
    private BigDecimal maxValue;
    private BigDecimal minValue;
    private String type;
    private String uom;
    private int iOrder;
    private BigDecimal offsetVector;
    private BigInteger dimensionality; // # of grid points along this axis
    private boolean    isIrregular;
    private List<BigDecimal> coefficients;
    private CrsDefinition.Axis axisDef;

    // constructor
    public DomainElement(
            BigDecimal min, 
            BigDecimal max, 
            String axisLabel,
            String axisType,
            String axisUom,
            String crsUri,
            int order, 
            BigInteger dim, 
            boolean isIrregular) 
            throws WCPSException {

        if (axisLabel == null) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    WcpsConstants.ERRTXT_INVALID_DOMAIN_ELEMENT_NULL);
        }

        // store to fields
        label = axisLabel;
        uom = axisUom;
        type = axisType;
        dimensionality = dim;
        this.isIrregular = isIrregular;
        coefficients = new ArrayList<BigDecimal>();
        iOrder = order;

        if ((min != null) && (max != null)) {
            minValue = min;
            maxValue = max;
        } else {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    WcpsConstants.ERRTXT_INVALID_DOMAIN_ELEMENT_EMPTY);
        }

        if ((crsUri == null) || crsUri.equals(CrsUtil.GRID_CRS)) {
               crs = CrsUtil.GRID_CRS;
        } else {
            crs = crsUri;
        }
        
        // Compute resolution (with irregular axes a resolution cannot be defined)
        if (!isIrregular) {
            // Consistency checks
            if (maxValue.compareTo(minValue) < 0) {
                throw new WCPSException(ExceptionCode.InvalidMetadata,
                        WcpsConstants.ERRTXT_INVALID_DOM_BOUNDS);
            }
            
            BigDecimal diffBD = maxValue.subtract(minValue);
            offsetVector      = diffBD.divide(new BigDecimal(dimensionality), RoundingMode.UP);
        }
        
        log.trace(toString());
    }

    @Override
    public DomainElement clone() {
        
        try {
            BigDecimal cloneMin = minValue == null ? null : minValue;
            BigDecimal cloneMax = maxValue == null ? null : maxValue;
            String cloneCrs     = crs      == null ? null : crs.toString();
            String cloneUom     = uom      == null ? null : uom.toString();
            String cloneLabel   = label    == null ? null : label.toString();
            String cloneType    = type     == null ? null : type.toString();
            int order         = new Integer(iOrder);
            boolean isIrr     = isIrregular ? true : false;
            DomainElement cloned = new DomainElement(
                    cloneMin, 
                    cloneMax, 
                    cloneLabel, 
                    cloneType, 
                    cloneUom, 
                    cloneCrs,
                    order, 
                    dimensionality, 
                    isIrr
                    );
            cloned.setCoefficients(this.coefficients);
            return cloned;
        } catch (Exception ime) {
            throw new RuntimeException(
                    WcpsConstants.ERRTXT_INVALID_METADAT_WHILE_CLONE,
                    ime);
        }
    }

    public boolean equals(DomainElement de) {
        return minValue.equals(de.minValue) && maxValue.equals(maxValue)
                && label.equals(de.label) && type.equals(de.type);
    }
   
    // Interface: getters/setters
    public String getLabel() {
        return label;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }
    
    public BigDecimal getOffsetVector() {
        return offsetVector;
    }
    
    public void setOffsetVector(BigDecimal res) {
        offsetVector = res;
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
    
    public CrsDefinition.Axis getAxisDef() {
        return axisDef;
    }
    
    public void setAxisDef(CrsDefinition.Axis axisDef) {
        this.axisDef = axisDef;
    }
    
    public boolean isIrregular() {
        return isIrregular;
    }
    
    public void setCoefficients(List<BigDecimal> coeffs) {
        this.coefficients = new ArrayList<BigDecimal>(coeffs);
    }
    
    public List<BigDecimal> getCoefficients() {
        return new ArrayList<BigDecimal>(this.coefficients);
    }
    
    @Override
    public String toString() {
        String d = WcpsConstants.MSG_DOMAIN_CAMEL + "#" + iOrder + " {"  
                + "Name:" + label 
                + " | Type:" + type
                + " | UoM:" + uom
                + " | [" + minValue 
                + ","    + maxValue + "]"
                + " | CRS:" + crs + "'}";
        return d;
    }
}
