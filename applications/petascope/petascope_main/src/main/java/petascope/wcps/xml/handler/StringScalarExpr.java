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

public class StringScalarExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(StringScalarExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_STRING_IDENTIFIER,
        WcpsConstants.MSG_STRING_CONSTANT
    };
    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private String operation, value;
    private CoverageExpr cov;

    public StringScalarExpr(Node node, WCPSXmlQueryParsingService xq) throws WCPSException, SecoreException {        
        log.trace(node.getNodeName());

        if (node.getNodeName().equalsIgnoreCase(WcpsConstants.MSG_STRING_IDENTIFIER)) {
            Node child = node.getFirstChild();
            cov = new CoverageExpr(child, xq);
            super.children.add(cov);
            operation = WcpsConstants.MSG_ID_LOWERCASE;
        } else if (node.getNodeName().equalsIgnoreCase(WcpsConstants.MSG_STRING_CONSTANT)) {
            operation = WcpsConstants.MSG_CONSTANT;
            value = node.getFirstChild().getNodeValue();
        } else {
            throw new WCPSException("Unknown String expr node: '" + node.getNodeName() + "'.");
        }

        log.trace("  " + WcpsConstants.MSG_OPERATION + ": " + operation + ", " + WcpsConstants.MSG_VALUE + ": " + value);
    }

    @Override
    public String toAbstractSyntax() {
        String result = "";
        if (operation.equalsIgnoreCase(WcpsConstants.MSG_CONSTANT)) {
            result = value;
        }
        if (operation.equalsIgnoreCase(WcpsConstants.MSG_ID_LOWERCASE)) {
            result = cov.toAbstractSyntax();
        }

        return result;
    }

    // Equivalent of NumericScalarExpr::getSingleValue() for String subset expressions (e.g. timestamps)
    public String getValue() {
        if (operation.equalsIgnoreCase(WcpsConstants.MSG_CONSTANT)) {
            return value;
        } else {
            return "";
        }
    }

    public boolean isSingleValue() {
        return operation.equalsIgnoreCase(WcpsConstants.MSG_CONSTANT);
    }
}
