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
 * Class to translate condense element from XML syntax to abstract syntax
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
//condense +
//over $x x(1:10)
//using $x * (a[i($x), j(10)]).red
//<condense>
//    <opPlus />
//    <iterator>
//        <iteratorVar>x</iteratorVar>
//        <axis>x</axis>
//        <numericConstant>1</numericConstant>
//        <numericConstant>10</numericConstant>
//    </iterator>
//    <mult>
//        <variableRef>x</variableRef>
//        <fieldSelect>
//            <slice>
//                <coverage>a</coverage>
//                <axis>i</axis>
//                <slicingPosition>
//                    <variableRef>x</variableRef>
//                </slicingPosition>
//                <axis>j</axis>
//                <slicingPosition>
//                    <numericConstant>10</numericConstant>
//                </slicingPosition>
//            </slice>
//            <field>
//                <name>red</name>
//            </field>
//        </fieldSelect>
//    </mult>
//</condense>
public class CondenseScalarExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(CondenseScalarExpr.class);

    private CondenseOperation operation;
    private List<AxisIterator> axisIterators;
    private IRasNode usingExpression;
    // NOTE: condense can have where expression
    private IRasNode whereExpression;

    public CondenseScalarExpr(Node node, WCPSXmlQueryParsingService wcpsXmlQueryParsingService) throws WCPSException, SecoreException {
        if (node.getNodeName().equals(WcpsConstants.MSG_CONDENSE)) {
            node = node.getFirstChild();
        }

        String nodeName = node.getNodeName();
        log.trace("node name: " + nodeName);

        axisIterators = new ArrayList<>();

        while (node != null) {
            String name = node.getNodeName();
            if (operation == null) {
                operation = new CondenseOperation(node, wcpsXmlQueryParsingService);
            } else if (name.equals(WcpsConstants.MSG_ITERATOR)) {
                // <iterator> ... </iterator>
                AxisIterator axisIterator = new AxisIterator(node.getFirstChild(), wcpsXmlQueryParsingService);
                axisIterators.add(axisIterator);
                super.children.add(axisIterator);
            } else if (name.equals(WcpsConstants.MSG_WHERE)) {
                // <where> ... </where>
//                condense +
//                over $x x(0:100),
//                     $y y(0:100)
//                where (max(c[i($x:$x),j($y:$y)]) < 100)
//                using c[i($x),j($y)]
                whereExpression = new BooleanScalarExpr(node.getFirstChild(), wcpsXmlQueryParsingService);
            } else {
                usingExpression = new CoverageExpr(node, wcpsXmlQueryParsingService);
            }

            node = node.getNextSibling();

            // Keep the children to let XML tree be re-traversed
            if (whereExpression != null) {
                super.children.add(whereExpression);
            }
            if (usingExpression != null) {
                super.children.add(usingExpression);
            }
        }
    }

    @Override
    public String toAbstractSyntax() {
//      condense +
//      over $x x(0:100),
//           $y y(0:100)
//      where (max(c[i($x:$x),j($y:$y)]) < 100)
//      using c[i($x),j($y)]
        String result = WcpsConstants.MSG_CONDENSE + " " + operation.toAbstractSyntax();
        result = result + " " + WcpsConstants.MSG_OVER + " ";

        // Axis iterators
        List<String> abstractAxisIterators = new ArrayList<>();
        for (AxisIterator axisIterator : axisIterators) {
            abstractAxisIterators.add(axisIterator.toAbstractSyntax());
        }
        result = result + " " + ListUtil.join(abstractAxisIterators, ", ");

        // where expression
        if (whereExpression != null) {
            result = result + " " + WcpsConstants.MSG_WHERE + " " + whereExpression.toAbstractSyntax();
        }

        // using expression
        result = result + " " + WcpsConstants.MSG_USING + " " + usingExpression.toAbstractSyntax();

        return result;
    }
}
