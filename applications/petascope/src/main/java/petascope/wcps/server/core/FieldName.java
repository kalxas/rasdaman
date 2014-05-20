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
package petascope.wcps.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import petascope.core.CoverageMetadata;
import petascope.exceptions.PetascopeException;
import petascope.util.MiscUtil;
import petascope.util.WcpsConstants;

public class FieldName extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(FieldName.class);

    /**
     * Band label.
     */
    private String name;
    /**
     * Band correspondent index in rasdaman.
     */
    private String nameIndex;
    /**
     * Coverage metadata needed for band label2index conversion.
     */
    private CoverageMetadata covMeta = null;

    public FieldName(Node node, XmlQuery xq, CoverageInfo covInfo) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        if (node == null) {
            throw new WCPSException("FieldNameType parsing error.");
        }

        String nodeName = node.getNodeName();
        log.trace(nodeName);

        if (nodeName.equals(WcpsConstants.MSG_NAME)) {
            this.name = node.getTextContent();
            log.trace("Found field name: " + name);
            String coverageName = covInfo.getCoverageName();
            try {
                covMeta = xq.getMetadataSource().read(coverageName);
            } catch (Exception ex) {
                log.error(ex.getMessage());
                throw new WCPSException(ex.getMessage(), ex);
            }
            try {
                nameIndex = covMeta.getRangeIndexByName(name).toString();
            } catch (PetascopeException ex1) {
                boolean wrongFieldSubset = true;
                log.debug("Range field subset " + name + " does not seem by-label: trying by-index.");

                if (MiscUtil.isInteger(name)) {
                    try {
                        // range subsetting might have been done via range field /index/ (instead of label): check this is a valid index
                        nameIndex = name;
                        name = covMeta.getRangeNameByIndex(Integer.parseInt(name));
                        wrongFieldSubset = false; // indeed subset was by-index
                    } catch (PetascopeException ex2) {
                        log.debug("Range field subset " + nameIndex + " is neither a valid index.");
                    }
                }
                if (wrongFieldSubset) {
                    log.error("Illegal range field selection: " + name);
                    throw new WCPSException(ex1.getExceptionCode(), ex1.getExceptionText());
                }
            }
        }
    }

    public String toRasQL() {
        // band labelling is not (necessarily) the same in coverage model and rasdaman collection: use RasQL index-based range field access
        // @see http://rasdaman.org/ticket/756
        return this.nameIndex;
    }
};
