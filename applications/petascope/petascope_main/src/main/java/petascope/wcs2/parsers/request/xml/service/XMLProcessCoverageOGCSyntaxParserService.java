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
import petascope.util.XMLUtil;

/**
 * Parse a ProcessCoverages from request body in XML to map of keys, values as
 * KVP request
 *
 * NOTE: Use the OGC POST process syntax as here
 * https://portal.opengeospatial.org/files/08-059r4, e.g:
 *
 * <proc:ProcessCoverages>
 * <proc:query>
 * for $c in ( Scene1 ) return encode( $c.red + $1, "$2" )
 * </proc:query>
 * <proc:extraParameter>
 * 42
 * </proc:extraParameter>
 * <proc:extraParameter>
 * image/jp2
 * </proc:extraParameter>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class XMLProcessCoverageOGCSyntaxParserService implements IXMLProcessCoverageParserService {

    @Override
    public boolean canParse(Element rootElement) {
        // NOTE: the rootElement must be *ProcessCoverages* not *ProcessCoveragesRequest*
        String rootElementName = rootElement.getLocalName();
        if (rootElementName.equals(XMLSymbols.LABEL_WCPS_ROOT_OGC_ABSTRACT_SYNTAX)) {
            return true;
        }
        
        return false;
    }

    @Override
    public String parseXMLRequest(Element rootElement) throws WCSException {
        Elements childElements = rootElement.getChildElements();
        String abstractWcpsQuery = "";
        // Iterate all the child elements
        for (int i = 0; i < childElements.size(); i++) {
            Element childElement = childElements.get(i);
            String elementName = childElement.getLocalName();
            if (elementName.equals(XMLSymbols.LABEL_WCPS_QUERY)) {
                abstractWcpsQuery = XMLUtil.getText(childElement);
            } else if (elementName.equals(XMLSymbols.LABEL_WCPS_EXTRA_PARAMETER)) {
                // Replace the $i with the value from each extraParameter element
                // e.g: for c in (test_mr) return encode(c, "$1") and extraParameter is png ($1)
                String extraParameterValue = XMLUtil.getText(childElement);
                abstractWcpsQuery = abstractWcpsQuery.replace("$" + i, extraParameterValue);
            } else {
                throw new WCSException(ExceptionCode.InvalidRequest, "A ProcessCoverages request cannot contain this element, given: '" + elementName + "'.");
            }
        }

        return abstractWcpsQuery;
    }
}