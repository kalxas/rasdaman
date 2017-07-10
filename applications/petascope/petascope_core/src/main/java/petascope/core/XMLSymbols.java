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
 /*
 * JOMDoc - A Java library for OMDoc documents (http://omdoc.org/jomdoc).
 *
 * Original author    Normen Müller <n.mueller@jacobs-university.de>
 * Web                http://kwarc.info/nmueller/
 * Created            Oct 18, 2007
 * Filename           $Id: XMLSymbols.java 2188 2010-11-03 09:29:09Z dmisev $
 * Revision           $Revision: 2188 $
 *
 * Last modified on   $Date:2007-10-25 18:50:01 +0200 (Thu, 25 Oct 2007) $
 *               by   $Author:nmueller $
 *
 * Copyright (C) 2007,2008 the KWARC group (http://kwarc.info)
 * Licensed under the GNU  Public License v3 (GPL3).
 * For other licensing contact Michael Kohlhase <m.kohlhase@jacobs-university.de>
 */
package petascope.core;

import nu.xom.XPathContext;

/**
 * All recognized XML symbols, including prefixes, namespaces, element labels,
 * attribute names, XPath contexts, and other XML-related constants.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public interface XMLSymbols {

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Prefixes
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    String PREFIX_XML = "xml";
    String PREFIX_XMLNS = "xmlns";
    String PREFIX_GML = "gml";
    String PREFIX_GMLCOV = "gmlcov";
    String PREFIX_GMLRGRID = "gmlrgrid";
    String PREFIX_SWE = "swe";
    String PREFIX_OWS = "ows";
    String PREFIX_WCS = "wcs";
    String PREFIX_WSDL = "wsdl";
    String PREFIX_XSI = "xsi";
    String PREFIX_XLINK = "xlink";
    // Used in CRS-Extensions
    String PREFIX_WCS_CRS = "wcscrs";
    String PREFIX_PROCESS_COVERAGE = "proc";
    String PREFIX_INT = "int"; // wcs interpolation

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Namespaces
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    String NAMESPACE_XML = "http://www.w3.org/XML/1998/namespace";
    String NAMESPACE_GML = "http://www.opengis.net/gml/3.2";
    String NAMESPACE_GMLCOV = "http://www.opengis.net/gmlcov/1.0";
    String NAMESPACE_GMLRGRID = "http://www.opengis.net/gml/3.3/rgrid";
    String NAMESPACE_SWE = "http://www.opengis.net/swe/2.0";
    String NAMESPACE_OWS = "http://www.opengis.net/ows/2.0";
    String NAMESPACE_WCS_OLD = "http://www.opengis.net/wcs/1.1";
    String NAMESPACE_WCS = "http://www.opengis.net/wcs/2.0";
    String NAMESPACE_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    String NAMESPACE_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    String NAMESPACE_XLINK = "http://www.w3.org/1999/xlink";
    String NAMESPACE_WCS_CRS = "http://www.opengis.net/wcs/service-extension/crs/1.0";
    String NAMESPACE_PROCESS_COVERAGE = "http://www.opengis.net/wcs/processing/2.0";
    String NAMESPACE_INTERPOLATION = "http://www.opengis.net/wcs/interpolation/1.0";

    // XML requests WCS elements
    String LABEL_GET_CAPABILITIES = "GetCapabilities";
    String LABEL_DESCRIBE_COVERAGE = "DescribeCoverage";
    String LABEL_GET_COVERAGE = "GetCoverage";

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // XML element labels
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // wcs/gml    
    String LABEL_BOUNDEDBY = "boundedBy";
    String LABEL_CAPABILITIES = "Capabilities";
    String LABEL_COVERAGE_ID = "CoverageId";
    String LABEL_COVERAGE_SUBTYPE = "CoverageSubtype";
    String LABEL_COVERAGE_SUBTYPE_PARENT = "CoverageSubtypeParent";
    String LABEL_COVERAGE_DESCRIPTIONS = "CoverageDescriptions";
    String LABEL_COVERAGE_DESCRIPTION = "CoverageDescription";
    String LABEL_COVERAGE_FUNCTION = "coverageFunction";
    String LABEL_DATABLOCK = "DataBlock";
    String LABEL_DIMENSION = "Dimension";
    String LABEL_DIMENSION_TRIM = "DimensionTrim";
    String LABEL_DIMENSION_SLICE = "DimensionSlice";
    String LABEL_DOMAIN_SET = "domainSet";
    String LABEL_ENVELOPE = "Envelope";
    String LABEL_FILE = "File";
    String LABEL_FILE_REFERENCE = "fileReference";
    String LABEL_FILE_STRUCTURE = "fileStructure";
    String LABEL_FORMAT = "format";
    String LABEL_GRID_ENVELOPE = "GridEnvelope";
    String LABEL_LIMITS = "limits";
    String LABEL_LOWER_CORNER = "lowerCorner";
    String LABEL_UPPER_CORNER = "upperCorner";
    String LABEL_MEDIATYPE = "mediaType";
    String LABEL_PROCESSCOVERAGE_REQUEST = "ProcessCoveragesRequest";
    String LABEL_RANGEPARAMETERS = "rangeParameters";
    String LABEL_SLICE_POINT = "SlicePoint";
    String LABEL_SRS_DIMENSION = "srsDimension";
    String LABEL_TRANSACTION = "Transaction";
    String LABEL_TRIM_LOW = "TrimLow";
    String LABEL_TRIM_HIGH = "TrimHigh";
    String LABEL_TUPLELIST = "tupleList";
    String LABEL_WGS84_BBOX = "Wgs84BoundingBox";

    // wms gml
    String LABEL_WMS_SERVICE = "Service";
    String LABEL_WMS_NAME = "Name";
    String LABEL_WMS_TITLE = "Title";
    String LABEL_WMS_ABSTRACT = "Abstract";
    String LABEL_WMS_KEYWORD_LIST = "Abstract";
    String LABEL_WMS_ONLINE_RESOURCE = "OnlineResource";
    String LABEL_WMS_CONTACT_INFORMATION = "ContactInformation";
    String LABEL_WMS_CONTACT_PERSON_PRIMARY = "ContactPersonPrimary";
    String LABEL_WMS_CONTACT_PERSON = "ContactPerson";
    String LABEL_WMS_CONTACT_ORGANIZATION = "ContactOrganization";
    String LABEL_WMS_CONTACT_POSITION = "ContactPosition";
    String LABEL_WMS_CONTACT_ADDRESS = "ContactAddress";
    String LABEL_WMS_ADDRESS_TYPE = "AddessType";
    String VALUE_WMS_ADDRES_TYPE = "postal";
    String LABEL_WMS_ADDRESS = "Address";
    String LABEL_WMS_CITY = "City";
    String LABEL_WMS_STATE_OR_PROVINCE = "StateOrProvince";
    String LABEL_WMS_POST_CODE = "PostCode";
    String LABEL_WMS_COUNTRY = "Country";
    String LABEL_WMS_CONTACT_VOICE_TELEPHONE = "ContactVoiceTelephone";
    String LABEL_WMS_CONTACT_FACSIMILE_TELEPHONE = "ContactFacsimileTelephone";
    String LABEL_WMS_CONTACT_EMAIL = "ContactElectronicMailAddress";
    String LABEL_WMS_FEES = "Fees";
    String LABEL_WMS_ACCESS_CONSTRAINTS = "AccessConstraints";
    String LABEL_WMS_CAPABILITY = "Capability";
    String LABEL_WMS_REQUEST = "Request";
    String LABEL_WMS_GET_CAPABILITIES = "GetCapabilities";
    String LABEL_WMS_GET_MAP = "GetMap";
    String LABEL_WMS_EXCEPTION = "Exception";
    String LABEL_WMS_FORMAT = "Format";
    String LABEL_WMS_LAYER = "Layer";
    String LABEL_WMS_CRS = "CRS";
    String LABEL_WMS_EX_BBOX = "EX_GeographicBoundingBox";
    String LABEL_WMS_WEST_BOUND_LONGITUDE = "westBoundLongitude";
    String LABEL_WMS_EAST_BOUND_LONGITUDE = "eastBoundLongitude";
    String LABEL_WMS_SOUTH_BOUND_LATITUDE = "southBoundLatitude";
    String LABEL_WMS_NORTH_BOUND_LATITUDE = "northBoundLatitude";
    String LABEL_WMS_BOUNDING_BOX = "BoundingBox";
    String ATT_WMS_MIN_X = "minx";
    String ATT_WMS_MIN_Y = "miny";
    String ATT_WMS_MAX_X = "maxx";
    String ATT_WMS_MAX_Y = "maxy";
    String LABEL_WMS_DIMENSION = "Dimension";
    String ATT_WMS_NAME = "name";
    String ATT_WMS_UNITS = "units";
    String ATT_WMS_UNIT_SYMBOLS = "units";
    String ATT_WMS_DEFAULT = "default";
    String ATT_WMS_MULTIPLE_VALUES = "multipleValues";
    String ATT_WMS_NEAREST_VALUES = "nearestValues";
    String ATT_WMS_CURRENT = "current";
    String LABEL_WMS_STYLE = "Style";
    String LABEL_WMS_LEGEND_URL = "LegendURL";
    String ATT_WMS_WIDTH = "width";
    String ATT_WMS_HEIGHT = "height";    

    // wcs POST XML Extension
    String LABEL_EXTENSION = "Extension";

    // wcs crs-extension
    String LABEL_CRS_METADATA = "CrsMetadata";
    // this element is child of CrsMetadata
    String LABEL_CRS_SUPPORTED = "crsSupported";
    String LABEL_CRS = "CRS";

    String LABEL_GENERAL_GRID_AXIS = "generalGridAxis";
    String LABEL_GENERAL_GRID = "GeneralGridAxis";
    String LABEL_METADATA = "metadata";
    // wcs scaling extension
    String LABEL_SCALING = "Scaling";
    String LABEL_SCALEBYFACTOR = "ScaleByFactor";
    String LABEL_SCALEAXESBYFACTOR = "ScaleAxesByFactor";
    String LABEL_SCALETOSIZE = "ScaleToSize";
    String LABEL_SCALETOEXTENT = "ScaleToExtent";
    String LABEL_SCALEFACTOR = "scaleFactor";
    String LABEL_AXIS = "axis";
    String LABEL_TARGETSIZE = "targetSize";
    String LABEL_LOW = "low";
    String LABEL_HIGH = "high";
    // wcs range subsetting extension
    String LABEL_RANGE_SUBSET = "RangeSubset";
    String LABEL_RANGE_ITEM = "RangeItem";
    String LABEL_RANGE_COMPONENT = "RangeComponent";
    String LABEL_RANGE_INTERVAL = "RangeInterval";
    String LABEL_START_COMPONENT = "startComponent";
    String LABEL_END_COMPONENT = "endComponent";

    // wcs interpolation extension
    String LABEL_INTERPOLATION = "Interpolation";
    String LABEL_GLOBAL_INTERPOLATION = "globalInterpolation";

    // gml:rangeSet from wcst_import
    String LABEL_RANGESET = "rangeSet";

    // processing coverage extension
    String LABEL_WCPS_PROCESS_COVERAGES = "ProcessCoverages";
    // OGC WCPS POST abstract syntax
    String LABEL_WCPS_ABSTRACT_SYNTAX = "abstractSyntax";
    // OGC WCPS POST syntax
    String LABEL_WCPS_QUERY = "query";
    String LABEL_WCPS_EXTRA_PARAMETER = "extraParameter";
    // wcs interpolation extension
    String INT_LABEL_INTERPOLATION_METADATA = "int:InterpolationMetadata";
    String INT_LABEL_INTERPOLATION_SUPPORTED = "int:InterpolationSupported";

    // ows
    // // Service Identification
    String OWS_LABEL_SERVICE_IDENTIFICATION = "ows:ServiceIdentification";
    String OWS_LABEL_TITLE = "ows:Title";
    String OWS_LABEL_ABSTRACT = "ows:Abstract";
    String OWS_LABEL_KEYWORDS = "ows:Keywords";
    String OWS_LABEL_KEYWORD = "ows:Keyword";
    String OWS_LABEL_TYPE = "ows:Type";
    String OWS_LABEL_SERVICE_TYPE = "ows:ServiceType";
    String OWS_LABEL_SERVICE_TYPE_VERSION = "ows:ServiceTypeVersion";
    String OWS_LABEL_FEES = "ows:Fees";
    String OWS_LABEL_ACCESS_CONSTRAINTS = "ows:AccessConstraints";
    String OWS_LABEL_PROFILE = "ows:Profile";
    // // Service Provider
    String OWS_LABEL_SERVICE_PROVIDER = "ows:ServiceProvider";
    String OWS_LABEL_PROVIDER_NAME = "ows:ProviderName";
    String OWS_LABEL_PROVIDER_SITE = "ows:ProviderSite";
    // // // Service Contact
    String OWS_LABEL_SERVICE_CONTACT = "ows:ServiceContact";
    String OWS_LABEL_INDIVIDUAL_NAME = "ows:IndividualName";
    String OWS_LABEL_POSITION_NAME = "ows:PositionName";
    String OWS_LABEL_ROLE = "ows:Role";
    // // // // Contact Info
    String OWS_LABEL_CONTACT_INFO = "ows:ContactInfo";
    String OWS_LABEL_HOURS_OF_SERVICE = "ows:HoursOfService";
    String OWS_LABEL_CONTACT_INSTRUCTIONS = "ows:ContactInstructions";
    String OWS_LABEL_ONLINE_RESOURCE = "ows:OnlineResource";
    // // // // // Address
    String OWS_LABEL_ADDRESS = "ows:Address";
    String OWS_LABEL_DELIVERY_POINT = "ows:DeliveryPoint";
    String OWS_LABEL_CITY = "ows:City";
    String OWS_LABEL_ADMINISTRATIVE_AREA = "ows:AdministrativeArea";
    String OWS_LABEL_POSTAL_CODE = "ows:PostalCode";
    String OWS_LABEL_COUNTRY = "ows:Country";
    String OWS_LABEL_EMAIL_ADDRESS = "ows:ElectronicMailAddress";
    // // // // // Phone
    String OWS_LABEL_PHONE = "ows:Phone";
    String OWS_LABEL_VOICE = "ows:Voice";
    String OWS_LABEL_FACSIMILE = "ows:Facsimile";

    // // OperationsMetadata
    String OWS_LABEL_OPERATIONS_METADATA = "ows:OperationsMetadata";
    String OWS_LABEL_OPERATION = "ows:Operation";
    String OWS_LABEL_DCP = "ows:DCP";
    String OWS_LABEL_HTTP = "ows:HTTP";
    String OWS_LABEL_GET = "ows:Get";
    String OWS_LABEL_VERSION = "Version";
    String OWS_LABEL_ACCEPT_VERSIONS = "AcceptVersions";
    String OWS_LABEL_ACCEPT_FORMATS = "AcceptFormats";
    String OWS_LABEL_ACCEPT_LANGUAGES = "AcceptLanguages";

    // // ServiceMetadata
    String WCS_LABEL_SERVICE_METADATA = "wcs:ServiceMetadata";
    String WCS_LABEL_FORMAT_SUPPORTED = "wcs:formatSupported";
    // wcs Extention
    String WCS_LABEL_EXTENSION = "wcs:Extension";
    // // Contents
    String WCS_LABEL_CONTENTS = "wcs:Contents";
    String WCS_LABEL_COVERAGE_SUMMARY = "wcs:CoverageSummary";
    String OWS_LABEL_BOUNDING_BOX = "ows:BoundingBox";
    String OWS_LABEL_LOWER_CORNER = "ows:LowerCorner";
    String OWS_LABEL_UPPER_CORNER = "ows:UpperCorner";

    // gmlcov
    String LABEL_ABSTRACT_COVERAGE = "Coverage";
    String LABEL_ABSTRACT_DISCRETE_COVERAGE = "AbstractDiscreteCoverage";
    String LABEL_GRID_COVERAGE = "GridCoverage";
    String LABEL_GMLCOVMETADATA = "metadata";
    // Coverage's metadata of DescribeCoverage, GetCovearge must be put inside this element
    String LABEL_COVERAGE_METADATA = "covMetadata";
    String LABEL_GMLCOVMETADATA_EXTENSION = "Extension";
    String LABEL_MULTIPOINT_COVERAGE = "MultiPointCoverage";
    String LABEL_MULTICURVE_COVERAGE = "GridCoverage";
    String LABEL_MULTISURFACE_COVERAGE = "GridCoverage";
    String LABEL_MULTISOLID_COVERAGE = "GridCoverage";
    String LABEL_RANGE_TYPE = "rangeType";
    String LABEL_RECTIFIED_GRID = "RectifiedGrid";
    String LABEL_GRID = "Grid";
    String LABEL_RECTIFIED_GRID_COVERAGE = "RectifiedGridCoverage";
    String LABEL_REFERENCEABLE_GRID_COVERAGE = "ReferenceableGridCoverage";

    // swe    
    String LABEL_DATA_RECORD = "DataRecord";
    String LABEL_DESCRIPTION = "description";
    String LABEL_FIELD = "field";
    String LABEL_LABEL = "label";
    String LABEL_FIELD_NAME = "name";
    String LABEL_NILVALUE = "nilValue";
    String LABEL_NILVALUES_ASSOCIATION_ROLE = "nilValues";
    String LABEL_NILVALUES = "NilValues";
    String LABEL_QUANTITY = "Quantity";
    String LABEL_UOM = "uom";

    // contrain with AllowedValues
    String LABEL_CONSTRAINT = "constraint";
    String LABEL_ALLOWED_VALUES = "AllowedValues";
    String LABEL_INTERVAL = "interval";
    String LABEL_VALUE = "value";

    // gmlrgrid
    String LABEL_COEFFICIENTS = "coefficients";
    String LABEL_RGBA = "ReferenceableGridByArray";
    String LABEL_RGBV = "ReferenceableGridByVectors";
    String LABEL_RGBT = "ReferenceableGridByTransformation";

    // soap
    String LABEL_BODY = "Body";

    // CRS definitions
    String LABEL_CRSAXIS = "axis";
    String LABEL_CSAXIS = "CoordinateSystemAxis";
    String LABEL_AXISABBREV = "axisAbbrev";
    String LABEL_AXISDIRECTION = "axisDirection";
    String LABEL_CSA = "CoordinateSystemAxis";
    String LABEL_NAME = "name";
    String LABEL_OFFSET_VECTOR = "offsetVector";
    String LABEL_ORIGIN = "origin";
    String LABEL_POINT = "Point";
    String LABEL_POS = "pos";
    String LABEL_TEMPORALCRS = "TemporalCRS";
    // (suffixes: e.g. ProjectedCRS, TemporalCRS, etc.)
    String CRS_GMLSUFFIX = "CRS";
    String CS_GMLSUFFIX = "CS";
    String DATUM_GMLSUFFIX = "Datum";

    // SECORE equality test
    String LABEL_COMPARISON_RESULT = "comparisonResult";
    String LABEL_EQUAL = "equal";
    String LABEL_EXCEPTION_TEXT = "ExceptionText";

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Attributes
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    String ATT_SERVICE = "service";
    String ATT_VERSION = "version";
    String ATT_SCHEMA_LOCATION = "schemaLocation";
    String ATT_CODESPACE = "codeSpace";
    String ATT_LANG = "lang";
    String ATT_HREF = "href";

    String ATT_ID = "id";

    // gml:SRSReferenceGroup
    String ATT_SRS_NAME = "srsName";
    String ATT_SRS_DIMENSION = "srsDimension";
    String ATT_UOM_LABELS = "uomLabels";
    String ATT_AXIS_LABELS = "axisLabels";

    // crs-extension
    String ATT_SUPPORTED_CRS = "supportedCrs";
    String ATT_SUBSET_CRS = "subsettingCrs";
    String ATT_OUTPUT_CRS = "outputCrs";

    // bbox    
    String ATT_CRS = "crs";
    String ATT_DIMENSIONS = "dimensions";

    // CRS definitions
    String ATT_UOM = "uom";
    String ATT_NAME = "name";

    // swe
    String ATT_DEFINITION = "definition";
    String ATT_REASON = "reason";
    String ATT_UOMCODE = "code";

    String ATT_CS = "cs";
    String ATT_TS = "ts";

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // XPath Contexts
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    XPathContext CTX_XML = new XPathContext(PREFIX_XML, NAMESPACE_XML);
    XPathContext CTX_WCS = new XPathContext(PREFIX_WCS, NAMESPACE_WCS);
    XPathContext CTX_CRS = new XPathContext(PREFIX_WCS_CRS, NAMESPACE_WCS_CRS);
    XPathContext CTX_PROCESS_COVERAGE = new XPathContext(PREFIX_PROCESS_COVERAGE, NAMESPACE_PROCESS_COVERAGE);

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Schema locations
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    String SCHEMA_LOCATION_WCS = "http://schemas.opengis.net/wcs/2.0/wcsAll.xsd";
    String SCHEMA_LOCATION_GML = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
    String SCHEMA_LOCATION_GMLRGRID = "http://schemas.opengis.net/gml/3.3/referencableGrid.xsd";

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Predefined entities' names
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    String PREDEFINED_ENTITY_AMPERSAND = "amp";
    String PREDEFINED_ENTITY_APOSTROPHE = "apos";
    String PREDEFINED_ENTITY_LESSTHAN_SIGN = "lt";
    String PREDEFINED_ENTITY_GREATERTHAN_SIGN = "gt";
    String PREDEFINED_ENTITY_QUOTES = "quot";
}
