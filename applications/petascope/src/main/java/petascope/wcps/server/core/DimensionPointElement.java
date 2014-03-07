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
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.WcpsConstants;
import static petascope.util.WcpsConstants.MSG_STAR;


public class DimensionPointElement extends AbstractRasNode {

    Logger log = LoggerFactory.getLogger(DimensionPointElement.class);

    private IRasNode child;
    private ScalarExpr domain;
    private AxisName axis;
    private Crs crs;
    private boolean finished = false;
    private Node nextNode;
    private CoverageMetadata meta = null;       // metadata about the current coverage
    private boolean transformedCoordinates = false;
    private long coord;

    public DimensionPointElement(Node node, XmlQuery xq, CoverageInfo covInfo)
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

        String name;

        while (node != null && finished == false) {
            if (node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                node = node.getNextSibling();
                continue;
            }

            // Try Axis
            try {
                log.trace("Matching axis name.");
                axis = new AxisName(node, xq);
                node = node.getNextSibling();
                continue;
            } catch (WCPSException e) {
            }

            // Try CRS name
            try {
                log.trace("Matching crs.");
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
//                log.error("Failed to parse domain metadata!");
//            }

            // Then it must be a "slicingPosition"
            if (node.getNodeName().equals(WcpsConstants.MSG_SLICING_POSITION)) {
                log.trace("Slice position");
                domain = new ScalarExpr(node.getFirstChild(), xq);
                if (axis == null) {
                    throw new WCPSException("Expected <axis> node before <slicingPosition>.");
                }
            } else {
                throw new WCPSException("Unexpected node: " + node.getFirstChild().getNodeName());
            }

            if (domain.toRasQL().equals(MSG_STAR)) {
                // Throw InvalidSubsetting to let the exception surface out of WCPS parsing (see CoverageExpr)
                throw new WCPSException(ExceptionCode.InvalidSubsetting, "Cannot use asterisk in slicing expression.");
            }

            if (axis != null && domain != null) {
                finished = true;
            }

            if (finished == true) {
                nextNode = node.getNextSibling();
            }

            node = node.getNextSibling();
        }

        if (crs == null) {
            // if no CRS is specified assume native CRS -- DM 2012-mar-05
            String axisName = axis.toRasQL();

            DomainElement axisDomain = meta.getDomainByName(axisName);
            if (axisDomain != null) {
                String crsName = axisDomain.getNativeCrs();
                log.info("Using native CRS: " + crsName);
                crs = new Crs(crsName, xq);
            } else {
                log.warn("No native CRS specified for axis " + axisName + ": assuming pixel coordinates.");
                crs = new Crs(CrsUtil.GRID_CRS, xq);
                this.transformedCoordinates = true;
            }
        }

        if (finished == true &&
                covInfo.isGridded() &&
                crs != null &&
                !crs.getName().equals(CrsUtil.GRID_CRS)) {
            convertToPixelCoordinate();
        }
    }

    /* If input coordinates are geo-, convert them to pixel coordinates. */
    private void convertToPixelCoordinate() throws WCPSException {
        if (meta.getBbox() == null && crs != null) {
            throw new RuntimeException(WcpsConstants.MSG_COVERAGE + " '" + meta.getCoverageName() + "' is not georeferenced.");
        }
        if (crs != null && domain.isSingleValue()) {
            log.debug("[Transformed] requested subsettingCrs is '{}', should match now native CRS is '{}'", crs.getName(), meta.getBbox().getCrsName());
            try {
                this.transformedCoordinates = true;
                // Convert to pixel coordinates
                String val = domain.getSingleValue();
                boolean domIsNum = !domain.valueIsString();
                String axisName = axis.toRasQL();
                coord = crs.convertToPixelIndices(meta, axisName, val, domIsNum);
            } catch (PetascopeException e) {
                this.transformedCoordinates = false;
                log.error("Error while transforming geo-coordinates to pixel coordinates. The metadata is probably not valid.");
                throw new WCPSException(e.getExceptionCode(), e.getMessage());
            }
        } // else no crs was embedded in the slice expression
    }

    @Override
    public String toRasQL() {
        return child.toRasQL();
    }

    public Node getNextNode() {
        return nextNode;
    }

    public String getAxisName() {
        return this.axis.toRasQL();
    }

    public String getCrsName() {
        return this.crs.toRasQL();
    }

    public String getSlicingPosition() {
        if (transformedCoordinates) {
            return String.valueOf(coord);
        } else {
            return this.domain.toRasQL();
        }
    }
}
