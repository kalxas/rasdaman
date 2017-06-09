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
 * DomainSet describing the coverage’s domain and a rangeSet component
 * containing the range values (“pixels”, “voxels”) of the coverage.
 *
 * A coverage contains a domainSet component describing the coverage’s domain
 * (the set of “direct positions”, i.e., the locations for which values are
 * stored in the coverage).
 *
 * The domain set is defined through an ordered list of axes whose lower and
 * upper bounds establish the extent along each axis (extracted from CRS
 * reference group).
 *
 * Each of coverage type (GeneralGridCoverage, MultiPointCoverage,
 * MultiCurveCoverage, MultiSurfaceCoverage, MultiSolidCoverage) has different
 * DomainSet.
 *
 */
@Entity
@Table(name = DomainSet.TABLE_NAME)
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class DomainSet {

    public static final String TABLE_NAME = "domain_set";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    public DomainSet() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
