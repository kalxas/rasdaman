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

package petascope.wms2.service.insertwcslayer;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import petascope.core.CoverageMetadata;
import petascope.wcps.metadata.Bbox;
import petascope.wms2.metadata.EXGeographicBoundingBox;
import petascope.wms2.metadata.Layer;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.exception.error.WMSInternalException;
import petascope.wms2.service.exception.error.WMSInvalidCrsUriException;
import petascope.wms2.util.CrsComputer;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for translating wcs coverage objects into wms layers.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class LayerParser {

    /**
     * Creates a layer from a wcs coverage metadata object. The layer is not yet persistent and doesn't contain the associated
     * bounding box, crses, extra dimensions and styles yet. They MUST be added after the layer is persisted.
     *
     * @param coverageMetadata
     * @param persistentMetadataObjectProvider
     * @return the layer
     * @throws WMSInternalException
     */
    public static Layer fromWcsCoverage(CoverageMetadata coverageMetadata, PersistentMetadataObjectProvider persistentMetadataObjectProvider) throws WMSInternalException, WMSInvalidCrsUriException, TransformException, FactoryException, SQLException {
        String layerTitle = coverageMetadata.getCoverageName();
        Bbox wcsBbox = coverageMetadata.getBbox();
        String currentCrs = CrsComputer.convertCrsUriToWmsCrs(wcsBbox.getCrsName());
        EXGeographicBoundingBox exGeographicBoundingBox = CrsComputer.covertToWgs84(currentCrs,
            wcsBbox.getMinX(), wcsBbox.getMinY(), wcsBbox.getMaxX(), wcsBbox.getMaxY());
        String layerAbstract = coverageMetadata.getAbstract();
        List<Layer> possibleLayers = persistentMetadataObjectProvider.getLayer().queryForEq(Layer.NAME_COLUMN_NAME, layerTitle);
        final Layer retLayer;
        if (possibleLayers.isEmpty()) {
            Layer newLayer = new Layer(0, 0, 1, 0, 0, 0, layerTitle, layerTitle, layerAbstract, exGeographicBoundingBox, null);
            newLayer = persistentMetadataObjectProvider.getLayer().createIfNotExists(newLayer);
            retLayer = persistentMetadataObjectProvider.getLayer().queryForId(newLayer.getId());
        } else {
            retLayer = possibleLayers.get(0);
        }
        return retLayer;
    }

    /**
     * Computes the name of the new layer. wcsCoverageId is used as layer name, unless a layer with this name already
     * exists. In this case, the layer name is generated.
     *
     * @param wcsCoverageId
     * @return
     * @deprecated
     */
    private static String computeLayerTitle(String wcsCoverageId, PersistentMetadataObjectProvider persistentMetadataObjectProvider) throws WMSInternalException {
        try {
            if (persistentMetadataObjectProvider.getLayer().queryForEq(Layer.NAME_COLUMN_NAME, wcsCoverageId).isEmpty()) {
                //a layer with this name doesn't exist, use the wcsCoverageId
                return wcsCoverageId;
            } else {
                //a layer with this name already exists, generate one
                return UUID.randomUUID().toString();
            }
        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
    }


}
