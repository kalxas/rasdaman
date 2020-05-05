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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.repository.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.Style;
import org.rasdaman.repository.interfaces.LayerRepository;
import org.rasdaman.repository.interfaces.StyleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import petascope.exceptions.PetascopeException;

/**
 *
 * Service class to access to WMS repository (data access class) in database
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
@Transactional
public class WMSRepostioryService {

    @Autowired
    private LayerRepository layerRepository;
    @Autowired
    private StyleRepository styleRepository;

    // NOTE: for migration, Hibernate caches the object in first-level cache internally
    // and recheck everytime a new entity is saved, then with thousands of cached objects for nothing
    // it will slow significantly the speed of next saving coverage, then it must be clear this cache.        
    // As targetEntityManagerFactory is set with Primary in bean application of migration application, no need to specify the unitName=target for this PersistenceContext
    @PersistenceContext
    private EntityManager entityManager;   

    private static final Logger log = LoggerFactory.getLogger(WMSRepostioryService.class);

    // Cache all the metadata for WMS layers
    public static final Map<String, Layer> layersCacheMap = new ConcurrentSkipListMap<>();
    
    /**
     * Check if a layer already exists from local loaded cache map
     */
    public boolean isInLocalCache(String layerName) throws PetascopeException {
        if (layersCacheMap.isEmpty()) {
            for (String layerNameTmp : this.readAllLocalLayerNames()) {
                this.readLayerByNameFromCache(layerNameTmp);
            }
        }
        
        if (!layersCacheMap.containsKey(layerName)) {
            return false;
        }
        
        return true;
    }

    /**
     *
     * Read persisted OwsServiceMetadata from cache. NOTE: only used when read
     * layer's metadata to GetMap, not for updating or deleting as it will have
     * error in Hibernate (Constraint violation with cached object)
     *
     * @param layerName
     * @return
     */
    public Layer readLayerByNameFromCache(String layerName) throws PetascopeException {
        // Check if layer already cached        
        Layer layer = layersCacheMap.get(layerName);
        if (layer == null) {
            layer = this.readLayerByNameFromDatabase(layerName);
        }

        return layer;
    }

    /**
     *
     * Read persisted OwsServiceMetadata from database. NOTE: used only when
     * update/insert/delete layer.
     *
     * @param layerName
     * @return
     */
    public Layer readLayerByNameFromDatabase(String layerName) throws PetascopeException {
        
        // This happens when Petascope starts and user sends a WMS GetMap query to a coverage instead of WMS GetCapabilities
        if (layersCacheMap.isEmpty()) {
            this.readAllLayers();
        }

        Layer layer = this.layerRepository.findOneByName(layerName);
        if (layer == null) {
            return null;
        }

        // put to cache        
        layersCacheMap.put(layerName, layer);

        log.debug("WMS Layer: " + layerName + " is read from database.");

        return layer;
    }

    /**
     *
     * Read all persisted WMS layers from database
     *
     * @return
     */
    public List<Layer> readAllLayers() throws PetascopeException {

        // Read all layers from database
        List<Layer> layers = this.readAllLocalLayers();

        return layers;
    }


     /**
     * This one should return only local layer of this node and not contain any remote layers.
     */
    public List<Layer> readAllLocalLayers() throws PetascopeException {
        List<Layer> layers = new ArrayList<>();
        if (layersCacheMap.isEmpty()) {
            for (Layer layer : this.layerRepository.findAll()) {
                layers.add(layer);
                layersCacheMap.put(layer.getName(), layer);
            }

            log.debug("Read all persistent WMS layers from database.");
        } else {
            layers = new ArrayList<>(layersCacheMap.values());
        }

        return layers;
    }

    /**
     * Save a WMS Layer object to persistent database
     */
    public void saveLayer(Layer layer) {
        this.layerRepository.save(layer);

        // add to WMS layers cache if it does not exist or update the existing one        
        layersCacheMap.put(layer.getName(), layer);
        
        entityManager.flush();
        entityManager.clear();

        log.debug("WMS Layer: " + layer.getName() + " is persisted to database.");
    }

    /**
     * Delete a WMS Layer object to persistent database
     */
    public void deleteLayer(Layer layer) {
        this.layerRepository.delete(layer);
        // remove layer from cache
        layersCacheMap.remove(layer.getName());

        entityManager.flush();
        entityManager.clear();

        log.debug("WMS Layer: " + layer.getName() + " is removed from database.");
    }

    /**
     * Delete a WMS Style object to persistent database
     */
    public void saveStyle(Style style) {
        this.styleRepository.save(style);

        entityManager.flush();
        entityManager.clear();

        log.debug("WMS Style: " + style.getName() + " is persited to database.");
    }

    /**
     * Delete a WMS Style object to persistent database
     */
    public void deleteStyle(Style style) {

        this.styleRepository.delete(style);

        entityManager.flush();
        entityManager.clear();

        log.debug("WMS Style: " + style.getName() + " is removed from database.");
    }

    // For migration only
    
    /**
     * Check if layer name already migrated in new database
     */
    public boolean layerNameExist(String legacyWMSLayerName) {
        Layer layer = this.layerRepository.findOneByName(legacyWMSLayerName);

        return layer != null;
    }
    
    /**
     * Fetch all the layer names from wms13 layer table
     */
    public List<String> readAllLocalLayerNames() {
        List<String> layerNames = layerRepository.readAllLayerNames();
        
        return layerNames;
    }
}
