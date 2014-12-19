package petascope.wcps2.metadata;

import petascope.ConfigManager;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.wcps.metadata.CoverageInfo;
import petascope.wcps2.error.managed.processing.CoverageMetadataException;
import petascope.wcps2.error.managed.processing.CoverageMetadataNotInitializedException;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Adds an alias for a coverage (e.g. from for clauses, for coverageAlias in coverageName)
     *
     * @param coverageName  the name of the coverage
     * @param coverageAlias the alias that can be used for this coverage
     */
    public void addCoverageMapping(String coverageName, String coverageAlias) {
        coverageMappings.put(coverageAlias, coverageName);
    }

    /**
     * Returns a full coverage object based on the coverage name
     *
     * @param coverageName the name of the coverage
     * @return a full coverage object
     * @throws WCPSProcessingError
     */
    public Coverage lookupCoverage(String coverageName) throws WCPSProcessingError {
        try {
            initalizeMetadataSource();
            CoverageMetadata metadata = metadataSource.read(coverageName);
            return new Coverage(coverageName, new CoverageInfo(metadata), metadata);
        } catch (PetascopeException e) {
            throw new CoverageMetadataException(e);
        } catch (SecoreException e) {
            throw new CoverageMetadataException(e);
        }
    }

    /**
     * Returns the first coverage in the registry.
     * @return
     */
    public Coverage getFirstCoverage(){
        return getCoverageByAlias(coverageMappings.keySet().iterator().next());
    }

    public Coverage getCoverageByAlias(String coverageAlias) {
        return lookupCoverage(coverageMappings.get(coverageAlias));
    }

    public boolean coverageAliasExists(String coverageAlias) {
        return coverageMappings.containsKey(coverageAlias);
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

    private DbMetadataSource metadataSource;
    private Map<String, String> coverageMappings = new HashMap<String, String>();

}
