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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.rasdaman.config.ConfigManager;
import static org.rasdaman.config.ConfigManager.PETASCOPE_CONNETION_TIMEOUT;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class HttpUtil {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(HttpUtil.class);
    
    /**
     * Check if an URL returns some valid output (not server error code)
     */
    public static boolean urlExists(String endpoint) {
        try {
            URL url = new URL(endpoint);
        
            // We want to check the current URL
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        
            // We don't need to get data
            httpURLConnection.setRequestMethod("HEAD");
            httpURLConnection.setConnectTimeout(3000);

            return httpURLConnection.getResponseCode() < 500;
        } catch (Exception ex) {
            log.warn("URL '" + endpoint + "' is not available. Reason: " + ex.getMessage());
            return false;
        }

    }

    /**
     * Get String content from a URL endpoint
     */
    public static String getObjectFromEndpoint(String requestURL) throws PetascopeException {
        
        String result = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(PETASCOPE_CONNETION_TIMEOUT).build();
            HttpGet httpGet = new HttpGet(requestURL);         
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            result = IOUtils.toString(httpResponse.getEntity().getContent());
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.IOConnectionError, "Cannot get result from URL '" + requestURL + "'. Reason: " + ex.getMessage(), ex);
        }

        return result;
    }
    
    /**
     * Get input stream from an external URL
     */
    public static InputStream getInputStream(String inputUrl) throws IOException {
        URL url = new URL(inputUrl);
        HttpURLConnection con = (HttpURLConnection)(url.openConnection());
        con.setConnectTimeout(ConfigManager.CRSRESOLVER_CONN_TIMEOUT);
        con.setReadTimeout(ConfigManager.CRSRESOLVER_READ_TIMEOUT);

        // NOTE: allow to follow requesting from http -> https 
        // e.g. http://ows.rasdaman.org/def//uom/EPSG/0/9122 -> https://ows.rasdaman.org/def//uom/EPSG/0/9122
        int status = con.getResponseCode();
        if (status == HttpURLConnection.HTTP_MOVED_TEMP
            || status == HttpURLConnection.HTTP_MOVED_PERM) {
            String location = con.getHeaderField("Location");
            URL newUrl = new URL(location);
            con = (HttpURLConnection) newUrl.openConnection();
         }

        InputStream result = con.getInputStream();
        return result;        
    }
    
    /**
     * Return the last segment of an URL, e.g: http://localhost:8080/rasdaman/ows -> ows
     */
    public static String getLastSegmentOfURL(String inputURL) {
        String result = FilenameUtils.getName(inputURL);
        return result;
    }
    

}
