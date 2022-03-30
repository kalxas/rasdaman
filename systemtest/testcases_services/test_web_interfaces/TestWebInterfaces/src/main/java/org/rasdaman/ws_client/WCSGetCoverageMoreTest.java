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
package org.rasdaman.ws_client;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * Class to test wcs_client, tab WCS/GetCapabilities
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WCSGetCoverageMoreTest extends WCSGetCoverageTest {

    private static final Logger log = Logger.getLogger(WCSGetCapabilitiesTest.class);

    public WCSGetCoverageMoreTest() {
        super();
        this.sectionName = "wcs_get_coverage_more";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");
        
        this.waitForPageLoad(webDriver);
        
        // First, change to tab GetCoverage
        this.clickOnElement(webDriver, "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/ul/li[3]/a");

        String testCaseName;
        Select dropdown;
        
        // First change the coverage id in text box
        this.addTextToTextBox(webDriver, "test_mean_summer_airtemp", "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[3]/div/div/div[1]/div[1]/div/input");

        String selectCoverageButtonXPath = "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[3]/div/div/div[1]/div[1]/div/span[2]/button";
        // Then click on the Get Coverage button
        this.runTestByClickingOnElementWithoutComparingOracle(webDriver, "", selectCoverageButtonXPath);
        
        // --------- Real tests -----------------
        
        // Download a whole coverage in PNG (GML cannot be captured by PhantomJS)
        testCaseName = this.getSectionTestCaseName("get_whole_2D_coverage_in_png");
        log.info("Testing get whole coverage with encoding as 2D PNG...");
        // Then select coverage as png
        dropdown = new Select(webDriver.findElement(By.xpath("//*[@id=\"select-coverage-format\"]")));
        dropdown.selectByVisibleText("image/png");
        // Then click on the Download Coverage button which will open a new window
        this.runTestByClickingOnElementAndCaptureTheOpenedWindow(webDriver, testCaseName, downloadCoverageButtonXPath);

        // Refocus on the tab
        this.focusOnGetCoverageTab(webDriver);
        
        String minLatXPath = "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[3]/div/div/div[1]/div[4]/div[2]/uib-accordion/div/div/div[2]/div/div/div[2]/div/div[1]/div/div[2]/ul/li[1]/input[3]";
        String minLongXPath = "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[3]/div/div/div[1]/div[4]/div[2]/uib-accordion/div/div/div[2]/div/div/div[2]/div/div[2]/div/div[2]/ul/li[1]/input[3]";

        // Download a subset coverage in JSON
        testCaseName = this.getSectionTestCaseName("get_subset_2D_coverage_in_json");
        log.info("Testing get subset coverage with encoding as 2D JSON...");
        // Then select coverage as png
        dropdown = new Select(webDriver.findElement(By.xpath("//*[@id=\"select-coverage-format\"]")));
        dropdown.selectByVisibleText("application/json");
        // Then subset on Lat axis (min lat)
        this.addTextToTextBox(webDriver, "-10.5", minLatXPath);
        // Then subset on Lon axis (min Lon)
        this.addTextToTextBox(webDriver, "154.275", minLongXPath);
        // Then click on the Download Coverage button which will open a new window
        this.runTestByClickingOnElementAndCaptureTheOpenedWindow(webDriver, testCaseName, downloadCoverageButtonXPath);

        // ******************** Test HTTP POST request ***********************
        // Refocus on the tab
        this.focusOnGetCoverageTab(webDriver);
        // then change to POST HTTP request
        dropdown = new Select(webDriver.findElement(By.xpath("//*[@id=\"selectHTTPRequest\"]")));
        dropdown.selectByVisibleText("POST");

        testCaseName = this.getSectionTestCaseName("get_subset_2D_coverage_in_png_http_post");
        log.info("Testing get subset coverage with encoding as 2D PNG in HTTP POST...");
        // Then select coverage as png
        dropdown = new Select(webDriver.findElement(By.xpath("//*[@id=\"select-coverage-format\"]")));
        dropdown.selectByVisibleText("image/png");
        // Then subset on Lat axis (min lat)
        this.addTextToTextBox(webDriver, "-20.5", minLatXPath);
        // Then subset on Lon axis (min Lon)
        this.addTextToTextBox(webDriver, "135.5", minLongXPath);
        // Then click on the Download Coverage button which will open a new window
        this.runTestByClickingOnElementAndCaptureTheOpenedWindow(webDriver, testCaseName, downloadCoverageButtonXPath);


    }
}
