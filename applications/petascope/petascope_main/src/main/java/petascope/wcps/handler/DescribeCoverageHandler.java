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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import nu.xom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.core.XMLSymbols.LABEL_GENERAL_GRID_COVERAGE;
import static petascope.core.gml.GMLDescribeCoverageBuilder.isCIS11;
import petascope.core.gml.GMLWCSRequestResultBuilder;
import petascope.core.json.JSONWCSRequestResultBuilder;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.MIMEUtil;
import petascope.util.XMLUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;

/**
 * Class to handle describe(coverageExpression, "format", "extra params") operator
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class DescribeCoverageHandler {
    
    @Autowired
    private GMLWCSRequestResultBuilder gmlWCSRequestResultBuilder;
    @Autowired
    private JSONWCSRequestResultBuilder jsonWCSRequestResultBuilder;
    
    // For General Grid Coverage in CIS 1.1
    public static final String EXTRA_PARAMETER_KEY_OUTPUT_TYPE = "outputType";
    
    public WcpsMetadataResult handle(WcpsResult coverageExpression, String outputFormat, String extraParams) throws PetascopeException, SecoreException {

        if (!(MIMEUtil.isGML(outputFormat) || MIMEUtil.isJSON(outputFormat))) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Format value is not supported. Given '" + outputFormat + "'");
        }
        
        // to differentiate GML CIS 1.0 and CIS 1.1 GeneralGridCoverage
        String coverageOutputType = "";
        
        if (extraParams.contains("=")) {
            String[] params = extraParams.split(",");
            for (String param : params) {
                if (param.contains(EXTRA_PARAMETER_KEY_OUTPUT_TYPE)) {
                    // e.g: return describe( $c, "application/json", "outputType=GeneralGridCoverage" )
                    coverageOutputType = param.split("=")[1];
                }
            }
        }
        
        if (!coverageOutputType.isEmpty() && !coverageOutputType.equals(LABEL_GENERAL_GRID_COVERAGE)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Value for 'outputType' parameter is not supported. Given '" + coverageOutputType + "'");
        }
        
        if (MIMEUtil.isJSON(outputFormat) && !coverageOutputType.equals(LABEL_GENERAL_GRID_COVERAGE)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Encoding in JSON format needs to have \"outputType=GeneralGridCoverage\" as extra parameter");
        }
        
        String output = "";
        WcpsCoverageMetadata wcpsCoverageMetadata = coverageExpression.getMetadata();
        if (MIMEUtil.isGML(outputFormat)) {
            if (isCIS11(coverageOutputType)) {
                // CIS 1.1 GML
                wcpsCoverageMetadata.setCoverageType(LABEL_GENERAL_GRID_COVERAGE);
            }
            
            Element element = this.gmlWCSRequestResultBuilder.buildGetCoverageResult(wcpsCoverageMetadata, null);
            output = XMLUtil.formatXML(element.toXML());
            
            outputFormat = MIMEUtil.MIME_GML;
            
        } else if (MIMEUtil.isJSON(outputFormat)) {
            output = this.jsonWCSRequestResultBuilder.buildGetCoverageResult(wcpsCoverageMetadata, null);
            
            outputFormat = MIMEUtil.MIME_JSON;
        }
        
        WcpsMetadataResult result = new WcpsMetadataResult(coverageExpression.getMetadata(), output);
        result.setMimeType(outputFormat);
        
        return result;
    }
    
}
