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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.controller.handler.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.rasdaman.config.ConfigManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionReport;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.ExceptionUtil;
import petascope.util.SOAPUtil;
import petascope.util.XMLUtil;
import petascope.util.MIMEUtil;

/**
 * Service class to handler SOAP POST WCS
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class SOAPWCSServiceHandler extends AbstractHandler {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SOAPWCSServiceHandler.class);

    @Autowired
    XMLWCSServiceHandler xmlWCSServiceHandler;

    public SOAPWCSServiceHandler() {
        // SOAP WCS is a part of WCS2
        service = KVPSymbols.KEY_SOAP;        
        requestServices.add(KVPSymbols.VALUE_GET_CAPABILITIES);
        requestServices.add(KVPSymbols.VALUE_DESCRIBE_COVERAGE);
        requestServices.add(KVPSymbols.VALUE_GET_COVERAGE);
        requestServices.add(KVPSymbols.VALUE_PROCESS_COVERAGES);
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws WCSException, IOException, PetascopeException, SecoreException {
        Response response;
        // NOTE: SOAP request is as same as XML request, but the output must be enclosed by SOAP envelope for both success/failure case
        try {
            response = xmlWCSServiceHandler.handle(kvpParameters);
            byte[] bytes = response.getDatas().get(0);
            String textResult = new String(bytes);
            if (!XMLUtil.isXmlString(textResult)) {
                textResult = new String(Base64.encodeBase64(bytes));
            }
            textResult = SOAPUtil.addSOAPSuccessMessageBody(textResult);
            response.setDatas(Arrays.asList(textResult.getBytes()));
        } catch (Exception ex) {            
            ExceptionReport exceptionReport = ExceptionUtil.exceptionToReportStringSOAP(ex);
            response = new Response(Arrays.asList(exceptionReport.getExceptionText().getBytes()), 
                    MIMEUtil.MIME_XML, exceptionReport.getHttpCode());
            log.error("Exception when handling SOAP request", ex);
        }

        return response;
    }
}
