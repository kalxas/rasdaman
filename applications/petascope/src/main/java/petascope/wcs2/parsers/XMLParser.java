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

import java.io.IOException;
import java.io.StringReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import petascope.HTTPRequest;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import static petascope.util.XMLSymbols.*;
import petascope.util.XMLUtil;

/**
 * An abstract superclass for XML/POST protocol binding extensions, which
 * provides some convenience methods to concrete implementations.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 *
 * @param <T>
 */
public abstract class XMLParser<T extends Request> extends AbstractRequestParser<T> {

    private static Logger log = LoggerFactory.getLogger(XMLParser.class);

    @Override
    public boolean canParse(HTTPRequest request) {
        boolean canParse = request.getRequestString() != null
                && request.getRequestString().startsWith("<")
                && XMLUtil.isFirstTag(request.getRequestString(), getOperationName());
        log.trace("XMLParser<{}> {} parse the request", getOperationName(), canParse ? "can" : "cannot");
        return canParse;
    }

    protected Element parseInput(String input) throws WCSException {
        try {
            Document doc = XMLUtil.buildDocument(null, input);
            Element root = doc.getRootElement();

            String service = root.getAttributeValue(ATT_SERVICE);
            String version = root.getAttributeValue(ATT_VERSION);
            if ((null==service) || (!service.equals(BaseRequest.SERVICE))
                    || (version != null && !version.matches(BaseRequest.VERSION))) {
                throw new WCSException(ExceptionCode.VersionNegotiationFailed, "Service/Version not supported.");
            }

            return root;
        } catch (ParsingException ex) {
            throw new WCSException(ExceptionCode.XmlNotValid.locator(
                    "line: " + ex.getLineNumber() + ", column:" + ex.getColumnNumber()),
                    ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new WCSException(ExceptionCode.XmlNotValid, ex.getMessage(), ex);
        }
    }

    /**
     * Validates the input XML request against the given XML schema definition.
     *
     * @param input
     * @param schema
     * @throws WCSException
     */
    protected void validateInput(String input, Schema schema) throws WCSException {

        // create XML validator
        Source requestStream = new StreamSource(new StringReader(input));
        Validator validator = schema.newValidator();

        // validate
        try {
            validator.validate(requestStream);
        } catch(SAXException e) {
            throw new WCSException(ExceptionCode.XmlNotValid,"The structure of the provided input is not valid.");
        } catch(NullPointerException e) {
            throw new WCSException(ExceptionCode.InvalidRequest, "The received XML document is empty.");
        } catch(IOException e) {
            throw new WCSException(ExceptionCode.WcsError,"A fatal error ocurred while validating the input schema.");
        }
    }
}
