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
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;

/**
 * Translator class for extracting domain (interval / lowerBound or upperBound or resolution)
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
    
    public static final String DOMAIN_PROPERTY_LOWER_BOUND = "lo";
    public static final String DOMAIN_PORPERTY_UPPER_BOUND = "hi";
    public static final String DOMAIN_PROPERTY_RESOLUTION = "resolution";
    
    public DomainIntervalsHandler() {
        
    }
    
    public DomainIntervalsHandler create(Handler coverageExpressionHandler, StringScalarHandler domainProperyValueHandler) {
        DomainIntervalsHandler result = new DomainIntervalsHandler();
        result.setChildren(Arrays.asList(coverageExpressionHandler, domainProperyValueHandler));
        
        return result;
    }
    
    public WcpsMetadataResult handle() throws PetascopeException {
        WcpsMetadataResult metadataResult = ((WcpsMetadataResult)this.getFirstChild().handle());
        String domainPropertyValue = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        
        WcpsMetadataResult result = this.handle(new WcpsResult(metadataResult.getMetadata(), metadataResult.getResult()), domainPropertyValue);
        return result;
    }

    private WcpsMetadataResult handle(WcpsResult coverageExpression, String domainPropertyValue) throws PetascopeException {
        
        if (domainPropertyValue != null && coverageExpression.getMetadata().getAxes().size() > 1) {
            throw new WCPSException(ExceptionCode.InvalidRequest, "Cannot extract bound from result of imageCrsdomain() / domain() on 2D+ coverage.");
        }
        
        if (domainPropertyValue != null && domainPropertyValue.equalsIgnoreCase(DOMAIN_PROPERTY_RESOLUTION)) {
            // domain($c, Lat).resolution
            Axis axis = coverageExpression.getMetadata().getAxes().get(0);
            // After .resolution, the result is 0D
            coverageExpression.setMetadata(new WcpsCoverageMetadata());
            WcpsMetadataResult wcpsMetadataResult = new WcpsMetadataResult(coverageExpression.getMetadata(), axis.getResolution().toPlainString());
            
            return wcpsMetadataResult;
        }
        
        // e.g: imageCrsdomain(c, Lat)[0]
        String domainIntervalsResult = coverageExpression.getRasql();
        String result = domainIntervalsResult;
        
        if (domainPropertyValue != null) {
            String bounds = domainIntervalsResult.substring(domainIntervalsResult.indexOf("(") + 1, domainIntervalsResult.indexOf(")"));           

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

            // e.g: imageCrsdomain(c)[0].lo
            if (domainPropertyValue.equalsIgnoreCase(DOMAIN_PROPERTY_LOWER_BOUND)) {
                result = lowerBound;
            } else if (domainPropertyValue.equalsIgnoreCase(DOMAIN_PORPERTY_UPPER_BOUND)) {
                result = upperBound;
            }

            // After .lo / .hi, the result is 0D
            coverageExpression.setMetadata(new WcpsCoverageMetadata());            
        }
            
        WcpsMetadataResult wcpsMetadataResult = new WcpsMetadataResult(coverageExpression.getMetadata(), result);
        return wcpsMetadataResult;
    }
}
