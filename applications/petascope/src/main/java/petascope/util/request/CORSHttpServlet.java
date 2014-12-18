package petascope.util.request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Base servlet for services that wish to allow browser clients to request resources from different origin domains.
 * Servlets that wish to implement the CORS protocol should extend this class and call the super methods for
 * the doGet() and doOptions()
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class CORSHttpServlet extends HttpServlet {

    /**
     * Implement the CORS requirements to allow browser clients to request
     * resources from different origin domains. i.e. http://example.org can make
     * requests to http://example.com
     *
     * @param req  the http request
     * @param resp the http response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", CORS_ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    /**
     * Implement the CORS requirements to allow browser clients to request
     * resources from different origin domains. i.e. http://example.org can make
     * requests to http://example.com
     *
     * @param req  the http request
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

    private static final String CORS_ACCESS_CONTROL_ALLOW_ORIGIN = "*";
    private static final String CORS_ACCESS_CONTROL_ALLOW_METHODS = "POST, GET, OPTIONS";
    private static final String CORS_ACCESS_CONTROL_ALLOW_HEADERS = "Content-Type";
    private static final String CORS_ACCESS_CONTROL_MAX_AGE = "1728000";
}
