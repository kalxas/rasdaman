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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.metadata.GetCapabilitiesFormat;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.base.Validator;
import petascope.wms2.service.exception.error.*;

import java.sql.SQLException;
import java.util.List;

/**
 * A validator for get capabilities requests. It implements the request validator class and extends it
 * by checking for the update sequence of the request
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class GetCapabilitiesValidator implements Validator<GetCapabilitiesRequest> {
    /**
     * Constructor for the class
     *
     * @param persistentMetadataObjectProvider the metadata object provider
     */
    public GetCapabilitiesValidator(PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        super();
        this.persistentMetadataObjectProvider = persistentMetadataObjectProvider;
    }

    /**
     * Validates the request by checking the sequence number according to this table:
     *
     * @throws petascope.wms2.service.exception.error.WMSException
     */
    @Override
    public void validate(GetCapabilitiesRequest request) throws WMSException {
        try {
            validateUpdateSequenceNumber(request.getUpdateSequence());
            validateFormat(request.getFormat());
        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
    }

    /**
     * Validates the update sequence number
     * +--------------------------------------------------------------------------------------------+
     * |    Client Request    |   Server Metadata    | Server Response                              |
     * | UpdateSequence Value | UpdateSequence Value |                                              |
     * |----------------------+----------------------+----------------------------------------------|
     * |         none         |         any          | most recent Capabilities XML                 |
     * |----------------------+----------------------+----------------------------------------------|
     * |         any          |         none         | most recent Capabilities XML                 |
     * |----------------------+----------------------+----------------------------------------------|
     * |        equal         |        equal         | Exception: code=CurrentUpdateSequence        |
     * |----------------------+----------------------+----------------------------------------------|
     * |        lower         |        higher        | most recent Capabilities XML                 |
     * |----------------------+----------------------+----------------------------------------------|
     * |        higher        |        lower         | Exception: code=InvalidUpdateSequence        |
     * +--------------------------------------------------------------------------------------------+
     *
     * @throws SQLException
     * @throws petascope.wms2.service.exception.error.WMSCurrentUpdateSequenceException
     * @throws petascope.wms2.service.exception.error.WMSInvalidUpdateSequenceException
     */
    private void validateUpdateSequenceNumber(@Nullable String clientUpdateSequenceString)
            throws SQLException, WMSCurrentUpdateSequenceException, WMSInvalidUpdateSequenceException {
        if (clientUpdateSequenceString != null) {
            long updateSequence = persistentMetadataObjectProvider.getLayer().countOf();
            long clientUpdateSequence = Long.parseLong(clientUpdateSequenceString);
            if (updateSequence == clientUpdateSequence) {
                throw new WMSCurrentUpdateSequenceException(clientUpdateSequence, updateSequence);
            } else if (updateSequence < clientUpdateSequence) {
                throw new WMSInvalidUpdateSequenceException(clientUpdateSequence, updateSequence);
            }
        }
    }

    /**
     * Validates the format of the response requested
     *
     * @param clientFormat the format that the client wants the response in
     * @throws petascope.wms2.service.exception.error.WMSInvalidFormatException
     * @throws WMSInternalException
     */
    private void validateFormat(@Nullable String clientFormat) throws WMSInvalidFormatException, WMSInternalException {
        if (clientFormat != null) {
            try {
                Dao<GetCapabilitiesFormat, String> getCapFormatDao = persistentMetadataObjectProvider.getGetCapabilitiesFormat();
                QueryBuilder<GetCapabilitiesFormat, String> queryBuilder = getCapFormatDao.queryBuilder();
                queryBuilder.where().eq(GetCapabilitiesFormat.FORMAT_FIELD_NAME, clientFormat);
                List<GetCapabilitiesFormat> formats = getCapFormatDao.query(queryBuilder.prepare());
                if (formats.size() == 0) {
                    throw new WMSInvalidFormatException(clientFormat);
                }
            } catch (SQLException e) {
                throw new WMSInternalException(e);
            }

        }

    }

    private final PersistentMetadataObjectProvider persistentMetadataObjectProvider;
}
