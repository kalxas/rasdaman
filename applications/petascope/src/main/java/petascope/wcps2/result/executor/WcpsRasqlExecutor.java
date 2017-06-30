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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.result.executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.ras.RasUtil;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.result.WcpsResult;
import petascope.core.gml.GmlCoverageBuilder;
import petascope.util.MIMEUtil;
import petascope.core.Templates;
import petascope.util.XMLUtil;

/**
 * Execute the Rasql query and return result.
 *
 * @author <a href="mailto:bphamhuux@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class WcpsRasqlExecutor implements WcpsExecutor<WcpsResult> {

    @Autowired
    private GmlCoverageBuilder gmlCoverageBuilder;

    public WcpsRasqlExecutor() {

    }

    /**
     * Execute the Rasql query and return result.
     *
     * @param wcpsResult
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    @Override
    public byte[] execute(WcpsResult wcpsResult) throws PetascopeException, SecoreException {
        // mimeType is a full mime, e.g: application/gml+xml, image/png,...
        String mimeType = wcpsResult.getMimeType();
        // Return the result of rasql query as array of bytes
        byte[] arrayData = RasUtil.getRasqlResultAsBytes(wcpsResult.getRasql());
        // If encoding is gml so build the GML Coverage with the tupleList contains the rasql result values
        if (mimeType != null && mimeType.equals(MIMEUtil.MIME_GML)) {
            return buildGmlCovResult(wcpsResult.getMetadata(), arrayData);
        } else {
            // not gml, just return the result as binary (png, tiff,...) or text (csv, json)
            return arrayData;
        }
    }

    /**
     * Build a GML coverage in application/gml+xml as a GetCoverage request
     *
     * @param wcpsCoverageMetadata
     * @param arrayData
     * @return
     * @throws PetascopeException
     * @throws SecoreException
     */
    private byte[] buildGmlCovResult(WcpsCoverageMetadata wcpsCoverageMetadata, byte[] arrayData) throws PetascopeException, SecoreException {
        // Run the rasql query to get the data and put in <tupleList ts="," cs="> ... </tupleList>        
        String data = new String(arrayData);
        data = this.rasJsonToTupleList(data);

        // Get the GML Coverage in application/gml+xml format (text)
        String gml = gmlCoverageBuilder.build(wcpsCoverageMetadata, Templates.WCS2_GET_COVERAGE_FILE);
        // and add the rasdaman result in the tupeList element
        gml = gml.replace(GmlCoverageBuilder.KEY_COVERAGEDATA, data);
        
        // format the output with indentation
        gml = XMLUtil.formatXML(gml);

        return gml.getBytes();
    }

    /**
     * Transforms a JSON output (http://rasdaman.org/ticket/1578) returned by rasdaman server into a JSON format
     * accepted by the gml:tupleList according to section 19.3.8 of the OGC GML
     * standard version 3.2.1
     *
     * @param json - a JSON input like [b1 b2 ... bn, b1 b2 ... bn, ...], [...]
     * where each [...] represents a dimension and each sequence b1 ... bn n
     * bands
     * @return JSON string of form b1 b2 .. bn, b1 b2 ... bn, ...
     */
    private String rasJsonToTupleList(String json) {
        return json.replace("[", "").replace("]", "").replace("\"", "");
    }
}
