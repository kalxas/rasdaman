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
import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import static petascope.util.ras.RasConstants.*;

/**
 * Class to translate a boolean expression from XML syntax to abstract syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// (max(c[i($x:$x),j($y:$y)]) < 100)
//<booleanLessThan>
//    <reduce>
//        <max>
//            <trim>
//                <coverage>c</coverage>
//                <axis>i</axis>
//                <lowerBound>
//                    <variableRef>x</variableRef>
//                </lowerBound>
//                <upperBound>
//                    <variableRef>x</variableRef>
//                </upperBound>
//                <axis>j</axis>
//                <lowerBound>
//                    <variableRef>y</variableRef>
//                </lowerBound>
//                <upperBound>
//                    <variableRef>y</variableRef>
//                </upperBound>
//            </trim>
//        </max>
//    </reduce>
//    <numericConstant>100</numericConstant>
//</booleanLessThan>
public class BooleanScalarExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(BooleanScalarExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_BOOLEAN_CONSTANT,
        WcpsConstants.MSG_BOOLEAN_AND,
        WcpsConstants.MSG_BOOLEAN_OR,
        WcpsConstants.MSG_BOOLEAN_XOR,
        WcpsConstants.MSG_BOOLEAN_LESSTHAN,
        WcpsConstants.MSG_BOOLEAN_LESSOREQUAL,
        WcpsConstants.MSG_BOOLEAN_GREATERTHAN,
        WcpsConstants.MSG_BOOLEAN_GREATEROREQUAL,
        WcpsConstants.MSG_BOOLEAN_EQUALNUMERIC,
        WcpsConstants.MSG_BOOLEAN_NOTEQUALNUMERIC,
        WcpsConstants.MSG_BOOLEAN_EQUAL_STRING,
        WcpsConstants.MSG_BOOLEAN_NOT_EQUAL_STRING,
        WcpsConstants.MSG_BOOLEAN_NOT,
        WcpsConstants.MSG_BIT
    };

    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private IRasNode first, second;
    private String operation;
    private boolean simple;    // true if the expression is just a scalar value
    private String value;

    public BooleanScalarExpr(Node node, WCPSXmlQueryParsingService wcpsXmlQueryParsingService) throws WCPSException, SecoreException {

        String nodeName = node.getNodeName();
        log.debug("node name: " + nodeName);

        simple = false;

        if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_CONSTANT)) {
            simple = true;
            value = node.getFirstChild().getNodeValue();
        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_AND)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_OR)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_XOR)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSTHAN)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSOREQUAL)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATERTHAN)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATEROREQUAL)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUALNUMERIC)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOTEQUALNUMERIC)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUAL_STRING)
                || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOT_EQUAL_STRING)) {
            // Logical operations
            if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_AND)
                    || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_OR)
                    || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_XOR)) {
                // Remove the "boolean" in front
                operation = nodeName.substring(7).toLowerCase();

                Node child = node.getFirstChild();

                first = new BooleanScalarExpr(child, wcpsXmlQueryParsingService);
                child = child.getNextSibling();
                second = new BooleanScalarExpr(child, wcpsXmlQueryParsingService);
            } else { // Boolean Comparison operations between numbers or strings
                if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSTHAN) || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSOREQUAL)
                        || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATERTHAN) || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATEROREQUAL)
                        || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUALNUMERIC) || nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOTEQUALNUMERIC)) {
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSTHAN)) {
                        operation = "<";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_LESSOREQUAL)) {
                        operation = "<=";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATERTHAN)) {
                        operation = ">";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_GREATEROREQUAL)) {
                        operation = ">=";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUALNUMERIC)) {
                        operation = "=";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOTEQUALNUMERIC)) {
                        operation = "!=";
                    }

                    Node child = node.getFirstChild();

                    first = new NumericScalarExpr(child, wcpsXmlQueryParsingService);
                    child = child.getNextSibling();
                    second = new NumericScalarExpr(child, wcpsXmlQueryParsingService);
                } else {
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_EQUAL_STRING)) {
                        operation = "=";
                    }
                    if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOT_EQUAL_STRING)) {
                        operation = "!=";
                    }

                    Node child = node.getFirstChild();

                    first = new StringScalarExpr(child, wcpsXmlQueryParsingService);
                    child = child.getNextSibling();
                    second = new StringScalarExpr(child, wcpsXmlQueryParsingService);
                }
            }

        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BOOLEAN_NOT)) {
            operation = WcpsConstants.MSG_NOT;
            first = new BooleanScalarExpr(node.getFirstChild(), wcpsXmlQueryParsingService);
        } else if (nodeName.equalsIgnoreCase(WcpsConstants.MSG_BIT)) {
            operation = WcpsConstants.MSG_BIT;
            first = new CoverageExpr(node.getFirstChild(), wcpsXmlQueryParsingService);
            second = new NumericScalarExpr(node.getFirstChild().getNextSibling(), wcpsXmlQueryParsingService);
        } else {
            throw new WCPSException("Unexpected Binary Expression node '" + node.getNodeName() + "'.");
        }
        log.trace("Boolean Scalar Expr SUCCESS: " + node.getNodeName());

        // Keep children for XML tree crawling
        super.children.add(first);
        if (second != null) {
            super.children.add(second);    // "!" operation is unary
        }
    }

    @Override
    public String toAbstractSyntax() {
        
        String result = "";
        if (simple) {
            result = value;
        } else if (operation.equalsIgnoreCase(WcpsConstants.MSG_NOT)) {
            result = WcpsConstants.MSG_NOT + "( " + first.toAbstractSyntax() + " ) ";
        } else if (operation.equalsIgnoreCase(WcpsConstants.MSG_BIT)) {
            result = WcpsConstants.MSG_BIT;
            result = result + "( " + first.toAbstractSyntax() + ",";
            result = result + second.toAbstractSyntax() + " ) ";
        } else {
            result = "( ( " + first.toAbstractSyntax() + " ) " + operation;
            result = result + " ( " + second.toAbstractSyntax() + " ) )";
        }

        return result;
    }
}
