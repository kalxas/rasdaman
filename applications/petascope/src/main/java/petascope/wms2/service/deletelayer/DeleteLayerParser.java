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

import petascope.wms2.metadata.Layer;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.base.Parser;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInternalException;
import petascope.wms2.service.exception.error.WMSInvalidLayerException;
import petascope.wms2.servlet.WMSGetRequest;

import java.sql.SQLException;
import java.util.List;

/**
 * Parser for DeleteLayer request
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class DeleteLayerParser extends Parser<DeleteLayerRequest> {

    public DeleteLayerParser(PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        this.persistentMetadataObjectProvider = persistentMetadataObjectProvider;
    }

    @Override
    public boolean canParse(WMSGetRequest rawRequest) {
        String requestType = rawRequest.getGetValueByKey(DeleteLayerRequest.getRequestParameterRequest());
        return requestType != null &&
               requestType.equals(DeleteLayerRequest.getRequestParamValue());
    }

    @Override
    public DeleteLayerRequest parse(WMSGetRequest rawRequest) throws WMSException {
        String layerStr = rawRequest.getGetValueByKey(DeleteLayerRequest.getLayerParamName());
        if (layerStr == null) {
            throw new WMSInvalidLayerException("");
        }
        try {
            List<Layer> layer = persistentMetadataObjectProvider.getLayer().queryForEq(Layer.NAME_COLUMN_NAME, layerStr);
            if (layer.isEmpty()) {
                throw new WMSInvalidLayerException(layerStr);
            }
            return new DeleteLayerRequest(parseBaseRequest(rawRequest), layer.get(0));

        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
    }

    private final PersistentMetadataObjectProvider persistentMetadataObjectProvider;
}
