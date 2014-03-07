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

/**
 * Represents an exception code, as defined in [OGC 06-121r9] Clause 8
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class ExceptionCode {

    /**
     * Default HTTP exit code for an exception
     */
    public static final int DEFAULT_EXIT_CODE = 500;

    /**
     * Code representing type of this exception
     */
    private final String exceptionCode;
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
        this.exceptionCode = exceptionCode;
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
        ExceptionCode ret = new ExceptionCode(exceptionCode, description, httpErrorCode);
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
    public String getExceptionCode() {
        return exceptionCode;
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
                + "\n  exceptionCode = " + exceptionCode
                + "\n  description = " + description
                + "\n  locator = " + locator
                + "\n  httpErrorCode = " + httpErrorCode
                + "\n}";
    }

    public static final ExceptionCode BadResponseHandler = new ExceptionCode("BadResponseHandler");
    public static final ExceptionCode BadPostParameter = new ExceptionCode("BadPostParameter");
    public static final ExceptionCode InternalComponentError = new ExceptionCode("InternalComponentError");
    public static final ExceptionCode InternalSqlError = new ExceptionCode("InternalSqlError");
    public static final ExceptionCode InvalidAxisLabel = new ExceptionCode("InvalidAxisLabel",
            "The dimension subsetting operation specified an axis label that does not exist in the Envelope " +
            "or has been used more than once in the GetCoverage request.", 404);
    public static final ExceptionCode InvalidMediatype = new ExceptionCode("InvalidMediatype",
            "Request contains an invalid mediatype: it must contain a MIME type identifier of fixed value multipart/related.", 400);
    public static final ExceptionCode InvalidCoverageConfiguration = new ExceptionCode("InvalidCoverageConfiguration");
    public static final ExceptionCode InvalidEncodingSyntax = new ExceptionCode("InvalidEncodingSyntax",
            "Document received does not conform with protocol syntax", 400);
    public static final ExceptionCode InvalidMetadata = new ExceptionCode("InvalidMetadata");
    public static final ExceptionCode InvalidParameterValue = new ExceptionCode("InvalidParameterValue",
            "Operation request contains an invalid parameter value", 400);
    public static final ExceptionCode InvalidPropertyValue = new ExceptionCode("InvalidPropertyValue");
    public static final ExceptionCode InvalidRequest = new ExceptionCode("InvalidRequest", 400);
    public static final ExceptionCode InvalidServiceConfiguration = new ExceptionCode("InvalidServiceConfiguration");
    public static final ExceptionCode InvalidSubsetting = new ExceptionCode("InvalidSubsetting",
            "Operation request contains an invalid subsetting value; either a trim or slice parameter value "
            + "is outside the extent of the coverage or, in a trim operation, a lower bound is above the upper bound.", 404);
    public static final ExceptionCode InvalidTemporalMetadata = new ExceptionCode("InvalidTemporalMetadata");
    public static final ExceptionCode InvalidUpdateSequence = new ExceptionCode("InvalidUpdateSequence",
            "Value of (optional) updateSequence parameter in GetCapabilities operation request "
            + "is greater than current value of service metadata updateSequence number", 400);
    public static final ExceptionCode IOConnectionError = new ExceptionCode("IOConnectionError");
    public static final ExceptionCode MaliciousQuery = new ExceptionCode("MaliciousQuery");
    public static final ExceptionCode MissingCRS = new ExceptionCode("MissingCRS", "CRS could be missing in the query. Please check", 400);
    public static final ExceptionCode MissingParameterValue = new ExceptionCode("MissingParameterValue",
            "Operation request does not include a parameter value, and this server did "
            + "not declare a default value for that parameter. Locator: Name of missing parameter", 400);
    public static final ExceptionCode MultiBandImagesNotSupported = new ExceptionCode("MultiBandImagesNotSupported");
    public static final ExceptionCode NoApplicableCode = new ExceptionCode("NoApplicableCode",
            "No other exceptionCode specified by this service and server applies to this exception");
    public static final ExceptionCode NodeParsingNotImplemented = new ExceptionCode("NodeParsingNotImplemented");
    public static final ExceptionCode NoSuchCoverage = new ExceptionCode("NoSuchCoverage",
            "One of the identifiers passed does not match with any of the coverages offered by this server. "
            + "Locator: List of violating coverage identifiers", 404);
    public static final ExceptionCode NotEnoughStorage = new ExceptionCode("NotEnoughStorage");
    public static final ExceptionCode OperationNotSupported = new ExceptionCode("OperationNotSupported",
            "Request is for an operation that is not supported by this server. Locator: Name of operation not supported", 501);
    public static final ExceptionCode OptionNotSupported = new ExceptionCode("OptionNotSupported",
            "Request is for an option that is not supported by this server. Locator: Identifier of option not supported", 501);
    public static final ExceptionCode RasdamanRequestFailed = new ExceptionCode("RasdamanRequestFailed", 404);
    public static final ExceptionCode RasdamanUnavailable = new ExceptionCode("RasdamanUnavailable");
    public static final ExceptionCode RasdamanError = new ExceptionCode("RasdamanError", 404);
    public static final ExceptionCode ResolverError = new ExceptionCode("ResolverError");
    public static final ExceptionCode ResourceError = new ExceptionCode("ResourceError");
    public static final ExceptionCode RuntimeError = new ExceptionCode("RuntimeError", 404);
    public static final ExceptionCode SecoreError = new ExceptionCode("SecoreError");
    public static final ExceptionCode ServletConnectionError = new ExceptionCode("ServletConnectionError");
    public static final ExceptionCode UnknownError = new ExceptionCode("UnknownError", 404);
    public static final ExceptionCode UnsupportedCombination = new ExceptionCode("UnsupportedCombination", 501);
    public static final ExceptionCode UnsupportedCoverageConfiguration = new ExceptionCode("UnsupportedCoverageConfiguration",
            "One or more of the available coverages is not queryable due to unimplemented service features.", 501);
    public static final ExceptionCode VersionNegotiationFailed = new ExceptionCode("VersionNegotiationFailed",
            "List of versions in AcceptVersions parameter value in GetCapabilities operation "
            + "request did not include any version supported by this server", 400);
    public static final ExceptionCode XmlNotValid = new ExceptionCode("XmlNotValid");
    public static final ExceptionCode XmlStructuresError = new ExceptionCode("XmlStructuresError");
    public static final ExceptionCode WcpsError = new ExceptionCode("WcpsError");
    public static final ExceptionCode WcsError = new ExceptionCode("WcsError", 404);
    public static final ExceptionCode WcstError = new ExceptionCode("WcstError");
    public static final ExceptionCode WpsError = new ExceptionCode("WpsError");
    public static final ExceptionCode MultipleRangeSubsets = new ExceptionCode("Invalid RangeSubsets parameters",
            "Multiple RangeSubset parameters have been provided, only one can be accepted", 400);
    public static final ExceptionCode NoSuchField = new ExceptionCode("NoSuchField",
            "One or more range field names indicated in the request are not defined in range type of the coverage addressed", 400);
    public static final ExceptionCode InvalidCoverageType = new ExceptionCode("InvalidCoverageType",
            "Coverage addressed is not a grid coverage", 400);
    public static final ExceptionCode InvalidScaleFactor = new ExceptionCode("InvalidScaleFactor",
            "Scale factor passed is not valid (no number or less than or equal to zero)", 400);
    public static final ExceptionCode InvalidExtent = new ExceptionCode("InvalidExtent",
            "Extent interval passed has upper bound smaller than lower bound", 400);
    public static final ExceptionCode ScaleAxisUndefined = new ExceptionCode("ScaleAxisUndefined",
            "CRS axis indicated is not an axis occurring in this coverage", 400);
    public static final ExceptionCode WCSPMissingQueryParameter = new ExceptionCode("WCSPMissingQueryParameter",
            "No query parameter was found in the request.");
}
