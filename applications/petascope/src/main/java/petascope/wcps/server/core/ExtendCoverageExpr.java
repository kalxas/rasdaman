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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.WcpsConstants;
import static petascope.util.ras.RasConstants.*;

public class ExtendCoverageExpr extends AbstractRasNode implements ICoverageInfo {

    private static Logger log = LoggerFactory.getLogger(ExtendCoverageExpr.class);

    private List<DimensionIntervalElement> axisList;
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo;
    private String[] dim;
    private int dims;
    private DimensionIntervalElement elem;

    public ExtendCoverageExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {

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
                elem = new DimensionIntervalElement(child, xq, coverageInfo);
                log.trace("added new axis to list: " + elem.getAxisName());
                axisList.add(elem);
                super.children.add(elem);
                child = elem.getNextNode();
                continue;
            } catch (WCPSException e) {
                log.error("This was no DimensionIntervalELement: " + child.getNodeName());
            }

            // else unknown element
            throw new WCPSException("Unknown node for ExtendCoverage expression: " + child.getNodeName());
        }

        dims = coverageInfo.getNumDimensions();
        log.trace("Number of dimensions: " + dims);
        dim = new String[dims];

        for (int j = 0; j < dims; ++j) {
            dim[j] = null;
        }


        Iterator<DimensionIntervalElement> i = axisList.iterator();
        DimensionIntervalElement axis;
        int axisId;
        String axisLo, axisHi;
        int order =0;

        while (i.hasNext()) {
            axis = i.next();

            // check if axis is sliced from the coverage, it should not go into the extend
            if (coverageExprType.slicedAxis(axis.getAxisName())) {
                continue;
            }

            axisId = coverageInfo.getDomainIndexByName(axis.getAxisName());
            log.trace("Axis ID: " + axisId);
            log.trace("Axis name: " + axis.getAxisName());
            log.trace("Axis coords: ");

            axisLo = axis.getLowCellCoord();
            axisHi = axis.getHighCellCoord();
            dim[axisId] = axisLo + ":" + axisHi;
            coverageInfo.setCellDimension(
                    axisId,
                    new CellDomainElement(axisLo, axisHi, axisId)
                    );
            order += 1;
        }
    }

    public CoverageInfo getCoverageInfo() {
        return coverageInfo;
    }

    public String toRasQL() {
        String result = RASQL_EXTEND + "(" + coverageExprType.toRasQL() + ",[";

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
     * @throws NumberFormatException
     */
    public Double[] extendingValues(String axisName) throws NumberFormatException {
        try {
        for (DimensionIntervalElement extend : axisList) {
            if (extend.getAxisName().equals(axisName)) {
                return new Double[]{
                    Double.parseDouble(extend.getLowCoord()),
                    Double.parseDouble(extend.getHighCoord())};
            }
        }
        } catch (NumberFormatException ex) {
            log.error("Cannot extend non-numerical intervals: " + ex.getMessage());
            throw ex;
        }
        return new Double[]{};
    }
}
