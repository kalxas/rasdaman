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
 * Class to translate condense operation element from XML syntax to abstract syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
//condense +
//<condense>
//    <opPlus />
//</condense>
public class CondenseOperation extends AbstractRasNode {

    private String name;

    private static Logger log = LoggerFactory.getLogger(CondenseOperation.class);

    public CondenseOperation(Node node, WCPSXmlQueryParsingService xq) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        String nodeName = node.getNodeName();
        log.debug("node name: " + nodeName);
        this.name = formatOperation(nodeName);

        if (name == null) {
            throw new WCPSException("Unknown condense operation '" + nodeName + "'.");
        }
    }

    private String formatOperation(String name) {
        String shortOperation = null;
        if (name.equals(WcpsConstants.MSG_OP_PLUS)) {
            shortOperation = WcpsConstants.MSG_PLUS;
        }
        if (name.equals(WcpsConstants.MSG_OP_MULT)) {
            shortOperation = WcpsConstants.MSG_STAR;
        }
        if (name.equals(WcpsConstants.MSG_OP_MIN)) {
            shortOperation = WcpsConstants.MSG_MIN;
        }
        if (name.equals(WcpsConstants.MSG_OP_MAX)) {
            shortOperation = WcpsConstants.MSG_MAX;
        }
        if (name.equals(WcpsConstants.MSG_OP_AND)) {
            shortOperation = WcpsConstants.MSG_AND;
        }
        if (name.equals(WcpsConstants.MSG_OP_OR)) {
            shortOperation = WcpsConstants.MSG_OR;
        }

        return shortOperation;
    }

    @Override
    public String toAbstractSyntax() {
        String result = name;
        
        return result;
    }
}
