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

import java.util.Map;
import org.rasdaman.config.VersionManager;
import static petascope.core.KVPSymbols.KEY_FORMAT;
import static petascope.core.KVPSymbols.KEY_OUTPUT_TYPE;
import static petascope.core.KVPSymbols.KEY_VERSION;
import static petascope.core.KVPSymbols.VALUE_GENERAL_GRID_COVERAGE;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.ihandlers.kvp.IKVPHandler;
import static petascope.util.MIMEUtil.MIME_GML;

/**
 * Abstract class for WCS Handlers (GetCapabilities, DescribeCoverage,
 * GetCoverage and ProcessCoverage)
 *
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public abstract class KVPWCSAbstractHandler implements IKVPHandler {
    
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
                if (outputFormat != null && !outputFormat.equalsIgnoreCase(MIME_GML)) {
                    throw new PetascopeException(ExceptionCode.InvalidRequest, 
                            "GET KVP '" + KEY_OUTPUT_TYPE + "=" + VALUE_GENERAL_GRID_COVERAGE + "'"
                          + " is only valid if output format is '" + MIME_GML + "', given: '" + outputFormat + "'.");
                }
            }
        }
    }
        
}
