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
import org.rasdaman.Config;
import static org.rasdaman.ws_client.WCSInsertCoverageTest.coverageId;

/**
 * Class to test wcs_client, tab WCS/DeleteCoverage
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WCSDeleteCoverageTest extends WSAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(WCSDeleteCoverageTest.class);

    public WCSDeleteCoverageTest() {
        super();
        this.sectionName = "wcs_delete_coverage";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");

        String testCaseName;

        // First, change to tab DeleteCoverage     
        testCaseName = this.getSectionTestCaseName("change_to_delete_coverage_tab");
        log.info("Testing change current tab to DeleteCoverage...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/ul/li[5]/a");
        
        testCaseName = this.getSectionTestCaseName("delete_coverage_empty_grid_domain_by_gml");
        log.info("Testing delete a coverage named '" + coverageId + "'...");
        // First add the new inserted coverage from kahlua GML file                
        this.addTextToTextBox(webDriver, coverageId, "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[5]/div/div/div/div/input");

        // Then click on the Delete Coverage button
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[5]/div/div/div/div/span[2]/button");
        
        Thread.sleep(Config.TIME_TO_WAIT_TO_CAPTURE_WEB_PAGE);
    }

}
