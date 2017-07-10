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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * CIS 1.1
 *
 *
 * Optional (zero or One)
 *
 * The coverageFunction component is identical in its syntax and meaning to the
 * corresponding element defined in GML 3.2.1 [2] Subclause 19.3.11. It
 * describes the mapping function from the domain to the range of the coverage.
 * For a grid coverage, it specifies the serialization of the multi-dimensional
 * grid in the range set.
 *
 * Note 1: This becomes particularly relevant when defining encoding formats,
 * such as GML or JSON. Note 2: For the reader’s convenience, the default is
 * copied from GML 3.2.1: If the gml:coverageFunction property is omitted for a
 * gridded coverage (including rectified gridded coverages) the gml:startPoint
 * is assumed to be the value of the gml:low property in the gml:Grid geometry,
 * and the gml:sequenceRule is assumed to be linear and the gml:axisOrder
 * property is assumed to be "+1 +2"
 *
 *
 * Clearer definition is in: http://www.rasdaman.org/wiki/PetascopeSubsets
 */
@Entity
@Table(name = CoverageFunction.TABLE_NAME)
public class CoverageFunction {

    public static final String TABLE_NAME = "coverage_function";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    // all old legacy coverages does not have a definition for gridFunction, so use the default value
    public static final String DEFAULT_SEQUENCE_RULE = "linear";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;
    
    public CoverageFunction() {
        
    }
    
    public CoverageFunction(String sequenceRule) {
        this.sequenceRule = sequenceRule;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "sequence_rule")
    // If the gml:coverageFunction property is omitted
    // the gml:sequenceRule is assumed to be linear (e.g: +2 +1 for coverage with 2 axes)
    // rasdaman uses its own grid function when listing cell values, linearly spanning the outer dimensions first, 
    // then proceeding to the innermost ones. To make it clearer, this means column-major listing order in the 2D case.    
    private String sequenceRule;

    public String getSequenceRule() {
        if (sequenceRule == null) {
            sequenceRule = this.DEFAULT_SEQUENCE_RULE;
        }
        return sequenceRule;
    }

    public void setSequenceRule(String sequenceRule) {
        this.sequenceRule = sequenceRule;
    }
}
