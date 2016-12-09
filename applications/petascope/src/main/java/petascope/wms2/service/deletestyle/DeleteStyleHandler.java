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

import org.jetbrains.annotations.NotNull;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.base.Handler;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInternalException;

import java.sql.SQLException;
import petascope.wms2.metadata.Style;

/**
 * Handler for DeleteStyle request
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class DeleteStyleHandler implements Handler<DeleteStyleRequest, DeleteStyleResponse> {

    public DeleteStyleHandler(PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        this.persistentMetadataObjectProvider = persistentMetadataObjectProvider;
    }

    @NotNull
    @Override
    public DeleteStyleResponse handle(@NotNull DeleteStyleRequest request) throws WMSException {
        Style style = request.getStyle();
        try {
            DeleteStyleContent(style);
            return new DeleteStyleResponse();
        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
    }

    /**
     * Remove the related style from layer
     * @param style
     */
    private void DeleteStyleContent(Style style) throws SQLException {
        persistentMetadataObjectProvider.getStyle().delete(style);
    }

    private final PersistentMetadataObjectProvider persistentMetadataObjectProvider;
}
