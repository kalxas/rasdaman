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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Handler for expression
    CASE booleanSwitchCaseCombinedExpression RETURN coverageExpression;
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SwitchCaseElementHandler extends Handler {
    
    public SwitchCaseElementHandler() {
        
    }
    
    public SwitchCaseElementHandler create(Handler booleanCoverageExpressionHandler, Handler returnValueCoverageExpression) {
        SwitchCaseElementHandler result = new SwitchCaseElementHandler();
        result.setChildren(Arrays.asList(booleanCoverageExpressionHandler, returnValueCoverageExpression));
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        WcpsResult booleanCoverageExpression = (WcpsResult) this.getFirstChild().handle();
        WcpsResult returnValueCoverageExpression = (WcpsResult) this.getSecondChild().handle();
        
        VisitorResult result = this.handle(booleanCoverageExpression, returnValueCoverageExpression);
        return result;
    }
    
    private VisitorResult handle(WcpsResult booleanCoverageExpression, WcpsResult returnValueCoverageExpression) {
        String rasql = " WHEN " + booleanCoverageExpression.getRasql() + " THEN " + returnValueCoverageExpression.getRasql();
        
        WcpsCoverageMetadata booleanExpressionMetadata = booleanCoverageExpression.getMetadata();
        
        WcpsCoverageMetadata returnedValueMetadata = returnValueCoverageExpression.getMetadata();
        if (returnedValueMetadata == null) {
            // e.g. return 3 (scalar value)
            returnedValueMetadata = new WcpsCoverageMetadata();
            RangeField rangeField = new RangeField(RangeField.DATA_TYPE, null, null, new ArrayList<>(), RangeField.UOM_CODE, null, null);
            returnedValueMetadata.setRangeFields(Arrays.asList(rangeField));
        }
        
        List<RangeField> returnedRangeFields = returnedValueMetadata.getRangeFields();
        if (booleanExpressionMetadata != null) {
            booleanExpressionMetadata.setRangeFields(returnedRangeFields);
        }
        
        return new WcpsResult(booleanExpressionMetadata, rasql);
    }
    
}
