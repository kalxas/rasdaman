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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.metadata.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * This class has the purpose of keeping information about coverage aliases inside 1 query (e.g. "for c in mr" means
 * that c is an alias for mr in this query).  1 instance of this object is created for every Wcps query.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CoverageAliasRegistry {

    // NOTE: a coverage variable can be alias for multiple coverage names
    private LinkedHashMap<String, ArrayList<String>> coverageMappings = new LinkedHashMap<>();

    public CoverageAliasRegistry() {
        
    }
    
    /**
     * As this bean exists for a HTTP request, so in case of WCS multipart, it still contains the data from the first WCPS query, so need to clear it
     */
    public void clear() {
        coverageMappings = new LinkedHashMap<>();
    }

    public void addCoverageMapping(String coverageAlias, String coverageName) {
        ArrayList<String> values = coverageMappings.get(coverageAlias);
        if (values != null) {
            // If key -> value exist then just add new coverage name to value
            coverageMappings.get(coverageAlias).add(coverageName);
        } else {
            // if key does not exist then need to add key first then add value for this key
            coverageMappings.put(coverageAlias, new ArrayList<String> (Arrays.asList(coverageName)));
        }
    }

    /* Always get the first coverageName in the arrayList to create defaultRasql query
    * e.g: for c in (mr, rgb) ... then rgb can be used later to create another Rasql query for multipart
    * NOTE: due to coverage variable name (e.g: c) and axis iterator is same syntax (e.g: $px)
    * then check if the alias is for an existing coverage first, if not then check if it is an axis iterator.
    */
    public String getCoverageName(String alias) {
        String coverageName = null;
        if (coverageMappings.get(alias) != null) {
            coverageName = coverageMappings.get(alias).get(0);
        }

        return coverageName;
    }

    /**
     * return list( $coverageVariableName -> arrayList (coverageNames) )
     * e.g: $c -> (mr, rgb), $d -> (mr1, rgb1)
     * @return
     */
    public LinkedHashMap<String, ArrayList<String>> getCoverageMappings() {
        return this.coverageMappings;
    }
}
