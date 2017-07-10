/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package petascope.util;

import petascope.core.Pair;
import petascope.core.Templates;

/**
 * Utility class for SOAP request
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class SOAPUtil {

    /**
     * For a failure request from SOAP POST, it also needs to return a result in
     * SOAP body
     *
     * @param textError
     * @return
     */
    public static String addSOAPFailureMessageBody(String textError) {
        textError = Templates.getTemplate(Templates.GENERAL_SOAP_FAULT, Pair.of("%exceptionReport%", XMLUtil.removeXmlDecl(textError)));
        return textError;
    }

    /**
     * For a failure request from SOAP POST, it also needs to return a result in
     * SOAP body
     *
     * @param textResult
     * @return
     */
    public static String addSOAPSuccessMessageBody(String textResult) {
        textResult = Templates.getTemplate(Templates.GENERAL_SOAP_MESSAGE, Pair.of("%body%", XMLUtil.removeXmlDecl(textResult)));
        return textResult;
    }
}
