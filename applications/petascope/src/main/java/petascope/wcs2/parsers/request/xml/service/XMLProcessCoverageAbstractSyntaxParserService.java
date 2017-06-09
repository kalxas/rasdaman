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
package petascope.wcs2.parsers.request.xml.service;

import nu.xom.Element;
import nu.xom.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.LABEL_PROCESSCOVERAGE_REQUEST;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.LABEL_WCPS_QUERY;
import static petascope.core.XMLSymbols.LABEL_WCPS_ABSTRACT_SYNTAX;

/**
 * Parse a ProcessCoverages from request body in XML to map of keys, values as
 * KVP request
 *
 * NOTE: Use the ProcessCoverages from this example:
 * https://ies-svn.jrc.ec.europa.eu/attachments/download/1939/%5BDOC17%5D_TechnicalGuidance_Download_Services_WCS_v1.0rc2.pdf,
 * e.g:
 *
 * <?xml version="1.0" encoding="UTF-8"?>
 * <ProcessCoveragesRequest service="WCS" version="2.0.1"
 * xmlns="http://www.opengis.net/wcps/1.0
 * http://schemas.opengis.net/wcps/1.0/wcpsProcessCoverages.xsd">
 * <query>
 * <abstractSyntax>
 * for c in (glasgow_bron_th) return encode (
 * c[E:"http://www.opengis.net/def/crs/EPSG/0/27700"(260000),
 * N:"http://www.opengis.net/def/crs/EPSG/0/27700"(665000)], "csv" )
 * </abstractSyntax>
 * </query>
 * </ProcessCoveragesRequest>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class XMLProcessCoverageAbstractSyntaxParserService implements IXMLProcessCoverageParserService {

    @Override
    public boolean canParse(Element rootElement) {
        Elements childElements = rootElement.getChildElements();
        // Iterate all the child elements
        for (int i = 0; i < childElements.size(); i++) {
            Element childElement = childElements.get(i);
            String elementName = childElement.getLocalName();
            // There is a <abstractSyntax> element, so it can parse
            if (elementName.equals(XMLSymbols.LABEL_WCPS_ABSTRACT_SYNTAX)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String parseXMLRequest(Element rootElement) throws WCSException {       
        
        if (!rootElement.getLocalName().equals(LABEL_PROCESSCOVERAGE_REQUEST)) {
            throw new WCSException(ExceptionCode.InvalidRequest, "A WCPS request in abstractSyntax must specify one " + LABEL_PROCESSCOVERAGE_REQUEST + " element.");
        }
        
        Elements queryElements = rootElement.getChildElements();
        if (queryElements.size() > 0) {
            throw new WCSException(ExceptionCode.InvalidRequest, LABEL_PROCESSCOVERAGE_REQUEST + " element should contain only one " + LABEL_WCPS_QUERY + " element.");
        }
        
        Elements abstractSyntaxElements = queryElements.get(0).getChildElements();
        if (abstractSyntaxElements.size() > 0) {
            throw new WCSException(ExceptionCode.InvalidRequest, LABEL_WCPS_QUERY + " element should contain only one " + LABEL_WCPS_ABSTRACT_SYNTAX + " element.");
        }
        
        String wcpsQuery = XMLUtil.getText(abstractSyntaxElements.get(0));        

        return wcpsQuery;
    }
}
