package petascope.wcs2.extensions;

/**
 * Implementation of the Extension interface for the Process Coverage Extension defined in
 * the  OGC Web Coverage Service (WCS)– Processing Extension, version OGC.08-059r4
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ProcessCoverageExtension implements Extension {
    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.PROCESS_COVERAGE_IDENTIFIER;
    }

    public static final String WCPS_20_VERSION_STRING = "2.0";
    public static final String WCPS_10_VERSION_STRING = "1.0";
    public static final String WCPS_EXTRA_PARAM_PREFIX = "$";
}
