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
import static petascope.util.ras.RasConstants.*;

public class UnaryOperationCoverageExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(UnaryOperationCoverageExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_UNARY_PLUS, WcpsConstants.MSG_UNARY_MINUS,
        WcpsConstants.MSG_SQRT, WcpsConstants.MSG_ABS,
        WcpsConstants.MSG_EXP, WcpsConstants.MSG_LOG,
        WcpsConstants.MSG_SIN, WcpsConstants.MSG_COS,
        WcpsConstants.MSG_SINH, WcpsConstants.MSG_COSH,
        WcpsConstants.MSG_TANH, WcpsConstants.MSG_ARCSIN,
        WcpsConstants.MSG_ARCCOS, WcpsConstants.MSG_ARCTAN,
        WcpsConstants.MSG_NOT, WcpsConstants.MSG_RE,
        WcpsConstants.MSG_LN, WcpsConstants.MSG_BIT,
        WcpsConstants.MSG_TAN, WcpsConstants.MSG_CAST,
        WcpsConstants.MSG_IM, WcpsConstants.MSG_FIELD_SELECT,};

    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private CoverageExpr childNode;
    private String operation;
    private String params;

    public UnaryOperationCoverageExpr(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService) throws WCPSException, SecoreException {
        String nodeName = node.getNodeName();
        log.trace(nodeName);

        if (nodeName.equals(WcpsConstants.MSG_UNARY_PLUS)) {
            operation = WcpsConstants.MSG_PLUS;
            childNode = new CoverageExpr(node.getFirstChild(), wcpsXMLQueryParsingService);
        } else if (nodeName.equals(WcpsConstants.MSG_UNARY_MINUS)) {
            operation = WcpsConstants.MSG_MINUS;
            childNode = new CoverageExpr(node.getFirstChild(), wcpsXMLQueryParsingService);
        } else if (nodeName.equals(WcpsConstants.MSG_SQRT) || nodeName.equals(WcpsConstants.MSG_ABS)
                || nodeName.equals(WcpsConstants.MSG_EXP) || nodeName.equals(WcpsConstants.MSG_LOG) || nodeName.equals(WcpsConstants.MSG_LN)
                || nodeName.equals(WcpsConstants.MSG_SIN) || nodeName.equals(WcpsConstants.MSG_COS) || nodeName.equals(WcpsConstants.MSG_TAN)
                || nodeName.equals(WcpsConstants.MSG_SINH) || nodeName.equals(WcpsConstants.MSG_COSH)
                || nodeName.equals(WcpsConstants.MSG_TANH) || nodeName.equals(WcpsConstants.MSG_ARCSIN)
                || nodeName.equals(WcpsConstants.MSG_ARCCOS) || nodeName.equals(WcpsConstants.MSG_ARCTAN)
                || nodeName.equals(WcpsConstants.MSG_NOT) || nodeName.equals(WcpsConstants.MSG_RE) || nodeName.equals(WcpsConstants.MSG_IM)) {
            operation = nodeName;
            childNode = new CoverageExpr(node.getFirstChild(), wcpsXMLQueryParsingService);
        } else if (nodeName.equals(WcpsConstants.MSG_BIT)) {
            // e.g: bit(c, 1)
            operation = WcpsConstants.MSG_BIT;
            Node bitNode = node.getFirstChild();            
            if (bitNode.getNodeName().equals(WcpsConstants.MSG_BITINDEX)) {
                try {
                    params = bitNode.getFirstChild().getNodeValue();
                    log.trace("Found bitIndex = " + params);
                } catch (NumberFormatException e) {
                    throw new WCPSException("Invalid Number as bitIndex: '" + params + "'.");
                }
            } else {
                childNode = new CoverageExpr(bitNode, wcpsXMLQueryParsingService);
            }

            // params
            bitNode = bitNode.getNextSibling();
            CoverageExpr coverageExprTmp = new CoverageExpr(bitNode, wcpsXMLQueryParsingService);
            params = coverageExprTmp.toAbstractSyntax();
            
            
        } else if (nodeName.equals(WcpsConstants.MSG_CAST)) {
            // e.g (unsigned char) (c)
            operation = WcpsConstants.MSG_CAST;
            Node castNode = node.getFirstChild();

            while (castNode != null) {
                log.trace("  " + WcpsConstants.MSG_CHILD + " " + WcpsConstants.MSG_NAME + ": " + castNode.getNodeName());

                if (castNode.getNodeName().equals(WcpsConstants.MSG_TYPE)) {
                    CastType rangeFieldType = new CastType(castNode, wcpsXMLQueryParsingService);
                    params = rangeFieldType.toAbstractSyntax();
                } else {
                    childNode = new CoverageExpr(castNode, wcpsXMLQueryParsingService);
                }

                castNode = castNode.getNextSibling();
            }
        } else if (nodeName.equals(WcpsConstants.MSG_FIELD_SELECT)) {
            operation = WcpsConstants.MSG_SELECT;
            Node fieldSelectNode = node.getFirstChild();

            while (fieldSelectNode != null) {
                if (fieldSelectNode.getNodeName().equals(WcpsConstants.MSG_FIELD)) {
                    Field field = new Field(fieldSelectNode.getFirstChild(), wcpsXMLQueryParsingService);
                    params = field.toAbstractSyntax();
                } else {
                    childNode = new CoverageExpr(fieldSelectNode, wcpsXMLQueryParsingService);
                }

                fieldSelectNode = fieldSelectNode.getNextSibling();
            }
        } else {
            throw new WCPSException("Unknown unary operation: '" + nodeName + "'.");
        }

        log.trace("  " + WcpsConstants.MSG_OPERATION + ": " + operation);

        // Add children to let the XML query be re-traversed
        if (childNode != null) {
            super.children.add(childNode);
        }
    }

    @Override
    public String toAbstractSyntax() {
        switch (operation) {
            case WcpsConstants.MSG_SQRT:
            case WcpsConstants.MSG_ABS:
            case WcpsConstants.MSG_EXP:
            case WcpsConstants.MSG_LOG:
            case WcpsConstants.MSG_LN:
            case WcpsConstants.MSG_SIN:
            case WcpsConstants.MSG_COS:
            case WcpsConstants.MSG_TAN:
            case WcpsConstants.MSG_TANH:
            case WcpsConstants.MSG_ARCSIN:
            case WcpsConstants.MSG_ARCCOS:
            case WcpsConstants.MSG_ARCTAN:
            case WcpsConstants.MSG_NOT:
            case WcpsConstants.MSG_PLUS:
            case WcpsConstants.MSG_MINUS:
                return operation + " ( " + childNode.toAbstractSyntax() + " )";
            case WcpsConstants.MSG_CAST:
                return "(" + params + ")( " + childNode.toAbstractSyntax() + " )";
            case WcpsConstants.MSG_SELECT:
                return "(" + childNode.toAbstractSyntax() + ")." + params;
            case WcpsConstants.MSG_BIT:
                return WcpsConstants.MSG_BIT + " ( " + childNode.toAbstractSyntax() + "," + params + " )";
            default:
                throw new WCPSException("Unary operation: '" + operation + "' is not supported.");
        }
    }
}
