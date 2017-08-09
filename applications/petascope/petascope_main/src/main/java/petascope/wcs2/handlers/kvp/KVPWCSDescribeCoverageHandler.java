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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Templates;
import petascope.core.gml.GmlCoverageBuilder;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.exceptions.WMSException;
import petascope.util.MIMEUtil;
import petascope.util.XMLUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.WcpsCoverageMetadataTranslator;

/**
 * Class which handle WCS 2.0.1 DescribeCoverage request NOTE: 1 coverage can
 * have multiple coverageIds e.g: coverageIds=test_mr,test_irr_cube_2 the XML
 * result is concatenated from both GML results.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class KVPWCSDescribeCoverageHandler extends KVPWCSAbstractHandler {

    @Autowired
    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    @Autowired
    private GmlCoverageBuilder gmlCoverageBuilder;

    @Override
    public void validate(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        if (kvpParameters.get(KVPSymbols.KEY_COVERAGEID) == null) {
            throw new WCSException(ExceptionCode.InvalidRequest, "A DescribeCoverage request must specify at least one " + KVPSymbols.KEY_COVERAGEID + ".");
        }
    }

    @Override
    public Response handle(Map<String, String[]> kvpParameters) throws PetascopeException, SecoreException, WMSException {
        // Validate before handling the request
        this.validate(kvpParameters);

        // DecribeCoverage can contain multiple coverageIds (e.g: coverageIds=test_mr,test_irr_cube_2)
        String[] coverageIds = kvpParameters.get(KVPSymbols.KEY_COVERAGEID)[0].split(",");

        // the DescribeCoverage template which can contain multiples coverages's description
        String gmlTemplate = Templates.getTemplate(Templates.WCS2_DESCRIBE_COVERAGE_FILE);
        String content = "";

        // The result is:
//        <wcs:CoverageDescriptions>
//          <wcs:CoverageDescription gml:id="test_mr">...</wcs:CoverageDescription>
//          <wcs:CoverageDescription gml:id="test_rgb">...</wcs:CoverageDescription>
//        </wcs:CoverageDescriptions>
        for (String coverageId : coverageIds) {
            WcpsCoverageMetadata wcpsCoverageMetadata = wcpsCoverageMetadataTranslator.translate(coverageId);
            // After that, we can build the GML string for the DescribeCoverage
            String gml = gmlCoverageBuilder.build(wcpsCoverageMetadata, Templates.WCS2_DESCRIBE_COVERAGE_COVERAGE_DESCRIPTION_FILE);
            content += gml + "\n";
        }

        // Replace the content in the template
        gmlTemplate = gmlTemplate.replace(Templates.WCS2_DESCRIBE_COVERAGE_CONTENT, content);

        // format the output with indentation
        gmlTemplate = XMLUtil.formatXML(gmlTemplate);

        return new Response(Arrays.asList(gmlTemplate.getBytes()), MIMEUtil.MIME_GML, null);
    }

}
