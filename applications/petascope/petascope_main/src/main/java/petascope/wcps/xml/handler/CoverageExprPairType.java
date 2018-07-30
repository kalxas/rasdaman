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

import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;

public class CoverageExprPairType extends AbstractRasNode {

    private IRasNode first, second;
    private boolean ok = false;

    public CoverageExprPairType(Node node, WCPSXmlQueryParsingService xq) throws WCPSException, SecoreException {
        String nodeName = node.getNodeName();

        try {
            first = new ScalarExpr(node, xq);
        } catch (WCPSException ex) {
            first = new CoverageExpr(node, xq);
        }

        node = node.getNextSibling();
        try {
            second = new ScalarExpr(node, xq);
        } catch (WCPSException ex) {
            second = new CoverageExpr(node, xq);
        }
    }

    @Override
    public String toAbstractSyntax() {
        if (ok == true) {
            return first.toAbstractSyntax() + second.toAbstractSyntax();
        } else {
            return " '" + WcpsConstants.MSG_ERROR + "' ";
        }
    }

    public IRasNode getFirst() {
        return first;
    }

    public IRasNode getSecond() {
        return second;
    }
}
