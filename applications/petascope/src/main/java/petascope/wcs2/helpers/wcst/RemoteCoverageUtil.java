package petascope.wcs2.helpers.wcst;
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

import org.apache.commons.io.FileUtils;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import petascope.exceptions.wcst.WCSTCoverageNotFound;

/**
 * utilities class for remote coverages.
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RemoteCoverageUtil {

    /**
     * Fetches a file that is either remote (http) or local (file) given it's
     * url.
     *
     * @param url
     * @return
     * @throws java.io.IOException
     */
    public static InputStreamReader fetchFile(URL url) throws IOException {
        //if the protoocl is http, make a request
        if (url.getProtocol().equals(HTTP_URL_PROTOCOL)) {
            HttpURLConnection conn;
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            return new InputStreamReader(conn.getInputStream());
        }
        //otherwise assume file
        return new FileReader(url.getPath());
    }

    /**
     * Retrieves a coverage for which a source URL is given.
     *
     * @param url: the url where the coverage sits.
     * @return the contents of file at which the url points.
     * @throws petascope.exceptions.wcst.WCSTCoverageNotFound
     */
    public static String getRemoteGMLCoverage(URL url) throws WCSTCoverageNotFound {
        BufferedReader rd;
        String line;
        String result = "";
        try {
            rd = new BufferedReader(RemoteCoverageUtil.fetchFile(url));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (IOException e) {
            throw new WCSTCoverageNotFound();
        }
        return result;
    }

    public static File copyFileLocally(String fileUrl) throws IOException {
        String filePath = TEMP_FILE_PATH_PREFIX + java.util.UUID.randomUUID().toString();
        File tmpFile = new File(filePath);
        FileUtils.copyURLToFile(new URL(fileUrl), tmpFile);
        return tmpFile;
    }

    //indicates where to save a file passed as xlink inside the gml coverage
    private static final String TEMP_FILE_PATH_PREFIX = "/tmp/wcst-";
    private static final String HTTP_URL_PROTOCOL = "http";
}
