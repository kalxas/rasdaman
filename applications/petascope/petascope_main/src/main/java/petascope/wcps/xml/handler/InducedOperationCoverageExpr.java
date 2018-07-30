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
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;

/**
 * Class to translate unary operation / binary operation from XML syntax to
 * abstrac syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InducedOperationCoverageExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(InducedOperationCoverageExpr.class);

    private IRasNode child;

    public InducedOperationCoverageExpr(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService) throws WCPSException, SecoreException {
        String nodeName = node.getNodeName();

        log.trace(nodeName);

        if (nodeName.equals(WcpsConstants.MSG_RANGE_CONSTRUCTOR)) {
            child = new RangeConstructorExpr(node, wcpsXMLQueryParsingService);
        } else {    // Try one of the groups
            child = null;

            if (UnaryOperationCoverageExpr.NODE_NAMES.contains(nodeName)) {
                try {
                    child = new UnaryOperationCoverageExpr(node, wcpsXMLQueryParsingService);
                    log.trace("induced Operation SUCCESS: " + node.getNodeName());
                } catch (WCPSException e) {
                    child = null;
                    if (e.getMessage().equals("Method not implemented")) {
                        throw e;
                    }
                    // range field subsetting exception
                    if (e.getExceptionCode().equals(ExceptionCode.NoSuchField)) {
                        throw e;
                    }
                }
            }

            if (BinaryOperationCoverageExpr.NODE_NAMES.contains(nodeName)) {
                try {
                    child = new BinaryOperationCoverageExpr(node, wcpsXMLQueryParsingService);
                    log.trace("Binary operation SUCCESS: " + node.getNodeName());
                } catch (WCPSException e) {
                    child = null;
                }
            }

            if (child == null) {
                throw new WCPSException("Invalid induced coverage expression, next node '" + node.getNodeName() + "'.");
            } else {
                // Keep the child to let the XML tree be traversed
                super.children.add(child);
            }
        }

    }

    @Override
    public String toAbstractSyntax() {
        String result = child.toAbstractSyntax();

        return result;
    }
}
