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
 * Get a CRS metadata by a URI ("http://localhost:8080/def/crs/EPSG/0/4326")
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class SecoreExecuteXQueryTest extends SecoreAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(SecoreExecuteXQueryTest.class);

    public SecoreExecuteXQueryTest() {
        super();
        this.testURL = Config.SECORE_CONTEXT_PATH + "index.jsp?query=true";
        this.sectionName = "execute_xquery";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);

        String testCaseName = this.getSectionTestCaseName("list_all_elements_from_user_database");
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");
        log.info("Testing execute XQuery to list all element sfrom user database...");
        String xquery = "let $x := collection('userdb') return $x";
        this.addTextToTextBox(webDriver, xquery, "/html/body/form/textarea");      
        // then, capture the result
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/form/input");
    }
}
