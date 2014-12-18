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

import petascope.wms2.service.exception.error.WMSException;

/**
 * A validator analyzes a request and throws an error if a the request is incorrect (for example, a parameter is missing,
 * or the value for some parameter is wrong). As a rule of thumb not much work should be done in a validator, especially
 * if it will be repeated in the Hanlder
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public interface Validator<RT extends Request> {

    /**
     * Validates the request and throws the corresponding WMS exception if invalid
     *
     * @throws petascope.wms2.service.exception.error.WMSException
     */
    public void validate(RT request) throws WMSException;
}
