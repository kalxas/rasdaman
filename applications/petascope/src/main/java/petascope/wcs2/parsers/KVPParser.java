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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.parsers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import static petascope.util.KVPSymbols.*;
import petascope.util.ListUtil;
import petascope.wcs2.handlers.RequestHandler;

/**
 * An abstract superclass for XML/POST protocol binding extensions, which provides some
 * convenience methods to concrete implementations.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public abstract class KVPParser<T extends Request> extends AbstractRequestParser<T> {

    private static Logger log = LoggerFactory.getLogger(KVPParser.class);

    @Override
    public boolean canParse(HTTPRequest request) {
        return request.getRequestString() != null && !request.getRequestString().startsWith("<")
                && request.getRequestString().contains(getOperationName());
    }

    protected String get(String key, Map<String, List<String>> m) {
        if (m.containsKey(key)) {
            return m.get(key).get(0);
        } else {
            return null;
        }
    }

    /**
     * Checks for requirement 9 in OGC 09-147r1
     *
     * @param m
     * @param keys KVP keys that the operation supports
     * @throws WCSException thrown when the request doesn't comply with the
     * KVP syntax
     */
    protected void checkEncodingSyntax(Map<String, List<String>> m, String... keys) throws WCSException {
        List<String> possibleKeys = ListUtil.toList(keys);
        Set<String> requestKeys = m.keySet();

        String request = get(KEY_REQUEST, m);
        if (!RequestHandler.GET_CAPABILITIES.equals(request)) {
            String version = get(KEY_VERSION, m);
            if (version == null || !version.matches(BaseRequest.VERSION)) {
                log.error("Version = " + version);
                throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(KEY_VERSION));
            }
        }
        for (String k : requestKeys) {
            if (k.equals(KEY_REQUEST) || k.equals(KEY_SERVICE) || k.equals(KEY_VERSION)) {
                if (m.get(k).size() > 1) {
                    throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(k));
                }
            }
            if (k.equals(KEY_REQUEST) || k.equals(KEY_SERVICE) || (k.startsWith(KEY_SUBSET)
                    && getOperationName().equals(RequestHandler.GET_COVERAGE))) {
                continue;
            }
            if (!possibleKeys.contains(k)) {
                throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(k));
            }
        }

        checkValue(m, KEY_REQUEST, getOperationName());
        checkValue(m, KEY_SERVICE, BaseRequest.SERVICE);
    }

    private void checkValue(Map<String, List<String>> m, String k, String... vals) throws WCSException {
        String v = get(k, m);
        if (v == null || (!ListUtil.toList(vals).contains(v))) {
            throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(k));
        }
    }
}
