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
package petascope.wcps.metadata.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.JSONUtil;
import petascope.wcps.result.ParameterResult;
import petascope.wcps.result.VisitorResult;
import petascope.wcps.result.WcpsResult;

/**
 * This class has the purpose of keeping information about LET clause variables aliases inside 1 query.
 * e.g: LET $a = $c[Lat(30:50), Long(60:70)],
 *          $b = $c + 2
 * 
 * $a and $b are variables in LET clause.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LetClauseAliasRegistry {

    // NOTE: a coverage variable can be alias for multiple coverage names
    private Map<String, VisitorResult> variablesMap = new LinkedHashMap<>();

    public LetClauseAliasRegistry() {
        
    }
    
    /**
     * Add a new variable and its processed coverage expression
     * e.g: $a -> output of Subsetting handler for ($c[Lat(20:30), Long(40:60)])
     */
    public void add(String variableName, VisitorResult wcpsResult) {
        this.variablesMap.put(variableName, wcpsResult);
    }
    
    /**
     * Check if map of variables already contains a variable name
     */
    public boolean exist(String variableName, WcpsResult wcpsResult) {
        return this.variablesMap.containsKey(variableName);
    }
    
    /**
     * Get the processed coverage expression by variable name
     */
    public VisitorResult get(String variableName) {
        VisitorResult tmp = this.variablesMap.get(variableName);
        if (tmp == null) {
            return null;
        }
        
        VisitorResult result = tmp;
        try {
            if (tmp instanceof WcpsResult) {
                result = (WcpsResult) JSONUtil.clone(tmp);
            } else {
                return tmp;
            }
        } catch (Exception ex) {
            throw new WCPSException(ExceptionCode.InternalComponentError, "Cannot clone WCPS metadata object to another object. Reason: " + ex.getMessage(), ex);
        }            
        
        return result;
    }
}
