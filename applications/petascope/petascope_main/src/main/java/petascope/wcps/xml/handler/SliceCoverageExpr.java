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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.ListUtil;

/**
 * Class to translate subset slices from XML syntax to WCPS syntax
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class SliceCoverageExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(SliceCoverageExpr.class);

    // e.g: i(0)
    private List<DimensionPoint> dimensionPoints = new ArrayList<>();
    private CoverageExpr coverageExprType;
    private DimensionPoint elem;

    public SliceCoverageExpr(Node node, WCPSXmlQueryParsingService xq) throws WCPSException, SecoreException {

        Node child = node.getFirstChild();
        while (child != null) {
            String nodeName = child.getNodeName();
            log.debug("node name: " + nodeName);

            if (nodeName.equals(WcpsConstants.MSG_AXIS)) {
                // Start a new axis and save it
                log.trace("  " + WcpsConstants.MSG_AXIS);
                elem = new DimensionPoint(child, xq);
                dimensionPoints.add(elem);
                child = elem.getNextNode();
            } else {
                try {
                    log.trace("  " + WcpsConstants.MSG_COVERAGE);
                    coverageExprType = new CoverageExpr(child, xq);
                    super.children.add(coverageExprType);
                    child = child.getNextSibling();
                } catch (WCPSException ex) {
                    log.error("Expected coverage node, got " + nodeName, ex);
                    throw new WCPSException("Unknown node for SliceCoverage expression: '" + child.getNodeName() + "'.");
                }
            }
        }

        // Add children to let the XML query be re-traversed
        super.children.addAll(dimensionPoints);
    }

    @Override
    public String toAbstractSyntax() {
        // e.g: c[i(0)]: slice(c, {i(0)})
        String result = "slice( " + coverageExprType.toAbstractSyntax() + ", { ";        
        List<String> subsetAbstracts = new ArrayList<>();
        for (DimensionPoint dimensionPoint : dimensionPoints) {
            String subsetAbstract = dimensionPoint.toAbstractSyntax();
            subsetAbstracts.add(subsetAbstract);
        }
        result = result + ListUtil.join(subsetAbstracts, ", ") + " } ";
        result = result + ")";

        return result;
    }
}
