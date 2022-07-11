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
package petascope.wcps.handler;

import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.MIMEUtil;
import petascope.util.ras.RasConstants;
import petascope.wcps.exception.processing.CoverageNotEncodedInReturnClauseException;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

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
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ReturnClauseHandler extends Handler {
    
    public ReturnClauseHandler() {
        
    }
    
    public ReturnClauseHandler create(List<Handler> childHandlers) {
        ReturnClauseHandler result = new ReturnClauseHandler();
        result.setChildren(childHandlers);
        return result;
    }
    
    public VisitorResult handle() throws PetascopeException {
        VisitorResult temp = this.getFirstChild().handle();
        
        VisitorResult result = temp;
        if (temp instanceof WcpsResult) {
            result = this.handle((WcpsResult) temp);
        }
        
        return result;
    }

    private VisitorResult handle(WcpsResult processingExpr) {
        String template = TEMPLATE_RASQL.replace("$processingExpression", processingExpr.getRasql());
        WcpsCoverageMetadata metadata = processingExpr.getMetadata();
        // NOTE: If result in RETURN clause is scalar (E.g: return 2, return 2 + 3, return avg($c))
        // then WCPS coverage metadata object is null. If not, according to WCPS document, result should be encoded.
        if (metadata != null) {
            if (!metadata.getAxes().isEmpty()) {
                // Coverage result is not scalar (it has axis domain(s)) so it must start with encode()
                // NOTE: dem() is a special one in rasql and does not need encode().
                String tmp = processingExpr.getRasql().toLowerCase().trim();
                if (!(tmp.startsWith(RasConstants.RASQL_ENCODE) || tmp.startsWith(MIMEUtil.ENCODE_DEM))) {
                    throw new CoverageNotEncodedInReturnClauseException();
                }
            }
        }
        processingExpr.setMetadata(metadata);
        processingExpr.setRasql(template);
        return processingExpr;
    }

    private  final String TEMPLATE_RASQL = "SELECT $processingExpression ";
}
