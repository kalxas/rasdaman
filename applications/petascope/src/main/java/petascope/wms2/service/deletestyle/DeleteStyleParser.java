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

package petascope.wms2.service.deletestyle;

import petascope.wms2.metadata.Layer;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.base.Parser;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInternalException;
import petascope.wms2.service.exception.error.WMSInvalidLayerException;
import petascope.wms2.servlet.WMSGetRequest;

import java.sql.SQLException;
import java.util.List;
import petascope.wms2.metadata.Style;
import petascope.wms2.service.exception.error.WMSInvalidStyleException;

/**
 * Parser for DeleteStyle request
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class DeleteStyleParser extends Parser<DeleteStyleRequest> {

    public DeleteStyleParser(PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        this.persistentMetadataObjectProvider = persistentMetadataObjectProvider;
    }

    @Override
    public boolean canParse(WMSGetRequest rawRequest) {
        String requestType = rawRequest.getGetValueByKey(DeleteStyleRequest.getRequestParameterRequest());
        return requestType != null &&
               requestType.equals(DeleteStyleRequest.getRequestParamValue());
    }

    @Override
    public DeleteStyleRequest parse(WMSGetRequest rawRequest) throws WMSException {        
        String layerStr = rawRequest.getGetValueByKey(DeleteStyleRequest.getLayerParamName());
        String styleStr = rawRequest.getGetValueByKey(DeleteStyleRequest.getStyleParamName());
        
        if (layerStr == null) {
            throw new WMSInvalidLayerException("");
        } else if (styleStr == null) {
            throw new WMSInvalidStyleException("");
        }
        try {
            // Get layer containing style
            List<Layer> layer = persistentMetadataObjectProvider.getLayer().queryForEq(Layer.NAME_COLUMN_NAME, layerStr);
            Style deleteStyle = null;
            // Iterate the styles from layer to get the style object to delete
            for (Style style:layer.get(0).getStyles()) {
                if (style.getName().equals(styleStr)) {
                    deleteStyle = style;
                    break;
                }
            }
            // Style is not found from layer so it is invalid request
            if (deleteStyle == null) {
                throw new WMSInvalidStyleException(styleStr);
            }

            return new DeleteStyleRequest(parseBaseRequest(rawRequest), deleteStyle);

        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
    }

    private final PersistentMetadataObjectProvider persistentMetadataObjectProvider;
}
