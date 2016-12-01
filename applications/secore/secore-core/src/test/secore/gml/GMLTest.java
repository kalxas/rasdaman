/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package secore.gml;

import org.junit.Test;
import static org.junit.Assert.*;
import secore.handler.GeneralHandler;
import secore.req.ResolveRequest;
import secore.req.ResolveResponse;

/**
 *
 * @author Bang Pham Huu
 */
public class GMLTest {

    GeneralHandler handler; // This will handle resolve complete URI
    GMLValidator gmlValidator; // This will validate resolved GML definition

    /* -------------------------- Test Well-Formed ----------------------------*/
    // This test cases will test with validate GML
    public GMLTest() {
        handler = new GeneralHandler();
        gmlValidator = new GMLValidator();
    }

    @Test
    public void testNotWellFormed() throws Exception {
        String tmp = " This is not valid XML";
        String expectedOutput = " XML is not well-formed: Content is not allowed in prolog.";

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        // Let check valid result
        assertEquals(validResult, expectedOutput);
    }

    /**
     * *
     * The input GML is not well-formed
     *
     * @throws Exception
     */
    @Test
    public void testNotWellFormed_1() throws Exception {

        String tmp = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
                     + "<gml:GeodeticCRS xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:epsg=\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\" gml:id=\"epsg-crs-4326\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">\n"
                     + " ";
        String expectedOutput = " XML is not well-formed: XML document structures must start and end within the same entity.";

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        // Let check valid result
        assertEquals(validResult, expectedOutput);
    }

    /**
     * The input GML is not well-formed
     *
     * @throws Exception
     */
    @Test
    public void testNotWellFormed_2() throws Exception {

        String tmp = "<epsg:AxisName xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:epsg=\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\" xmlns:rim=\"urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0\" gml:id=\"epsg-axisname-9907\">\n"
                     + "  <gml:metaDataProperty>\n"
                     + "    <epsg:CommonMetaData>\n"
                     + "      <epsg:informationSource>OGP</epsg:informationSource>\n"
                     + "      <epsg:revisionDate>2000-03-07</epsg:revisionDate>\n"
                     + "      <epsg:isDeprecated>false</epsg:isDeprecated>\n"
                     + "    </epsg:CommonMetaData>\n"
                     + "  </gml:metaDataProperty>\n"
                     + "  <gml:description>North pointing axis used in 2D projected coordinate systems.</gml:description>\n"
                     + "  <gml:identifier codeSpace=\"OGP\">http://www.opengis.net/def/axis-name/EPSG/0/9907</gml:identifier>\n"
                     + "  <gml:name>Northing</gml:name>\n"
                     + "</epsg:AxisName1>";
        String expectedOutput = " XML is not well-formed: The end-tag for element type \"epsg:AxisName\" must end with a '>' delimiter.";

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        // Let check valid result
        assertEquals(validResult, expectedOutput);
    }

    /*---------------------- Test Valid -------------------------------*/
    /**
     * This test will reslove GML definition and return valid ("")
     *
     */
    @Test
    public void testValidValidationAxisName() throws Exception {

        String uri = "axis-name/EPSG/0/9907";
        ResolveRequest request = new ResolveRequest(uri);
        ResolveResponse result = handler.handle(request);

        // Get data from resolve
        String tmp = result.getData();

        String expectedOutput = "";

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        // Let check valid result (return "")
        assertEquals(validResult, expectedOutput);
    }

    /**
     * This test return valid validation
     *
     * @throws Exception
     */
    @Test
    public void testValidValidationVersionHistory() throws Exception {

        String uri = "version-history/EPSG/0/1.0";

        ResolveRequest request = new ResolveRequest(uri);
        ResolveResponse result = handler.handle(request);

        // Get data from resolve
        String tmp = result.getData();

        String expectedOutput = "";

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        // Let check valid result (return "")
        assertEquals(validResult, expectedOutput);
    }

    /**
     * Test valid validation NamingSystem
     *
     * @throws Exception
     */
    @Test
    public void testValidValidationNamingSystem() throws Exception {
        String uri = "naming-system/EPSG/0/7317";

        ResolveRequest request = new ResolveRequest(uri);
        ResolveResponse result = handler.handle(request);

        // Get data from resolve
        String tmp = result.getData();

        String expectedOutput = "";

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        // Let check valid result (return "")
        assertEquals(validResult, expectedOutput);
    }

