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

package petascope.wms2.orchestration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.rasdaman.RasdamanService;
import petascope.wms2.service.base.Controller;
import petascope.wms2.service.base.RequestCacheEngine;
import petascope.wms2.service.base.ServiceValidator;
import petascope.wms2.service.base.Validator;
import petascope.wms2.service.cache.CacheController;
import petascope.wms2.service.cache.CacheHandler;
import petascope.wms2.service.cache.CacheParser;
import petascope.wms2.service.deletewcslayer.DeleteLayerController;
import petascope.wms2.service.deletewcslayer.DeleteLayerHandler;
import petascope.wms2.service.deletewcslayer.DeleteLayerParser;
import petascope.wms2.service.getcapabilities.GetCapabilitiesController;
import petascope.wms2.service.getcapabilities.GetCapabilitiesHandler;
import petascope.wms2.service.getcapabilities.GetCapabilitiesParser;
import petascope.wms2.service.getcapabilities.GetCapabilitiesValidator;
import petascope.wms2.service.getmap.GetMapController;
import petascope.wms2.service.getmap.GetMapHandler;
import petascope.wms2.service.getmap.GetMapParser;
import petascope.wms2.service.getmap.GetMapValidator;
import petascope.wms2.service.insertstyle.InsertStyleController;
import petascope.wms2.service.insertstyle.InsertStyleHandler;
import petascope.wms2.service.insertstyle.InsertStyleParser;
import petascope.wms2.service.insertstyle.InsertStyleValidator;
import petascope.wms2.service.insertwcslayer.InsertWCSLayerController;
import petascope.wms2.service.insertwcslayer.InsertWCSLayerHandler;
import petascope.wms2.service.insertwcslayer.InsertWCSLayerParser;
import petascope.wms2.service.insertwcslayer.InsertWCSLayerValidator;
import petascope.wms2.servlet.WMSGetRequest;
import petascope.wms2.util.ConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A registry of controllers that can provide the correct controller for any raw request.
 * Each new controller should be registered here and exposed if you want it to handle typed requests
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
class ControllerRegistry {

    /**
     * Constructor for the class
     *
     * @param configManager                    the config manager
     * @param persistentMetadataObjectProvider the persistence metadata object provider
     * @param cacheEngine                      the cache engine where to keep the cached results
     * @param rasdamanService                  the rasdaman service
     */
    public ControllerRegistry(@NotNull ConfigManager configManager,
                              @NotNull PersistentMetadataObjectProvider persistentMetadataObjectProvider,
                              @NotNull RequestCacheEngine cacheEngine,
                              @NotNull RasdamanService rasdamanService) {
        controllers = new ArrayList<Controller>();
        registerCacheController(cacheEngine);
        registerGetCapabilitiesController(configManager, persistentMetadataObjectProvider);
        registerInsertWCSLayerController(configManager, persistentMetadataObjectProvider);
        registerGetMapController(configManager, persistentMetadataObjectProvider, rasdamanService, cacheEngine);
        registerInsertStyleController(configManager, persistentMetadataObjectProvider);
        registerDeleteLayerController(configManager, persistentMetadataObjectProvider);
    }

    /**
     * Registers the get map controller
     *
     * @param configManager                    the config manager
     * @param persistentMetadataObjectProvider the persistence provider
     * @param rasdamanService                  the rasdaman service
     * @param cacheEngine                      the cache engine
     */
    private void registerGetMapController(@NotNull ConfigManager configManager,
                                          @NotNull PersistentMetadataObjectProvider persistentMetadataObjectProvider,
                                          @NotNull RasdamanService rasdamanService,
                                          @NotNull RequestCacheEngine cacheEngine) {
        GetMapParser parser = new GetMapParser(persistentMetadataObjectProvider);
        GetMapHandler handler = new GetMapHandler(rasdamanService, cacheEngine);
        GetMapValidator validator = new GetMapValidator();
        ServiceValidator serviceValidator = new ServiceValidator(configManager.getVersion(), configManager.getServiceName());
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(validator);
        validators.add(serviceValidator);
        registerController(new GetMapController(parser, validators, handler));


    }

