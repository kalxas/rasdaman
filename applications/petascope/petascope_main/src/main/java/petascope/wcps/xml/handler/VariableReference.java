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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCPSException;
import org.w3c.dom.*;

/**
 * Class to translate a variableRef element of axisIterator from XML syntax to
 * abstract syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// $second y(0:99)
//<axisIterator>
//    <iteratorVar>second</iteratorVar>
//    <axis>y</axis>
//    <numericConstant>0</numericConstant>
//    <numericConstant>99</numericConstant>
//</axisIterator>
public class VariableReference extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(VariableReference.class);

    private String name;

    public VariableReference(Node node, WCPSXmlQueryParsingService xq) throws WCPSException {
        String nodeName = node.getNodeName();
        log.trace("node name: " + nodeName);

        if (node != null && node.getNodeName().equals(WcpsConstants.MSG_VARIABLE_REF)) {
            name = node.getTextContent();
        } else {
            log.error("No variable reference found");
            throw new WCPSException("Could not find any variable reference.");
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toAbstractSyntax() {
        String result = "$" + name;

        return result;
    }
}
