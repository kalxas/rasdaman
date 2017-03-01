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
package petascope.wcps.server.core;

import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import petascope.util.WcpsConstants;

public class CondenseOperation extends AbstractRasNode {

    private String name;

    public CondenseOperation(Node node, XmlQuery xq) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        String text = node.getNodeName();
        this.name = formatOperation(text);

        if (name == null) {
            throw new WCPSException("Unknown condense operation: " + text);
        }
    }

    private String formatOperation(String name) {
        String shortOp = null;
        if (name.equals(WcpsConstants.MSG_OP_PLUS)) {
            shortOp = WcpsConstants.MSG_PLUS;
        }
        if (name.equals(WcpsConstants.MSG_OP_MULT)) {
            shortOp = WcpsConstants.MSG_STAR;
        }
        if (name.equals(WcpsConstants.MSG_OP_MIN)) {
            shortOp = WcpsConstants.MSG_MIN;
        }
        if (name.equals(WcpsConstants.MSG_OP_MAX)) {
            shortOp = WcpsConstants.MSG_MAX;
        }
        if (name.equals(WcpsConstants.MSG_OP_AND)) {
            shortOp = WcpsConstants.MSG_AND;
        }
        if (name.equals(WcpsConstants.MSG_OP_OR)) {
            shortOp = WcpsConstants.MSG_OR;
        }

        return shortOp;
    }

    public String toRasQL() {
        return name;
    }
}
