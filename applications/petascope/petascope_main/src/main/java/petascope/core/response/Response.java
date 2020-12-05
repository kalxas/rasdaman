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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.response;

import java.util.List;
import org.slf4j.LoggerFactory;
import petascope.util.MIMEUtil;

/**
 * Holds the response from executing a request operation.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class Response {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Response.class);
    
    private static final int DEFAULT_HTTP_RESPONSE_CODE = 200;
    public static final String DEFAULT_COVERAGE_ID = "ows";
    
    // Multiparts responses (e.g: a request which returns multipart on multi coverages)
    private List<byte[]> datas;
    // HTTP code returned to client
    private int httpCode = DEFAULT_HTTP_RESPONSE_CODE;
    // formatType of encoding in WCPS query, by default is gml
    private String formatType = MIMEUtil.MIME_GML;
    // if formatType is not text (gml, xml, text) then it needs a file name from
    // coverageID to set when download WCS/WCPS result  
    private String coverageID = DEFAULT_COVERAGE_ID;

    public Response() {
    }
    
    public Response(List<byte[]> datas, String formatType) {
        this.datas = datas;
        this.formatType = formatType;
    }

    public Response(List<byte[]> datas, String formatType, String coverageID) {
        this(datas, formatType);
        this.coverageID = coverageID;
    }
    
    public Response(List<byte[]> datas, String formatType, int httpCode) {
        this(datas, formatType);
        this.httpCode = httpCode;
    }

    public void setDatas(List<byte[]> datas) {
        this.datas = datas;
    }
    
    // data (NOTE: in case of multipart, this can contain mixing of text: gml and binary: e.g: tiff and so on)
    public List<byte[]> getDatas() {
        return datas;
    }
    
    public boolean hasDatas() {
        return datas != null && !datas.isEmpty();
    }
    
    public boolean isMultipart() {
        return hasDatas() && datas.size() > 1;
    }
    
    public String getMimeType() {
        String mimeType = getFormatType();
        // To display application/gml+xml in browser, change in HTTP response to text/xml
        if (mimeType.equals(MIMEUtil.MIME_GML)) {
            mimeType = MIMEUtil.MIME_XML;
        }
        return mimeType;
    }    
    
    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    // Encoding in Rasql
    public String getFormatType() {
        return formatType != null ? formatType.trim() : "";
    }

    public int getHTTPCode() {
        return httpCode;
    }

    public String getCoverageID() {
        return (coverageID != null) ? coverageID : DEFAULT_COVERAGE_ID;
    }

    public void setCoverageID(String coverageID) {
        this.coverageID = coverageID;
    }
}
