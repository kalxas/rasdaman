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
package petascope.wcps2.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.NilValue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps2.parameters.model.netcdf.NetCDFExtraParams;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.result.WcpsResult;
import petascope.util.JsonUtil;
import petascope.util.MIMEUtil;
import petascope.wcps2.encodeparameters.model.GeoReference;
import petascope.wcps2.encodeparameters.service.GeoReferenceService;
import petascope.wcps2.encodeparameters.service.SerializationEncodingService;
import petascope.wcps2.exception.processing.EncodingCoverageMetadataIsNullException;
import petascope.wcps2.exception.processing.InvalidJsonDeserializationException;
import petascope.wcps2.exception.processing.InvalidNumberOfNodataValuesException;
import petascope.wcps2.exception.processing.MetadataSerializationException;
import petascope.wcps2.metadata.model.RangeField;
import petascope.wcps2.parameters.netcdf.service.NetCDFParametersService;

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
public class EncodeCoverageHandler {

    private org.slf4j.Logger log = LoggerFactory.getLogger(EncodeCoverageHandler.class);

    @Autowired
    private SerializationEncodingService serializationEncodingService;
    @Autowired
    private NetCDFParametersService netCDFParametersFactory;

    public WcpsResult handle(WcpsResult coverageExpression, String format, String extraParams) throws PetascopeException, JsonProcessingException {
        //strip first part of the mime type because rasdaman encode function does not support mime types yet
        //e.g. image/png -> png; text/csv -> csv and so on.
        format = format.contains("/") ? format.split("/")[1] : format;
        format = format.replace("\"", "");

        // then get the Gdal code according to the extracted format (e.g: tiff -> GTiff).
        // Migrate:
        String rasqlFormat = null;
        //String rasqlFormat = coverageRegistry.getMetadataSource().formatToGdalid(format);

        if (rasqlFormat == null) {
            // NOTE: csv, dem does not exist in GDAL code so use the input format type directly.
            rasqlFormat = format;
        }

        // get the mime-type before modifying the rasqlFormat
        String mimeType = MIMEUtil.getMimeType(format);

        // NOTE: must use JP2OpenJPEG to encode with geo-reference metadata for JPEG2000 (JP2)
        if (rasqlFormat.equalsIgnoreCase(MIMEUtil.FORMAT_ID_JP2)) {
            rasqlFormat = MIMEUtil.FORMAT_ID_OPENJP2;
        } else if (rasqlFormat.equalsIgnoreCase(MIMEUtil.ENCODE_GML)) {
            // NOTE: We need the values from JSON encoding of a coverage (http://rasdaman.org/ticket/1578)
            // to add in the tupleLists element of output in application/gml+xml            
            rasqlFormat = MIMEUtil.ENCODE_JSON;
        }

        // NOTE: we have 2 cases for extra params:
        // + In old style: for c in (test_eobstest) return encode(c, "netcdf", "nodata=0"), then extra params will need to be built in JSON format
        // + In json style: for c in (test_eobstest) return encode(c, "netcdf", "{\"dimensions\":....}"), then
        //   check if crs and bbox already existed in the JSON extra params, if it does then will not do anything, otherwise, add these param keys-values to this extra param input
        //   then pass it in JSON string as rasql's encode extra parameters
        String otherParamsString = null;
        try {
            otherParamsString = getExtraParams(coverageExpression, rasqlFormat, extraParams);
        } catch (IOException ex) {
            log.debug("Failed get extra params: ", ex);
            throw new MetadataSerializationException();
        }

        //get the right template for rasql string (the dem() encode still use the old format, other will use the new JSON format)
        String template = getTemplate(rasqlFormat);
        String resultRasql = template.replace("$covExpression", coverageExpression.getRasql())
                .replace("$format", '"' + rasqlFormat + '"')
                .replace("$otherParams", otherParamsString);
        WcpsResult wcpsResult = new WcpsResult(coverageExpression.getMetadata(), resultRasql);
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
            String extraParams) throws PetascopeException, JsonProcessingException, IOException {
        String otherParamsString = "";
        NetCDFExtraParams netCDFExtraParams = null;
        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        // set parameters for dem
        if (rasqlFormat.equalsIgnoreCase(MIMEUtil.ENCODE_DEM)) {
            // keep the arguments without need to calculate anything else
            otherParamsString = "\"" + extraParams + "\"";
            return otherParamsString;
        } else if (!addDefaultParams(rasqlFormat)) {
            // e.g: csv, json no add default params            
            return otherParamsString;
        } else if (rasqlFormat.equalsIgnoreCase(MIMEUtil.ENCODE_GML)) {
            otherParamsString = "";
            return otherParamsString;
        } else if (rasqlFormat.equalsIgnoreCase(MIMEUtil.ENCODE_NETCDF)) {
            // netcdf (we build some netCDF parameters separately)            
            netCDFExtraParams = netCDFParametersFactory.buildParameters(coverageExpression.getMetadata());

        }

        // encode number or string which returns metadata is null in non csv/json will be invalid request
        // e.g: encode(2, "png")
        if (metadata == null) {
            throw new EncodingCoverageMetadataIsNullException();
        }

        // this is the most imporatant parameter which need to be built from coverage metadata
        GeoReferenceService geoReferenceService = new GeoReferenceService();
        GeoReference geoReference = geoReferenceService.buildGeoReference(metadata);

        String jsonOutput = "";

        // Check if extra params is in old style or new JSON style
        if (JsonUtil.isJsonValid(extraParams)) {
            // extra params is new JSON style
            jsonOutput = serializationEncodingService.serializeExtraParamsToJson(rasqlFormat, extraParams,
                    metadata, netCDFExtraParams, geoReference);
        } else if (extraParams.contains("{") || extraParams.contains("}")) {
            // it is invalid JSON format and not old style (e.g: "nodata=0")
            log.error("Extra parameters string: " + extraParams + " is not valid JSON format.");
            throw new InvalidJsonDeserializationException();
        } else {
            // extra params is old style (check if it has "nodata" as parameter to add to metadata)
            parseNoDataFromExtraParams(extraParams, metadata);
            jsonOutput = serializationEncodingService.serializeExtraParamsToJson(rasqlFormat, metadata, netCDFExtraParams, geoReference);
        }

        // as all of the parameters go inside the new JSON style, so replace "{" to "{\""
        otherParamsString = ", \"" + jsonOutput.replace("\"", "\\\"") + "\"";

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
    private void parseNoDataFromExtraParams(String extraParams, WcpsCoverageMetadata metadata) {
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
        }
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
