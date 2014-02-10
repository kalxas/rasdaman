package petascope.wcs2.parsers;

import petascope.wcs2.extensions.ProcessCoverageExtension;

import java.util.Map;

/**
 * Description of a process coverage requests. It has 3 main components:
 * - the wcps query to be executed
 * - the wcps version of the standard against which the query is evaluated
 * - extra parameters to be substituted in the query
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ProcessCoverageRequest extends Request {

    /**
     * Constructor for the class
     *
     * @param query           the query string to be executed
     * @param wcpsVersion     the version of the wcps standard on which the query is based
     * @param extraParameters any extra parameters to be substituted in the query
     */
    public ProcessCoverageRequest(String query, String wcpsVersion, Map<Integer, String> extraParameters) {
        this.query = query;
        this.wcpsVersion = wcpsVersion == null ? ProcessCoverageExtension.WCPS_10_VERSION_STRING : wcpsVersion;
        this.extraParameters = extraParameters;
    }


    /**
     * Returns any extra parameters that were defined in the request
     * @return a map of extra parameters in the form position => parameter
     */
    public Map<Integer, String> getExtraParameters() {
        return extraParameters;
    }

    /**
     * Returns the wcps query to be executed
     * @return
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the version of the WCPS standard to which the query adheres.
     * @return
     */
    public String getWcpsVersion() {
        return wcpsVersion;
    }

    private final String query;
    private final String wcpsVersion;
    private final Map<Integer, String> extraParameters;

}
