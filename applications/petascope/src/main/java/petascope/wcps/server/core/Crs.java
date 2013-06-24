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
import petascope.core.CoverageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.core.DbMetadataSource;
import petascope.core.DynamicMetadataSource;
import petascope.core.IDynamicMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.AxisTypes;
import petascope.util.WcpsConstants;
import petascope.util.CrsUtil;
import petascope.util.TimeUtil;

public class Crs extends AbstractRasNode {

    private static final Logger log = LoggerFactory.getLogger(Crs.class);
    private String crsName;
    private DbMetadataSource dbMeta;
    
    public Crs(String srsName) {
        crsName = srsName;
    }
    
    public Crs(Node node, XmlQuery xq) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        log.trace(node.getNodeName());
        
        if (node != null && node.getNodeName().equals(WcpsConstants.MSG_SRS_NAME)) {
            String val = node.getTextContent();
            this.crsName = val;
            //if (crsName.equals(DomainElement.IMAGE_CRS) || crsName.equals(DomainElement.WGS84_CRS)) {
            log.trace(WcpsConstants.MSG_FOUND_CRS + ": " + crsName);
            //} else {
            //    throw new WCPSException("Invalid CRS: '" + crsName + "'");
            //}
        } else {
            throw new WCPSException(WcpsConstants.ERRTXT_COULD_NOT_FIND_SRSNAME);
        }
        
