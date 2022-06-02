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

import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.StringUtil;
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
public class DomainIntervalsHandler extends AbstractOperatorHandler {
    
    public WcpsResult handle(WcpsResult coverageExpression, Boolean sdomLowerBound) throws PetascopeException {
        
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
        if (sdomLowerBound) {
            result = lowerBound;
        } else {
            result = upperBound;
        }
        
        WcpsResult wcpsResult = new WcpsResult(null, result);
        return wcpsResult;
    }
}
