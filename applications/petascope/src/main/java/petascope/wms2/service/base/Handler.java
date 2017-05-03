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

package petascope.wms2.service.base;

import org.jetbrains.annotations.NotNull;
import petascope.exceptions.PetascopeException;
import petascope.wms2.service.exception.error.WMSException;

/**
 * Interface for WMS handlers. Each handler transforms a WMS typed wms request into a generic response that can be
 * handled by the server
 * Each handler has to implement two methods, one defining if this handler can handle the requests and the other
 * one creating the response based on the request
 *
 * @param <RT>  the request class that will be used
 * @param <RSP> the response class that will be used
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public interface Handler<RT extends Request, RSP extends Response> {

    /**
     * Handles the typed wms request into a generic response that can be
     *
     * @param request                          the typed wms request
     * @return the response to this request
     */
    @NotNull
    RSP handle(@NotNull RT request) throws WMSException, PetascopeException;

}