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
import static org.rasdaman.Config.TIME_TO_WAIT_AFTER_SWITCHING_IFRAME;

/**
 * Class to test wcs_client, tab WCS/GetCapabilities
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WCSGetCapabilitiesTest extends WSAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(WCSGetCapabilitiesTest.class);

    public WCSGetCapabilitiesTest() {
        super();
        this.sectionName = "wcs_get_capabilities";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");

        String testCaseName;

        // Switch to iframe to parse the web element
        Thread.sleep(TIME_TO_WAIT_AFTER_SWITCHING_IFRAME);
        webDriver.switchTo().frame(0);
        Thread.sleep(TIME_TO_WAIT_AFTER_SWITCHING_IFRAME);
        
        // Click on get capabilities button
        testCaseName = this.getSectionTestCaseName("click_on_GetCapabilities_button");
        log.info("Testing change current tab to GetCapabilities...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/ul/li[1]/a");
        Thread.sleep(TIME_TO_WAIT_AFTER_SWITCHING_IFRAME);
        /* 
        As the returned coverages from Petascope are produced from a Map, so they can show different coverageIds in second page.
        // Click on paging button        
        testCaseName = this.getSectionTestCaseName("click_on_Paging_button");
        log.info("Testing click on another paging button...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[1]/div/div/div/div[2]/uib-accordion/div/div[1]/div[2]/div/table/tfoot/tr/td/div/nav/ul/li[2]/a"); 
        */

        // Click on GML server Capabilities document dropdown button
        testCaseName = this.getSectionTestCaseName("click_on_ServiceIdentification_dropdown_button");
        log.info("Testing click on Service identification dropdown button...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/div/div[1]/div/div/div/div[2]/uib-accordion/div/div[3]/div[1]/h4/a/span/i");
        
        // Search the coverage by id
        // NOTE: this one lists only one coverage and has no paging button, so must put it at the last test case
        testCaseName = this.getSectionTestCaseName("search_coverage_by_id");
        log.info("Testing search coverage by Id textbox...");
        this.runTestByAddingTextToTextBox(webDriver, testCaseName, "test_mr", "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/div/div[1]/div/div/div/div[2]/uib-accordion/div/div[1]/div[2]/div/table/thead/tr[2]/th/input");
        
        // Click on the search result (only one result) to move to next tab
        testCaseName = this.getSectionTestCaseName("click_on_a_search_result");
        log.info("Testing click on a found result of searching by coverageId...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/div/div[1]/div/div/div/div[2]/uib-accordion/div/div[1]/div[2]/div/table/tbody/tr/td[1]/a");

    }
}
