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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import net.opengis.ows.v_1_0_0.ExceptionReport;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
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
import petascope.util.Pair;
import petascope.util.PostgisQueryResult;
import petascope.util.StringUtil;
import petascope.util.WcpsConstants;
import petascope.util.XMLSymbols;
import petascope.util.XMLUtil;
import petascope.util.ras.RasQueryResult;
import petascope.util.response.MultipartResponse;
import petascope.wcps.server.core.ProcessCoveragesRequest;
import petascope.wcps.server.core.Wcps;
import petascope.wcs.server.WcsServer;
import petascope.wcs2.extensions.DecodeFormatExtension;
import petascope.wcs2.extensions.ExtensionsRegistry;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.ProtocolExtension;
import petascope.wcs2.handlers.RequestHandler;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.XMLParser;
import petascope.wcs2.templates.Templates;
import petascope.wms2.servlet.PetascopeInterfaceAdapter;

/**
 * This servlet is a unified entry-point for all the PetaScope services.
 * Extends CORSHttpServlet for the CORS requirements to allow browser clients to request
 * resources from different origin domains. i.e. http://example.org can make
 * requests to http://example.com.
 *
 * @author Andrei Aiordachioaie
 * @author Dimitar Misev
 */
public class PetascopeInterface extends CORSHttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PetascopeInterface.class);
    private DbMetadataSource meta;
    /* Instance of WCPS service */
    private Wcps wcps;
    /* Instance of WcsServer service */
    private WcsServer wcs;

    private static final String WMS_SERVICE = "WMS";
    private static final String WCS_SERVICE = "WCS";
    private static final String REQUEST_GET_PARAMETER = "request";

    // By default, this servlet will recive GET request
    private boolean isGet = true;

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

        // Initialize WSC schema (NOTE: it will take 1 - 2 minutes, so only run when xml_validation=true)
        if (ConfigManager.XML_VALIDATION) {
            try {
                XMLParser.parseWcsSchema();
            } catch (WCSException ex) {
                log.debug("Cannot load XML schema from opengis.", ex);
                throw new ServletException("Cannot load XML schema from opengis.", ex);
            }
        }

        // Initialize the logging system
        log.info("Petascope {} starting", ConfigManager.RASDAMAN_VERSION);

        // External libraries licensing issues
        log.info("To obtain a list of external packages used, please visit www.rasdaman.org .");

        // Force GeoTools referencing libraries to X->Y ordered CRSs
        System.setProperty("org.geotools.referencing.forceXY", "true");

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
            log.info("WCPS 1.0 (deprecated): Initializing...");
            wcps = new Wcps(meta);
            log.info("WCPS: initialization complete.");
        } catch (Exception e) {
            log.error("Stack trace: {}", e);
            throw new ServletException("WCPS initialization error", e);
        }
        
        /**
         * Initialize the WMS 1.1.0 service
         * NOTE: WMS 1.1.0 is deprecated and it cannot initialize database
         * with the error as in http://rasdaman.org/ticket/898. Use WMS 1.3.0 instead.
         */
        /*try {
            log.info("WMS11: Initializing the WMS 1.1.0 service...");
            wms13Adapter.initWMS11Servlet(this.getServletConfig());
            log.info("WMS11: Initialization complete.");
        } catch (Exception e) {
            log.error("WMS 1.1.0 could not be initialized");
        }*/
        

        /**
         * Initialize the WMS 1.3.0 service
         * NOTE: WMS 1.1.0 is deprecated and obsolete.
         */
        try {
            log.info("WMS13: Initializing the WMS 1.3.0 service...");
            wms13Adapter.initWMS13Servlet(this.getServletConfig());
            log.info("WMS13: Initialization complete.");
        } catch (Exception e) {
            log.error("WMS 1.3.0 could not be initialized due to {}", e);
            throw new ServletException("WMS 1.3.0 could not be initialized due to {}", e);
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
    public void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, UnsupportedEncodingException, IOException {
        CustomRequestWrapper wrapperRequest  = new CustomRequestWrapper(httpRequest);
        // POST request KVP
        log.debug("Received POST request.");
        wrapperRequest.buildPostKvpParametersMap();
        this.handleRequest(wrapperRequest, httpResponse);
    }

    /* Handle Get requests. This function delegates the request to the service
     specified in the request by the "service" parameter. */
    @Override
    public void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, UnsupportedEncodingException, IOException {
        CustomRequestWrapper wrapperRequest  = new CustomRequestWrapper(httpRequest);
        // NOTE: To support the "+" which is missing from URI as it is converted to space, we need to manipulate the queryString and parse it to parameter maps correctly
        // GET request KVP
        log.debug("Received GET request.");
        wrapperRequest.buildGetKvpParametersMap();
        this.handleRequest(wrapperRequest, httpResponse);
    }

    /**
     * After parsing the parameters for KVP (if possible) in GET/POST, we handle the request services here.
     * @param wrapperRequest
     * @param httpResponse
     */
    private void handleRequest(CustomRequestWrapper wrapperRequest, HttpServletResponse httpResponse) {
        try {
            super.doGet(wrapperRequest, httpResponse);
            setServletURL(wrapperRequest);

            // reload the GetCapabilities when update service information from admin page
            reloadServiceInformation(wrapperRequest);                
            meta.ensureConnection();

            // parsing request parameters if request is KVP (e.g: ows?service=WCS&version=2.0.1&request=GetCapabilities)
            Map<String, String> paramMap = buildParameterMap(wrapperRequest);
            log.trace("Request parameters : {}", paramMap);

            // Assume this is KVP request, check to get the 'service' parameter.
            String service = paramMap.get(KVPSymbols.KEY_SERVICE);
            // Backward support for WCPS (this is not valid extension: http://localhost:8080/rasdaman/ows?query=)
            // the valid one is: http://localhost:8080/rasdaman/ows?service=WCS&version=2.0.1&request=ProcessCoverages&query=....
            String query = paramMap.get(KVPSymbols.KEY_QUERY);

            // *Handle KVP request*
            if (service != null || query != null) {
                log.debug("KVP request received: " + wrapperRequest.getQueryString());
                this.handleKVPRequest(wrapperRequest, httpResponse);
            } else {
                // parsing request and call the service handler accordingly (queryString is built for POST in wrapper class).
                String requestBody = wrapperRequest.getQueryString();
                Map<String, String> params = buildParameterDictionary(requestBody);

                // *Handle SOAP and XML request*
                String request = StringUtil.urldecode(requestBody, wrapperRequest.getContentType());
                // Get root element of the request in XML
                String root = XMLUtil.getRootElementName(request);
                if (root != null) {
                    // SOAP request
                    if (root.equals(XMLSymbols.LABEL_ENVELOPE)) {
                        log.debug("SOAP request received: " + requestBody);
                        handleSOAPRequest(request, wrapperRequest, httpResponse);
                    } else if (!params.isEmpty()) {
                       // XML request
                        log.debug("XML request received: " + requestBody);
                        handleXMLRequest(request, wrapperRequest, httpResponse);
                    }
                } else {
                    // Neither KVP nor SOAP/XML and without any requested parameters, so it *must* return WCS client.
                    // e.g: localhost:8080/rasdaman/ows
                    showWcsClient(httpResponse);
                }
            }
        } catch (SQLException ex) {
            log.error("Cannot connect to petascopedb: " + ex.getMessage());
            printError(httpResponse, "Cannot connect to petascopedb: " + ex.getMessage(), ex);
        } catch (PetascopeException ex) {
            log.error("Petascope has internal error while processing request: " + ex.getMessage());
            printError(httpResponse, "Petascope has internal error while processing request: " + ex.getMessage(), ex);
        } catch (SecoreException ex) {
            log.error("Secore has internal error while processing request: " + ex.getMessage());
            printError(httpResponse, "Secore has internal error while processing request: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Runtime error while processing request: " + ex.getMessage());
            printError(httpResponse, "Runtime error while processing request: " + ex.getMessage(), ex);
        }
    }

    /**
     * Depend on service parameter in KVP request to call WMS/WCS KVP handler
     * @param wrapperRequest
     * @param httpResponse
     * @throws ServletException
     * @throws IOException
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     * @throws SQLException
     */
    private void handleKVPRequest(CustomRequestWrapper wrapperRequest, HttpServletResponse httpResponse) throws ServletException, IOException,
                                  PetascopeException, WCSException, SecoreException, SQLException {
        Map<String, String> paramMap = buildParameterMap(wrapperRequest);
        String service = paramMap.get(KVPSymbols.KEY_SERVICE);
        String version = paramMap.get(KVPSymbols.KEY_VERSION);
        // this is for backward support WCPS (localhost:8080/rasdaman/ows?query=...)
        String query = paramMap.get(KVPSymbols.KEY_QUERY);

        if (service != null) {
            if (service.equals(WMS_SERVICE)) {
                // Only support WMS 1.3
                wms13Adapter.handleGetRequests(wrapperRequest, httpResponse);
            } else if (service.equals(WCS_SERVICE)) {
                // Support WCS 2.0.1
                String queryString = wrapperRequest.getQueryString();
                handleWcsRequest(version, paramMap.get(REQUEST_GET_PARAMETER), queryString,
                                httpResponse, wrapperRequest);
            }
        } else if (query != null) {
            // WCPS backward support, e.g: localhost:8080/def/rasdaman/ows?query=for c in ( test_rgb ) return encode( c[ i(0:100), j(0:100) ], "png", "nodata=0")
            // check if KVP request has only "query" parameter
            String processingRequest = KVPSymbols.KEY_SERVICE + "=" + WCS_SERVICE  + "&"
                                     + KVPSymbols.KEY_VERSION + "=" + ConfigManager.WCS_DEFAULT_VERSION  + "&"
                                     + KVPSymbols.KEY_REQUEST + "=" + KVPSymbols.KEY_PROCESS_COVERAGES + "&"
                                     + query;

            // Using WCS 2.0.1 to handle this backward support WCPS request
            handleWcsRequest(ConfigManager.WCS_DEFAULT_VERSION, KVPSymbols.KEY_PROCESS_COVERAGES,
                            processingRequest, httpResponse, wrapperRequest);
        }
    }

    /**
     * Depend on service parameter in SOAP request to call WCS/WCPS SOAP handler
     * @param request
     * @param wrapperRequest
     * @param httpResponse
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     * @throws Exception
     */
    private void handleSOAPRequest(String request, CustomRequestWrapper wrapperRequest, HttpServletResponse httpResponse) throws PetascopeException, WCSException,
                                   SecoreException, Exception {
        // Here we have 2 kind of requests in SOAP body-message
        // (1 is WCS, e.g: <wcs:GetCoverage>, 2 is WCPS, e.g: <ProcessCoveragesRequest>)
        // Need to get the XML content inside the <env:Body>...</env:Body>
        String bodyContent = XMLUtil.extractWcsRequest(request);
        if (bodyContent.contains(XMLSymbols.LABEL_PROCESSCOVERAGE_REQUEST)) {
            // WCPS 1.0 XML in SOAP, need to extract the process query in body tag
            handleProcessCoverages(bodyContent, httpResponse);
        } else {
            // WCS XML in SOAP, it will be extract the body tag later in SOAP handler
            handleWcs2Request(request, httpResponse, wrapperRequest);
        }
    }

    /**
     * Depend on service parameter in XML request to call WCS/WCPS XML handler
     * @param request
     * @param wrapperRequest
     * @param httpResponse
     * @throws PetascopeException
     * @throws WCSException
     * @throws SecoreException
     * @throws SQLException
     * @throws IOException
     * @throws ParsingException
     */
    private void handleXMLRequest(String request, CustomRequestWrapper wrapperRequest, HttpServletResponse httpResponse) throws PetascopeException, WCSException,
                                SecoreException, SQLException, IOException, ParsingException {
        // Get root element of the request in XML
        String root = XMLUtil.getRootElementName(request);
        if (root.endsWith(XMLSymbols.LABEL_PROCESSCOVERAGE_REQUEST)) {
            // Assume this is valid WCPS 1.0 in XML
            handleProcessCoverages(request, httpResponse);
        } else {
            // Assume this is valid WCS in XML
            Document doc = XMLUtil.buildDocument(null, request);
            
            Element rootEl = doc.getRootElement();
            
            String version = null;
            if (root.equals(KVPSymbols.KEY_GET_CAPABILITIES)) {
                // NOTE: WCS 2.0.1 GetCapabilities, version is gotten from "  <ows:AcceptVersions>   <ows:Version>2.0.1</ows:Version>   </ows:AcceptVersions>"               
                Element versionEl = XMLUtil.firstChildRecursivePattern(rootEl, XMLSymbols.LABEL_VERSION);
                if (versionEl == null) {
                    throw new WCSException("Missing Version element in " + KVPSymbols.KEY_GET_CAPABILITIES + " XML POST request.");
                }
                version = versionEl.getValue();
            } else {
                // meanwhile, DescribeCoverage or GetCoverage, version is gotten from attribute (e.g: <wcs:GetCoverage .... version="2.0.1">...</wcs:GetCoverage>)
                version = doc.getRootElement().getAttributeValue(KVPSymbols.KEY_VERSION);
            }
            
            handleWcsRequest(version, root, request, httpResponse, wrapperRequest);
        }
    }


    /**
     * A request without any parameters will get WCS client interface.
     * @param httpResponse
     * @throws IOException
     */
    private void showWcsClient(HttpServletResponse httpResponse) throws IOException, WCSException {
        PrintWriter out = httpResponse.getWriter();
        httpResponse.setContentType("text/html");
        // Read servlet HTML usage message from disk
        // path to the default HTML response of the interface servlet
        String usageFilePath = "/templates/interface-servlet.html";
        String usageMessage = null;
        try {
            usageFilePath = getServletContext().getRealPath(usageFilePath);
            usageMessage = FileUtils.readFileToString(new File(usageFilePath));
        } catch (IOException e) {
            log.error("Could not read WCS Client interface for response. Stack trace: {}", e);
            throw new WCSException(ExceptionCode.InternalComponentError, "Could not read WCS Client interface for response", e);
        }
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
            log.trace("setting response MIME type to text/html; charset=utf-8");
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
            // All of special characters for regex need to be quoted first, otherwise, getTemplate will have error.
            sub = Matcher.quoteReplacement(sub);
            output = Templates.getTemplate(Templates.EXCEPTION_REPORT, Pair.of("\\{exception\\}", sub));
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
     * @param operation WCS operation (WCS 1.0 deprecated)
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
     * @deprecated
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

            String fileName = res.getCoverageID();
            String fileType = "";

            String mimeType = res.getFormatType();

            // NOTE: WCS is translated to WCPS, hence they have both same mimeType formally, but WCST_Import can be empty
            if (mimeType.contains("/")) {
                // e.g: (image/png -> only .png)
                // if MIME type have a special file extension (e.g: application/netcdf -> .nc), then it should not get from MIME type
                if (DecodeFormatExtension.getFileExtension(mimeType) != null) {
                    fileType = DecodeFormatExtension.getFileExtension(mimeType);
                } else {
                    fileType = mimeType.split("/")[1];
                }
                fileName = fileName + "." + fileType;
            }

            // then download the result as a file (but only when the request has a specific coverage name)
            if (res.getCoverageID() != null) {
                // NOTE: Content-Disposition: attachment; will download file in WebBrowser instead of trying to display (GML/PNG/JPEG) type.
                response.setHeader("Content-disposition", "inline; filename=" + fileName);
                response.setHeader("File-name", fileName);                
            }

            // WCS multipart
            if (res.isMultiPart() && !res.isProcessCoverage()) {
                MultipartResponse multi = new MultipartResponse(response);
                // WCS jp2000 multipart encoded GML into the coverage result
                if (res.getXml() != null) {
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
                    if (res.isMultiPart()) {
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
                        String outputXML = res.getXml()[0];
                        /*  NOTE: output XML must be well formated (although due to it don't have indentation due to WCS OGC (test name: wcs2:get-kvp-core-req42)
                            deep-equal() will fail when the identation of DescribeCoverage and GetCoverage is different for 3 elements: boundedBy, domainSet, rangeType)
                            reason: DescribeCoverage have 2 levels parents
                            <CoverageDescriptions/>
                                <CoverageDescription>
                                    <boundedBy/>
                            meanwhile GetCoverage only have 1
                            <RectifiedGridCoverage/>
                                <boundedBy/>
                            so the indentations are different.
                        */

                        if (ConfigManager.OGC_CITE_OUTPUT_OPTIMIZATION) {
                            // Only when ogc_cite_output_optimization=true in petascope.properties
                            IOUtils.write(XMLUtil.transformXML(XMLUtil.trimSpaceBetweenElements(outputXML)), os);
                        } else {
                            IOUtils.write(outputXML, os);
                        }

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

    /**
     * @deprecated
     * This method is used to handle WCPS 1.0 in SOAP format or as an extension parameter from WCS
     * e.g: SERVICE=WCS&VERSION=2.0.1&REQUEST=ProcessCoverages&query=for c in (test_mr) return avg(c)
     * @param xmlRequest
     * @param response
     * @throws WCSException
     * @throws PetascopeException
     * @throws SecoreException
     * @throws SQLException
     */
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

    /**
     * When updating service information (e.g: service provider) from admin page, then reload these information from database
     * @throws ServletException
     */
    private void reloadServiceInformation(CustomRequestWrapper wrapperRequest) throws ServletException {
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
    }
}
