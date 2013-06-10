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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.WCPSConstants;

// TODO: implement class MetadataScalarExprType
public class MetadataScalarExpr extends AbstractRasNode {
    
    private static Logger log = LoggerFactory.getLogger(MetadataScalarExpr.class);
    
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo;
    private String op;
    private String lo, hi;

    public MetadataScalarExpr(Node node, XmlQuery xq) throws WCPSException {
        String nodeName = node.getNodeName();
        log.trace(nodeName);
        
        Node child = node.getFirstChild();
        while (child != null && child.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
            child = child.getNextSibling();
        }
        
        // the first argument is always a coverage expression
        coverageExprType = new CoverageExpr(child, xq);
        coverageInfo = coverageExprType.getCoverageInfo();
        super.children.add(coverageExprType);
        child = child.getNextSibling();
        
        op = nodeName;
        AxisName axis = null;
        if (nodeName.equals(WCPSConstants.MSG_DOMAIN_METADATA_CAMEL)) {
            axis = new AxisName(child, xq);            
            int axisIndex = coverageInfo.getDomainIndexByName(axis.toRasQL());
            DomainElement domainElement = coverageInfo.getDomainElement(axisIndex);
            lo = domainElement.getMinValue().toString();
            hi = domainElement.getMaxValue().toString();
        } else if (nodeName.equals(WCPSConstants.MSG_IMAGE_CRSDOMAIN)) {
            axis = new AxisName(child, xq);
            int axisIndex = coverageInfo.getDomainIndexByName(axis.toRasQL());
            CellDomainElement cellDomain = coverageInfo.getCellDomainElement(axisIndex);
            lo = cellDomain.getLo().toString();
            hi = cellDomain.getHi().toString();
        } else if (!nodeName.equals(WCPSConstants.MSG_SET_IDENTIFIER ) && 
                   !nodeName.equals(WCPSConstants.MSG_IMAGE_CRS2)) {
            throw new WCPSException(WCPSConstants.ERRTXT_NO_METADATA_NODE + nodeName);
        }
        
        // Store the child for XML tree re-traversing
        if (axis != null) super.children.add(axis);
    }

    public String toRasQL() {
        String ret = "";
        if (op.equals(WCPSConstants.MSG_IDENTIFIER)) {
            ret = coverageInfo.getCoverageName();
        } else if (op.equals(WCPSConstants.MSG_IMAGE_CRS)) {
            ret = CrsUtil.GRID_CRS;
        } else if (op.equals(WCPSConstants.MSG_DOMAIN_METADATA_CAMEL) || op.equals(WCPSConstants.MSG_IMAGE_CRSDOMAIN)) {
            ret = "(" + lo + "," + hi + ")";
        }
        return ret;
    }
}
