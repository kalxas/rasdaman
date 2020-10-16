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
package petascope.core;

/**
 * All recognized keys for KVP requests
 *
 * @author <a href="mailto:m.rusu@jacobs-university.de">Mihaela Rusu</a>
 */
public interface KVPSymbols {

    String KEY_ACCEPTFORMATS = "acceptformats";
    String KEY_ACCEPTLANGUAGES = "acceptlanguages";
    String KEY_ACCEPTVERSIONS = "acceptversions";
    String KEY_COVERAGEID = "coverageId";
    String KEY_FORMAT = "format";
    String KEY_INTERPOLATION = "interpolation";
    String KEY_MEDIATYPE = "mediatype";
    String KEY_RANGESUBSET = "rangesubset";
    String KEY_REQUEST = "request";
    // Used to transform CIS 1.0 coverages to CIS 1.1 if output Type is GML in DescribeCoverage/GetCoverage requests.
    String KEY_OUTPUT_TYPE = "outputType";
    String KEY_SCALE_PREFIX = "scale";
    String KEY_SCALEAXES = "scaleaxes";
    String KEY_SCALEEXTENT = "scaleextent";
    String KEY_SCALEFACTOR = "scalefactor";
    String KEY_SCALESIZE = "scalesize";
    String KEY_SERVICE = "service";
    String KEY_SUBSET = "subset";
    String KEY_VERSION = "version";
    String KEY_PROCESS_COVERAGES = "ProcessCoverages";
    String VERSIONS_SEP = ",";

    // NOTE: According to 06-121r9_OGC (7.3.3) Sections parameter can only have these values (ServiceIdentification, ServiceProvider, OperationsMetadata, Contents, Languages, All)    
    // Also, this is an optional parameter and we can just check if the value of "sections" parameter is valid then return all XML elements as a normal GetCapabilitie request.
    // "Server implementation of the Sections parameter is optional. When a server does not
    // implement this Sections parameter, it shall ignore this parameter if present in a
    // GetCapabilities operation request, and shall return the complete service metadata document".
    String KEY_SECTIONS = "sections";
    String VALUE_SECTIONS_SERVICE_IDENTIFICATION = "ServiceIdentification";
    String VALUE_SECTIONS_SERVICE_PROVIDER = "ServiceProvider";
    String VALUE_SECTIONS_OPERATIONS_METADATA = "OperationsMetadata";
    String VALUE_SECTIONS_CONTENTS = "Contents";
    String VALUE_SECTIONS_LANGUAGES = "Languages";
    String VALUE_SECTIONS_ALL = "All";

    String KEY_OUTPUT_CRS = "outputCrs";
    String KEY_SUBSETTING_CRS = "subsettingCrs";
    // clip coverage with WKT (e.g: POLYGON, LineString,...)
    String KEY_CLIP = "clip";
    
    String VALUE_TIME_PATTERN_CHARACTER = "\"";
    
    // WCS
    String VALUE_GET_CAPABILITIES = "GetCapabilities";
    String VALUE_DESCRIBE_COVERAGE = "DescribeCoverage";
    String VALUE_GET_COVERAGE = "GetCoverage";    
    // it is the name of file parameter to be posted to server
    String KEY_UPLOADED_FILE_VALUE = "file";
    String VALUE_GENERAL_GRID_COVERAGE = "GeneralGridCoverage";

    // WCST
    String VALUE_INSERT_COVERAGE = "InsertCoverage";
    String VALUE_DELETE_COVERAGE = "DeleteCoverage";
    String VALUE_UPDATE_COVERAGE = "UpdateCoverage";
    String VALUE_INSERT_SCALE_LEVEL = "InsertScaleLevel";
    String VALUE_DELETE_SCALE_LEVEL = "DeleteScaleLevel";

    String KEY_USE_ID = "useId";
    String KEY_COVERAGE_REF = "coverageRef";
    String KEY_COVERAGE = "coverage";
    String KEY_INPUT_COVERAGE = "inputCoverage";
    String KEY_INPUT_COVERAGE_REF = "inputCoverageRef";
    String KEY_MASK_GRID = "maskgrid";
    String KEY_MASK_GRID_REF = "maskgridref";
    String KEY_PIXEL_DATA_TYPE = "pixelDataType";
    String KEY_TILING = "tiling";
    String KEY_LEVEL = "level";
    
    // WCPS
    String VALUE_PROCESS_COVERAGES = "ProcessCoverages";
    String KEY_QUERY = "query";

    // WCS XML
    // Made up for handling POST XML request
    String VALUE_REQUEST_WCS_XML = "wcsXML";
    // Made up for handling POST SOAP request
    String VALUE_REQUEST_WCS_SOAP = "wcsSOAP";
    String KEY_REQUEST_BODY = "requestBody";

    // rasql KVP
    String KEY_PASSWORD = "password";
    String KEY_USERNAME = "username";

