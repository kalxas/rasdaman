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
package org.rasdaman;

import java.io.IOException;
import org.openqa.selenium.WebDriver;

/**
 * Interface of all classes which are related to web page to test from Earthlook
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public interface IWebPageTestable {

    /**
     * Run the test of the web page from webURL
     *
     * @param webDriver
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    void runTest(WebDriver webDriver) throws InterruptedException, IOException;
}
