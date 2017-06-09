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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.handler;

import org.springframework.stereotype.Service;
import petascope.exceptions.WCPSException;
import petascope.wcps2.result.WcpsResult;
import petascope.util.ras.CastDataTypeConverter;

/**
 * Translation node from wcps to rasql for the case expression. Example:  <code>
 * (char) $c1
 * </code> translates to  <code>
 * (char) c1
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class CastExpressionHandler {

    public WcpsResult handle(String rangeType, WcpsResult coverageExp) throws WCPSException {
        String template = TEMPLATE.replace("$rangeType", CastDataTypeConverter.convert(rangeType))
                .replace("$coverageExp", coverageExp.getRasql());
        return new WcpsResult(coverageExp.getMetadata(), template);
    }

    private final String TEMPLATE = "($rangeType) $coverageExp";
}
