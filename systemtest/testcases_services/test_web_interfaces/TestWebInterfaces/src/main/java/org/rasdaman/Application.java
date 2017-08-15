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
  *  Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import static org.rasdaman.Config.PATH_TO_PHANTOMJS_FILE;
import org.rasdaman.secore.SecoreBrowseCRSMetadataTest;
import org.rasdaman.secore.SecoreCompare2CRSsMetadataTest;
import org.rasdaman.secore.SecoreDeleteCRSMetadataTest;
import org.rasdaman.secore.SecoreExecuteXQueryTest;
import org.rasdaman.secore.SecoreGetCRSMetadataTest;
import org.rasdaman.ws_client.WCSDeleteCoverageTest;
import org.rasdaman.ws_client.WCSDescribeCoverageTest;
import org.rasdaman.ws_client.WCSGetCapabilitiesTest;
import org.rasdaman.ws_client.WCSGetCoverageTest;
import org.rasdaman.ws_client.WCSInsertCoverageTest;
import org.rasdaman.ws_client.WCSProcessCoverageTest;

/**
 * Main class to start the test
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class Application {

    final static Logger log = Logger.getLogger(Application.class);

    public static void main(String args[]) {
                        
        // Read the input command arguments for petascope port, secore port in test.cfg file
        Config.PETASCOPE_PORT = args[0];
        Config.SECORE_PORT = args[1];
        // Initialize context paths to Petascope and Secore web applications
        Config config = new Config();

        // No test failed, so the test is done
        int exitCode = 0;
        try {
            log.debug("Phantomjs full path at /tmp directory '" + PATH_TO_PHANTOMJS_FILE + "'.");

            // For PhantomJS
            // Path to the binary to run web browser
            System.setProperty("phantomjs.binary.path", PATH_TO_PHANTOMJS_FILE);
            DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
            capabilities.setJavascriptEnabled(true);
            String[] phantomArgs = new String[]{"--webdriver-loglevel=NONE"};
            // NOTE: Disable all the logs from phantomjs ( phantomjs://platform/console++.js)
            capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
            WebDriver webDriver = new PhantomJSDriver(capabilities);
            webDriver.manage().window().setSize(new Dimension(800, 600));

            // List of test classes
            List<AbstractWebPageTest> webPageTests = new ArrayList<>();
            webPageTests.add(new WCSGetCapabilitiesTest());
            webPageTests.add(new WCSDescribeCoverageTest());
            webPageTests.add(new WCSGetCoverageTest());
            webPageTests.add(new WCSProcessCoverageTest());
            webPageTests.add(new WCSInsertCoverageTest());
            webPageTests.add(new WCSDeleteCoverageTest());

            // Test SECORE
            webPageTests.add(new SecoreGetCRSMetadataTest());
            webPageTests.add(new SecoreCompare2CRSsMetadataTest());
            webPageTests.add(new SecoreBrowseCRSMetadataTest());
            webPageTests.add(new SecoreDeleteCRSMetadataTest());
            webPageTests.add(new SecoreExecuteXQueryTest());

            log.info("----------------------- Test Start ------------------");

            // Run each test web page
            for (AbstractWebPageTest webPageTest : webPageTests) {
                webPageTest.runTest(webDriver);
            }

            log.debug("Close the web browser.");
            webDriver.close();
            webDriver.quit();

            // Check if any test returns error
            for (AbstractWebPageTest webPageTest : webPageTests) {
                if (webPageTest.hasErrorTest()) {
                    exitCode = 1;
                    break;
                }
            }

            log.info("----------------------- Test Done ------------------");
        } catch (Exception ex) {
            log.info("The test process failed with an unexpected exception '" + ex.getMessage() + "'. ", ex);
            exitCode = 1;
        }

        // Return the code to the console
        System.exit(exitCode);
    }
}
