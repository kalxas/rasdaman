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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.Pair;
import petascope.util.WcpsConstants;

public class SubsetOperationCoverageExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(SubsetOperationCoverageExpr.class);
    
    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_TRIM,
        WcpsConstants.MSG_EXTEND,
        WcpsConstants.MSG_SLICE,
    };
    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private IRasNode child;
    private CoverageInfo info = null;

    public SubsetOperationCoverageExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {

        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        if (node == null) {
            throw new WCPSException("SubsetOperationCoverageExpr parsing error.");
        }

        String nodeName = node.getNodeName();
        log.trace(nodeName);

        if (nodeName.equals(WcpsConstants.MSG_TRIM)) {
            log.trace("  " + WcpsConstants.MSG_TRIM + " " + WcpsConstants.MSG_CHILD);
            child = new TrimCoverageExpr(node, xq);
            info = ((TrimCoverageExpr) child).getCoverageInfo();
        } else if (nodeName.equals(WcpsConstants.MSG_EXTEND)) {
            log.trace("  " + WcpsConstants.MSG_EXTEND + " " + WcpsConstants.MSG_CHILD);
            child = new ExtendCoverageExpr(node, xq);
            info = ((ExtendCoverageExpr) child).getCoverageInfo();
        } else if (nodeName.equals(WcpsConstants.MSG_SLICE)) {
            log.trace("  " + WcpsConstants.MSG_SLICE + " " + WcpsConstants.MSG_CHILD);
            try {
                child = new SliceCoverageExpr(node, xq);
            } catch (WCPSException ex) {
                throw ex;
            } 
            info = ((SliceCoverageExpr) child).getCoverageInfo();
        } else {
            log.error("Failed to match SubsetOperation: " + nodeName);
            throw new WCPSException("failed to match SubsetOperation: " + nodeName);
        }
        
        // Keep the children to re-traverse the XML tree
        if (child != null) super.children.add(child);
    }

    @Override
    public String toRasQL() {
        return child.toRasQL();
    }
    
    Pair<String[], String> computeRasQL() {
        if (child instanceof TrimCoverageExpr) {
            return ((TrimCoverageExpr) child).computeRasQL();
        } else if (child instanceof SliceCoverageExpr) {
            return ((SliceCoverageExpr) child).computeRasQL();
        } else {
            return Pair.of(null, child.toRasQL());
        }
    }
    
    public IRasNode getChild() {
        return child;
    }
    
    @Override
    public CoverageInfo getCoverageInfo() {
        return info;
    }
}
