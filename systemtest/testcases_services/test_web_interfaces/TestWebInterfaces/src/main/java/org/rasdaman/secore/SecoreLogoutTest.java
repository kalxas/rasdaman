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
import org.openqa.selenium.WebDriver;
import org.rasdaman.Config;

/**
 * Logout from admin page ("http://localhost:8080/def/crs/EPSG/0/4326/logout.jsp")
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class SecoreLogoutTest extends SecoreAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(SecoreExecuteXQueryTest.class);

    public SecoreLogoutTest() {
        super();
        this.testURL = Config.SECORE_CONTEXT_PATH + "/crs/logout.jsp";
        this.sectionName = "logout_admin_page";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);

        String testCaseName = this.getSectionTestCaseName("logout");
        log.info("Testing logout from browsing CRS metadata from EPSG:4326 URI...");
        this.runTestByNonElementEvent(webDriver, testCaseName);        
    }
}
