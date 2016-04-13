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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import petascope.exceptions.ExceptionCode;
import petascope.util.WcpsConstants;

public class AxisName extends AbstractRasNode {
    
    private static Logger log = LoggerFactory.getLogger(AxisName.class);

    private String name;

    public AxisName(Node node, XmlQuery xq) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        if (node != null && node.getNodeName().equals(WcpsConstants.MSG_AXIS)) {
            log.trace(node.getNodeName());
            String axis = node.getTextContent();
            this.name = axis;
            log.trace("  " + WcpsConstants.MSG_AXIS + " " + WcpsConstants.MSG_NAME + ": " + name);
        } else {
            throw new WCPSException(ExceptionCode.InvalidRequest, "Could not find an axis node.");
        }
    }

    public String toRasQL() {
        return name;
    }
}
