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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.legacy.readdatabase;

import java.util.List;
import org.rasdaman.migration.domain.legacy.LegacyCoverageMetadata;
import org.rasdaman.migration.domain.legacy.LegacyDbMetadataSource;
import org.rasdaman.migration.domain.legacy.LegacyWcsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Save one/all the coverages from legacy database by DbMetadataSource 
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class ReadLegacyCoveragesService {
    
    private static final Logger log = LoggerFactory.getLogger(ReadLegacyCoveragesService.class);
    
    @Autowired private
    LegacyDbMetadataSource meta;
    
    /**
     * Returns all the coverage ids from the legacy ps_coverage table
     * @return 
     * @throws java.lang.Exception 
     */
    public List<String> readAllCoverageIds() throws Exception {
        List<String> coverageIds = meta.coverages();
        
        return coverageIds;
    }
    
    /**
     * Read one legacy coverage 
     * @param coverageName the coverage which is fetched
     * @return
     * @throws Exception 
     */
    public LegacyCoverageMetadata read(String coverageName) throws Exception {
        long start = System.currentTimeMillis();
        LegacyCoverageMetadata coverageMetadata = LegacyWcsUtil.getMetadata(meta, coverageName);
        long end = System.currentTimeMillis();
        log.debug("Time to read legacy coverage metadata from database is: " + String.valueOf(end - start));
        
        return coverageMetadata;
    }
}
