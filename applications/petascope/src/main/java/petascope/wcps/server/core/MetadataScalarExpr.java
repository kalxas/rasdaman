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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.WcpsConstants;

// TODO: implement class MetadataScalarExprType
public class MetadataScalarExpr extends AbstractRasNode {
    
    private static Logger log = LoggerFactory.getLogger(MetadataScalarExpr.class);
    
    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_DOMAIN_METADATA_CAMEL,
        WcpsConstants.MSG_IMAGE_CRSDOMAIN,
        WcpsConstants.MSG_CRS_SET,
        WcpsConstants.MSG_IDENTIFIER,
    };
    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }
    
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo;
    private String op;
    private String lo, hi;
    private String crss = "";

    public MetadataScalarExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        String nodeName = node.getNodeName();
        log.trace(nodeName);
        
        Node child = node.getFirstChild();
        while (child != null && child.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            child = child.getNextSibling();
        }
        
        // the first argument is always a coverage expression
        coverageExprType = new CoverageExpr(child, xq);
        coverageInfo = coverageExprType.getCoverageInfo();
        super.children.add(coverageExprType);
        child = child.getNextSibling();
        
        op = nodeName;
        AxisName axis = null;
        if (nodeName.equals(WcpsConstants.MSG_DOMAIN_METADATA_CAMEL)) {
            axis = new AxisName(child, xq);            
            int axisIndex = coverageInfo.getDomainIndexByName(axis.toRasQL());
            DomainElement domainElement = coverageInfo.getDomainElement(axisIndex);
            lo = domainElement.getMinValue().toString();
            hi = domainElement.getMaxValue().toString();
        } else if (nodeName.equals(WcpsConstants.MSG_IMAGE_CRSDOMAIN)) {
            axis = new AxisName(child, xq);
            int axisIndex = coverageInfo.getDomainIndexByName(axis.toRasQL());
            CellDomainElement cellDomain = coverageInfo.getCellDomainElement(axisIndex);
            lo = cellDomain.getLo().toString();
            hi = cellDomain.getHi().toString();
        } else if (nodeName.equals(WcpsConstants.MSG_CRS_SET)){
            int n = coverageInfo.getNumDimensions();
            for(int i=0; i<n;i++){
                DomainElement domainElement = coverageInfo.getDomainElement(i);
                String axName = domainElement.getLabel();
                crss+=axName + ":";
                Set<String> set = new HashSet<String>();
                set.add(domainElement.getCrs());
                for(String str:set){
                    crss+=(str + " ");
                }
                //delete last space
                crss = crss.substring(0,crss.length()-1);
                if(i+1!=n) // eliminate possibility of trailing commas
                    crss+=", ";
            }
        }
        else if (nodeName.equals(WcpsConstants.MSG_IDENTIFIER )) {
            op = WcpsConstants.MSG_IDENTIFIER;
        }
        else if (!nodeName.equals(WcpsConstants.MSG_IMAGE_CRS2) && 
                   !nodeName.equals(WcpsConstants.MSG_SET_IDENTIFIER) &&
                   !nodeName.equals(WcpsConstants.MSG_IMAGE_CRS)) {
            throw new WCPSException("No metadata node: " + nodeName);
        }
        
        // Store the child for XML tree re-traversing
        if (axis != null) super.children.add(axis);
    }

    public String toRasQL() {
        String ret = "";
        if (op.equals(WcpsConstants.MSG_IDENTIFIER)) {
            ret = coverageInfo.getCoverageName();
        } else if (op.equals(WcpsConstants.MSG_IMAGE_CRS)) {
            ret = CrsUtil.GRID_CRS;
        } else if (op.equals(WcpsConstants.MSG_DOMAIN_METADATA_CAMEL) || op.equals(WcpsConstants.MSG_IMAGE_CRSDOMAIN)) {
            ret = "(" + lo + "," + hi + ")";
        }
        return ret;
    }
}
