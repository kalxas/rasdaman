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

import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to translate cast type from XML syntax to abstract syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// (unsigned char) (abs((c.1) - (c.0)))
//<cast>
//    <abs>
//        <minus>
//            <fieldSelect>
//                <coverage>c</coverage>
//                <field>
//                    <name>1</name>
//                </field>
//            </fieldSelect>
//            <fieldSelect>
//                <coverage>c</coverage>
//                <field>
//                    <name>0</name>
//                </field>
//            </fieldSelect>
//        </minus>
//    </abs>
//    <type>unsigned char</type>
//</cast>
public class CastType extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(CastType.class);

    private String type;

    public CastType(Node node, WCPSXmlQueryParsingService xq) throws WCPSException {

        String nodeName = node.getNodeName();
        log.trace("node name: " + nodeName);
        if (node == null) {
            throw new WCPSException("RangeFieldType parsing error.");
        }

        if (nodeName.equals(WcpsConstants.MSG_TYPE)) {
            String type = node.getTextContent();            
            this.type = type;
            log.trace("Range field type: " + type);
        }
    }

    @Override
    public String toAbstractSyntax() {
        String result = this.type;

        return result;
    }
};
