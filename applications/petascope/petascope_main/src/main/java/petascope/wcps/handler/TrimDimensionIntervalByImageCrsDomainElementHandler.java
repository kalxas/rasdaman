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
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;

/**
 Handler for expression:
// axisName (COLON crsName)? LEFT_PARENTHESIS
                           imageCrsDomainByDimensionExpression
                          RIGHT_PARENTHESIS     
// e.g: i:"CRS:1"(imageCrsDomain(c[i(0:30)], i))
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TrimDimensionIntervalByImageCrsDomainElementHandler extends Handler {
    
    public TrimDimensionIntervalByImageCrsDomainElementHandler() {
        
    }
    
    public TrimDimensionIntervalByImageCrsDomainElementHandler create(StringScalarHandler axisNameHandler, StringScalarHandler crsHandler,
                                                       Handler imageCrsDomainByDimensionExpressionHandler) {
        TrimDimensionIntervalByImageCrsDomainElementHandler result = new TrimDimensionIntervalByImageCrsDomainElementHandler();
        result.setChildren(Arrays.asList(axisNameHandler, crsHandler, imageCrsDomainByDimensionExpressionHandler));
        
        return result;
    }

    @Override
    public VisitorResult handle() throws PetascopeException {
        String axisName = ((WcpsResult)this.getFirstChild().handle()).getRasql();
        String crs = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        WcpsMetadataResult gridBoundExpression = (WcpsMetadataResult) this.getThirdChild().handle();
        
        VisitorResult result = this.handle(axisName, crs, gridBoundExpression);
        return result;
    }
    
    private VisitorResult handle(String axisName, String crs, WcpsMetadataResult gridBoundExpression) throws PetascopeException {
        // axisName (COLON crsName)? LEFT_PARENTHESIS
//                                   imageCrsDomainByDimensionExpression
//                                  RIGHT_PARENTHESIS     
        // e.g: i:"CRS:1"(imageCrsDomain(c[i(0:30)], i))
        String[] gridBounds = gridBoundExpression.getResult()
                                        .replace("(", "")
                                        .replace(")", "")
                                        .split(":");
        
        String rawLowerBound = gridBounds[0];
        String rawUpperBound = gridBounds[1];
        
        WcpsTrimSubsetDimension result = new WcpsTrimSubsetDimension(axisName, crs, rawLowerBound, rawUpperBound);
        
        return result;
    }
    
}
