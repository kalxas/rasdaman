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
package petascope.wcps.server.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.CrsDefinition;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import static petascope.util.CrsUtil.GRID_UOM;

/**
 * This is an axis in geographic coordinates. See the WCPS standard.
 */
public class DomainElement implements Cloneable {

    private static Logger log = LoggerFactory.getLogger(DomainElement.class);

    private String nativeCrs;
    private List<String> crsSet; // keep order for oracles
    private String label;
    private BigDecimal maxValue;
    private BigDecimal minValue;
    private String type;
    private String uom;
    private int iOrder;
    private BigDecimal scalarResolution; // positive absolute resolution value
    private boolean    positiveForwards; // grid axis direction = CRS axis direction (or viceversa)
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
            boolean positiveForwards,
            boolean isIrregular)
            throws WCPSException {

        if (axisLabel == null) {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    "Invalid domain element: Element name and type cannot be null.");
        }

        // store to fields
        crsSet = new ArrayList<String>(2); // capacity is 2: internal+external CRS (CRS extension is not enabled)
        label = axisLabel;
        uom = axisUom;
        type = axisType;
        dimensionality = dim;
        this.isIrregular = isIrregular;
        this.positiveForwards = positiveForwards;
        coefficients = new ArrayList<BigDecimal>();
        iOrder = order;

        if ((min != null) && (max != null)) {
            minValue = min;
            maxValue = max;
        } else {
            throw new WCPSException(ExceptionCode.InvalidMetadata,
                    "Invalid domain element: Element name cannot be empty.");
        }

        // native CRS and crsSet := Native + GridCRS (no CRS extension enables)
        if ((crsUri == null) || crsUri.equals(CrsUtil.GRID_CRS)) {
               nativeCrs = CrsUtil.GRID_CRS;
               crsSet.add(nativeCrs);
        } else {
            nativeCrs = crsUri;
            crsSet.addAll(Arrays.asList(nativeCrs, CrsUtil.GRID_CRS));
        }

        // Compute resolution (with irregular axes a resolution cannot be defined)
        if (!isIrregular) {
            // Consistency checks
            if (maxValue.compareTo(minValue) < 0) {
                throw new WCPSException(ExceptionCode.InvalidMetadata,
                        "Invalid domain element: upper-bound is greater then lower-bound.");
            }

            BigDecimal diffBD = maxValue.subtract(minValue).add(uom.equals(CrsUtil.GRID_UOM) ? BigDecimal.ONE : BigDecimal.ZERO);
            scalarResolution  = BigDecimalUtil.divide(diffBD, new BigDecimal(dimensionality));
        }

        log.trace(toString());
    }

    @Override
    public DomainElement clone() {

        try {
            BigDecimal cloneMin = minValue  == null ? null : minValue;
            BigDecimal cloneMax = maxValue  == null ? null : maxValue;
            String cloneCrs     = nativeCrs == null ? null : nativeCrs.toString();
            String cloneUom     = uom       == null ? null : uom.toString();
            String cloneLabel   = label     == null ? null : label.toString();
            String cloneType    = type      == null ? null : type.toString();
            int order         = new Integer(iOrder);
            boolean posForwards = positiveForwards ? true : false;
            boolean isIrr       =      isIrregular ? true : false;
            DomainElement cloned = new DomainElement(
                    cloneMin,
                    cloneMax,
                    cloneLabel,
                    cloneType,
                    cloneUom,
                    cloneCrs,
                    order,
                    dimensionality,
                    posForwards,
                    isIrr
                    );
            cloned.setCoefficients(this.coefficients);
            return cloned;
        } catch (Exception ime) {
            throw new RuntimeException("Invalid metadata while cloning DomainElement. This is a software bug in WCPS.", ime);
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

    /**
     * Cell width for this domain element.
     * @return Positive scalar value: (M-m)/W
     */
    public BigDecimal getScalarResolution() {
        return scalarResolution;
    }

    public void setScalarResolution(BigDecimal res) {
        scalarResolution = res;
    }

    /**
     * Scalar resolution, with negative sign in case the direction of
     * the grid axis associated with this domain element points to the opposite
     * direction of the related CRS axis.
     * @return [-]getScalarResolution()
     */
    public BigDecimal getDirectionalResolution() {
        return positiveForwards ? scalarResolution : BigDecimal.valueOf(-1).multiply(scalarResolution);
    }

    public String getType() {
        return type;
    }

    public String getNativeCrs() {
        return nativeCrs;
    }

    public List<String> getCrsSet() {
        return crsSet;
    }

    public String getUom() {
        return uom;
    }

    public int getOrder() {
       return iOrder;
    }

    public CrsDefinition getCrsDef() {
        return axisDef.getCrsDefinition();
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

    /**
     * Determine whether the points along this domain element represent a non 0D sample space.
     * This happens when the associated grid axis is regular.
     * @return True is the domain element points do represent a sample space.
     */
    public boolean hasSampleSpace() {
        return !isIrregular && !(getUom().equals(GRID_UOM));
    }

    /**
     * @return True if the direction of the grid axis associated with this domain element is
     * positive forwards with respect to the correspondent axis of the native CRS.
     */
    public boolean isPositiveForwards() {
        return positiveForwards;
    }
    public void setCoefficients(List<BigDecimal> coeffs) {
        this.coefficients = new ArrayList<BigDecimal>(coeffs);
    }

    public List<BigDecimal> getCoefficients() {
        return new ArrayList<BigDecimal>(this.coefficients);
    }

    @Override
    public String toString() {
        String d = "Domain #" + iOrder + " {"
                + "Name:" + label
                + " | Type:" + type
                + " | UoM:" + uom
                + " | [" + minValue
                + ","    + maxValue + "]"
                + " | CRS:" + nativeCrs + "'}";
        return d;
    }
}
