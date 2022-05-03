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
package org.rasdaman.admin;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.rasdaman.Config;

/**
 * Open Administration tab in WSClient to login and update OWS metadata to petascopedb.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class AdminOWSMetadataManagementTest extends AdminAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(AdminOWSMetadataManagementTest.class);

    public AdminOWSMetadataManagementTest() {
        super();
        this.testURL = Config.WS_CLIENT_CONTEXT_PATH;
        this.sectionName = "ows_metadata_mangement";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);

        
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");
        
        String testCaseName = this.getSectionTestCaseName("change_to_admin_tab");
        log.info("Testing change current tab to OWS Admin tab...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div[2]/div/div/div/div/div/ul/li[3]/a");
        
        
        testCaseName = this.getSectionTestCaseName("login_in_admin_tab");
        log.info("Testing login in admin tab...");
        this.addTextToTextBox(webDriver, Config.RASDAMAN_ADMIN_USER, "/html/body/div[2]/div/div/div/div/div/div/div[3]/div/ul/div/div/div/div/div/div/div/uib-accordion/div/div/div[2]/div/div/div[1]/input");
        this.addTextToTextBox(webDriver, Config.RASDAMAN_ADMIN_PASS, "/html/body/div[2]/div/div/div/div/div/div/div[3]/div/ul/div/div/div/div/div/div/div/uib-accordion/div/div/div[2]/div/div/div[2]/input");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div[2]/div/div/div/div/div/div/div[3]/div/ul/div/div/div/div/div/div/div/uib-accordion/div/div/div[2]/div/div/div[3]/input[1]");

        
        // Then, switch back to WCS / DescribeCoverage to test rename coverage id
        this.runTestByClickingOnElementWithoutComparingOracle(webDriver, testCaseName, "/html/body/div[2]/div/div/div/div/div/ul/li[1]/a");
                
        testCaseName = this.getSectionTestCaseName("change_to_describe_coverage_tab");
        log.info("Testing change current tab to DescribeCoverage...");
        this.runTestByClickingOnElementWithoutComparingOracle(webDriver, testCaseName, "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/ul/li[2]/a");
        
        String coverageIdTextBoxXPath = "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[2]/div/div/div/div[1]/div/input";
        String describeCoverageButtonXPath = "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[2]/div/div/div/div[1]/div/span[2]/button";
        
        // Describe a coverage first
        testCaseName = this.getSectionTestCaseName("select_coverage_to_rename");
        log.info("Testing describe a coverage to rename...");
        // First change the coverage id in text box
        this.addTextToTextBox(webDriver, "test_TO_RENAME", coverageIdTextBoxXPath);
        // Then click on the Describe Coverage button
        this.runTestByClickingOnElementWithoutComparingOracle(webDriver, testCaseName, describeCoverageButtonXPath);
        
        String newCoverageIdTextBoxXPath = "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[2]/div/div/div/div[1]/div[2]/input";
        String renameCoverageIdButtonXPath = "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[2]/div/div/div/div[1]/div[2]/span[2]/button";
        
        // Then, rename this coverage
        testCaseName = this.getSectionTestCaseName("rename_coverage_id");
        log.info("Testing rename this coverage...");
        // Add to text box the new name
        this.addTextToTextBox(webDriver, "test_TO_RENAME_123456789", newCoverageIdTextBoxXPath);
        // Then click on the Rename Coverage Id button
        this.runTestByClickingOnElement(webDriver, testCaseName, renameCoverageIdButtonXPath);
    }
}