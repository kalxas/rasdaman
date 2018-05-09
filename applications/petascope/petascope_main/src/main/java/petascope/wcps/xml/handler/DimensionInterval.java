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
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;

/**
 * Translate a subset dimension from XML syntax to abstract syntax, used in
 * trimming expression
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
// i(1:100)
//<axis>i</axis>
//<lowerBound>
//    <numericConstant>1</numericConstant>
//</lowerBound>
//<upperBound>
//    <numericConstant>100</numericConstant>
//</upperBound>
public class DimensionInterval extends AbstractRasNode {

    Logger log = LoggerFactory.getLogger(DimensionInterval.class);

    private AxisName axisName;
    private SrsName srsName;
    // lower and upper bound, or "DomainMetadataExprType" and null    
    private ScalarExpr lowerBound;
    private ScalarExpr upperBound;
    private Node nextNode;

    /**
     * Constructs an element of a dimension interval.
     *
     * @param node XML Node
     * @param wcpsXmlQueryParsingService WCPS Xml Query object
     * @throws WCPSException
     * @throws SecoreException
     */
    public DimensionInterval(Node node, WCPSXmlQueryParsingService wcpsXmlQueryParsingService)
            throws WCPSException, SecoreException {

        String nodeName = node.getNodeName();
        log.trace(nodeName);

        // Just read all the sibling elements of the current subset dimension                    
        axisName = new AxisName(node, wcpsXmlQueryParsingService);
        node = node.getNextSibling();

        // Try CRS name (as axis can have no CRS element)
        try {
            srsName = new SrsName(node, wcpsXmlQueryParsingService);
            node = node.getNextSibling();
            if (axisName == null) {
                throw new WCPSException("Expected Axis node before CRS.");
            }
        } catch (WCPSException e) {

        }
        
        // NOTE: axis can have no lowerBound and upperBound elements
        // and it should go to the next axis element when it is not null        
        if (node == null || (node != null && node.getNodeName().equals(WcpsConstants.MSG_AXIS))) {
            nextNode = node;
            return;
        }
        
        // Then it must be a pair of nodes "lowerBound" + "upperBound"
        if (node.getNodeName().equals(WcpsConstants.MSG_LOWER_BOUND)) {
            lowerBound = new ScalarExpr(node.getFirstChild(), wcpsXmlQueryParsingService);
            if (axisName == null) {
                log.error("Expected <axis> node before <lowerBound>.");
                throw new WCPSException("Expected <axis> node before <lowerBound>.");
            }
            node = node.getNextSibling();
        }

        if (node.getNodeName().equals(WcpsConstants.MSG_UPPER_BOUND)) {
            upperBound = new ScalarExpr(node.getFirstChild(), wcpsXmlQueryParsingService);
            if (axisName == null) {
                log.error("Expected <lowerBound> node before <upperBound>.");
                throw new WCPSException("Expected <lowerBound> node before <upperBound>.");
            }
            node = node.getNextSibling();
        } else {
            log.error("Unexpected node: " + node.getFirstChild().getNodeName());
            throw new WCPSException("Unexpected node '" + node.getFirstChild().getNodeName() + "'.");
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

        if (lowerBound != null && upperBound != null) {
            result = result + "(";
            result = result + lowerBound.toAbstractSyntax();
            result = result + RASQL_BOUND_SEPARATION;
            result = result + upperBound.toAbstractSyntax();
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

    public String getCrs() {
        return this.srsName.getName();
    }
}
