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

import java.util.List;
import java.util.logging.Level;
import javax.annotation.Resource;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.migration.service.AbstractMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import petascope.util.DatabaseUtil;

@SpringBootApplication
@ComponentScan({"org.rasdaman", "petascope"})
@PropertySource({"classpath:application.properties"})
/**
 * Main class of migration application.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class ApplicationMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMigration.class);

    @Resource
    // Spring finds all the subclass of AbstractMigrationService and injects to the list
    List<AbstractMigrationService> migrationServices;

    /**
     * Return the exit code to user
     */
    public enum ExitCode {
        SUCCESS(0),
        FAILURE(1);

        private final int value;

        private ExitCode(int value) {
            this.value = value;
        }

        public int getExitCode() {
            return this.value;
        }
    }

    public static void main(String[] args) throws Exception {        
        SpringApplication.run(ApplicationMigration.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("Migrating petascopedb from JDBC URL '" + ConfigManager.LEGACY_DATASOURCE_URL + "' to JDBC URL '" + ConfigManager.PETASCOPE_DATASOURCE_URL + "'...");
        // First, check if old JDBC URL can be connected
        if (!DatabaseUtil.checkJDBCConnection(ConfigManager.LEGACY_DATASOURCE_URL,
                ConfigManager.LEGACY_DATASOURCE_USERNAME, ConfigManager.LEGACY_DATASOURCE_PASSWORD)) {
            log.error("Cannot connect to existing petascopedb database at JDBC URL '" + ConfigManager.LEGACY_DATASOURCE_URL + "', aborting the migration process.");
            System.exit(ExitCode.FAILURE.getExitCode());
        }

        /*
        NOTE: Hibernate already connected when migration application starts,
        so With the embedded database, if another connection tries to connect, it will return exception.
         */
        // Then, check what kind of migration should be done
        for (AbstractMigrationService migrationService : migrationServices) {
            // @TODO: make a service to migrate for new database version from 9.5
            if (migrationService.isMigrating()) {
                // A migration process is running, don't do anything else
                log.error("A migration process is already running.");
                System.exit(ExitCode.FAILURE.getExitCode());
            }
            if (migrationService.canMigrate()) {
                try {
                    migrationService.migrate();
                } catch (Exception ex) {
                    log.error("An error occured while migrating, aborting the migration process.", ex);
                    // Release the lock on Migration table so later can run migration again
                    System.exit(ExitCode.FAILURE.getExitCode());
                }

            }
        }

        log.info("petascopedb migrated successfully.");
        System.exit(ExitCode.SUCCESS.getExitCode());
    }
}
