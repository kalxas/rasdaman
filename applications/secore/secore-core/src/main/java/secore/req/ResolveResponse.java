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
 * Copyright 2003 - 2012 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package secore.req;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static secore.util.Constants.*;
import secore.util.StringUtil;

/**
 * Wrapper for GML responses.
 *
 * @author Dimitar Misev
 */
public class ResolveResponse {

    private static Logger log = LoggerFactory.getLogger(ResolveResponse.class);

    // regex pattern matching empty XML returned from BaseX
    private static final Pattern EMPTY_XML = Pattern.compile("(<\\?xml.*\\?>\\n)?<empty/>");

    private final String data;

    public ResolveResponse(String data) {
        // Add copyright

// results in invalid GML, so commented out until we find a way to inject the credits
        /*
            int ind = data.indexOf(METADATA_LABEL);
            if (ind != -1) {
              int end = data.indexOf(">", ind);
              int nextStart = data.indexOf("<", end);

              String firstPart = data.substring(0, end + 1);
              String secondPart = data.substring(end + 1);
              String indent = data.substring(end + 1, nextStart);

              // finally recompose data
              data = firstPart + indent +
                  "<credits xmlns=\"http://rasdaman.org/ns\">" + COPYRIGHT + "</credits>" +
                  secondPart;
            }
        */
        if (!StringUtil.emptyQueryResult(data)) {
            if (!data.startsWith("<?xml")) {
                data = XML_DECL + data;
            }

            // add missing namespaces
            int offset = data.indexOf("<", 1) + 1;
            int topLevelTagEnd = data.indexOf(">", offset);
            if (topLevelTagEnd != -1) {
                data = addMissingNamespace(data, GMD_PREFIX, GMD_NAMESPACE, topLevelTagEnd);
                data = addMissingNamespace(data, GCO_PREFIX, GCO_NAMESPACE, topLevelTagEnd);
            }
        }

        this.data = data;
    }

    /**
     * Adds missing namespace to XML data
     * @param data XML document
     * @param prefix XML prefix
     * @param namespace XML namespace
     * @param topLevelTagEnd the index of the closing '>' of the top-level XML tag in data
     * @return the same XML data with the namespace possibly added.
     */
    private String addMissingNamespace(String data, String prefix, String namespace, int topLevelTagEnd) {
        String ret = data;
        int namespaceIndex = data.indexOf(namespace);
        if (namespaceIndex > topLevelTagEnd || namespaceIndex == -1) {
            ret = data.substring(0, topLevelTagEnd) + " xmlns:" + prefix + "=\"" + namespace + "\"" + data.substring(topLevelTagEnd);
        }
        return ret;
    }

    /**
     * Gets the GML data stored in the object
     * Surrounds it by XML tags defining the document and declaring the required namespaces
     *
     * @return the GML definition of the resource requested
     */
    public String getData() {
        return data;
    }

    /**
     * Check if def is not an empty XML document.
     * @return true if not empty, false otherwise.
     */
    public boolean isValidDefinition() {
        if (data != null) {
            Matcher matcher = EMPTY_XML.matcher(data);
            return !EMPTY.equals(data) && !matcher.matches();
        }
        return false;
    }
}
