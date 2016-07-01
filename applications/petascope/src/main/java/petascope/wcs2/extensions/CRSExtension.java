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
package petascope.wcs2.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCSException;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;

/**
 * Manage CRS Extension (OGC 11-053).
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class CRSExtension implements Extension {

    private static final Logger log = LoggerFactory.getLogger(CRSExtension.class);
    public static final String REST_SUBSETTING_PARAM = "subsettingcrs";
    public static final String REST_OUTPUT_PARAM = "outputcrs";

    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.CRS_IDENTIFIER;
    }

    /**
     * Method for the handling of possible subsets in case of subsettingCrs not
     * correspondent to the one which the desired collection is natively stored.
     *
     * @param request The WCS request, which is directly modified.
     * @param m coverage metadata 
     *
     */
    protected void handle(GetCoverageRequest request, GetCoverageMetadata m) throws WCSException {
        
        GetCoverageRequest.CrsExt crsExt = request.getCrsExt();
        if(crsExt.getOutputCrs() != null) {
            m.setOutputCrs(crsExt.getOutputCrs());
        }        
        if(crsExt.getSubsettingCrs() != null) {
            m.setSubsettingCrs(crsExt.getSubsettingCrs());
        }        
        crsExt.getSubsettingCrs();
    }

    @Override
    public Boolean hasParent() {
        return true;
    }

    @Override
    public String getParentExtensionIdentifier() {
        return "";
    }
}