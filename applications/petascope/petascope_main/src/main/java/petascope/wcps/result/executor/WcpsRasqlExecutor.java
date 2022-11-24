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
package petascope.wcps.result.executor;

import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import nu.xom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import static petascope.core.XMLSymbols.LABEL_GENERAL_GRID_COVERAGE;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.ras.RasUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsResult;
import petascope.util.MIMEUtil;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.TimeUtil;
import petascope.core.gml.GMLWCSRequestResultBuilder;
import petascope.core.json.JSONWCSRequestResultBuilder;
import petascope.util.StringUtil;
import petascope.util.XMLUtil;
import static petascope.wcps.handler.ClipWKTExpressionHandler.WITH_COORDINATES;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.wcps.metadata.model.ParsedSubset;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.metadata.service.CoordinateTranslationService;

/**
 * Execute the Rasql query and return result.
 *
 * @author <a href="mailto:bphamhuux@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class WcpsRasqlExecutor implements WcpsExecutor<WcpsResult> {

    @Autowired
    private GMLWCSRequestResultBuilder gmlWCSRequestResultBuilder;
    
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private JSONWCSRequestResultBuilder jsonWCSRequestResultBuilder;

    public WcpsRasqlExecutor() {
    }

    @Override
    public byte[] execute(WcpsResult wcpsResult) throws PetascopeException, SecoreException {
        // mimeType is a full mime, e.g: application/gml+xml, image/png,...
        String mimeType = wcpsResult.getMimeType();
        // Return the result of rasql query as array of bytes
        Pair<String, String> rasUserCredentialsPair = AuthenticationService.getBasicAuthCredentialsOrRasguest(this.httpServletRequest);
        byte[] arrayData = RasUtil.getRasqlResultAsBytes(wcpsResult.getRasql(), rasUserCredentialsPair.fst, rasUserCredentialsPair.snd);
        // If encoding is gml so build the GML Coverage with the tupleList contains the rasql result values
        if (mimeType != null) {
            String coverageType = "";
            if (wcpsResult.getMetadata() != null) {
                coverageType = wcpsResult.getMetadata().getCoverageType();
            }
            
            if (coverageType == null) {
                return arrayData;
            }
            
            if (wcpsResult.withCoordinates()) {
                if (!(mimeType.equals(MIMEUtil.MIME_CSV) || mimeType.equals(MIMEUtil.MIME_JSON))) {
                    throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                                 "'" + WITH_COORDINATES.trim() + "' can be applied with encode in CSV/JSON only, given '" + mimeType + "'.");
                }
                arrayData = this.buildWithCoordinatesResult(wcpsResult.getMetadata(), arrayData, mimeType);
            } else if (mimeType.equals(MIMEUtil.MIME_GML)) {
                // encode(c, "gml")
                arrayData = this.buildGmlCovResult(wcpsResult.getMetadata(), arrayData);
            } else if (mimeType.equals(MIMEUtil.MIME_JSON) && coverageType.equals(LABEL_GENERAL_GRID_COVERAGE)) {
                // encode(c, "json", "{\"outputType\":\"GeneralGridCoverage\"}")
                arrayData = this.buildJsonCovResult(wcpsResult.getMetadata(), arrayData);
            }
        }
        return arrayData;
    }
    
    /**
     * Translate grid to geo coordinates for encode(clip(c, LineString()) with coordinates, "csv/json").
     * The result is 1D array as string.
     * 
     * @TODO: It can process only for 1D array result now.
     */
    private String translateGridToGeoCoordinates(WcpsCoverageMetadata wcpsCoverageMetadata, String data) throws PetascopeException, SecoreException {
        data = data.replace("{", "").replace("}", "").replace("[", "").replace("]", "");
        
        List<String> translatedArrayValues = new ArrayList<>();
        int numberOfOriginalAxes = wcpsCoverageMetadata.getNumberOfOriginalAxes();

        // e.g: "x1 y1 value1","x2 y2 value2","x3 y3 value3"            
        String[] arrayValues = data.substring(1, data.length() - 1).split(",");
        
        // Grid coordinates should be translated to geo coordinates with geo axes order (not rasdaman order).
        Map<Integer, String> geoCoordinatesMap = new TreeMap<>();
        
        for (String arrayValue : arrayValues) {
            arrayValue = arrayValue.replace("\"", "");

            String[] values = arrayValue.split(" ");
            List<String> geoCoordinates = new ArrayList<>();
            
            for (int i = 0; i < numberOfOriginalAxes; i++) {
                String gridCoordinate = values[i];
                Axis axis = wcpsCoverageMetadata.getOriginalAxisByGridOrder(i);
                ParsedSubset<BigDecimal> gridSubset = new ParsedSubset<>(new BigDecimal(gridCoordinate), new BigDecimal(gridCoordinate));
                BigDecimal gridDomainMin = axis.getGridBounds().getLowerLimit();
                BigDecimal gridDomainMax = axis.getGridBounds().getUpperLimit();
                BigDecimal resolution = axis.getResolution();
                BigDecimal geoDomainMin = axis.getGeoBounds().getLowerLimit();
                
                ParsedSubset<BigDecimal> geoSubset = null;
                String geoCoordinate = "";
                if (axis instanceof RegularAxis) {
                    geoSubset = CoordinateTranslationService.gridToGeoForRegularAxis(gridSubset, gridDomainMin, gridDomainMax, resolution, geoDomainMin);
                    geoCoordinate = geoSubset.getUpperLimit().toPlainString();
                } else {
                    geoSubset = CoordinateTranslationService.gridToGeoForIrregularAxes(gridSubset, (IrregularAxis)axis);                    
                }
                if (axis.isTimeAxis()) {
                    // Time value should strip "2018-01-01" -> 2018-01-01 to present in CSV/JSON encode
                    geoCoordinate = TimeUtil.valueToISODateTime(BigDecimal.ZERO, geoSubset.getLowerLimit(), CrsUtil.getCrsDefinition(axis.getNativeCrsUri())).replace("\"", "");
                }
                
                int geoAxisOrder = wcpsCoverageMetadata.getOriginalAxisGeoOrder(axis.getLabel());
                geoCoordinatesMap.put(geoAxisOrder, geoCoordinate);                
            }

            List<String> bandValues = new ArrayList<>();
            for (int i = numberOfOriginalAxes; i < values.length; i++) {
                bandValues.add(values[i]);
            }
            
            geoCoordinates.addAll(geoCoordinatesMap.values());
            geoCoordinates.addAll(bandValues);                
            String translatedArrayValue = "\"" +  ListUtil.join(geoCoordinates, " ") + "\"";
            translatedArrayValues.add(translatedArrayValue);
        }
        
        String result = ListUtil.join(translatedArrayValues, ",");
        return result;
    }
    
    /**
     * If encode(clip(c, LineString()) WITH COORDINATES, "csv/json") then parse result of rasdaman 
     * 
     * "grid_x1 grid_y1 value1","grid_x2 grid_y2 value2",...
     * to 
     * "geo_x1 geo_y1 value1","geo_x2 geo_y2 value2",...
     * 
     * and return to client in same MIME type (csv/json).
     */
    private byte[] buildWithCoordinatesResult(WcpsCoverageMetadata wcpsCoverageMetadata, byte[] arrayData, String mimeType) throws PetascopeException, SecoreException {
        
        String data = new String(arrayData);
        String result = this.translateGridToGeoCoordinates(wcpsCoverageMetadata, data);
            
        // Prase 1D array from clip(c, LineString()) WITH COORDINATES as JSON/CSV to translate grid coordinates to geo coordinates
        if (mimeType.equals(MIMEUtil.MIME_JSON)) {
            result = "[" + result + "]";
        }
        return result.getBytes();
    }

    /**
     * Build a GML coverage in application/gml+xml as a GetCoverage request
     */
    private byte[] buildGmlCovResult(WcpsCoverageMetadata wcpsCoverageMetadata, byte[] arrayData) throws PetascopeException, SecoreException {
        // Run the rasql query to get the data and put in <tupleList ts="," cs="> ... </tupleList>        
        String tupleList = new String(arrayData);
        tupleList = this.rasJsonToTupleList(tupleList);

        Element gmlGetCoverageElement = this.gmlWCSRequestResultBuilder.buildGetCoverageResult(wcpsCoverageMetadata, tupleList);              
        // format the output with indentation
        String gml = XMLUtil.formatXML(gmlGetCoverageElement);

        return gml.getBytes();
    }

    /**
     * Return the list of Object (String / BigDecimal) for CIS 1.1 JSON RangeSet values
     */
    public List<Object> getJsonPixelValues(byte[] arrayData) {
        String tupleList = new String(arrayData);
        
        List<Object> pixelValuesObjects = new ArrayList<>();
        List<String> pixelValues = Arrays.asList(this.rasJsonToTupleList(tupleList).split(","));
        
        boolean hasMultipleBands = tupleList.contains("\"");
        for (String value : pixelValues) {
            if (hasMultipleBands) {
                // e.g: "01 01"
                pixelValuesObjects.add(value);
            } else {
                // e.g: 2.35353
                pixelValuesObjects.add(new BigDecimal(value));
            }
        }
        
        return pixelValuesObjects;
    }
    
    /**
     * Build a JSON CIS 1.1 coverage result as a GetCoverage request with formatType=application/json&outputType=GeneralGridCoverage
     */
    private byte[] buildJsonCovResult(WcpsCoverageMetadata wcpsCoverageMetadata, byte[] arrayData) throws PetascopeException, SecoreException { 
        List<Object> pixelValuesObjects = this.getJsonPixelValues(arrayData);
        
        String jsonResult = this.jsonWCSRequestResultBuilder.buildGetCoverageResult(wcpsCoverageMetadata, pixelValuesObjects);
        return jsonResult.getBytes();
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
        /*
        e.g: coverage has 2 bands
        
        <V>01 01</V>
        <V>02 02</V>
        <V>03 03</V>
        */
        String result = StringUtil.stripQuotes(StringUtil.stripBrackets(json));
        
        return result;
    }
}
