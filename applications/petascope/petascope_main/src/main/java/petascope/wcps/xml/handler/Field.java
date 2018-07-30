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
 * Class to translate field (band name) element of a coverage from XML syntax to abstract syntax
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// c.red
//<field>
//    <name>red</name>
//</field>
public class Field extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(Field.class);

    /**
     * Band label.
     */
    private String name;

    public Field(Node node, WCPSXmlQueryParsingService xq) throws WCPSException {        
        if (node == null) {
            throw new WCPSException("FieldNameType parsing error.");
        }

        String nodeName = node.getNodeName();
        log.trace(nodeName);

        if (nodeName.equals(WcpsConstants.MSG_NAME)) {
            this.name = node.getTextContent();
            log.trace("Found field name: " + name);
        }
    }

    @Override
    public String toAbstractSyntax() {
        String result = this.name;
        
        return result;
    }
};
