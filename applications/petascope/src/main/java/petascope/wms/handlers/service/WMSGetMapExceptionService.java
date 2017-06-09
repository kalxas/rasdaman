/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wms.handlers.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.MIMEUtil;
import petascope.wms.handlers.kvp.KVPWMSGetCapabilitiesHandler;

/**
 * There are 3 types of exceptions in WMS: + XML (default): just throw it and
 * later on will be caught and written in WMS 1.3.0 exception report. + BLANK
 * (image): return a blank image. + INIMAGE (image): return an image containing
 * exception text
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WMSGetMapExceptionService {

    private static Logger log = LoggerFactory.getLogger(WMSGetMapExceptionService.class);

    private String errorMessage;
    private String exceptionFormat;
    private int width;
    private int height;
    // output image format
    private String format;

    public WMSGetMapExceptionService() {

    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setExceptionFormat(String exceptionFormat) {
        this.exceptionFormat = exceptionFormat;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Create an image for the exception to the WMS client.
     *
     * @return
     */
    public Response createImageExceptionResponse() {
        String mimeFormat = this.format;

        //create buffered image object img
        Color backgroundColor = new Color(0, 0, 0);
        BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, this.width, this.height);
        if (exceptionFormat.equalsIgnoreCase(KVPWMSGetCapabilitiesHandler.EXCEPTION_INIMAGE)) {
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Arial Black", Font.TRUETYPE_FONT, 10));
            graphics.drawString(this.errorMessage, width / 10, height / 2);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, MIMEUtil.getEncodingType(mimeFormat).get(0), byteArrayOutputStream);
        } catch (PetascopeException | IOException ex) {
            log.error("Cannot create WMS image exception", ex);
        }

        byte[] bytes = byteArrayOutputStream.toByteArray();
        Response response = new Response(Arrays.asList(bytes), mimeFormat, ExceptionCode.InternalWMSError.getHttpErrorCode());

        return response;
    }
}
