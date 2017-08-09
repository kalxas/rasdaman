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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.WCPSException;

/**
 * Class to translate srsName element of subset axis in XML syntax to abstract
 * syntax
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
// Lat:"CRS:1"(0:20)
//<axis>Lat</axis>
//<srsName>CRS:1</srsName>
//<lowerBound>
//    <numericConstant>0</numericConstant>
//</lowerBound>
//<upperBound>
//    <numericConstant>20</numericConstant>
//</upperBound>
public class SrsName extends AbstractRasNode {

    private static final Logger log = LoggerFactory.getLogger(SrsName.class);
    private String srsName;

    public SrsName(String srsName, WCPSXmlQueryParsingService xq) {
        this.srsName = srsName;
    }

    public SrsName(Node node, WCPSXmlQueryParsingService xq) throws WCPSException {
        log.trace(node.getNodeName());

        if (node.getNodeName().equals(WcpsConstants.MSG_SRS_NAME)
                || node.getNodeName().equals(WcpsConstants.MSG_CRS)) {
            String val = node.getTextContent();
            this.srsName = val;
            log.trace("Found CRS: " + srsName);
        } else {
            throw new WCPSException("Could not find a 'srsName' node.");
        }
    }

    @Override
    public String toAbstractSyntax() {
        // e.g: "CRS:1"
        String result = "\"" + srsName + "\"";
        
        return result;
    }

    public String getName() {
        return srsName;
    }
}
