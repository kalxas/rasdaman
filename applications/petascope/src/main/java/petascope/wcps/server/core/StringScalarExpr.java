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
import petascope.util.WcpsConstants;

public class StringScalarExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(StringScalarExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_STRING_IDENTIFIER,
        WcpsConstants.MSG_STRING_CONSTANT,
    };
    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private String op, string;
    private CoverageExpr cov;

    public StringScalarExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        while ((node != null) && (node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT))) {
            node = node.getNextSibling();
        }
        log.trace(node.getNodeName());

        if (node.getNodeName().equals(WcpsConstants.MSG_STRING_IDENTIFIER)) {
            Node child = node.getFirstChild();
            cov = new CoverageExpr(child, xq);
            super.children.add(cov);
            op = WcpsConstants.MSG_ID_LOWERCASE;
        } else if (node.getNodeName().equals(WcpsConstants.MSG_STRING_CONSTANT)) {
            op = WcpsConstants.MSG_CONSTANT;
            string = node.getFirstChild().getNodeValue();
        } else {
            throw new WCPSException("Unknown String expr node: " + node.getNodeName());
        }

        log.trace("  " + WcpsConstants.MSG_OPERATION + ": " + op + ", " + WcpsConstants.MSG_VALUE + ": " + string);
    }

    public String toRasQL() {
        String result = "";
        if (op.equals(WcpsConstants.MSG_CONSTANT)) {
            result = string;
        }
        if (op.equals(WcpsConstants.MSG_ID_LOWERCASE)) {
            result = cov.toRasQL();
        }

        return result;
    }

    // Equivalent of NumericScalarExpr::getSingleValue() for String subset expressions (e.g. timestamps)
    public String getValue() {
        if (op.equals(WcpsConstants.MSG_CONSTANT)) {
            return string;
        } else return "";
    }

    public boolean isSingleValue() {
        return op.equals(WcpsConstants.MSG_CONSTANT);
    }
}
