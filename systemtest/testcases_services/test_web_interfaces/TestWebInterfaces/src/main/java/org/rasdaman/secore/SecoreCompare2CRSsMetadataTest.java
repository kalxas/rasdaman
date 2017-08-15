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
 * Compare 2 CRSs http://localhost:8080/def/crs/EPSG/0/4326 and
 * http://localhost:8080/def/crs/EPSG/0/4326
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class SecoreCompare2CRSsMetadataTest extends SecoreAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(SecoreCompare2CRSsMetadataTest.class);

    public SecoreCompare2CRSsMetadataTest() {
        super();        
        this.testURL = Config.SECORE_CONTEXT_PATH;
        this.sectionName = "compare_2_crss_metadata";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {        
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");

        String testCaseName = this.getSectionTestCaseName("check_crss_equality");        
        this.testURL = Config.SECORE_CONTEXT_PATH + "equal?1=" + Config.SECORE_CONTEXT_PATH + "crs/EPSG/0/4326&2=" + Config.SECORE_CONTEXT_PATH + "crs/EPSG/0/4326";
        webDriver.navigate().to(this.testURL);
        log.info("Testing compare 2 CRSs equality metadata EPSG:4326 and EPSG:4326...");
        this.runTestByNonElementEvent(webDriver, testCaseName);
        
        testCaseName = this.getSectionTestCaseName("check_crss_inequality");
        log.info("Testing compare 2 CRSs inequality metadata EPSG:4326 and EPSG:4327...");
        this.testURL = Config.SECORE_CONTEXT_PATH + "equal?1=" + Config.SECORE_CONTEXT_PATH + "crs/EPSG/0/4326&2=" + Config.SECORE_CONTEXT_PATH + "crs/EPSG/0/4327";        
        webDriver.navigate().to(this.testURL);
        this.runTestByNonElementEvent(webDriver, testCaseName);
    }
}
