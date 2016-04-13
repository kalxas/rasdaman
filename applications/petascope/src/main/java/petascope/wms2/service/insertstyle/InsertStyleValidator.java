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

package petascope.wms2.service.insertstyle;

import petascope.wms2.service.base.Validator;
import petascope.wms2.service.exception.error.*;

/**
 * Validates an insert style request
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class InsertStyleValidator implements Validator<InsertStyleRequest> {

    /**
     * Checks that the parameters in the request are set
     * @param request the request to be checked
     * @throws WMSException
     */
    @Override
    public void validate(InsertStyleRequest request) throws WMSException {
        if (request.getRasqlTransformFragment() == null) {
            throw new WMSEmptyRasqlQuery();
        }
        if (request.getStyleName() == null) {
            throw new WMSEmptyStyleName();
        }
        if(request.getStyleAbstract() == null){
            throw new WMSInvalidStyleAbstract();
        }
        if(request.getLayerName() == null){
            throw new WMSInvalidLayerException("");
        }
    }
}
