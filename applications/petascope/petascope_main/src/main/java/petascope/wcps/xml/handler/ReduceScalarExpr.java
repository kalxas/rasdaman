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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;

/**
 * Class to translate reduce element from XML syntax to abstract syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// count( c.1 = 0 )
//<reduce>
//    <count>
//        <equals>
//            <fieldSelect>
//                <coverage>c</coverage>
//                <field>
//                    <name>1</name>
//                </field>
//            </fieldSelect>
//            <numericConstant>0</numericConstant>
//        </equals>
//    </count>
//</reduce>
public class ReduceScalarExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(ReduceScalarExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_ALL,
        WcpsConstants.MSG_SOME,
        WcpsConstants.MSG_COUNT,
        WcpsConstants.MSG_ADD,
        WcpsConstants.MSG_AVG,
        WcpsConstants.MSG_MIN,
        WcpsConstants.MSG_MAX,};

    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private CoverageExpr expr;
    private String operation;

    public ReduceScalarExpr(Node node, WCPSXmlQueryParsingService xq) throws WCPSException, SecoreException {
        log.trace(node.getNodeName());
        if (node.getNodeName().equals(WcpsConstants.MSG_REDUCE)) {
            node = node.getFirstChild();
        }
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        String nodeName = node.getNodeName();

        if (nodeName.equals(WcpsConstants.MSG_ALL)
                || nodeName.equals(WcpsConstants.MSG_SOME)
                || nodeName.equals(WcpsConstants.MSG_COUNT)
                || nodeName.equals(WcpsConstants.MSG_ADD)
                || nodeName.equals(WcpsConstants.MSG_AVG)
                || nodeName.equals(WcpsConstants.MSG_MIN)
                || nodeName.equals(WcpsConstants.MSG_MAX)) {
            operation = nodeName;
            log.trace("Reduce operation: " + operation);
            node = node.getFirstChild();
            expr = new CoverageExpr(node, xq);

            // Keep the child for XML tree re-traversing
            super.children.add(expr);

        } else {
            throw new WCPSException("invalid ReduceScalarExprType node: '" + nodeName + "'.");
        }
    }

    @Override
    public String toAbstractSyntax() {
        String result = operation + "(" + expr.toAbstractSyntax() + ")";

        return result;
    }
}
