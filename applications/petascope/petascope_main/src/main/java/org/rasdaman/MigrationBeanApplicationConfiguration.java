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

import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.rasdaman.ApplicationMain.ExitCode;
import static org.rasdaman.BeanApplicationConfiguration.LIQUIBASE_CHANGELOG_PATH;
import static org.rasdaman.CustomImplicitNamingStrategyImpl.HIBERNATE_IMPLICIT_NAMING_STRATEGY_KEY;
import static org.rasdaman.CustomImplicitNamingStrategyImpl.HIBERNATE_IMPLICIT_NAMING_STRATEGY_VALUE;
import static org.rasdaman.CustomPhysicalNamingStrategyImpl.HIBERNATE_PHYSICAL_NAMING_STRATEGY_KEY;
import static org.rasdaman.CustomPhysicalNamingStrategyImpl.HIBERNATE_PHYSICAL_NAMING_STRATEGY_VALUE;
import static org.rasdaman.InitAllConfigurationsApplicationService.addJDBCDriverToClassPath;
import org.rasdaman.config.ConfigManager;
import org.rasdaman.migration.domain.legacy.LegacyDbMetadataSource;
import org.rasdaman.migration.service.LegacyMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import petascope.util.DatabaseUtil;
import static org.rasdaman.migration.service.LegacyMigrationService.LEGACY_PETASCOPEDB_MIGRATION_TEMP;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
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
public class MigrationBeanApplicationConfiguration implements Condition {

    private static final Logger log = LoggerFactory.getLogger(MigrationBeanApplicationConfiguration.class);

    // Configuration for different datasources used by Spring Hibernate in migration application
    public static final String BASE_PACKAGE = "org.rasdaman";
    public static final String SOURCE_ENTITY_MANAGER = "sourceEntityManager";
    public static final String TARGET_ENTITY_MANAGER = "targetEntityManager";
    public static final String SOURCE_ENTITY_MANAGER_FACTORY = "sourceEntityManagerFactory";
    public static final String TARGET_ENTITY_MANAGER_FACTORY = "targetEntityManagerFactory";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String SOURCE_TRANSACTION_MANAGER = "sourceTransactionManager";
    public static final String TARGET_TRANSACTION_MANAGER = "targetTransactionManager";    
           
    
    // ************************ For migrating beans ************************        
    /**
     * NOTE: with migration, this one is used to create datasource (populate database schema by Liquibase)
     * for spring configuration in petascope.properties.
     */
    @Bean
    @Conditional(MigrationBeanApplicationConfiguration.class)
    public SpringLiquibase migrationLiquibase(String datasourceUrl) throws Exception {
        SpringLiquibase liquibase = new SpringLiquibase();
        DataSource dataSource = DataSourceBuilder.create().url(datasourceUrl)
                .username(ConfigManager.PETASCOPE_DATASOURCE_USERNAME)
                .password(ConfigManager.PETASCOPE_DATASOURCE_PASSWORD).build();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(LIQUIBASE_CHANGELOG_PATH);
        liquibase.setShouldRun(true);

        return liquibase;
    }
    

    /**
     * Create a source datasource from JDBC URL, username, password manually
     * (i.e: source database).
     *
     * @return
     * @throws java.lang.Exception
     */
    @Bean
    @Conditional(MigrationBeanApplicationConfiguration.class)
    public DataSource sourceDataSource() throws Exception {
        // If user doesn't use postgresql by default
        if (!ConfigManager.SOURCE_DATASOURCE_URL.contains(ConfigManager.DEFAULT_DMBS)) {
            try {
                addJDBCDriverToClassPath(ConfigManager.SOURCE_DATASOURCE_JDBC_JAR_PATH, ConfigManager.SOURCE_DATASOURCE_URL);
            } catch (PetascopeException ex) {
                throw new PetascopeException(ExceptionCode.InvalidPropertyValue,
                        "JDBC driver jar file path for current DBMS configured in petascope.properties with key '"
                        + ConfigManager.KEY_SOURCE_DATASOURCE_JDBC_JAR_PATH + "', given value '"
                        + ConfigManager.SOURCE_DATASOURCE_JDBC_JAR_PATH + "' is not valid.",
                        ex);
            }
        }

        DataSource dataSource = DataSourceBuilder.create().url(ConfigManager.SOURCE_DATASOURCE_URL)
                .username(ConfigManager.SOURCE_DATASOURCE_USERNAME)
                .password(ConfigManager.SOURCE_DATASOURCE_PASSWORD)
                .build();

        try {
            DatabaseUtil.checkJDBCConnection(ConfigManager.SOURCE_DATASOURCE_URL,
                    ConfigManager.SOURCE_DATASOURCE_USERNAME, ConfigManager.SOURCE_DATASOURCE_PASSWORD);
        } catch (Exception ex) {
            log.error("Cannot migrate petascopedb by source datasource with JDBC URL '" + ConfigManager.SOURCE_DATASOURCE_URL + "'."
                    + "\n Reason: " + ex.getMessage());
            System.exit(ExitCode.FAILURE.getExitCode());
        }

        if (DatabaseUtil.petascopeDatabaseEmpty(dataSource)) {
            // NOTE: petascopedb exists but doesn't have any tables to migrate, just consider it is a success instead of failure.
            log.info("petascopedb is empty, nothing to migrate.");
            System.exit(ExitCode.SUCCESS.getExitCode());
        }

        return dataSource;
    }

