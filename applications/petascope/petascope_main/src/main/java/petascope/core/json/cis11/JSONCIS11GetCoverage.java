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
package petascope.core.json.cis11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import static petascope.core.json.cis11.JSONCIS11GetCoverage.COVERAGE_ID_NAME;
import static petascope.core.json.cis11.JSONCIS11GetCoverage.TYPE_NAME;
import petascope.core.json.cis11.model.rangeset.RangeSet;

/**
 * Class to build result for WCS GetCoverage request in JSON. 
 * e.g: http://schemas.opengis.net/cis/1.1/json/examples/30_4D_height+time.json
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@JsonPropertyOrder({TYPE_NAME, COVERAGE_ID_NAME})
public class JSONCIS11GetCoverage {
    
    public static final String COVERAGE_ID_NAME = "id";
    public static final String TYPE_NAME = "type";

    private final String type  = "CoverageByDomainAndRangeType";
    
    private String coverageId;
    // Unwrapp this property to get all contents in JSON output
    @JsonUnwrapped
    private JSONCoreCIS11 jsonCore;
    private RangeSet rangeSet;

    public JSONCIS11GetCoverage(String coverageId, JSONCoreCIS11 gmlCore, RangeSet rangeSet) {
        this.coverageId = coverageId;
        this.jsonCore = gmlCore;
        this.rangeSet = rangeSet;
    }

    public String getType() {
        return type;
    }    

    @JsonProperty(COVERAGE_ID_NAME)
    public String getCoverageId() {
        return coverageId;
    }

    @JsonProperty(COVERAGE_ID_NAME)
    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public JSONCoreCIS11 getJsonCore() {
        return jsonCore;
    }

    public void setJsonCore(JSONCoreCIS11 jsonCore) {
        this.jsonCore = jsonCore;
    }

    public RangeSet getRangeSet() {
        return rangeSet;
    }

    public void setRangeSet(RangeSet rangeSet) {
        this.rangeSet = rangeSet;
    }

}
