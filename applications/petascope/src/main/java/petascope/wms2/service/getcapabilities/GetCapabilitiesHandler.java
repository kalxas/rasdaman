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

package petascope.wms2.service.getcapabilities;

import org.jetbrains.annotations.NotNull;
import petascope.wms2.metadata.*;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.base.Handler;
import petascope.wms2.service.exception.error.WMSInternalException;
import petascope.wms2.util.ConfigManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Handles get capabilities requests and provides as a response a GetCapabilitiesResponse
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class GetCapabilitiesHandler implements Handler<GetCapabilitiesRequest, GetCapabilitiesResponse> {


    /**
     * Constructor for the class
     *
     * @param persistentMetadataObjectProvider the persistent metadata object provider
     * @param configManager                    a configuration manager
     */
    public GetCapabilitiesHandler(PersistentMetadataObjectProvider persistentMetadataObjectProvider, ConfigManager configManager) {
        this.persistentMetadataObjectProvider = persistentMetadataObjectProvider;
        this.configManager = configManager;
    }

    /**
     * Handles the request and provides a response by querying the metadata objects for the needed information.
     * The GetCapabilities metadata object is created and passes to a GetCapabilitiesResponse object
     *
     * @param request the typed wms request
     * @return the get capabilities response for the given request
     * @throws WMSInternalException
     */
    @NotNull
    @Override
    public GetCapabilitiesResponse handle(@NotNull GetCapabilitiesRequest request) throws WMSInternalException {
        try {
            Service service = persistentMetadataObjectProvider.getService().iterator().next();
            List<GetCapabilitiesFormat> getCapabilitiesFormats = persistentMetadataObjectProvider.getGetCapabilitiesFormat().queryForAll();
            List<GetMapFormat> getMapFormats = persistentMetadataObjectProvider.getGetMapFormat().queryForAll();
            List<ExceptionFormat> exceptionFormats = persistentMetadataObjectProvider.getExceptionFormat().queryForAll();
            List<Layer> layers = persistentMetadataObjectProvider.getLayer().queryForAll();
            GetCapabilities capabilities = new GetCapabilities(
                configManager.getVersion(),
                configManager.getSchemaLocation(),
                layers.size(),
                request.getBaseUrl(),
                service,
                getCapabilitiesFormats,
                getMapFormats,
                exceptionFormats,
                layers
            );            
            return new GetCapabilitiesResponse(capabilities);
        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
    }

    private final PersistentMetadataObjectProvider persistentMetadataObjectProvider;
    private final ConfigManager configManager;

}
