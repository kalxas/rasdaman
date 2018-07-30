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
package petascope.wcs2.handlers.kvp.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.util.ListUtil;

/**
 * Service class for Scaling handler of GetCoverageKVP class
 *
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWCSGetCoverageScalingService {

    public KVPWCSGetCoverageScalingService() {

    }

    /**
     * Depend on which type of scale to generate correct WCPS query e.g:
     * scalefactor=0.5 -> scalefactor(c, 0.5)
     *
     * @param queryContent
     * @param kvpParameters
     * @return
     * @throws petascope.exceptions.WCSException
     */
    public String handleScaleExtension(String queryContent, Map<String, String[]> kvpParameters) throws WCSException {
        // Validate first as no mixing scale parameters (e.g: scalefactor=2&scaleaxes=Lat(0.5),Long(4.3)
        // but scaleaxes=Lat(0.5)&scaleaxes=Long(0.5) is ok
        Set<String> scaleParameters = new HashSet<>();
        for (Map.Entry<String, String[]> entry : kvpParameters.entrySet()) {
            if (entry.getKey().toLowerCase().contains(KVPSymbols.KEY_SCALE_PREFIX)) {
                scaleParameters.add(entry.getKey().toLowerCase());
            }
        }

        if (scaleParameters.size() > 1) {
            throw new WCSException(ExceptionCode.InvalidRequest, "Multiple scaling parameters in the request: must be unique.");
        }

        if (kvpParameters.containsKey(KVPSymbols.KEY_SCALEFACTOR)) {
            // e.g: scalefactor=3 then all axes will downscaled by 3 (e.g: i has 100 pixels, then the ouput is 100 / 3)
            String scaleFactor = kvpParameters.get(KVPSymbols.KEY_SCALEFACTOR)[0];
            queryContent = KVPSymbols.KEY_SCALEFACTOR + "( " + queryContent + ", " + scaleFactor + " )";

        } else if (kvpParameters.containsKey(KVPSymbols.KEY_SCALEAXES)) {
            // e.g: scaleaxes=i(5)&scaleaxes=j(3) then (e.g: i has 100 pixels, then the output is 100 / 5 pixels, j has 100 pixels, then the output is 100 / 3 pixels)            
            String[] scaleAxesParams = kvpParameters.get(KVPSymbols.KEY_SCALEAXES);
            List<String> scaleAxes = new ArrayList<>();
            for (String scaleAxisParam : scaleAxesParams) {
                scaleAxes.add(scaleAxisParam);
            }
            queryContent = KVPSymbols.KEY_SCALEAXES + "( " + queryContent + ", [" + ListUtil.join(scaleAxes, ", ") + "] )";
        } else if (kvpParameters.containsKey(KVPSymbols.KEY_SCALESIZE)) {
            // e.g: scalesize=i(5)&scalesize=j(3) then (e.g: i has 100 pixels, then the output is 5 pixels, j has 100 pixels, then the output is 3 pixels)            
            String[] scaleSizeParams = kvpParameters.get(KVPSymbols.KEY_SCALESIZE);
            List<String> scaleSizes = new ArrayList<>();
            for (String scaleSizeParam : scaleSizeParams) {
                scaleSizes.add(scaleSizeParam);
            }
            queryContent = KVPSymbols.KEY_SCALESIZE + "( " + queryContent + ", [" + ListUtil.join(scaleSizes, ", ") + "] )";
        } else if (kvpParameters.containsKey(KVPSymbols.KEY_SCALEEXTENT)) {
            // e.g: scaleextent=i(5:10)&scaleaxes=j(3:30) then (e.g: i has 100 pixels, then the output is 5:10 pixels, j has 100 pixels, then the output is 3:30 pixels)            
            String[] scaleExtentParams = kvpParameters.get(KVPSymbols.KEY_SCALEEXTENT);
            List<String> scaleExtents = new ArrayList<>();
            for (String scaleExtentParam : scaleExtentParams) {
                scaleExtents.add(scaleExtentParam);
            }
            queryContent = KVPSymbols.KEY_SCALEEXTENT + "( " + queryContent + ", [" + ListUtil.join(scaleExtents, ", ") + "] )";
        }

        return queryContent;
    }
}
