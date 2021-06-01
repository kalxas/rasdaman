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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import static org.rasdaman.config.ConfigManager.PETASCOPE_CONNETION_TIMEOUT;

/**
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class HttpUtil {

    /*
     * Rasfed sends HTTP requests to a local/remote petascope endpoint to collect 
     * required metadata from coverages/layers which returned in String JSON format.
     */
    public static String getObjectFromEndpoint(String requestURL) throws Exception {
        String result = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(PETASCOPE_CONNETION_TIMEOUT).build();
            HttpGet httpGet = new HttpGet(requestURL);
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            result = IOUtils.toString(httpResponse.getEntity().getContent());
        } catch (Throwable ex) {
            throw new Exception("Cannot get result from URL: " + requestURL + ". Reason: " + ex.getMessage(), ex);
        }

        return result;
    }

}
