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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.admin.pyramid.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Class to represent the result of pyramid member in ListPyramidMembers request in JSON format
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class PyramidMember {
    
    private String coverage;
    private List<BigDecimal> scaleFactors;

    public PyramidMember(String coverage, List<BigDecimal> scaleFactors) {
        this.coverage = coverage;
        this.scaleFactors = scaleFactors;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public List<BigDecimal> getScaleFactors() {
        return scaleFactors;
    }

    public void setScaleFactors(List<BigDecimal> scaleFactors) {
        this.scaleFactors = scaleFactors;
    }
}
