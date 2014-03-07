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
package petascope.wcs2.parsers;

import nu.xom.Element;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.wcs2.handlers.RequestHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static petascope.util.XMLSymbols.*;
import static petascope.util.XMLUtil.collectAll;

/**
 * Parser for xml encoding of a processing extension request to the wcs service
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class XMLProcessCoverageParser extends XMLParser<ProcessCoverageRequest> {
    /**
     * Parses the given xml encoded request into a ProcessCoverageRequest object.
     *
     * @param request the xml request to be parsed
     * @return the resulting ProcessCoverageRequest object
     * @throws WCSException
     */
    @Override
    public ProcessCoverageRequest parse(HTTPRequest request) throws WCSException {
        Element root = parseInput(request.getRequestString());
        List<Element> queryElements = collectAll(root, PREFIX_PROCESS_COVERAGE, LABEL_PROCESSING_QUERY, CTX_PROCESS_COVERAGE);
        if (queryElements.size() != 1) {
            throw new WCSException(ExceptionCode.WCSPMissingQueryParameter, "Exactly one query xml element must be provided.");
        }
        List<Element> extraParameterElements = collectAll(root, PREFIX_PROCESS_COVERAGE, LABEL_PROCESSING_EXTRA_PARAMETER, CTX_PROCESS_COVERAGE);
        Map<Integer, String> extraParameters = new HashMap<Integer, String>(extraParameterElements.size());
        int cardinalPosition = 1;
        for (Element extraParamElement : extraParameterElements) {
            extraParameters.put(cardinalPosition, extraParamElement.getValue());
            cardinalPosition += 1;
        }
        return new ProcessCoverageRequest(queryElements.get(0).getValue(), "", extraParameters);
    }

    /**
     * Returns the operation name for this parser.
     *
     * @return the operation name of the parser
     */
    @Override
    public String getOperationName() {
        return RequestHandler.PROCESS_COVERAGE;
    }
}
