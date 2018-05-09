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
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * CIS 1.1
 * 
 * In a MultiPointCoverage the domain set is DirectMultiPoint which contains a collection of
arbitrarily distributed geometric points (positions).
 */
@Entity
@Table(name = DirectMultiPoint.TABLE_NAME)
public class DirectMultiPoint implements Serializable {
    
    public static final String TABLE_NAME = "general_grid";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;
    
    @ElementCollection(fetch = FetchType.EAGER)    
    // current is is combination of axes (e.g: coverage with 3 axes Lat Long high,
    // then a point is describe with coordinates: 456377.56257493998 339867.24995001999 53.953899579999998
    @OrderColumn
    private List<String> positions;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public List<String> getPositions() {
        return positions;
    }

    public void setPositions(List<String> positions) {
        this.positions = positions;
    }   
    
}
