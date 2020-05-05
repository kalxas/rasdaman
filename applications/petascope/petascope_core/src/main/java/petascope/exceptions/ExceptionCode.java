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
package petascope.exceptions;

import static javax.servlet.http.HttpServletResponse.*;
import petascope.core.XMLSymbols;

/**
 * Represents an exception code, as defined in [OGC 06-121r9] Clause 8
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class ExceptionCode {

    /**
     * Default HTTP exit code for an exception
     */
    public static final int DEFAULT_EXIT_CODE = SC_INTERNAL_SERVER_ERROR;

    /**
     * Code representing type of this exception
     */
    private final String exceptionCodeName;
    /**
     * Exception code description
     */
    private final String description;
    /**
     * Indicator of location in the client's operation request where this
     * exception was encountered
     */
    private String locator;
    /**
     * HTTP response code, when the exception is returned over HTTP
     */
    private int httpErrorCode;

    /**
     * Create new ExceptionCode with no description text and default HTTP exit code
     *
     * @param exceptionCode Code representing type of this exception
     */
    public ExceptionCode(String exceptionCode) {
        this(exceptionCode, null);
    }

    /**
     * Create new ExceptionCode with default HTTP exit code
     *
     * @param exceptionCode Code representing type of this exception
     * @param description Exception code description
     */
    public ExceptionCode(String exceptionCode, String description) {
        this(exceptionCode, description, DEFAULT_EXIT_CODE);
    }

    /**
     * Create new ExceptionCode with no description text
     *
     * @param exceptionCode Code representing type of this exception
     * @param httpErrorCode HTTP response code, when the exception is returned over HTTP
     */
    public ExceptionCode(String exceptionCode, int httpErrorCode) {
        this(exceptionCode, null, httpErrorCode);
    }

    /**
     * Create new ExceptionCode with specified description and HTTP exit code
     *
     * @param exceptionCode Code representing type of this exception
     * @param description Exception code description
     * @param httpErrorCode HTTP response code, when the exception is returned over HTTP
     */
    public ExceptionCode(String exceptionCode, String description, int httpErrorCode) {
        this.exceptionCodeName = exceptionCode;
        this.description = description;
        this.httpErrorCode = httpErrorCode;
    }

    /**
     * Use this to set the locator on the fly, while passing the exception code
     *
     * @param locator Indicator of location in the client's operation request
     * where this exception was encountered
     * @return a copy of the original exception code
     */
    public ExceptionCode locator(String locator) {
        ExceptionCode ret = new ExceptionCode(exceptionCodeName, description, httpErrorCode);
        ret.setLocator(locator);
        return ret;
    }

    /**
     * @return Exception code description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Code representing type of this exception
     */
    public String getExceptionCodeName() {
        return exceptionCodeName;
    }

    /**
     * @return HTTP response code, when the exception is returned over HTTP
     */
    public int getHttpErrorCode() {
        return httpErrorCode;
    }

    /**
     * @return Indicator of location in the client's operation request where
     * this exception was encountered
     */
    public String getLocator() {
        return locator;
    }

    /**
     * @param locator Indicator of location in the client's operation request
     * where this exception was encountered
     */
    public void setLocator(String locator) {
        this.locator = locator;
    }

    @Override
    public String toString() {
        return "Exception Code {"
               + "\n  exceptionCode = " + exceptionCodeName
               + "\n  description = " + description
               + "\n  locator = " + locator
               + "\n  httpErrorCode = " + httpErrorCode
               + "\n}";
    }

    public static final ExceptionCode BadResponseHandler = new ExceptionCode("BadResponseHandler");
    public static final ExceptionCode BadPostParameter = new ExceptionCode("BadPostParameter");
    public static final ExceptionCode IllegalFieldSequence = new ExceptionCode("IllegalFieldSequence",
            "Lower limit is above the upper limit in the range field interval.", SC_BAD_REQUEST);
    public static final ExceptionCode InternalComponentError = new ExceptionCode("InternalComponentError");
    public static final ExceptionCode InternalSqlError = new ExceptionCode("InternalSqlError");
    public static final ExceptionCode InternalWMSError = new ExceptionCode("InternalWMSError", SC_BAD_REQUEST);
    public static final ExceptionCode InterpolationMethodNotSupported = new ExceptionCode("InterpolationMethodNotSupported",
            "'interpolation' parameter indicated is not supported by this server (i.e., URL is not known to this server).", SC_METHOD_NOT_ALLOWED);
    public static final ExceptionCode InvalidAxisLabel = new ExceptionCode("InvalidAxisLabel",
            "The dimension subsetting operation specified an axis label that does not exist in the Envelope " +
            "or has been used more than once in the GetCoverage request.", SC_NOT_FOUND);
    public static final ExceptionCode InvalidMediatype = new ExceptionCode("InvalidMediatype",
            "Request contains an invalid mediatype: it must contain a MIME type identifier of fixed value multipart/related.", SC_UNSUPPORTED_MEDIA_TYPE);
    public static final ExceptionCode InvalidCoverageConfiguration = new ExceptionCode("InvalidCoverageConfiguration");
    public static final ExceptionCode InvalidEncodingSyntax = new ExceptionCode("InvalidEncodingSyntax",
            "Document received does not conform with protocol syntax", SC_BAD_REQUEST);
    public static final ExceptionCode InvalidURL = new ExceptionCode("InvalidURL", SC_BAD_REQUEST);
    public static final ExceptionCode SyntaxError = new ExceptionCode("SyntaxError",
            "Operation request is syntactically malformed.", SC_BAD_REQUEST);
    public static final ExceptionCode SemanticError = new ExceptionCode("SemanticError",
            "Operation request is semantically wrong.", SC_BAD_REQUEST);
    public static final ExceptionCode InvalidMetadata = new ExceptionCode("InvalidMetadata");
    public static final ExceptionCode InvalidParameterValue = new ExceptionCode("InvalidParameterValue",
            "Operation request contains an invalid parameter value", SC_BAD_REQUEST);
    public static final ExceptionCode InvalidPropertyValue = new ExceptionCode("InvalidPropertyValue");
    public static final ExceptionCode InvalidRequest = new ExceptionCode("InvalidRequest", SC_BAD_REQUEST);
    public static final ExceptionCode AccessDenied = new ExceptionCode("AccessDenied", "Access denied, please login first.", SC_BAD_REQUEST);
    public static final ExceptionCode Unauthorized = new ExceptionCode("Unauthorized", "Unauthorized request.", SC_UNAUTHORIZED);
    public static final ExceptionCode InvalidServiceConfiguration = new ExceptionCode("InvalidServiceConfiguration");
    public static final ExceptionCode InvalidSubsetting = new ExceptionCode("InvalidSubsetting",
            "Operation request contains an invalid subsetting value; either a trim or slice parameter value "
            + "is outside the extent of the coverage or, in a trim operation, a lower bound is above the upper bound.", SC_NOT_FOUND);
    public static final ExceptionCode InvalidTemporalMetadata = new ExceptionCode("InvalidTemporalMetadata");
    public static final ExceptionCode InvalidUpdateSequence = new ExceptionCode("InvalidUpdateSequence",
            "Value of (optional) updateSequence parameter in GetCapabilities operation request "
            + "is greater than current value of service metadata updateSequence number", SC_BAD_REQUEST);
    public static final ExceptionCode IOConnectionError = new ExceptionCode("IOConnectionError");
    public static final ExceptionCode MaliciousQuery = new ExceptionCode("MaliciousQuery");
    public static final ExceptionCode MissingPropertyKey = new ExceptionCode("MissingPropertyKey");
    public static final ExceptionCode MissingCRS = new ExceptionCode("MissingCRS", "CRS could be missing in the query. Please check", SC_BAD_REQUEST);
    public static final ExceptionCode MissingParameterValue = new ExceptionCode("MissingParameterValue",
            "Operation request does not include a parameter value, and this server did "
            + "not declare a default value for that parameter. Locator: Name of missing parameter", SC_BAD_REQUEST);
    public static final ExceptionCode MultiBandImagesNotSupported = new ExceptionCode("MultiBandImagesNotSupported");
    public static final ExceptionCode NoApplicableCode = new ExceptionCode("NoApplicableCode",
            "No other exceptionCode specified by this service and server applies to this exception", SC_NOT_FOUND);
    public static final ExceptionCode NodeParsingNotImplemented = new ExceptionCode("NodeParsingNotImplemented");
    public static final ExceptionCode NoSuchCoverage = new ExceptionCode("NoSuchCoverage",
            "One of the identifiers passed does not match with any of the coverages offered by this server. "
            + "Locator: List of violating coverage identifiers", SC_NOT_FOUND);
    public static final ExceptionCode NoSuchLayer = new ExceptionCode("NoSuchLayer",
            "One of the identifiers passed does not match with any of the layers offered by this server. "
            + "Locator: List of violating coverage identifiers", SC_NOT_FOUND);
    public static final ExceptionCode NoSuchMediaType = new ExceptionCode("NoSuchMediaType",
            "The requested mediatype is not valid", SC_NOT_FOUND);
    public static final ExceptionCode NotEnoughStorage = new ExceptionCode("NotEnoughStorage");
    public static final ExceptionCode OperationNotSupported = new ExceptionCode("OperationNotSupported",
            "Request is for an operation that is not supported by this server. Locator: Name of operation not supported", SC_NOT_IMPLEMENTED);
    public static final ExceptionCode OptionNotSupported = new ExceptionCode("OptionNotSupported",
            "Request is for an option that is not supported by this server. Locator: Identifier of option not supported", SC_NOT_IMPLEMENTED);
    public static final ExceptionCode OperationNotAllowed = new ExceptionCode("OperationNotAllowed",
            "Request is for an operation that is not allowed by this server. Locator: Identifier of operation not allowed", SC_FORBIDDEN);
    public static final ExceptionCode RasdamanRequestFailed = new ExceptionCode("RasdamanRequestFailed");
    public static final ExceptionCode RasdamanUnavailable = new ExceptionCode("RasdamanUnavailable");
    public static final ExceptionCode RasdamanError = new ExceptionCode("RasdamanError");
    public static final ExceptionCode ResolverError = new ExceptionCode("ResolverError");
    public static final ExceptionCode ResourceError = new ExceptionCode("ResourceError");
    public static final ExceptionCode RuntimeError = new ExceptionCode("RuntimeError");
    public static final ExceptionCode SecoreError = new ExceptionCode("SecoreError");
    public static final ExceptionCode ServletConnectionError = new ExceptionCode("ServletConnectionError");
    public static final ExceptionCode UnknownError = new ExceptionCode("UnknownError");
    public static final ExceptionCode UnsupportedCombination = new ExceptionCode("UnsupportedCombination", SC_NOT_IMPLEMENTED);
    public static final ExceptionCode UnsupportedEncodingFormat = new ExceptionCode("UnsupportedEncodingFormat", SC_NOT_IMPLEMENTED);
    public static final ExceptionCode UnsupportedCoverageConfiguration = new ExceptionCode("UnsupportedCoverageConfiguration",
            "One or more of the available coverages is not queryable due to unimplemented service features.", SC_NOT_IMPLEMENTED);
    public static final ExceptionCode VersionNegotiationFailed = new ExceptionCode("VersionNegotiationFailed",
            "List of versions in AcceptVersions parameter value in GetCapabilities operation "
            + "request did not include any version supported by this server.", SC_BAD_REQUEST);
    public static final ExceptionCode XmlNotValid = new ExceptionCode("XmlNotValid");
    public static final ExceptionCode XmlStructuresError = new ExceptionCode("XmlStructuresError");
    public static final ExceptionCode WcpsError = new ExceptionCode("WcpsError", SC_NOT_FOUND);
    public static final ExceptionCode WcsError = new ExceptionCode("WcsError", SC_NOT_FOUND);
    public static final ExceptionCode WcstError = new ExceptionCode("WcstError", SC_NOT_FOUND);
    public static final ExceptionCode WpsError = new ExceptionCode("WpsError", SC_NOT_FOUND);
    public static final ExceptionCode MultipleRangeSubsets = new ExceptionCode("Invalid RangeSubsets parameters",
            "Multiple RangeSubset parameters have been provided, only one can be accepted.", SC_BAD_REQUEST);
    public static final ExceptionCode NoSuchField = new ExceptionCode("NoSuchField",
            "One or more range field names indicated in the request are not defined in range type of the coverage addressed.", SC_BAD_REQUEST);
    public static final ExceptionCode InvalidCoverageType = new ExceptionCode("InvalidCoverageType",
            "Coverage addressed is not a grid coverage", SC_BAD_REQUEST);
    public static final ExceptionCode InvalidScaleFactor = new ExceptionCode("InvalidScaleFactor",
            "Scale factor passed is not valid (no number or less than or equal to zero.)", SC_BAD_REQUEST);
    public static final ExceptionCode InvalidExtent = new ExceptionCode("InvalidExtent",
            "Extent interval passed has upper bound smaller than lower bound.", SC_BAD_REQUEST);
    public static final ExceptionCode ScaleAxisUndefined = new ExceptionCode("ScaleAxisUndefined",
            "CRS axis indicated is not an axis occurring in this coverage.", SC_BAD_REQUEST);
    public static final ExceptionCode WCSPMissingQueryParameter = new ExceptionCode("WCSPMissingQueryParameter",
            "No query parameter was found in the request.", SC_BAD_REQUEST);
    public static final ExceptionCode WCSTMissingCoverageParameter = new ExceptionCode("WCSTMissingCoverageParameter",
            "No \"coverage\" of \"coverageRef\" parameters were found in the request (at least one expected).");
    public static final ExceptionCode WCSTUnknownUseId = new ExceptionCode("WCSTUnknownUseId",
            "Unknown value of the useId parameter(expecting one of \"existing\" or \"new\").");
    public static final ExceptionCode WCSTMalformedURL = new ExceptionCode("WCSTMalformedURL",
            "The URL provided in the coverageRef parameter is malformed.");
    public static final ExceptionCode WCSTMissingBoundedBy = new ExceptionCode("WCSTMissingBoundedBy",
            "The coverage contains wrong number of \"" + XMLSymbols.LABEL_BOUNDEDBY + "\" elements (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingEnvelope = new ExceptionCode("WCSTMissingEnvelope",
            "The coverage contains wrong number of \"" + XMLSymbols.LABEL_ENVELOPE + "\" elements (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingDomainSet = new ExceptionCode("WCSTMissingDomainSet",
            "The coverage contains wrong number of \"" + XMLSymbols.LABEL_DOMAIN_SET + "\" elements (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingGridType = new ExceptionCode("WCSTMissingGridType",
            "The \"" + XMLSymbols.LABEL_DOMAIN_SET + "\" element contains the wrong number of grid types (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingLimits = new ExceptionCode("WCSTMissingLimits",
            "The \"" + XMLSymbols.LABEL_RECTIFIED_GRID + "\" element contains the wrong number of \"" +
            XMLSymbols.LABEL_LIMITS + "\" (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingGridEnvelope = new ExceptionCode("WCSTMissingGridEnvelope",
            "The \"" + XMLSymbols.LABEL_LIMITS + "\" element contains the wrong number of \"" +
            XMLSymbols.LABEL_GRID_ENVELOPE + "\" (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingLow = new ExceptionCode("WCSTMissingLow",
            "The \"" + XMLSymbols.LABEL_GRID_ENVELOPE + "\" element contains the wrong number of \"" +
            XMLSymbols.LABEL_LOW + "\" (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingLowerCorner = new ExceptionCode("WCSTMissingLowCorner",
            "The \"" + XMLSymbols.LABEL_GRID_ENVELOPE + "\" element contains the wrong number of \"" +
            XMLSymbols.LABEL_LOWER_CORNER + "\" (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingHigh = new ExceptionCode("WCSTMissingHigh",
            "The \"" + XMLSymbols.LABEL_GRID_ENVELOPE + "\" element contains the wrong number of \"" +
            XMLSymbols.LABEL_HIGH + "\" (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingUpperCorner = new ExceptionCode("WCSTMissingUpperCorner",
            "The \"" + XMLSymbols.LABEL_GRID_ENVELOPE + "\" element contains the wrong number of \"" +
            XMLSymbols.LABEL_UPPER_CORNER + "\" (exactly 1 expected).");
    public static final ExceptionCode WCSTLowHighDifferentSizes = new ExceptionCode("WCSTHighLowDifferentSizes",
            "The \"" + XMLSymbols.LABEL_LOW + "\" and \"" + XMLSymbols.LABEL_HIGH + "\" elements don't contain the same number of points.");
    public static final ExceptionCode WCSTCoverageNotFound = new ExceptionCode("WCSTCoverageNotFound",
            "No coverage found at the given URL.");
    public static final ExceptionCode WCSTInvalidXML = new ExceptionCode("WCSTInvalidXML",
            "The coverage is not valid XML.");
    public static final ExceptionCode WCSTMissingGridOrigin = new ExceptionCode("WCSTMissingPoint",
            "The \"" + XMLSymbols.LABEL_RECTIFIED_GRID + "\" element contains the wrong number of \"" +
            XMLSymbols.LABEL_ORIGIN + "\" (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingPoint = new ExceptionCode("WCSTMissingPoint",
            "The \"" + XMLSymbols.LABEL_ORIGIN + "\" element contains the wrong number of \"" +
            XMLSymbols.LABEL_POINT + "\" (exactly 1 expected).");
    public static final ExceptionCode WCSTMissingPos = new ExceptionCode("WCSTMissingPos",
            "The \"" + XMLSymbols.LABEL_ORIGIN + "\" element contains the wrong number of \"" +
            XMLSymbols.LABEL_POS + "\" (exactly 1 expected).");
    public static final ExceptionCode WCSTWrongNumberOfOffsetVectors = new ExceptionCode("WCSTWrongNumberOfOffsetVectors",
            "The number of \"" + XMLSymbols.LABEL_OFFSET_VECTOR + "\" elements is different from the number of axes");
    public static final ExceptionCode WCSTWrongInervalFormat = new ExceptionCode("WCSTWrongIntervalFormat",
            "The number of points in at least one of the \"" + XMLSymbols.LABEL_INTERVAL + "\" elements is wrong "
            + "(exactly 2 expected).");
    public static final ExceptionCode WCSTWrongNumberOfTupleLists = new ExceptionCode("WCSTWrongNumberOfTupleLists",
            "The number of \"" + XMLSymbols.LABEL_TUPLELIST + "\" elements is wrong (exactly 1 expected).");
    public static final ExceptionCode WCSTWrongNumberOfPixels = new ExceptionCode("WCSTWrongNumberOfPixels",
            "The number of pixels provided in the \"" + XMLSymbols.LABEL_TUPLELIST + "\" element doesn't correspond to the coverage size");
    public static final ExceptionCode WCSTWrongNumberOfRangeSetElements = new ExceptionCode("WCSTWrongNumberOfRangeSetElements",
            "The number of \"" + XMLSymbols.LABEL_RANGESET + "\" elements is wrong (exactly 1 expected).");
    public static final ExceptionCode WCSTWrongNumberOfDataBlockElements = new ExceptionCode("WCSTWrongNumberOfDataBlockElements",
            "The number of \"" + XMLSymbols.LABEL_DATABLOCK + "\" elements is wrong (exactly 1 expected).");
    public static final ExceptionCode WCSTWrongNumberOfFileElements = new ExceptionCode("WCSTWrongNumberOfFileElements",
            "The number of \"" + XMLSymbols.LABEL_FILE + "\" elements is wrong (exactly 1 expected).");
    public static final ExceptionCode WCSTWrongNumberOfFileReferenceElements = new ExceptionCode("WCSTWrongNumberOfFileReferenceElements",
            "The number of \"" + XMLSymbols.LABEL_FILE_REFERENCE + "\" elements is wrong (exactly 1 expected).");
    public static final ExceptionCode WCSTWrongNumberOfFileStructureElements = new ExceptionCode("WCSTWrongNumberOfFileStructureElements",
            "The number of \"" + XMLSymbols.LABEL_FILE_STRUCTURE + "\" elements is wrong (exactly 1 expected).");
    public static final ExceptionCode WCSTDuplicatedCoverageId = new ExceptionCode("WCSTDuplicatedCoverageName",
            "A coverage with this name already exists (pick a different name or use the request parameter useId=new).");
    public static final ExceptionCode WCSTTypeRegistryNotFound = new ExceptionCode("WCSTTypeRegistryNotFound",
            "Could not read the rasdaman type registry.");
    public static final ExceptionCode WCSTOnlyRectifiedGridsSupported = new ExceptionCode("WCSTOnlyRectifiedGridsSupported",
            "Only rectified grids are supported in the domainSet element.");

    public static final ExceptionCode InconsistentChange = new ExceptionCode("InconsistentChange", SC_NOT_FOUND);
    public static final ExceptionCode CollectionExists = new ExceptionCode("CollectionExists", "Collection name already exists in rasdaman.");
    public static final ExceptionCode CollectionDoesNotExist = new ExceptionCode("CollectionDoesNotExist", "Collection name does not exist in rasdaman.");

    // WCS CRS-extension
    public static final ExceptionCode SubsettingCrsNotSupported = new ExceptionCode("SubsettingCrs-NotSupported",
            "CRS indicated in the subsettingCrs parameter is not supported by this server.", 501);
    public static final ExceptionCode OutputCrsNotSupported = new ExceptionCode("OutputCrs-NotSupported",
            "CRS indicated in the outputCrs parameter is not supported by this server.", 501);
    public static final ExceptionCode GridCoverageNotSupported = new ExceptionCode("GridCoverageNotSupported",
            "Grid coverage cannot be used to project (transform) by this server.", 501);
    
    // WCS 2 Exception code (InvalidRequest returns error 400 but in some cases OGC CITE wants error 404 for WCS 2.0.1)
    public static final ExceptionCode WCSBadRequest = new ExceptionCode("WCSBadRequest", 404);

}
