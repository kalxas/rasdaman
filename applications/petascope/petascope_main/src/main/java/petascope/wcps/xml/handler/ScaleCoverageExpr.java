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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.ListUtil;

/**
 * Convert Scale expression in XML element to abstract element
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
// scale( c[t:"CRS:1"(2)], { Lat:"CRS:1"(0:20), Long:"CRS:1"(0:20) }
// <scale>
//    <slice>
//        <coverage>c</coverage>
//        <axis>t</axis>
//        <srsName>CRS:1</srsName>
//        <slicingPosition>
//            <numericConstant>2</numericConstant>
//        </slicingPosition>
//    </slice>
//    <axis>Lat</axis>
//    <srsName>CRS:1</srsName>
//    <lowerBound>
//        <numericConstant>0</numericConstant>
//    </lowerBound>
//    <upperBound>
//        <numericConstant>20</numericConstant>
//    </upperBound>
//    <axis>Long</axis>
//    <srsName>CRS:1</srsName>
//    <lowerBound>
//        <numericConstant>0</numericConstant>
//    </lowerBound>
//    <upperBound>
//        <numericConstant>20</numericConstant>
//    </upperBound>
//</scale>
public class ScaleCoverageExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(ScaleCoverageExpr.class);

    private List<DimensionInterval> dimensionIntervals = new ArrayList<>();
    private CoverageExpr coverageExpr;
    private FieldInterpolationElement fieldInterp;

    public ScaleCoverageExpr(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService) throws WCPSException, SecoreException {

        Node child = node.getFirstChild();
        while (child != null) {
            String nodeName = child.getNodeName();
            log.trace("node name: " + nodeName);

            if (nodeName.equals(WcpsConstants.MSG_AXIS)) {
                // Start a new axis and save it
                log.trace("  " + WcpsConstants.MSG_AXIS);
                DimensionInterval dimensionInterval = new DimensionInterval(child, wcpsXMLQueryParsingService);
                dimensionIntervals.add(dimensionInterval);
                child = dimensionInterval.getNextNode();
            } else if (nodeName.equals(WcpsConstants.MSG_NAME)) {
                log.trace("Field interpolation");
                fieldInterp = new FieldInterpolationElement(child, wcpsXMLQueryParsingService);
                child = fieldInterp.getNextNode();
            } else if (nodeName.equals(WcpsConstants.MSG_IMAGE_CRSDOMAIN)) {
                log.trace("  " + WcpsConstants.MSG_IMAGE_CRSDOMAIN);
                child = child.getFirstChild();

                if (child != null) {
                    child = child.getNextSibling();
                }
            } else {
                // has to be the coverage expression
                try {
                    log.trace("coverage expression");
                    coverageExpr = new CoverageExpr(child, wcpsXMLQueryParsingService);
                    super.children.add(coverageExpr);
                    child = child.getNextSibling();
                } catch (WCPSException ex) {
                    log.error("Unknown node for ScaleCoverageExpr expression: " + child.getNodeName());
                    throw new WCPSException(ExceptionCode.InvalidMetadata, "Unknown node for ScaleCoverageExpr expression: '" + child.getNodeName() + "'.");
                }
            }
        }

        // Add children to let the XML query be re-traversed
        super.children.addAll(dimensionIntervals);
    }

    @Override
    public String toAbstractSyntax() {
        // scale(c, {i(0:200), j(0:200)})
        String result = " " + WcpsConstants.MSG_SCALE + "( ";
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
