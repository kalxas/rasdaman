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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.ListUtil;

/**
 * Translate a constant coverage in XML syntax to abstract syntax
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
//coverage const 
//over $px x(0:1) 
//value list <0;1>
//<const>
//    <name>const</name>
//    <axisIterator>
//        <iteratorVar>px</iteratorVar>
//        <axis>x</axis>
//        <numericConstant>0</numericConstant>
//        <numericConstant>1</numericConstant>
//    </axisIterator>
//    <value>0</value>
//    <value>1</value>
//</const>
public class ConstantCoverageExpr extends AbstractRasNode {

    private static final Logger log = LoggerFactory.getLogger(ConstantCoverageExpr.class);

    private String coverageName;
    private List<AxisIterator> axisIterators = new ArrayList<>();
    private ConstantList valueList;

    public ConstantCoverageExpr(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService)
            throws WCPSException, SecoreException {

        while (node != null) {
            String nodeName = node.getNodeName();
            log.trace("node name: " + nodeName);

            if (nodeName.equals(WcpsConstants.MSG_NAME)) {
                coverageName = node.getTextContent();
                log.trace("  " + WcpsConstants.MSG_COVERAGE + " " + coverageName);
            } else if (nodeName.equals(WcpsConstants.MSG_AXIS_ITERATOR)) {
                log.trace("over: add axis iterator.");
                AxisIterator axisIterator = new AxisIterator(node.getFirstChild(), wcpsXMLQueryParsingService);
                axisIterators.add(axisIterator);
            } else {
                log.trace("value list");
                valueList = new ConstantList(node, wcpsXMLQueryParsingService);
                node = valueList.getLastNode();
                super.children.add(valueList);
            }

            node = node.getNextSibling();
        }
    }

    @Override
    public String toAbstractSyntax() {
        String result = " " + WcpsConstants.MSG_COVERAGE + " " + coverageName + " ";
        result = result + " " + WcpsConstants.MSG_OVER + " ";

        // axis iterators
        List<String> abstractAxisIterators = new ArrayList<>();
        for (AxisIterator axisIterator : axisIterators) {
            abstractAxisIterators.add(axisIterator.toAbstractSyntax());
        }

        result = result + " " + ListUtil.join(abstractAxisIterators, ", ");

        // value list
        result = result + " " + WcpsConstants.MSG_VALUE + " " + WcpsConstants.MSG_LIST + " ";
        result = result + "<" + valueList.toAbstractSyntax() + ">";

        return result;
    }
}
