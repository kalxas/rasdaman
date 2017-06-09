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
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.result.WcpsResult;

/**
 * Translation node from wcps to rasql for the return clause. Example:  <code>
 * return $c1 + $c2
 * </code> translates to  <code>
 * SELECT c1 + c2
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class ReturnClauseHandler {

    public  WcpsResult handle(WcpsResult processingExpr) {
        String template = TEMPLATE_RASQL.replace("$processingExpression", processingExpr.getRasql());
        WcpsCoverageMetadata metadata = processingExpr.getMetadata();
        processingExpr.setMetadata(metadata);
        processingExpr.setRasql(template);
        return processingExpr;
    }

    private  final String TEMPLATE_RASQL = "SELECT $processingExpression ";
}
