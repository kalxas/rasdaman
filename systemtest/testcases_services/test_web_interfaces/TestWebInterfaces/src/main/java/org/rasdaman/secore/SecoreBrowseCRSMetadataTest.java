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
package org.rasdaman.secore;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.rasdaman.Config;

/**
 * Browse a CRS metadata by a URI
 * ("http://localhost:8080/def/crs/EPSG/0/4326/browse.jsp")
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class SecoreBrowseCRSMetadataTest extends SecoreAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(SecoreBrowseCRSMetadataTest.class);

    public SecoreBrowseCRSMetadataTest() {
        super();
        this.testURL = Config.SECORE_CONTEXT_PATH + "crs/EPSG/0/4326/browse.jsp";
        this.sectionName = "browse_crs_metadata";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);

        String testCaseName;
        
        // It needs to login with SECORE admin and password first
        testCaseName = this.getSectionTestCaseName("login_admin_page");
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");
        log.info("Testing login with username, password in admin page: crs/EPGS/0/4326/browse.jsp URI...");
        String username = "secoreuser";
        this.addTextToTextBox(webDriver, username, "//*[@id=\"username\"]");
        String password = "secorepasswd";
        this.addTextToTextBox(webDriver, password, "//*[@id=\"password\"]");
        // then, login and capture the result
        this.runTestByClickingOnElement(webDriver, testCaseName, "//*[@id=\"collapseOne\"]/div/form/p[3]/input");

        // First, just display the crs in a textarea
        testCaseName = this.getSectionTestCaseName("view_crs_metadata");
        log.info("Testing browse CRS metadata from EPSG:4326 URI...");
        this.runTestByNonElementEvent(webDriver, testCaseName);

        // Then, validate the original GML metadata from epsg database
        testCaseName = this.getSectionTestCaseName("validate_crs");
        log.info("Testing validate browsed CRS metadata from EPSG:4326 URI...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/form/input[1]");

        // Then, add this original GML metadata to user database by clicking on Add button
        testCaseName = this.getSectionTestCaseName("add_crs_to_user_database");
        log.info("Testing add CRS metadata to user database from EPSG:4326 URI...");        
        // NOTE: PhantomJs needs to click 2 times with confirm dialog
        // first, click on add button to add CRS metadata to user database
        this.clickOnElement(webDriver, "/html/body/form/input[2]");
        // Click on Ok button, to add the CRS metadata to user database        
        this.clickOkInConfirmDialog(webDriver);                
        // Do it once more time
        this.clickOnElement(webDriver, "/html/body/form/input[2]");
        // Click on Ok button, to add the CRS metadata to user database        
        this.clickOkInConfirmDialog(webDriver);                
        // then, capture the result
        this.runTestByNonElementEvent(webDriver, testCaseName);        
    }
}
