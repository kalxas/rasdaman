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

import org.rasdaman.AbstractWebPageTest;
import org.rasdaman.Config;

/**
 * An abstract class for a section of a web page (URL) is tested
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public abstract class SecoreAbstractSectionWebPageTest extends AbstractWebPageTest {
    
    // each section belongs to a URL and can contain multiple cases
    protected String sectionName;
    
    public SecoreAbstractSectionWebPageTest() {
        super(Config.WS_CLIENT_CONTEXT_PATH);
        this.testFolder = "secore";
    } 
    
    /**
     * Return the string representing the test case with section name
     * @param testCaseName
     * @return 
     */
    protected String getSectionTestCaseName(String testCaseName) {
        return this.sectionName + "/" + testCaseName;
    }
}
