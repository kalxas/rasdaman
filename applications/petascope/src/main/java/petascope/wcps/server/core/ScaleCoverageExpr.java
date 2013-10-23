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
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.WcpsConstants;

public class ScaleCoverageExpr extends AbstractRasNode implements ICoverageInfo {

    private static Logger log = LoggerFactory.getLogger(ScaleCoverageExpr.class);

    private List<DimensionIntervalElement> axisList;
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo;
    private String[] dim;
    private int dims;
    private DimensionIntervalElement elem;
    private FieldInterpolationElement fieldInterp;

    public ScaleCoverageExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
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
            } else if (nodeName.equals(WcpsConstants.MSG_NAME)) {
                log.trace("Field interpolation");
                fieldInterp = new FieldInterpolationElement(child, xq);
                child = fieldInterp.getNextNode();
            } else if (nodeName.equals(WcpsConstants.MSG_IMAGE_CRSDOMAIN)) {
                log.trace("  " + WcpsConstants.MSG_IMAGE_CRSDOMAIN);
                child = child.getFirstChild();
                while (child != null && child.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                    child = child.getNextSibling();
                }

                CoverageExpr covExprType = new CoverageExpr(child, xq);
                CoverageInfo covInfo = covExprType.getCoverageInfo();
                int n = covInfo.getNumDimensions();
                for (int i = 0; i < n; i++) {
                    CellDomainElement cde = covInfo.getCellDomainElement(i);
                    // pc NOTE[MERGE]: using order of CellDomainElement as axis label: otherwise need to restore labels in CellDomainElement?
                    DimensionIntervalElement die = new DimensionIntervalElement(cde.getLoInt(), cde.getHiInt(), covInfo.getDomainElement(cde.getOrder()).getLabel());
                    axisList.add(die);
                }
                if (child != null) {
                    child = child.getNextSibling();
                }
            } else {
                // has to be the coverage expression
                try {
                    log.trace("coverage expression");
                    coverageExprType = new CoverageExpr(child, xq);
                    coverageInfo = coverageExprType.getCoverageInfo();
                    super.children.add(coverageExprType);
                    child = child.getNextSibling();
                } catch (WCPSException ex) {
                    log.error("Unknown node for ScaleCoverageExpr expression: " + child.getNodeName());
                    throw new WCPSException(ExceptionCode.InvalidMetadata, "Unknown node for ScaleCoverageExpr expression: " + child.getNodeName());
                }
            }
        }

        // Add children to let the XML query be re-traversed
        super.children.addAll(axisList);

        dims = axisList.size();
        log.trace("Number of dimensions: " + dims);
        dim = new String[dims];

        for (int j = 0; j < dims; ++j) {
            dim[j] = "*:*";
        }


        Iterator<DimensionIntervalElement> i = axisList.iterator();

        log.trace("Axis list count: " + axisList.size());
        DimensionIntervalElement axis;
        int axisId, scaleId = 0;
        String axisLo;
        String axisHi;
        int order = 0;

        while (i.hasNext()) {
            axis = i.next();
            axisId = coverageInfo.getDomainIndexByName(axis.getAxisName());
            log.trace("Axis ID: " + axisId);
            log.trace("Axis name: " + axis.getAxisName());

            axisLo = axis.getLowCellCoord();
            axisHi = axis.getHighCellCoord();
            dim[scaleId] = axisLo + ":" + axisHi;
            log.trace("axis coords: " + dim[scaleId]);
            ++scaleId;

            coverageInfo.setCellDimension(axisId,
                    new CellDomainElement(axisLo, axisHi, order)
                    );
            order += 1;
        }

    }

    public String toRasQL() {
        String result = WcpsConstants.MSG_SCALE + "( " + coverageExprType.toRasQL() + ", [";

        for (int j = 0; j < dims; ++j) {
            if (j > 0) {
                result += ",";
            }

            result += dim[j];
        }

        result += "] )";
        return result;
    }

    public CoverageInfo getCoverageInfo() {
        return coverageInfo;
    }
}
