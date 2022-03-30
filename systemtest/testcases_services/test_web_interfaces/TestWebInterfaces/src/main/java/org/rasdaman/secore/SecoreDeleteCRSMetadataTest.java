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
import org.openqa.selenium.WebDriver;
import org.rasdaman.Config;

/**
 * Delete an imported CRS from user_database
 * ("http://localhost:8080/def/crs/EPSG/0/browse.jsp")
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class SecoreDeleteCRSMetadataTest extends SecoreAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(SecoreDeleteCRSMetadataTest.class);

    public SecoreDeleteCRSMetadataTest() {
        super();
        this.testURL = Config.SECORE_CONTEXT_PATH + "crs/EPSG/0/browse.jsp";
        this.sectionName = "delete_crs_metadata";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");

        String testCaseName = this.getSectionTestCaseName("delete_epsg_4269_from_user_database");
        log.info("Testing delete imported CRS metadata EPSG:4269 from user database...");
        webDriver.findElement(By.xpath("/html/body/table/tbody/tr[2124]/td[1]/a")).click();
        // then, click on the Ok button in the confirm dialog to remove it from database        
        this.clickOkInConfirmDialog(webDriver);

        
        // then, back to the browse CRS metadata, it should show the Add button instead of Update        
        webDriver.navigate().to(Config.SECORE_CONTEXT_PATH + "crs/EPSG/0/4269/browse.jsp");        
        // then, capture the result
        this.runTestByNonElementEvent(webDriver, testCaseName);
    }
}
