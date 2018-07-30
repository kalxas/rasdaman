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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;

/**
 * Class to translate metadata expression from XML syntax to abstract syntax
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
// return identifier(c)
// <identifier>
//    <coverage>c</coverage>
// </identifier>
public class MetadataScalarExpr extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(MetadataScalarExpr.class);

    public static final Set<String> NODE_NAMES = new HashSet<String>();
    private static final String[] NODE_NAMES_ARRAY = {
        WcpsConstants.MSG_DOMAIN_METADATA,
        WcpsConstants.MSG_IMAGE_CRSDOMAIN,
        WcpsConstants.MSG_CRS_SET,
        WcpsConstants.MSG_IDENTIFIER,
        WcpsConstants.MSG_IMAGE_CRS
    };

    static {
        NODE_NAMES.addAll(Arrays.asList(NODE_NAMES_ARRAY));
    }

    private String operation;
    // Translated child elements to abstract syntax, e.g: identifier(c)
//    <identifier>
//        <coverage>c</coverage>
//    </identifier>
    private String abstractChildContent;

    public MetadataScalarExpr(Node node, WCPSXmlQueryParsingService wcpsXMLQueryParsingService) throws WCPSException, SecoreException {
        String nodeName = node.getNodeName();
        log.trace("node name: " + nodeName);
        operation = nodeName;

        // the first argument is always a coverage expression
        Node childNode = node.getFirstChild();
        CoverageExpr coverageExpr = new CoverageExpr(childNode, wcpsXMLQueryParsingService);
        abstractChildContent = coverageExpr.toAbstractSyntax();

        if (nodeName.equals(WcpsConstants.MSG_DOMAIN_METADATA)) {
            // domain(c, Long, "http://localhost:8080/def/crs/EPSG/0/4326")
            // <DomainMetadata>
            //    <coverage>c</coverage>
            //    <axis>Long</axis>
            //    <crs>CRS:1</crs>
            // </DomainMetadata>
            // return the geo domain of the coverage's axis with the input CRS (-40.50:75.50)
            operation = WcpsConstants.MSG_DOMAIN;
            Node axisNode = childNode.getNextSibling();
            abstractChildContent = abstractChildContent + ", " + axisNode.getTextContent();

            Node crsNode = axisNode.getNextSibling();
            abstractChildContent = abstractChildContent + ", " + "\"" + crsNode.getTextContent() + "\"";

        } else if (nodeName.equals(WcpsConstants.MSG_IMAGE_CRSDOMAIN)) {
            // NOTE: There are 2 types of imageCrsDomain to return the grid interval for all axes or just one axis
            // it can be: return imageCrsDomain(c) returns (0:5, 0:100, 0:10) with c is 3D coverages
            // or it can be: return imageCrsDomain(c, Lat) returns (0:10) of axis Lat with c is 3D coverages
            Node axisNode = childNode.getNextSibling();
            if (axisNode != null) {
                abstractChildContent = abstractChildContent + ", " + axisNode.getTextContent();
            }
        } else if (nodeName.equals(WcpsConstants.MSG_CRS_SET)) {
            // crsSet(c), already translated        
            // result: t:http://localhost:8080/def/crs/OGC/0/AnsiDate?axis-label="t" CRS:1,
            //         Lat:http://localhost:8080/def/crs/EPSG/0/4326 CRS:1,
            //         Long:http://localhost:8080/def/crs/EPSG/0/4326 CRS:1
        } else if (nodeName.equals(WcpsConstants.MSG_IDENTIFIER)) {
            // identifier(c), already translated            
            // result: test_mr
        } else if (nodeName.equals(WcpsConstants.MSG_IMAGE_CRS)) {
            // imageCrs(c), already translated
            // result: CRS:1 (grid CRS)
        }

        super.children.add(coverageExpr);
    }

    @Override
    public String toAbstractSyntax() {
        // e.g: identifier(c), imageCrsDomain(c, Lat)
        String result = operation + "(" + abstractChildContent + ")";

        return result;
    }
}
