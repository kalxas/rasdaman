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
import petascope.exceptions.WCSException;

/**
 * Interface class for XML Process Coverage parser to a WCPS query in abstract
 * syntax (e.g: for c in (test_mr) return encode(c, "png"))
 *
 * @author rasdaman
 */
public interface IXMLProcessCoverageParserService {
    /**
     * There are 3 types of WCPS POST requests and depend on the XML element to parse them correctly
     * @param rootElement
     * @return 
     * @throws petascope.exceptions.WCSException 
     */
    boolean canParse(Element rootElement) throws WCSException;
    
    /**
     * Parse a XML document to a WCPS query String
     * @param rootElement
     * @return
     * @throws WCSException 
     */
    String parseXMLRequest(Element rootElement) throws WCSException;
}
