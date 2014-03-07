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
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.WcpsConstants;



/**
 * This is a component of a RangeStructure. 
 * @author Andrei Aiordachioaie
 */
public class RangeComponent extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(RangeComponent.class);

    private String field = null;
    private CoverageInfo info = null;
    private CoverageExpr expr = null;

    public RangeComponent(Node node, XmlQuery xq) throws WCPSException, SecoreException {

        String nodeName = node.getNodeName();
        log.trace(nodeName);
        if (nodeName.equals(WcpsConstants.MSG_COMPONENT))
            node = node.getFirstChild();
            
        while (node != null)
        {
            nodeName = node.getNodeName();
            if (nodeName.equals("#" + WcpsConstants.MSG_TEXT))
            {
                node = node.getNextSibling();
                continue;
            }

            if (nodeName.equals(WcpsConstants.MSG_FIELD)) {
                this.field = node.getTextContent();
                log.trace("  " + WcpsConstants.MSG_FIELD + ": " + field);
            } else {
                try
                {
                    this.expr = new CoverageExpr(node, xq);
                    this.info = expr.getCoverageInfo();
                }
                catch (WCPSException e)
                {
                    log.error("Could not match CoverageExpr inside RangeExpr. Next node: " + nodeName);
                    throw e;
                }
            }

            node = node.getNextSibling();
        }
    }

    public CoverageInfo getCoverageInfo() {
        return info;
    }

    public String toRasQL() {
        String result = "";

        if (this.expr != null)
            result = expr.toRasQL();

        return result;
    }
}
