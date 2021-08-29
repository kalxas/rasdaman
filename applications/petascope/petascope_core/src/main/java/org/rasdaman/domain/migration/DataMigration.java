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
package org.rasdaman.domain.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * If the version number in petascope and the one in petascopedb is different,
 * petascope needs to run migration internally, to populate data to 
 * the new existing tables / newly created tables by Liquibase
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Entity
@Table(name = DataMigration.TABLE_NAME)
public class DataMigration implements Serializable {
    
    public static final String TABLE_NAME = "data_migration";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    @Id
    @JsonIgnore
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @Column(name = "applied_migration")
    // NOTE: This is a random generated string uuid to store in database
    private String appliedMigration;

    public DataMigration() {
        this.appliedMigration = "";
    }
    
    public DataMigration(String appliedMigration) {
        this.appliedMigration = appliedMigration;
    }

    public String getAppliedMigration() {
        return appliedMigration;
    }

    public void setAppliedMigration(String appliedMigration) {
        this.appliedMigration = appliedMigration;
    }
    
}
