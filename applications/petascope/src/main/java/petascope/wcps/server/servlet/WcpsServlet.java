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
package petascope.wcps.server.servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.CORSHttpServlet;
import petascope.ConfigManager;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.PostgisQueryResult;
import petascope.util.StringUtil;
import petascope.util.WcpsConstants;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;
import petascope.util.response.MultipartResponse;
import petascope.wcps.server.core.ProcessCoveragesRequest;
import petascope.wcps.server.core.Wcps;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.executor.WcpsExecutor;
import petascope.wcps2.executor.WcpsExecutorFactory;
import petascope.wcps2.metadata.service.CoordinateTranslationService;
import petascope.wcps2.metadata.service.CoverageRegistry;
import petascope.wcps2.metadata.service.RasqlRewriteMultipartQueriesService;
import petascope.wcps2.metadata.service.RasqlTranslationService;
import petascope.wcps2.metadata.service.SubsetParsingService;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataService;
import petascope.wcps2.parser.WcpsTranslator;
import petascope.wcps2.result.VisitorResult;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;

//important limitation: this will only return the first result if several are available.
//The reason is that WCPS currently has no standardized way to return multiple byte streams to
//the user.
//This is the servlet interface of WCPS. It mostly consists of sanity checks and initialization,
//the meat is onyl a few lines. The WCPS class does the actual work.
public class WcpsServlet extends CORSHttpServlet {

    private static Logger log = LoggerFactory.getLogger(WcpsServlet.class);

    private DbMetadataSource meta;
    private String rasdamanDatabase;
    private String rasdamanUrl;
    // path to the default HTML response of the servlet
    private String servletHtmlPath = WcpsConstants.MSG_SERVLET_HTMLPATH;
    // String containing the HTML code for the default response
    private String defaultHtmlResponse;
    private final String WCPS_PROCESS_COVERAGE_XSD = WcpsConstants.MSG_WCPS_PROCESS_COVERAGE_XSD;

    @Override
    public void init() throws ServletException {
        try {
            String confDir = this.getServletContext().getInitParameter(ConfigManager.CONF_DIR);
            ConfigManager.getInstance(confDir);

            log.info("WCPS: initializing metadata database");
            meta = new DbMetadataSource(ConfigManager.METADATA_DRIVER,
                                        ConfigManager.METADATA_URL,
                                        ConfigManager.METADATA_USER,
                                        ConfigManager.METADATA_PASS, false);
            log.info("WCPS: initializing WCPS core");

            servletHtmlPath = getServletContext().getRealPath(servletHtmlPath);
            defaultHtmlResponse = FileUtils.readFileToString(new File(servletHtmlPath));

            log.info("WCPS: initialization complete");
        } catch (Exception e) {
            log.error("WCPS: initialization error", e);
            log.error("WCPS: closing metadata database");
            log.error("WCPS: done with init error");
            throw new ServletException("WCPS initialization error", e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        log.debug("WCPS: invoked with GET");
        printUsage(response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        super.doGet(request, response);
        log.debug("WCPS: invoked with POST");
        CoverageRegistry coverageRegistry = new CoverageRegistry(meta);
        CoordinateTranslationService coordinateTranslationService = new CoordinateTranslationService(coverageRegistry);
        WcpsCoverageMetadataService wcpsCoverageMetadataService = new WcpsCoverageMetadataService(coordinateTranslationService);
        RasqlTranslationService rasqlTranslationService = new RasqlTranslationService();
        SubsetParsingService subsetParsingService = new SubsetParsingService();
        WcpsTranslator wcpsTranslator = new WcpsTranslator(coverageRegistry, wcpsCoverageMetadataService,
                rasqlTranslationService, subsetParsingService);

        String query = StringUtil.urldecode(request.getParameter(WcpsConstants.MSG_QUERY), null);

        OutputStream os = response.getOutputStream();

        boolean isMultiPart = false;
        List<byte[]> results = new ArrayList<byte[]>();
        VisitorResult wcpsResult = null;
        try {
            wcpsResult = wcpsTranslator.translate(query);
            WcpsExecutor executor = WcpsExecutorFactory.getExecutor(wcpsResult);

            // Handle Multipart by rewriting multiple queries if it is necessary
            // NOTE: not support multipart if return metadata value (e.g: identifier())
            RasqlRewriteMultipartQueriesService multipartService =
                new RasqlRewriteMultipartQueriesService(wcpsTranslator.getCoverageAliasRegistry(), wcpsResult);
            isMultiPart = multipartService.isMultiPart();

            if (wcpsResult instanceof WcpsMetadataResult) {
                results.add(executor.execute(wcpsResult));
            } else {
                // create multiple rasql queries from a Rasql query result (if it is multipart)
                Stack<String> rasqlQueries = multipartService.rewriteQuery(wcpsTranslator.getCoverageAliasRegistry(),
                                             ((WcpsResult)wcpsResult).getRasql());
                // Run all the Rasql queries and get result
                while (!rasqlQueries.isEmpty()) {
                    // Execute multiple Rasql queries with different coverageID to get List of byte arrays
                    String rasql = rasqlQueries.pop();
                    log.debug("Executing rasql query: " + rasql);

                    ((WcpsResult)wcpsResult).setRasql(rasql);
                    results.add(executor.execute(wcpsResult));
                }
            }

            String mimeType = wcpsResult.getMimeType();

            if (isMultiPart) {
                MultipartResponse multi = new MultipartResponse(response);
                for (byte[] data : results) {
                    multi.startPart(mimeType);
                    IOUtils.write(data, os);
                    multi.endPart();
                }
                multi.finish();
            } else {
                response.setContentType(mimeType);
                IOUtils.write(results.get(0), os);
            }

        } catch (WCPSProcessingError ex) {
            response.setStatus(ExceptionCode.WcpsError.getHttpErrorCode());
            printError(response, "WCPS Error: " + ex.getMessage(), ex);
        } catch (PetascopeException ex) {
            response.setStatus(ExceptionCode.WcpsError.getHttpErrorCode());
            printError(response, "Petascope Error: " + ex.getMessage(), ex);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (meta != null) {
            meta.clearCache();
            meta.closeConnection();
        }
    }

    @Override
    public String getServletInfo() {
        return "Web Coverage Processing Service (Project PetaScope)";

    }

    private void printError(HttpServletResponse response, String message, Exception e)
    throws IOException {
        log.error("WCPS: error");
        log.error("WCPS: setting response mimetype to text/html; charset=utf-8");
        response.setContentType("text/html; charset=utf-8");
        log.error("WCPS: returning the following error message");
        e.printStackTrace(System.out);
        log.error("WCPS: end of error message");
        PrintWriter out = new PrintWriter(response.getOutputStream());

        out.println(
            "<html><head><title>Web Coverage Processing Service</title></head><body>");
        out.println("<h1>An error has occured</h1>");
        out.println("<p>" + message + "</p>");
        log.error("StackTrace:", e);
        out.println("</body></html>");
        out.close();
        log.error("WCPS: done with error");
    }

    private void printUsage(HttpServletResponse response) throws IOException {
        log.error("WCPS: setting response mimetype to text/html; charset=utf-8");
        log.error("WCPS: returning usage message");
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = new PrintWriter(response.getOutputStream());

        out.println(defaultHtmlResponse);

        out.close();
        log.warn("WCPS: done nothing");
    }
}
