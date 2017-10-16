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
 * Class to test wcs_client, tab WMS/DescribeLayer
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WMSDescribeLayerTest extends WSAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(WMSDescribeLayerTest.class);

    public WMSDescribeLayerTest() {
        super();
        this.sectionName = "wms_describe_layer";
    }

    /**
     * Only when clicking on WMS GetCapabilities button, styles which are
     * added/removed/inserted will show. And return back to the DescribeLayer
     * tab with click on Describe Layer button
     *
     * @param webDriver
     */
    private void fetchNewLayerMetadata(WebDriver webDriver) throws InterruptedException {
        // Click on WMS tab GetCapabilities
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/ul/li[1]/a");
        Thread.sleep(500);
        // Click on button Get Capabilities
        this.clickOnElement(webDriver, "//*[@id=\"wmsGetServerCapabilitiesBtn\"]");
        Thread.sleep(1000);
        // Change to DescribeLayer tab
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/ul/li[2]/a");
        // Then click on DescribeLayer button
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[2]/div/div/div/div[1]/div/span[2]/button");
        Thread.sleep(500);
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");

        // Switch to iframe to parse the web element
        webDriver.switchTo().frame(0);

        // Focus on the second parent tab (WMS)
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/ul/li[2]/a");

        String testCaseName;

        // First, change to tab DescribeLayer
        testCaseName = this.getSectionTestCaseName("change_to_describe_layer_tab");
        log.info("Testing change current tab to DescribeLayer...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/ul/li[2]/a");

        String layerNameTextBoxXPath = "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[2]/div/div/div/div[1]/div/input";
        String describeLayerButtonXPath = "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[2]/div/div/div/div[1]/div/span[2]/button";

        
        // Sometimes it captures the whole page, sometimes only a part of page, so it makes the test on Centos 7 fail randomly.
        // Describe a EPSG:3857 layer
        testCaseName = this.getSectionTestCaseName("describe_a_epsg_3857_layer");
        log.info("Testing describe a epsg:3857 layer...");
        // First change the layer name in text box
        this.addTextToTextBox(webDriver, "test_wms_3857", layerNameTextBoxXPath);
        // Then click on the Describe Layer button and will not compare the oracle file
        this.runTestByClickingOnElementWithoutComparingOracle(webDriver, testCaseName, describeLayerButtonXPath);
        log.info("TEST PASSED");

        // Describe a EPSG:4326 layer 
        testCaseName = this.getSectionTestCaseName("describe_a_epsg_4326_layer");
        log.info("Testing describe a epsg:4326 layer...");
        // First change the layer name in text box
        this.addTextToTextBox(webDriver, "test_wms_4326", layerNameTextBoxXPath);
        // Then click on the Describe Layer button
        this.runTestByClickingOnElement(webDriver, testCaseName, describeLayerButtonXPath);

        // ******* Layer's styles management *******
        // Insert a new style
        testCaseName = this.getSectionTestCaseName("insert_a_new_style");
        log.info("Testing insert a new style...");
        this.addTextToTextBox(webDriver, "style1", "//*[@id=\"styleName\"]");
        this.addTextToTextBox(webDriver, "abstract1", "//*[@id=\"styleAbstract\"]");
        this.addTextToTextBox(webDriver, "switch case $c > 1000 return {red: 107; green:17; blue:68} default return {red: 150; green:103; blue:14})", "//*[@id=\"styleQuery\"]");
        // click on Insert button
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[2]/div/div/div/div[4]/uib-accordion/div/div/div[2]/div/div/form/div[5]/div[1]/button");
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[2]/div/div/div/div[4]/uib-accordion/div/div/div[2]/div/div/form/div[5]/div[1]/button");
        // fetch new Capabilities to show the inserted style
        this.fetchNewLayerMetadata(webDriver);
        this.runTestByNonElementEvent(webDriver, testCaseName);

        // Update a style
        testCaseName = this.getSectionTestCaseName("update_a_style");
        log.info("Testing update a style...");        
        // Click on the inserted style name
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[2]/div/div/div/div[4]/uib-accordion/div/div/div[2]/div/table/tbody/tr/td[1]/a");
        this.addTextToTextBox(webDriver, "This is a new style is updated", "//*[@id=\"styleAbstract\"]");
        // click on Update button
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[2]/div/div/div/div[4]/uib-accordion/div/div/div[2]/div/div/form/div[5]/div[2]/button");
        // fetch new Capabilities to show the updated style
        this.fetchNewLayerMetadata(webDriver);
        this.runTestByNonElementEvent(webDriver, testCaseName);
        
        // Delete a style
        testCaseName = this.getSectionTestCaseName("delete_a_style");
        log.info("Testing delete a style...");
        this.clickOnElement(webDriver, "/html/body/div[1]/div/div/div/div/div[2]/div/ul/div/div/div/div[2]/div/div/div/div[4]/uib-accordion/div/div/div[2]/div/table/tbody/tr/td[3]/button");                
        // fetch new Capabilities to show the deleted style
        this.fetchNewLayerMetadata(webDriver);        
        this.runTestByNonElementEvent(webDriver, testCaseName);
    }
}
