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
package petascope.wcs2.handlers.kvp;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.rasdaman.config.VersionManager;
import static petascope.core.KVPSymbols.KEY_FORMAT;
import static petascope.core.KVPSymbols.KEY_OUTPUT_TYPE;
import static petascope.core.KVPSymbols.KEY_VERSION;
import static petascope.core.KVPSymbols.VALUE_GENERAL_GRID_COVERAGE;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.ihandlers.kvp.IKVPHandler;
import static petascope.util.MIMEUtil.MIME_GML;
import static petascope.util.MIMEUtil.MIME_JSON;

/**
 * Abstract class for WCS Handlers (GetCapabilities, DescribeCoverage,
 * GetCoverage and ProcessCoverage)
 *
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public abstract class KVPWCSAbstractHandler implements IKVPHandler {
    
    /**
     * Overrided by subclass to check if parameter is not valid in the request.
     */
    public static void validateParameters(Map<String, String[]> kvpParameters, Set<String> validParameters) throws WCSException {
        for (String key : kvpParameters.keySet()) {
            if (!validParameters.contains(key.toLowerCase())) {
                throw new WCSException(ExceptionCode.InvalidRequest, "Parameter '" + key + "' is not valid in request.");
            }
        }
    }
    
    /**
     * Check if the request contains all required parameters (no more / no less other parameters).
     */
    public static void validateAllRequiredParameters(Map<String, String[]> kvpParameters, Set<String> validParameters) throws PetascopeException {
    
        if (kvpParameters.size() != validParameters.size()) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, 
                                        "Number of input parameters '" + kvpParameters.size() + "' does not match with"
                                      + " number of required parameters '" + validParameters.size() + "' for the request.");
        }

        for (String key : kvpParameters.keySet()) {
            if (!validParameters.contains(key.toLowerCase())) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Parameter '" + key + "' is not valid in request.");
            }
        }
    }
    
    /**
     * Check if request contains more than one parameters for unique parameter (e.g: request=GetMap&subsettingCRS=...&subsetingCRS=... is invalid)
     */
    public static void validateUniqueParameters(Map<String, String[]> kvpParameters, Set<String> uniqueParameters) throws PetascopeException {
        for (String key : uniqueParameters) {
            String[] tmps = kvpParameters.get(key);
            if (tmps != null && tmps.length > 1) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "Parameter '" + key + "' is duplicate in request.");
            }
        }
    }
    
    /**
     *  Check if key exists in KVP GET request to return its value. 
     * 
    **/
    protected String getKVPValue(Map<String, String[]> kvpParameters, String kvpKey) {
        String value = null;
        
        if (kvpParameters.get(kvpKey) != null) {
            value = kvpParameters.get(kvpKey)[0];
        }
        
        return value;
    }
    
    /**
     * Check if petascope should converse CIS 1.0 to CIS 1.1 with format GML
     * by parameter outputType=GeneralGridCoverage
     */
    protected void validateCoverageConversionCIS11(Map<String, String[]> kvpParameters) throws PetascopeException {
        String outputType = this.getKVPValue(kvpParameters, KEY_OUTPUT_TYPE);
        String version = this.getKVPValue(kvpParameters, KEY_VERSION);
        
        if (outputType != null) {            
            if (!outputType.equalsIgnoreCase(VALUE_GENERAL_GRID_COVERAGE)) {
                throw new PetascopeException(ExceptionCode.InvalidRequest, "GET KVP value for key '" + KEY_OUTPUT_TYPE + "' is not valid. "
                                                                         + "Given: '" + outputType + "'.");
            } else {
                if (version.equals(VersionManager.WCS_VERSION_20)) {
                    throw new PetascopeException(ExceptionCode.InvalidRequest, "Request parameter '" 
                                                                               + KEY_OUTPUT_TYPE + "=" + VALUE_GENERAL_GRID_COVERAGE + "' is not valid for WCS version '"
                                                                               + VersionManager.WCS_VERSION_20 + "' as CIS 1.1 only supported in version 2.1+.");
                }
                
                String outputFormat = this.getKVPValue(kvpParameters, KEY_FORMAT);
                if (outputFormat != null && !this.isValidGeneralGridCoverageFormat(outputFormat) ) {
                    throw new PetascopeException(ExceptionCode.InvalidRequest, 
                            "GET KVP '" + KEY_OUTPUT_TYPE + "=" + VALUE_GENERAL_GRID_COVERAGE + "'"
                          + " is only valid if output format is '" + MIME_GML + "' or '" + MIME_JSON + "', given: '" + outputFormat + "'.");
                }
            }
        }
    }
    
    /**
     * Check if output format is gml / json
     */
    private boolean isValidGeneralGridCoverageFormat(String outputFormat) {
        // e.g: application/gml
        boolean fullMIME = outputFormat.equalsIgnoreCase(MIME_GML) || outputFormat.equalsIgnoreCase(MIME_JSON);
        // e.g: json
        boolean shorthandMIME = MIME_GML.contains(outputFormat.toLowerCase()) || MIME_JSON.contains(outputFormat.toLowerCase());
        
        return fullMIME || shorthandMIME;
    }
        
}
