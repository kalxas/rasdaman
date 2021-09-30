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
package org.rasdaman.domain.cis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import petascope.util.ListUtil;

/**
 * A coverage has a pyramid set which contains a list of coverages as pyramid members
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */

@Entity
@Table(name = CoveragePyramid.TABLE_NAME)
public class CoveragePyramid implements Serializable {
    
    public static final String TABLE_NAME = "coverage_pyramid";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    // for now, only X and Y axes can be scaled down, and they have non-one resolutions
    public static final String NON_XY_AXIS_SCALE_FACTOR = "1";
    
    @Id
    @JsonIgnore
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;    
    
    // NOTE: this one can be local coverage (test_cov) or remote coverage with qualified coverage id (e.g: vm1:test_cov)
    @Column(name = "pyramid_member_coverage_id")
    private String pyramidMemberCoverageId;
    
    @Column(name = "scale_factors")
    @Lob
    // NOTE: As this could be long text, so varchar(255) is not enough
    // Scale factor for each axis in geo CRS order (e.g: time, Lat and Long)
    // e.g: 1,0.2222222,0.3333333333
    private String scaleFactors;
    
    @Column(name = "synced")
    // NOTE: A base coverage can have multiple pyramid member coverages 
    // However, if a pyramid member coverage is created by CreatePyramidMember request OR it is created before version 10 as downscaled collection,
    // then, when updating the base coverage by WCS-T UpdateCoverage request,
    // this pyramid member **must be synced** by the input file
    @JsonIgnore
    boolean synced = false;
    
    public CoveragePyramid() {
        
    }

    public CoveragePyramid(String pyramidMemberCoverageId, List<String> scaleFactors, boolean synced) {
        this.pyramidMemberCoverageId = pyramidMemberCoverageId;
        this.scaleFactors = ListUtil.join(scaleFactors, ",");
        this.synced = synced;
    }

    public String getPyramidMemberCoverageId() {
        return pyramidMemberCoverageId;
    }

    public void setPyramidMemberCoverageId(String pyramidMemberCoverageId) {
        this.pyramidMemberCoverageId = pyramidMemberCoverageId;
    }

    @JsonIgnore
    public List<BigDecimal> getScaleFactorsList() {
        List<BigDecimal> results = new ArrayList<>();
        
        if (scaleFactors != null) {
            String[] values = scaleFactors.split(",");
            for (String value : values) {
                results.add(new BigDecimal(value));
            }
        }
        
        return results;
    }
    
    public String getScaleFactors() {
        return this.scaleFactors;
    }
    
    public boolean isSynced() {
        return this.synced;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        CoveragePyramid that = (CoveragePyramid) o;

        return pyramidMemberCoverageId.equals(that.pyramidMemberCoverageId);
    }
    
    @Override
    public int hashCode() {
        return pyramidMemberCoverageId.hashCode();
    }
    
    public String toString() {
        return "Coverage id '" + pyramidMemberCoverageId + "'.";
    }
    
}
