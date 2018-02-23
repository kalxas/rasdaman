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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.secore.gml_validate;

/**
 *
 * @author Bang Pham Huu
 */
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.rasdaman.secore.util.SecoreException;
import org.rasdaman.secore.Constants;
import org.rasdaman.secore.util.ExceptionCode;
import org.rasdaman.secore.util.IOUtil;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Bang Pham Huu
 */
public class GMLValidator {

    private static final Logger log = LoggerFactory.getLogger(GMLValidator.class);

    /**
     * Try to check GML definition is well-formed, if it is good then validate it
     * against EPSG Schema XSD
     *
     * @param gmlDefinition String GML Definition
     * @return error != "" if error does exist
     *
     */
    public static String parseAndValidateGMLFile(String gmlDefinition) {

        // Set the gmlXSDPath to current project (secore-core)
        String gmlXSDFile = "GML/epsg/EPSG.xsd";

        GMLParser gmlParser = new GMLParser();
        String ret = Constants.EMPTY;

        // 1. First check it is well-formed first
        try {
            ret = gmlParser.parseGML(gmlDefinition);
        } catch (SecoreException e) {
            log.error(e.getMessage());
            ret = e.getMessage();
        }

        // 2. If it is well-formed, then validate against GML Schema XSD
        if (ret.equals(Constants.EMPTY)) {
            ret = validateGMLDefinition(gmlDefinition, gmlXSDFile);
        }
        System.out.print(ret);
        return ret;
    }

    /**
     * Try to validate GML definition with EPSG Schema XSD
     *
     * @param gmlDefinition String - GML definition
     * @param gmlXSDFile String - file path to EPSG Schema XSD in
     * /etc/GML/EPSG.xsd
     * @return error != "" if error does exist
     *
     */
    public static String validateGMLDefinition(String gmlDefinition, String gmlXSDFile) {

        // List of exceptions when validate gmlDefinition to gmlXSDFile (collect all error when validate and return in one time)
        String error = Constants.EMPTY;
        final List<SAXParseException> exceptions = new LinkedList<>();
        try {
            SchemaFactory factory
                = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Important! without doing like this SAXParser could not load imported XSD in main XSD file (EPSG.xsd)
            URL schemaURL = Thread.currentThread().getContextClassLoader().getResource("schema/GML/epsg/EPSG.xsd");            
            Schema schema = factory.newSchema(schemaURL);
            Validator validator = schema.newValidator();

            // Handle all errors in validating before returning to user
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    exceptions.add(exception);
                }

                @Override
                // Note: when GML validator has fatal error, it will return immediately and could not continue to validate
                public void fatalError(SAXParseException exception) throws SAXException {
                    exceptions.add(exception);
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    exceptions.add(exception);
                }
            });

            // Validate gmlDefinition against EPSG Schema XML
            validator.validate(new StreamSource(new StringReader(gmlDefinition)));

        } catch (SAXException e) {
            log.debug(ExceptionCode.SAXParserException + " " + e.getMessage());
            error = ExceptionCode.SAXParserException + " " + e.getMessage();
        } catch (IOException e) {
            log.debug(ExceptionCode.IOConnectionError + " " + e.getMessage());
            error = ExceptionCode.IOConnectionError + " " + e.getMessage();
        }

        // Now return all errors if they do exist
        if (!exceptions.isEmpty()) {
            for (SAXParseException ex : exceptions) {
                // Ignore "there are multiple occurrences of ID value" due to
                // one definition can reference to multiple definitions and all of these objects can point to 1 defintion (1 gml identifier)
                // This is just a warning when resolve a definition
                if (!(ex.getMessage().contains("There are multiple occurrences of ID value")
                        || (ex.getMessage().contains("is not valid with respect to its type, 'ID'")))) {
                    error += ex.getMessage() + "\n";
                }
            }

            // check if all is just warning due to multiple occurrences of ID then it still valid
            // else it returns error
            if (!error.isEmpty()) {
                error = "The GML definition is not valid, please check error below \n" + error;
            }
        }
        return error;
    }
}
