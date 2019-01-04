/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wms.handlers.service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.rasdaman.config.ConfigManager;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;

/**
 * Only GetMap request needs to cache the response if it is not exception.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class WMSGetMapCachingService {

    private long totalCachedSize = 0;
    public static final Map<String, Response> responseCachingMap = new ConcurrentHashMap<>();

    public WMSGetMapCachingService() {

    }

    /**
     * Add a successful response to cache if the GetMap query string is not
     * cached yet.
     *
     * @param queryString
     * @param response
     */
    public void addResponseToCache(String queryString, Response response) {
        byte[] bytes = response.getDatas().get(0);
        // Check if cache's size is greater than the maximum configuration
        if (!(totalCachedSize + bytes.length <= ConfigManager.MAX_WMS_CACHE_SIZE)) {
            // Remove all the less received cached objects until there is enough bytes for the new cache response    
            Iterator<Map.Entry<String, Response>> iterator = responseCachingMap.entrySet().iterator();
            long removedBytes = 0;
            while (iterator.hasNext()) {
                removedBytes += iterator.next().getValue().getDatas().get(0).length;
                // Continue to remove older bytes
                if (removedBytes < bytes.length) {
                    iterator.remove();
                } else {
                    // removed enough
                    break;
                }
            }
        }
        // In case of the response is much bigger than the maximum of cache, no store anything.
        totalCachedSize += bytes.length;
        if (totalCachedSize <= ConfigManager.MAX_WMS_CACHE_SIZE) {
            // then add the new bytes[] to cached
            this.responseCachingMap.put(queryString, response);
        }

    }

    /**
     * Return the cached response to client instead of executing a Rasql query
     * which takes longer time.
     *
     * @param queryString
     * @return
     */
    public Response getResponseFromCache(String queryString) {
        return this.responseCachingMap.get(queryString);
    }
    
    /**
     * When a layer is removed from database, it is also needed to remove the layer from cache
     * NOTE: all GetMap request containing layerName will be removed from cache.
     * @param layerName 
     */
    public void removeLayerGetMapInCache(String layerName) {
        Iterator<Map.Entry<String, Response>> iterator = responseCachingMap.entrySet().iterator();
        while (iterator.hasNext()) {
            String requestQuery = iterator.next().getKey();
            String[] keyValues = requestQuery.split("&");
            
            for (String keyValue : keyValues) {
                String[] tmp = keyValue.split("=");
                if (tmp[0].equalsIgnoreCase(KVPSymbols.KEY_WMS_LAYERS)) {
                    if (tmp[1].contains(layerName)) {
                        // A GetMap request which contains the removed layerName, then remove it from cache
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }
    
    
    /**
     * When a style is removed from database, it is also needed to remove the layers's style name from cache.
     * NOTE: All GetMap requests containing the input layerName and styleName will be removed from cache.
     * @param layerName 
     * @param styleName 
     */
    public void removeStyleGetMapInCache(String layerName, String styleName) {
        Iterator<Map.Entry<String, Response>> iterator = responseCachingMap.entrySet().iterator();
        while (iterator.hasNext()) {
            String requestQuery = iterator.next().getKey();
            String[] keyValues = requestQuery.split("&");
            
            String layers = null;
            String styles = null;
            for (String keyValue : keyValues) {
                String[] tmp = keyValue.split("=");
                if (tmp[0].equalsIgnoreCase(KVPSymbols.KEY_WMS_LAYERS)) {
                    if (tmp.length > 1) {
                        layers = tmp[1];
                    }
                }
                if (tmp[0].equalsIgnoreCase(KVPSymbols.KEY_WMS_STYLES)) {
                    if (tmp.length > 1) {
                        styles = tmp[1];
                    }
                }         
                
                if (layers != null && styles != null) {
                    break;
                }
            }
            
            if (layers != null && layers.contains(layerName) && styles != null && styles.contains(styleName)) {
                // Remove the cache for this layer's style
                iterator.remove();
            }
        }
    }
}
