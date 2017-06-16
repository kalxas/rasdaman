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
import org.rasdaman.config.ConfigManager;
import org.rasdaman.migration.domain.legacy.LegacyDbMetadataSource;
import org.rasdaman.migration.service.LegacyMigrationService;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
/**
 * This class initializes the bean which needs the passing dependencies from
 * properties file
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class BeanApplicationConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/*").allowedOrigins("*");
            }
        };
    }

    // @TODO: remove this one when rasdaman installer changes from updating in legacy configurations for username, password to the new ones
    /**
     * Instead of reading properties automatically from petascope.properties,
     * use this one to read from legacy configuration due to rasdaman installer
     * creates password differently for each installation for username rasdaman
     * in postgresql and it only knows about legacy configurations.
     *
     * @return
     */
    @Bean
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(ConfigManager.PETASCOPE_DATASOURCE_URL);
        dataSourceBuilder.username(ConfigManager.LEGACY_DATASOURCE_USERNAME);
        dataSourceBuilder.password(ConfigManager.LEGACY_DATASOURCE_PASSWORD);
        return dataSourceBuilder.build();
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
    public LegacyDbMetadataSource dbMetadataSource() throws Exception {
        // Used to support the translation from legacy coverage metadata to new CIS coverage type
        // This bean is initialized by Spring and we could use it when possible
        // NOTE: Only create this beans if petascopedb existed in postgresql
        // It does not exist since version 9.5 with new installtion.       
        LegacyDbMetadataSource meta = null;
        if (LegacyMigrationService.checkLegacyDatabaseExist()) {
            meta = new LegacyDbMetadataSource(ConfigManager.LEGACY_DATASOURCE_DRIVER,
                    ConfigManager.LEGACY_DATASOURCE_URL,
                    ConfigManager.LEGACY_DATASOURCE_USERNAME,
                    ConfigManager.LEGACY_DATASOURCE_PASSWORD, false);
        }

        return meta;
    }
}
