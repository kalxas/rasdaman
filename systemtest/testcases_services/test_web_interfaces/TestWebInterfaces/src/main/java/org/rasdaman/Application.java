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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.rasdaman.admin.AdminOWSMetadataManagementTest;
import org.rasdaman.secore.SecoreBrowseCRSMetadataTest;
import org.rasdaman.secore.SecoreCompare2CRSsMetadataTest;
import org.rasdaman.secore.SecoreDeleteCRSMetadataTest;
import org.rasdaman.secore.SecoreExecuteXQueryTest;
import org.rasdaman.secore.SecoreGetCRSMetadataTest;
import org.rasdaman.secore.SecoreLogoutTest;
import org.rasdaman.ws_client.WCSDeleteCoverageTest;
import org.rasdaman.ws_client.WCSDescribeCoverageTest;
import org.rasdaman.ws_client.WCSGetCapabilitiesTest;
import org.rasdaman.ws_client.WCSGetCoverageMoreTest;
import org.rasdaman.ws_client.WCSGetCoverageTest;
import org.rasdaman.ws_client.WCSInsertCoverageTest;
import org.rasdaman.ws_client.WCSProcessCoverageTest;
import org.rasdaman.ws_client.WMSDescribeLayerTest;
import org.rasdaman.ws_client.WMSGetCapabilitiesTest;

/**
 * Main class to start the test
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class Application {

    final static Logger log = Logger.getLogger(Application.class);

    public static void main(String args[]) throws IOException {
        // Read the input command arguments for petascope port, secore port in test.cfg file
        Config.PETASCOPE_PORT = args[0];
        Config.SECORE_PORT = args[1];
        
        Config config = new Config();
        config.init();

        // No test failed, so the test is done
        int exitCode = 0;
        
        long startTime = System.currentTimeMillis();
        
        List<WebDriver> webDrivers = new ArrayList<>();
        
        try {

            // TEST WCS
            final List<AbstractWebPageTest> wcsTests1 = new ArrayList<>();
            wcsTests1.add(new WCSGetCoverageTest());
            
            final List<AbstractWebPageTest> wcsTests2 = new ArrayList<>();
            wcsTests2.add(new WCSGetCoverageMoreTest());
            
            final List<AbstractWebPageTest> wcsTests3 = new ArrayList<>();
            wcsTests3.add(new WCSGetCapabilitiesTest());
            wcsTests3.add(new WCSDescribeCoverageTest());
            
            final List<AbstractWebPageTest> wcsTests4 = new ArrayList<>();
            // TEST Admin (NOTE: need to login to admin tab before it shows InsertCoverage and DeleteCoverage tabs)
            wcsTests4.add(new AdminOWSMetadataManagementTest());
            wcsTests4.add(new WCSInsertCoverageTest());
            wcsTests4.add(new WCSDeleteCoverageTest());
            
            final List<AbstractWebPageTest> wcsTests5 = new ArrayList<>();
            wcsTests5.add(new WCSProcessCoverageTest());
            
            
            // TEST WMS
            final  List<AbstractWebPageTest> wmsTests = new ArrayList<>();
            wmsTests.add(new WMSGetCapabilitiesTest());
            wmsTests.add(new WMSDescribeLayerTest());
            
            // Test SECORE
            List<AbstractWebPageTest> secoreTests = new ArrayList<>();
            secoreTests.add(new SecoreGetCRSMetadataTest());
            secoreTests.add(new SecoreCompare2CRSsMetadataTest());
            secoreTests.add(new SecoreBrowseCRSMetadataTest());
            secoreTests.add(new SecoreExecuteXQueryTest());
            secoreTests.add(new SecoreLogoutTest());
            
            log.info("----------------------- Test Start ------------------");

            // Run each test web page
            
            List<List<AbstractWebPageTest>> testsList = Arrays.asList(
                                                    wcsTests1, wcsTests2, wcsTests3, wcsTests4, wcsTests5,
                                                    wmsTests,
                                                    secoreTests
            );
            
            runTestsInParallel(testsList);
            
            // ------------------------- Check result -------------------------
            
            List<AbstractWebPageTest> webPageTests = new ArrayList<>();
            for (List<AbstractWebPageTest> tests : testsList) {
                webPageTests.addAll(tests);
            }
            
            List<String> failedTests = new ArrayList<>();

            // Check if any test returns error
            for (AbstractWebPageTest webPageTest : webPageTests) {
                if (webPageTest.hasErrorTest()) {
                    exitCode = 1;
                    failedTests.add(webPageTest.getSectionName());
                }
            }

            log.info("----------------------- Test Done ------------------");
            
            
            if (exitCode == 1) {
                log.info("List of failed tests:");
                for (String failedTest : failedTests) {
                    log.info(failedTest);
                }
            }
        } catch (Exception ex) {
            log.info("The test process failed with an unexpected exception '" + ex.getMessage() + "'. ", ex);
            exitCode = 1;
        } finally {
            for (WebDriver webDriver : webDrivers) {
                webDriver.close();
                webDriver.quit();
            }
            
            long endTime = System.currentTimeMillis();
            log.info("Time to run all tests is: " + ((endTime - startTime) / 1000) + " s.");
            
            // Return the code to the console
            System.exit(exitCode);
        }
        
    }
    
    private static List<WebDriver> runTestsInParallel(List<List<AbstractWebPageTest>> inputTestsList) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        List<WebDriver> webDrivers = new ArrayList<>();
        
        for (List<AbstractWebPageTest> testsList : inputTestsList) {
            WebDriver webDriver = createWebDriver();
            webDrivers.add(webDriver);
            
            Thread thread = createThread(webDriver, testsList);
            threads.add(thread);
        }
        
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        
        return webDrivers;
    }
    
    private static WebDriver createWebDriver() {
        System.setProperty("webdriver.gecko.driver", Config.PATH_TO_GECKO_DRIVER);
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary(Config.PATH_TO_FIREFOX);
        // no GUI
        options.setHeadless(true);
        options.setLogLevel(FirefoxDriverLogLevel.FATAL);
        WebDriver webDriver = new FirefoxDriver(options);
        webDriver.manage().window().setSize(new Dimension(1024, 768));
        
        return webDriver;
    }
    
    private static Thread createThread(final WebDriver webDriver, final List<AbstractWebPageTest> webpageTests) {
        Thread thread = new Thread() {
            public void run() {
                for (AbstractWebPageTest webPageTest : webpageTests) {
                    try {
                        webPageTest.runTest(webDriver);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to run test: "+ webPageTest.getSectionName() + ". Reason: " + ex.getMessage(), ex);
                    }
                }
            }
        };
        
        return thread;
    }
}
