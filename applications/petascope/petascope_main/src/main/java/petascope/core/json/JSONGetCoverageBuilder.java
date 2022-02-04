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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.json;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.json.cis11.JSONCIS11GetCoverage;
import petascope.core.json.cis11.JSONCoreCIS11;
import petascope.core.json.cis11.JSONCoreCIS11Builder;
import petascope.core.json.cis11.model.rangeset.DataBlock;
import petascope.core.json.cis11.model.rangeset.RangeSet;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Build GMLGetCoverage object as result of WCS GetCoverage request in JSON.
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class JSONGetCoverageBuilder {

    @Autowired
    private JSONCoreCIS11Builder jsonCoreCIS11Builder;

    /**
     * Build result for GetCoverage request in JSON format
     */
    public JSONCIS11GetCoverage buildWCSGetCoverageResult(WcpsCoverageMetadata wcpsCoverageMetadata, List<Object> pixelValues) 
                throws PetascopeException {
        JSONCIS11GetCoverage result = this.buildJSONGetCoverage(wcpsCoverageMetadata, pixelValues);

        return result;
    }

    /**
     * Build RangeSet element based on the values from Rasql collection.
     */
    private RangeSet buildRangeSetCIS11(List<Object> pixelValues) {
        DataBlock dataBlock = new DataBlock(pixelValues);
        RangeSet rangeSet = new RangeSet(dataBlock);

        return rangeSet;
    }

    /**
     * Build JSON CIS 1.1 for GetCoverage request
     */
    private JSONCIS11GetCoverage buildJSONGetCoverage(WcpsCoverageMetadata wcpsCoverageMetadata, List<Object> pixelValues) throws PetascopeException {
        String coverageId = wcpsCoverageMetadata.getCoverageName();
        JSONCoreCIS11 jsonCore = this.jsonCoreCIS11Builder.build(wcpsCoverageMetadata);
        RangeSet rangeSet = null;
        if (pixelValues != null) {
            rangeSet = this.buildRangeSetCIS11(pixelValues);
        }
        JSONCIS11GetCoverage result = new JSONCIS11GetCoverage(coverageId, jsonCore, rangeSet);

        return result;
    }

}
