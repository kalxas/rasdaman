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
import org.w3c.dom.Node;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.core.DynamicMetadataSource;
import petascope.core.IDynamicMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.WcpsConstants;
import petascope.util.WcsUtil;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.CoverageInfo;

public class SliceCoverageExpr extends AbstractRasNode implements ICoverageInfo {

    private static Logger log = LoggerFactory.getLogger(SliceCoverageExpr.class);

    private List<DimensionPointElement> axisList;
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo = null;
    private String[] dimNames;
    private DimensionPointElement elem;
    private int dims;

    public SliceCoverageExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        log.trace(node.getNodeName());

        Node child = node.getFirstChild();
        String nodeName;

        axisList = new ArrayList<DimensionPointElement>();

        while (child != null) {
            nodeName = child.getNodeName();

            if (nodeName.equals("#" + WcpsConstants.MSG_TEXT)) {
                child = child.getNextSibling();
                continue;
            }

            if (nodeName.equals(WcpsConstants.MSG_AXIS)) {
                // Start a new axis and save it
                log.trace("  " + WcpsConstants.MSG_AXIS);
                try {
                    elem = new DimensionPointElement(child, xq, coverageInfo);
                } catch (WCPSException ex) {
                    throw ex;
                }
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
                    throw new WCPSException("Unknown node for SliceCoverage expression: " + child.getNodeName());
                }
            }
        }

        // Add children to let the XML query be re-traversed
        super.children.addAll(axisList);

        dims = coverageInfo.getNumDimensions();
        log.trace("Number of dimensions: " + dims);
        dimNames = new String[dims];

        for (int j = 0; j < dims; ++j) {
            dimNames[j] = "*:*";
        }

        Iterator<DimensionPointElement> i = axisList.iterator();
        DimensionPointElement axis;
        int axisId;
        String slicingPosInt;
        String slicingPosStr;
        int order = 0;

        while (i.hasNext()) {
            axis = i.next();
            /* TODO: BUG: This searches the axis types list using name, not type */
            axisId = coverageInfo.getDomainIndexByName(axis.getAxisName());
            slicingPosStr = axis.getSlicingPosition();
            // NOTE: in case get slicing position is domain or imageCrsDomain it will return interval in "(lo,high)"
            // need to convert interval into pixel coordinates
            if (slicingPosStr.matches("\\(.*,.*\\)")) {
                slicingPosStr = convertDomainIntervalToPixelInterval(slicingPosStr, axis, xq);
            }

            dimNames[axisId] = slicingPosStr;
            // Slicing position can be a constant number or a variable reference
            try {
                slicingPosInt = slicingPosStr;
            } catch (NumberFormatException e) {
                slicingPosInt = "1";
            }
            log.trace("Slice at axis id: " + axisId + ", axis name: " + axis.getAxisName() + ", slicing position: " + slicingPosInt);
            coverageInfo.setCellDimension(
                axisId,
                new CellDomainElement(slicingPosInt, slicingPosInt, order)
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

        String ret = "(" + res.snd + ") [";
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
     * @return How many dimensions are specified in this slice expression
     */
    public int numberOfDimensions() {
        return dims;
    }

    /**
     * @return The list of axes names specified in this slice expression
     */
    public List<String> getDimensionsNames() {
        return new ArrayList(Arrays.asList(dimNames));
    }

    /**
     * Utility to check whether a specified axis is involved in this slice expression
     * @param axisName  The name of the axis (specified in the request)
     * @return True is `axisName` is sliced here.
     */
    public boolean slicesDimension(String axisName) {
        for (DimensionPointElement slice : axisList) {
            if (slice.getAxisName().equals(axisName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param domainInterval: Domain Interval, e.g (Lat[lo]:Lat[hi]) in CRS degree
     * @param axis: Axis Name, e.g (Lat)
     * @param xq: XML Query
     * @return Returns Pixel Interval from Domain Interval, e.g (Lat[lo]:Lat[hi]) -> (lo:hi) in grid integer
     * @throws WCPSException
     * @throws PetascopeException
     */
    public String convertDomainIntervalToPixelInterval(String domainInterval, DimensionPointElement axis, XmlQuery xq) throws WCPSException {
        String tmp = domainInterval;
        tmp = tmp.substring(tmp.indexOf("(") + 1, tmp.indexOf(")"));
        tmp = tmp.replace(",", ":");
        String[] domainTmp = tmp.split(":");

        // Convert to pixel coordinates
        String val1 = domainTmp[0];
        String val2 = domainTmp[1];
        String thisAxisName = axis.getAxisName();

        CoverageMetadata meta = null;
        try {
            meta = xq.getMetadataSource().read(getCoverageInfo().getCoverageName());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new WCPSException(ex.getMessage(), ex);
        }

        DbMetadataSource dbMeta = null;

        IDynamicMetadataSource dmeta = xq.getMetadataSource();
        if (dmeta instanceof DynamicMetadataSource &&
                ((DynamicMetadataSource)dmeta).getMetadataSource() instanceof DbMetadataSource) {
            dbMeta = (DbMetadataSource)((DynamicMetadataSource)dmeta).getMetadataSource();
        }

        // Return lo:hi pixel interval
        long[] pCoord;
        try {
            pCoord = CrsUtil.convertToInternalGridIndices(meta, dbMeta, thisAxisName, val1, true, val2, true);
        } catch (PetascopeException ex) {
            throw new WCPSException(ex.getExceptionCode(), ex.getExceptionText());
        }
        domainInterval = pCoord[0] + ":" + pCoord[1];

        return domainInterval;
    }
}
