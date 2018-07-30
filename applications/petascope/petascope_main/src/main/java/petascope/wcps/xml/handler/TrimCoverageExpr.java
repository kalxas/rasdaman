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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.ListUtil;

public class TrimCoverageExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(TrimCoverageExpr.class);

    // The number of subsets in trim expression (each subset should contain an interval lowerBound:upperBound)
    private List<DimensionInterval> dimensionIntervals = new ArrayList<>();
    private CoverageExpr coverageExprType;
    private DimensionInterval elem;

    public TrimCoverageExpr(Node node, WCPSXmlQueryParsingService wcpsXmlQueryParsingService) throws WCPSException, SecoreException {
        Node child = node.getFirstChild();
        while (child != null) {
            String nodeName = child.getNodeName();
            log.trace("node name: " + nodeName);

            if (nodeName.equals(WcpsConstants.MSG_AXIS)) {
                // Start a new axis and save it
                log.trace("  " + WcpsConstants.MSG_AXIS);
                elem = new DimensionInterval(child, wcpsXmlQueryParsingService);
                dimensionIntervals.add(elem);
                child = elem.getNextNode();
            } else {
                try {
                    log.trace("  " + WcpsConstants.MSG_COVERAGE);
                    coverageExprType = new CoverageExpr(child, wcpsXmlQueryParsingService);
                    super.children.add(coverageExprType);
                    child = child.getNextSibling();
                } catch (WCPSException e) {
                    log.error("Expected coverage node, got " + nodeName);
                    throw new WCPSException("Unknown node for TrimCoverage expression: '" + child.getNodeName()
                            + "', error: '" + e.getMessage() + "'");
                }
            }
        }

        // Add children to let the XML query be re-traversed
        super.children.addAll(dimensionIntervals);
    }

    @Override
    public String toAbstractSyntax() {
        // e.g: c[i(0:20)]: trim(c, {i(0:20)})        
        String result = "trim( " + coverageExprType.toAbstractSyntax() + ", { ";
        List<String> subsetAbstracts = new ArrayList<>();
        for (DimensionInterval dimensionInterval : dimensionIntervals) {
            String subsetAbstract = dimensionInterval.toAbstractSyntax();
            subsetAbstracts.add(subsetAbstract);
        }

        result = result + ListUtil.join(subsetAbstracts, ", ") + "} ";
        result = result + ")";

        return result;
    }

    /**
     * Utility to check whether a specified axis is involved in this trim
     * expression
     *
     * @param axisName The name of the axis (specified in the request)
     * @return True is `axisName` is trimmed here.
     */
    public boolean trimsDimension(String axisName) {
        for (DimensionInterval trim : dimensionIntervals) {
            if (trim.getAxisName().equals(axisName)) {
                return true;
            }
        }
        return false;
    }
}
