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
package petascope.wcswsdl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WcsWsdlServlet serves to provide wcs-soap-binding.wsdl file at /ows/wcswsdl/wcs-soap-binding.wsdl
 * @author <a href="b.thapaliya@jacobs-university.de">Bidesh Thapaliya</a>
 */
public class WcsWsdlServlet extends HttpServlet {

    private static Logger log = LoggerFactory.getLogger(WcsWsdlServlet.class);
    private static String servletHtmlPath = "/templates/wcs-soap-binding.wsdl";
    private String defaultHtmlResponse;

    @Override
    public void init() throws ServletException {
        try {

            servletHtmlPath = getServletContext().getRealPath(servletHtmlPath);
            defaultHtmlResponse = FileUtils.readFileToString(new File(servletHtmlPath));

            log.info("WCSWSDL: initialization complete");
        } catch (Exception e) {
            log.error("WCSWSDL: initialization error", e);
            throw new ServletException("WCSWSDL initialization error", e);
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
        log.error("WCSWSDL : POST not suppported");
        throw new IOException();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public String getServletInfo() {
        return "WCS WSDL";
    }

    private void printError(HttpServletResponse response, String message, Exception e)
            throws IOException {
        log.error("WCSWSDL: error");
        log.error("WCSWSDL: setting response mimetype to text/html; charset=utf-8");
        response.setContentType("text/html; charset=utf-8");
        log.error("WCSWSDL: returning the following error message");
        e.printStackTrace(System.out);
        log.error("WCSWSDL: end of error message");
        PrintWriter out = new PrintWriter(response.getOutputStream());

        out.println(
                "<html><head><title>Wcs WSDL</title></head><body>");
        out.println("<h1>An error has occured</h1>");
        out.println("<p>" + message + "</p>");
        log.error("StackTrace:", e);
        out.println("</body></html>");
        out.close();
        log.error("WCSWSDL: done with error");
    }

    private void printUsage(HttpServletResponse response) throws IOException {
        log.info("WCSWSDL: setting response mimetype to text/xml; charset=utf-8");
        log.info("WCSWSDL: returning usage message");
        response.setContentType("text/xml; charset=utf-8");
        PrintWriter out = new PrintWriter(response.getOutputStream());

        out.println(defaultHtmlResponse);

        out.close();
        log.warn("WCSWSDL: done");
    }
}
