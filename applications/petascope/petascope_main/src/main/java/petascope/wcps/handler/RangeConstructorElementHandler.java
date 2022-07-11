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
import org.rasdaman.domain.cis.NilValue;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * Handler for expression:
    fieldName: coverageExpression (e.g. red: $c.red + 30)
   
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RangeConstructorElementHandler extends Handler {
    
    public RangeConstructorElementHandler() {
        
    }
    
    public RangeConstructorElementHandler create(StringScalarHandler fieldNameHandler, Handler coverageExpressionHandler) {
        RangeConstructorElementHandler result = new RangeConstructorElementHandler();
        result.setChildren(Arrays.asList(fieldNameHandler, coverageExpressionHandler));
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        String fieldName = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        WcpsResult coverageExpression = (WcpsResult) this.getSecondChild().handle();
        
        VisitorResult result = this.handle(fieldName, coverageExpression);
        return result;
    }
    
    private VisitorResult handle(String fieldName, WcpsResult coverageExpression) {
        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        RangeField rangeField = null;
        // e.g: { red: c.0 }
        if (metadata != null) {
            // we get metadata from the first range which has metadata (e.g: { red: c.0, .... } )
            // coverage must contain at least 1 range and when in range expression only 1 range can be used.
            // e.g: test_mr has 1 range (band) and can be used as { red: c }
            // e.g: test_rgb has 3 ranges (bands) and can be used as { red: c.red } "not" { red: c }
            // NOTE: in case of coverage constructor, it also has only 1 range
            rangeField = metadata.getRangeFields().get(0);
            rangeField.setName(fieldName);
        } else {
            metadata = new WcpsCoverageMetadata();
            // e.g: { red: 0 } which coverage metadata is null then need to create a range field for this case
            rangeField = new RangeField(RangeField.DATA_TYPE, fieldName, null, new ArrayList<>(), RangeField.UOM_CODE, null, null);
        }
         
        metadata.setRangeFields(Arrays.asList(rangeField));
        String rasql = coverageExpression.getRasql();
        
        WcpsResult wcpsResult = new WcpsResult(metadata, rasql);
        return wcpsResult;
    }
    
}
