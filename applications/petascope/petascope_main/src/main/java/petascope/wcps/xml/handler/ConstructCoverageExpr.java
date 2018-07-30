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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.ListUtil;


/**
 * Translate a CoverageConstructor expression in XML syntax to abstract syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// coverage histogram
//          over $n x(0:2)
//          values count( c = $n )
//<construct>
//    <name>histogram</name>
//    <axisIterator>
//        <iteratorVar>n</iteratorVar>
//        <axis>x</axis>
//        <numericConstant>0</numericConstant>
//        <numericConstant>2</numericConstant>
//    </axisIterator>
//    <reduce>
//        <count>
//            <equals>
//                <coverage>c</coverage>
//                <variableRef>n</variableRef>
//            </equals>
//        </count>
//    </reduce>
//</construct>
public class ConstructCoverageExpr extends AbstractRasNode {

    private String coverageName;
    private List<AxisIterator> axisIterators = new ArrayList<>();
    private IRasNode valuesExpression;

    private static Logger log = LoggerFactory.getLogger(ConstructCoverageExpr.class);

    public ConstructCoverageExpr(Node node, WCPSXmlQueryParsingService wcpsXmlQueryParsingService)
            throws WCPSException, SecoreException {

        while (node != null) {
            String nodeName = node.getNodeName();
            log.debug("node name: " + nodeName);

            if (nodeName.equals(WcpsConstants.MSG_NAME)) {
                coverageName = node.getTextContent();
            } else if (nodeName.equals(WcpsConstants.MSG_AXIS_ITERATOR)) {
                AxisIterator it = new AxisIterator(node.getFirstChild(), wcpsXmlQueryParsingService);
                axisIterators.add(it);
            } else {
                /* The iterator is probably used in the "values" section,
                 * so send the iterator to the top-level query */
                // And only then start parsing the "values" section
                valuesExpression = new CoverageExpr(node, wcpsXmlQueryParsingService);

                // Keep children to let the XML tree be re-traversed
                super.children.add(valuesExpression);
            }

            node = node.getNextSibling();
        }
    }

    @Override
    public String toAbstractSyntax() {
        // e.g: coverage greyImage 
        // over $first x(51:150), 
        //      $second y(0:99) 
        // values $first
        String result = " " + WcpsConstants.MSG_COVERAGE + " " + coverageName;
        result = result + " " + WcpsConstants.MSG_OVER;
        List<String> abstractAxisIterators = new ArrayList<>();
        for (AxisIterator axisIterator : axisIterators) {
            abstractAxisIterators.add(axisIterator.toAbstractSyntax());
        }

        result = result + " " + ListUtil.join(abstractAxisIterators, ", ");
        result = result + " " + WcpsConstants.MSG_VALUES;
        result = result + " " + valuesExpression.toAbstractSyntax();

        return result;
    }
}
