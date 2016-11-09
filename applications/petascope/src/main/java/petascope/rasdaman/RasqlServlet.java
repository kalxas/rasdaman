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
package petascope.rasdaman;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.CORSHttpServlet;
import petascope.ConfigManager;
import petascope.CustomRequestWrapper;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.rasdaman.RasdamanException;
import petascope.util.KVPSymbols;
import petascope.util.RequestUtil;
import petascope.util.StringUtil;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;

/**
 * A servlet handling rasql requests, available at http://domain:port/rasdaman/rasql
 * Accepts KVP requests, with the self-explanatory keys: username, password, query.
 * e.g: http://localhost:8080/rasdaman/rasql?username=rasadmin&password=rasadmin&query=select dbinfo(c) from mr as c
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class RasqlServlet extends CORSHttpServlet {

    private static final Logger log = LoggerFactory.getLogger(RasqlServlet.class);

    private enum request {
        INVALID,
        VALID
    }

    // indicates that the default HTTP response status should be used
    private static final int USE_DEFAULT_STATUS = -1;

    @Override
    public void init() throws ServletException {
        String confDir = this.getServletContext().getInitParameter(ConfigManager.CONF_DIR);
        try {
            ConfigManager.getInstance(confDir);
        } catch (RasdamanException e) {
            throw new ServletException(e);
        }
    }

    private void setServletURL(HttpServletRequest req) {
        if("".equals(ConfigManager.PETASCOPE_SERVLET_URL) )
            ConfigManager.PETASCOPE_SERVLET_URL = req.getRequestURL().toString();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        super.doGet(req, res);
        CustomRequestWrapper wrapperRequest  = new CustomRequestWrapper(req);
        setServletURL(wrapperRequest);
        OutputStream outStream = null;
        try {
            outStream = res.getOutputStream();
        } catch (IOException ex) {
            log.error("Cannot initialise output stream.");
            return;
        }

        Map<String, String> kvp = RequestUtil.parseKVPRequestParams(wrapperRequest.getQueryString());
        String username = kvp.get(KVPSymbols.KEY_USERNAME);
        String password = kvp.get(KVPSymbols.KEY_PASSWORD);
        String query = kvp.get(KVPSymbols.KEY_QUERY);

        // validate user before running the query
        request validRet = validateRequest(outStream, res, username, password, query);
        if (validRet == request.INVALID) {
            // validation is error, return
            return;
        }

        // check if user want to upload file to server by find decode() or inv_*() in the requested query
        String filePath = null;
        if (RasUtil.isDecodeQuery(query)) {
            try {
                // write the uploaded file to upload directory
                filePath = writeFileToUploadPath(wrapperRequest);
            } catch (Exception ex) {
                // NOTE: if user don't have permission to write (like rasguest), the error from Rasql will be catch here
                printError(outStream, res, "Internal error: failed saving uploaded file on the server.", ex, 400);
                return;
            }
        }

        // then run the rasql query to Rasdaman
        try {
            // select, delete, update without decode()
            executeQuery(outStream, res, username, password, query, filePath);
        } catch (PetascopeException ex) {
            printError(outStream, res, "Failed evaluating query: " + query, ex, USE_DEFAULT_STATUS);
            log.error("Failed evaluating query: " + query, ex);
        } catch (Exception ex) {
            printError(outStream, res, "Failed evaluating query: " + query, ex, USE_DEFAULT_STATUS);
            log.error("Failed evaluating query: " + query, ex);
        }
        finally {
            try {
                outStream.close();
            } catch (IOException ex) {
                log.error("Internal error: failed closing output stream.", ex);
            }
        }
    }

    /**
     * Write the uploaded file to configured upload path
     * @param req
     * @return String
     * @throws IOException
     * @throws Exception
     */
    private String writeFileToUploadPath(HttpServletRequest req) throws IOException, Exception {
        // username rasadmin get this file and upload to configured directory path in petascope.properties of server
        File uploadPath = new File(ConfigManager.RASQL_SERVLET_UPLOAD_DIR);
        if (!uploadPath.exists()) {
            log.debug("Creating rasql servlet upload directory: " + ConfigManager.RASQL_SERVLET_UPLOAD_DIR);
            uploadPath.mkdir();
            log.debug("done.");
        }

        String fileName = StringUtil.createRandomString("file");
        String filePath = ConfigManager.RASQL_SERVLET_UPLOAD_DIR + "/" + fileName;
        File file = new File(filePath);

        DiskFileItemFactory factory = new DiskFileItemFactory();
        // Location to save uploaded data
        factory.setRepository(new File(ConfigManager.RASQL_SERVLET_UPLOAD_DIR));
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request to get file items.
        List fileItems = upload.parseRequest(req);
        // Process the uploaded file items
        Iterator i = fileItems.iterator();
        log.debug("Writing the uploaded file to rasql server upload directory: " + filePath);

        while (i.hasNext()) {
            FileItem fi = (FileItem) i.next();
            if (!fi.isFormField()) {
                fi.write(file);
            }
        }
        log.debug("done.");

        return filePath;
    }

    /**
     * Execute rasql query.
     *
     * @param username rasdaman username
     * @param password rasdaman password
     * @param query rasql query to execute
     * @throws PetascopeException in case of error in query evaluation
     */
    private void executeQuery(OutputStream os, HttpServletResponse res, String username,
            String password, String query, String filePath) throws PetascopeException {
        try {
            Object tmpResult = null;

            if (filePath == null) {
                // no decode() or inv_*() in rasql query
                if (RasUtil.isSelectQuery(query)) {
                    // no need to open transcaction with "select" query
                    tmpResult = RasUtil.executeRasqlQuery(query, username, password, false);
                } else {
                    // drop, delete need to open transacation
                    tmpResult = RasUtil.executeRasqlQuery(query, username, password, true);
                }
            } else {
                // decode() or inv_*() in rasql query
                RasUtil.executeInsertUpdateFileStatement(query, filePath, username, password);
            }
            RasQueryResult queryResult = new RasQueryResult(tmpResult);

            if (!queryResult.getScalars().isEmpty()) {
                res.setContentType("text/plain");
                final PrintStream printStream = new PrintStream(os);
                for (String s : queryResult.getScalars()) {
                    printStream.print(s);
                }
            } else if (!queryResult.getMdds().isEmpty()) {
                res.setContentType("application/octet-stream");
                for (byte[] bs : queryResult.getMdds()) {
                    os.write(bs);
                }
            }
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.IOConnectionError,
                    "Failed writing result to output stream", ex);
        } finally {
            // remove the uploaded filePath after insert/update to collection
            if (filePath != null) {
                File file = new File(filePath);
                if (file.delete()) {
                    log.debug("Removed the uploaded file: " + filePath);
                } else {
                    log.error("Failed removing uploaded file: " + filePath);
                }
            }
        }

        log.debug("Rasql query finished successfully.");
    }

    /**
     * Check if a request is valid (i.e: has username, password and query parameters)
     * @param username
     * @param password
     * @param query
     */
    private request validateRequest(OutputStream os, HttpServletResponse res, String username, String password, String query) {
        if (username == null) {
            printError(os, res, "required KVP parameter " +
                    KVPSymbols.KEY_USERNAME + " missing from request.", null, 400);
            return request.INVALID;
        }
        if (password == null) {
            printError(os, res, "required KVP parameter " +
                    KVPSymbols.KEY_PASSWORD + " missing from request.", null, 400);
            return request.INVALID;
        }
        if (query == null) {
            printError(os, res, "required KVP parameter " +
                    KVPSymbols.KEY_QUERY + " missing from request.", null, 400);
            return request.INVALID;
        }

        // NOTE: no validate user permission based on query here, leave it to rasql
        return request.VALID;
    }

    /**
     * Print error from exception when receiving and processing rasql query
     * @param outStream
     * @param response
     * @param message
     * @param e
     * @param status
     */
    private void printError(OutputStream outStream, HttpServletResponse response, String message, Exception e, int status) {
        PrintWriter out = new PrintWriter(outStream);
        response.setContentType("text/html; charset=utf-8");
        if (status != USE_DEFAULT_STATUS) {
            response.setStatus(status);
        }

        out.println("<html><head><title>PetaScope Rasql Servlet</title></head><body>");
        out.println("<h1>An error has occured, please check log for more detail.</h1>");
        out.println("<p>" + message + "</p>");
        log.error(message);
        if (e != null) {
            out.println(e.getMessage());
            log.error(e.getMessage());
        }
        out.close();
        log.trace("done with error");
    }
}
