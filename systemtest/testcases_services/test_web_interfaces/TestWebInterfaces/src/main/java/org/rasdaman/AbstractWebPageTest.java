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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import static org.rasdaman.Config.ORACLE_FOLDER_PATH;

/**
 * Abstract class for all the classes which are used to test Web Page from
 * EarthLook
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public abstract class AbstractWebPageTest implements IWebPageTestable {

    private static final Logger log = Logger.getLogger(AbstractWebPageTest.class);

    /**
     * Override the test folder in each subclass
     */
    protected String testFolder = "";

    /**
     * The section to web page to test
     */
    protected String testURL;

    /**
     * Any subclass which has error test will lead to the test process failure
     */
    protected boolean errorTest = false;

    public AbstractWebPageTest(String testURL) {
        this.testURL = testURL;
    }

    /**
     * Save the test result as and image to temp folder /tmp
     *
     * @param webDriver
     * @param testCaseName
     * @throws IOException
     */
    private String captureOutputFile(WebDriver webDriver, String testCaseName) throws IOException {
        File captureOne = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
        // convert output in png to jpg
        this.convertPNGToJPG(captureOne);

        String testCaseResultFilePath = Config.OUTPUT_FOLDER_PATH + "/" + testFolder + "/" + testCaseName;
        FileUtils.copyFile(captureOne, new File(testCaseResultFilePath));

        return testCaseResultFilePath;
    }

    /**
     * Convert the captured image result from PhantomJS in PNG to JPG
     *
     * @param pngImage
     * @throws IOException
     */
    private void convertPNGToJPG(File pngImage) throws IOException {
        //read image file
        BufferedImage bufferedImage;
        bufferedImage = ImageIO.read(pngImage);

        // create a blank, RGB, same width and height, and a white background
        BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

        // write to jpeg file
        ImageIO.write(newBufferedImage, "jpg", pngImage);
    }

    /**
     * Compare the output image from /tmp with the oracle file
     *
     * @param testCaseName
     * @param webDriver
     * @throws java.io.IOException
     */
    private void compareOutputAndOracleFile(WebDriver webDriver, String testCaseName) throws IOException {
        String oracleFilePath = ORACLE_FOLDER_PATH + this.testFolder + "/" + testCaseName;                
        
        // Always capture the web page as image file
        String outputFilePath = this.captureOutputFile(webDriver, testCaseName);
        
        // NOTE: check if oracle file exists, if it does not, just copy output as a oracle file
        if (Files.exists(Paths.get(oracleFilePath))) {        
            // Check the similarity of 2 image files
            boolean testResult = Utility.compare2Images(new File(oracleFilePath), new File(outputFilePath));

            // Then, compare oracle and output files
            // this.testResults.put(testCaseName, testResult);
            if (testResult) {
                log.info("TEST PASSED");
            } else {
                log.info("TEST FAILED");
                this.errorTest = true;
            }
        } else {
            // Oracle does not exist, copy output as oracle file
            log.info("NO ORACLE FOUND.");
            log.info("copying from '" + outputFilePath + "' to '" + oracleFilePath + "'.");            
            FileUtils.copyFile(new File(outputFilePath), new File(oracleFilePath));
        }
    }

    /**
     * Just click on an element
     *
     * @param webDriver
     * @param xPathToElement
     * @throws java.lang.InterruptedException
     */
    protected void clickOnElement(WebDriver webDriver, String xPathToElement) throws InterruptedException {
        Thread.sleep(Config.TIME_TO_WAIT_AFTER_CLICK);
        WebElement webElement = webDriver.findElement(By.xpath(xPathToElement));
        webElement.click();
        Thread.sleep(Config.TIME_TO_WAIT_TO_CAPTURE_WEB_PAGE);
    }

    /**
     *
     * Just click on Ok button of dialog Javascript.
     *
     * NOTE: PhantomJS does not support alert/confirm/prompt dialog in
     * Javascript, so must use a trick to simulate this click on Ok button of
     * dialog.
     *
     * @param webDriver
     */
    protected void clickOkInConfirmDialog(WebDriver webDriver) {
        ((JavascriptExecutor) webDriver).executeScript("window.confirm = function(msg) { return true; }");
    }

    /**
     * Run the test on the web page when it is needed to click on the link
     * element <a> ... </a> or element <button> ... </button>
     * which will not open another page but send query to server and plot the
     * result.
     *
     * @param webDriver
     * @param testCaseName
     * @param xPathToElement
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    protected void runTestByClickingOnElement(WebDriver webDriver, String testCaseName, String xPathToElement) throws InterruptedException, IOException {
        this.clickOnElement(webDriver, xPathToElement);
        this.compareOutputAndOracleFile(webDriver, testCaseName);
    }
    
    
    /**
     * Run the test on the web page when it is needed to click on the link
     * element <a> ... </a> or element <button> ... </button>
     * which will not open another page but send query to server and plot the
     * result.
     * 
     * NOTE: it will not compare the output with oracle file.
     *
     * @param webDriver
     * @param testCaseName
     * @param xPathToElement
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    protected void runTestByClickingOnElementWithoutComparingOracle(WebDriver webDriver, String testCaseName, String xPathToElement) throws InterruptedException, IOException {
        this.clickOnElement(webDriver, xPathToElement);
        Thread.sleep(2000);
        //this.compareOutputAndOracleFile(webDriver, testCaseName);
    }

    /**
     * Run test by clicking on element, then, a tab/window is opened, and
     * capture the result from this window/tab. NOTE: a previous window will be
     * lost focus (and need to jump to iframe and switch to the correct tab of
     * current test case manually) from the caller method.
     *
     * @param webDriver
     * @param testCaseName
     * @param xPathToElement
     * @throws InterruptedException
     * @throws IOException
     */
    protected void runTestByClickingOnElementAndCaptureTheOpenedWindow(WebDriver webDriver, String testCaseName, String xPathToElement) throws InterruptedException, IOException {
        Thread.sleep(Config.TIME_TO_WAIT_AFTER_CLICK);
        WebElement webElement = webDriver.findElement(By.xpath(xPathToElement));
        webElement.click();
        Thread.sleep(Config.TIME_TO_WAIT_AFTER_CLICK);
        ArrayList<String> windows = new ArrayList<>(webDriver.getWindowHandles());
        // Then, switch to next tab and capture the result of this tab/window
        webDriver.switchTo().window(windows.get(1));
        Thread.sleep(Config.TIME_TO_WAIT_TO_CAPTURE_WEB_PAGE);
        this.compareOutputAndOracleFile(webDriver, testCaseName);
        // Then, close openeded withdow
        webDriver.close();
        // and change back to main window
        webDriver.switchTo().window(windows.get(0));
    }

    /**
     * Run the test on the web page and just capture the image as result without
     * clicking on anything.
     *
     * @param webDriver
     * @param testCaseName
     * @throws InterruptedException
     * @throws IOException
     */
    protected void runTestByNonElementEvent(WebDriver webDriver, String testCaseName) throws InterruptedException, IOException {
        Thread.sleep(Config.TIME_TO_WAIT_TO_CAPTURE_WEB_PAGE);
        this.compareOutputAndOracleFile(webDriver, testCaseName);
    }

    /**
     * Just add text to a HTML input ty=e"text"
     *
     * @param webDriver
     * @param text
     * @param xPathToElement
     * @throws java.lang.InterruptedException
     */
    protected void addTextToTextBox(WebDriver webDriver, String text, String xPathToElement) throws InterruptedException {
        WebElement webElement = webDriver.findElement(By.xpath(xPathToElement));
        // select all the text in the text box then override it with the new text
        webElement.sendKeys(Keys.chord(Keys.CONTROL, "a"), text);
        Thread.sleep(Config.TIME_TO_WAIT_AFTER_CLICK);
    }

    /**
     * Add test to a HTML input type="text"
     *
     * @param webDriver
     * @param testCaseName
     * @param text
     * @param xPathToElement
     * @throws java.io.IOException@throws java.lang.InterruptedException
     * @throws java.lang.InterruptedException
     */
    protected void runTestByAddingTextToTextBox(WebDriver webDriver, String testCaseName, String text, String xPathToElement) throws IOException, InterruptedException {
        this.addTextToTextBox(webDriver, text, xPathToElement);
        Thread.sleep(Config.TIME_TO_WAIT_AFTER_CLICK);
        this.compareOutputAndOracleFile(webDriver, testCaseName);
    }

    /**
     * Add text to CodeMirror which is a div element of WCPS console, Rasql
     * console
     *
     * @param webDriver
     * @param query
     */
    protected void runTestByAddingTextToCodeMirror(WebDriver webDriver, String query) {
        WebElement queryInput = webDriver.findElement(By.xpath("//*[@id=\"query\"]/div"));
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
        javascriptExecutor.executeScript("arguments[0].CodeMirror.setValue('" + query + "');", queryInput);
    }

    /**
     * Run the query from Rasql console, WCPS console and capture the output as
     * an image
     *
     * @param webDriver
     * @param testCaseName
     * @param query
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    protected void runTestQueryInConsoleCodeMirror(WebDriver webDriver, String testCaseName, String query) throws InterruptedException, IOException {
        // CodeMirror needs to set the properties CodeMirror of the div correctly
        this.runTestByAddingTextToCodeMirror(webDriver, query);
        this.runTestByClickingOnElement(webDriver, testCaseName, "//*[@id=\"run\"]");
    }

    /**
     * Print the page content of the current navigating web page, used to test
     * if the web page can be loaded.
     *
     * @param webDriver
     */
    public static void logPageContent(WebDriver webDriver) {
        String pageSource = webDriver.getPageSource();
        log.info(pageSource);
    }

    /**
     * Return false if any test case of subclass failed
     *
     * @return
     */
    public boolean hasErrorTest() {
        return this.errorTest;
    }
}
