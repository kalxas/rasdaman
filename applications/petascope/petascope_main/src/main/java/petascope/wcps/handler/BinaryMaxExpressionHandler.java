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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import org.springframework.stereotype.Service;
import petascope.wcps.result.WcpsResult;

/**
 *
 * e.g: max(test_mr, test_mr1)
 * or max(1, 2)
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class BinaryMaxExpressionHandler {

    public WcpsResult handle(WcpsResult coverageExp1, WcpsResult coverageExp2) {
        String template = TEMPLATE.replace("$coverageExp1", coverageExp1.getRasql())
                                  .replace("$coverageExp2", coverageExp2.getRasql());

        return new WcpsResult(coverageExp1.getMetadata(), template);
    }

    private final String TEMPLATE = "($coverageExp1 MAX $coverageExp2)";

}
