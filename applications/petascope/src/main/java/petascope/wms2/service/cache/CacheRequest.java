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
import petascope.wms2.service.base.Request;
import petascope.wms2.servlet.WMSGetRequest;

/**
 * Class representing a cache request. It is just a simple wrapper around the raw request
 * so that it respects the common service layout
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CacheRequest extends Request {

    /**
     * Constructor for the class
     *
     * @param request    the base request
     * @param rawRequest the raw request
     */
    protected CacheRequest(@NotNull Request request, WMSGetRequest rawRequest) {
        super(request);
        this.rawRequest = rawRequest;
    }

    /**
     * Returns the raw request
     *
     * @return the raw request
     */
    @NotNull
    public WMSGetRequest getRawRequest() {
        return rawRequest;
    }

    private final WMSGetRequest rawRequest;
}
