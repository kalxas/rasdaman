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
    String PREFIX_CRS = "crs";
    String PREFIX_PROCESS_COVERAGE = "proc";


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
    String NAMESPACE_CRS = "http://www.opengis.net/wcs/service-extension/crs/1.0";
    String NAMESPACE_PROCESS_COVERAGE = "http://www.opengis.net/wcs/processing/2.0";


    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // XML element labels
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // wcs/gml
    String LABEL_BBOX = "BoundingBox";
    String LABEL_BOUNDEDBY = "BoundedBy";
    String LABEL_CAPABILITIES = "Capabilities";
    String LABEL_CONTENTS = "Contents";
    String LABEL_COVERAGE_SUMMARY = "CoverageSummary";
    String LABEL_COVERAGE_ID = "CoverageId";
    String LABEL_COVERAGE_SUBTYPE = "CoverageSubtype";
    String LABEL_COVERAGE_SUBTYPE_PARENT = "CoverageSubtypeParent";
    String LABEL_COVERAGE_DESCRIPTIONS = "CoverageDescriptions";
    String LABEL_COVERAGE_DESCRIPTION = "CoverageDescription";
    String LABEL_DATABLOCK = "DataBlock";
    String LABEL_DIMENSION = "Dimension";
    String LABEL_DIMENSION_TRIM = "DimensionTrim";
    String LABEL_DIMENSION_SLICE = "DimensionSlice";
    String LABEL_ENVELOPE = "Envelope";
    String LABEL_FORMAT = "format";
    String LABEL_MEDIATYPE = "mediaType";
    String LABEL_PROCESSCOVERAGE_REQUEST = "ProcessCoveragesRequest";
    String LABEL_RANGEPARAMETERS = "rangeParameters";
    String LABEL_SERVICE_METADATA = "ServiceMetadata";
    String LABEL_SLICE_POINT = "SlicePoint";
    String LABEL_TRANSACTION = "Transaction";
    String LABEL_TRIM_LOW = "TrimLow";
    String LABEL_TRIM_HIGH = "TrimHigh";
    String LABEL_TUPLELIST = "tupleList";
    String LABEL_WGS84_BBOX = "Wgs84BoundingBox";
    String LABEL_EXTENSION = "Extension";
    String LABEL_FORMAT_SUPPORTED = "formatSupported";
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
    //wcs range subsetting
    String LABEL_RANGEITEM = "rangeItem";
    String LABEL_RANGEINTERVAL = "rangeInterval";
    String LABEL_STARTCOMPONENT = "startComponent";
    String LABEL_ENDCOMPONENT = "endComponent";
    String LABEL_RANGECOMPONENT = "rangeComponent";
    String LABEL_RANGESUBSET = "rangeSubset";
    //processing coverage extension
    String LABEL_PROCESSING_QUERY = "query";
    String LABEL_PROCESSING_EXTRA_PARAMETER = "extraParameter";

    // ows
    // // Service Identification
    String LABEL_SERVICE_IDENTIFICATION = "ServiceIdentification";
    String LABEL_TITLE = "Title";
    String LABEL_ABSTRACT = "Abstract";
    String LABEL_KEYWORDS = "Keywords";
    String LABEL_KEYWORD = "Keyword";
    String LABEL_TYPE = "Type";
    String LABEL_SERVICE_TYPE = "ServiceType";
    String LABEL_SERVICE_TYPE_VERSION = "ServiceTypeVersion";
    String LABEL_FEES = "Fees";
    String LABEL_ACCESS_CONSTRAINTS = "AccessConstraints";
    String LABEL_PROFILE = "Profile";
    // // Service Provider
    String LABEL_SERVICE_PROVIDER = "ServiceProvider";
    String LABEL_PROVIDER_NAME = "ProviderName";
    String LABEL_PROVIDER_SITE = "ProviderSite";
    // // // Service Contact
    String LABEL_SERVICE_CONTACT = "ServiceContact";
    String LABEL_INDIVIDUAL_NAME = "IndividualName";
    String LABEL_POSITION_NAME = "PositionName";
    String LABEL_ROLE = "Role";
    // // // // Contact Info
    String LABEL_CONTACT_INFO = "ContactInfo";
    String LABEL_PHONE = "Phone";
    String LABEL_HOURS_OF_SERVICE = "HoursOfService";
    String LABEL_CONTACT_INSTRUCTIONS = "ContactInstructions";
    // // // // // Address
    String LABEL_ADDRESS = "Address";
    String LABEL_DELIVERY_POINT = "DeliveryPoint";
    String LABEL_CITY = "City";
    String LABEL_ADMINISTRATIVE_AREA = "AdministrativeArea";
    String LABEL_POSTAL_CODE = "PostalCode";
    String LABEL_COUNTRY = "Country";
    String LABEL_EMAIL_ADDRESS = "ElectronicMailAddress";
    // // OperationsMetadata
    String LABEL_OPERATIONS_METADATA = "OperationsMetadata";
    String LABEL_OPERATION = "Operation";
    String LABEL_DCP = "DCP";
    String LABEL_HTTP = "HTTP";
    String LABEL_GET = "Get";
    String LABEL_VERSION = "Version";
    String LABEL_ACCEPT_VERSIONS = "AcceptVersions";
    String LABEL_ACCEPT_FORMATS = "AcceptFormats";
    String LABEL_ACCEPT_LANGUAGES = "AcceptLanguages";
    // // Contents
    String LABEL_OWSMETADATA = "Metadata";

    // gmlcov
    String LABEL_ABSTRACT_COVERAGE = "AbstractCoverage";
    String LABEL_ABSTRACT_DISCRETE_COVERAGE = "AbstractDiscreteCoverage";
    String LABEL_GRID_COVERAGE  = "GridCoverage";
    String LABEL_GMLCOVMETADATA = "metadata";
    String LABEL_MULTIPOINT_COVERAGE = "MultiPointCoverage";
    String LABEL_MULTICURVE_COVERAGE = "GridCoverage";
    String LABEL_MULTISURFACE_COVERAGE = "GridCoverage";
    String LABEL_MULTISOLID_COVERAGE = "GridCoverage";
    String LABEL_RECTIFIED_GRID_COVERAGE = "RectifiedGridCoverage";
    String LABEL_REFERENCEABLE_GRID_COVERAGE = "ReferenceableGridCoverage";

    // swe
    String LABEL_ALLOWED_VALUES = "AllowedValues";
    String LABEL_CONSTRAINT = "constraint";
    String LABEL_DESCRIPTION = "description";
    String LABEL_FIELD = "field";
    String LABEL_INTERVAL = "interval";
    String LABEL_LABEL = "label";
    String LABEL_FIELD_NAME = "name";
    String LABEL_NILVALUE = "nilValue";
    String LABEL_NILVALUES_ASSOCIATION_ROLE = "nilValues";
    String LABEL_NILVALUES = "NilValues";
    String LABEL_QUANTITY = "Quantity";
    String LABEL_UOM = "uom";

    // gmlrgrid
    String LABEL_COEFFICIENTS = "coefficients";
    String LABEL_RGBA = "ReferenceableGridByArray";
    String LABEL_RGBV = "ReferenceableGridByVectors";
    String LABEL_RGBT = "ReferenceableGridByTransformation";

    // soap
    String LABEL_BODY = "Body";

    // CRS definitions
    String LABEL_CRSAXIS       = "axis";
    String LABEL_CSAXIS        = "CoordinateSystemAxis";
    String LABEL_AXISABBREV    = "axisAbbrev";
    String LABEL_AXISDIRECTION = "axisDirection";
    String LABEL_CSA           = "CoordinateSystemAxis";
    String LABEL_NAME          = "name";
    String LABEL_ORIGIN        = "origin";
    String LABEL_TEMPORALCRS   = "TemporalCRS";
    // (suffixes: e.g. ProjectedCRS, TemporalCRS, etc.)
    String CRS_GMLSUFFIX   = "CRS";
    String CS_GMLSUFFIX    = "CS";
    String DATUM_GMLSUFFIX = "Datum";

    // SECORE equality test
    String LABEL_COMPARISON_RESULT = "comparisonResult";
    String LABEL_EQUAL             = "equal";
    String LABEL_EXCEPTION_TEXT    = "ExceptionText";

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

    // gml:SRSReferenceGroup
    String ATT_SRS_NAME = "srsName";
    String ATT_SRS_DIMENSION = "srsDimension";
    String ATT_UOM_LABELS = "uomLabels";
    String ATT_AXIS_LABELS = "axisLabels";

    // bbox
    String ATT_LOWERCORNER = "LowerCorner";
    String ATT_UPPERCORNER = "UpperCorner";
    String ATT_CRS = "crs";
    String ATT_DIMENSIONS = "dimensions";

    // CRS definitions
    String ATT_UOM = "uom";

    // swe
    String ATT_DEFINITION = "definition";
    String ATT_REASON = "reason";
    String ATT_UOMCODE = "code";

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

    String SCHEMA_LOCATION_WCS = "http://schemas.opengis.net/wcs/2.0/wcsAll.xsd";
    String SCHEMA_LOCATION_GML = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
    String SCHEMA_LOCATION_GMLRGRID = "http://schemas.opengis.net/gml/3.3/referencableGrid.xsd";

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Predefined entities' names
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String PREDEFINED_ENTITY_AMPERSAND        = "amp";
    String PREDEFINED_ENTITY_APOSTROPHE       = "apos";
    String PREDEFINED_ENTITY_LESSTHAN_SIGN    = "lt";
    String PREDEFINED_ENTITY_GREATERTHAN_SIGN = "gt";
    String PREDEFINED_ENTITY_QUOTES           = "quot";
}
