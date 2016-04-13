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
package petascope.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;

/**
 * A IMetadataSource that allows WCPS to store information about on-the-fly
 * constructed coverages (for example Construct Coverage expr).
 * Needs another MetadataSource as a backend, to retrieve metadata about
 * static coverages.
 */
public class DynamicMetadataSource implements IDynamicMetadataSource {
    
    private static Logger log = LoggerFactory.getLogger(DynamicMetadataSource.class);
    
    // Static coverages, served by the server at all times
    private Set<String> staticCoverageNames;
    // Dynamic coverages, built on-the-fly in a query
    private Set<String> dynamicCoverageNames;
    // Union of static and dynamic coverages
    private Set<String> allCoverageNames;
    // Metadata information for all available coverages
    private Map<String, CoverageMetadata> metadata;
    // Other metadata class that serves as backend
    private IMetadataSource metadataSource;

    public DynamicMetadataSource(IMetadataSource metadataSource) throws PetascopeException, SecoreException {
        log.trace("Creating dynamic metadata source from: " + metadataSource.getClass().getSimpleName());
        this.metadataSource = metadataSource;
        staticCoverageNames = metadataSource.coverages();
        dynamicCoverageNames = new HashSet<String>();
        allCoverageNames = staticCoverageNames;
        metadata = new HashMap<String, CoverageMetadata>();

        // Init metadata for static coverages
//        Iterator<String> i = staticCoverageNames.iterator();
//        while (i.hasNext()) {
//            String coverage = i.next();
//            // catch any possible exception thrown while the coverage is being read:
//            // it's not good to bring down petascope just because one coverage can't be read
//            try {
//                metadata.put(coverage, metadataSource.read(coverage));
//            } catch (PetascopeException ex) {
//                log.error("Error reading the coverage " + coverage + " from the database", ex);
//            }
//        }
    }

    @Override
    public Set<String> coverages() throws PetascopeException {
        return allCoverageNames;
    }

    @Override
    public String formatToMimetype(String format) {
        return metadataSource.formatToMimetype(format);
    }
    
    @Override
    public String mimetypeToFormat(String mime) {
        return metadataSource.formatToMimetype(mime);
    }

    @Override
    public String formatToGdalid(String format) {
        return metadataSource.formatToGdalid(format);
    }

    @Override
    public String gdalidToFormat(String gdalid) {
        return metadataSource.gdalidToFormat(gdalid);
    }

    @Override
    public CoverageMetadata read(String coverageName) throws PetascopeException, SecoreException {
        log.trace("Reading metadata for dynamic coverage: " + coverageName);
        if ((coverageName == null) || coverageName.equals("")) {
            throw new PetascopeException(ExceptionCode.InvalidRequest,
                    "Cannot retrieve coverage with null or empty name");
        }
        
        log.trace("coverages: " + this.coverages().toString());
        if (!this.coverages().contains(coverageName)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest,
                    "Coverage '" + coverageName + "' is not served by this server");
        }

        if (dynamicCoverageNames.contains(coverageName)) {
            // dynamic coverage
            return metadata.get(coverageName);
        } else {
            return metadataSource.read(coverageName);
        }
    }

    @Override
    public void addDynamicMetadata(String coverageName, CoverageMetadata meta) {
        log.trace("adding dynamic metadata for coverage: " + coverageName);
        metadata.put(coverageName, meta);
        dynamicCoverageNames.add(coverageName);
        allCoverageNames = staticCoverageNames;
        allCoverageNames.addAll(dynamicCoverageNames);
    }

    public IMetadataSource getMetadataSource() {
        return metadataSource;
    }
}
