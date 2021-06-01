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
package org.rasdaman.domain.cis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.*;

/**
 * The rangeType element of a coverage describes the coverage's range set data structure.
 * 
 * The rangeType component adds a structure description and technical metadata
 * required for an appropriate (however, application independent) understanding
 * of a coverage.
 *
 * Specification of the common data type all range values share is done through
 * the Data- Record part of the coverage’s rangeType component. Atomic data
 * types available for range values are those given by the SWE Common data type
 * AbstractSimpleComponent .
 *
 * <rangeType>
 * <swe:DataRecord>
 * <swe:field name="red">
 * <swe:Quantity definition="http://opengis.net/def/property/OGC/0/Radiance">
 * <swe:uom code="W/cm2"/>
 * </swe:Quantity>
 * </swe:field>
 * ...
 * </swe:DataRecord>
 * </rangeType>
 */
@Entity
@Table(name = RangeType.TABLE_NAME)
public class RangeType implements Serializable {

    public static final String TABLE_NAME = "range_type";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @OneToOne (cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = DataRecord.COLUMN_ID)
    // contains all the data type of all ranges (bands)
    private DataRecord dataRecord;

    @OneToOne (cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = InterpolationRestriction.COLUMN_ID)
    // Optional
    private InterpolationRestriction interpolationRestriction;

    public RangeType() {

    }

    public RangeType(DataRecord dataRecord, InterpolationRestriction interpolationRestriction) {
        this.dataRecord = dataRecord;
        this.interpolationRestriction = interpolationRestriction;
    }
    
    public DataRecord getDataRecord() {
        return dataRecord;
    }

    public void setDataRecord(DataRecord dataRecord) {
        this.dataRecord = dataRecord;
    }

    public InterpolationRestriction getInterpolationRestriction() {
        return interpolationRestriction;
    }

    public void setInterpolationRestriction(InterpolationRestriction interpolationRestriction) {
        this.interpolationRestriction = interpolationRestriction;
    }
}
