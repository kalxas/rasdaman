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

/**
 * Class to translate extend element from XML syntax to abstract syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// extend(c, {i(0:500), j(0:500)}
//<extend>
//    <coverage>c</coverage>
//    <axis>i</axis>
//    <lowerBound>
//        <numericConstant>0</numericConstant>
//    </lowerBound>
//    <upperBound>
//        <numericConstant>500</numericConstant>
//    </upperBound>
//    <axis>j</axis>
//    <lowerBound>
//        <numericConstant>0</numericConstant>
//    </lowerBound>
//    <upperBound>
//        <numericConstant>500</numericConstant>
//    </upperBound>
//</extend>
public class ExtendCoverageExpr extends AbstractRasNode {

    private static final Logger log = LoggerFactory.getLogger(ExtendCoverageExpr.class);

    private List<DimensionInterval> dimensionIntervals = new ArrayList<>();
    private CoverageExpr coverageExpr;

    public ExtendCoverageExpr(Node node, WCPSXmlQueryParsingService wcpsXmlQueryParsingService) throws WCPSException, SecoreException {

        Node child = node.getFirstChild();

        String nodeName = node.getNodeName();
        log.trace(nodeName);

        coverageExpr = new CoverageExpr(child, wcpsXmlQueryParsingService);
        super.children.add(coverageExpr);
        child = child.getNextSibling();

        while (child != null) {
            // Start a new axis and save it
            // it can extend in multiple subset dimensions
            DimensionInterval dimensionInterval = new DimensionInterval(child, wcpsXmlQueryParsingService);
            log.trace("added new axis to list: " + dimensionInterval.getAxisName());
            dimensionIntervals.add(dimensionInterval);
            super.children.add(dimensionInterval);
            child = dimensionInterval.getNextNode();
        }
    }

    @Override
    public String toAbstractSyntax() {
        // extend(c, {i(0:200), j(0:200)})
        String result = " " + WcpsConstants.MSG_EXTEND + "( ";
        result = result + coverageExpr.toAbstractSyntax() + ", {";
        // domains to extend
        List<String> abstractDimensionIntervals = new ArrayList<>();
        for (DimensionInterval dimensionInterval : dimensionIntervals) {
            abstractDimensionIntervals.add(dimensionInterval.toAbstractSyntax());
        }

        result = result + " " + ListUtil.join(abstractDimensionIntervals, ", ") + " }";
        result = result + ")";

        return result;
    }
}
