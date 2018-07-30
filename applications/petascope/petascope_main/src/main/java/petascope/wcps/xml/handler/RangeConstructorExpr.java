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
 * Class to translate rangeConstructor element from XML syntax to abstract
 * syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
//{
//    red: (unsigned char) 1;
//    green: (unsigned char) 2;
//    blue: (unsigned char) 3
//}
//<rangeConstructor>
//    <component>
//        <field>red</field>
//        <cast>
//            <numericConstant>1</numericConstant>
//            <type>unsigned char</type>
//        </cast>
//    </component>
//    <component>
//        <field>green</field>
//        <cast>
//            <numericConstant>2</numericConstant>
//            <type>unsigned char</type>
//        </cast>
//    </component>
//    <component>
//        <field>blue</field>
//        <cast>
//            <numericConstant>3</numericConstant>
//            <type>unsigned char</type>
//        </cast>
//    </component>
//</rangeConstructor>
public class RangeConstructorExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(RangeConstructorExpr.class);

    List<RangeComponent> rangeComponents = new ArrayList<>();

    public RangeConstructorExpr(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService) throws WCPSException, SecoreException {

        String nodeName = node.getNodeName();
        log.trace("node name: " + nodeName);

        if (nodeName.equals(WcpsConstants.MSG_RANGE_CONSTRUCTOR)) {
            node = node.getFirstChild();
        }

        while (node != null) {
            if (node.getNodeName().equals(WcpsConstants.MSG_COMPONENT)) {
                RangeComponent rangeComponent = new RangeComponent(node, wcpsXMLQueryParsingService);
                rangeComponents.add(rangeComponent);
            }

            node = node.getNextSibling();
        }

        // Keep children to let the XML tree be re-traversed
        super.children.addAll(rangeComponents);
    }

    @Override
    public String toAbstractSyntax() {
        String result = " {";
        
        // Range (band) components
        // red: (unsigned char) 1;
        // green: (unsigned char) 2;
        // blue: (unsigned char) 3
        List<String> abstractRangeComponents = new ArrayList<>();
        for (RangeComponent rangeComponent : rangeComponents) {
            abstractRangeComponents.add(rangeComponent.toAbstractSyntax());
        }
        
        result = result + " " + ListUtil.join(abstractRangeComponents, "; ");        
        result = result + " }";

        return result;
    }
}
