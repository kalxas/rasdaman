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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.config;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.CIS_SERVICE;
import static petascope.core.KVPSymbols.RASQL_SERVICE;
import static petascope.core.KVPSymbols.WCPS_SERVICE;
import static petascope.core.KVPSymbols.WCST_SERVICE;
import static petascope.core.KVPSymbols.WCS_SERVICE;
import static petascope.core.KVPSymbols.WMS_SERVICE;
import static petascope.core.KVPSymbols.WMTS_SERVICE;

/**
 * Class to control the W*S services and their versions which petascope can process.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class VersionManager {
    
        // CRS resolver
    public static final String SECORE_VERSION = "0.1.0";
    // OGC services info    
    public static final String CIS_VERSION = "1.1";
    public static final String WCST_VERSION = "2.0.0";
    public static final String WCPS_VERSION = "1.0.0";
    public static final String WCS_VERSION_20 = "2.0.1";
    public static final String WCS_VERSION_21 = "2.1.0";
    public static final String RASQL_SERVLET_VERSION = "1.0.0";
    public static final String WMS_VERSION_13 = "1.3.0";
    public static final String WMTS_VERSION_10 = "1.0.0";
    
    // Store serviceName -> versions (e.g: WCS -> 2.0.1, 2.1)
    private static final Map<String, List<String>> serviceVersionsMap = new LinkedHashMap<>();
    
    static {
        serviceVersionsMap.put(CIS_SERVICE,  Arrays.asList(CIS_VERSION));
        
        serviceVersionsMap.put(WCS_SERVICE,  Arrays.asList(WCS_VERSION_20, WCS_VERSION_21));
        serviceVersionsMap.put(WCST_SERVICE,  Arrays.asList(WCST_VERSION));
        serviceVersionsMap.put(WCPS_SERVICE,  Arrays.asList(WCPS_VERSION));
        
        serviceVersionsMap.put(WMS_SERVICE,  Arrays.asList(WMS_VERSION_13));
        serviceVersionsMap.put(WMTS_SERVICE,  Arrays.asList(WMTS_VERSION_10));
        
        serviceVersionsMap.put(RASQL_SERVICE,  Arrays.asList(RASQL_SERVLET_VERSION));
        
        serviceVersionsMap.put(KVPSymbols.KEY_SOAP,  Arrays.asList(WCS_VERSION_20, WCS_VERSION_21));
    }
    
    /**
     * Get latest version of a service (e.g: WCS -> 2.1).
     * 
     */
    public static String getLatestVersion(String serviceName) {
        List<String> versions = serviceVersionsMap.get(serviceName);
        String latestVersion = versions.get(versions.size() - 1);
        
        return latestVersion;
    }
    
    public static String getLowestVersion(String serviceName) {
        List<String> versions = serviceVersionsMap.get(serviceName);
        String lowestVersion = versions.get(0);
        
        return lowestVersion;
    }
    
    /**
     * Get all supported versions of a service (e.g: WCS -> 2.0.1 and 2.1).
     */
    public static List<String> getAllSupportedVersions(String serviceName) {
        List<String> versions = serviceVersionsMap.get(serviceName);
        return versions;       
    }

    /**
     * Check if a version of a service is supported (e.g: WMS 1.1.0 is not supported)
     */
    public static boolean isSupported(String serviceName, String version) {
        List<String> versions = serviceVersionsMap.get(serviceName);
        if (versions.contains(version)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if the requesting service is supported
     */
    public static boolean isSupported(String serviceName) {
        return serviceVersionsMap.get(serviceName) != null;
    }
    
    /*
    Check if the requesting version is WMTS or not
    */
    public static boolean isWMTSRequest(String[] versions) {
        return versions != null && versions[0].equals(VersionManager.WMTS_VERSION_10);
    }
}
