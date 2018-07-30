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
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;

/**
 * This is a component of a RangeStructure.
 *
 * @author Andrei Aiordachioaie
 */
public class RangeComponent extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(RangeComponent.class);

    private Field field;
    private CoverageExpr coverageExpr;

    public RangeComponent(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService) throws WCPSException, SecoreException {

        String nodeName = node.getNodeName();
        log.trace(nodeName);

        if (nodeName.equals(WcpsConstants.MSG_COMPONENT)) {
            node = node.getFirstChild();
        }

        while (node != null) {
            nodeName = node.getNodeName();

            if (nodeName.equals(WcpsConstants.MSG_FIELD)) {
                this.field = new Field(node.getFirstChild(), wcpsXMLQueryParsingService);
                log.trace("  " + WcpsConstants.MSG_FIELD + ": " + field);
            } else {
                try {
                    this.coverageExpr = new CoverageExpr(node, wcpsXMLQueryParsingService);
                } catch (WCPSException e) {
                    log.error("Could not match CoverageExpr inside RangeExpr. Next node: '" + nodeName + "'.");
                    throw e;
                }
            }

            node = node.getNextSibling();
        }
    }

    @Override
    public String toAbstractSyntax() {
        // red: (unsigned char) 1
        String result = " " + field.toAbstractSyntax() + ": ";
        result = result + coverageExpr.toAbstractSyntax();        

        return result;
    }
}
