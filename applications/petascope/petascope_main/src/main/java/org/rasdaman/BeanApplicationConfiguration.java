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

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import static org.rasdaman.InitAllConfigurationsApplicationService.addJDBCDriverToClassPath;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import petascope.util.DatabaseUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import petascope.controller.AbstractController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * This class initializes the beans to connect to the datasources (from legacy
 * petascope prior version 9.5, Spring Hibernate). *
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Configuration
@EnableTransactionManagement
public class BeanApplicationConfiguration implements Condition {

    private static final Logger log = LoggerFactory.getLogger(BeanApplicationConfiguration.class);
    public static final String LIQUIBASE_CHANGELOG_PATH = "classpath:database_versions/db.changelog-master.xml";

    // ************************ For general beans ************************
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }

    /**
     * NOTE: This must be enabled to have POST request body in POST handler of
     * controller with HttpServletRequest as parameter. If it is not enabled,
     * HttpServletRequest post body will always empty as this method below
     * already read the data before you can get it. If you are not using this
     * parameter but RequestBody of Spring, so no need to enable this hidden
     * feature.
     *
     * @param filter
     * @return
     */
    @Bean
    public FilterRegistrationBean registration(HiddenHttpMethodFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }
    
    // ************************ For non-migrating beans ************************

    /**
     * Build a DataSource (JDBC driver) to connect to underneath DBMS for
     * Liquibase. Configuration values are fetched from petascope.properties.
     *
     * @return
     * @throws petascope.exceptions.PetascopeException
     */
    private DataSource dynamicDataSource() throws PetascopeException {

        // If user doesn't use postgresql by default
        if (!ConfigManager.PETASCOPE_DATASOURCE_URL.contains(ConfigManager.DEFAULT_DMBS)) {
            try {
                addJDBCDriverToClassPath(ConfigManager.PETASCOPE_DATASOURCE_JDBC_JAR_PATH, ConfigManager.PETASCOPE_DATASOURCE_URL);
            } catch (PetascopeException ex) {
                throw new PetascopeException(ExceptionCode.InvalidPropertyValue,
                        "JDBC driver jar file path for current DBMS configured in petascope.properties with key '"
                        + ConfigManager.KEY_PETASCOPE_DATASOURCE_JDBC_JAR_PATH + "', given value '"
                        + ConfigManager.PETASCOPE_DATASOURCE_JDBC_JAR_PATH + "' is not valid.",
                        ex);
            }
        }

        DataSource dataSource = DataSourceBuilder.create().url(ConfigManager.PETASCOPE_DATASOURCE_URL)
                .username(ConfigManager.PETASCOPE_DATASOURCE_USERNAME)
                .password(ConfigManager.PETASCOPE_DATASOURCE_PASSWORD)
                .build();

        return dataSource;
    }
    
    /**
     * NOTE: without migration, this one is used to create datasource (populate database schema by Liquibase)
     * for spring configuration in petascope.properties.
     */
    @Bean
    @Conditional(BeanApplicationConfiguration.class)
    public SpringLiquibase liquibase() throws Exception {
        SpringLiquibase liquibase = new SpringLiquibase();
        DataSource dataSource;

        try {
            if (DatabaseUtil.checkDefaultDatabase()) {
                // NOTE: Only create a new petascopedb for Postgresql if it doesn't exist
                // and Liquibase will populate schema on this empty database.
                DatabaseUtil.createPostgresqlDatabaseIfNotExist(null);
            }            
        } catch (Exception ex) {
            // e.g: postgresql is not running, fatal error for Spring Framework to continue
            PetascopeException petascopeException = new PetascopeException(ExceptionCode.InternalSqlError,
                    "Cannot create new empty petascopedb for Postgresql, error '" + ex.getMessage() + "'.", ex);
            throw petascopeException;
        }
        
        // NOTE: Do not initialize/update petascopedb if petascope failed to start properly.
        if (AbstractController.startException != null) {
            liquibase.setShouldRun(false);
            return liquibase;
        }

        // Create dataSource for Liquibase
        dataSource = this.dynamicDataSource();       
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(LIQUIBASE_CHANGELOG_PATH);

        if (DatabaseUtil.targetLegacyPetascopeDatabaseExists()) {            
            if (!ApplicationMain.MIGRATE) {
                // Run rasdaman.war with petascopedb version 9.4 which is not valid
                String errorMessage = "petascopedb 9.4 or older already exists, "
                    + "please run the migrate_petascopedb.sh script to migrate to the new petascope schema first, then restart petascope.";
                PetascopeException exception = new PetascopeException(ExceptionCode.InternalSqlError, errorMessage);
                AbstractController.startException = exception;
                log.error(errorMessage, exception);
                liquibase.setShouldRun(false); 
            }            
        }
        
        // NOTE: delete any existing lock to allow Liquibase populate data if last process couldn't finish properly.
        if (!DatabaseUtil.petascopeDatabaseEmpty(dataSource)) {
            DatabaseUtil.deletelLiquibaseLock(dataSource);
        }

        return liquibase;
    }
    
    
    @Override
    // NOTE: only when NOT running java -jar rasdaman.war --migrate, it will need to create
    // the beans which have annotaion @Conditional which match this condition.
    public boolean matches(ConditionContext cc, AnnotatedTypeMetadata atm) {
        if (ApplicationMain.MIGRATE) {
            return false;
        } else {
            return true;
        }
    }
}
