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
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.exceptions.SecoreException;
import org.w3c.dom.*;
import petascope.util.ListUtil;

/**
 * Handle crsTransform() in WCPS
 *
 * e.g: crsTransform(c[ansi(148654)] ,{
 * E:"http://www.opengis.net/def/crs/EPSG/0/3542",
 * N:"http://www.opengis.net/def/crs/EPSG/0/3542"}, {}) then will convert the 2D
 * coverage with outputCrs (3542) instead of nativeCRS (e.g: 32633, 4326) NOTE:
 * It cannot convert any coverage which is not 2D and native CRS should be
 * geo-referenced CRS (not GridAxis).
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// e.g: crsTransform( c[t:"CRS:1"(1)], { 
//                       Lat:"http:www.opengis.net/def/crs/EPSG/0/3785", 
//                       Long:"http:www.opengis.net/def/crs/EPSG/0/3785"}, {} )            
//
//    <crsTransform>
//        <slice>
//            <coverage>c</coverage>
//            <axis>t</axis>
//            <srsName>CRS:1</srsName>
//            <slicingPosition>
//                <numericConstant>1</numericConstant>
//            </slicingPosition>
//        </slice>
//        <axis>Lat</axis>
//        <srsName>http:www.opengis.net/def/crs/EPSG/0/3785</srsName>
//        <axis>Long</axis>
//        <srsName>http:www.opengis.net/def/crs/EPSG/0/3785</srsName>
//    </crsTransform>
public class CrsTransformCoverageExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(CrsTransformCoverageExpr.class);

    private CoverageExpr coverageExpr;
    private FieldInterpolationElement fieldInterpolationElement;
    private List<DimensionInterval> dimensionIntervals = new ArrayList<>();

    public CrsTransformCoverageExpr(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService) throws WCPSException, SecoreException {
        Node childNode = node.getFirstChild();

        while (childNode != null) {
            String nodeName = childNode.getNodeName();
            log.debug("node name: " + nodeName);

            if (nodeName.equals(WcpsConstants.MSG_AXIS)) {
                log.trace(" axis");
                DimensionInterval dimensionInterval = new DimensionInterval(childNode, wcpsXMLQueryParsingService);
                // Start a new axis and save it
                dimensionIntervals.add(dimensionInterval);
                childNode = dimensionInterval.getNextNode();
            } else if (nodeName.equals(WcpsConstants.MSG_NAME)) {
                log.trace("  field interpolation");
                fieldInterpolationElement = new FieldInterpolationElement(childNode, wcpsXMLQueryParsingService);
                childNode = fieldInterpolationElement.getNextNode();
            } else {
                // has to be the coverage expression
                try {
                    log.trace("  coverage expression");
                    coverageExpr = new CoverageExpr(childNode, wcpsXMLQueryParsingService);
                    super.children.add(coverageExpr);
                    childNode = childNode.getNextSibling();
                } catch (WCPSException ex) {
                    log.error("  unknown node for CrsTransformCoverageExpr expression:" + childNode.getNodeName());
                    throw new WCPSException(ExceptionCode.InvalidMetadata, "Unknown node for CrsTransformCoverageExpr expression '" + childNode.getNodeName() + "'.");
                }
            }
        }

        // Add children to let the XML query be re-traversed
        super.children.addAll(dimensionIntervals);
    }

    @Override
    public String toAbstractSyntax() {
        // crsTransform(c[t:"CRS:1"(1)], 
        //             { Lat:"http://www.opengis.net/def/crs/EPSG/0/3785", 
        //               Long:"http://www.opengis.net/def/crs/EPSG/0/3785"}, {})
        String result = " " + WcpsConstants.MSG_CRS_TRANSFORM + "( ";
        result = result + " " + coverageExpr.toAbstractSyntax() + ", { ";
        
        // 2 XY axes with target CRS to transform
        List<String> abstractDimensionIntervals = new ArrayList<>();
        for (DimensionInterval dimensionInterval : dimensionIntervals) {
            abstractDimensionIntervals.add(dimensionInterval.toAbstractSyntax());
        }
        
        result = result + " " + ListUtil.join(abstractDimensionIntervals, ", ");
        // NOTE: not support interpolation now
        result = result + "}, {} )";

        return result;
    }
}
