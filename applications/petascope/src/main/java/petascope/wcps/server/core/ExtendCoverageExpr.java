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

import petascope.exceptions.WCPSException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.util.WCPSConstants;

public class ExtendCoverageExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(ExtendCoverageExpr.class);

    private List<DimensionIntervalElement> axisList;
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo;
    private String[] dim;
    private int dims;
    private DimensionIntervalElement elem;

    public ExtendCoverageExpr(Node node, XmlQuery xq) throws WCPSException {
        
        log.trace(node.getNodeName());

        Node child;
        String nodeName;

        axisList = new ArrayList<DimensionIntervalElement>();

        child = node.getFirstChild();
        while (child != null) {
            nodeName = child.getNodeName();

            if (nodeName.equals("#" + WCPSConstants.MSG_TEXT)) {
                child = child.getNextSibling();
                continue;
            }

            try {
                coverageExprType = new CoverageExpr(child, xq);
                coverageInfo = coverageExprType.getCoverageInfo();
                super.children.add(coverageExprType);
                child = child.getNextSibling();
                continue;
            } catch (WCPSException e) {
            }

            try {
                // Start a new axis and save it
                elem = new DimensionIntervalElement(child, xq, coverageInfo, false);
                log.trace("  " + WCPSConstants.MSG_ADD_NEW_AXIS + ": " + elem.getAxisName());
                axisList.add(elem);
                super.children.add(elem);
                child = elem.getNextNode();
                continue;
            } catch (WCPSException e) {
                log.error(WCPSConstants.ERRTXT_THIS_WAS_NO_DIM + ": " + child.getNodeName());
            }

            // else unknown element
            throw new WCPSException(WCPSConstants.ERRTXT_UNKNOWN_NODE_EXTENDCOVERAGE + child.getNodeName());
        }

        dims = coverageInfo.getNumDimensions();
        log.trace("  " + WCPSConstants.MSG_NUMBER_OF_DIMENSIONS + ": " + dims);
        dim = new String[dims];

        for (int j = 0; j < dims; ++j) {
            dim[j] = null;
        }


        Iterator<DimensionIntervalElement> i = axisList.iterator();
        DimensionIntervalElement axis;
        int axisId;
        String axisLo, axisHi;

        while (i.hasNext()) {
            axis = i.next();
            
            // check if axis is sliced from the coverage, it should not go into the extend
            if (coverageExprType.slicedAxis(axis.getAxisName())) {
                continue;
            }
            
            axisId = coverageInfo.getDomainIndexByName(axis.getAxisName());
            log.trace("  " + WCPSConstants.MSG_AXIS + " " + WCPSConstants.MSG_ID + ": " + axisId);
            log.trace("  " + WCPSConstants.MSG_AXIS + " " + WCPSConstants.MSG_NAME + ": " + axis.getAxisName());
            log.trace("  " + WCPSConstants.MSG_AXIS + " " + WCPSConstants.MSG_COORDS + ": ");

            axisLo = axis.getLoCellCoord();
            axisHi = axis.getHiCellCoord();
            dim[axisId] = axisLo + ":" + axisHi;
            coverageInfo.setCellDimension(
                    axisId,
                    new CellDomainElement(axisLo, axisHi, axis.getAxisName()));
        }
    }

    public CoverageInfo getCoverageInfo() {
        return coverageInfo;
    }

    public String toRasQL() {
        String result = WCPSConstants.MSG_EXTEND + "(" + coverageExprType.toRasQL() + ",[";

        for (int j = 0, i = 0; j < dims; ++j) {
            if (dim[j] == null) {
                continue;
            }
            if (i > 0) {
                result += ",";
            }
            ++i;
            result += dim[j];
        }

        result += "])";

        return result;
    }
    
    /** 
     * Utility to check whether a specified axis is involved in this extend expression
     * @param axisName  The name of the axis (specified in the request)
     * @return True is `axisName` is extended here.
     */    
    public boolean extendsDimension(String axisName) {
        for (DimensionIntervalElement extend : axisList) {
            if (extend.getAxisName().equals(axisName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Fetch the extend values that were requested on a specified axis
     *
     * @param axisName The name of the axis (specified in the request)
     * @return An array of 2 elements [lo,hi], with the extending values on the
     * specified axis.
     */
    public Double[] extendingValues(String axisName) {
        for (DimensionIntervalElement extend : axisList) {
            if (extend.getAxisName().equals(axisName)) {
                return new Double[]{extend.getLoCoord(), extend.getHiCoord()};
            }
        }
        return new Double[]{};
    }
}
