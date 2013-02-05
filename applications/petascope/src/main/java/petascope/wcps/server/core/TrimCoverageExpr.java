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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.WCPSException;
import petascope.util.Pair;
import petascope.util.WCPSConstants;
import petascope.util.WcsUtil;

public class TrimCoverageExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(TrimCoverageExpr.class);
    
    private List<DimensionIntervalElement> axisList;
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo;
    private String[] dimNames;
    private int dims;
    private DimensionIntervalElement elem;

    public TrimCoverageExpr(Node node, XmlQuery xq) throws WCPSException {
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

            if (nodeName.equals(WCPSConstants.MSG_AXIS)) {
                // Start a new axis and save it
                log.trace("  " + WCPSConstants.MSG_AXIS);
                elem = new DimensionIntervalElement(child, xq, coverageInfo);
                axisList.add(elem);
                child = elem.getNextNode();
            } else {
                try {
                    log.trace("  " + WCPSConstants.MSG_COVERAGE);
                    coverageExprType = new CoverageExpr(child, xq);
                    coverageInfo = coverageExprType.getCoverageInfo();
                    super.children.add(coverageExprType);
                    child = child.getNextSibling();
                    continue;
                } catch (WCPSException e) {
                    log.error("  " + WCPSConstants.ERRTXT_EXPECTED_COVERAGE_NODE_GOT + " " + nodeName);
                    throw new WCPSException(WCPSConstants.ERRTXT_UNKNOWN_NODE_FOR_TRIM_COV + ":" + child.getNodeName() 
                            + "[" + e.getMessage() + "]");
                }
            }
        }
        
        // Add children to let the XML query be re-traversed
        super.children.addAll(axisList);     
        
        // Afterward
        dims = coverageInfo.getNumDimensions();
        log.trace("  " + WCPSConstants.MSG_NUMBER_OF_DIMENSIONS + ": " + dims);
        dimNames = new String[dims];

        for (int j = 0; j < dims; ++j) {
            dimNames[j] = "*:*";
        }


        Iterator<DimensionIntervalElement> i = axisList.iterator();

        log.trace("  " + WCPSConstants.MSG_AXIS_LIST_COUNT + ": " + axisList.size());
        DimensionIntervalElement axis;
        int axisId;
        int axisLo, axisHi;

        while (i.hasNext()) {
            axis = i.next();
            axisId = coverageInfo.getDomainIndexByName(axis.getAxisName());
            log.trace("    " + WCPSConstants.MSG_AXIS + " " + WCPSConstants.MSG_ID + ": " + axisId);
            log.trace("    " + WCPSConstants.MSG_AXIS + " " + WCPSConstants.MSG_NAME + ": " + axis.getAxisName());

            axisLo = Integer.parseInt(axis.getLoCellCoord());
            axisHi = Integer.parseInt(axis.getHiCellCoord());
            dimNames[axisId] = axisLo + ":" + axisHi;
            log.trace("    " + WCPSConstants.MSG_AXIS + " " + WCPSConstants.MSG_COORDS + ": " + dimNames[axisId]);
            coverageInfo.setCellDimension(
                    axisId,
                    new CellDomainElement(
                    BigInteger.valueOf(axisLo), BigInteger.valueOf(axisHi), axis.getAxisName()));
        }

    }

    @Override
    public CoverageInfo getCoverageInfo() {
        return coverageInfo;
    }

    @Override
    public String toRasQL() {
        Pair<String[], String> res = computeRasQL();
        
        String ret = res.snd + "[";
        for (int j = 0; j < res.fst.length; ++j) {
            if (j > 0) {
                ret += ",";
            }

            ret += res.fst[j];
        }

        ret += "]";
        return ret;
    }
    
    Pair<String[], String> computeRasQL() {
        Pair<String[], String> res = null;
        IRasNode c = coverageExprType.getChild();
        if (c instanceof SubsetOperationCoverageExpr) {
            res = ((SubsetOperationCoverageExpr) c).computeRasQL();
        } else if (c instanceof TrimCoverageExpr) {
            res = ((TrimCoverageExpr) c).computeRasQL();
        } else if (c instanceof SliceCoverageExpr) {
            res = ((SliceCoverageExpr) c).computeRasQL();
        } else {
            return Pair.of(dimNames, coverageExprType.toRasQL());
        }
        String[] a = res.fst;
        String[] b = new String[dims];
        for (int i = 0; i < dims; i++) {
            b[i] = WcsUtil.min(a[i], dimNames[i]);
        }
        
        return Pair.of(b, res.snd);
    }
    
    /** 
     * @return How many dimensions are specified in this trim expression
     */
    int numberOfDimensions() {
        return dims;
    }
    
    /**
     * @return The list of axes names specified in this trim expression
     */
    List<String> getDimensionsNames() {
        return new ArrayList(Arrays.asList(dimNames));
    }
    
    /** 
     * Utility to check whether a specified axis is involved in this trim expression
     * @param axisName  The name of the axis (specified in the request)
     * @return True is `axisName` is trimmed here.
     */    
    boolean trimsDimension(String axisName) {
        for (DimensionIntervalElement trim : axisList) {
            if (trim.getAxisName().equals(axisName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Fetch the trim values that were requested on a specified axis
     * @param axisName The name of the axis (specified in the request)
     * @return An array of 2 elements [lo,hi], with the trimming values on the specified axis.
     */
    Double[] trimmingValues(String axisName) {
        for (DimensionIntervalElement trim : axisList) {
            if (trim.getAxisName().equals(axisName)) {
                return new Double[]{trim.getLoCoord(), trim.getHiCoord()};
            }
        }
        return new Double[]{};
    }    
}
