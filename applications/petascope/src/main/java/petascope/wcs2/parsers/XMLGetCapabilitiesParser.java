/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.parsers;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import nu.xom.Element;
import nu.xom.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import petascope.ConfigManager;
import static petascope.ConfigManager.XML_VALIDATION_T;
import petascope.HTTPRequest;
import petascope.exceptions.WCSException;
import static petascope.util.XMLSymbols.*;
import static petascope.util.XMLUtil.*;
import petascope.wcs2.handlers.RequestHandler;

/**
 * Parse a GetCapabilities XML request.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class XMLGetCapabilitiesParser extends XMLParser<GetCapabilitiesRequest> {

    Logger log = LoggerFactory.getLogger(XMLGetCapabilitiesParser.class);

    private Schema schema;
    private SchemaFactory schemaFactory;
    private final String WCS2_GETCAP_SCHEMA = "http://schemas.opengis.net/wcs/2.0/wcsGetCapabilities.xsd";

    public XMLGetCapabilitiesParser(){
        if(ConfigManager.XML_VALIDATION.equals(XML_VALIDATION_T)){
            try {
                log.info("Loading XML schema definition from " + WCS2_GETCAP_SCHEMA + "...");
                schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = schemaFactory.newSchema(new URL(WCS2_GETCAP_SCHEMA));
                log.info("Done.");
            } catch(SAXException e) {
                log.error("Could not initialize the GetCapabilities XML Schema validator. Schema validation will be disabled.",e);
            } catch(MalformedURLException e) {
                log.error("Could not initialize the GetCapabilities XML Schema validator. Schema validation will be disabled.",e);
            }
        }
    }

    @Override
    public GetCapabilitiesRequest parse(HTTPRequest request) throws WCSException {

        // input XML validation
        if(ConfigManager.XML_VALIDATION.equals(XML_VALIDATION_T)){
            validateInput(request.getRequestString(), schema);
        }

        // parsing
        Element root = parseInput(request.getRequestString());

        return new GetCapabilitiesRequest(
                childrenToString(firstChildRecursive(root, LABEL_ACCEPT_VERSIONS)),
                childrenToString(firstChildRecursive(root, LABEL_ACCEPT_FORMATS)),
                childrenToString(firstChildRecursive(root, LABEL_ACCEPT_LANGUAGES)));
    }

    private String childrenToString(Element e) {
        if (e == null) {
            return null;
        }
        String ret = "";
        for (int i = 0; i < e.getChildCount(); i++) {
            Node n = e.getChild(i);
            if (n instanceof Element) {
                ret += getText((Element)n) + ",";
            }
        }
        return ret.substring(0, ret.length() - 1);
    }

    @Override
    public String getOperationName() {
        return RequestHandler.GET_CAPABILITIES;
    }
}
