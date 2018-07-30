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
package petascope.wcst.parsers;

import java.math.BigDecimal;

/**
 *
 * Parse InsertScaleLevel request from WCST_Import to create a corresponding downscaled
 * collection based on the input scale level.
 * 
 * e.g: http://localhost:8080/rasdaman/ows?serivce=WCS&version=2.0.1&request=InsertScaleLevel&level=2&coverageId=test_mr
 * 
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InsertScaleLevelRequest extends AbstractWCSTRequest {
    
    private String coverageId;
    private BigDecimal level;

    public InsertScaleLevelRequest(String coverageId, BigDecimal level) {
        this.coverageId = coverageId;
        this.level = level;
    }

    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public BigDecimal getLevel() {
        return level;
    }

    public void setLevel(BigDecimal level) {
        this.level = level;
    }
}
