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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.ExceptionCode;
import petascope.core.CoverageMetadata;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.WcpsConstants;
import petascope.util.XMLSymbols;
import static petascope.wcs2.parsers.GetCoverageRequest.QUOTED_SUBSET;

public class ScalarExpr extends AbstractRasNode implements ICoverageInfo {

    private final static Logger log = LoggerFactory.getLogger(ScalarExpr.class);

    private IRasNode child;
    private final CoverageInfo info;
    private boolean singleValue = false;
    private String value; // It can be NumericScalar or StringScalar

    public ScalarExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }

        String nodeName = node.getNodeName();

        Node childNode = node;
        while ((childNode != null) && childNode.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            childNode = childNode.getNextSibling();
        }
        String n = childNode.getNodeName();

        // Try one of the groups
        child = null;

//      MetadataScalarExprType
        if (child == null) {
            if (MetadataScalarExpr.NODE_NAMES.contains(nodeName)) {
                try {
                    child = new MetadataScalarExpr(node, xq);
                    log.trace("Matched metadata scalar expression.");
                } catch (WCPSException e) {
                    // Wrong/unsupported input
                    if (e.getExceptionCode() == ExceptionCode.UnsupportedCombination) throw(e);
                    child = null;
                }
            }
        }

//            BooleanScalarExprType
        if (child == null) {
            if (BooleanScalarExpr.NODE_NAMES.contains(nodeName)) {
                try {
                    child = new BooleanScalarExpr(node, xq);
                    log.trace("Matched boolean scalar expression.");
                } catch (WCPSException e) {
                    child = null;
                }
            }
        }

//            NumericScalarExprType
        if (child == null) {
            if (NumericScalarExpr.NODE_NAMES.contains(n)) {
                try {
                    child = new NumericScalarExpr(node, xq);
                    singleValue = ((NumericScalarExpr) child).isSingleValue();
                    value = "" + ((NumericScalarExpr) child).getSingleValue();
                    log.trace("Matched numeric scalar expression.");
                } catch (WCPSException e) {
                    if (e.getExceptionCode().equals(ExceptionCode.MissingCRS) ||
                        e.getExceptionCode().equals(ExceptionCode.InvalidSubsetting)) throw(e);
                    child = null;
                }
            }
        }

//            ReduceScalarExprType
        if (child == null) {
            if (node.getNodeName().equals(WcpsConstants.MSG_REDUCE)) {
                childNode = node.getFirstChild();
            }
            while ((childNode != null) && childNode.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                childNode = childNode.getNextSibling();
            }
            String n_lower = childNode.getNodeName().toLowerCase();
            if (ReduceScalarExpr.NODE_NAMES.contains(n_lower)) {
                try {
                    child = new ReduceScalarExpr(node, xq);
                    log.trace("Matched reduce scalar expression.");
                } catch (WCPSException e) {
                    child = null;
                }
            }
        }

//            StringScalarExprType
        if (child == null) {
            if (StringScalarExpr.NODE_NAMES.contains(n)) {
                try {
                    child = new StringScalarExpr(node, xq);
                    log.trace("Matched string scalar expression.");
                    singleValue = ((StringScalarExpr) child).isSingleValue();
                    value = ((StringScalarExpr) child).getValue();
                } catch (WCPSException e) {
                    child = null;
                }
            }
        }

        // Error check
        if (child == null) {
            log.error("Invalid coverage Expression, next node: " + node.getNodeName());
            throw new WCPSException("Invalid coverage Expression, next node: " + node.getNodeName());
        } else {
            // Add it to the children for XML tree re-traversing
            super.children.add(child);
        }

        CoverageMetadata meta = createScalarExprMetadata(xq);
        info = new CoverageInfo(meta);
    }

    @Override
    public String toRasQL() {
        return child.toRasQL();
    }

    @Override
    public CoverageInfo getCoverageInfo() {
        return info;
    }

    /** Builds full metadata for the newly constructed coverage **/
    private CoverageMetadata createScalarExprMetadata(XmlQuery xq) throws WCPSException {
        List<CellDomainElement> cellDomainList = new LinkedList<CellDomainElement>();
        List<RangeElement> rangeList = new LinkedList<RangeElement>();
        String coverageName = WcpsConstants.MSG_SCALAR_EXPR;
        List<DomainElement> domainList = new LinkedList<DomainElement>();
        List<String> crs = new ArrayList<String>(1);
        crs.add(CrsUtil.GRID_CRS);

        // Build domain metadata
        cellDomainList.add(new CellDomainElement("1", "1", 0));
        domainList.add( new DomainElement(
                BigDecimal.ONE,
                BigDecimal.ONE,
                AxisTypes.X_AXIS,
                AxisTypes.X_AXIS,
                CrsUtil.PURE_UOM,
                crs.get(0),
                0,
                BigInteger.ONE,
                true, false)
                );
        // "unsigned int" is default datatype
        rangeList.add(new RangeElement(WcpsConstants.MSG_DYNAMIC_TYPE, WcpsConstants.MSG_UNSIGNED_INT, null));

        try {
            Set<Pair<String,String>> emptyMetadata = new HashSet<Pair<String,String>>();
            CoverageMetadata metadata = new CoverageMetadata(
                    coverageName,
                    XMLSymbols.LABEL_GRID_COVERAGE,
                    "", // native format
                    emptyMetadata,
                    crs,
                    cellDomainList,
                    domainList,
                    Pair.of(BigInteger.ZERO, ""),
                    rangeList
                    );
            return metadata;
        } catch (PetascopeException ex) {
            throw (WCPSException) ex;
        } catch (Exception ex) {
            throw (WCPSException) ex;
        }
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    public String getSingleValue() {
        return value;
    }

    /** (campalani)
     * @param newValue Replace single value (e.g. when a coordinate transform is operated on this element or with asterisks in trims).
     */
    public void setSingleValue(String newValue) {
        value = newValue;
    }

    public boolean isMetadataExpr() {
        return child instanceof MetadataScalarExpr;
    }

    // Purpose: differentiate between a numeric- and a timestamp-based temporal subset
    public boolean valueIsString() {
        // When an asterisk is translated to timestamp, the ScalarExpr is not String*,
        // but still quotes can help distinguish a numeric subset from a timestamp.
        return child instanceof StringScalarExpr || value.matches(QUOTED_SUBSET);
    }
}
