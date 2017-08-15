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
public class WCSInsertCoverageTest extends WSAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(WCSInsertCoverageTest.class);
    
    public static final String coverageId = "test_wcs_extract_selenium";

    public WCSInsertCoverageTest() {
        super();
        this.sectionName = "wcs_insert_coverage";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");

        // Switch to iframe to parse the web element
        webDriver.switchTo().frame(0);

        String testCaseName;

        // First, change to tab InsertCoverage
        testCaseName = this.getSectionTestCaseName("change_to_insert_coverage_tab");
        log.info("Testing change current tab to InsertCoverage...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/ul/li[6]/a");

        // Then add a URL from kahlua to a GML file which contain a GML for a coverage (3D domain is[0:0, 0:0, 0:0]
        // It is used to test for InsertCoverage tab in wcs-client and later can be deleted in DeleteCoverage in wcs-lcient
        testCaseName = this.getSectionTestCaseName("insert_coverage_empty_grid_domain_by_gml");
        log.info("Testing insert coverage with coverageId '" + coverageId + "' from a GML file at online URL...");
        // First add the URL to the GML file for the input coverage
        String urlToGMLFile = "http://kahlua.eecs.jacobs-university.de:8080/test_wcsclient_insertcoverage/example.gml";
        this.addTextToTextBox(webDriver, urlToGMLFile, "/html/body/div/div/div/div/div/div[6]/div/div/div/div[1]/input");
        // Then click on the Insert Coverage button
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[6]/div/div/div/div[1]/span[2]/button");
        Thread.sleep(2000);
    }
}
