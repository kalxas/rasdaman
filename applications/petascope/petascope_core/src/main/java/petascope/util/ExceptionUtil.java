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
  *  Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import org.apache.commons.io.IOUtils;
import org.rasdaman.config.VersionManager;
import org.slf4j.LoggerFactory;
import static petascope.core.KVPSymbols.WCS_SERVICE;
import static petascope.core.KVPSymbols.WMS_SERVICE;
import petascope.core.Templates;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.ExceptionReport;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WMSException;

/**
 * Exception utility class
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class ExceptionUtil {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExceptionUtil.class);
    
    /**
     * Handle exception and write result to client.
     */
    public static void handle(String version, Exception ex, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType(MIMEUtil.MIME_XML);
        
        log.error("Caught an exception ", ex);
        
        OutputStream outputStream = httpServletResponse.getOutputStream();

        httpServletResponse.setContentType(MIMEUtil.MIME_XML);
        httpServletResponse.setHeader("Content-disposition", "inline; filename=error.xml");  

        ExceptionReport exceptionReport = ExceptionUtil.exceptionToReportString(ex, version);
        httpServletResponse.setStatus(exceptionReport.getHttpCode());
        IOUtils.write(exceptionReport.getExceptionText(), outputStream);
        IOUtils.closeQuietly(outputStream);
    }

    /**
     * Return a WCS XML exception in String, used by SOAP for a failure request
     *
     * @param ex
     * @return
     */
    private static ExceptionReport exceptionToReportString(Exception ex, String version) {
        // NOTE: all kind of exceptions will use the WCS exception report, except WMS uses a different kind of XML structure.
        String exceptionText = Templates.getTemplate(Templates.GENERAL_WCS_EXCEPTION_REPORT);

        String exceptionCodeName = "";
        String detailMessage = "";
        int httpCode = SC_INTERNAL_SERVER_ERROR;

        if (ex instanceof WCSException) {
            ExceptionCode exceptionCode = ((WCSException) ex).getExceptionCode();
            exceptionCodeName = exceptionCode.getExceptionCodeName();
            httpCode = exceptionCode.getHttpErrorCode();
            detailMessage = ((WCSException) ex).getExceptionText();
        } else if (ex instanceof WCPSException) {
            ExceptionCode exceptionCode = ((WCPSException) ex).getExceptionCode();
            exceptionCodeName = exceptionCode.getExceptionCodeName();
            httpCode = exceptionCode.getHttpErrorCode();
            detailMessage = ((WCPSException) ex).getMessage();
        } else if (ex instanceof WMSException) {       
            // NOTE: WMS use different exception report structure.
            exceptionText = Templates.getTemplate(Templates.GENERAL_WMS_EXCEPTION_REPORT);
            
            exceptionCodeName = ((WMSException) ex).getExceptionCode();
            httpCode = ExceptionCode.InvalidRequest.getHttpErrorCode();
            detailMessage = ((WMSException) ex).getMessage();
        } else if (ex instanceof SecoreException) {
            ExceptionCode exceptionCode = ((SecoreException) ex).getExceptionCode();
            exceptionCodeName = exceptionCode.getExceptionCodeName();
            httpCode = exceptionCode.getHttpErrorCode();
            detailMessage = ((SecoreException) ex).getExceptionText();
        } else if (ex instanceof PetascopeException) {
            ExceptionCode exceptionCode = ((PetascopeException) ex).getExceptionCode();
            exceptionCodeName = exceptionCode.getExceptionCodeName();
            httpCode = exceptionCode.getHttpErrorCode();
            detailMessage = ((PetascopeException) ex).getExceptionText();
        } else if (ex instanceof ServletException) {
            if (ex.getMessage().contains("Missing basic authentication header")) {
                exceptionCodeName = ExceptionCode.AccessDenied.getExceptionCodeName();
                detailMessage = ex.getMessage();
                httpCode = SC_NOT_FOUND;
            } else {
                ExceptionCode exceptionCode = ExceptionCode.InvalidRequest;
                exceptionCodeName = ExceptionCode.InvalidRequest.getExceptionCodeName();
                httpCode = exceptionCode.getHttpErrorCode();
                detailMessage = ex.getMessage();
            }
        } else {
            // Other kinds of exception, also needs to wrap in a XML exception report
            exceptionCodeName = ExceptionCode.InternalComponentError.getExceptionCodeName();
            httpCode = ExceptionCode.InternalComponentError.getHttpErrorCode();
            detailMessage = ex.getMessage();
            if (detailMessage == null) {
                detailMessage = ex.getClass().getSimpleName();
            }
            detailMessage += ". Please check the log for the detail error.";
        }
        
        if (detailMessage == null) {
            detailMessage = "Unknown error, possibly a null pointer exception; please check the petascope log for further details.";
        }
        
        if (ex instanceof WMSException) {
            if (!VersionManager.isSupported(WMS_SERVICE, version)) {
                version = VersionManager.getLatestVersion(WMS_SERVICE);
            }
        } else if (!VersionManager.isSupported(WCS_SERVICE, version)) {
            version = VersionManager.getLatestVersion(WCS_SERVICE);
        }
        
        exceptionText = exceptionText.replace(Templates.GENERAL_EXCEPTION_VERSION_REPLACEMENT, version);
        exceptionText = exceptionText.replace(Templates.GENERAL_EXCEPTION_CODE_REPLACEMENT, exceptionCodeName);
        exceptionText = exceptionText.replace(Templates.GENERAL_EXCEPTION_TEXT_REPLACEMENT, detailMessage);

        return new ExceptionReport(exceptionText, httpCode);
    }

    /**
     * Return an exception report but for SOAP request which needs to add the
     * result in SOAP envelope
     *
     * @param ex
     * @return
     */
    public static ExceptionReport exceptionToReportStringSOAP(Exception ex) {
        ExceptionReport exceptionReport = exceptionToReportString(ex, null);
        String exceptionText = exceptionReport.getExceptionText();
        exceptionText = SOAPUtil.addSOAPFailureMessageBody(exceptionText);

        return new ExceptionReport(exceptionText, exceptionReport.getHttpCode());
    }
}
