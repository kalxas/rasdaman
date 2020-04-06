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

import java.io.Serializable;
import javax.persistence.*;

/**
 * SWE 08-094 This element is used to specify numerical constraints for the
 * “Count” and “Quantity” elements and the corresponding range components.
 *
 * Several allowed intervals and values can also be combined to express a
 * complex constraint
 *
 * <swe:AllowedValues>
 * <swe:interval>-180 0</swe:interval>
 * <swe:interval>1 180</swe:interval>
 * </swe:AllowedValues>
 * or it can combine with a single value
 * <swe:value>5</swe:value>
 *
 */
@Entity
@Table(name = AllowedValue.TABLE_NAME)
public class AllowedValue implements Serializable {

    public static final String TABLE_NAME = "allowed_value";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "allowed_values")
    private String values;

    public AllowedValue() {

    }

    public AllowedValue(String values) {
        this.values = values;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

}
