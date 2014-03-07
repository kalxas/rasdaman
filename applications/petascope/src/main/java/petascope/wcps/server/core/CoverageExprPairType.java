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

import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.WcpsConstants;

public class CoverageExprPairType extends AbstractRasNode implements ICoverageInfo {

    private IRasNode first, second;
    private CoverageInfo info;
    private boolean ok = false;

    public CoverageExprPairType(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        String nodeName = node.getNodeName();

        try {
            first = new ScalarExpr(node, xq);
        } catch (WCPSException ex) {
            first = new CoverageExpr(node, xq);
        }

        node = node.getNextSibling();
        try {
            second = new ScalarExpr(node, xq);
        } catch (WCPSException ex) {
            second = new CoverageExpr(node, xq);
        }
        
        info = new CoverageInfo(((ICoverageInfo) first).getCoverageInfo());
        if(info == null || first instanceof ScalarExpr){
            info = new CoverageInfo(((ICoverageInfo) second).getCoverageInfo());
        }
    }

    public CoverageInfo getCoverageInfo() {
        return info;
    }

    public String toRasQL() {
        if (ok == true) {
            return first.toRasQL() + second.toRasQL();
        } else {
            return " " + WcpsConstants.MSG_ERROR + " ";
        }
    }

    public IRasNode getFirst() {
        return first;
    }

    public IRasNode getSecond() {
        return second;
    }
}
