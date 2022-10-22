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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.domain.cis.Wgs84BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.PetascopeController;
import petascope.core.KVPSymbols;
import petascope.core.Pair;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WMSException;
import petascope.wms.handlers.kvp.KVPWMSGetMapHandler;

/**
 * Only GetMap request needs to cache the response if it is not exception.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class WMSGetMapCachingService {
    
    private static Logger log = LoggerFactory.getLogger(WMSGetMapCachingService.class);
    
    @Autowired
    private KVPWMSGetMapHandler kvpWMSGetMapHandler;
    @Autowired
    private PetascopeController petascopeController;

    private long totalCachedSize = 0;
    public static final Map<String, Pair<Wgs84BoundingBox, Response>> responseCachingMap = new ConcurrentHashMap<>();

    public WMSGetMapCachingService() {

    }

    /**
     * Add a successful response to cache if the GetMap query string is not
     * cached yet.
     *
     * @param queryString
     * @param response
     */
    public void addResponseToCache(String queryString, Wgs84BoundingBox wgs84BBox, Response response) {
        byte[] bytes = response.getDatas().get(0);
        // Check if cache's size is greater than the maximum configuration
        if (!(totalCachedSize + bytes.length <= ConfigManager.MAX_WMS_CACHE_SIZE)) {
            // Remove all the less received cached objects until there is enough bytes for the new cache response    
            Iterator<Map.Entry<String, Pair<Wgs84BoundingBox, Response>>> iterator = responseCachingMap.entrySet().iterator();
            long removedBytes = 0;
            while (iterator.hasNext()) {
                removedBytes += iterator.next().getValue().snd.getDatas().get(0).length;
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
            this.responseCachingMap.put(queryString, new Pair<> (wgs84BBox, response));
        }

    }
    
    /**
     * Any cached requests intersecting with an input WGS84 will need to be updated results from rasdaman
     */
    public void updateCachesIntersectingWGS84BBox(String inputLayerName, Wgs84BoundingBox inputWGS84BBox) throws PetascopeException, WMSException, Exception {
        Iterator<Map.Entry<String, Pair<Wgs84BoundingBox, Response>>> iterator = responseCachingMap.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, Pair<Wgs84BoundingBox, Response>> entry = iterator.next();
            Wgs84BoundingBox wgs84BBox = entry.getValue().fst;
            String request = entry.getKey();
            
            if (wgs84BBox.intersects(inputWGS84BBox)) {
                String[] keyValues = request.split("&");

                if (this.contain(keyValues, inputLayerName)) {
                    // A GetMap request which contains the removed layerName, then remove it from cache
                    iterator.remove();
                }
            }
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
        return this.responseCachingMap.get(queryString).snd;
    }
    
    /**
     * When a layer is removed from database, it is also needed to remove the layer from cache
     * NOTE: all GetMap request containing layerName will be removed from cache.
     * @param layerName 
     */
    public void removeLayerGetMapInCache(String layerName) {
        Iterator<Map.Entry<String, Pair<Wgs84BoundingBox, Response>>> iterator = responseCachingMap.entrySet().iterator();
        while (iterator.hasNext()) {
            String requestQuery = iterator.next().getKey();
            String[] keyValues = requestQuery.split("&");
            
            if (this.contain(keyValues, layerName)) {
                // A GetMap request which contains the removed layerName, then remove it from cache
                iterator.remove();
            }
            
        }
    }
    
    /**
     * Check if a cached WMS / WMTS request contains a layername 
     */
    private boolean contain(String[] keyValues, String layerName) {
        for (String keyValue : keyValues) {
            // e.g. styles= or layers=test_wms
            String[] tmp = keyValue.split("=");
            String key = tmp[0];
            
            if (tmp.length == 2) {
                String value = tmp[1];

                if (key.equalsIgnoreCase(KVPSymbols.KEY_WMS_LAYERS)
                    || key.equalsIgnoreCase(KVPSymbols.KEY_WMTS_RASDAMAN_INTERNAL_FOR_GETMAP_REQUEST_PYRAMID_COVERAGE_ID)) {
                    if (value.contains(layerName)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    
    /**
     * When a style is removed from database, it is also needed to remove the layers's style name from cache.
     * NOTE: All GetMap requests containing the input layerName and styleName will be removed from cache.
     * @param layerName 
     * @param styleName 
     */
    public void removeStyleGetMapInCache(String layerName, String styleName) {
        Iterator<Map.Entry<String, Pair<Wgs84BoundingBox, Response>>> iterator = responseCachingMap.entrySet().iterator();
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
            
            if (styleName == null && layers != null && layers.contains(layerName)) {
                // Remove GetMap request without style defined
                iterator.remove();
            } else if (layers != null && layers.contains(layerName) && styles != null && styles.contains(styleName)) {
                // Remove the cache for this layer's style
                iterator.remove();
            }
        }
    }
}
