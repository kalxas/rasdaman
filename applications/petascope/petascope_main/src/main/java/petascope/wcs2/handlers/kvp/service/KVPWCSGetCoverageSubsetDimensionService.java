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

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import petascope.core.KVPSymbols;
import petascope.exceptions.WCSException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcs2.parsers.subsets.AbstractSubsetDimension;
import petascope.wcs2.parsers.subsets.SubsetDimensionParserService;

/**
 * Service class for Subset handler of GetCoverageKVP class
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class KVPWCSGetCoverageSubsetDimensionService {

    public KVPWCSGetCoverageSubsetDimensionService() {

    }

    /**
     * Parse subsets in String to List of SubsetDimensions
     * e.g: subset=i(0,200)&subset=j(0,30)
     *
     * @param kvpParameters
     * @param wcpsCoverageMetadata
     * @return
     * @throws petascope.exceptions.WCSException
     */
    public List<AbstractSubsetDimension> parseSubsets(Map<String, String[]> kvpParameters, WcpsCoverageMetadata wcpsCoverageMetadata) throws WCSException {
        String[] subsets = kvpParameters.get(KVPSymbols.KEY_SUBSET);
        if (subsets == null) {
            subsets = new String[wcpsCoverageMetadata.getAxes().size()];
            // Now, build the subsets for all the axes of coverage when no subset is mentioned as parameter
            int i = 0;
            for (Axis axis : wcpsCoverageMetadata.getAxes()) {
                subsets[i] = axis.getLabel() + "(" + axis.getGeoBounds().getLowerLimit() + "," + axis.getGeoBounds().getUpperLimit() + ")";
                i++;
            }
        }
        // convert the string to list of subset dimension objects
        List<AbstractSubsetDimension> dimensionSubsets = SubsetDimensionParserService.parseSubsets(subsets);
        return dimensionSubsets;
    }
}
