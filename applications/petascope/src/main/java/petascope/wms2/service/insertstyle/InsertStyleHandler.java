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

package petascope.wms2.service.insertstyle;

import org.jetbrains.annotations.NotNull;
import petascope.wms2.metadata.Layer;
import petascope.wms2.metadata.Style;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.base.Handler;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInternalException;
import petascope.wms2.service.exception.error.WMSInvalidLayerException;

import java.sql.SQLException;
import java.util.List;

/**
 * Handler for the insert style request type. It reads the parameters from the request and reates a style object out
 * of them
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class InsertStyleHandler implements Handler<InsertStyleRequest, InsertStyleResponse> {

    /**
     * Constructor for the class
     *
     * @param persistenceProvider the persistence provider for metadata objects
     */
    public InsertStyleHandler(PersistentMetadataObjectProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    /**
     * Handles the request and produces a insert layer response
     *
     * @param request the typed wms request
     * @return the insert layer response
     * @throws WMSException
     */
    @NotNull
    @Override
    public InsertStyleResponse handle(@NotNull InsertStyleRequest request) throws WMSException {
        try {
            List<Layer> layers = persistenceProvider.getLayer().queryForEq(Layer.NAME_COLUMN_NAME, request.getLayerName());
            if (layers.size() == 0) {
                throw new WMSInvalidLayerException(request.getLayerName());
            }
            persistenceProvider.getStyle().createIfNotExists(
                    new Style(request.getStyleName(), request.getStyleName(), request.getStyleAbstract(), null,
                            request.getRasqlTransformFragment(), layers.get(0))
            );
        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
        return new InsertStyleResponse();
    }

    private final PersistentMetadataObjectProvider persistenceProvider;
}
