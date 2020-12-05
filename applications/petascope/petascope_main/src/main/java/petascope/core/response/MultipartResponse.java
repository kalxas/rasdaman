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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Provides encapsulation of an HttpServletResponse that is to be sent as multipart.
 */
public class MultipartResponse {

    private ServletOutputStream responseOutputStream;

    private static final String MULTIPART_CONTENT_TYPE = "multipart/x-mixed-replace;boundary=End";
    private static final String CONTENT_TYPE_KEY = "Content-type: ";
    private static final String PART_END = "--End";
    private static final String FINISH_END = "--End--";

    /**
     * Makes the response multi part by setting its content type.
     * @param servletResponse
     * @throws IOException
     */
    public MultipartResponse(HttpServletResponse servletResponse) throws IOException {
        servletResponse.setContentType(MULTIPART_CONTENT_TYPE);
        this.responseOutputStream = servletResponse.getOutputStream();
    }

    /**
     * Write content type with the mime
     */
    public void writeContentType(String contentType) throws IOException {
        this.addContentType(contentType);
        this.addLine();
    }

    /**
     * Ends the current part in the response.
     */
    public void endPart() throws IOException {
        this.addEnd();
    }

    /**
     * Adds the ending part in the response body.
     */
    public void finish() throws IOException {
        this.addLine();
        this.addFinish();
    }

    public void addLine() throws IOException {
        this.responseOutputStream.println();
    }

    private void addEnd() throws IOException {
        this.responseOutputStream.println(PART_END);
        this.responseOutputStream.flush();
    }

    private void addContentType(String contentType) throws IOException {
        this.responseOutputStream.println(CONTENT_TYPE_KEY + contentType);
    }

    private void addFinish() throws IOException {
        this.responseOutputStream.println(FINISH_END);
        this.responseOutputStream.flush();
    }
}
