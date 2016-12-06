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

package petascope.wms2.service.deletelayer;

import org.jetbrains.annotations.NotNull;
import petascope.wms2.metadata.Layer;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.base.Handler;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInternalException;

import java.sql.SQLException;
import java.util.List;

/**
 * Class description
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class DeleteLayerHandler implements Handler<DeleteLayerRequest, DeleteLayerResponse> {

    public DeleteLayerHandler(PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        this.persistentMetadataObjectProvider = persistentMetadataObjectProvider;
    }

    @NotNull
    @Override
    public DeleteLayerResponse handle(@NotNull DeleteLayerRequest request) throws WMSException {
        Layer layer = request.getLayer();
        try {
            DeleteLayerContent(layer);
            return new DeleteLayerResponse();
        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
    }

    /**
     * Delete WMS layer by LayerID (CoverageID) when deleting coverage in WCS request.
     * @param layerID
     * @throws WMSException
     * @throws SQLException
     */
    public void DeleteLayerByID(@NotNull String layerID) throws WMSException, SQLException {
        List<Layer> layers = persistentMetadataObjectProvider.getLayer().queryForEq(Layer.NAME_COLUMN_NAME, layerID);
        // only remove layer by layerID if it does exist in database
        if (!layers.isEmpty()) {
            Layer layer = layers.get(0);
            try {
                DeleteLayerContent(layer);
            } catch (SQLException e) {
                throw new WMSInternalException(e);
            }
        }
    }

    /**
     * Remove any related properties of layer including itselfi n database from the layer.
     * @param layer
     */
    private void DeleteLayerContent(Layer layer) throws SQLException {
        //TODO throw error if layer has child layers
        persistentMetadataObjectProvider.getExGeographicBoundingBox().delete(layer.getExBoundingBox());
        if (layer.getStyles() != null && layer.getStyles().size() > 0) {
            persistentMetadataObjectProvider.getStyle().delete(layer.getStyles());
        }
        persistentMetadataObjectProvider.getBoundingBox().delete(layer.getBoundingBoxes());
        persistentMetadataObjectProvider.getRasdamanLayer().delete(layer.getRasdamanLayers());
        if (layer.getDimensions() != null && layer.getDimensions().size() > 0) {
            persistentMetadataObjectProvider.getDimension().delete(layer.getDimensions());
        }
        persistentMetadataObjectProvider.getLayer().delete(layer);
    }

    private final PersistentMetadataObjectProvider persistentMetadataObjectProvider;
}
