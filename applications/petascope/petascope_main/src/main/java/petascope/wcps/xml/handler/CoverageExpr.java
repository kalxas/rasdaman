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

import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;

/**
 * Class to translate unary operation from XML syntax to abstract syntax
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
// sin((c.1)[i(100:200), j(100:200)])
//<sin>
//    <trim>
//        <fieldSelect>
//            <coverage>c</coverage>
//            <field>
//                <name>1</name>
//            </field>
//        </fieldSelect>
//        <axis>i</axis>
//        <lowerBound>
//            <numericConstant>100</numericConstant>
//        </lowerBound>
//        <upperBound>
//            <numericConstant>200</numericConstant>
//        </upperBound>
//        <axis>j</axis>
//        <lowerBound>
//            <numericConstant>100</numericConstant>
//        </lowerBound>
//        <upperBound>
//            <numericConstant>200</numericConstant>
//        </upperBound>
//    </trim>
//</sin>
public class CoverageExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(CoverageExpr.class);

    private IRasNode childNode;
    // The translated content of CoverageExpr, e.g: c[i(0:20)]
    private String childInfo;
    private boolean scalarExpr = false;
    // True if the coverage is just a string, e.g:
    // <coverage>c</coverage>
    private boolean simpleCoverage;
    // Store the exception message for throwing exception
    private String exceptionMessage = "";

    public CoverageExpr(Node node, WCPSXmlQueryParsingService xmlQueryParsingService) throws WCPSException, SecoreException {

        if (node == null) {
            throw new WCPSException("CoverageExprType parsing error.");
        }

        String nodeName = node.getNodeName();
        log.trace(nodeName);

        simpleCoverage = false;

        if (nodeName.equals(WcpsConstants.MSG_COVERAGE)) {
            // e.g: c
//          <coverage>c</coverage>
            simpleCoverage = true;
            childInfo = node.getFirstChild().getNodeValue();

            if (!xmlQueryParsingService.isIteratorDefined(childInfo)) {
                throw new WCPSException(WcpsConstants.MSG_ITERATOR + " '" + childInfo + "' not defined");
            }
        } else if (nodeName.equals(WcpsConstants.MSG_CRS_TRANSFORM)) {
            childNode = new CrsTransformCoverageExpr(node, xmlQueryParsingService);
        } else if (nodeName.equals(WcpsConstants.MSG_SCALE)) {
            childNode = new ScaleCoverageExpr(node, xmlQueryParsingService);
        } else if (nodeName.equals(WcpsConstants.MSG_CONSTRUCT)) {
            childNode = new ConstructCoverageExpr(node.getFirstChild(), xmlQueryParsingService);
        } else if (nodeName.equals(WcpsConstants.MSG_CONST)) {
            childNode = new ConstantCoverageExpr(node.getFirstChild(), xmlQueryParsingService);
        } else if (nodeName.equals(WcpsConstants.MSG_SWITCH)) {
            childNode = new SwitchCaseExpr(node, xmlQueryParsingService);
        } //        else if (nodeName.equals("variableRef"))
        //        {
        //            child = new VariableReference(node, xq);
        //        }
        else {    // Try one of the groups
            childNode = null;
            String firstMessage = "";

            Node childNode = node;
            String childNodeName = childNode.getNodeName();

            // TODO: not implemented
//            if (child == null) {
//                try {
//                    child = new SetMetadataCoverageExpr(node, xq);
//                    log.trace("  " + WcpsConstants.MSG_MATCHED_SET_METADATA);
//                } catch (WCPSException e) {
//                    child = null;
//                    exMessage = e.getMessage();
//                    firstMessage = exMessage;
//                }
//            }
            if (this.childNode == null) {
                // rangeConstructor element
                if (childNodeName.equals(WcpsConstants.MSG_RANGE_CONSTRUCTOR)
                        || UnaryOperationCoverageExpr.NODE_NAMES.contains(childNodeName)
                        || BinaryOperationCoverageExpr.NODE_NAMES.contains(nodeName)) {
                    try {
                        this.childNode = new InducedOperationCoverageExpr(node, xmlQueryParsingService);
                        log.trace("Matched induced coverage expression operation.");
                    } catch (WCPSException e) {
                        this.childNode = null;
                        if (e.getMessage().equals("Method not implemented")) {
                            throw e;
                        }
                        // range field subsetting exception
                        if (e.getExceptionCode().equals(ExceptionCode.NoSuchField)) {
                            throw e;
                        }
                    }
                }
            }

            if (this.childNode == null) {
                // trim, slice elements
                if (SubsetOperationCoverageExpr.NODE_NAMES.contains(childNodeName)) {
                    try {
                        this.childNode = new SubsetOperationCoverageExpr(node, xmlQueryParsingService);
                        log.trace("Matched subset operation.");
                    } catch (WCPSException e) {
                        if (e.getExceptionCode().equals(ExceptionCode.MissingCRS)
                                || e.getExceptionCode().equals(ExceptionCode.InvalidSubsetting)) {
                            throw (e);
                        }
                        this.childNode = null;
                        exceptionMessage = exceptionMessage.equals(firstMessage) ? e.getMessage() : exceptionMessage;
                    }
                }
            }

            if (this.childNode == null) {
                // scalar value
                try {
                    this.childNode = new ScalarExpr(node, xmlQueryParsingService);
                    this.scalarExpr = true;
                    log.trace("Matched scalar expression.");
                } catch (WCPSException e) {
                    this.childNode = null;
                    exceptionMessage = exceptionMessage.equals(firstMessage) ? e.getMessage() : exceptionMessage;
                }
            }
        }

        if (!simpleCoverage && (childNode == null)) {
            throw new WCPSException("Invalid coverage Expression, next node '"
                    + node.getNodeName() + "' - " + exceptionMessage);
        }

        if (childNode != null) {
            // Keep child for XML tree crawling
            super.children.add(childNode);
        }
    }

    public boolean isScalarExpr() {
        return scalarExpr;
    }

    public ScalarExpr getScalarExpr() {
        ScalarExpr r = null;
        if (scalarExpr) {
            r = (ScalarExpr) childNode;
        }
        return r;
    }

    public IRasNode getChild() {
        return childNode;
    }

    @Override
    public String toAbstractSyntax() {
        String result = "";
        if (simpleCoverage) {
            result = childInfo;            
        } else {
            result = childNode.toAbstractSyntax();            
        }
        return result;
        
    }
}
