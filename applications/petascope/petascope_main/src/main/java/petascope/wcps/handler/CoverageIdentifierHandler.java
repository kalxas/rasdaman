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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import java.util.Arrays;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import static petascope.wcps.handler.AbstractOperatorHandler.checkOperandIsCoverage;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translator class for Identifier of the coverage (e.g coverage's name from
 * identifier($c))  <code>
 * for c in (mr), d in (rgb) return identifier(c)
 * </code>  <code>
 * mr
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CoverageIdentifierHandler extends Handler {
    
    public static final String OPERATOR = "identifier";
    
    public CoverageIdentifierHandler() {
        
    }
    
    public CoverageIdentifierHandler create(Handler coverageExpressionHandler) {
        CoverageIdentifierHandler result = new CoverageIdentifierHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler));
        return result;
    }
    
    public VisitorResult handle() throws PetascopeException {
        WcpsResult coverageExpression = (WcpsResult) this.getFirstChild().handle();
        WcpsMetadataResult result = this.handle(coverageExpression);
        return result;
    }

    public WcpsMetadataResult handle(WcpsResult coverageExpression) {
        checkOperandIsCoverage(coverageExpression, OPERATOR); 
        return new WcpsMetadataResult(coverageExpression.getMetadata(), coverageExpression.getMetadata().getCoverageName());
    }

}
