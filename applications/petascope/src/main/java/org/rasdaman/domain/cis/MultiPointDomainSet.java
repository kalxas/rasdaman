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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 *
 * 
 */
@Entity
@Table(name = MultiPointDomainSet.TABLE_NAME)
@PrimaryKeyJoinColumn(name = GeneralGridDomainSet.COLUMN_ID, referencedColumnName = DomainSet.COLUMN_ID)
public class MultiPointDomainSet extends DomainSet {
    
    public static final String TABLE_NAME = "multi_point_domain_set";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    @OneToOne (cascade = CascadeType.ALL)
    @JoinColumn (name = DirectMultiPoint.COLUMN_ID)
    private DirectMultiPoint directMultiPoint;
    
    public MultiPointDomainSet(DirectMultiPoint directMultiPoint) {
        this.directMultiPoint = directMultiPoint;
    }

    public DirectMultiPoint getDirectMultiPoint() {
        return directMultiPoint;
    }

    public void setDirectMultiPoint(DirectMultiPoint directMultiPoint) {
        this.directMultiPoint = directMultiPoint;
    }
    
}
