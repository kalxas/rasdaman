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
import java.util.List;
import javax.persistence.*;

/**
 * CIS 1.1
 *
 * Description of the common data type of all range values.
 *
 * Specification of the common data type all range values share is done through
 * the Data- Record part of the coverage’s rangeType component.
 *
 * Within a DataRecord contained in a concrete range structure, each of its
 * record components is locally uniquely identified by the record component’s
 * field attribute, in accordance with the “soft-typing” property introduced by
 * SWE Common.
 *
 * <swe:DataRecord>
 * <swe:field name="red">
 * <swe:Quantity definition="http://opengis.net/def/property/OGC/0/Radiance">
 * <swe:uom code="W/cm2"/>
 * </swe:Quantity>
 * </swe:field>
 * ...
 * </swe:DataRecord>
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = DataRecord.TABLE_NAME)
public class DataRecord implements Serializable {

    public static final String TABLE_NAME = "data_record";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = DataRecord.COLUMN_ID)
    @OrderColumn
    private List<Field> fields;
    
    public DataRecord() {
        
    }

    public DataRecord(List<Field> fields) {
        this.fields = fields;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
