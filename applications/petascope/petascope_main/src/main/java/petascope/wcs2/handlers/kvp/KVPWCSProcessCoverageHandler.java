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
package petascope.wcs2.handlers.kvp;

import petascope.wcps.metadata.service.CoverageAliasRegistry;
import petascope.wcps.metadata.service.RasqlRewriteMultipartQueriesService;
import petascope.exceptions.WCSException;
import petascope.core.response.Response;
import org.slf4j.LoggerFactory;
import petascope.exceptions.*;
import petascope.wcps.result.executor.WcpsExecutor;
import petascope.wcps.result.executor.WcpsExecutorFactory;
import petascope.wcps.parser.WcpsTranslator;
import petascope.wcps.result.VisitorResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;

/**
 * Handler for the Process Coverages Extension
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class KVPWCSProcessCoverageHandler extends KVPWCSAbstractHandler {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(KVPWCSProcessCoverageHandler.class);

    @Autowired
    private WcpsTranslator wcpsTranslator;
    @Autowired
    private WcpsExecutorFactory wcpsExecutorFactory;
    @Autowired
    private RasqlRewriteMultipartQueriesService rasqlRewriteMultipartQueriesService;
    @Autowired
    private CoverageAliasRegistry coverageAliasRegistry;

    /**
     * Constructor for the class.
     *
     */
    public KVPWCSProcessCoverageHandler() {

    }

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {

    }

    /**
     * Handles a general WCPS request and delegates the execution to the
     * corresponding internal services based on the version of the request.
     *
     * @param kvpParameters
     * @return the result of the processing as a Response object
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     */
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, WCSException, SecoreException, WMSException {
        // Validate before handling the request
        this.validate(kvpParameters);

        String coverageID = null;
        String wcpsQuery = kvpParameters.get(KVPSymbols.KEY_QUERY)[0];

        VisitorResult visitorResult = wcpsTranslator.translate(wcpsQuery);
        WcpsExecutor executor = wcpsExecutorFactory.getExecutor(visitorResult);

        List<byte[]> results = new ArrayList<>();
        if (visitorResult instanceof WcpsMetadataResult) {
            results.add(executor.execute(visitorResult));
        } else {
            WcpsResult wcpsResult = (WcpsResult) visitorResult;
            // In case of 0D, metadata is null
            if (wcpsResult.getMetadata() != null) {
                coverageID = wcpsResult.getMetadata().getCoverageName();
            }
            // create multiple rasql queries from a Rasql query result (if it is multipart)
            Stack<String> rasqlQueries = rasqlRewriteMultipartQueriesService.rewriteQuery(wcpsResult.getRasql());
            // Run all the Rasql queries and get result
            while (!rasqlQueries.isEmpty()) {
                // Execute multiple Rasql queries with different coverageIDs to get List of byte arrays
                String rasql = rasqlQueries.pop();
                log.debug("Executing rasql query: " + rasql);

                ((WcpsResult) visitorResult).setRasql(rasql);
                results.add(executor.execute(visitorResult));
            }
        }

        String mimeType = visitorResult.getMimeType();
        // clear the stored coverages id so next WCPS query in same HTTP request will not use existing ones
        coverageAliasRegistry.clear();

        return new Response(results, mimeType, coverageID);
    }

    /**
     * Process a WCPS query and returns the Response
     *
     * @param wcpsQuery
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.WCSException
     * @throws petascope.exceptions.SecoreException
     * @throws petascope.exceptions.WMSException
     */
    public Response processQuery(final String wcpsQuery) throws PetascopeException, WCSException, SecoreException, WMSException {
        Map<String, String[]> kvpParameters = new HashMap<String, String[]>() {
            {
                put(KVPSymbols.KEY_QUERY, new String[]{wcpsQuery});
            }
        };

        return this.handle(kvpParameters);
    }
}
