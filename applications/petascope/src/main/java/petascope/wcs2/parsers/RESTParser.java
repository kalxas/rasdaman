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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.HTTPRequest;
import petascope.wcs2.helpers.rest.RESTUrl;

/**
 * Parser class that processes requests using the REST interface.
 *
 * @param <T>
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 */
public abstract class RESTParser<T extends Request> extends AbstractRequestParser<T> {

    public static final String RANGE_SEPARATOR = ":";
    public static final String ENUMERATOR_SEPARATOR = ",";
    private static Logger log = LoggerFactory.getLogger(RESTParser.class);

    /**
     * Implementation of the AbstractRequestParser interface The RESTParser can
     * only parse the request if it has no query string attached to it and the
     * operation name is container in the url
     s
     * @param request the request string
     * @return canParse true if it can parse the request, false otherwise
     */
    public boolean canParse(HTTPRequest request) {
        RESTUrl rUrl = new RESTUrl(request.getUrlPath());
        Boolean canParse = true;
        if (!(request.getQueryString() == null || request.getQueryString().isEmpty())) {
            canParse = false;
        } else if (!rUrl.existsKey(this.getOperationName())) {
            canParse = false;
        }
        log.trace("RESTParser<{}> {} parse the request", getOperationName(), canParse ? "can" : "cannot");
        return canParse;
    }
}
