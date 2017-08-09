package petascope.wcps.xml.handler;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.ListUtil;

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
/**
 * Class for translating the switch expression from XML to abstract syntax.
 *
 * @author Vlad Merticariu <vlad@flanche.net>
 */
//switch case c>20 return (char)2
//       case c>10 return (char)1
//       default return (char)0
//<switch>
//    <case>
//        <condition>
//            <greaterThan>
//                <coverage>c</coverage>
//                <numericConstant>20</numericConstant>
//            </greaterThan>
//        </condition>
//        <result>
//            <cast>
//                <numericConstant>2</numericConstant>
//                <type>char</type>
//            </cast>
//        </result>
//    </case>
//    <case>
//        <condition>
//            <greaterThan>
//                <coverage>c</coverage>
//                <numericConstant>10</numericConstant>
//            </greaterThan>
//        </condition>
//        <result>
//            <cast>
//                <numericConstant>1</numericConstant>
//                <type>char</type>
//            </cast>
//        </result>
//    </case>
//    <default>
//        <result>
//            <cast>
//                <numericConstant>0</numericConstant>
//                <type>char</type>
//            </cast>
//        </result>
//    </default>
//</switch>
public class SwitchCaseExpr extends AbstractRasNode {

    private static final Logger log = LoggerFactory.getLogger(SwitchCaseExpr.class);

    private List<CoverageExpr> coverageExprs = new ArrayList<>();

    public SwitchCaseExpr(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService) throws WCPSException, SecoreException {
        int defaultOk = 0;
        String nodeName = node.getNodeName();
        log.debug("node name: " + nodeName);

        if (nodeName.equals(WcpsConstants.MSG_SWITCH)) {
            NodeList childNodes = node.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node currentNode = childNodes.item(i);
                if (currentNode.getNodeName().equals(WcpsConstants.MSG_CASE)) {
                    //get the condition and the result elements
                    NodeList caseChildren = currentNode.getChildNodes();

                    for (int j = 0; j < caseChildren.getLength(); j++) {
                        Node caseNode = caseChildren.item(j);
                        if (caseNode.getNodeName().equals(WcpsConstants.MSG_CONDITION)
                                || caseNode.getNodeName().equals(WcpsConstants.MSG_RESULT)) {
                            CoverageExpr coverageExpr = new CoverageExpr(caseNode.getFirstChild(), wcpsXMLQueryParsingService);
                            coverageExprs.add(coverageExpr);
                        } else {
                            throw new WCPSException(WcpsConstants.ERRTXT_UNEXPETCTED_NODE + ": '" + caseNode + "'.");
                        }
                    }
                } else if (currentNode.getNodeName().equals(WcpsConstants.MSG_DEFAULT)) {
                    //now get the default result
                    defaultOk = 1;
                    NodeList defChildren = currentNode.getChildNodes();
                    for (int j = 0; j < defChildren.getLength(); j++) {
                        Node defaultNode = defChildren.item(j);
                        if (defaultNode.getNodeName().equals(WcpsConstants.MSG_RESULT)) {
                            CoverageExpr coverageExpr = new CoverageExpr(defaultNode.getFirstChild(), wcpsXMLQueryParsingService);
                            coverageExprs.add(coverageExpr);
                        } else {
                            throw new WCPSException(WcpsConstants.ERRTXT_UNEXPETCTED_NODE + ": '" + defaultNode + "'.");
                        }
                    }
                }
            }
            if (defaultOk == 0) {
                throw new WCPSException(WcpsConstants.ERRTXT_MISSING_SWITCH_DEFAULT);
            }
        } else {
            throw new WCPSException(WcpsConstants.ERRTXT_UNEXPETCTED_NODE + ": '" + nodeName  + "'.");
        }
    }

    @Override
    public String toAbstractSyntax() {
        // switch case c>20 return (char)2 case c>10 return (char)1 default return (char)0
        String result = " " + WcpsConstants.MSG_SWITCH + " ";
        List<String> abstractSwitchCases = new ArrayList<>();
        
        // case ... return ...
        for (int i = 0; i < coverageExprs.size() - 1; i = i + 2) {
            String abstractConditionCase = " " + WcpsConstants.MSG_CASE + " " + coverageExprs.get(i).toAbstractSyntax();
            String abstractResultCase = " " + WcpsConstants.MSG_RETURN + " " + coverageExprs.get(i + 1).toAbstractSyntax();
            abstractSwitchCases.add(abstractConditionCase + " " + abstractResultCase);
        }
        
        result = result + " " + ListUtil.join(abstractSwitchCases, " ");
        
        // default return ...
        String defaultCase = " " + WcpsConstants.MSG_DEFAULT + " " + WcpsConstants.MSG_RETURN;
        defaultCase = defaultCase + " " + coverageExprs.get(coverageExprs.size() - 1).toAbstractSyntax();
        result = result + " " + defaultCase;
        
        return result;
    }
}
