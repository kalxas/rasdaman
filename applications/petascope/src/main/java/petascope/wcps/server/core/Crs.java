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

import petascope.core.CoverageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.core.DbMetadataSource;
import petascope.core.DynamicMetadataSource;
import petascope.core.IDynamicMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.WcpsConstants;

public class Crs extends AbstractRasNode {

    private static final Logger log = LoggerFactory.getLogger(Crs.class);
    private String crsName;
    private DbMetadataSource dbMeta;

    public Crs(String srsName, XmlQuery xq) {
        crsName = srsName;
        IDynamicMetadataSource dmeta = xq.getMetadataSource();
        if (dmeta instanceof DynamicMetadataSource &&
                ((DynamicMetadataSource)dmeta).getMetadataSource() instanceof DbMetadataSource) {
            dbMeta = (DbMetadataSource) ((DynamicMetadataSource)dmeta).getMetadataSource();
        }
    }

    public Crs(Node node, XmlQuery xq) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        log.trace(node.getNodeName());

        if (node != null && (
                node.getNodeName().equals(WcpsConstants.MSG_SRS_NAME) || // TODO: unify syntax to either `crs' or `srsName' in petascope.wcps.grammar
                node.getNodeName().equals(WcpsConstants.MSG_CRS))) {
            String val = node.getTextContent();
            this.crsName = val;
            //if (crsName.equals(DomainElement.IMAGE_CRS) || crsName.equals(DomainElement.WGS84_CRS)) {
            log.trace("Found CRS: " + crsName);
            //} else {
            //    throw new WCPSException("Invalid CRS: '" + crsName + "'");
            //}
        } else {
            throw new WCPSException("Could not find a 'srsName' node.");
        }

        // If coverage is not dynamic, it can be irregular: I need to query the DB to convert to pixels.
        IDynamicMetadataSource dmeta = xq.getMetadataSource();
        if (dmeta instanceof DynamicMetadataSource &&
                ((DynamicMetadataSource)dmeta).getMetadataSource() instanceof DbMetadataSource) {
            dbMeta = (DbMetadataSource) ((DynamicMetadataSource)dmeta).getMetadataSource();
        }
    }

    /**
     * Converts an interval subset to CRS:1 domain (grid indices).
     * @param covMeta       Metadata of the coverage
     * @param axisName      The axis label of the subset
     * @param stringLo      The lower bound of the subset
     * @param loIsNumeric   True if the bound is a numeric value (otherwise timestamp)
     * @param stringHi      The upper bound of the subset
     * @param hiIsNumeric   True if the bound is a numeric value (otherwise timestamp)
     * @return              The pixel indices corresponding to this subset
     * @throws WCPSException
     */
    public long[] convertToPixelIndices(CoverageMetadata covMeta, String axisName,
            String stringLo, boolean loIsNumeric, String stringHi, boolean hiIsNumeric)
            throws PetascopeException {
        return CrsUtil.convertToInternalGridIndices(covMeta, dbMeta, axisName, stringLo, loIsNumeric, stringHi, hiIsNumeric);
    }
    // Dummy overload (for DimensionPointElements)
    public long convertToPixelIndices(CoverageMetadata meta, String axisName, String value, boolean isNumeric) throws PetascopeException {
        return CrsUtil.convertToInternalGridIndices(meta, dbMeta, axisName, value, isNumeric, value, isNumeric)[0];
    }

    @Override
    public String toRasQL() {
        return crsName;
    }

    public String getName() {
        return crsName;
    }
}
