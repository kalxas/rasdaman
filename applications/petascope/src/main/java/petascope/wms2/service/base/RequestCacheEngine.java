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

import org.jetbrains.annotations.NotNull;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.servlet.WMSGetRequest;

import java.util.*;

/**
 * Class representing a service that caches requests and their responses
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RequestCacheEngine {

    /**
     * Constructor for the class
     *
     * @param maxSizeOfCache the maximum size of the cache in bytes
     */
    public RequestCacheEngine(long maxSizeOfCache) {
        this.maxSizeOfCache = maxSizeOfCache;
        currentSize = 0;
        cache = new HashMap<WMSGetRequest, CachedResponse>((int) (maxSizeOfCache / MEDIAN_SIZE_OF_REQUEST_IN_BYTES));
    }

    /**
     * Returns true if there is a cached response for this request
     *
     * @param request the request to be checked for
     * @return true if there is a cached response, false otherwise
     */
    public boolean hasResponse(@NotNull WMSGetRequest request) {
        synchronized (cache) {
            return cache.containsKey(request);
        }
    }

    /**
     * Returns a cached response for a given request. If the request was not cached an exception will be thrown
     *
     * @param request the request for which the response should be returned
     * @return the cached response
     */
    public Response getResponse(@NotNull WMSGetRequest request) {
        synchronized (cache) {
            CachedResponse response = cache.get(request);
            if (response == null) {
                throw new IllegalArgumentException("No cached response was found for this request " + request);
            }
            return response.getResponse();
        }
    }

    /**
     * Adds a response to the cache
     *
     * @param request  the request to be added
     * @param response the response for the given request
     * @throws WMSException
     */
    public void addCachedResponse(@NotNull WMSGetRequest request, @NotNull Response response) throws WMSException {
        checkSize(response.toBytes().length);
        synchronized (cache) {
            cache.put(request, new CachedResponse(response));
        }

    }

    /**
     * Checks if more can be added for the cache and if it is full it removes a part of it
     *
     * @param sizeToBeAdded the sizes to be added to the cache
     */
    private void checkSize(long sizeToBeAdded) {
        if (sizeToBeAdded + currentSize > maxSizeOfCache) {
            synchronized (cache) {
                List<Map.Entry<WMSGetRequest, CachedResponse>> sortedMap = sortByValue(cache);
                for (Map.Entry<WMSGetRequest, CachedResponse> entry : sortedMap) {
                    if ((currentSize / (double) maxSizeOfCache) > PERCENT_TO_DELETE_WHEN_FULL) {
                        return;
                    }
                    CachedResponse resp = cache.remove(entry.getKey());
                    try {
                        currentSize -= resp.getSize();
                    } catch (WMSException e) {
                        //we do not want to stop the size check so continue even if there was an error
                        currentSize -= MEDIAN_SIZE_OF_REQUEST_IN_BYTES;
                    }
                }
            }
        }
    }


    /**
     * Sorts a map based on the ordering of its values and returns a list of the sorted iterms
     *
     * @param map the map to be sorted
     * @param <K> the type of the key of the map
     * @param <V> the type of the value of the map
     * @return the sorted map
     */
    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
            new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        return list;
    }


    @NotNull
    private final Map<WMSGetRequest, CachedResponse> cache;
    private final long maxSizeOfCache;
    private long currentSize;
    private final static long MEDIAN_SIZE_OF_REQUEST_IN_BYTES = 200l * 1000l; //200 kb
    private final static double PERCENT_TO_DELETE_WHEN_FULL = 0.25d;
}
