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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import org.rasdaman.secore.db.DbManager;
import org.rasdaman.secore.db.DbSecoreVersion;
import org.rasdaman.secore.ConfigManager;
import static org.rasdaman.secore.ConfigManager.KEY_JAVA_SERVER;
import static org.rasdaman.secore.ConfigManager.SECORE_PROPERTIES_FILE;
import static org.rasdaman.secore.ConfigManager.VALUE_JAVA_SERVER_EXTERNAL;
import org.rasdaman.secore.handler.AbstractHandler;
import org.rasdaman.secore.util.ExceptionCode;
import org.rasdaman.secore.util.SecoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
@ComponentScan({"org.rasdaman"})
// NOTE: classpath is important when running as war package or it will have error resource not found
@PropertySource({"classpath:application.properties"})
/**
 * Main class to start Spring boot
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class ApplicationMain extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMain.class);
    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";
    private static final String KEY_SECORE_CONF_DIR = "secore.confDir";

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() throws SecoreException, IOException {
        Properties properties = new Properties();
        InputStream resourceStream = new ClassPathResource(APPLICATION_PROPERTIES_FILE).getInputStream();
        properties.load(resourceStream);

        PropertySourcesPlaceholderConfigurer propertyResourcePlaceHolderConfigurer = new PropertySourcesPlaceholderConfigurer();
        File initialFile = new File(properties.getProperty(KEY_SECORE_CONF_DIR) + "/" + ConfigManager.SECORE_PROPERTIES_FILE);
        propertyResourcePlaceHolderConfigurer.setLocation(new FileSystemResource(initialFile));

        String confDir = properties.getProperty(KEY_SECORE_CONF_DIR);
        String confFile = confDir + "/" + SECORE_PROPERTIES_FILE;
        
        Properties props = new Properties();

        try {        
            File file = new File(confFile);
            InputStream is = new FileInputStream(file);            
            props.load(is);
        } catch (Exception ex) {
            throw new SecoreException(ExceptionCode.InternalComponentError, "Cannot load properties from properties file '" + confFile + ", reason: " + ex.getMessage(), ex);
        }
        
        // NOTE: only have effect when running def.war separately from petascope
        String value = props.getProperty(KEY_JAVA_SERVER);
        boolean embedded = true;
        if (value != null && value.trim().equals(VALUE_JAVA_SERVER_EXTERNAL)) {
            embedded = false;
        } else if (value == null) {
            log.warn(KEY_JAVA_SERVER + " setting does not exist in properties file '" + confFile + "', internal mode is selected.");
        }
        
        // NOTE: this class is not touched when SECORE runs as embedded library in petascope (!)
        
        try {
            ConfigManager.initInstance(confDir, embedded, null);
            //  Create (first time load) or Get the BaseX database from caches.
            DbManager dbManager = DbManager.getInstance();

            // NOTE: we need to check current version of Secoredb first, if it is not latest, then run the update definition files with the current version to the newest versionNumber from files.
            // in $RMANHOME/share/rasdaman/secore.
            // if current version of Secoredb is empty then add SecoreVersion element to BaseX database and run all the db_updates files.
            DbSecoreVersion dbSecoreVersion = new DbSecoreVersion(dbManager.getDb());
            dbSecoreVersion.handle();
            
            log.debug("Initialze BaseX dbs successfully.");
        } catch (SecoreException ex) {
            throw new SecoreException(ExceptionCode.InternalComponentError, "Cannot initialize database manager", ex);
        } catch (IOException ex) {
            throw new SecoreException(ExceptionCode.InternalComponentError, "Cannot update SECORE version from files", ex);
        }

        return propertyResourcePlaceHolderConfigurer;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // This one will be invoked only when running in web application container (i.e: not as jar file)
        return builder.sources(ApplicationMain.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApplicationMain.class, args);
    }
}
