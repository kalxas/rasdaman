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
package petascope.wcps.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.rasdaman.domain.cis.NilValue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.parameters.model.netcdf.NetCDFExtraParams;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.result.WcpsResult;
import petascope.util.JSONUtil;
import petascope.util.MIMEUtil;
import petascope.wcps.encodeparameters.model.GeoReference;
import petascope.wcps.encodeparameters.service.GeoReferenceService;
import petascope.wcps.encodeparameters.service.SerializationEncodingService;
import petascope.wcps.exception.processing.InvalidJsonDeserializationException;
import petascope.wcps.exception.processing.InvalidNumberOfNodataValuesException;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.parameters.netcdf.service.NetCDFParametersService;

/**
 * Class to translate a WCPS expression with encode() *
 *
 * NOTE: we use the new JSON encoding format when generating rasql expression:
 * http://rasdaman.org/wiki/CommonFormatsInterface
 *
 *
 * rasql -q 'SELECT encode(c, "GTiff", "{ \"geoReference\": { \"bbox\": {
 * \"xmin\": 111.975, \"ymin\": -44.525, \"xmax\": 156.275, \"ymax\": -8.975, },
 * \"crs\": \"http://localhost:8080/def/crs/EPSG/0/4326\" } }") FROM
 * test_mean_summer_airtemp AS c'  <code>
 * WCPS: return encode(c, "tiff", "nodata=0")
 * </code> translates to  <code>
 * RASQL: select encode(c, "GTiff", { "nodata": 0 })
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EncodeCoverageHandler extends Handler {

    private org.slf4j.Logger log = LoggerFactory.getLogger(EncodeCoverageHandler.class);

    @Autowired
    private SerializationEncodingService serializationEncodingService;
    @Autowired
    private NetCDFParametersService netCDFParametersFactory;
    
    public EncodeCoverageHandler() {
        
    }
    
    public EncodeCoverageHandler create(Handler coverageExpressionHandler, Handler formatTypeStringScalarHandler, Handler extraParamsStringScalarHandler) {
        EncodeCoverageHandler result = new EncodeCoverageHandler();
        result.serializationEncodingService = this.serializationEncodingService;
        result.netCDFParametersFactory = this.netCDFParametersFactory;
        result.setChildren(Arrays.asList(coverageExpressionHandler, formatTypeStringScalarHandler, extraParamsStringScalarHandler));
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        WcpsResult coverageExpressionVisitorResult = (WcpsResult) this.getFirstChild().handle();
        String formatType = ((WcpsResult)this.getSecondChild().handle()).getRasql();
        String extraParams = ((WcpsResult)this.getThirdChild().handle()).getRasql();
        
        WcpsResult result = this.handle(coverageExpressionVisitorResult, formatType, extraParams, coverageExpressionVisitorResult.withCoordinates());
        return result;
    }

    private WcpsResult handle(WcpsResult coverageExpression, String format, String extraParams, boolean widthCoordinates) throws PetascopeException {
        // get the mime-type before modifying the rasqlFormat
        String mimeType = MIMEUtil.getMimeType(format);
        
        boolean isGML = false;
        
        // NOTE: must use JP2OpenJPEG to encode with geo-reference metadata for JPEG2000 (JP2)
        if (format.contains(MIMEUtil.FORMAT_ID_JP2) || format.contains(MIMEUtil.CODEC_JP2)) {
            format = MIMEUtil.FORMAT_ID_OPENJP2;
        } else if (format.contains(MIMEUtil.ENCODE_GML)) {
            // NOTE: We need the values from JSON encoding of a coverage (http://rasdaman.org/ticket/1578)
            // to add in the tupleLists element of output in application/gml+xml            
            format = MIMEUtil.ENCODE_JSON;
            isGML = true;
        }

        // NOTE: we have 2 cases for extra params:
        // + In old style: for c in (test_eobstest) return encode(c, "netcdf", "nodata=0"), then extra params will need to be built in JSON format
        // + In json style: for c in (test_eobstest) return encode(c, "netcdf", "{\"dimensions\":....}"), then
        //   check if crs and bbox already existed in the JSON extra params, if it does then will not do anything, otherwise, add these param keys-values to this extra param input
        //   then pass it in JSON string as rasql's encode extra parameters
        String otherParamsString = getExtraParams(coverageExpression, format, extraParams, isGML);
        
        //get the right template for rasql string (the dem() encode still use the old format, other will use the new JSON format)
        String template = getTemplate(format);
        String resultRasql = template.replace("$covExpression", coverageExpression.getRasql())
                .replace("$format", '"' + format + '"')
                .replace("$otherParams", otherParamsString);
        WcpsResult wcpsResult = new WcpsResult(coverageExpression.getMetadata(), resultRasql);
        wcpsResult.setWithCoordinates(widthCoordinates);
        wcpsResult.setMimeType(mimeType);

        return wcpsResult;
    }

    /**
     * Depend on the rasqlFormat type to return the correct parameters for this
     * MIME
     *
     * @return
     */
    private String getExtraParams(WcpsResult coverageExpression, String rasqlFormat,
            String extraParams, boolean isGML) throws PetascopeException {
        String otherParamsString = "";
        NetCDFExtraParams netCDFExtraParams = null;
        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // set parameters for dem
        if (rasqlFormat.contains(MIMEUtil.ENCODE_DEM)) {
            // keep the arguments without need to calculate anything else
            otherParamsString = "\"" + extraParams + "\"";
            return otherParamsString;
        } else if (rasqlFormat.contains(MIMEUtil.ENCODE_NETCDF)) {
            // netcdf (we build some netCDF parameters separately)            
            netCDFExtraParams = netCDFParametersFactory.buildParameters(coverageExpression.getMetadata());
        }

        if (metadata != null) {
            // this is the most imporatant parameter which need to be built from coverage metadata
            GeoReferenceService geoReferenceService = new GeoReferenceService();
            GeoReference geoReference = geoReferenceService.buildGeoReference(metadata);

            String jsonOutput = "";
            
            // Check if extra params is in old style or new JSON style
            if (JSONUtil.isJsonValid(extraParams)) {
                // extra params is new JSON style
                jsonOutput = serializationEncodingService.serializeNewStyleExtraParamsToJson(rasqlFormat, extraParams,
                        metadata, netCDFExtraParams, geoReference, isGML);
            } else if (extraParams.contains("{") || extraParams.contains("}")) {
                // it is invalid JSON format and not old style (e.g: "nodata=0")
                log.error("Extra parameters string: " + extraParams + " is not valid JSON format.");
                throw new InvalidJsonDeserializationException();
            } else {
                // extra params is old style (check if it has "nodata" as parameter to add to metadata)
                boolean hasNoData = parseNoDataFromExtraParams(extraParams, metadata);
                jsonOutput = serializationEncodingService.serializeOldStyleExtraParamsToJson(rasqlFormat, metadata, netCDFExtraParams, geoReference, hasNoData,
                                                                                             isGML);
            }

            // as all of the parameters go inside the new JSON style, so replace "{" to "{\""
            otherParamsString = ", \"" + jsonOutput.replace("\"", "\\\"") + "\"";
        }

        return otherParamsString;
    }

    /**
     * To support old style in encoding (e.g: "nodata=0,1,2,3"), we need to
     * parse this string and set the values into coverage metadata. In new JSON
     * style if this value does exist, we just pass it as it is in JSON format
     * to rasql (e.g: "...\"nodata\": [0,1,2,3]...")
     *
     * @param extraParams *
     */
    private boolean parseNoDataFromExtraParams(String extraParams, WcpsCoverageMetadata metadata) {
        String str = extraParams.replace(" ", "");
        if (str.contains(EncodeCoverageHandler.NO_DATA + "=")) {
            // parse param values string to List of nodata values
            // get the value of nodata parameter (e.g: 2,3,4)
            String[] values = str.split("=")[1].split(",");
            
            // Check how many bands of coverag expression
            // NOTE: if nodata has 1 value then it applies to all bands, if nodata has multiple values then it must match: 1 value for 1 band repectively
            // e.g: nodata=2,3,4 then band1 with nodata = 2, band2 with nodata=3, band3 with nodata=4
            int numberOfNodata = values.length;
            // default coverage has at least 1 range field
            int numberOfRange = 1;
            if (metadata.getRangeFields() != null) {
                numberOfRange = metadata.getRangeFields().size();
            }
            // different between number of ranges and number of nodata values (array)
            if (numberOfNodata > 1 && numberOfRange != numberOfNodata) {
                throw new InvalidNumberOfNodataValuesException(numberOfRange, numberOfNodata);
            }
            List<NilValue> noDataValues = new ArrayList<>();
            for (String value : values) {
                noDataValues.add(new NilValue(value, null));
            }

            // Update the nodata values in range fields as well
            updateNoDataInRangeFileds(noDataValues, metadata);

            return true;                
        }
        
        return false;
    }

    /**
     * Update the range filed's nodata value from passing nodata values as extra
     * parameter
     *
     * @param noDataValues
     * @param metadata
     */
    public void updateNoDataInRangeFileds(List<NilValue> noDataValues, WcpsCoverageMetadata metadata) {
        if (!noDataValues.isEmpty()) {
            // We update the range fields of coverages with the passing nodata values
            if (noDataValues.size() == 1) {
                // Only 1 nodata value for all bands
                List<NilValue> nodata = new ArrayList<>();
                nodata.add(noDataValues.get(0));
                for (RangeField rangeField : metadata.getRangeFields()) {
                    rangeField.setNodata(nodata);
                }
            } else {
                // One nodata value for each band
                int i = 0;
                for (RangeField rangeField : metadata.getRangeFields()) {
                    List<NilValue> nodata = new ArrayList<>();
                    nodata.add(noDataValues.get(i));
                    rangeField.setNodata(nodata);
                    i++;
                }
            }
        }
    }

    /**
     * Returns the right template, depending on the operation that has been
     * executed.
     *
     * @param rasqlFormat
     * @return
     */
    private String getTemplate(String rasqlFormat) {
        if (rasqlFormat.equalsIgnoreCase(MIMEUtil.ENCODE_DEM)) {
            return NON_GDAL_OPERATION_TEMPLATE.replace("$rasqlFormat", MIMEUtil.ENCODE_DEM);
        }
        return TEMPLATE;
    }

    /**
     * Check if WCPS should add some default params (e.g: crs=;bbox=....) when
     * encoding. NOTE: encode(c, "csv") or encode(c, "json") should not add
     * these default parameters.
     *
     * @return
     */
    private boolean addDefaultParams(String rasqlFormat) {
        if (rasqlFormat.equalsIgnoreCase(MIMEUtil.ENCODE_CSV)
                || rasqlFormat.equalsIgnoreCase(MIMEUtil.MIME_CSV)
                || rasqlFormat.equalsIgnoreCase(MIMEUtil.ENCODE_JSON)
                || rasqlFormat.equalsIgnoreCase(MIMEUtil.MIME_JSON)) {
            return false;
        }
        return true;
    }

    public static final String NO_DATA = "nodata";

    // otherParams can be empty then no add "," after $format
    private final String TEMPLATE = "encode($covExpression, $format $otherParams)";

    // e.g: dem(): for a in (mr) return encode(a[ i(0:100), j(0:100) ], "dem", "startx=0,starty=0,endx=100,endy=100,resx=0.1222,resy=0.15")
    private final String NON_GDAL_OPERATION_TEMPLATE = "$rasqlFormat($covExpression, $otherParams)";
}