    /**
     * Create a target datasource from JBDC URL, username, password manually
     * (i.e: target database).
     *
     * @return
     * @throws java.lang.ClassNotFoundException
     * @throws petascope.exceptions.PetascopeException
     * @throws java.sql.SQLException
     */
    @Bean
    @Primary
    @Conditional(MigrationBeanApplicationConfiguration.class)
    public DataSource targetDataSource() throws Exception {

        // If user doesn't use postgresql by default
        if (!ConfigManager.PETASCOPE_DATASOURCE_URL.contains(ConfigManager.DEFAULT_DMBS)) {
            try {
                addJDBCDriverToClassPath(ConfigManager.PETASCOPE_DATASOURCE_JDBC_JAR_PATH, ConfigManager.PETASCOPE_DATASOURCE_URL);
            } catch (PetascopeException ex) {
                throw new PetascopeException(ExceptionCode.InvalidPropertyValue,
                        "JDBC driver jar file path for target DBMS configured in petascope.properties with key '"
                        + ConfigManager.KEY_PETASCOPE_DATASOURCE_JDBC_JAR_PATH + "', given value '"
                        + ConfigManager.PETASCOPE_DATASOURCE_JDBC_JAR_PATH + "' is not valid.",
                        ex);
            }
        }

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        String dataSourceURL = ConfigManager.PETASCOPE_DATASOURCE_URL;

        // NOTE: if legacy petascopedb exists, *must* create a temp database for Liquibase to populate tables to migrate first
        // then when the migration is done, rename the temp database back to petascopedb and the legacy petascopedb to petascopedb_94_backup
        if (DatabaseUtil.sourceLegacyPetascopeDatabaseExists()) {
            String tempDatabaseName = LEGACY_PETASCOPEDB_MIGRATION_TEMP;
            DatabaseUtil.createPostgresqlDatabaseIfNotExist(tempDatabaseName);
            log.info("petascopedb 9.4 or older already exists, creating a temporary backup database '"
                    + LEGACY_PETASCOPEDB_MIGRATION_TEMP + "' for it.");

            // jdbc:postgresql://localhost:5432/petascopedb_migration_temp
            String tempDataSourceURL = ConfigManager.PETASCOPE_DATASOURCE_URL + LegacyMigrationService.LEGACY_PETASCOPEDB_MIGRATION_TEMP_POSTFIX;
            dataSourceURL = tempDataSourceURL;
        }

        dataSourceBuilder.url(dataSourceURL);
        // spring.datasource.* in petascope.properties
        dataSourceBuilder.username(ConfigManager.PETASCOPE_DATASOURCE_USERNAME);
        dataSourceBuilder.password(ConfigManager.PETASCOPE_DATASOURCE_PASSWORD);

        try {
            DatabaseUtil.checkJDBCConnection(ConfigManager.PETASCOPE_DATASOURCE_URL,
                    ConfigManager.PETASCOPE_DATASOURCE_USERNAME, ConfigManager.PETASCOPE_DATASOURCE_PASSWORD);
        } catch (Exception ex) {
            log.error("Cannot migrate petascopedb to target datasource with JDBC URL '" + ConfigManager.SOURCE_DATASOURCE_URL + "'."
                    + "\n Reason: " + ex.getMessage());
            System.exit(ExitCode.FAILURE.getExitCode());
        }

        // NOTE: must use like this or with 2 different EntityManagers, Liquibase will not run to populate schema to target datasource.
        migrationLiquibase(dataSourceURL);
        return dataSourceBuilder.build();
    }

