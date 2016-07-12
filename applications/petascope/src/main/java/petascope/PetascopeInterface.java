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
package petascope;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import net.opengis.ows.v_1_0_0.ExceptionReport;
import nu.xom.Document;
import nu.xom.Element;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.rasdaman.RasdamanException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WCSException;
import petascope.util.KVPSymbols;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.util.PostgisQueryResult;
import petascope.util.StringUtil;
import petascope.util.WcpsConstants;
import petascope.util.XMLSymbols;
import petascope.util.XMLUtil;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;
import petascope.util.response.MultipartResponse;
import petascope.wcps.server.core.ProcessCoveragesRequest;
import petascope.wcps.server.core.Wcps;
import petascope.wcs.server.WcsServer;
import petascope.wcs2.extensions.ExtensionsRegistry;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.ProtocolExtension;
import petascope.wcs2.extensions.RESTProtocolExtension;
import petascope.wcs2.handlers.RequestHandler;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.templates.Templates;
import petascope.wms2.servlet.PetascopeInterfaceAdapter;

/**
 * This servlet is a unified entry-point for all the PetaScope services.
 *
 * @author Andrei Aiordachioaie
 * @author Dimitar Misev
 */
public class PetascopeInterface extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PetascopeInterface.class);
    private DbMetadataSource meta;
    // path to the default HTML response of the interface servlet
    private String usageFilePath = "/templates/interface-servlet.html";
    // String containing the HTML code for the default response
    private String usageMessage;
    /* Instance of WCPS service */
    private Wcps wcps;
    /* Instance of WcsServer service */
    private WcsServer wcs;

    private static final String CORS_ACCESS_CONTROL_ALLOW_ORIGIN = "*";
    private static final String CORS_ACCESS_CONTROL_ALLOW_METHODS = "POST, GET, OPTIONS";
    private static final String CORS_ACCESS_CONTROL_ALLOW_HEADERS = "Content-Type";
    private static final String CORS_ACCESS_CONTROL_MAX_AGE = "1728000";

    private static final String WCPS_QUERY_GET_PARAMETER = "query";
    private static final String WCPS_REQUEST_GET_PARAMETER = "request";

    /* Initialize the various services: WCPS, WcsServer and WcsServer-T */
    @Override
    public void init() throws ServletException {
        String confDir = this.getServletContext().getInitParameter(ConfigManager.CONF_DIR);

        // Initialize the singleton configuration manager. Now all classes can read the settings.
        try {
            ConfigManager.getInstance(confDir);
        } catch (RasdamanException ex) {
            throw new ServletException(ex);
        }

        // Initialize the logging system
        log.info("Petascope {} starting", ConfigManager.RASDAMAN_VERSION);

        // External libraries licensing issues
        log.info("To obtain a list of external packages used, please visit www.rasdaman.org .");

        // Force GeoTools referencing libraries to X->Y ordered CRSs
        System.setProperty("org.geotools.referencing.forceXY", "true");

        // Read servlet HTML usage message from disk
        try {
            usageFilePath = getServletContext().getRealPath(usageFilePath);
            usageMessage = FileUtils.readFileToString(new File(usageFilePath));
        } catch (IOException e) {
            log.error("Could not read default servlet HTML response. Stack trace: {}", e);
            throw new ServletException("Could not read interface servlet HTML response", e);
        }

        try {
            log.info("Initializing metadata database");
            meta = new DbMetadataSource(ConfigManager.METADATA_DRIVER,
                    ConfigManager.METADATA_URL,
                    ConfigManager.METADATA_USER,
                    ConfigManager.METADATA_PASS, false);
            log.info("Metadata initialization complete.");
        } catch (Exception e) {
            log.error("Stack trace: {}", e);
            throw new ServletException("Error initializing metadata database", e);
        }

        /* Initialize WCPS Service */
        try {
            log.info("WCPS: Initializing...");
            wcps = new Wcps(meta);
            log.info("WCPS: initialization complete.");
        } catch (Exception e) {
            log.error("Stack trace: {}", e);
            throw new ServletException("WCPS initialization error", e);
        }

        /**
         * Initialize the WMS 1.3.0 service
         */
        try {
            log.info("WMS13: Initializing the WMS 1.3.0 service...");
            wms13Adapter.initWMS13Servlet(this.getServletConfig());
            log.info("WMS13: Initialization complete.");
        } catch (Exception e) {
            log.error("WMS 1.3.0 could not be initialized due to {}", e);
        }

        /**
         * Initialize the WMS 1.1.0 service
         * NOTE: WMS 1.1.0 is deprecated and it cannot initialize database
         * with the error as in  in http://rasdaman.org/ticket/898. Use WMS 1.3.0 instead.
         */
        try {
            log.info("WMS11: Initializing the WMS 1.1.0 service...");
            wms13Adapter.initWMS11Servlet(this.getServletConfig());
            log.info("WMS11: Initialization complete.");
        } catch (Exception e) {
            log.error("WMS 1.1.0 could not be initialized");
        }

        /* Initialize WCS Service */
        try {
            log.info("WCS: Initialization ...");
            wcs = new WcsServer(meta);
            log.info("WCS: Initialization complete.");
        } catch (Exception e) {
            log.error("Stack trace: {}", e);
            throw new ServletException("WCS initialization error", e);
        }

        log.info("-----------------------------------------------");
        log.info("      PetaScope {} successfully started      ", ConfigManager.RASDAMAN_VERSION);
        log.info("-----------------------------------------------");
    }

    @Override
    public void destroy() {
        super.destroy();
        if (meta != null) {
            meta.closeConnection();
            meta.clearCache();
        }
    }


    /* Build a dictionary of parameter names and values, given a request string */
    private Map<String, String> buildParameterDictionary(String request) {
        HashMap<String, String> map = new HashMap<String, String>(3);
        if (request == null) {
            return map;
        }

        String[] pairs = request.split("&");
        String key = null, val = null;
        int pos = -1;
        for (int i = 0; i < pairs.length; i++) {
            pos = pairs[i].indexOf("=");
            if (pos != -1) {
                key = pairs[i].substring(0, pos);
                val = pairs[i].substring(pos + 1, pairs[i].length());
                map.put(key.toLowerCase(), val);
            }
        }

        return map;
    }

    private void setServletURL(HttpServletRequest req) {
        if ("".equals(ConfigManager.PETASCOPE_SERVLET_URL)) {
            ConfigManager.PETASCOPE_SERVLET_URL = req.getRequestURL().toString();
        }
    }

    private void setCORSHeader(HttpServletResponse httpResponse) throws ServletException {
        httpResponse.setHeader("Access-Control-Allow-Origin", CORS_ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    /**
     * @return a parameter map of the query string in lower case parameters
     */
    private Map<String, String> buildParameterMap(HttpServletRequest req) {
        Map<String, String> ret = new HashMap<String, String>();
        Set<Entry<String, String[]>> p = req.getParameterMap().entrySet();
        for (Entry<String, String[]> e : p) {
            ret.put(e.getKey().toLowerCase(), e.getValue()[0]);
        }
        return ret;
    }

    /* Respond to Post requests just like in the case of Get requests */
    @Override
    public void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, UnsupportedEncodingException {
        doGet(httpRequest, httpResponse);
    }

    /* Handle Get requests. This function delegates the request to the service
     specified in the request by the "service" parameter. */
    @Override
    public void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, UnsupportedEncodingException {
        // NOTE: To support the "+" which is missing from URI as it is converted to space, we need to manipulate the queryString and parse it to parameter maps correctly
        CustomRequestWrapper wrapperRequest  = new CustomRequestWrapper(httpRequest);
        
        setServletURL(wrapperRequest);
        setCORSHeader(httpResponse);

        // NOTE: admin can change Service Prodiver, Identification then session *reloadPage* will exist and value is true
        HttpSession session = wrapperRequest.getSession();
        if (session.getAttribute("reloadMetadata") != null && Boolean.valueOf(session.getAttribute("reloadMetadata").toString()) == true) {
            try {
                meta = new DbMetadataSource(ConfigManager.METADATA_DRIVER,
                        ConfigManager.METADATA_URL,
                        ConfigManager.METADATA_USER,
                        ConfigManager.METADATA_PASS, false);

                // Then remove session as it does not need anymore
                session.removeAttribute("reloadMetadata");
            } catch (Exception e) {
                log.error("Stack trace: {}", e);
                throw new ServletException("Error initializing metadata database", e);
            }
        } else {
            // it is just normal query without updating metadata then session is null
            meta.clearCache();
        }

        String request = null;
        String requestBody = null;

        /* Process the request */
        try {
            try {
                //Check if this is a wms request and handled it accordingly
                if (wms13Adapter.handleGetRequests(wrapperRequest, httpResponse)) {
                    //this is a wms13 request, and it was handled in the handleGetRequests
                    //stop further execution
                    return;
                }

                meta.ensureConnection();

                requestBody = IOUtils.toString(wrapperRequest.getReader());

                log.trace("POST Request length: {}", wrapperRequest.getContentLength());
                log.trace("POST request body  : \n------START REQUEST--------\n{}\n------END REQUEST------\n", requestBody);
                log.trace("GET Query string   : {}", wrapperRequest.getQueryString());

                Map<String, String> params = buildParameterDictionary(requestBody);
                Map<String, String> paramMap = buildParameterMap(wrapperRequest);
                log.trace("Request parameters : {}", params);

                // GET interface processing
                String service = paramMap.get(KVPSymbols.KEY_SERVICE);

                // REST interface checks
                // get the uri contained after the context name i.e after petascope
                // in example.com/petascope/rest/wcs/... returns [rest,wcs]
                String pathInfo = wrapperRequest.getPathInfo();
                log.debug("Analyzing path info \"{}\" for REST interface checks.", pathInfo);
                // Java API: "This method returns null if there was no extra path information."
                String[] prsUrl = (null == pathInfo) ? new String[0] : pathInfo.substring(1).split("/");
                ArrayList<String> splitURI = new ArrayList<String>(Arrays.asList(
                        prsUrl
                ));
                if (service == null && splitURI.size() > 1
                        && (splitURI.get(0).equalsIgnoreCase(RESTProtocolExtension.REST_PROTOCOL_WCS_IDENTIFIER)
                        || splitURI.get(0).equals(RESTProtocolExtension.REST_PROTOCOL_WCPS_IDENTIFIER))) {
                    service = splitURI.get(0).toUpperCase();
                }

                if (service != null) {
                    // Removed WPS support as it doesn't work. Using 52n instead -- DM 2012-may-24
//                    if (service.equals("WPS")) {
//                        WpsServer wpsServer = new WpsServer(httpResponse, httpRequest);
//                        request = wpsServer.request;
//                    } else
                    if (service.equals(petascope.wcs2.parsers.BaseRequest.SERVICE)) {
                        // extract version
                        String version = null;
                        String operation = paramMap.get(WCPS_REQUEST_GET_PARAMETER);

                        //This might be a REST operation, try to get the operation from the uri
                        if (operation == null && splitURI.size() > 2) {
                            operation = RESTProtocolExtension.mapRestResourcesToCoverageOperation(wrapperRequest.getRequestURI());
                        }

                        if (operation.equals(RequestHandler.GET_CAPABILITIES)) {
                            version = paramMap.get(KVPSymbols.KEY_ACCEPTVERSIONS);
                            if (version == null && splitURI.size() > 1) {
                                version = splitURI.get(1);
                            }
                            log.trace(KVPSymbols.KEY_ACCEPTVERSIONS + ": " + version);
                            if (version == null) {
                                version = ConfigManager.WCS_DEFAULT_VERSION;
                            } else {
                                String[] versions = version.split(KVPSymbols.VERSIONS_SEP);
                                version = "";
                                for (String v : versions) {
                                    if (ConfigManager.WCS_VERSIONS.contains(v)
                                            && !v.startsWith("1")) { // the WCS 1.1 server doesn't support GET-KVP
                                        version = v;
                                        break;
                                    }
                                }
                            }
                        } else if (operation.equals(RequestHandler.DESCRIBE_COVERAGE) || operation.equals(RequestHandler.PROCESS_COVERAGE)
                                || operation.equals(RequestHandler.GET_COVERAGE) || operation.equals(RequestHandler.INSERT_COVERAGE)
                                || operation.equals(RequestHandler.DELETE_COVERAGE) || operation.equals(RequestHandler.UPDATE_COVERAGE)) {
                            version = paramMap.get(KVPSymbols.KEY_VERSION);
                            if (version == null && splitURI.size() > 1) {
                                version = splitURI.get(1);
                            }
                        }

                        // handle request
                        request = StringUtil.urldecode(wrapperRequest.getQueryString(), wrapperRequest.getContentType());
                        handleWcsRequest(version, paramMap.get(WCPS_REQUEST_GET_PARAMETER), request, httpResponse, wrapperRequest);
                        return;
                    }
                }

                // To preserve compatibility with previous client versions, we allow
                // GET requests with parameter "query"
                String request2 = null;
                request2 = paramMap.get(WCPS_QUERY_GET_PARAMETER);
                if (request2 == null) {
                    request2 = StringUtil.urldecode(params.get(WCPS_QUERY_GET_PARAMETER), wrapperRequest.getContentType());
                }

                // splitURI list can be of size 0 if there is not path info in the HTTP request.
                if (request2 == null && splitURI.size() > 0
                        && splitURI.get(0).equalsIgnoreCase(RESTProtocolExtension.REST_PROTOCOL_WCPS_IDENTIFIER)) {
                    if (splitURI.size() > 2 && splitURI.get(1).equals(RESTProtocolExtension.REST_PROTOCOL_WCPS_IDENTIFIER)) {
                        String queryDecoded = StringUtil.urldecode(splitURI.get(2), wrapperRequest.getContentType());
                        request2 = RasUtil.abstractWCPSToRasql(queryDecoded, wcps);
                    }
                } else if (request2 != null) {
                    log.debug("Received Abstract Syntax Request via GET: \n\t\t{}", request2);
                    request2 = RasUtil.abstractWCPStoXML(request2);
                }
                request = StringUtil.urldecode(params.get(WCPS_REQUEST_GET_PARAMETER), wrapperRequest.getContentType());
                if (request == null && request2 != null) {
                    request = request2;
                }

                // Empty request ?
                if (request == null && (requestBody == null || requestBody.length() == 0)) {
                    if (paramMap.size() > 0) {
                        throw new WCSException(ExceptionCode.NoApplicableCode,
                                "Couldn't understand the recieved request, is the service attribute missing?");
                    } else {
                        printUsage(httpResponse, request);
                        return;
                    }
                }

                //   No parameters, just XML in the request body:
                if (request == null && requestBody != null && requestBody.length() > 0) {
                    request = StringUtil.urldecode(requestBody, wrapperRequest.getContentType());
                }

                log.debug("Petascope Request: \n------START REQUEST--------\n{}"
                        + "\n------END REQUEST------\n", request);

                String root = XMLUtil.getRootElementName(request);
                log.debug("Root Element name: {}", root);
                if (root == null) {
                    return;
                }
                if (root.equals(XMLSymbols.LABEL_ENVELOPE)) {
                    handleWcs2Request(request, httpResponse, wrapperRequest);
                } else if (root.endsWith(XMLSymbols.LABEL_PROCESSCOVERAGE_REQUEST)) {
                    /* ProcessCoverages is defined in the WCPS extension to WcsServer */
                    handleProcessCoverages(request, httpResponse);
                } else if (root.equals(RequestHandler.GET_CAPABILITIES)) {
                    // extract the version that the client prefers
                    Document doc = XMLUtil.buildDocument(null, request);
                    String version = "";
                    List<Element> acceptVersions = XMLUtil.collectAll(doc.getRootElement(), XMLSymbols.LABEL_ACCEPT_VERSIONS);
                    if (!acceptVersions.isEmpty()) {
                        List<Element> versions = XMLUtil.collectAll(ListUtil.head(acceptVersions), XMLSymbols.LABEL_VERSION);
                        for (Element v : versions) {
                            String val = XMLUtil.getText(v);
                            if (val != null && ConfigManager.WCS_VERSIONS.contains(val)) {
                                version = val;
                                break;
                            }
                        }
                    } else {
                        version = ConfigManager.WCS_DEFAULT_VERSION;  // by default the latest supported by petascope
                    }
                    handleWcsRequest(version, root, request, httpResponse, wrapperRequest);
                } else if (root.equals(RequestHandler.DESCRIBE_COVERAGE) || root.equals(RequestHandler.GET_COVERAGE) || root.equals(RequestHandler.PROCESS_COVERAGE)
                        || root.equals(RequestHandler.INSERT_COVERAGE) || root.equals(RequestHandler.DELETE_COVERAGE) || root.equals(RequestHandler.UPDATE_COVERAGE)) {
                    Document doc = XMLUtil.buildDocument(null, request);
                    String version = doc.getRootElement().getAttributeValue(KVPSymbols.KEY_VERSION);
                    handleWcsRequest(version, root, request, httpResponse, wrapperRequest);
                } else {
                    // error
                    handleUnknownRequest(request, httpResponse);
                }
            } catch (WCSException e) {
                throw e;
            } catch (SecoreException e) {
                throw new WCSException(ExceptionCode.SecoreError, e);
            } catch (Exception e) {
                // Finally, cast all other exceptions into a WCSException
                log.error("Runtime error : {}", e.getMessage());
                throw new WCSException(ExceptionCode.RuntimeError,
                        "Runtime error while processing request: " + e.getMessage(), e);
            }
        } // And catch all WCSExceptions, to display to the client
        catch (WCSException e) {
            printError(httpResponse, request, e);
        }
    }

    /**
     * Implement the CORS requirements to allow browser clients to request
     * resources from different origin domains. i.e. http://example.org can make
     * requests to http://example.com
     *
     * @param req the http request
     * @param resp the http response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", CORS_ACCESS_CONTROL_ALLOW_ORIGIN);
        resp.setHeader("Access-Control-Allow-Methods", CORS_ACCESS_CONTROL_ALLOW_METHODS);
        resp.setHeader("Access-Control-Allow-Headers", CORS_ACCESS_CONTROL_ALLOW_HEADERS);
        resp.setHeader("Access-Control-Max-Age", CORS_ACCESS_CONTROL_MAX_AGE);
        resp.setHeader("Content-Length", "0");
        resp.setStatus(200);
    }

    private void printUsage(HttpServletResponse httpResponse, String request) throws IOException {
        PrintWriter out = httpResponse.getWriter();
        httpResponse.setContentType("text/html");
        out.write(usageMessage);
        out.flush();
    }

    private void printError(HttpServletResponse response, String message, Exception e) {
        PrintWriter out;
        try {
            out = new PrintWriter(response.getOutputStream());
        } catch (IOException e1) {
            log.error("Could not print exception because of IO error. Stack trace:", e1);
            return;
        }

        log.error("Error stack trace:", e);
        if (e instanceof WCSException) {
            // We can send an error report
            String output = exceptionToXml((WCSException) e);
            response.setStatus(((WCSException) e).getExceptionCode().getHttpErrorCode());
            response.setContentType("text/xml; charset=utf-8");
            out.println(output);
            out.close();
        } else {
            log.trace("setting response mimetype to text/html; charset=utf-8");
            response.setContentType("text/html; charset=utf-8");
            log.trace("returning the following error message.", e);
            log.trace("end of error message");

            out.println(
                    "<html><head><title>PetaScope</title></head><body>");
            out.println("<h1>An error has occured</h1>");
            out.println("<p>" + message + "</p>");
            out.println("<p>Stack trace:<br/><small>");
            e.printStackTrace(out);
            out.println("</small></p></body></html>");
            out.close();
            log.trace("done with error");
        }

    }

    private void handleUnknownRequest(String request, HttpServletResponse httpResponse) {
        request = "'" + request + "'";
        printError(httpResponse, request, new WCSException(
                ExceptionCode.NoApplicableCode, "Could not understand request " + request));
    }

    private String exceptionReportToXml(ExceptionReport report) {
        String output = null;
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(report.getClass().getPackage().getName());
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.setProperty("jaxb.schemaLocation",
                    "http://www.opengis.net/ows http://schemas.opengis.net/ows/2.0/owsExceptionReport.xsd");
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new PetascopeXmlNamespaceMapper());
            StringWriter strWriter = new StringWriter();
            marshaller.marshal(report, strWriter);
            output = strWriter.toString();
            String sub = output.substring(output.indexOf("<ows:Exception "), output.indexOf("</ows:ExceptionReport>"));
            try {
                output = Templates.getTemplate(Templates.EXCEPTION_REPORT, Pair.of("\\{exception\\}", sub));
            } catch (Exception ex) {
                log.warn("Error handling exception report template");
            }
            log.debug("Done marshalling Error Report.");
        } catch (JAXBException e2) {
            log.error("Stack trace: {}", e2);
            log.error("Error stack trace: " + e2);
        }
        return output;
    }

    private String exceptionToXml(PetascopeException e) {
        return exceptionReportToXml(e.getReport());
    }

    /**
     * Handle a WCS request.
     *
     * @param version WCS version
     * @param operation WCS operation
     * @param request the actual request
     * @param response stream to which the result will be written
     * @param srvRequest the servlet request
     * @throws WCSException
     * @throws PetascopeException
     */
    private void handleWcsRequest(String version, String operation, String request,
            HttpServletResponse response, HttpServletRequest srvRequest)
            throws WCSException, PetascopeException, SecoreException, SQLException {

        if (version == null) {
            throw new WCSException(ExceptionCode.InvalidRequest, "No WCS version specified.");
        }
        if (version.startsWith("2")) {
            handleWcs2Request(request, response, srvRequest);
        } else if (version.startsWith("1")) {
            handleWcs1Request(operation, request, response);
        } else {
            throw new WCSException(ExceptionCode.VersionNegotiationFailed);
        }
    }

    /**
     * Handle WCS 1.1 request.
     *
     * @param operation WCS operation
     * @param request request string
     * @param response
     * @throws WCSException in case of I/O error, or if the server is unable to
     * handle the request
     */
    private void handleWcs1Request(String operation, String request, HttpServletResponse response)
            throws WCSException, PetascopeException, SecoreException, SQLException {

        log.info("Handling WCS 1.1 request");

        // compute result
        String result = null;
        if (operation.endsWith(RequestHandler.GET_CAPABILITIES)) {
            result = wcs.GetCapabilities(request);
        } else if (operation.endsWith(RequestHandler.DESCRIBE_COVERAGE)) {
            result = wcs.DescribeCoverage(request);
        } else if (operation.endsWith(RequestHandler.GET_COVERAGE)) {
            String xmlRequest = wcs.GetCoverage(request, wcps);
            log.debug("Received GetCoverage Request: \n{}", xmlRequest);
            // redirect the request to WCPS
            handleProcessCoverages(xmlRequest, response);
            return; // the result has been written already so we return
        }

        // write result to output stream
        if (result != null) {
            PrintWriter out;
            try {
                out = response.getWriter();
                response.setContentType("text/xml; charset=utf-8");
                out.write(result);
                out.flush();
                out.close();
            } catch (IOException e) {
                throw new WCSException(ExceptionCode.IOConnectionError, e.getMessage(), e);
            }
        }
    }

    /**
     * Handle WCS 2.0 request.
     *
     * @param request request string
     * @param response
     * @throws WCSException in case of I/O error, or if the server is unable to
     * handle the request
     */
    private void handleWcs2Request(String request, HttpServletResponse response, HttpServletRequest srvRequest)
            throws WCSException, PetascopeException, SecoreException {
        try {
            log.info("Handling WCS 2.0 request");
            HTTPRequest petascopeRequest = this.parseUrl(srvRequest, request);
            ProtocolExtension pext = ExtensionsRegistry.getProtocolExtension(petascopeRequest);
            if (pext == null) {
                throw new WCSException(ExceptionCode.NoApplicableCode,
                        "No protocol binding extension that can handle this request was found ");
            }
            log.info("Protocol binding extension found: {}", pext.getExtensionIdentifier());
            Response res = pext.handle(petascopeRequest, meta);

            OutputStream os = response.getOutputStream();
            response.setStatus(res.getExitCode());

            // Convert from encoding format type to MIME type (e.g: tiff -> image/tiff)
            // In case of WCS query, "res" converted from formatType to mimeType (not as WCPS query)
            String mimeType = "";
            if(res.getFormatType().contains("/")) {
                mimeType = res.getFormatType();
            } else {
                mimeType = meta.formatToMimetype(res.getFormatType());
            }

            // WCS multipart
            if (res.isMultiPart() && !res.isProcessCoverage()) {
                MultipartResponse multi = new MultipartResponse(response);
                // WCS jp2000 multipart encoded GML into the coverage result
                if(res.getXml() != null) {
                    multi.startPart(FormatExtension.MIME_GML);
                    IOUtils.write(res.getXml()[0], os);
                    multi.endPart();
                }
                // Write byte array to output stream
                multi.startPart(mimeType);
                IOUtils.write(res.getData().get(0), os);
                multi.endPart();

                multi.finish();
            }
            // WCPS
            else if (res.isProcessCoverage()) {
                try {
                    if(res.isMultiPart()) {
                        MultipartResponse multi = new MultipartResponse(response);
                        for (byte[] data : res.getData()) {
                            multi.startPart(mimeType);
                            IOUtils.write(data, os);
                            multi.endPart();
                        }
                        multi.finish();
                    } else {
                        response.setContentType(mimeType);
                        IOUtils.write(res.getData().get(0), os);
                    }
                } finally {
                    IOUtils.closeQuietly(os);
                }
            } else {
                try {
                    // WCS
                    if (mimeType != null) {
                        response.setContentType(mimeType);
                    } else {
                        response.setContentType(FormatExtension.MIME_TEXT);
                    }
                    if (res.getXml() != null) {
                        IOUtils.write(res.getXml()[0], os);
                    } else if (res.getData() != null) {
                        IOUtils.write(res.getData().get(0), os);
                    }
                } finally {
                    IOUtils.closeQuietly(os);
                }
            }
        } catch (WCSException e) {
            throw e;
        } catch (SecoreException e) {
            throw e;
        } catch (PetascopeException e) {
            throw new WCSException(e.getExceptionCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Runtime error : {}", e.getMessage());
            throw new WCSException(ExceptionCode.RuntimeError,
                    "Runtime error while processing request", e);
        }
    }

    private void handleProcessCoverages(String xmlRequest, HttpServletResponse response)
            throws WCSException, PetascopeException, SecoreException, SQLException {

        OutputStream webOut = null;
        try {
            log.debug("Received a ProcessCoverages request: \n{}", xmlRequest);

            log.debug("-------------------------------------------------------");
            log.debug("Converting to rasql");
            wcps = new Wcps(new File(getServletContext().getRealPath(
                    WcpsConstants.MSG_WCPS_PROCESS_COVERAGE_XSD)), meta);
            ProcessCoveragesRequest processCoverageRequest
                    = wcps.pcPrepare(ConfigManager.RASDAMAN_URL, ConfigManager.RASDAMAN_DATABASE,
                            IOUtils.toInputStream(xmlRequest));
            log.debug("-------------------------------------------------------");

            String query = processCoverageRequest.getRasqlQuery();
            String mime = processCoverageRequest.getMime();

            response.setContentType(mime);
            webOut = response.getOutputStream();

            if (processCoverageRequest.isRasqlQuery()) {
                log.debug("executing request");
                log.debug("[" + mime + "] " + query);

                RasQueryResult res = new RasQueryResult(processCoverageRequest.execute());
                if (!res.getMdds().isEmpty() || !res.getScalars().isEmpty()) {
                    final PrintStream printStream = new PrintStream(webOut);
                    for (String s : res.getScalars()) {
                        printStream.print(s);
                    }
                    for (byte[] bs : res.getMdds()) {
                        webOut.write(bs);

                        if (ConfigManager.CCIP_HACK == true) {
                            try {
                                String dir = getServletContext().getRealPath("/");
                                File f = new File(dir + "image.jpeg");

                                log.info("HACK: Writing image to: " + f.getAbsolutePath());
                                OutputStream os = new DataOutputStream(new FileOutputStream(f, false));
                                os.write(bs);
                                os.close();
                                log.info("HACK: Wrote image successfully !");
                            } catch (Exception e) {
                                log.warn("Error while evaluating CCIP hack: '{}'", e.getMessage());
                            }
                        }
                    }
                } else {
                    log.warn("WCPS: Warning! No result returned from rasql query.");
                }
                // Execute the query ... (?)
            } else if (processCoverageRequest.isPostGISQuery()) {
                PostgisQueryResult res = new PostgisQueryResult(processCoverageRequest.execute());
                webOut.write(res.toCSV(res.getValues()).getBytes());

            } else {
                log.debug("metadata result, no rasql to execute");
                webOut.write(query.getBytes());
            }

            log.debug("WCPS: done");
        } catch (WCPSException e) {
            throw new WCSException(e.getExceptionCode(), e.getMessage(), e);
        } catch (SAXException e) {
            throw new WCSException(ExceptionCode.XmlNotValid, e.getMessage(), e);
        } catch (IOException e) {
            throw new WCSException(ExceptionCode.IOConnectionError, e.getMessage(), e);
        } finally {
            if (webOut != null) {
                try {
                    webOut.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Parses the HTTP servlet request into a Petascope Servlet Request
     *
     * @param srvRequest the http servlet request
     * @param request the request string needed by parsers
     * @return complete ServletRequest
     */
    // TODO: duplicate code in Wcs2Servlet class: to be addressed in ticket #307.
    private HTTPRequest parseUrl(HttpServletRequest srvRequest, String request) {
        String contextPath = "", pathInfo = "";
        //get rid of the prefix slashes
        if (srvRequest.getContextPath().length() > 1) {
            contextPath = srvRequest.getContextPath().substring(1);
        }
        // [If no extra path info are specified in the request, .getPathInfo() returns `null`]
        if (srvRequest.getPathInfo() != null && srvRequest.getPathInfo().length() > 1) {
            pathInfo = srvRequest.getPathInfo().substring(1);
        }
        HTTPRequest srvReq = new HTTPRequest(contextPath,
                pathInfo, srvRequest.getQueryString(), request);
        return srvReq;
    }

    private final PetascopeInterfaceAdapter wms13Adapter = new PetascopeInterfaceAdapter();
}