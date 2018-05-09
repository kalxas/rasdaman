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

import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;

/**
 * Class to translate axisIterator element from XML syntax to abstract syntax
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
//$first x(51:150)
//<axisIterator>
//    <iteratorVar>first</iteratorVar>
//    <axis>x</axis>
//    <numericConstant>51</numericConstant>
//    <numericConstant>150</numericConstant>
//</axisIterator>
public class AxisIterator extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(AxisIterator.class);

    private String iteratorVar;
    private AxisName axis;
    private NumericScalarExpr lowerBound, upperBound;
    // In case of axis iterator using imageCrsDomain, don't parse it as lowerBound, upperBound as it is not numeric
    private String imageCrsDomainBounds;

    public AxisIterator(Node node, WCPSXmlQueryParsingService wcpsXmlQueryParsingService) throws WCPSException, SecoreException {

        while (node != null) {
            String nodeName = node.getNodeName();
            log.debug("node name: " + nodeName);

            if (nodeName.equals(WcpsConstants.MSG_ITERATORVAR)) {
                iteratorVar = node.getTextContent();
                // This variable will be referenced later on. Translate it.
                log.trace("Iterator var: " + iteratorVar);
            } else if (nodeName.equals(WcpsConstants.MSG_AXIS)) {
                axis = new AxisName(node, wcpsXmlQueryParsingService);
                log.trace("Of axis: " + axis.getName());
            } else if (nodeName.equals(WcpsConstants.MSG_IMAGE_CRSDOMAIN)) {
                MetadataScalarExpr metadataScalarExpr = new MetadataScalarExpr(node, wcpsXmlQueryParsingService);
                imageCrsDomainBounds = metadataScalarExpr.toAbstractSyntax();
            } else {
                if (lowerBound == null) {
                    lowerBound = new NumericScalarExpr(node, wcpsXmlQueryParsingService);
                } else if (upperBound == null) {
                    upperBound = new NumericScalarExpr(node, wcpsXmlQueryParsingService);
                } else {
                    throw new WCPSException(ExceptionCode.UnsupportedCombination,
                            "Unknown node in AxisIterator: '" + nodeName + "'.");
                }
            }

            node = node.getNextSibling();
            while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                node = node.getNextSibling();
            }
        }
    }

    @Override
    public String toAbstractSyntax() {
        // e.g: $first x(51:150)
        String result = "$" + iteratorVar + " " + axis.getName();
        if (imageCrsDomainBounds != null) {
            result = result + "(" + imageCrsDomainBounds + ")";
        } else {
            // lowerBound and upperBound is numeric
            result = result + "(" + lowerBound.toAbstractSyntax() + RASQL_BOUND_SEPARATION + upperBound.toAbstractSyntax() + ")";
        }

        return result;
    }

    /**
     * Return the Higher bound for the axis iterator. This only works for
     * constant expressions. TODO: implement arbitrary expressions.
     *
     * @return BigInteger
     */
    public BigInteger getUpperBound() {
        return new BigInteger(upperBound.toAbstractSyntax());
    }

    /**
     * Return the Lower bound for the axis iterator. This only works for
     * constant expressions. TODO: implement arbitrary expressions.
     *
     * @return BIgInteger
     */
    public BigInteger getLowerBound() {
        return new BigInteger(lowerBound.toAbstractSyntax());
    }

    /* Returns the m-interval that this axis iterates over in a string form */
    public String getInterval() {
        return lowerBound.toAbstractSyntax() + ":" + upperBound.toAbstractSyntax();
    }
}
