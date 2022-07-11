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
 * Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import java.util.Arrays;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.StringUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translator class for extracting domain (interval / lowerBound or upperBound)
 * of a selected index from imageCrsdomain() or domain().
 * 
 * e.g: imageCrsdomain(c, Lat).lo returns 20
 *      imageCrsdomain(c, Lat).hi returns 30
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DomainIntervalsHandler extends Handler {
    
    public DomainIntervalsHandler() {
        
    }
    
    public DomainIntervalsHandler create(Handler coverageExpressionHandler, StringScalarHandler sdomLowerBoundHandler) {
        DomainIntervalsHandler result = new DomainIntervalsHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, sdomLowerBoundHandler));
        
        return result;
    }
    
    public WcpsMetadataResult handle() throws PetascopeException {
        WcpsMetadataResult metadataResult = ((WcpsMetadataResult)this.getFirstChild().handle());
        Boolean sdomLowerBound = StringUtil.stringToBoolean(((WcpsResult)this.getSecondChild().handle()).getRasql());
        
        WcpsMetadataResult result = this.handle(new WcpsResult(metadataResult.getMetadata(), metadataResult.getResult()), sdomLowerBound);
        return result;
    }

    private WcpsMetadataResult handle(WcpsResult coverageExpression, Boolean sdomLowerBound) throws PetascopeException {
        
        if (sdomLowerBound != null && coverageExpression.getMetadata().getAxes().size() > 1) {
            throw new WCPSException(ExceptionCode.InvalidRequest, "Cannot extract bound from result of imageCrsdomain() / domain() on 2D+ coverage.");
        }
        
        String result = coverageExpression.getRasql();
        
        // e.g: imageCrsdomain(c, Lat)[0]
        String domainIntervalsResult = coverageExpression.getRasql();            
        String bounds = domainIntervalsResult.substring(domainIntervalsResult.indexOf("(") + 1, domainIntervalsResult.indexOf(")"));           

        result = bounds;
        
        String[] tmp = bounds.split(":");
        
        boolean isTimeAxis = false;
        if (bounds.contains("\"")) {
            isTimeAxis = true;
            // e.g. "2015-01-02":"2015-06-07T01:00:02")
            tmp = bounds.split("\":");
        }

        String lowerBound = tmp[0];        
        String upperBound = tmp[1];
        if (isTimeAxis) {
            lowerBound = lowerBound + "\"";
        }

        // 0D coverage
        // e.g: imageCrsdomain(c)[0].lo
        if (sdomLowerBound != null) {
            if (sdomLowerBound) {
                result = lowerBound;
            } else if (sdomLowerBound == false) {
                result = upperBound;
            }
            
            // After .lo / .hi, the result is 0D
            coverageExpression.setMetadata(new WcpsCoverageMetadata());
        }
        
        WcpsMetadataResult wcpsMetadataResult = new WcpsMetadataResult(coverageExpression.getMetadata(), result);
        return wcpsMetadataResult;
    }
}
