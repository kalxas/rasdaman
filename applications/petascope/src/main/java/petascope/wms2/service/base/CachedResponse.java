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

package petascope.wms2.service.base;

import com.sun.istack.Nullable;
import org.jetbrains.annotations.NotNull;
import petascope.wms2.service.exception.error.WMSException;

/**
 * Class to represent a cached response
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
class CachedResponse implements Comparable {

    /**
     * Constructor for the class
     *
     * @param response the response to be cached
     */
    public CachedResponse(@NotNull Response response) {
        this.response = response;
    }

    /**
     * Returns the cached response
     *
     * @return the cached response
     */
    public Response getResponse() {
        accessCounter += 1;
        return response;
    }

    /**
     * The size of the cached response that this object is holding
     *
     * @return the size of the cached response in bytes
     * @throws WMSException
     */
    public int getSize() throws WMSException {
        return response.toBytes().length;
    }

    /**
     * Returns the number of accesses of the cached response.
     *
     * @return the access counter of the response
     */
    public long getAccessCounter() {
        return accessCounter;
    }

    /**
     * Compares this object with another one base on their size
     *
     * @param o the object to compare with
     * @return -1 if this object is smaller, 0 if equal, 1 otherwise
     */
    @Override
    public int compareTo(@Nullable Object o) {
        if (o == null || !(o instanceof CachedResponse)) {
            throw new IllegalArgumentException("Cannot compare to a null or different typed object");
        }
        CachedResponse resp = (CachedResponse) o;
        return Long.valueOf(getAccessCounter()).compareTo(resp.getAccessCounter());
    }

    private final Response response;
    private long accessCounter;
}
