/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.rasdaman.domain.migration.Migration;
import org.rasdaman.repository.interfaces.MigrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Abstract class for migration service
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public abstract class AbstractMigrationService {

    @Autowired
    protected MigrationRepository migrationRepository;       

    /**
     * Return the list of migrations in database (each time migration is
     * processed, another entry is written in Migration table).
     *
     * @return
     */
    protected List<Migration> getMigrations() {
        Iterator<Migration> iterator = migrationRepository.findAll().iterator();
        List<Migration> migrations = new ArrayList<>();
        while (iterator.hasNext()) {
            migrations.add(iterator.next());
        }

        return migrations;
    }

    /**
     * Check if the database migration is running.
     *
     * @return
     */
    public boolean isMigrating() {
        // Get the last entry of table to check lock        
        Migration migration = null;
        Iterator<Migration> iterator = migrationRepository.findAll().iterator();
        if (iterator.hasNext()) {
            while (iterator.hasNext()) {
                migration = iterator.next();
            }
        } else {
            // In case of database does not have any entry when legacydatabase not exist
            return false;
        }

        return migration.isLock();
    }

    /**
     * Migrate from old database to new database
     *
     * @throws java.lang.Exception
     */
    protected abstract void migrate() throws Exception;

    /**
     * Check if a concrete service can be run or not.
     *
     * @return
     * @throws Exception
     */
    public abstract boolean canMigrate() throws Exception;

}
