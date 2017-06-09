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

import javax.persistence.*;

/**
 * A coverage contains a rangeType element which de- scribes the coverage's
 * range set data structure (in the case of images usually called the “pixel
 * data type”). Such a type often consists of one or more fields (also referred
 * to as bands or channels or variables)
 *
 * <rangeType>
 * <swe:DataRecord>
 * <swe:field name="singleBand">
 * <swe:Quantity definition="http://www.opengis.net/def/dataType/OGC/0/unsignedInt">
 * <swe:uom code="10^0"/>
 * </swe:Quantity>
 * </swe:field>
 * </swe:DataRecord>
 * </rangeType>
 */
@Entity
@Table(name = Field.TABLE_NAME)
public class Field {

    public static final String TABLE_NAME = "field";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "name")
    private String name;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Quantity.COLUMN_ID)
    private Quantity quantity;

    public Field() {

    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

}
