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
import petascope.wms2.service.base.Handler;
import petascope.wms2.service.base.RequestCacheEngine;
import petascope.wms2.service.base.Response;
import petascope.wms2.service.exception.error.WMSException;

/**
 * Handler for the cached responses
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CacheHandler implements Handler<CacheRequest, Response> {


    /**
     * Constructor for the class
     *
     * @param cacheEngine the cache engine from which to retrieve the responses
     */
    public CacheHandler(RequestCacheEngine cacheEngine) {
        this.cacheEngine = cacheEngine;
    }

    /**
     * Handles the request and delivers a response
     *
     * @param request the typed wms request
     * @return the response corresponding to the request
     * @throws WMSException
     */
    @NotNull
    @Override
    public Response handle(@NotNull CacheRequest request) throws WMSException {
        return cacheEngine.getResponse(request.getRawRequest());
    }

    private final RequestCacheEngine cacheEngine;
}
