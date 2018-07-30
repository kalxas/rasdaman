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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.xml.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;

public class ScalarExpr extends AbstractRasNode {

    private final static Logger log = LoggerFactory.getLogger(ScalarExpr.class);

    private IRasNode child;

    public ScalarExpr(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService) throws WCPSException, SecoreException {

        String nodeName = node.getNodeName();
        Node childNode = node;
        String childNodeName = childNode.getNodeName();

        // Try one of the groups
        child = null;

//      MetadataScalarExprType
        if (child == null) {
            if (MetadataScalarExpr.NODE_NAMES.contains(nodeName)) {
                child = new MetadataScalarExpr(node, wcpsXMLQueryParsingService);
                log.trace("Matched metadata scalar expression.");
            }
        }

//      BooleanScalarExprType
        if (child == null) {
            if (BooleanScalarExpr.NODE_NAMES.contains(nodeName)) {
                child = new BooleanScalarExpr(node, wcpsXMLQueryParsingService);
                log.trace("Matched boolean scalar expression.");
            }
        }

//      NumericScalarExprType
        if (child == null) {
            if (NumericScalarExpr.NODE_NAMES.contains(childNodeName)) {
                child = new NumericScalarExpr(node, wcpsXMLQueryParsingService);
                log.trace("Matched numeric scalar expression.");
            }
        }

//      ReduceScalarExprType
        if (child == null) {
            if (node.getNodeName().equals(WcpsConstants.MSG_REDUCE)) {
                childNode = node.getFirstChild();
            }
            String childNodeTmp = childNode.getNodeName();
            if (ReduceScalarExpr.NODE_NAMES.contains(childNodeTmp)) {
                child = new ReduceScalarExpr(node, wcpsXMLQueryParsingService);
                log.trace("Matched reduce scalar expression.");
            }
        }

//      StringScalarExprType
        if (child == null) {
            if (StringScalarExpr.NODE_NAMES.contains(childNodeName)) {
                child = new StringScalarExpr(node, wcpsXMLQueryParsingService);
                log.trace("Matched string scalar expression.");
            }
        }

        // Error check
        if (child == null) {
            log.error("Invalid coverage Expression, next node: " + node.getNodeName());
            throw new WCPSException("Invalid coverage Expression, next node: '" + node.getNodeName() + "'.");
        } else {
            // Add it to the children for XML tree re-traversing
            super.children.add(child);
        }

    }

    @Override
    public String toAbstractSyntax() {
        String result = child.toAbstractSyntax();

        return result;
    }
}
