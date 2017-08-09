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
package petascope.wcps.xml.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;

public class SubsetOperationCoverageExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(SubsetOperationCoverageExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_TRIM,
        WcpsConstants.MSG_EXTEND,
        WcpsConstants.MSG_SLICE,};

    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private IRasNode child;

    public SubsetOperationCoverageExpr(Node node, WCPSXmlQueryParsingService xq) throws WCPSException, SecoreException {

        if (node == null) {
            throw new WCPSException("SubsetOperationCoverageExpr parsing error.");
        }

        String nodeName = node.getNodeName();
        log.trace(nodeName);

        if (nodeName.equals(WcpsConstants.MSG_TRIM)) {
            log.trace("  " + WcpsConstants.MSG_TRIM + " " + WcpsConstants.MSG_CHILD);
            child = new TrimCoverageExpr(node, xq);
        } else if (nodeName.equals(WcpsConstants.MSG_EXTEND)) {
            log.trace("  " + WcpsConstants.MSG_EXTEND + " " + WcpsConstants.MSG_CHILD);
            child = new ExtendCoverageExpr(node, xq);
        } else if (nodeName.equals(WcpsConstants.MSG_SLICE)) {
            log.trace("  " + WcpsConstants.MSG_SLICE + " " + WcpsConstants.MSG_CHILD);
            child = new SliceCoverageExpr(node, xq);
        } else {
            log.error("Failed to match SubsetOperation: " + nodeName);
            throw new WCPSException("failed to match SubsetOperation: '" + nodeName + "'.");
        }

        // Keep the children to re-traverse the XML tree
        if (child != null) {
            super.children.add(child);
        }
    }

    @Override
    public String toAbstractSyntax() {
        String result = child.toAbstractSyntax();
        
        return result;
    }

    public IRasNode getChild() {
        return child;
    }

}
