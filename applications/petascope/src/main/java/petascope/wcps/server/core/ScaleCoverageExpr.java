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
import petascope.exceptions.WCPSException;
import petascope.util.WCPSConstants;

public class ScaleCoverageExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(ScaleCoverageExpr.class);

    private List<DimensionIntervalElement> axisList;
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo;
    private String[] dim;
    private int dims;
    private DimensionIntervalElement elem;
    private FieldInterpolationElement fieldInterp;

    public ScaleCoverageExpr(Node node, XmlQuery xq) throws WCPSException {
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
            } else if (nodeName.equals(WCPSConstants.MSG_NAME)) {
                log.trace("  " + WCPSConstants.MSG_FIELD_INTERPOLATION);
                fieldInterp = new FieldInterpolationElement(child, xq);
                child = fieldInterp.getNextNode();
            } else {
                // has to be the coverage expression
                try {
                    log.trace("  " + WCPSConstants.MSG_COVERAGE_EXPR);
                    coverageExprType = new CoverageExpr(child, xq);
                    coverageInfo = coverageExprType.getCoverageInfo();
                    super.children.add(coverageExprType);
                    child = child.getNextSibling();
                } catch (WCPSException ex) {
                    log.error(" " + WCPSConstants.ERRTXT_UNKNOWN_NODE_FOR_SCALE_COV + child.getNodeName());
                    throw new WCPSException(ExceptionCode.InvalidMetadata, WCPSConstants.ERRTXT_UNKNOWN_NODE_FOR_SCALE_COV + child.getNodeName());
                }
            }
        }
                
        // Add children to let the XML query be re-traversed
        super.children.addAll(axisList);
                
        dims = axisList.size();
        log.trace("  " + WCPSConstants.MSG_NUMBER_OF_DIMENSIONS + ": " + dims);
        dim = new String[dims];

        for (int j = 0; j < dims; ++j) {
            dim[j] = "*:*";
        }


        Iterator<DimensionIntervalElement> i = axisList.iterator();

        log.trace("  " + WCPSConstants.MSG_AXIS_LIST_COUNT + ":" + axisList.size());
        DimensionIntervalElement axis;
        int axisId, scaleId = 0;
        int axisLo, axisHi;

        while (i.hasNext()) {
            axis = i.next();
            axisId = coverageInfo.getDomainIndexByName(axis.getAxisName());
            log.trace("    " + WCPSConstants.MSG_AXIS + " " + WCPSConstants.MSG_ID + ": " + axisId);
            log.trace("    " + WCPSConstants.MSG_AXIS + " " + WCPSConstants.MSG_NAME + ": " + axis.getAxisName());

            axisLo = Integer.parseInt(axis.getLoCellCoord());
            axisHi = Integer.parseInt(axis.getHiCellCoord());
            dim[scaleId] = axisLo + ":" + axisHi;
            log.trace("    " + WCPSConstants.MSG_AXIS_COORDS + ": " + dim[scaleId]);
            ++scaleId;
            
            coverageInfo.setCellDimension(axisId,
                    new CellDomainElement(
                    BigInteger.valueOf(axisLo), BigInteger.valueOf(axisHi), axis.getAxisName()));
        }

    }

    public String toRasQL() {
        String result = WCPSConstants.MSG_SCALE + "( " + coverageExprType.toRasQL() + ", [";

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
