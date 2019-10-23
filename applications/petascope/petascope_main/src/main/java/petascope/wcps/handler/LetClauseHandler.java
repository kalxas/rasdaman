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

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.WCPSException;
import petascope.wcps.metadata.service.CoverageAliasRegistry;
import petascope.wcps.metadata.service.LetClauseAliasRegistry;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.DimensionIntervalList;

/**
 * Translation variable in LET clause to rasql.
 * 
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class LetClauseHandler {

    @Autowired
    private LetClauseAliasRegistry letClauseAliasRegistry;
    @Autowired
    private CoverageAliasRegistry coverageAliasRegistry;
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LetClauseHandler.class);

    public WcpsResult handle(String variableName, WcpsResult wcpsResult) {
        
        this.validate(variableName);
        
        this.letClauseAliasRegistry.add(variableName, wcpsResult);
        
        WcpsResult result = new WcpsResult(null, null);
        return result;
    }
    
    public WcpsResult handle(String variableName, DimensionIntervalList dimensionIntervalList) {
        
        this.validate(variableName);
        
        WcpsResult result = new WcpsResult(dimensionIntervalList);
        this.letClauseAliasRegistry.add(variableName, result);
        
        return result;
    }
    
    private void validate(String variableName) {
        if (this.coverageAliasRegistry.getAliasByCoverageName(variableName) != null) {
            throw new WCPSException("Variable '" + variableName + "' in LET clause must not exist in FOR clause");
        } else if (this.letClauseAliasRegistry.get(variableName) != null) {
            throw new WCPSException("Variable '" + variableName + "' in LET clause is duplicate");
        }
    }
}
