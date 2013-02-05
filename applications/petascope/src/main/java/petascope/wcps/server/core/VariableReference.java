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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import petascope.util.WCPSConstants;

public class VariableReference extends AbstractRasNode {
    
    private static Logger log = LoggerFactory.getLogger(VariableReference.class);

    private String name;
    private String translatedName;

    public VariableReference(Node node, XmlQuery xq) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        log.trace(node.getNodeName());

        if (node != null && node.getNodeName().equals(WCPSConstants.MSG_VARIABLE_REF)) {
            name = node.getTextContent();
            translatedName = xq.getReferenceVariableName(name);
            log.trace("  " + WCPSConstants.MSG_VARIABLE + " " + name + " " + WCPSConstants.MSG_HAS_BEEN_RENAMED+ " " + translatedName);
        } else {
            log.error("  " + WCPSConstants.ERRTXT_NOT_VAR_REF_FOUND);
            throw new WCPSException(WCPSConstants.ERRTXT_COULD_NOT_FIND_ANY_VAR_REF);
        }
    }

    public String getName() {
        return name;
    }

    public String toRasQL() {
        return translatedName;
    }
}
