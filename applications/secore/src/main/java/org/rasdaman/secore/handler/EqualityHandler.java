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
import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.secore.Resolver;
import org.rasdaman.secore.req.RequestParam;
import static org.rasdaman.secore.Constants.*;
import org.rasdaman.secore.util.ExceptionCode;
import org.rasdaman.secore.util.SecoreException;
import org.rasdaman.secore.util.StringUtil;
import org.rasdaman.secore.util.XMLDiff;

/**
 * Test if two CRS definitions are equal.
 *
 * @author Dimitar Misev
 */
public class EqualityHandler extends AbstractHandler {

    private static Logger log = LoggerFactory.getLogger(EqualityHandler.class);

    @Override
    public ResolveResponse handle(ResolveRequest request) throws SecoreException {
        log.debug("Handling resolve request...");

        List<RequestParam> params = request.getParams();

        if (request.getOperation().equals(getOperation()) && params.size() == 2) {

            List<RequestParam> components = request.getParams();

            // do some checking first, whether they are existing references
            String def1 = EMPTY;
            String def2 = EMPTY;

            for (RequestParam component : components) {
                try {
                    if (!(component.val instanceof ResolveRequest)) {
                        throw new SecoreException(ExceptionCode.InvalidParameterValue,
                                                  "Invalid parameter value received for " + component.key + ": " + component.val);
                    }
                    String res = null;
                    ResolveRequest req = (ResolveRequest) component.val;
                    if (req.isLocal()) {
                        res = Resolver.resolve(req).getData();
                    } else {
                        res = Resolver.resolve(new URL(req.getOriginalRequest())).getData();
                    }
                    if (EMPTY.equals(res)) {
                        throw new SecoreException(ExceptionCode.NoSuchDefinition,
                                                  "Invalid definition received for " + component.val);
                    }
                    String id = StringUtil.getElementValue(res, IDENTIFIER_LABEL);
                    if (id == null) {
                        throw new SecoreException(ExceptionCode.XmlNotValid,
                                                  "Invalid definition received for " + component.val);
                    }
                    if (def1.equals(EMPTY)) {
                        def1 = res;
                    } else {
                        def2 = res;
                    }
                } catch (Exception ex) {
                    log.error("Failed resolving definition: " + component.val, ex);
                    throw new SecoreException(ExceptionCode.NoSuchDefinition,
                                              "Failed resolving definition: " + component, ex);
                }
            }

            log.debug("Comparing the retrieved definitions...");
            String res = XMLDiff.equals(def1, def2).toXML();
            log.trace(res);

            log.debug("Done, returning response.");
            return new ResolveResponse(res);
        } else {
            log.error("Can't handle the given parameters, exiting with error.");
            throw new SecoreException(ExceptionCode.MissingParameterValue,
                                      "Insufficient parameters provided");
        }
    }

    @Override
    public String getOperation() {
        return OP_EQUAL;
    }
}
