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
 * CIS 1.1
 * 
 * A rangeSet component containing the range values (“pixels”, “voxels”)
of the coverage (stored as Rasdaman collection and mdds).
 */

@Entity
@Table(name = RasdamanRangeSet.TABLE_NAME)
public class RasdamanRangeSet {
    
    public static final String TABLE_NAME = "rasdaman_range_set";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "collection_name")
    private String collectionName;

    @Column(name = "collection_type")
    private String collectionType;

    @Column(name = "oid")
    private Long oid;

    @Column(name= "mdd_type")
    private String mddType;

    public RasdamanRangeSet() {
        
    }

    protected RasdamanRangeSet(String collectionName, String collectionType, Long oid, String mddType) {
        this.collectionName = collectionName;
        this.collectionType = collectionType;
        this.oid = oid;
        this.mddType = mddType;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public String getMddType() {
        return mddType;
    }

    public void setMddType(String mddType) {
        this.mddType = mddType;
    }
}
