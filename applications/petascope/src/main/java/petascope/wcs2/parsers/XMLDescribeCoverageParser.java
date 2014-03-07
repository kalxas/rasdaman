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
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import nu.xom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import petascope.HTTPRequest;
import petascope.exceptions.WCSException;
import static petascope.util.XMLSymbols.*;
import static petascope.util.XMLUtil.*;
import petascope.wcs2.handlers.RequestHandler;
import petascope.ConfigManager;
import static petascope.ConfigManager.XML_VALIDATION_T;
import petascope.exceptions.ExceptionCode;

/**
 * Parse a GetCapabilities XML request.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class XMLDescribeCoverageParser extends XMLParser<DescribeCoverageRequest> {

    Logger log = LoggerFactory.getLogger(XMLDescribeCoverageParser.class);

    private Schema schema;
    private SchemaFactory schemaFactory;
    private final String WCS2_DESCRCOV_SCHEMA = "http://schemas.opengis.net/wcs/2.0/wcsDescribeCoverage.xsd";

    public XMLDescribeCoverageParser(){
        if(ConfigManager.XML_VALIDATION.equals(XML_VALIDATION_T)){
            try {
                log.info("Loading XML schema definition from " + WCS2_DESCRCOV_SCHEMA + "...");
                schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = schemaFactory.newSchema(new URL(WCS2_DESCRCOV_SCHEMA));
                log.info("Done.");
            } catch(SAXException e) {
                log.error("Could not initialize the DescribeCoverage XML Schema validator. Schema validation will be disabled.",e);
            } catch(MalformedURLException e) {
                log.error("Could not initialize the DescribeCoverage XML Schema validator. Schema validation will be disabled.",e);
            }
        }
    }

    @Override
    public DescribeCoverageRequest parse(HTTPRequest request) throws WCSException {

        // input XML validation
        if(ConfigManager.XML_VALIDATION.equals(XML_VALIDATION_T)){
            validateInput(request.getRequestString(), schema);
        }

        // parsing
        Element root = parseInput(request.getRequestString());
        List<Element> coverageIds = collectAll(root, PREFIX_WCS,
                LABEL_COVERAGE_ID, CTX_WCS);
        if (coverageIds.isEmpty()) {
            log.error("Missing required " + LABEL_COVERAGE_ID + " element in request.");
            throw new WCSException(ExceptionCode.InvalidRequest, "No <" + LABEL_COVERAGE_ID + "> found in request.");
        }
        DescribeCoverageRequest ret = new DescribeCoverageRequest();
        for (Element coverageId : coverageIds) {
            ret.getCoverageIds().add(getText(coverageId));
        }
        return ret;
    }

    @Override
    public String getOperationName() {
        return RequestHandler.DESCRIBE_COVERAGE;
    }
}