    // WMS
    // Not standard request (only Rasdaman supports)    
    String VALUE_WMS_INSERT_WCS_LAYER = "InsertWCSLayer";
    String VALUE_WMS_UPDATE_WCS_LAYER = "UpdateWCSLayer";
    // Delete a WMS layer but not delete the associated WCS coverage
    String VALUE_WMS_DELETE_LAYER = "DeleteLayer";
    String VALUE_WMS_DESCRIBE_LAYER = "DescribeLayer";
    String VALUE_WMS_INSERT_STYLE = "InsertStyle";
    String VALUE_WMS_UPDATE_STYLE = "UpdateStyle";
    String VALUE_WMS_DELETE_STYLE = "DeleteStyle";
    String VALUE_WMS_GET_MAP = "GetMap";
    String KEY_WMS_LAYER = "layer";
    String KEY_WMS_LAYER_NAME = "layerName";
    String KEY_WMS_STYLE = "style";
    String KEY_WMS_NAME = "name";
    String KEY_WMS_TITLE = "title";
    String KEY_WMS_ABSTRACT = "abstract";
    String KEY_WMS_RASQL_TRANSFORM_FRAGMENT = "rasqlTransformFragment";
    String KEY_WMS_WCPS_QUERY_FRAGMENT = "wcpsQueryFragment";
    String KEY_WMS_WCSCOVERAGEID = "wcsCoverageId";
    String KEY_WMS_LAYERS = "layers";
    String KEY_WMS_STYLES = "styles";
    String KEY_WMS_CRS = "crs";
    String KEY_WMS_BBOX = "bbox";
    String KEY_WMS_WIDTH = "width";
    String KEY_WMS_HEIGHT = "height";
    String KEY_WMS_FORMAT = "format";
    String KEY_WMS_TRANSPARENT = "transparent";
    String KEY_WMS_BGCOLOR = "bgcolor";
    String KEY_WMS_COLOR_TABLE_TYPE = "ColorTableType";
    String KEY_WMS_COLOR_TABLE_DEFINITION = "ColorTableDefinition";

    // used for non XY axes, time axis, elevation axis, (e.g: dim_pressure with axis name is pressure) 
    String KEY_WMS_DIM_PREFIX = "dim_";
    String KEY_WMS_EXCEPTIONS = "exceptions";
    String VALUE_WMS_EXCEPTIONS_XML = "xml";
    String VALUE_WMS_EXCEPTIONS_INIMAGE = "inimage";
    String VALUE_WMS_EXCEPTIONS_BLANK = "blank";
    String KEY_WMS_TIME = "time";
    String KEY_WMS_ELEVATION = "elevation";
    String KEY_WMS_INTERPOLATION = "interpolation";
    // separate between min/max values for dimension subsets (e.g: time=min/max)
    String VALUE_WMS_DIMENSION_MIN_MAX_SEPARATE_CHARACTER = "/";
    // e.g: elevation=20,30,50
    String VALUE_WMS_SUBSET_SEPARATE_CHARACTER = ",";

    /**
     * Service name
     */
    String CIS_SERVICE = "CIS";
    String WCS_SERVICE = "WCS";
    String WCST_SERVICE = "WCST";
    String WMS_SERVICE = "WMS";
    String WCPS_SERVICE = "WCPS";
    // Make up for RASQL servlet
    String RASQL_SERVICE = "RASQL";

    /**
     * WCS version, fixed to 2.0.x
     */
    String WCS_VERSION_PATTERN = "2\\.0\\.\\d+";
    
    /**
     * WCPS version, fixed to 1.0.x
     */
    String WCPS_VERSION_PATTERN = "1\\.0\\.\\d+";

    // WCS mutlipart
    String VALUE_MULTIPART_RELATED = "multipart/related";

    // OWS metadata (ServiceIdentification and ServiceProvider) in GetCapabilities response
    String KEY_OWS_METADATA_SERVICE_TITLE = "serviceTitle";
    String KEY_OWS_METADATA_ABSTRACT = "abstract";
    
    String KEY_OWS_METADATA_PROVIDER_NAME = "providerName";
    String KEY_OWS_METADATA_PROVIDER_SITE = "providerSite";
    String KEY_OWS_METADATA_INDIVIDUAL_NAME = "individualName";
    String KEY_OWS_METADATA_POSITION_NAME = "positionName";
    String KEY_OWS_METADATA_ROLE = "role";    
    String KEY_OWS_METADATA_EMAIL = "email";
    String KEY_OWS_METADATA_VOICE_PHONE = "voicePhone";
    String KEY_OWS_METADATA_FACSIMILE_PHONE = "facsimilePhone";
    String KEY_OWS_METADATA_HOURS_OF_SERVICE = "hoursOfService";
    String KEY_OWS_METADATA_CONTACT_INSTRUCTIONS = "contactInstructions";
    String KEY_OWS_METADATA_CITY = "city";
    String KEY_OWS_METADATA_ADMINISTRATIVE_AREA = "administrativeArea";
    String KEY_OWS_METADATA_POSTAL_CODE = "postalCode";
    String KEY_OWS_METADATA_COUNTRY = "country";
    
    // Update coverage's id by new id
    String KEY_COVERAGE_ID = "coverageId";
    String KEY_NEW_ID = "newId";
    
}
