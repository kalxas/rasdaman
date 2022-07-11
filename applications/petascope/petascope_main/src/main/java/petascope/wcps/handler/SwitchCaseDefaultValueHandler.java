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
 * Copyright 2003 - 2022 Peter Baumann / rasdaman GmbH.
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
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Handler for expression
   switchCaseDefaultElement: coverageExpression;
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SwitchCaseDefaultValueHandler extends Handler {
    
    public SwitchCaseDefaultValueHandler() {
        
    }
    
    public SwitchCaseDefaultValueHandler create(Handler defaultValueCoverageExpression) {
        SwitchCaseDefaultValueHandler result = new SwitchCaseDefaultValueHandler();
        result.setChildren(Arrays.asList(defaultValueCoverageExpression));
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        WcpsResult defaultValueCoverageExpression = (WcpsResult) this.getFirstChild().handle();
        
        VisitorResult result = this.handle(defaultValueCoverageExpression);
        return result;
    }
    
    private VisitorResult handle(WcpsResult defaultValueCoverageExpression) {
        String rasql = " ELSE " + defaultValueCoverageExpression.getRasql();
        
        WcpsCoverageMetadata metadata = defaultValueCoverageExpression.getMetadata();
        return new WcpsResult(metadata, rasql);
    }
    
}
