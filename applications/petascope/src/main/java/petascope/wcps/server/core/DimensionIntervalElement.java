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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.core.CoverageMetadata;
import petascope.core.CrsDefinition;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.StringUtil;
import petascope.util.TimeUtil;
import petascope.util.WcpsConstants;
import static petascope.util.WcpsConstants.MSG_STAR;

/**
 * @author <?>
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class DimensionIntervalElement extends AbstractRasNode implements ICoverageInfo {

    Logger log = LoggerFactory.getLogger(DimensionIntervalElement.class);

    private CoverageInfo info = null;
    private AxisName axis;
    private Crs crs;
    private ScalarExpr domain1;
    private ScalarExpr domain2;  // lower and upper bound, or "DomainMetadataExprType" and null
    private long cellCoord1;    // Subsets (after conversion to pixel indices)
    private long cellCoord2;
    private String axisName;
    private String axisType;
    private int counter = 0;            // counter for the domain vars
    private CoverageMetadata meta = null;       // metadata about the current coverage
    private boolean finished = false;
    private Node nextNode;
    private boolean transformedCoordinates = false;

    /**
     * Constructs an element of a dimension interval.
     * @param node XML Node
     * @param xq WCPS Xml Query object
     * @param covInfo CoverageInfo object about the Trim parent object
     * @throws WCPSException
     * @throws SecoreException
     */
    public DimensionIntervalElement(Node node, XmlQuery xq, CoverageInfo covInfo)
            throws WCPSException, SecoreException {

        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        log.trace(node.getNodeName());

        if (covInfo.getCoverageName() != null) {
            // Add Bbox information from coverage metadata, may be useful
            // for converting geo-coordinates to pixel-coordinates
            String coverageName = covInfo.getCoverageName();
            try {
                meta = xq.getMetadataSource().read(coverageName);
            } catch (Exception ex) {
                log.error(ex.getMessage());
                throw new WCPSException(ex.getMessage(), ex);
            }
        }

        while (node != null && finished == false) {
            if (node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                node = node.getNextSibling();
                continue;
            }

            // Try Axis
            try {
                axis = new AxisName(node, xq);
                node = node.getNextSibling();
                continue;
            } catch (WCPSException e) {
            }

            // Try CRS name
            try {
                crs = new Crs(node, xq);
                node = node.getNextSibling();
                if (axis == null) {
                    throw new WCPSException("Expected Axis node before CRS.");
                }
                continue;
            } catch (WCPSException e) {
            }

            // TODO: how to implement DomainMetadataExpr ?

            //            // Try last thing
            //            try
            //            {
            //                domain1 = new DomainMetadataExprType(node, xq);
            //                counter = 1;
            //                continue;
            //            }
            //            catch (WCPSException e)
            //            {
            //                System.err.println("Failed to parse domain metadata!");
            //            }

            // Then it must be a pair of nodes "lowerBound" + "upperBound"
            if (node.getNodeName().equals(WcpsConstants.MSG_LOWER_BOUND)) {
                counter = 2;
                domain1 = new ScalarExpr(node.getFirstChild(), xq);
                if (axis == null) {
                    log.error("Expected <axis> node before <lowerBound>.");
                    throw new WCPSException("Expected <axis> node before <lowerBound>.");
                }
            } else if (node.getNodeName().equals(WcpsConstants.MSG_UPPER_BOUND)) {
                counter = 2;
                domain2 = new ScalarExpr(node.getFirstChild(), xq);
                if (axis == null) {
                    log.error("Expected <lowerBound> node before <upperBound>.");
                    throw new WCPSException("Expected <lowerBound> node before <upperBound>.");
                }
            } else {
                log.error("Unexpected node: " + node.getFirstChild().getNodeName());
                throw new WCPSException("Unexpected node: " + node.getFirstChild().getNodeName());
            }

            if (axis != null && counter == 1 && domain1 != null) {
                finished = true;
            }
            if (axis != null && counter == 2 && domain1 != null && domain2 != null) {
                finished = true;
            }

            if (finished == true) {
                nextNode = node.getNextSibling();
            }

            node = node.getNextSibling();
        }

        this.axisName = axis.toRasQL();

        // Convert possible asterisks to real bounds
        stars2bounds(domain1, domain2, meta.getDomainByName(axisName));

        // Assign native CRS if not set in the interval
        if (crs == null) {
            // if no CRS is specified assume native CRS -- DM 2012-mar-05
            DomainElement axisDomain = meta.getDomainByName(axisName);
            if (axisDomain != null) {
                String crsName = axisDomain.getNativeCrs();
                log.info("  Using native CRS: " + crsName);
                crs = new Crs(crsName, xq);

                if (crsName == null) {
                    log.warn("  No native CRS specified for axis " + axisName + ", assuming pixel coordinates.");
                    crs = new Crs(CrsUtil.GRID_CRS, xq);
                }
            } else {
                log.warn("No native CRS specified for axis " + axisName + ": assuming pixel coordinates.");
                crs = new Crs(CrsUtil.GRID_CRS, xq);
                this.transformedCoordinates = true;
            }
        }

        // Axis type
        try {
            String axisSingleCrs = covInfo.getDomainElement(covInfo.getDomainIndexByName(axisName)).getNativeCrs();
            this.axisType = CrsUtil.getAxisType(CrsUtil.getGmlDefinition(axisSingleCrs), axisName);
        } catch (PetascopeException ex) {
            throw new WCPSException("Failed while getting the type of axis " + axisName + " for CRS " + crs.getName(), ex);
        } catch (SecoreException ex) {
            throw ex;
        }

        // Pixel indices are retrieved from bbox, which is stored for XY plane only.
        if (finished == true && covInfo.isGridded()) {
            if (!crs.getName().equals(CrsUtil.GRID_CRS)) {
                convertToPixelCoordinates();
            } else {
                // Set grid values which were directly set in the requests
                try {
                    // sometimes integers x is passed as x.0: parse double first to avoid parsing errors
                    cellCoord1 = (long)Double.parseDouble(domain1.getSingleValue());
                    cellCoord2 = (long)Double.parseDouble(domain2.getSingleValue());
                    this.transformedCoordinates = false;
                } catch (NumberFormatException ex) {
                    String message = ex.getMessage();
                    log.error(message);
                    throw new WCPSException(ExceptionCode.InternalComponentError, message);
                }
            }
        }
    }

    public DimensionIntervalElement(String crs, long cellCoordLo, long cellCoordHi, String axisName) throws PetascopeException, SecoreException {
        this.cellCoord1 = cellCoordLo;
        this.cellCoord2 = cellCoordHi;
        this.axisName = axisName;
        this.transformedCoordinates = true;
        this.axisType = CrsUtil.getAxisType(CrsUtil.getGmlDefinition(crs), axisName);
    }

    /* If input coordinates are geo-, convert them to pixel coordinates. */
    private void convertToPixelCoordinates() throws WCPSException {

        if (meta.getBbox() == null && crs != null) {
            throw new RuntimeException(WcpsConstants.MSG_COVERAGE + " '" + meta.getCoverageName() + "' is not georeferenced.");
        }
        if (counter == 2 && crs != null) {
            if (domain1.isSingleValue() && domain2.isSingleValue()) {
                log.debug("[Transformed] requested subsettingCrs is '{}', should match now native CRS '{}'",
                        crs.getName(), meta.getBbox().getCrsName());
                try {
                    // Convert to pixel coordinates
                    String val1 = domain1.getSingleValue();
                    String val2 = domain2.getSingleValue();
                    boolean dom1IsNum = !domain1.valueIsString();
                    boolean dom2IsNum = !domain2.valueIsString();
                    String thisAxisName = axis.toRasQL();
                    long[] pCoord = crs.convertToPixelIndices(meta, thisAxisName, val1, dom1IsNum, val2, dom2IsNum);
                    cellCoord1 = pCoord[0];
                    cellCoord2 = pCoord[1];
                    this.transformedCoordinates = true;
                } catch (PetascopeException e) {
                    this.transformedCoordinates = false;
                    log.error("Error while transforming geo-coordinates to pixel coordinates. The metadata is probably not valid.");
                    throw new WCPSException(e.getExceptionCode(), e.getMessage());
                }
            } else {
                // domain values are not number, but variables
                cellCoord1 = 0L;
                cellCoord2 = 0L;
                this.transformedCoordinates = false;
            }
        }
    }

    /* Not used */
    @Override
    public String toRasQL() {
        return "<DimensionIntervalElement Not Converted to RasQL>";
    }

    @Override
    public CoverageInfo getCoverageInfo() {
        return info;
    }

    public Node getNextNode() {
        return nextNode;
    }

    public String getAxisName() {
        if (axisName != null) {
            return axisName;
        } else {
            return this.axis.toRasQL();
        }
    }

    public String getAxisType() {
        if (axisType != null) {
            return axisType;
        } else {
            return "";
        }
    }

    public String getCrs() {
        return this.crs.toRasQL();
    }

    public String getAxisCoords() {
        return this.domain1.toRasQL() + " : " + this.domain2.toRasQL();
    }

    public String getLowCoord() {
        return this.domain1.toRasQL();
    }
    public String getHighCoord() {
        return this.domain2.toRasQL();
    }

    public String getLowCellCoord() {
        if (transformedCoordinates) {
            return String.valueOf(cellCoord1);
        } else {
            return this.domain1.toRasQL();
        }
    }

    public String getHighCellCoord() {
        if (transformedCoordinates) {
            return String.valueOf(cellCoord2);
        } else {
            return this.domain2.toRasQL();
        }
    }

    /**
     * Replaces possibly inserted asterisks with real bounds of this axis.
     * @param lo
     * @param hi
     * @param domEl
     * @throws WCPSException
     */
    private void stars2bounds(ScalarExpr lo, ScalarExpr hi, DomainElement domEl) throws WCPSException {

        try {
            if (lo.toRasQL().equals(MSG_STAR)) {
                if (hi.valueIsString() && TimeUtil.isValidTimestamp(hi.toRasQL())) {
                    // other end of interval is a timestamp: need to make a uniform subset
                    lo.setSingleValue(StringUtil.quote(
                        TimeUtil.coordinate2timestamp(
                            domEl.getMinValue().multiply(domEl.getScalarResolution()).doubleValue(),
                            domEl.getCrsDef().getDatumOrigin(),
                            domEl.getUom())
                        ));
                } else {
                    lo.setSingleValue(domEl.getMinValue().toString());
                }
            }
            if (hi.toRasQL().equals(MSG_STAR)) {
                if (lo.valueIsString() && TimeUtil.isValidTimestamp(lo.toRasQL())) {
                    // other end of interval is a timestamp: need to make a uniform subset
                    hi.setSingleValue(StringUtil.quote(
                        TimeUtil.coordinate2timestamp(
                            domEl.getMaxValue().multiply(domEl.getScalarResolution()).doubleValue(),
                            domEl.getCrsDef().getDatumOrigin(),
                            domEl.getUom())
                        ));
                } else {
                    hi.setSingleValue(domEl.getMaxValue().toString());
                }
            }
        } catch (PetascopeException ex) {
            log.debug("Error while converting asterisk to time instant equivalent.");
            throw new WCPSException(ExceptionCode.InternalComponentError, ex);
        }
    }
}
