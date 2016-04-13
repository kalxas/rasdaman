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

/**
 * ServletRequest is a class containing information collected from the
 * HTTPServletRequest that needs to be passed to the request parsers
 *
 * @author <a href="alex@flanche.net">Alex Dumitru</a>
 */
public class HTTPRequest {    
    /**
     * Constructor for the class In
     * http://example.org/petascope/wcs/2.0/?foo=bar the components are: 
     * - contextPath: petascope 
     * - urlPath : wcs/2.0 
     * - queryString : foo=bar
     *
     * @param contextPath
     * @param urlPath
     * @param queryString
     * @param requestString
     */
    public HTTPRequest(String contextPath, String urlPath, String queryString, String requestString) {
        this.urlPath = urlPath;
        this.queryString = queryString;
        this.requestString = requestString;
        this.contextPath = contextPath;
    }

    /**
     * Returns the url path starting after the context path e.g. if petascope is
     * deployed at http://example.org/foo and the request is at
     * http://example.org/foo/wcs/2.0/ the string returned is wcs/2.0 Please
     * note that trailing and prefix slashes are not included
     *
     * @return the url path
     */
    public String getUrlPath() {
        return urlPath;
    }

    /**
     * Returns the query string, i.e the part after the "?" character The "?" is
     * not prepended
     *
     * @return the query string
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Returns the full path comprised of the context path and the url path. No
     * trailing or prefix slashes are returned Example: In
     * http://example.org/petascope/wcs/2.0 contextPath is petascope and url
     * path is wcs/2.0
     *
     * @return the full path
     */
    public String getFullUrlPath() {
        return this.contextPath + "/" + this.urlPath;
    }

    /**
     * Returns the url accessed including the context path, url path and the
     * query string. Example For http://example.org/petascope/wcs/2.0?foo=bar
     * the same url (http://example.org/petascope/wcs/2.0?foo=bar) is returned.
     *
     * @return the full url
     */
    public String getFullUrl() {
        return this.contextPath + "/" + this.urlPath + "?" + this.queryString;

    }

    /**
     * Method provided for backwards compatibility with parsers that expect the
     * whole request to be provided as a string, i.e KVP expects the query
     * string, XML expects an XML compatible string etc In the future these 
     * parsers should extract the needed information from the HTTPRequest
     *
     * @return the request string as expected by parsers
     */
    public String getRequestString() {
        return requestString;
    }

    public void setRequestString(String requestString) {
        this.requestString = requestString;
    }
    

    @Override
    public String toString() {
        return "HTTPRequest{" + "urlPath=" + urlPath + ", queryString=" + queryString + ", requestString=" + requestString + ", contextPath=" + contextPath + '}';
    }
    private String urlPath;
    private String queryString;
    private String requestString;
    private String contextPath;
}
