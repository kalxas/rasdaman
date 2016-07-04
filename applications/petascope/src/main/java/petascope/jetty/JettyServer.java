package petascope.jetty;

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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/


import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Properties;

public class JettyServer {

    private static final String PORT_KEY = "jetty_port";
    private static final String DIRECTORY_PATH = "jetty_extracted_path";
    private static final String RASDAMAN_CONTEXT_PATH = "/rasdaman";
    private static final String SECORE_CONTEXT_PATH = "/def";
    private final String petascopePropertiesFilePath;
    private final String petascopeWarPath;
    private final String secoreWarPath;

    /**
     * Class representing the jetty server
     *
     * @param petascopePropertiesFilePath path to the petascope properties file
     * @param petascopeWarPath            path to the petascope war
     * @param secoreWarPath               path to the secore war
     */
    private JettyServer(String petascopePropertiesFilePath, String petascopeWarPath, String secoreWarPath) {
        this.petascopePropertiesFilePath = petascopePropertiesFilePath;
        this.petascopeWarPath = petascopeWarPath;
        this.secoreWarPath = secoreWarPath;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            reportError("Please provide the path to the petascope.properties file, the path to the rasdaman.war file and" +
                    " the path to the secore def.war file as command line parameters in this specific order.");
        }
        JettyServer jettyServer = new JettyServer(args[0], args[1], args[2]);
        jettyServer.loadJettyConfiguration();
        jettyServer.start();
    }

    /**
     * Reports an error and exits gracefully
     *
     * @param error the error to report
     */
    private static void reportError(String error) {
        System.err.println(error);
        System.exit(1);
    }

    /**
     * Ensures a directory exists
     *
     * @param directoryPath the path to the directory
     * @return a file handle to it
     */
    private File ensureDirectoryExists(String directoryPath) {
        File theDir = new File(directoryPath);
        if (!theDir.exists()) {
            try {
                theDir.mkdir();
            } catch (SecurityException se) {
                reportError("Could not initialize extracted war directory at path " + directoryPath + " due to invalid permissions.");
            }
        }
        return theDir;
    }

    /**
     * Generates the jetty application context for a specific war file
     *
     * @param configuration the configuration for the jetty
     * @param warFilePath   the path to the war file
     * @param contextPath   the context path to the server
     * @return the web app context
     */
    private WebAppContext generateAppContext(JettyConfiguration configuration, String warFilePath, String contextPath) {
        String rasdamanExtractedWarDirectoryPath = configuration.getExtractedWarDirectory() + contextPath;
        WebAppContext webApp = new WebAppContext();
        webApp.setContextPath(contextPath);
        File warFileRasdaman = new File(warFilePath);

        if (!warFileRasdaman.exists()) {
            reportError("Invalid path to war file: " + warFilePath);
        }
        webApp.setWar(warFileRasdaman.getAbsolutePath());
        webApp.setTempDirectory(ensureDirectoryExists(rasdamanExtractedWarDirectoryPath));
        return webApp;
    }


    /**
     * Starts the jetty server
     *
     * @throws Exception
     */
    private void start() throws Exception {
        JettyConfiguration configuration = loadJettyConfiguration();

        Server server = new Server(configuration.getPort());
        MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbContainer);

        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(generateAppContext(configuration, petascopeWarPath, RASDAMAN_CONTEXT_PATH));
        handlers.addHandler(generateAppContext(configuration, secoreWarPath, SECORE_CONTEXT_PATH));

        server.setHandler(handlers);
        server.start();
    }

    /**
     * Loads the jetty configuration from the petascope properties
     *
     * @return a jetty configuration object
     * @throws IOException
     */
    private JettyConfiguration loadJettyConfiguration() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(petascopePropertiesFilePath)));
        int port = -1;

        try {
            port = Integer.parseInt(properties.getProperty(PORT_KEY));
        } catch (NumberFormatException ex) {
            reportError("Please provide a valid integer port in petascope.properties for key " + PORT_KEY);
        }

        String extractedDirectoryPath = properties.getProperty(DIRECTORY_PATH);
        if (extractedDirectoryPath == null || (!new File(extractedDirectoryPath).exists())) {
            reportError("Please provide a valid directory path for extracting the rasdaman war files in petascope.properties for key " + DIRECTORY_PATH);
        }

        return new JettyConfiguration(port, extractedDirectoryPath);
    }

    private static class JettyConfiguration {
        private int port;
        private String extractedWarDirectory;

        /**
         * Configuration for external server using jetty
         *
         * @param port                  the port to which the jetty server should bind
         * @param extractedWarDirectory the directory to which to extract the war files of rasdaman
         */
        public JettyConfiguration(int port, String extractedWarDirectory) {
            this.port = port;
            this.extractedWarDirectory = extractedWarDirectory;
        }

        /**
         * Returns the port to which jetty should bind
         *
         * @return
         */
        public int getPort() {
            return port;
        }

        /**
         * Returns the directory to which to extract the war
         *
         * @return the directory path where the war files are extracted
         */
        public String getExtractedWarDirectory() {
            return extractedWarDirectory;
        }
    }
}
