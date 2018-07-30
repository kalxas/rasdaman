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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import petascope.core.XMLSymbols;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.ListUtil;
import petascope.util.XMLUtil;

/**
 * Service class to parse and translate WCPS query in XML syntax to abstract
 * syntax e.g:
 * <ProcessCoveragesRequest xmlns="http://www.opengis.net/wcps/1.0" service="WCPS" version="1.0.0">
 * <query>
 * <xmlSyntax>
 * <coverageIterator>
 * <iteratorVar>c</iteratorVar>
 * <coverageName>test_rgb</coverageName>
 * </coverageIterator>
 * </xmlSyntax>
 * </query>
 * </ProcessCoveragesRequest>
 *
 * to for c in (test_rgb)
 *
 * @author Andrei Aiordachioaie
 */
@Service
public class WCPSXmlQueryParsingService extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(WCPSXmlQueryParsingService.class);

    private List<CoverageIterator> coverageIterators;
    // If a WCPS query contains a where expression
    private BooleanScalarExpr whereExpression;
    private IRasNode coverageExpr;
    
    /**
     * Validate if the input WCPS query is XML syntax, e.g:
     *
     * <ProcessCoveragesRequest xmlns="http://www.opengis.net/wcps/1.0" service="WCPS" version="1.0.0">
     * <query>
     * <xmlSyntax>
     * ...
     * </xmlSyntax>
     * </query>
     * </ProcessCoveragesRequest>
     *
     * @param child
     */
    public Node validate(Node child) {
        if (!child.getNodeName().equals(XMLSymbols.LABEL_WCPS_ROOT_XML_SYNTAX)) {
            throw new WCPSException("Could not find element: ;" + XMLSymbols.LABEL_WCPS_ROOT_XML_SYNTAX  + "'.");
        }

        // child is now the node <query>.
        child = child.getFirstChild();
        if (!child.getNodeName().equals(XMLSymbols.LABEL_WCPS_QUERY)) {
            throw new WCPSException("Could not find element: '" + XMLSymbols.LABEL_WCPS_QUERY + "'.");
        }

        // child is now the node <xmlSyntax>
        child = child.getFirstChild();
        if (!child.getNodeName().equals(XMLSymbols.LABEL_WCPS_XML_SYNTAX)) {
            throw new WCPSException("Could not find element: '" + XMLSymbols.LABEL_WCPS_XML_SYNTAX + "'.");
        }

        log.debug("Found XML Syntax query.");

        // Return the first inner element to parse
        return child.getFirstChild();
    }

    /**
     * Check if iterator is defined in for clause (<coverageIterator/> element)
     * e.g:
     * <coverageIterator>
     * <iteratorVar>c</iteratorVar>
     * <coverageName>test_mr</coverageName>
     * </coverageIterator>
     * ...
     * <encode store="false">
     * <coverage>c</coverage>
     * </encode>
     *
     * then c is defined as coverage iterator.
     *
     * @param iteratorName
     * @return
     */
    public Boolean isIteratorDefined(String iteratorName) {
        Iterator<CoverageIterator> it = coverageIterators.iterator();
        while (it.hasNext()) {
            CoverageIterator tmp = it.next();
            if (iteratorName.equals(tmp.getIteratorName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Parse the WCPS query in XML Syntax to corresponding objects which will be
     * used to generate WCPS in abstract Syntax, e.g:
     *
     *
     * @param wcpsXMLQuery
     * @throws WCPSException
     * @throws PetascopeException
     * @throws SecoreException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public void parse(String wcpsXMLQuery) throws WCPSException, PetascopeException, SecoreException, ParserConfigurationException, SAXException, IOException {
        // NOTE: remove any spaces between elements (e.g: <x>...</x>    <y>...</y> to <x>...</x><y>...</y>)
        // or parser will parse spaces as text elements which is not correct behavior
        wcpsXMLQuery = XMLUtil.removeSpaceBetweenElements(wcpsXMLQuery);
        coverageIterators = new ArrayList<>();
        whereExpression = null;
        
        DocumentBuilder wcpsDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Node node = wcpsDocumentBuilder.parse(IOUtils.toInputStream(wcpsXMLQuery));
        log.debug("Processing XML Request: " + node.getNodeName());

        Node child = node.getFirstChild();
        // Validate before parsing also return the first inner element to parse
        child = this.validate(child);

        // Iterate all the elements of the input WCPS XML syntax
        while (child != null) {
            String nodeName = child.getNodeName();
            log.debug("The current node is: " + nodeName);

            if (nodeName.equals(WcpsConstants.MSG_COVERAGE_ITERATOR)) {
                // e.g: for c in (test_rgb)
//              <coverageIterator>
//                   <iteratorVar>c</iteratorVar> 
//                   <coverageName>test_rgb</coverageName>
//              </coverageIterator>

                coverageIterators.add(new CoverageIterator(child));
            } else if (nodeName.equals(WcpsConstants.MSG_WHERE)) {
                // e.g: where avg(c) > 30
//              <where>
//                    <booleanGreaterThan>
//                        <reduce>
//                            <avg>
//                                <coverage>c</coverage>
//                            </avg>
//                        </reduce>
//                        <numericConstant>30</numericConstant>
//                    </booleanGreaterThan>
//              </where>

                whereExpression = new BooleanScalarExpr(child.getFirstChild(), this);
            } else if (nodeName.equals(WcpsConstants.MSG_ENCODE)) {
                // e.g: encode(....)
//              <encode store="false">
//              </encode>

                EncodeDataExpr encode = new EncodeDataExpr(child, this);
                coverageExpr = encode;
            } else {
                // It has to be a scalar Expr, e.g: avg(c)
//                <reduce>
//                    <avg>
//                        <coverage>c</coverage>
//                    </avg>
//                </reduce>

                coverageExpr = new ScalarExpr(child, this);
            }

            child = child.getNextSibling();
        }
    }

    @Override
    public String toAbstractSyntax() {
        // First with for clause
        List<String> abstractCoverageIterators = new ArrayList<>();
        for (CoverageIterator coverageIterator : coverageIterators) {
            abstractCoverageIterators.add(coverageIterator.toAbstractSyntax());
        }

        // e.g: for c in (test_mr, test_rgb), d in (test_mr1, test_rgb1)
        String result = " for " + ListUtil.join(abstractCoverageIterators, ", ");
        // Then with the whereClause
        if (null != whereExpression) {
            result += " where " + whereExpression.toAbstractSyntax();
        }

        // Then with the return encode or scalar value
        result = result + " return ";
        result = result + coverageExpr.toAbstractSyntax();

        return result;
    }

    public List<CoverageIterator> getCoverageIterator() {
        return coverageIterators;
    }

}
