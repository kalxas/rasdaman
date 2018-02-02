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
package petascope.wcs2.handlers.kvp.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;

/**
 * Service class to parse WCS GetCoverage with clipping extension e.g:
 * clip=POLYGON((...)) with GET request or clip=$1 with POST request and a text
 * file contaning WKT (e.g: POLYGON((...))) to replace at $1.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class KVPWCSGetcoverageClipService {

    public String handle(Map<String, String[]> kvpParameters, String coverageExpression, String subsettingCrs) throws WCSException {

        // Handle for WCS WKT clipping extension
        // e.g: clip=POLYGON((...))&subsettingCRS=http://opengis.net/def/crs/EPSG/0/4326
        String wkt = kvpParameters.get(KVPSymbols.KEY_CLIP) != null
                ? kvpParameters.get(KVPSymbols.KEY_CLIP)[0] : null;

        if (wkt != null) {
            // NOTE: CRS of WKT is applied from subsettingCrs parameter 
            // (e.g: WKT is in EPSG:4326 and coverage is in ESPG:3857, then without subsettingCrs=EPSG:3857, it cannot translate coordinates for WKT)
            if (subsettingCrs == null) {
                // e.g: clip(c,  POLYGON((...)) )
                coverageExpression = KVPSymbols.KEY_CLIP + "( " + coverageExpression + ", " + wkt + " )";
            } else {
                // e.g: clip (c,  POLYGON((...)), "http://opengis.net/def/crs/EPSG/0/3857")
                coverageExpression = KVPSymbols.KEY_CLIP + "(" + coverageExpression + ", " + wkt + ", \"" + subsettingCrs + "\")";
            }
        }

        return coverageExpression;
    }
}
