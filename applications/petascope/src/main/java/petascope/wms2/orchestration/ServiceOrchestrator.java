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

import com.sun.istack.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.base.Controller;
import petascope.wms2.service.base.Response;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInternalException;
import petascope.wms2.service.exception.error.WMSInvalidOperationRequestException;
import petascope.wms2.service.exception.response.ExceptionResponseFactory;
import petascope.wms2.servlet.WMSGetRequest;
import petascope.wms2.util.ConfigManager;

/**
 * Orchestrates the workflow in the WMS service and ensures that the system is running
 * correctly.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class ServiceOrchestrator {

    public ServiceOrchestrator(@Nullable String pathToConfigurationDirectory) {
        initializeService(pathToConfigurationDirectory);
        logger.info("Service was initialized successfully.");
    }

    /**
     * Handles a wms raw request coming from a front-end (for example a servlet)
     *
     * @param rawRequest the raw request to be handled
     * @return a response to the request
     */
    public Response handleWMSRequest(@NotNull WMSGetRequest rawRequest) {
        final long startTime = System.currentTimeMillis();
        checkVersionOfRequest(rawRequest);
        Controller controller = controllerRegistry.getController(rawRequest);
        logger.info("Controller {} selected to handle the request {}.", controller == null ? "DEFAULT" : controller, rawRequest);
        if (controller == null) {
            return ExceptionResponseFactory.getExceptionResponse(
                       new WMSInvalidOperationRequestException("We could not interpret your request. Make sure the request " +
                               "is WMS valid. This is what we received: " + rawRequest.toString()), null);
        }
        Response response = controller.getResponse(rawRequest);
        final long endTime = System.currentTimeMillis();
        logger.info("Request took {}", endTime - startTime);
        return response;
    }

    /**
     * Handles any exception that goes throw the service after it has been initialized
     *
     * @param e the exception to be handled
     * @return a byte response representing the exception
     */
    public byte[] handleExceptions(Exception e) {
        WMSException exception;
        if (e instanceof WMSException) {
            exception = (WMSException) e;
        } else {
            exception = new WMSInternalException(e);
        }
        return ExceptionResponseFactory.getExceptionResponse(exception, null).toBytes();
    }

    /**
     * Return the initialized metadata object provider to connect to WMS database
     * @return
     */
    public PersistentMetadataObjectProvider getPersistentProvider() {
        return this.persistentMetadataObjectProvider;
    }

    /**
     * Closes the service, useful to be called before your front-end closes so that we free any resources
     */
    public void close() {
        persistentMetadataObjectProvider.closePersistence();
    }

    /**
     * Initializes the service by instantiating all the needed providers and managers
     *
     * @param pathToConfigurationDirectory the path to the configuration directory of rasdaman
     */
    private void initializeService(@NotNull String pathToConfigurationDirectory) {
        ServiceInitializer initializer = new ServiceInitializer(pathToConfigurationDirectory);
        initializer.initializeSystem();
        configManager = initializer.getConfigManager();
        persistentMetadataObjectProvider = initializer.getPersistentMetadataObjectProvider();
        controllerRegistry = new ControllerRegistry(initializer.getConfigManager(),
                persistentMetadataObjectProvider,
                initializer.getCacheEngine(),
                initializer.getRasdamanService()
                                                   );
    }

    private void checkVersionOfRequest(WMSGetRequest request) {
        if (!request.hasGetValue(configManager.getVersionParam())) {
            request.setGetValue(configManager.getVersionParam(), configManager.getVersion());
        }
    }


    @NotNull
    private static final Logger logger = LoggerFactory.getLogger(ServiceOrchestrator.class);
    private ControllerRegistry controllerRegistry;
    private ConfigManager configManager;
    private PersistentMetadataObjectProvider persistentMetadataObjectProvider;

}