    /*------------------------ Test Invalid -------------------------*/
    /**
     * Test invalid UOM - Unit of Measurement return valid ("")
     */
    @Test
    public void testInValidValidationUOM() throws Exception {

        String uri = "uom/EPSG/0/1024";

        ResolveRequest request = new ResolveRequest(uri);
        ResolveResponse result = handler.handle(request);

        // Get data from resolve
        String tmp = result.getData();

        String expectedOutput = "The GML definition is not valid, please check error below \n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:ChangeRequest'. One of '{\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":changeID}' is expected.\n"
                                + "";

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        // Let check valid result (return "")
        assertEquals(validResult, expectedOutput);
    }

    /**
     * Test invalid Parameter
     *
     * @throws Exception
     */
    @Test
    public void testInValidValidationParemeter() throws Exception {

        String uri = "parameter/EPSG/0/1024";
        ResolveRequest request = new ResolveRequest(uri);
        ResolveResponse result = handler.handle(request);

        // Get resloved definition from SECORE
        String tmp = result.getData();

        String expectedOutput = "The GML definition is not valid, please check error below \n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:ChangeRequest'. One of '{\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":changeID}' is expected.\n"
                                + "";

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        // Let check valid result (return "")
        assertEquals(validResult, expectedOutput);
    }

    @Test
    public void testInvalidValidationDatumOGC() throws Exception {

        String uri = "datum/OGC/0/AnsiDateDatum";
        ResolveRequest request = new ResolveRequest(uri);
        ResolveResponse result = handler.handle(request);

        // Get resloved definition from SECORE
        String tmp = result.getData();

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        String expectedOutput = "The GML definition is not valid, please check error below \n"
                                + "cvc-complex-type.3.2.2: Attribute 'id' is not allowed to appear in element 'TemporalDatum'.\n"
                                + "cvc-complex-type.4: Attribute 'id' must appear on element 'TemporalDatum'.\n"
                                + "";

        // Let check valid result
        assertEquals(validResult, expectedOutput);
    }

    /**
     * This test will resolve GML definition with current state of SECORE NOTE:
     * when the bug in ticket 732 is fixed then result should be valid (valid
     * returns empty)
     *
     * @throws Exception
     */
    @Test
    public void testInvalidValidationCRS() throws Exception {

        String uri = "crs/EPSG/0/4326";
        ResolveRequest request = new ResolveRequest(uri);
        ResolveResponse result = handler.handle(request);

        // Get resloved definition from SECORE
        String tmp = result.getData();

        // Now validate it by GMLValidator
        String validResult = gmlValidator.parseAndValidateGMLFile(tmp);

        // Current SECORE this definition is not valid, after ticket 732 this should be valid and validResult return ""
        String expectedOutput = "The GML definition is not valid, please check error below \n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:ChangeRequest'. One of '{\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":changeID}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'gml:Conversion'. One of '{\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":projectionConversion, \"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":sourceGeographicCRS}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:AreaOfUse'. One of '{\"http://www.opengis.net/gml/3.2\":domainOfValidity, \"http://www.opengis.net/gml/3.2\":scope}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'gml:BaseUnit'. One of '{\"http://www.isotc211.org/2005/gmd\":valueType, \"http://www.isotc211.org/2005/gmd\":valueUnit}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:ChangeRequest'. One of '{\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":changeID}' is expected.\n"
                                + "cvc-id.2: There are multiple occurrences of ID value 'epsg-cr-2006.810'.\n"
                                + "cvc-attribute.3: The value 'epsg-cr-2006.810' of attribute 'gml:id' on element 'epsg:ChangeRequest' is not valid with respect to its type, 'ID'.\n"
                                + "cvc-id.2: There are multiple occurrences of ID value 'epsg-cr-2007.079'.\n"
                                + "cvc-attribute.3: The value 'epsg-cr-2007.079' of attribute 'gml:id' on element 'epsg:ChangeRequest' is not valid with respect to its type, 'ID'.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'gml:Conversion'. One of '{\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":projectionConversion, \"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":sourceGeographicCRS}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:AreaOfUse'. One of '{\"http://www.opengis.net/gml/3.2\":name, \"http://www.opengis.net/gml/3.2\":remarks, \"http://www.opengis.net/gml/3.2\":domainOfValidity, \"http://www.opengis.net/gml/3.2\":scope}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:AreaOfUse'. One of '{\"http://www.opengis.net/gml/3.2\":name, \"http://www.opengis.net/gml/3.2\":remarks, \"http://www.opengis.net/gml/3.2\":domainOfValidity, \"http://www.opengis.net/gml/3.2\":scope}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'empty'. One of '{\"http://www.opengis.net/gml/3.2\":name, \"http://www.opengis.net/gml/3.2\":remarks, \"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":isoA2CountryCode, \"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":isoA3CountryCode, \"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":isoNumericCountryCode, \"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":geometryFile}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:ChangeRequest'. One of '{\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":changeID}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:AxisName'. One of '{\"http://www.opengis.net/gml/3.2\":metaDataProperty, \"http://www.opengis.net/gml/3.2\":description, \"http://www.opengis.net/gml/3.2\":descriptionReference, \"http://www.opengis.net/gml/3.2\":identifier}' is expected.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:AxisName'. One of '{\"http://www.opengis.net/gml/3.2\":metaDataProperty, \"http://www.opengis.net/gml/3.2\":description, \"http://www.opengis.net/gml/3.2\":descriptionReference, \"http://www.opengis.net/gml/3.2\":identifier}' is expected.\n"
                                + "cvc-id.2: There are multiple occurrences of ID value 'ogp-datum-6326'.\n"
                                + "cvc-attribute.3: The value 'ogp-datum-6326' of attribute 'gml:id' on element 'gml:GeodeticDatum' is not valid with respect to its type, 'ID'.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:ChangeRequest'. One of '{\"urn:x-ogp:spec:schema-xsd:EPSG:1.0:dataset\":changeID}' is expected.\n"
                                + "cvc-id.2: There are multiple occurrences of ID value 'epsg-cr-2002.151'.\n"
                                + "cvc-attribute.3: The value 'epsg-cr-2002.151' of attribute 'gml:id' on element 'epsg:ChangeRequest' is not valid with respect to its type, 'ID'.\n"
                                + "cvc-id.2: There are multiple occurrences of ID value 'epsg-cr-2006.810'.\n"
                                + "cvc-attribute.3: The value 'epsg-cr-2006.810' of attribute 'gml:id' on element 'epsg:ChangeRequest' is not valid with respect to its type, 'ID'.\n"
                                + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'epsg:AreaOfUse'. One of '{\"http://www.opengis.net/gml/3.2\":domainOfValidity, \"http://www.opengis.net/gml/3.2\":scope}' is expected.\n"
                                + "";

        // Let check valid result
        assertEquals(validResult, expectedOutput);
    }

    /* @Test
     public void testDocumentNames() throws Exception {
     String query =
     "for $doc in collection() return base-uri($doc)";
     String result = DbManager.getInstance().getDb().query(query);
     assertEquals("userdb/userdb.xml", result);
     }*/
}
