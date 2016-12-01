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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.metadata.service;

import petascope.ConfigManager;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.wcps2.error.managed.processing.CoverageMetadataException;
import petascope.wcps2.error.managed.processing.CoverageMetadataNotInitializedException;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;

/**
 * Thin wrapper around the coverage metadataSource functionality to insulate the parser code from future changes in metadataSource
 * classes
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageRegistry {

    /**
     * Constructor for the class
     */
    public CoverageRegistry() {
        metadataSource = null;
    }

    public CoverageRegistry(DbMetadataSource metadataSource) {
        this.metadataSource = metadataSource;
        this.wcpsCoverageMetadataTranslator = new WcpsCoverageMetadataTranslator();
    }

    /**
     * Returns a full coverage object based on the coverage name
     *
     * @param coverageName the name of the coverage
     * @return a full coverage object
     * @throws WCPSProcessingError
     */
    public WcpsCoverageMetadata lookupCoverage(String coverageName) throws WCPSProcessingError {
        try {
            initalizeMetadataSource();
            CoverageMetadata metadata = metadataSource.read(coverageName);
            return wcpsCoverageMetadataTranslator.translate(metadata);
        } catch (PetascopeException e) {
            throw new CoverageMetadataException(e);
        } catch (SecoreException e) {
            throw new CoverageMetadataException(e);
        }
    }

    /**
     * Checks if a coverage exists
     *
     * @param coverageName the name of the coverage
     * @return true if it exists, false otherwise
     */
    public boolean coverageExists(String coverageName) {
        initalizeMetadataSource();
        return metadataSource.existsCoverageName(coverageName);
    }

    /**
     * Returns the metadataSource source to offer access to static methods
     *
     * @return
     */
    public DbMetadataSource getMetadataSource() {
        initalizeMetadataSource();
        return metadataSource;
    }

    /**
     * Initializes the internal metadataSource object
     */
    private void initalizeMetadataSource() {
        if (metadataSource == null) {
            try {
                metadataSource = new DbMetadataSource(ConfigManager.METADATA_DRIVER, ConfigManager.METADATA_URL,
                                                      ConfigManager.METADATA_USER, ConfigManager.METADATA_PASS, false);
            } catch (Exception e) {
                e.printStackTrace();
                throw new CoverageMetadataNotInitializedException(e);
            }
        }
    }

    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    private DbMetadataSource metadataSource;
}
