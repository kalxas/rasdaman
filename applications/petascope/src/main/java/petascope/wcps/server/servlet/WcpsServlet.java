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
import java.util.Iterator;
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
import petascope.ConfigManager;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.PostgisQueryResult;
import petascope.util.WcpsConstants;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;
import petascope.wcps.server.core.ProcessCoveragesRequest;
import petascope.wcps.server.core.Wcps;

//important limitation: this will only return the first result if several are available.
//The reason is that WCPS currently has no standardized way to return multiple byte streams to
//the user.
//This is the servlet interface of WCPS. It mostly consists of sanity checks and initialization,
//the meat is onyl a few lines. The WCPS class does the actual work.
public class WcpsServlet extends HttpServlet {

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

            System.out.println("WCPS: initializing metadata database");
            meta = new DbMetadataSource(ConfigManager.METADATA_DRIVER,
                    ConfigManager.METADATA_URL,
                    ConfigManager.METADATA_USER,
                    ConfigManager.METADATA_PASS, false);
            System.out.println("WCPS: initializing WCPS core");

            servletHtmlPath = getServletContext().getRealPath(servletHtmlPath);
            defaultHtmlResponse = FileUtils.readFileToString(new File(servletHtmlPath));

            System.out.println("WCPS: initialization complete");
        } catch (Exception e) {
            System.out.println("WCPS: initialization error");
            System.out.println("WCPS: closing metadata database");

            if (meta != null) {
                meta.close();
            }

            System.out.println("WCPS: done with init error");
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
        log.debug("WCPS: invoked with POST");
        OutputStream webOut = null;

        meta.clearCache();
        Wcps wcps;

        try{
            wcps = new Wcps(new File(getServletContext().getRealPath(WCPS_PROCESS_COVERAGE_XSD)), meta);
        }catch (Exception e) {

            throw new ServletException("Error initializing WCPS",e);
        }

        try {
            String xmlRequest = null;
            response.setHeader("Access-Control-Allow-Origin", "*");

            if (ServletFileUpload.isMultipartContent(request)) {
                @SuppressWarnings("unchecked")
                        Iterator<FileItem> fileItems =
                        (Iterator<FileItem>) (new ServletFileUpload(
                        new DiskFileItemFactory())).parseRequest(
                        request).iterator();

                if (!fileItems.hasNext()) {
                    throw new IOException(
                            "Multipart POST request contains no parts");
                }

                FileItem fileItem = fileItems.next();

                if (fileItems.hasNext()) {
                    throw new IOException(
                            "Multipart POST request contains too many parts");
                }

                if (!fileItem.isFormField()
                        && fileItem.getContentType().equals("text/xml")) {
                    xmlRequest = fileItem.getString();
                }

                if (xmlRequest == null) {
                    log.warn(
                            "WCPS: no XML file was uploaded within multipart POST request");
                    printUsage(response);
                    return;
                }

                log.debug("WCPS: received XML via a multipart POST request");
            } else {
                String xml = request.getParameter(WcpsConstants.MSG_XML);
                String query = request.getParameter(WcpsConstants.MSG_QUERY);

                if (xml != null) {
                    log.debug("WCPS: received XML via a 'xml' parameter in a POST request");
                    xmlRequest = xml;
                } else if (query != null) {
                    log.debug("WCPS: received the following  query via a 'query' "
                            + "parameter in a POST request:");
                    log.debug(query);

                    xmlRequest = RasUtil.abstractWCPStoXML(query);
                } else {
                    log.debug("WCPS: no request was received");
                    printUsage(response);
                    return;
                }
            }

            log.debug("-------------------------------------------------------");
            log.debug("Converting to rasql");
            ProcessCoveragesRequest processCoverageRequest = wcps.pcPrepare(
                    ConfigManager.RASDAMAN_URL, ConfigManager.RASDAMAN_DATABASE, IOUtils.toInputStream(xmlRequest));
            log.debug("-------------------------------------------------------");

            String query = processCoverageRequest.getRasqlQuery();
            String mime = processCoverageRequest.getMime();

            response.setContentType(mime);
            webOut = response.getOutputStream();

            /* Alireza
            Object res = processCoverageRequest.execute();
            if (res instanceof RasQueryResult){
                log.debug("executing request");
                log.debug("[" + mime + "] " + query);

                for (String s : ((RasQueryResult) res).getScalars()) {
                    webOut.write(s.getBytes());
                }
                for (byte[] bs : ((RasQueryResult) res).getMdds()) {
                    webOut.write(bs);
                }

            } else if (res instanceof String){
                webOut.write(out.getBytes());
            }
            */

            if (processCoverageRequest.isRasqlQuery()) {
                log.debug("executing request");
                log.debug("[" + mime + "] " + query);

                RasQueryResult res = new RasQueryResult(processCoverageRequest.execute());
                for (String s : res.getScalars()) {
                    webOut.write(s.getBytes());
                }
                for (byte[] bs : res.getMdds()) {
                    webOut.write(bs);
                }
            // Execute the query ... (?)
            } else if(processCoverageRequest.isPostGISQuery()){
                PostgisQueryResult res = new PostgisQueryResult(processCoverageRequest.execute());
                webOut.write(res.toCSV(res.getValues()).getBytes());

            } else {
                log.debug("metadata result, no rasql to execute");
                webOut.write(query.getBytes());
            }
            log.debug("WCPS: done");
        } catch (WCPSException e) {
            response.setStatus(e.getExceptionCode().getHttpErrorCode());
            printError(response, "WCPS Error: " + e.getMessage(), e);
        } catch (SecoreException e) {
            response.setStatus(e.getExceptionCode().getHttpErrorCode());
            printError(response, "SECORE Error: " + e.getMessage(), e);
        } catch (Exception e) {
            response.setStatus(ExceptionCode.DEFAULT_EXIT_CODE);
            printError(response, "Error: " + e.getMessage(), e);
        } finally {
            if (webOut != null) {
                try {
                    webOut.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();

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
