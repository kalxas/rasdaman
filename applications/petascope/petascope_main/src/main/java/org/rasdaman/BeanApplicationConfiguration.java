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
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import petascope.controller.AbstractController;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.DatabaseUtil;

/**
 * This class initializes the bean which needs the passing dependencies from
 * properties file
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Configuration
public class BeanApplicationConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BeanApplicationConfiguration.class);

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/*").allowedOrigins("*");
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

    @Bean
    public SpringLiquibase liquibase() throws Exception {
        SpringLiquibase liquibase = new SpringLiquibase();
        // NOTE: Do not initialize/update petascopedb if petascope failed to start properly.
        if (AbstractController.startException != null) {
            liquibase.setShouldRun(false);
            return liquibase;
        }
        
        try {
            // Create the new petascopedb database if not exist
            DatabaseUtil.createDatabaseIfNotExist(null);
        } catch (Exception ex) {
            // e.g: postgresql is not running
            PetascopeException petascopeException = new PetascopeException(ExceptionCode.InternalSqlError, 
                    "Cannot connect to petascopedb via base DMBS, error '" + ex.getMessage() + "'.", ex);
            log.error(petascopeException.getExceptionText(), petascopeException);
            AbstractController.startException = ex;
            liquibase.setShouldRun(false);
            return liquibase;
        }
        
        String datasourceUrl = ConfigManager.PETASCOPE_DATASOURCE_URL;
        DataSource dataSource = DataSourceBuilder.create().url(datasourceUrl)
                .username(ConfigManager.PETASCOPE_DATASOURCE_USERNAME)
                .password(ConfigManager.PETASCOPE_DATASOURCE_PASSWORD).build();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:database_versions/db.changelog-master.xml");
        // NOTE: Don't populate new schema from petascopedb9.5 to petascopedb9.4, return error later to client via controller also
        if (DatabaseUtil.legacyPetascopeDatabaseExists()) {
            String errorMessage = "petascopedb 9.4 or older already exists, "
                    + "please run the migrate_petascopedb.sh script to migrate to the new petascope schema first, then restart petascope.";
            PetascopeException exception = new PetascopeException(ExceptionCode.InternalSqlError, errorMessage);
            AbstractController.startException = exception;
            log.error(errorMessage, exception);
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(true);
        }

        return liquibase;
    }
}
