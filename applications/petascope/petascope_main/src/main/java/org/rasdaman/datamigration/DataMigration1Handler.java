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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.datamigration;
import org.springframework.stereotype.Service;

/**
 * Class to handle data migration version number 1
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class DataMigration1Handler extends AbstractDataMigrationHandler {
    
    public DataMigration1Handler() {
        // NOTE: update this by one for new handler class
        this.migrationVersion = 1;
        this.handlerId = "aea84698-80b6-11eb-8e53-509a4cb4e064";
    }

    @Override
    public void migrate() {
        
        // @TODO: add code here for handling migration verison number 1

    }
    
}