    /**
     * Create a bean object to manage the database connection for legacy
     * petascopedb prior version 9.5.
     *
     * @return
     * @throws java.lang.Exception
     */
    @Bean
    public LegacyDbMetadataSource dbMetadataSource() throws Exception {
        // Used to support the translation from legacy coverage metadata to new CIS coverage type
        // This bean is initialized by Spring and we could use it when possible
        // NOTE: LegacyMigrationService uses this bean only.
        LegacyDbMetadataSource meta = null;
        if (DatabaseUtil.sourceLegacyPetascopeDatabaseExists()) {
            meta = new LegacyDbMetadataSource(ConfigManager.POSTGRESQL_DATASOURCE_DRIVER,
                    ConfigManager.SOURCE_DATASOURCE_URL,
                    ConfigManager.SOURCE_DATASOURCE_USERNAME,
                    ConfigManager.SOURCE_DATASOURCE_PASSWORD, false);
        }

        return meta;
    }

    @Bean
    @Conditional(MigrationBeanApplicationConfiguration.class)
    public EntityManager sourceEntityManager() throws Exception {
        return sourceEntityManagerFactory().createEntityManager();
    }

    @Bean
    @Conditional(MigrationBeanApplicationConfiguration.class)
    public EntityManager targetEntityManager() throws Exception {
        return entityManagerFactory().createEntityManager();
    }

    @Bean
    @Conditional(MigrationBeanApplicationConfiguration.class)
    public EntityManagerFactory sourceEntityManagerFactory() throws Exception {
        return createEntityManagerFactory(SOURCE, this.sourceDataSource());
    }

    @Bean
    @Primary
    @Conditional(MigrationBeanApplicationConfiguration.class)
    public EntityManagerFactory entityManagerFactory() throws Exception {
        return createEntityManagerFactory(TARGET, this.targetDataSource());
    }

    @Bean(name = SOURCE_TRANSACTION_MANAGER)
    @Conditional(MigrationBeanApplicationConfiguration.class)
    // Read to source database, use source transaction manager
    public PlatformTransactionManager sourceTransactionManager() throws Exception {
        return new JpaTransactionManager(sourceEntityManagerFactory());
    }

    @Bean
    @Primary
    @Conditional(MigrationBeanApplicationConfiguration.class)
    // Read to target database, use the default transaction manager 
    // (why default? because target datasource is more important to read/save with JPA repositories).
    // while source datasource is only used for read (which uses Hibernate HQL).
    public PlatformTransactionManager transactionManager() throws Exception {
        return new JpaTransactionManager(entityManagerFactory());
    }

    /**
     * Create the EntityManagerFactory object based on the datasource input
     * (source / target).
     *
     * @param persistenceUnitName
     * @param dataSource
     * @return
     */
    private EntityManagerFactory createEntityManagerFactory(final String persistenceUnitName, DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        JpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter);
        entityManagerFactory.setPackagesToScan(BASE_PACKAGE);
        entityManagerFactory.setPersistenceUnitName(persistenceUnitName);

        final Properties properties = new Properties();

        // Enable Hibernate log for debugging SQL queries
        //properties.setProperty("hibernate.show_sql", "true");
        //properties.setProperty("hibernate.format_sql", "true");
        // NOTE: Only populate schema on target datasource for ***testing***) 
        // if (persistenceUnitName.equals(TARGET)) {
        // as spring.jpa.hibernate.ddl-auto=create in petascope.properties has no useage when having different datasources.
        // properties.setProperty("hibernate.hbm2ddl.auto", "create");
        // }
        // NOTE: When configuring EntityManagerFactory manually, must config Hibernate 5 to use the naming strategies or the rasql query can be mixed up with lower case, upper case and camel case.
        properties.setProperty(HIBERNATE_IMPLICIT_NAMING_STRATEGY_KEY, HIBERNATE_IMPLICIT_NAMING_STRATEGY_VALUE);
        properties.setProperty(HIBERNATE_PHYSICAL_NAMING_STRATEGY_KEY, HIBERNATE_PHYSICAL_NAMING_STRATEGY_VALUE);
        entityManagerFactory.setJpaProperties(properties);

        entityManagerFactory.afterPropertiesSet();

        return entityManagerFactory.getObject();
    }
    
    @Override
    // NOTE: only when running java -jar rasdaman.war --migrate, it will need to create
    // the beans which have annotaion @Conditional which match this condition.
    public boolean matches(ConditionContext cc, AnnotatedTypeMetadata atm) {
        if (ApplicationMain.MIGRATE) {
            return true;
        } else {
            return false;
        }
    }
}