    private void registerDeleteLayerController(@NotNull ConfigManager configManager,
            @NotNull PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        DeleteLayerParser parser = new DeleteLayerParser(persistentMetadataObjectProvider);
        DeleteLayerHandler handler = new DeleteLayerHandler(persistentMetadataObjectProvider);
        ServiceValidator validator = new ServiceValidator(configManager.getVersion(), configManager.getServiceName());
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(validator);
        registerController(new DeleteLayerController(parser, validators, handler));
    }

    /**
     * Registers the get capabilities controller
     *
     * @param configManager                    the config manager
     * @param persistentMetadataObjectProvider the persistence metadata object provider
     */
    private void registerGetCapabilitiesController(@NotNull ConfigManager configManager, @NotNull PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        GetCapabilitiesParser parser = new GetCapabilitiesParser();
        GetCapabilitiesHandler handler = new GetCapabilitiesHandler(persistentMetadataObjectProvider, configManager);
        GetCapabilitiesValidator validator = new GetCapabilitiesValidator(persistentMetadataObjectProvider);
        ServiceValidator serviceValidator = new ServiceValidator(configManager.getVersion(), configManager.getServiceName());
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(validator);
        validators.add(serviceValidator);
        registerController(new GetCapabilitiesController(parser, validators, handler));
    }

    /**
     * Registers the insert style controller
     *
     * @param configManager                    the config manager
     * @param persistentMetadataObjectProvider the persistence metadata object provider
     */
    private void registerInsertStyleController(@NotNull ConfigManager configManager, @NotNull PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        InsertStyleParser parser = new InsertStyleParser();
        InsertStyleHandler handler = new InsertStyleHandler(persistentMetadataObjectProvider);
        InsertStyleValidator validator = new InsertStyleValidator();
        ServiceValidator serviceValidator = new ServiceValidator(configManager.getVersion(), configManager.getServiceName());
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(validator);
        validators.add(serviceValidator);
        registerController(new InsertStyleController(parser, validators, handler));
    }

    /**
     * Registers the insert wcs layer controller
     *
     * @param configManager the config manager
     */
    private void registerInsertWCSLayerController(@NotNull ConfigManager configManager, PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        InsertWCSLayerParser parser = new InsertWCSLayerParser();
        InsertWCSLayerHandler handler = new InsertWCSLayerHandler(persistentMetadataObjectProvider);
        InsertWCSLayerValidator validator = new InsertWCSLayerValidator();
        ServiceValidator serviceValidator = new ServiceValidator(configManager.getVersion(), configManager.getServiceName());
        List<Validator> validators = new ArrayList<Validator>();
        validators.add(validator);
        validators.add(serviceValidator);
        registerController(new InsertWCSLayerController(parser, validators, handler));
    }


    /**
     * Registers the cache controller
     *
     * @param cacheEngine the cache engine
     */
    private void registerCacheController(@NotNull RequestCacheEngine cacheEngine) {
        CacheParser parser = new CacheParser(cacheEngine);
        CacheHandler handler = new CacheHandler(cacheEngine);
        List<Validator> validators = new ArrayList<Validator>();
        registerController(new CacheController(parser, validators, handler));
    }

    /**
     * Returns a controller if one is found that can support the raw request given.
     * If none is found null is returned
     *
     * @param rawRequest the raw request to be handled
     * @return the controller that can handle the request
     */
    @Nullable
    public Controller getController(@NotNull WMSGetRequest rawRequest) {
        for (Controller controller : controllers) {
            if (controller.supports(rawRequest)) {
                return controller;
            }
        }
        return null;
    }

    /**
     * Registers a controller in the registry
     *
     * @param controller the controller to be registered
     */
    private void registerController(@NotNull Controller controller) {
        controllers.add(controller);
    }

    @NotNull
    private final List<Controller> controllers;
}
