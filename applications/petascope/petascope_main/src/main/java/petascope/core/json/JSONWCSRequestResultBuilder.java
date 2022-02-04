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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.json;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.json.cis11.JSONCIS11GetCoverage;
import petascope.exceptions.PetascopeException;
import petascope.util.JSONUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Build GML result for WCS requests (GetCoverage) in
 * application/json.
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class JSONWCSRequestResultBuilder {
    
    @Autowired
    private JSONGetCoverageBuilder jsonGetCoverageBuilder;
    
    /**
     * Build result for WCS GetCoverage with result in GML (it can be only 1 coverage)
     */
    public String buildGetCoverageResult(WcpsCoverageMetadata wcpsCoverageMetadata, List<Object> pixelValues) 
            throws PetascopeException {
        JSONCIS11GetCoverage obj = this.jsonGetCoverageBuilder.buildWCSGetCoverageResult(wcpsCoverageMetadata, pixelValues);
        String result = JSONUtil.serializeObjectToJSONString(obj);
        
        return result;
    }
}
