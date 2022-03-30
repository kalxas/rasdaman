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
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Class to test wcs_client, tab WCS/ProcessCoverage
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WCSProcessCoverageTest extends WSAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(WCSProcessCoverageTest.class);

    public WCSProcessCoverageTest() {
        super();
        this.sectionName = "wcs_process_coverage";
    }
    
    private void runWCPSQuery(String testCaseName, WebDriver webDriver, String query) throws InterruptedException, IOException {
//      getting codemirror element
        WebElement codeMirror = webDriver.findElement(By.className("CodeMirror"));

//      getting the first line of code inside codemirror and clicking it to bring it in focus
        WebElement codeLine = codeMirror.findElements(By.className("CodeMirror-lines")).get(0);
        codeLine.click();

//      sending keystokes to textarea once codemirror is in focus
        WebElement textareaElement = codeMirror.findElement(By.cssSelector("textarea"));
        
        // clear the textarea of codemirror
        textareaElement.sendKeys(Keys.CONTROL + "a");
        textareaElement.sendKeys(Keys.DELETE);

        textareaElement.sendKeys(query);
        
        String executeButtonXPath = "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/div/div[4]/div/div/div/div[2]/div[2]/button";
        
        this.runTestByClickingOnElement(webDriver, testCaseName, executeButtonXPath);
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");
        
        this.waitForPageLoad(webDriver);
        
        String testCaseName;
        String query;
        
        // First, change to tab ProcessCoverage
        testCaseName = this.getSectionTestCaseName("change_to_process_coverage_tab");        
        log.info("Testing change current tab to ProcessCoverages...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div[2]/div/div/div/div/div/div/div[1]/div/ul/div/div/ul/li[4]/a");
        
        // No encoding
        testCaseName = this.getSectionTestCaseName("no_encoding");
        log.info("Testing a WCPS query without encoding...");
        query = "text>>for c in (test_mean_summer_airtemp) return avg(c)"; 
        this.runWCPSQuery(testCaseName, webDriver, query);

        // Encode 2D as PNG with widget
        testCaseName = this.getSectionTestCaseName("encode_2d_png_widget");
        log.info("Testing a WCPS query with encoding as PNG and image widget...");
        query = "image>>for c in (test_mean_summer_airtemp) return encode(c, \"png\")"; 
        this.runWCPSQuery(testCaseName, webDriver, query);

        // Encode 1D as JSON with widget
        testCaseName = this.getSectionTestCaseName("encode_1d_json_widget");
        query = "diagram>>for c in (test_mean_summer_airtemp) return encode(c[Lat:\"CRS:1\"(0)], \"json\")"; 
        this.runWCPSQuery(testCaseName, webDriver, query);
        
        // Encode 2D as gml
        testCaseName = this.getSectionTestCaseName("encode_2d_gml");
        log.info("Testing a WCPS query with encoding as GML...");
        query = "text>>for c in (test_mean_summer_airtemp) return encode(c[Lat(-40:-35), Lon(120:130)], \"gml\")"; 
        this.runWCPSQuery(testCaseName, webDriver, query);
        
    }
}
