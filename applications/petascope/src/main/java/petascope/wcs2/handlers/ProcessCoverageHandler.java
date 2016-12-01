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
package petascope.wcs2.handlers;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import petascope.ConfigManager;
import petascope.core.DbMetadataSource;
import petascope.exceptions.*;
import petascope.util.PostgisQueryResult;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;
import petascope.wcps.server.core.ProcessCoveragesRequest;
import petascope.wcps.server.core.Wcps;
import petascope.wcps2.executor.WcpsExecutor;
import petascope.wcps2.executor.WcpsExecutorFactory;
import petascope.wcps2.metadata.service.*;
import petascope.wcps2.parser.WcpsTranslator;
import petascope.wcps2.result.VisitorResult;
import petascope.wcs2.extensions.ProcessCoverageExtension;
import petascope.wcs2.parsers.ProcessCoverageRequest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;

/**
 * Handler for the Process Coverages Extension
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class ProcessCoverageHandler extends AbstractRequestHandler<ProcessCoverageRequest> {

    private final WcpsTranslator wcpsTranslator;

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ProcessCoverageHandler.class);

    /**
     * Constructor for the class.
     *
     * @param meta metadata access object to request metadata information about the coverages referenced in the query
     */
    public ProcessCoverageHandler(DbMetadataSource meta) {
        super(meta);
        CoverageRegistry coverageRegistry = new CoverageRegistry(meta);
        CoordinateTranslationService coordinateTranslationService = new CoordinateTranslationService(coverageRegistry);
        WcpsCoverageMetadataService wcpsCoverageMetadataService = new WcpsCoverageMetadataService(coordinateTranslationService);
        RasqlTranslationService rasqlTranslationService = new RasqlTranslationService();
        SubsetParsingService subsetParsingService = new SubsetParsingService();
        wcpsTranslator = new WcpsTranslator(coverageRegistry, wcpsCoverageMetadataService,
                                            rasqlTranslationService, subsetParsingService);
    }

    /**
     * Handles a general WCPS request and delegates the execution to the corresponding internal services based on the
     * version of the request.
     *
     * @param request a parsed process coverage request
     * @return the result of the processing as a Response object
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     */
    @Override
    public Response handle(ProcessCoverageRequest request) throws PetascopeException, WCSException, SecoreException {
        /*if (request.getWcpsVersion().equals(ProcessCoverageExtension.WCPS_20_VERSION_STRING)) {
            return handleWCPS2Request(request);
        } else {
            return handleWCPS1Request(request);
        } */
        return handleWCPS2Request(request);
    }

    /**
     * Handles a WCPS 1.0 request and returns the expected result.
     *
     * @param request the request to be processed.
     * @return the result of the query contained in the request
     * @throws SecoreException
     * @throws PetascopeException
     */
    private Response handleWCPS1Request(ProcessCoverageRequest request) throws SecoreException, PetascopeException {
        String xmlRequest = RasUtil.abstractWCPStoXML(substituteExtraParametersInQuery(request));
        List<byte[]> results = new ArrayList<byte[]>();
        String mime;
        try {
            Wcps wcps1Service = new Wcps(null, meta);
            ProcessCoveragesRequest processCoverageRequest = wcps1Service.pcPrepare(
                        ConfigManager.RASDAMAN_URL,
                        ConfigManager.RASDAMAN_DATABASE,
                        IOUtils.toInputStream(xmlRequest)
                    );

            String query = processCoverageRequest.getRasqlQuery();
            mime = processCoverageRequest.getMime();
            if (processCoverageRequest.isRasqlQuery()) {
                RasQueryResult res = null;
                try {
                    res = new RasQueryResult(processCoverageRequest.execute());
                } catch (Exception ex) {
                    throw new WCSException(ExceptionCode.SemanticError, ex);
                }
                if (!res.getMdds().isEmpty() || !res.getScalars().isEmpty()) {
                    for (String s : res.getScalars()) {
                        results.add(s.getBytes(Charset.forName("UTF-8")));
                    }
                    for (byte[] bs : res.getMdds()) {
                        results.add(bs);
                    }
                } else {

                }
            } else if (processCoverageRequest.isPostGISQuery()) {
                PostgisQueryResult res = new PostgisQueryResult(processCoverageRequest.execute());
                results.add(res.toCSV(res.getValues()).getBytes());
            } else {
                results.add(query.getBytes());
            }
        } catch (WCPSException e) {
            throw new WCSException(e.getExceptionCode(), e.getMessage(), e);
        } catch (SAXException e) {
            throw new WCSException(ExceptionCode.XmlNotValid, e.getMessage(), e);
        } catch (IOException e) {
            throw new WCSException(ExceptionCode.IOConnectionError, e.getMessage(), e);
        } catch (SQLException e) {
            throw new WCSException(ExceptionCode.InternalSqlError, e.getMessage(), e);
        }

        // No GML return with WCPS query
        return new Response(results, null, mime, true);
    }

    /**
     * Handles a WCPS 2.0 request and returns the expected result.
     *
     * @param request the request to be process
     * @return the result of the query contained in the request
     * @throws WCSException
     * @todo Implement it as soon as WCPS2.0 fixes are done.
     */
    private Response handleWCPS2Request(ProcessCoverageRequest request) throws WCSException, PetascopeException {
        String coverageID = null;
        boolean isMultiPart = false;
        List<byte[]> results = new ArrayList<byte[]>();
        String query = request.getQuery();
        VisitorResult visitorResult = wcpsTranslator.translate(query);
        WcpsExecutor executor = WcpsExecutorFactory.getExecutor(visitorResult);
        // Handle Multipart by rewriting multiple queries if it is necessary
        // NOTE: not support multipart if return metadata value (e.g: identifier())
        RasqlRewriteMultipartQueriesService multipartService =
            new RasqlRewriteMultipartQueriesService(wcpsTranslator.getCoverageAliasRegistry(), visitorResult);
        isMultiPart = multipartService.isMultiPart();

        if (visitorResult instanceof WcpsMetadataResult) {
            results.add(executor.execute(visitorResult));
        } else {
            WcpsResult wcpsResult = (WcpsResult)visitorResult;
            // In case of 0D, metadata is null
            if (wcpsResult.getMetadata() != null) {
                coverageID = wcpsResult.getMetadata().getCoverageName();
            }
            // create multiple rasql queries from a Rasql query result (if it is multipart)
            Stack<String> rasqlQueries = multipartService.rewriteQuery(wcpsTranslator.getCoverageAliasRegistry(),
                                         wcpsResult.getRasql());
            // Run all the Rasql queries and get result
            while (!rasqlQueries.isEmpty()) {
                // Execute multiple Rasql queries with different coverageID to get List of byte arrays
                String rasql = rasqlQueries.pop();
                log.debug("Executing rasql query: " + rasql);

                ((WcpsResult)visitorResult).setRasql(rasql);
                results.add(executor.execute(visitorResult));
            }
        }

        String mimeType = visitorResult.getMimeType();
        return new Response(results, null, mimeType, true, isMultiPart, coverageID);
    }

    /**
     * Substitutes the cardinal parameters inside the query with their equivalent extra parameters.
     *
     * @param request the Process coverage request
     * @return the modified query
     */
    private String substituteExtraParametersInQuery(ProcessCoverageRequest request) {
        String query = request.getQuery();
        for (Map.Entry<Integer, String> extraParam : request.getExtraParameters().entrySet()) {
            query = query.replace(ProcessCoverageExtension.WCPS_EXTRA_PARAM_PREFIX + extraParam.getKey(), extraParam.getValue().trim());
        }
        return query;
    }
}
