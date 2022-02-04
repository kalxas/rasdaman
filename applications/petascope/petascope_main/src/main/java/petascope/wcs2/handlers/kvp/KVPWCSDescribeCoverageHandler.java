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
package petascope.wcs2.handlers.kvp;

import java.util.Arrays;
import petascope.core.response.Response;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nu.xom.Element;
import org.rasdaman.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.controller.PetascopeController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_COVERAGEID;
import static petascope.core.KVPSymbols.VALUE_GENERAL_GRID_COVERAGE;
import petascope.core.gml.GMLWCSRequestResultBuilder;
import petascope.exceptions.WMSException;
import petascope.util.MIMEUtil;
import petascope.util.XMLUtil;
import static petascope.core.KVPSymbols.KEY_OUTPUT_TYPE;
import static petascope.core.KVPSymbols.KEY_REQUEST;
import static petascope.core.KVPSymbols.KEY_SERVICE;
import static petascope.core.KVPSymbols.KEY_VERSION;
import petascope.util.SetUtil;

/**
 * Class which handle WCS 2.0.1 DescribeCoverage request NOTE: 1 coverage can
 * have multiple coverageIds e.g: coverageIds=test_mr,test_irr_cube_2 the XML
 * result is concatenated from both GML results.
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWCSDescribeCoverageHandler extends KVPWCSAbstractHandler {

    @Autowired
    private GMLWCSRequestResultBuilder gmlWCSRequestResultBuilder;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private PetascopeController petascopeController;
    
    protected static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_SERVICE, KEY_VERSION, KEY_REQUEST, 
                                                                                  KEY_COVERAGEID, KEY_OUTPUT_TYPE);

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        if (kvpParameters.get(KVPSymbols.KEY_COVERAGEID) == null) {
            throw new WCSException(ExceptionCode.InvalidRequest, "A DescribeCoverage request must specify at least one " + KVPSymbols.KEY_COVERAGEID + ".");
        }
        
        this.validateParameters(kvpParameters, VALID_PARAMETERS);
        this.validateCoverageConversionCIS11(kvpParameters);
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        // Validate before handling the request
        this.validate(kvpParameters);

        // DecribeCoverage can contain multiple coverageIds (e.g: coverageIds=test_mr,test_irr_cube_2)
        String[] coverageIds = kvpParameters.get(KEY_COVERAGEID)[0].split(",");
        String outputType = this.getKVPValue(kvpParameters, KEY_OUTPUT_TYPE);
        if (outputType != null && !outputType.equalsIgnoreCase(VALUE_GENERAL_GRID_COVERAGE)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "GET KVP value for key '" + KEY_OUTPUT_TYPE + "' is not valid. Given: '" + outputType + "'.");
        }

        // The result is:
//        <wcs:CoverageDescriptions>
//          <wcs:CoverageDescription gml:id="test_mr">...</wcs:CoverageDescription>
//          <wcs:CoverageDescription gml:id="test_rgb">...</wcs:CoverageDescription>
//        </wcs:CoverageDescriptions>

        Element coverageDescriptionsElement = this.gmlWCSRequestResultBuilder.buildDescribeCoverageResult(outputType, Arrays.asList(coverageIds));
        String result = coverageDescriptionsElement.toXML();

        result = XMLUtil.formatXML(result);

        return new Response(Arrays.asList(result.getBytes()), MIMEUtil.MIME_GML, coverageIds[0]);
    }
}
