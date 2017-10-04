/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.secore.handler;

import org.rasdaman.secore.req.ResolveResponse;
import org.rasdaman.secore.req.ResolveRequest;
import org.rasdaman.secore.util.SecoreException;
import org.rasdaman.secore.util.ExceptionCode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.secore.db.DbManager;
import org.rasdaman.secore.req.RequestParam;
import org.rasdaman.secore.util.Constants;

/**
 * Resolves axis identifiers.
 * In case a single parameter is provided in a RESTful request it is defaulted to a name search
 *
 */
public class AxisHandler extends AbstractHandler {

    private static final Logger log = LoggerFactory.getLogger(AxisHandler.class);

    private static final String ELEMENT = "synonym";
    private static final String IDENTIFIER = "identifier";

    @Override
    public ResolveResponse handle(ResolveRequest request) throws SecoreException {
        log.debug("Handling resolve request...");
        List<RequestParam> params = request.getParams();

        // NOTE: default search is fix version gml dictionary.
        String versionNumber = DbManager.FIX_GML_VERSION_NUMBER;

        if (request.getOperation().equals(getOperation()) && params.size() == 1) {
            String name = params.get(0).val + "";
            String id = resolveAttribute(ELEMENT, name, versionNumber);
            log.debug("Retrieved the identifier '" + id + "' of the synonym for " + name);
            String res = resolve(IDENTIFIER, id, versionNumber, Constants.ZERO);
            log.debug("Done, returning response.");
            return new ResolveResponse(res);
        } else {
            log.error("Can't handle the given parameters, exiting with error.");
            throw new SecoreException(ExceptionCode.MissingParameterValue, "Insufficient parameters provided");
        }
    }

    @Override
    public String getOperation() {
        return OP_AXIS;
    }
}
