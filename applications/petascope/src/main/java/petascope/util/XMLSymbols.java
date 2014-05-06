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
package petascope.util;


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

    public static final String PREFIX_XML = "xml";
    public static final String PREFIX_XMLNS = "xmlns";
    public static final String PREFIX_GML = "gml";
    public static final String PREFIX_GMLCOV = "gmlcov";
    public static final String PREFIX_GMLRGRID = "gmlrgrid";
    public static final String PREFIX_SWE = "swe";
    public static final String PREFIX_OWS = "ows";
    public static final String PREFIX_WCS = "wcs";
    public static final String PREFIX_WSDL = "wsdl";
    public static final String PREFIX_XSI = "xsi";
    public static final String PREFIX_XLINK = "xlink";
    public static final String PREFIX_CRS = "crs";
    public static final String PREFIX_PROCESS_COVERAGE = "proc";
    public static final String PREFIX_INT = "int"; // wcs interpolation


    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Namespaces
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String NAMESPACE_XML = "http://www.w3.org/XML/1998/namespace";
    public static final String NAMESPACE_GML = "http://www.opengis.net/gml/3.2";
    public static final String NAMESPACE_GMLCOV = "http://www.opengis.net/gmlcov/1.0";
    public static final String NAMESPACE_GMLRGRID = "http://www.opengis.net/gml/3.3/rgrid";
    public static final String NAMESPACE_SWE = "http://www.opengis.net/swe/2.0";
    public static final String NAMESPACE_OWS = "http://www.opengis.net/ows/2.0";
    public static final String NAMESPACE_WCS_OLD = "http://www.opengis.net/wcs/1.1";
    public static final String NAMESPACE_WCS = "http://www.opengis.net/wcs/2.0";
    public static final String NAMESPACE_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static final String NAMESPACE_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String NAMESPACE_XLINK = "http://www.w3.org/1999/xlink";
    public static final String NAMESPACE_CRS = "http://www.opengis.net/wcs/service-extension/crs/1.0";
    public static final String NAMESPACE_PROCESS_COVERAGE = "http://www.opengis.net/wcs/processing/2.0";
    public static final String NAMESPACE_INTERPOLATION = "http://www.opengis.net/wcs/interpolation/1.0";


    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // XML element labels
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // wcs/gml
    public static final String LABEL_BBOX = "BoundingBox";
    public static final String LABEL_BOUNDEDBY = "BoundedBy";
    public static final String LABEL_CAPABILITIES = "Capabilities";
    public static final String LABEL_CONTENTS = "Contents";
    public static final String LABEL_COVERAGE_SUMMARY = "CoverageSummary";
    public static final String LABEL_COVERAGE_ID = "CoverageId";
    public static final String LABEL_COVERAGE_SUBTYPE = "CoverageSubtype";
    public static final String LABEL_COVERAGE_SUBTYPE_PARENT = "CoverageSubtypeParent";
    public static final String LABEL_COVERAGE_DESCRIPTIONS = "CoverageDescriptions";
    public static final String LABEL_COVERAGE_DESCRIPTION = "CoverageDescription";
    public static final String LABEL_COVERAGE_FUNCTION = "coverageFunction";
    public static final String LABEL_DATABLOCK = "DataBlock";
    public static final String LABEL_DIMENSION = "Dimension";
    public static final String LABEL_DIMENSION_TRIM = "DimensionTrim";
    public static final String LABEL_DIMENSION_SLICE = "DimensionSlice";
    public static final String LABEL_ENVELOPE = "Envelope";
    public static final String LABEL_FORMAT = "format";
    public static final String LABEL_MEDIATYPE = "mediaType";
    public static final String LABEL_PROCESSCOVERAGE_REQUEST = "ProcessCoveragesRequest";
    public static final String LABEL_RANGEPARAMETERS = "rangeParameters";
    public static final String LABEL_SERVICE_METADATA = "ServiceMetadata";
    public static final String LABEL_SLICE_POINT = "SlicePoint";
    public static final String LABEL_TRANSACTION = "Transaction";
    public static final String LABEL_TRIM_LOW = "TrimLow";
    public static final String LABEL_TRIM_HIGH = "TrimHigh";
    public static final String LABEL_TUPLELIST = "tupleList";
    public static final String LABEL_WGS84_BBOX = "Wgs84BoundingBox";
    public static final String LABEL_EXTENSION = "Extension";
    public static final String LABEL_FORMAT_SUPPORTED = "formatSupported";
    // wcs scaling extension
    public static final String LABEL_SCALING = "Scaling";
    public static final String LABEL_SCALEBYFACTOR = "ScaleByFactor";
    public static final String LABEL_SCALEAXESBYFACTOR = "ScaleAxesByFactor";
    public static final String LABEL_SCALETOSIZE = "ScaleToSize";
    public static final String LABEL_SCALETOEXTENT = "ScaleToExtent";
    public static final String LABEL_SCALEFACTOR = "scaleFactor";
    public static final String LABEL_AXIS = "axis";
    public static final String LABEL_TARGETSIZE = "targetSize";
    public static final String LABEL_LOW = "low";
    public static final String LABEL_HIGH = "high";
    // wcs range subsetting
    public static final String LABEL_RANGEITEM = "rangeItem";
    public static final String LABEL_RANGEINTERVAL = "rangeInterval";
    public static final String LABEL_STARTCOMPONENT = "startComponent";
    public static final String LABEL_ENDCOMPONENT = "endComponent";
    public static final String LABEL_RANGECOMPONENT = "rangeComponent";
    public static final String LABEL_RANGESUBSET = "rangeSubset";
    // processing coverage extension
    public static final String LABEL_PROCESSING_QUERY = "query";
    public static final String LABEL_PROCESSING_EXTRA_PARAMETER = "extraParameter";
    // wcs interpolation extension
    public static final String LABEL_GLOBAL_INTERPOLATION = "globalInterpolation";
    public static final String LABEL_INTERPOLATION = "Interpolation";
    public static final String LABEL_INTERPOLATION_METADATA = "InterpolationMetadata";
    public static final String LABEL_INTERPOLATION_SUPPORTED = "InterpolationSupported";

    // ows
    // // Service Identification
    public static final String LABEL_SERVICE_IDENTIFICATION = "ServiceIdentification";
    public static final String LABEL_TITLE = "Title";
    public static final String LABEL_ABSTRACT = "Abstract";
    public static final String LABEL_KEYWORDS = "Keywords";
    public static final String LABEL_KEYWORD = "Keyword";
    public static final String LABEL_TYPE = "Type";
    public static final String LABEL_SERVICE_TYPE = "ServiceType";
    public static final String LABEL_SERVICE_TYPE_VERSION = "ServiceTypeVersion";
    public static final String LABEL_FEES = "Fees";
    public static final String LABEL_ACCESS_CONSTRAINTS = "AccessConstraints";
    public static final String LABEL_PROFILE = "Profile";
    // // Service Provider
    public static final String LABEL_SERVICE_PROVIDER = "ServiceProvider";
    public static final String LABEL_PROVIDER_NAME = "ProviderName";
    public static final String LABEL_PROVIDER_SITE = "ProviderSite";
    // // // Service Contact
    public static final String LABEL_SERVICE_CONTACT = "ServiceContact";
    public static final String LABEL_INDIVIDUAL_NAME = "IndividualName";
    public static final String LABEL_POSITION_NAME = "PositionName";
    public static final String LABEL_ROLE = "Role";
    // // // // Contact Info
    public static final String LABEL_CONTACT_INFO = "ContactInfo";
    public static final String LABEL_PHONE = "Phone";
    public static final String LABEL_HOURS_OF_SERVICE = "HoursOfService";
    public static final String LABEL_CONTACT_INSTRUCTIONS = "ContactInstructions";
    // // // // // Address
    public static final String LABEL_ADDRESS = "Address";
    public static final String LABEL_DELIVERY_POINT = "DeliveryPoint";
    public static final String LABEL_CITY = "City";
    public static final String LABEL_ADMINISTRATIVE_AREA = "AdministrativeArea";
    public static final String LABEL_POSTAL_CODE = "PostalCode";
    public static final String LABEL_COUNTRY = "Country";
    public static final String LABEL_EMAIL_ADDRESS = "ElectronicMailAddress";
    // // OperationsMetadata
    public static final String LABEL_OPERATIONS_METADATA = "OperationsMetadata";
    public static final String LABEL_OPERATION = "Operation";
    public static final String LABEL_DCP = "DCP";
    public static final String LABEL_HTTP = "HTTP";
    public static final String LABEL_GET = "Get";
    public static final String LABEL_VERSION = "Version";
    public static final String LABEL_ACCEPT_VERSIONS = "AcceptVersions";
    public static final String LABEL_ACCEPT_FORMATS = "AcceptFormats";
    public static final String LABEL_ACCEPT_LANGUAGES = "AcceptLanguages";
    // // Contents
    public static final String LABEL_OWSMETADATA = "Metadata";

    // gmlcov
    public static final String LABEL_ABSTRACT_COVERAGE = "AbstractCoverage";
    public static final String LABEL_ABSTRACT_DISCRETE_COVERAGE = "AbstractDiscreteCoverage";
    public static final String LABEL_GRID_COVERAGE  = "GridCoverage";
    public static final String LABEL_GMLCOVMETADATA = "metadata";
    public static final String LABEL_MULTIPOINT_COVERAGE = "MultiPointCoverage";
    public static final String LABEL_MULTICURVE_COVERAGE = "GridCoverage";
    public static final String LABEL_MULTISURFACE_COVERAGE = "GridCoverage";
    public static final String LABEL_MULTISOLID_COVERAGE = "GridCoverage";
    public static final String LABEL_RECTIFIED_GRID_COVERAGE = "RectifiedGridCoverage";
    public static final String LABEL_REFERENCEABLE_GRID_COVERAGE = "ReferenceableGridCoverage";

    // swe
    public static final String LABEL_ALLOWED_VALUES = "AllowedValues";
    public static final String LABEL_CONSTRAINT = "constraint";
    public static final String LABEL_DESCRIPTION = "description";
    public static final String LABEL_FIELD = "field";
    public static final String LABEL_INTERVAL = "interval";
    public static final String LABEL_LABEL = "label";
    public static final String LABEL_FIELD_NAME = "name";
    public static final String LABEL_NILVALUE = "nilValue";
    public static final String LABEL_NILVALUES_ASSOCIATION_ROLE = "nilValues";
    public static final String LABEL_NILVALUES = "NilValues";
    public static final String LABEL_QUANTITY = "Quantity";
    public static final String LABEL_UOM = "uom";

    // gmlrgrid
    public static final String LABEL_COEFFICIENTS = "coefficients";
    public static final String LABEL_RGBA = "ReferenceableGridByArray";
    public static final String LABEL_RGBV = "ReferenceableGridByVectors";
    public static final String LABEL_RGBT = "ReferenceableGridByTransformation";

    // soap
    public static final String LABEL_BODY = "Body";

    // CRS definitions
    public static final String LABEL_CRSAXIS       = "axis";
    public static final String LABEL_CSAXIS        = "CoordinateSystemAxis";
    public static final String LABEL_AXISABBREV    = "axisAbbrev";
    public static final String LABEL_AXISDIRECTION = "axisDirection";
    public static final String LABEL_CSA           = "CoordinateSystemAxis";
    public static final String LABEL_NAME          = "name";
    public static final String LABEL_ORIGIN        = "origin";
    public static final String LABEL_TEMPORALCRS   = "TemporalCRS";
    // (suffixes: e.g. ProjectedCRS, TemporalCRS, etc.)
    public static final String CRS_GMLSUFFIX   = "CRS";
    public static final String CS_GMLSUFFIX    = "CS";
    public static final String DATUM_GMLSUFFIX = "Datum";

    // SECORE equality test
    public static final String LABEL_COMPARISON_RESULT = "comparisonResult";
    public static final String LABEL_EQUAL             = "equal";
    public static final String LABEL_EXCEPTION_TEXT    = "ExceptionText";

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Attributes
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ATT_SERVICE = "service";
    public static final String ATT_VERSION = "version";
    public static final String ATT_SCHEMA_LOCATION = "schemaLocation";
    public static final String ATT_CODESPACE = "codeSpace";
    public static final String ATT_LANG = "lang";
    public static final String ATT_HREF = "href";

    // gml:SRSReferenceGroup
    public static final String ATT_SRS_NAME = "srsName";
    public static final String ATT_SRS_DIMENSION = "srsDimension";
    public static final String ATT_UOM_LABELS = "uomLabels";
    public static final String ATT_AXIS_LABELS = "axisLabels";

    // bbox
    public static final String ATT_LOWERCORNER = "LowerCorner";
    public static final String ATT_UPPERCORNER = "UpperCorner";
    public static final String ATT_CRS = "crs";
    public static final String ATT_DIMENSIONS = "dimensions";

    // CRS definitions
    public static final String ATT_UOM = "uom";

    // swe
    public static final String ATT_DEFINITION = "definition";
    public static final String ATT_REASON = "reason";
    public static final String ATT_UOMCODE = "code";

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // XPath Contexts
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    XPathContext CTX_XML = new XPathContext(PREFIX_XML, NAMESPACE_XML);
    XPathContext CTX_WCS = new XPathContext(PREFIX_WCS, NAMESPACE_WCS);
    XPathContext CTX_CRS = new XPathContext(PREFIX_CRS, NAMESPACE_CRS);
    XPathContext CTX_PROCESS_COVERAGE = new XPathContext(PREFIX_PROCESS_COVERAGE, NAMESPACE_PROCESS_COVERAGE);

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Schema locations
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String SCHEMA_LOCATION_WCS = "http://schemas.opengis.net/wcs/2.0/wcsAll.xsd";
    public static final String SCHEMA_LOCATION_GML = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
    public static final String SCHEMA_LOCATION_GMLRGRID = "http://schemas.opengis.net/gml/3.3/referencableGrid.xsd";

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Predefined entities' names
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String PREDEFINED_ENTITY_AMPERSAND        = "amp";
    public static final String PREDEFINED_ENTITY_APOSTROPHE       = "apos";
    public static final String PREDEFINED_ENTITY_LESSTHAN_SIGN    = "lt";
    public static final String PREDEFINED_ENTITY_GREATERTHAN_SIGN = "gt";
    public static final String PREDEFINED_ENTITY_QUOTES           = "quot";
}
