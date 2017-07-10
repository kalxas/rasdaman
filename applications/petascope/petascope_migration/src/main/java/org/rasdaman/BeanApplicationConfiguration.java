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
package org.rasdaman;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.migration.domain.legacy.LegacyDbMetadataSource;
import org.rasdaman.migration.service.LegacyMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import petascope.exceptions.PetascopeException;
import petascope.util.DatabaseUtil;

@Configuration
/**
 * This class initializes the bean which needs the passing dependencies from
 * properties file
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class BeanApplicationConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BeanApplicationConfiguration.class);

    /**
     * Instead of reading properties automatically from petascope.properties,
     * use this one to read from legacy configuration due to rasdaman installer
     * creates password differently for each installation for username rasdaman
     * in postgresql and it only knows about legacy configurations.
     *
     * @return
     * @throws java.lang.ClassNotFoundException
     * @throws petascope.exceptions.PetascopeException
     * @throws java.sql.SQLException
     */
    @Bean
    public DataSource dataSource() throws ClassNotFoundException, PetascopeException, SQLException {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();

        // NOTE: if legacy petascopedb exists, *must* create a temp database for Liquibase to populate tables to migrate first
        // then when the migration is done, rename the temp database back to petascopedb and the legacy petascopedb to petascopedb_94_backup
        if (DatabaseUtil.legacyPetascopeDatabaseExists()) {
            String tempDatabaseName = ConfigManager.PETASCOPE_DB + LegacyMigrationService.PETASCOPEDB_MIGRATION_TEMP_POSTFIX;
            DatabaseUtil.createDatabaseIfNotExist(tempDatabaseName);
            log.info("petascopedb 9.4 or older already exists, creating a temporary backup database '"
                    + ConfigManager.PETASCOPE_DB + LegacyMigrationService.PETASCOPEDB_MIGRATION_TEMP_POSTFIX + "' for it.");

            // jdbc:postgresql://localhost:5432/petascopedb_migration_temp
            String tempDatasourceURL = ConfigManager.PETASCOPE_DATASOURCE_URL + LegacyMigrationService.PETASCOPEDB_MIGRATION_TEMP_POSTFIX;
            dataSourceBuilder.url(tempDatasourceURL);
        } else {
            dataSourceBuilder.url(ConfigManager.PETASCOPE_DATASOURCE_URL);
        }

        // @TODO: remove this one when rasdaman installer changes from updating in legacy configurations for username, password to the new ones
        dataSourceBuilder.username(ConfigManager.LEGACY_DATASOURCE_USERNAME);
        dataSourceBuilder.password(ConfigManager.LEGACY_DATASOURCE_PASSWORD);

        return dataSourceBuilder.build();
    }

    @Bean
    public LegacyDbMetadataSource dbMetadataSource() throws Exception {
        // Used to support the translation from legacy coverage metadata to new CIS coverage type
        // This bean is initialized by Spring and we could use it when possible
        // NOTE: LegacyMigrationService uses this bean only.
        LegacyDbMetadataSource meta = null;
        if (DatabaseUtil.legacyPetascopeDatabaseExists()) {
            meta = new LegacyDbMetadataSource(ConfigManager.POSTGRESQL_DATASOURCE_DRIVER,
                    ConfigManager.LEGACY_DATASOURCE_URL,
                    ConfigManager.LEGACY_DATASOURCE_USERNAME,
                    ConfigManager.LEGACY_DATASOURCE_PASSWORD, false);
        }

        return meta;
    }

}
