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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.RasdamanException;
import petascope.util.KVPSymbols;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;

/**
 * A servlet handling rasql requests, available at /petascope/rasql
 * 
 * Accepts KVP requests, with the self-explanatory keys: username, password, query.
 * 
 * Future work: manage updates and uploading data to the server.
 *
 * @author Dimitar Misev
 */
public class RasqlServlet extends HttpServlet {

    private static Logger log = LoggerFactory.getLogger(RasqlServlet.class);
    
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
        ConfigManager.PETASCOPE_SERVLET_URL = req.getRequestURL().toString();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        // TODO
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        setServletURL(req);

        Map<String, List<String>> kvp = StringUtil.parseQuery(req.getQueryString());

        // todo: check keys
        String username = ListUtil.head(kvp.get(KVPSymbols.KEY_USERNAME));
        String password = ListUtil.head(kvp.get(KVPSymbols.KEY_PASSWORD));
        String query = ListUtil.head(kvp.get(KVPSymbols.KEY_QUERY));
        
        if (username == null) {
            printError(res, "required KVP parameter " + 
                    KVPSymbols.KEY_USERNAME + " missing from request.", null, 400);
            return;
        }
        if (password == null) {
            printError(res, "required KVP parameter " + 
                    KVPSymbols.KEY_PASSWORD + " missing from request.", null, 400);
            return;
        }
        if (query == null) {
            printError(res, "required KVP parameter " + 
                    KVPSymbols.KEY_QUERY + " missing from request.", null, 400);
            return;
        }

        try {
            executeQuery(res, username, password, query);
        } catch (PetascopeException ex) {
            printError(res, "Failed evaluating query: " + query, ex, USE_DEFAULT_STATUS);
            log.error("Failed evaluating query: " + query, ex);
        }
    }
    
    /**
     * Execute rasql query.
     * 
     * @param username rasdaman username
     * @param password rasdaman password
     * @param query rasql query to execute
     * @throws PetascopeException in case of error in query evaluation
     */
    private void executeQuery(HttpServletResponse res, String username,
            String password, String query) throws PetascopeException {
        try {
            OutputStream os = res.getOutputStream();

            Object tmpResult = RasUtil.executeRasqlQuery(query, username, password);
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
        }
    }

    private void printError(HttpServletResponse response, String message, Exception e, int status) {
        PrintWriter out;
        try {
            out = response.getWriter();
        } catch (IOException ex) {
            log.error("could not print exception because of IO error.", ex);
            return;
        }

        response.setContentType("text/html; charset=utf-8");
        if (status != USE_DEFAULT_STATUS) {
            response.setStatus(status);
        }

        out.println("<html><head><title>PetaScope</title></head><body>");
        out.println("<h1>An error has occured</h1>");
        out.println("<p>" + message + "</p>");
        if (e != null) {
            out.println("<p>Stack trace:<br/><small>");
            e.printStackTrace(out);
            out.println("</small></p></body></html>");
        }
        out.close();
        log.trace("done with error");
    }
}
