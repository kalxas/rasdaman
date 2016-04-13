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

package petascope.wms2.service.cache;

import org.jetbrains.annotations.NotNull;
import petascope.wms2.service.base.Parser;
import petascope.wms2.service.base.RequestCacheEngine;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.servlet.WMSGetRequest;

/**
 * Parser for the caching mechanism. A request can be parsed if there is an entry in the
 * cache engine for it
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CacheParser extends Parser<CacheRequest> {


    /**
     * Constructor for the class
     *
     * @param requestCacheEngine the request cache engine
     */
    public CacheParser(RequestCacheEngine requestCacheEngine) {
        this.requestCacheEngine = requestCacheEngine;
    }

    /**
     * Returns true if the request can be returned from cache, false otherwise
     *
     * @param rawRequest the raw wms http request
     * @return true if it can be parsed, false otherwise
     */
    @Override
    public boolean canParse(@NotNull WMSGetRequest rawRequest) {
        return requestCacheEngine.hasResponse(rawRequest);
    }

    /**
     * Parses the request and returns a cache request
     *
     * @param rawRequest the raw wms http request
     * @return the cache request
     * @throws WMSException
     */
    @Override
    public CacheRequest parse(@NotNull WMSGetRequest rawRequest) throws WMSException {
        return new CacheRequest(parseBaseRequest(rawRequest), rawRequest);
    }

    private final RequestCacheEngine requestCacheEngine;
}
