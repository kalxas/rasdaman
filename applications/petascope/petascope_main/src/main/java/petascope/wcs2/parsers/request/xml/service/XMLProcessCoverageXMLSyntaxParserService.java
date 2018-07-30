/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.parsers.request.xml.service;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import nu.xom.Element;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.LABEL_PROCESSCOVERAGE_REQUEST;
import static petascope.core.XMLSymbols.LABEL_WCPS_QUERY;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WCSException;
import petascope.wcps.xml.handler.WCPSXmlQueryParsingService;

/**
 * Parse the WCPS query in XML syntax which has been used since WCPS 1.0 legacy.
 * NOTE: Use the schema from: http://schemas.opengis.net/wcps/1.0/, e.g:
 *
 * <ProcessCoveragesRequest xmlns="http://www.opengis.net/wcps/1.0" service="WCPS" version="1.0.0">
 * <query>
 * <xmlSyntax>
 * <coverageIterator>
 * <iteratorVar>c</iteratorVar>
 * <coverageName>test_rgb</coverageName>
 * </coverageIterator>
 * <encode store="false">
 * <trim>
 * <coverage>c</coverage>
 * <axis>i</axis>
 * <lowerBound>
 * <numericConstant>0</numericConstant>
 * </lowerBound>
 * <upperBound>
 * <numericConstant>100</numericConstant>
 * </upperBound>
 * <axis>j</axis>
 * <lowerBound>
 * <numericConstant>0</numericConstant>
 * </lowerBound>
 * <upperBound>
 * <numericConstant>100</numericConstant>
 * </upperBound>
 * </trim>
 * <format>png</format>
 * <extraParameters>nodata=0</extraParameters>
 * </encode>
 * </xmlSyntax>
 * </query>
 * </ProcessCoveragesRequest>
 *
 * which is translated to WCPS abstract syntax:
 *
 * for c in (test_rgb) return encode(c[i(0:100), j(0:100), "png", "nodata=0")
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class XMLProcessCoverageXMLSyntaxParserService implements IXMLProcessCoverageParserService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(XMLProcessCoverageXMLSyntaxParserService.class);

    @Autowired
    WCPSXmlQueryParsingService wcpsXmlQueryParsingService;

    @Override
    public boolean canParse(Element rootElement) throws WCSException {
        String rootElementName = rootElement.getLocalName();

        if (rootElementName.equals(XMLSymbols.LABEL_WCPS_ROOT_ABSTRACT_SYNTAX)) {
            // First element must be <query>
            Element queryElement = rootElement.getFirstChildElement(XMLSymbols.LABEL_WCPS_QUERY, XMLSymbols.NAMESPACE_WCPS);
            if (queryElement == null) {
                throw new WCSException(ExceptionCode.InvalidRequest, LABEL_PROCESSCOVERAGE_REQUEST + " element should contain only one '" + LABEL_WCPS_QUERY + "' element.");
            }

            // Next element must be <xmlSyntax>
            Element xmlSyntaxtElement = queryElement.getFirstChildElement(XMLSymbols.LABEL_WCPS_XML_SYNTAX, XMLSymbols.NAMESPACE_WCPS);
            if (xmlSyntaxtElement == null) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public String parseXMLRequest(Element rootElement) throws WCSException, WCPSException {
        // The WCPS query is just in xml syntax, so need to translate to abstract syntax
        // by using the WCPS 1.0 legacy parser        
        String xmlWcpsQuery = rootElement.toXML();
        try {
            wcpsXmlQueryParsingService.parse(xmlWcpsQuery);
        } catch (PetascopeException | SecoreException | ParserConfigurationException | SAXException | IOException ex) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Cannot parse the WCPS in XML syntax with error '" + ex.getMessage() + "'.", ex);
        }
        String abstractWcpsQuery = wcpsXmlQueryParsingService.toAbstractSyntax();
        log.debug("Parsed an abstract WCPS query from XML syntax " + abstractWcpsQuery);

        return abstractWcpsQuery;
    }
}
