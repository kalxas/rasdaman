/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package petascope.util;

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

    /**
     * Return a WCS XML exception in String, used by SOAP for a failure request
     *
     * @param ex
     * @return
     */
    public static ExceptionReport exceptionToReportString(Exception ex) {
        // NOTE: all kind of exceptions will use the WCS exception report, except WMS uses a different kind of XML structure.
        String exceptionText = Templates.getTemplate(Templates.GENERAL_WCS_EXCEPTION_REPORT);

        String exceptionCodeName;
        String detailMessage;
        int httpCode;

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
        ExceptionReport exceptionReport = exceptionToReportString(ex);
        String exceptionText = exceptionReport.getExceptionText();
        exceptionText = SOAPUtil.addSOAPFailureMessageBody(exceptionText);

        return new ExceptionReport(exceptionText, exceptionReport.getHttpCode());
    }
}
