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

import java.util.Iterator;
import java.util.List;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.MiscUtil;
import petascope.util.WcpsConstants;

public class CoverageExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(CoverageExpr.class);

    private IRasNode child;
    private String childInfo;
    private CoverageInfo info;
    private boolean scalarExpr = false;
    private boolean simpleCoverage;    // True if the coverage is just a string
    private String exMessage = "";
    private static List<SliceCoverageExpr> slices = null;

    public CoverageExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        if (node == null) {
            throw new WCPSException("CoverageExprType parsing error.");
        }

        String nodeName = node.getNodeName();
        log.trace(nodeName);

        simpleCoverage = false;

        if (nodeName.equals(WcpsConstants.MSG_COVERAGE)) {
            simpleCoverage = true;
            childInfo = node.getFirstChild().getNodeValue();

            if (!xq.isIteratorDefined(childInfo)) {
                throw new WCPSException(WcpsConstants.MSG_ITERATOR + " " + childInfo + " not defined");
            }

            Iterator<String> coverages = xq.getCoverages(childInfo);

            try {
                info = new CoverageInfo(xq.getMetadataSource().read(coverages.next()));

                while (coverages.hasNext()) {    // Check if all the coverages are compatible
                    CoverageInfo tmp = new CoverageInfo(
                            xq.getMetadataSource().read(
                            coverages.next()));

                    if (!tmp.isCompatible(info)) {
                        throw new WCPSException("Incompatible coverages within the same iterator.");
                    }
                }
            } catch (Exception ex) {
                throw new WCPSException(ex.getMessage(), ex);
            }

            log.trace("Found simple coverage definition: " + childInfo + ", " + info.toString());
        } else if (nodeName.equals(WcpsConstants.MSG_CRS_TRANSFORM)) {
            // TODO: implement CrsTransform class
            child = new CrsTransformCoverageExpr(node, xq);
        } else if (nodeName.equals(WcpsConstants.MSG_SCALE)) {
            child = new ScaleCoverageExpr(node, xq);
        } else if (nodeName.equals(WcpsConstants.MSG_CONSTRUCT)) {
            child = new ConstructCoverageExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WcpsConstants.MSG_CONST)) {
            child = new ConstantCoverageExpr(node.getFirstChild(), xq);
        } else if (nodeName.equals(WcpsConstants.MSG_SWITCH)) {
            child = new SwitchExpr(node, xq);
        }
        //        else if (nodeName.equals("variableRef"))
        //        {
        //            child = new VariableReference(node, xq);
        //        }
        else {    // Try one of the groups
            child = null;
            String firstMessage = "";

            Node childNode = node;
            while ((childNode != null) && childNode.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                childNode = childNode.getNextSibling();
            }
            String n = childNode.getNodeName();

            // TODO: not implemented
//            if (child == null) {
//                try {
//                    child = new SetMetadataCoverageExpr(node, xq);
//                    log.trace("  " + WcpsConstants.MSG_MATCHED_SET_METADATA);
//                } catch (WCPSException e) {
//                    child = null;
//                    exMessage = e.getMessage();
//                    firstMessage = exMessage;
//                }
//            }

            if (child == null) {
                if (n.equals(WcpsConstants.MSG_RANGE_CONSTRUCTOR) ||
                    UnaryOperationCoverageExpr.NODE_NAMES.contains(n) ||
                    BinaryOperationCoverageExpr.NODE_NAMES.contains(nodeName)) {
                    try {
                        child = new InducedOperationCoverageExpr(node, xq);
                        log.trace("Matched induced coverage expression operation.");
                    } catch (WCPSException e) {
                        child = null;
                        if (e.getMessage().equals("Method not implemented")) {
                            throw e;
                        }
                    }
                }
            }

            if (child == null) {
                if (SubsetOperationCoverageExpr.NODE_NAMES.contains(n)) {
                    try {
                        child = new SubsetOperationCoverageExpr(node, xq);
                        log.trace("Matched subset operation.");
                    } catch (WCPSException e) {
                        if (e.getExceptionCode().equals(ExceptionCode.MissingCRS) ||
                            e.getExceptionCode().equals(ExceptionCode.InvalidSubsetting)) {
                            throw(e);
                        }
                        child = null; 
                        exMessage = exMessage.equals(firstMessage) ? e.getMessage() : exMessage;
                    }
                }
            }

            if (child == null) {
                try {
                    child = new ScalarExpr(node, xq);
                    this.scalarExpr = true;
                    log.trace("Matched scalar expression.");
                } catch (WCPSException e) {
                    child = null;
                    exMessage = exMessage.equals(firstMessage) ? e.getMessage() : exMessage;
                }
            }
        }

        if (!simpleCoverage && (child == null)) {
            throw new WCPSException("Invalid coverage Expression, next node: "
                    + node.getNodeName() + " - " + exMessage);
        }

        if (info == null) {
            info = new CoverageInfo(((ICoverageInfo) child).getCoverageInfo());
        }
        
        if (!(child == null)) {
            // Keep child for XML tree crawling
            super.children.add(child);
        }

        // Fetch slices, so that we know which axes should be discarded from the computation
        slices = MiscUtil.childrenOfType(this, SliceCoverageExpr.class);
    }

    @Override
    public CoverageInfo getCoverageInfo() {
        return info;
    }

    public boolean isScalarExpr()
    {
        return scalarExpr;
    }

    public ScalarExpr getScalarExpr()
    {
        ScalarExpr r = null;
        if (scalarExpr) {
            r = (ScalarExpr) child;
        }
        return r;
    }

    public IRasNode getChild() {
        return child;
    }

    @Override
    public String toRasQL() {
        if (simpleCoverage) {
            return childInfo;
        } else {
            return child.toRasQL();
        }
    }
    
    /**
     * Check if axisName is sliced from this coverage expression.
     * 
     * @param axisName axis name
     * @return true if axis is involved in a slice operation, false otherwise
     */
    public boolean slicedAxis(String axisName) {
        boolean ret = false;
        
        for (SliceCoverageExpr slice : slices) {
            if (slice.slicesDimension(axisName)) {
                ret = true;
                break;
            }
        }
        return ret;
    }
};