        // If coverage is not dynamic, it can be irregular: I need to query the DB to convert to pixels.
        IDynamicMetadataSource dmeta = xq.getMetadataSource();
        if (dmeta instanceof DynamicMetadataSource &&
                ((DynamicMetadataSource)dmeta).getMetadataSource() instanceof DbMetadataSource) {
            dbMeta = (DbMetadataSource) ((DynamicMetadataSource)dmeta).getMetadataSource();
        }
    }
    
    /**
     * Converts an interval subset to CRS:1 domain (grid indices).
     * @param covMeta       Metadata of the coverage
     * @param axisName      The axis label of the subset
     * @param stringLo      The lower bound of the subset
     * @param loIsNumeric   True if the bound is a numeric value (otherwise timestamp)
     * @param stringHi      The upper bound of the subset
     * @param hiIsNumeric   True if the bound is a numeric value (otherwise timestamp)
     * @return              The pixel indices corresponding to this subset
     * @throws WCPSException 
     */
    public long[] convertToPixelIndices(CoverageMetadata covMeta, String axisName,
            String stringLo, boolean loIsNumeric, String stringHi, boolean hiIsNumeric)
            throws PetascopeException {
        // TODO: although now grid axes are meant to be aligned with CRS axes, the conversion
        //       here should involve vectorial operations (use petascope.util.Vectors.java),
        //       eg dot-product, etc.
        
        long[] out = new long[2];
        
        // IMPORTANT: y axis are decreasing wrt pixel domain
        // TODO: generalize behaviour by assigning a direction to each axis (e.g. in the DomainElement object).
        boolean zeroIsMin = !axisName.equals(AxisTypes.Y_AXIS);
        
        DomainElement      dom = covMeta.getDomainByName(axisName);
        CellDomainElement cdom = covMeta.getCellDomainByName(axisName);        
        
        // null-pointers check
        if (null == cdom || null == dom) {
            log.error("Could not find the \"" + axisName + "\" axis for coverage:" + covMeta.getCoverageName());
            throw new PetascopeException(ExceptionCode.NoApplicableCode, "Could not find the \"" + axisName + "\" axis for coverage:" + covMeta.getCoverageName());
        }
        
        // Get datum origin can also be null if !temporal axis: use axisType to guard)
        String datumOrigin = dom.getAxisDef().getCrsDefinition().getDatumOrigin();
        
        // Get Unit of Measure of this axis:
        String axisUoM = dom.getUom();
        
        // Get cellDomain extremes
        long pxMin = cdom.getLo().longValue();
        long pxMax = cdom.getHi().longValue();
        log.trace(WcpsConstants.MSG_CELL_DOMAIN_EXTREMES + pxMin + ", " + WcpsConstants.MSG_HIGH_U + ":" + pxMax);
        
        // Get Domain extremes (real sdom)
        BigDecimal domMin = dom.getMinValue();
        BigDecimal domMax = dom.getMaxValue();
        log.trace("Domain extremes coordinates: (" + domMin + ", " + domMax + ")");
        log.trace("Subset cooordinates: (" + stringLo + ", " + stringHi + ")");
        
        /*---------------------------------*/
        /*         VALIDITY CHECKS         */
        /*---------------------------------*/
        log.trace("Checking order, format and bounds of axis {} ...", axisName);

        // Requires homogeneity in the bounds of a subset
        if (loIsNumeric ^ hiIsNumeric) { // XOR
            log.error("(" + stringLo + "," + stringHi + ") subset is invalid.");
            throw new PetascopeException(ExceptionCode.InvalidRequest,
                    "(" + stringLo + "," + stringHi + ") subset requires bounds of the same domain.");
        }
        //~(From now on, some tests can be made on a single bound)
        boolean subsetWithTimestamps = !loIsNumeric; // = !hiIsNumeric
        
        // if subsets are not numeric, /now/ they other choice is that they are timestamps: check the format is valid
        if (subsetWithTimestamps && !TimeUtil.isValidTimestamp(stringLo)) {
            throw new WCPSException(ExceptionCode.InvalidRequest,
                    "Subset '" + stringLo + "' is not valid nor as a number, nor as a supported time description. "
                  + "See Date4J javadoc for supported formats.");
        }
        if (subsetWithTimestamps && !TimeUtil.isValidTimestamp(stringHi)) {
            throw new WCPSException(ExceptionCode.InvalidRequest,
                    "Subset '" + stringHi + "' is not valid nor as a number, nor as a supported time description. "
                  + "See Date4J javadoc for supported formats.");
        }
        
        // Check order of subset: separate treatment for temporal axis with timestamps subsets
        if (subsetWithTimestamps) {            
            // Check order
            if (!TimeUtil.isOrderedTimeSubset(stringLo, stringHi)) {
                throw new PetascopeException(ExceptionCode.InvalidSubsetting,
                        axisName + " axis: lower bound " + stringLo + " is greater then the upper bound " + stringHi);
            }            
        } else {
            
            // Numerical axis:
            double coordLo = Double.parseDouble(stringLo);
            double coordHi = Double.parseDouble(stringHi);
            
            // Check order
            if (coordHi < coordLo) {
                throw new PetascopeException(ExceptionCode.InvalidSubsetting,
                        axisName + " axis: lower bound " + coordLo + " is greater the upper bound " + coordHi);
            }
            
            // Check intersection with extents
            if (dom.getUom().equals(CrsUtil.PIXEL_UOM)) {
                // Pixel-domain axis: get info from cellDomain (sdom) directly.
                if (coordLo > pxMax || coordHi < pxMin) {
                    throw new PetascopeException(ExceptionCode.InvalidSubsetting,
                            axisName + " axis: subset (" + coordLo + ":" + coordHi + ") is out of bounds.");
                }
            } else {
                if (coordLo > domMax.doubleValue() || coordHi < domMin.doubleValue()) {
                    throw new PetascopeException(ExceptionCode.InvalidSubsetting,
                            axisName + " axis: subset (" + coordLo + ":" + coordHi + ") is out of bounds.");
                }
            }
        }
        
        /*---------------------------------*/
        /*             CONVERT             */
        /*---------------------------------*/
        log.trace("Converting axis {} interval to pixel indices ...", axisName);
        // There can be several different cases depending on spacing and type of this axis:
        if (dom.isIrregular()) {
            
            // Consistency check
            // TODO need to find a way to solve `static` issue of dbMeta
            if (dbMeta == null) {
                throw new PetascopeException(ExceptionCode.InternalComponentError,
                        "Axis " + axisName + " is irregular but is not linked to DbMetadataSource.");
            }
            
            // Need to query the database (IRRSERIES table) to get the extents
            try {
                String numLo = stringLo;
                String numHi = stringHi;
                
                if (subsetWithTimestamps) {
                    // Need to convert timestamps to TemporalCRS numeric coordinates
                    numLo = "" + TimeUtil.countOffsets(datumOrigin, stringLo, axisUoM);
                    numHi = "" + TimeUtil.countOffsets(datumOrigin, stringHi, axisUoM);
                }
            
                // Retrieve correspondent cell indexes (unique method for numerical/timestamp values)
                // TODO: I need to extract all the values, not just the extremes
                out = dbMeta.getIndexesFromIrregularRectilinearAxis(
                        covMeta.getCoverageName(),
                        covMeta.getDomainIndexByName(axisName), // i-order of axis
                        new BigDecimal(numLo), 
                        new BigDecimal(numHi),
                        pxMin, pxMax);
                
                // Retrieve the coefficients values and store them in the DomainElement
                dom.setCoefficients(dbMeta.getCoefficientsOfInterval(
                        covMeta.getCoverageName(),
                        covMeta.getDomainIndexByName(axisName), // i-order of axis
                        new BigDecimal(numLo), 
                        new BigDecimal(numHi)
                        ));
                
                // Add sdom lower bound
                out[0] = out[0] + pxMin;
                
            } catch (Exception e) {
                throw new PetascopeException(ExceptionCode.InternalComponentError,
                        "Error while fetching cell boundaries of irregular axis '" +
                        axisName + "' of coverage " + covMeta.getCoverageName() + ": " + e.getMessage(), e);
            }
            
        } else {
            // The axis is regular: need to differentiate between numeric and timestamps
            if (subsetWithTimestamps) {
                // Need to convert timestamps to TemporalCRS numeric coordinates
                int numLo = TimeUtil.countOffsets(datumOrigin, stringLo, axisUoM);
                int numHi = TimeUtil.countOffsets(datumOrigin, stringHi, axisUoM);                
                
                // Consistency check
                if (numHi < domMin.doubleValue() || numLo > domMax.doubleValue()) {
                    throw new PetascopeException(ExceptionCode.InternalComponentError,
                            "Translated pixel indixes of regular temporal axis (" + 
                            numLo + ":" + numHi +") exceed the allowed values.");
                }
                
                // Replace timestamps with numeric subsets
                stringLo = "" + numLo;
                stringHi = "" + numHi;
                // Now subsets can be tranlsated to pixels with normal numeric proportion                
            }
            
            
            /* Loop to get the pixel subset values.
             * Different cases (0%-overlap subsets are excluded by the checks above)
             *        MIN                                                     MAX
             *         |-------------+-------------+-------------+-------------|
             *              cell0         cell1         cell2         cell3
             * i)   |_____________________|
             * ii)                              |__________________|
             * iii)                                                         |_________________|
             */
            // Numeric interval: simple mathematical proportion
            double coordLo = Double.parseDouble(stringLo);
            double coordHi = Double.parseDouble(stringHi);
            
            // Get cell dimension -- Use BigDecimals to avoid finite arithmetic rounding issues of Doubles
            //double cellWidth = dom.getResolution().doubleValue();
            double cellWidth = (
                    domMax.subtract(domMin))
                   .divide((BigDecimal.valueOf(pxMax+1)).subtract(BigDecimal.valueOf(pxMin)), RoundingMode.UP)
                   .doubleValue();
            //      = (dDomHi-dDomLo)/(double)((pxHi-pxLo)+1);
            
            // Open interval on the right: take away epsilon from upper bound:
            //double coordHiEps = coordHi - cellWidth/10000;    // [a,b) subsets
            double coordHiEps = coordHi;                        // [a,b] subsets
            
            // Conversion to pixel domain
            /*
             * Policy = minimum encompassing BoundingBox returned (of course)
             *
             *     9°       10°       11°       12°       13°
             *     o---------o---------o---------o---------o
             *     |  cell0  |  cell1  |  cell2  |  cell3  |
             *     [=== s1 ==]
             *          [=== s2 ==]
             *           [===== s3 ====]
             *      [= s4 =]
             *
             * -- [a,b] closed intervals:
             * s1(9°  ,10°  ) -->  [cell0:cell1]
             * s2(9.5°,10.5°) -->  [cell0:cell1]
             * s3(9.7°,11°  ) -->  [cell0:cell2]
             * s4(9.2°,9.8° ) -->  [cell0:cell0]
             *
             * -- [a,b) open intervals:
             * s1(9°  ,10°  ) -->  [cell0:cell0]
             * s2(9.5°,10.5°) -->  [cell0:cell1]
             * s3(9.7°,11°  ) -->  [cell0:cell1]
             * s4(9.2°,9.8° ) -->  [cell0:cell0]
             */
            if (dom.getUom().equals(CrsUtil.PIXEL_UOM)) {
                // Gridspace referenced image: subsets *are* pixels
                out = new long[] { (long)coordLo, (long)coordHi };
            } else if (zeroIsMin) {
                // Normal linear numerical axis
                out = new long[] {
                    (long)Math.floor((coordLo    - domMin.doubleValue()) / cellWidth) + pxMin,
                    (long)Math.floor((coordHiEps - domMin.doubleValue()) / cellWidth) + pxMin
                };
            } else {
                // Linear negative axis (eg northing of georeferenced images)
                out = new long[] {
                    // First coordHi, so that left-hand index is the lower one
                    (long)Math.ceil((domMax.doubleValue() - coordHiEps) / cellWidth) + pxMin,
                    (long)Math.ceil((domMax.doubleValue() - coordLo)    / cellWidth) + pxMin
                };
            }
            log.debug("Transformed coords indices (" + out[0] + "," + out[1] + ")");     
        }
        
        // Check outside bounds:
        out[0] = (out[0]<pxMin) ? pxMin : ((out[0]>pxMax)?pxMax:out[0]);
        out[1] = (out[1]<pxMin) ? pxMin : ((out[1]>pxMax)?pxMax:out[1]);
        log.debug("Transformed rebounded coords indices (" + out[0] + "," + out[1] + ")");
        
        return out;
    }
    // Dummy overload (for DimensionPointElements)
    public long convertToPixelIndices(CoverageMetadata meta, String axisName, String value, boolean isNumeric) throws PetascopeException {
        return convertToPixelIndices(meta, axisName, value, isNumeric, value, isNumeric)[0];
    }
    
    @Override
    public String toRasQL() {
        return crsName;
    }
    
    public String getName() {
        return crsName;
    }
}
