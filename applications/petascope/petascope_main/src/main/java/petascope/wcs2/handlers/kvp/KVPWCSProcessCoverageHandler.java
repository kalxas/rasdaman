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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
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
import java.util.regex.Matcher;
import static org.rasdaman.config.ConfigManager.UPLOADED_FILE_DIR_TMP;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.controller.AbstractController.getValueByKeyAllowNull;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_QUERY;
import static petascope.core.KVPSymbols.KEY_QUERY_SHORT_HAND;
import petascope.core.Pair;
import petascope.util.StringUtil;
import static petascope.util.StringUtil.DOLLAR_SIGN;
import static petascope.util.StringUtil.POSITIONAL_PARAMETER_PATTERN;
import petascope.core.service.GdalFileToCoverageTranslatorService;
import petascope.wcps.metadata.service.TempCoverageRegistry;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcst.handlers.InsertCoverageHandler;

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
    @Autowired
    private GdalFileToCoverageTranslatorService gdalFileToCoverageTranslatorService;
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;
    @Autowired
    private TempCoverageRegistry tempCoverageRegistry;
    @Autowired
    private InsertCoverageHandler insertCoverageHandler;

    public KVPWCSProcessCoverageHandler() {
    }

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
    }

    /**
     * Handles a general WCPS request and delegates the execution to the
     * corresponding internal services based on the version of the request.
     *
     * @return the result of the processing as a Response object
     */
    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, WCSException, SecoreException, WMSException {
        // Validate before handling the request
        this.validate(kvpParameters);

        String coverageID = null;
        String wcpsQuery = getValueByKeyAllowNull(kvpParameters, KEY_QUERY);
        if (wcpsQuery == null) {
            wcpsQuery = getValueByKeyAllowNull(kvpParameters, KEY_QUERY_SHORT_HAND);
        }
        
        String newWcpsQuery = this.adjustWcpsQueryByPositionalParameters(kvpParameters, wcpsQuery);
              
        VisitorResult visitorResult;
        List<byte[]> results = new ArrayList<>();
        
        try {
            visitorResult = wcpsTranslator.translate(newWcpsQuery);
            WcpsExecutor executor = wcpsExecutorFactory.getExecutor(visitorResult);

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
                    ((WcpsResult) visitorResult).setRasql(rasql);
                    results.add(executor.execute(visitorResult));
                }
            }
        } finally {
            this.coverageAliasRegistry.clear();
            this.tempCoverageRegistry.clear();
        }

        String mimeType = visitorResult.getMimeType();

        return new Response(results, mimeType, coverageID);
    }
    
    /**
     * Build a wcpsQuery which will return a Rasql query
     */
    public String buildRasqlQuery(String wcpsQuery) throws WCPSException, PetascopeException {
        VisitorResult visitorResult = wcpsTranslator.translate(wcpsQuery);
        WcpsResult wcpsResult = (WcpsResult) visitorResult;        
        String rasql = wcpsResult.getRasql();
        
        return rasql;
    }

    /**
     * Process a WCPS query and returns the Response
     */
    public Response processQuery(final String wcpsQuery) throws PetascopeException, WCSException, SecoreException, WMSException {
        Map<String, String[]> kvpParameters = new HashMap<String, String[]>() {
            {
                put(KVPSymbols.KEY_QUERY, new String[]{wcpsQuery});
            }
        };

        return this.handle(kvpParameters);
    }
    
    
    /**
     * Replace the positional parameters with proper values
     * For example: for $c in (covA), $d in (decode($1)) return $c + $d + $2
     * -> for $c in (covA), $d in (decode($1)) return $c + $d + 5
     * with $1 is TMP_COV (created from an uploaded file) and $2=5 (uploaded in query POST body)
     */
    private String adjustWcpsQueryByPositionalParameters(Map<String, String[]> kvpParameters, String wcpsQuery) throws PetascopeException, SecoreException {
        
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = POSITIONAL_PARAMETER_PATTERN.matcher(wcpsQuery);
        while (matcher.find()) {
            // e.g: $1, $2,...
            String positionalParameter = matcher.group();
            String value = getValueByKeyAllowNull(kvpParameters, StringUtil.stripDollarSign(positionalParameter));
            
            if (value != null) {            
                if (value.startsWith(UPLOADED_FILE_DIR_TMP)) {
                    String filePath = value;
                    // e.g: $1 -> /tmp/rasdaman_petacope/rasdaman...tif (uploaded file in POST body)
                    //      $2 -> 5 (uploaded value in POST body)
                    Coverage coverage = this.gdalFileToCoverageTranslatorService.translate(filePath);

                    // @TODO: until rasdaman works without error with SELECT decode() in rasql, a temp collection will be created for a temp coverage
                    String decodeExpression = coverage.getRasdamanRangeSet().getDecodeExpression();
                    this.insertCoverageHandler.insertTempCoverage(coverage, decodeExpression);
                    // @TODO: a coverage from decode() does not have a rasdaman collection name. 
                    // It sets this temp collection until the rasql SELECT decode() works fine
                    coverage.getRasdamanRangeSet().setCollectionName(coverage.getCoverageId());

                    String coverageId = coverage.getCoverageId();
                    this.coverageRepositoryService.localCoveragesCacheMap.put(coverageId, new Pair<>(coverage, true));

                    // e.g: $1 -> (TEMP_COV_abc_202001010, /tmp/rasdaman_petacope/rasdaman...tif)
                    this.tempCoverageRegistry.add(positionalParameter, coverageId, value);
                } else {                
                    // e.g: replace $2 in query with 5
                    matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(value));
                }
            }
                        
        }
        
        matcher.appendTail(stringBuffer);
        String result = stringBuffer.toString();

        return result;
    }
}
