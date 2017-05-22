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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.KVPSymbols;
import static petascope.util.KVPSymbols.*;
import petascope.util.ListUtil;
import petascope.wcs2.handlers.RequestHandler;

/**
 * An abstract superclass for XML/POST protocol binding extensions, which provides some
 * convenience methods to concrete implementations.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 *
 * @param <T>
 */
public abstract class KVPParser<T extends Request> extends AbstractRequestParser<T> {

    private static Logger log = LoggerFactory.getLogger(KVPParser.class);

    @Override
    public boolean canParse(HTTPRequest request) {
        boolean canParse = request.getRequestString() != null
                           && !request.getRequestString().startsWith("<")
                           && request.getRequestString().contains(getOperationName());
        log.trace("KVPParser<{}> {} parse the request", getOperationName(), canParse ? "can" : "cannot");
        return canParse;
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
     * @throws WCSException thrown when the request doesn't comply with the KVP syntax
     */
    protected void checkEncodingSyntax(Map<String, List<String>> m, String... keys) throws WCSException {
        List<String> possibleKeys = ListUtil.toList(keys);
        Set<String> requestKeys = m.keySet();

        String request = get(KEY_REQUEST, m);
        if (!RequestHandler.GET_CAPABILITIES.equals(request)) {
            String version = get(KEY_VERSION, m);
            if (version == null) {
                version = get(KEY_ACCEPTVERSIONS, m);
            }
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

        // NOTE: According to 06-121r9_OGC (7.3.3) Sections parameter can only have these values (ServiceIdentification, ServiceProvider, OperationsMetadata, Contents, Languages, All)
        // TODO: as this "sections" parameter with GetCapabilities is optional, then we only check if the input parameters are valid and will return the XML elements as normal GetCapabilitie request
        // To support this parameter completely, it will need to parse the specified input values of this paramater and return the XML elements accordingly.
        // e.g: http://localhost:8080/rasdaman/ows?service=WCS&version=2.0.1&request=GetCapabilities&sections=ServiceIdentification,ServiceProvider
        // and in the response XML of GetCapabilities, it will contain only 2 specified elements, instead of returning with all elements.
        // <wcs:Capabilities>
        //    <ows:ServiceIdentification>...</ows:ServiceIdentification>
        //    <ows:ServiceProvider>...</ows:ServiceProvider>
        // </wcs:Capabilities>
        if (m.containsKey(KEY_SECTIONS)) {
            String[] validCases = { KVPSymbols.VALUE_SECTIONS_SERVICE_IDENTIFICATION,
                                    KVPSymbols.VALUE_SECTIONS_SERVICE_PROVIDER,
                                    KVPSymbols.VALUE_SECTIONS_OPERATIONS_METADATA,
                                    KVPSymbols.VALUE_SECTIONS_CONTENTS,
                                    KVPSymbols.VALUE_SECTIONS_LANGUAGES,
                                    KVPSymbols.VALUE_SECTIONS_ALL
                                  };
            checkValue(m, KEY_SECTIONS, validCases);
        }
    }

    /**
    * This function will check value for argument in list of valid cases
    * @param m: List arguments
    * @param k: Argument's key which is needed to check
    * @param vals: Argument's list of valid cases
    * @throws WCSException
    */
    private void checkValue(Map<String, List<String>> m, String k, String... vals) throws WCSException {
        String v = get(k, m);
        if (v == null || (!ListUtil.toList(vals).contains(v))) {
            throw new WCSException(ExceptionCode.InvalidEncodingSyntax.locator(k));
        }
    }
}