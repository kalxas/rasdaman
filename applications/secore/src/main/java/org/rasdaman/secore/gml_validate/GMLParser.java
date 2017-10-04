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

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.rasdaman.secore.util.SecoreException;
import org.rasdaman.secore.util.ExceptionCode;
import org.xml.sax.helpers.DefaultHandler;
import org.rasdaman.secore.Resolver;
import org.rasdaman.secore.util.Constants;

/**
 *
 * @author Bang Pham Huu
 */
public class GMLParser extends DefaultHandler {

    private static final Logger log = LoggerFactory.getLogger(Resolver.class);

    /**
     * Try to parse GML definition and return error != "" if it is not well-formed
     * @param gmlDefinition
     * @return "" if does not have error
     * @throws SecoreException
     */
    public String parseGML(String gmlDefinition) throws SecoreException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        String error = Constants.EMPTY;
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(new StringReader(gmlDefinition)), this);
        } catch (ParserConfigurationException e) {
            error = e.getMessage();
            throw new SecoreException(ExceptionCode.ParserConfigurationException, " Error in parser configuration: " + e.getMessage(), e);
        } catch (SAXException e) {
            error = e.getMessage();
            throw new SecoreException(ExceptionCode.SAXParserException, " XML is not well-formed: " +  e.getMessage(), e);
        } catch (IOException e) {
            error = e.getMessage();
            throw new SecoreException(ExceptionCode.IOConnectionError, " The request could not be retrieved: " + e.getMessage(), e);
        }
        return error;
    }

}
