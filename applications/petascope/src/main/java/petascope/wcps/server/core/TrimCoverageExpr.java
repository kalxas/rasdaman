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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.Pair;
import petascope.util.WcpsConstants;
import petascope.util.WcsUtil;

public class TrimCoverageExpr extends AbstractRasNode implements ICoverageInfo {

    private static Logger log = LoggerFactory.getLogger(TrimCoverageExpr.class);

    private List<DimensionIntervalElement> axisList;
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo;
    private String[] dimNames;
    private int dims;
    private DimensionIntervalElement elem;

    public TrimCoverageExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        log.trace(node.getNodeName());

        Node child;
        String nodeName;

        axisList = new ArrayList<DimensionIntervalElement>();

        child = node.getFirstChild();
        while (child != null) {
            nodeName = child.getNodeName();

            if (nodeName.equals("#" + WcpsConstants.MSG_TEXT)) {
                child = child.getNextSibling();
                continue;
            }

            if (nodeName.equals(WcpsConstants.MSG_AXIS)) {
                // Start a new axis and save it
                log.trace("  " + WcpsConstants.MSG_AXIS);
                elem = new DimensionIntervalElement(child, xq, coverageInfo);
                axisList.add(elem);
                child = elem.getNextNode();
            } else {
                try {
                    log.trace("  " + WcpsConstants.MSG_COVERAGE);
                    coverageExprType = new CoverageExpr(child, xq);
                    coverageInfo = coverageExprType.getCoverageInfo();
                    super.children.add(coverageExprType);
                    child = child.getNextSibling();
                    continue;
                } catch (WCPSException e) {
                    log.error("Expected coverage node, got " + nodeName);
                    throw new WCPSException("Unknown node for TrimCoverage expression: " + child.getNodeName()
                            + "[" + e.getMessage() + "]");
                }
            }
        }

        // Add children to let the XML query be re-traversed
        super.children.addAll(axisList);

        // Afterward
        dims = coverageInfo.getNumDimensions();
        log.trace("Number of dimensions: " + dims);
        dimNames = new String[dims];

        for (int j = 0; j < dims; ++j) {
            dimNames[j] = "*:*";
        }


        Iterator<DimensionIntervalElement> i = axisList.iterator();

        log.trace("Axis list count: " + axisList.size());
        DimensionIntervalElement axis;
        int axisId;
        String axisLo;
        String axisHi;
        int order = 0;

        // Set the associated cell dimensions
        while (i.hasNext()) {
            axis = i.next();
            axisId = coverageInfo.getDomainIndexByName(axis.getAxisName());

            log.trace("Axis ID: " + axisId);
            log.trace("Axis name: " + axis.getAxisName());

            // Fixing bug #394 [author Swing:It]
            // axisLo = Integer.parseInt(axis.getLoCellCoord());
            // axisHi = Integer.parseInt(axis.getHiCellCoord());
            // Start fix

            log.trace("  " + WcpsConstants.MSG_AXIS + ": Getting coordinates.");
            axisLo = axis.getLowCellCoord();
            axisHi = axis.getHighCellCoord();

            // End fix #394
            dimNames[axisId] = axisLo + ":" + axisHi;
            log.trace("    " + WcpsConstants.MSG_AXIS + " " + WcpsConstants.MSG_COORDS + ": " + dimNames[axisId]);
            coverageInfo.setCellDimension(
                    axisId,
                    new CellDomainElement(axisLo, axisHi, axisId)
                    );
            order += 1;
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

    public Pair<String[], String> computeRasQL() {
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
    public int numberOfDimensions() {
        return dims;
    }

    /**
     * @return The list of axes names specified in this trim expression
     */
    public List<String> getDimensionsNames() {
        return new ArrayList(Arrays.asList(dimNames));
    }

    /**
     * Utility to check whether a specified axis is involved in this trim expression
     * @param axisName  The name of the axis (specified in the request)
     * @return True is `axisName` is trimmed here.
     */
    public boolean trimsDimension(String axisName) {
        for (DimensionIntervalElement trim : axisList) {
            if (trim.getAxisName().equals(axisName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetch the trim values that were requested on a specified axis
     *
     * @param axisName The name of the axis (specified in the request)
     * @return An array of 2 elements [lo,hi], with the trimming values on the
     * specified axis.
     */
    public Double[] trimmingValues(String axisName) {
        for (DimensionIntervalElement trim : axisList) {
            if (trim.getAxisName().equals(axisName)) {
                return new Double[]{
                    Double.parseDouble(trim.getLowCoord()),
                    Double.parseDouble(trim.getHighCoord())};
            }
        }
        return new Double[]{};
    }
}
