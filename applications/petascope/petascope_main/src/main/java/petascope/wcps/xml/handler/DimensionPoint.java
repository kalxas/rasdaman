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
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import static petascope.wcps.xml.handler.WcpsConstants.MSG_STAR;

/**
 * Class to translate a slicing point from XML syntax to abstract syntax, used
 * for slicing expression.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
// A slicing point is: i(0) or in XML syntax
//<axis>i</axis>
//<slicingPosition>
//    <numericConstant>0</numericConstant>
//</slicingPosition>
public class DimensionPoint extends AbstractRasNode {

    Logger log = LoggerFactory.getLogger(DimensionPoint.class);

    private AxisName axisName;
    private SrsName srsName;
    private Node nextNode;
    private ScalarExpr bound;

    public DimensionPoint(Node node, WCPSXmlQueryParsingService wcpsXmlQueryParsingService)
            throws WCPSException, SecoreException {

        String nodeName = node.getNodeName();
        log.trace("node name: " + nodeName);

        log.trace("Matching axis name.");
        axisName = new AxisName(node, wcpsXmlQueryParsingService);
        node = node.getNextSibling();

        // Try CRS name as axis can have no CRS element
        try {
            log.trace("Matching crs.");
            srsName = new SrsName(node, wcpsXmlQueryParsingService);
            if (axisName == null) {
                throw new WCPSException("Expected Axis node before CRS.");
            }
            node = node.getNextSibling();
        } catch (WCPSException e) {
        }

        // NOTE: axis can have no slicingPoint element
        // and it should go to the next axis element when it is not null        
        if (node == null || (node != null && node.getNodeName().equals(WcpsConstants.MSG_AXIS))) {
            nextNode = node;
            return;
        }

        // Then it must be a "slicingPosition"
        if (node.getNodeName().equals(WcpsConstants.MSG_SLICING_POSITION)) {
            log.trace("Slice position");
            bound = new ScalarExpr(node.getFirstChild(), wcpsXmlQueryParsingService);
            if (axisName == null) {
                throw new WCPSException("Expected <axis> node before <slicingPosition>.");
            }
            node = node.getNextSibling();
        } else {
            throw new WCPSException("Unexpected node: " + node.getFirstChild().getNodeName());
        }

        if (bound.toAbstractSyntax().equals(MSG_STAR)) {
            // Throw InvalidSubsetting to let the exception surface out of WCPS parsing (see CoverageExpr)
            throw new WCPSException(ExceptionCode.InvalidSubsetting, "Cannot use asterisk in slicing expression.");
        }

        nextNode = node;
    }

    @Override
    public String toAbstractSyntax() {
        // e.g: i:"CRS:1"(0:0)
        String result = axisName.toAbstractSyntax();
        if (srsName != null) {
            result = result + ":" + srsName.toAbstractSyntax();
        }
        if (bound != null) {
            result = result + "(";
            result = result + bound.toAbstractSyntax();
            result = result + ")";
        }

        return result;
    }

    public Node getNextNode() {
        return nextNode;
    }

    public String getAxisName() {
        return this.axisName.getName();
    }

    public String getSrsName() {
        return this.srsName.getName();
    }
}
