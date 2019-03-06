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
import org.openqa.selenium.WebDriver;

/**
 * Class to test wcs_client, tab WCS/DescribeCoverage
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WCSDescribeCoverageTest extends WSAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(WCSDescribeCoverageTest.class);

    public WCSDescribeCoverageTest() {
        super();
        this.sectionName = "wcs_describe_coverage";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");

        // Switch to iframe to parse the web element
        this.switchToIFirstIframe(webDriver);

        String testCaseName;

        // First, change to tab DescribeCoverage
        testCaseName = this.getSectionTestCaseName("change_to_describe_coverage_tab");
        log.info("Testing change current tab to DescribeCoverage...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/ul/li[2]/a");

        String coverageIdTextBoxXPath = "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/div/div[2]/div/div/div/div[1]/div/input";
        String describeCoverageXPath = "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/div/div[2]/div/div/div/div[1]/div/span[2]/button";
        
        // Describe a rectified coverage
        testCaseName = this.getSectionTestCaseName("describe_a_rectified_grid_coverage");
        log.info("Testing describe a rectified grid coverage...");
        // First change the coverage id in text box
        this.addTextToTextBox(webDriver, "test_mean_summer_airtemp", coverageIdTextBoxXPath);
        // Then click on the Describe Coverage button
        this.runTestByClickingOnElement(webDriver, testCaseName, describeCoverageXPath);

        // Describe a referenceable rectified coverage
        testCaseName = this.getSectionTestCaseName("describe_a_referenceable_rectified_grid_coverage");
        log.info("Testing describe a referenceable rectified grid coverage...");
        // First change the coverage id in text box
        this.addTextToTextBox(webDriver, "test_irr_cube_2", coverageIdTextBoxXPath);
        // Then click on the Describe Coverage button
        this.runTestByClickingOnElement(webDriver, testCaseName, describeCoverageXPath);

        // Describe a grid coverage
        testCaseName = this.getSectionTestCaseName("describe_a_grid_coverage");
        log.info("Testing describe a grid coverage...");
        // First change the coverage id in text box
        this.addTextToTextBox(webDriver, "test_mr", coverageIdTextBoxXPath);
        // Then click on the Describe Coverage button
        this.runTestByClickingOnElement(webDriver, testCaseName, describeCoverageXPath);

        // Click on a DescribeCoverage GML dropdown button
        testCaseName = this.getSectionTestCaseName("click_on_coverage_descriptions_dropdown_button");
        log.info("Testing click on GML coverage descriptions dropdown button...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/div/div[2]/div/div/div/div[5]/uib-accordion/div/div/div[1]/h4/a/span/i");
    }

}
