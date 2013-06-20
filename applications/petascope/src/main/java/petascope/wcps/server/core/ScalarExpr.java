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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.server.core;

import java.math.BigDecimal;
import petascope.core.CoverageMetadata;
import petascope.exceptions.WCPSException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.WcpsConstants;
import petascope.wcs2.templates.Templates;

public class ScalarExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(ScalarExpr.class);

    private IRasNode child;
    private CoverageInfo info;
    private boolean singleValue = false;
    private String value; // It can be NumericScalar or StringScalar

    public ScalarExpr(Node node, XmlQuery xq) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        
        log.trace(node.getNodeName());

        // Try one of the groups
        child = null;

//      MetadataScalarExprType
        if (child == null) {
            try {
                child = new MetadataScalarExpr(node, xq);
                log.trace(WcpsConstants.MSG_MATCHED_METADATA_SCALAR_EXPR);
            } catch (WCPSException e) {
                child = null;
            }
        }

//            BooleanScalarExprType
        if (child == null) {
            try {
                child = new BooleanScalarExpr(node, xq);
                log.trace(WcpsConstants.MSG_MATCHED_BOOLEAN_SCALAR_EXPR);
            } catch (WCPSException e) {
                child = null;
            }
        }

//            NumericScalarExprType
        if (child == null) {
            try {
                child = new NumericScalarExpr(node, xq);
                singleValue = ((NumericScalarExpr) child).isSingleValue();
                value = "" + ((NumericScalarExpr) child).getSingleValue();
                log.trace(WcpsConstants.MSG_MATCHED_NUMERIC_SCALAR_EXPR);
            } catch (WCPSException e) {
                child = null;
            }
        }

//            ReduceScalarExprType
        if (child == null) {
            try {
                child = new ReduceScalarExpr(node, xq);
                log.trace(WcpsConstants.MSG_MATCHED_REDUCE_SCALAR_EXPR);
            } catch (WCPSException e) {
                child = null;
            }
        }

//            StringScalarExprType
        if (child == null) {
            try {
                child = new StringScalarExpr(node, xq);
                singleValue = ((StringScalarExpr) child).isSingleValue();
                log.trace(WcpsConstants.MSG_MATCHED_STRING_SCALAR_EXPR);
                value = ((StringScalarExpr) child).getValue();
            } catch (WCPSException e) {
                child = null;
            }
        }

        // Error check
        if (child == null) {
            log.error("  " + WcpsConstants.ERRTXT_INVALID_COVERAGE_EXPR + ": " + node.getNodeName());
            throw new WCPSException(WcpsConstants.ERRTXT_INVALID_COVERAGE_EXPR + ": " + node.getNodeName());
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
        cellDomainList.add(new CellDomainElement(
                BigInteger.ONE, 
                BigInteger.ONE,
                0)
                );  
        domainList.add( new DomainElement(
                BigDecimal.ONE,
                BigDecimal.ONE,
                AxisTypes.X_AXIS, 
                AxisTypes.X_AXIS,
                CrsUtil.PURE_UOM,
                crs.get(0),
                0,
                BigInteger.ONE,
                false)
                );
        // "unsigned int" is default datatype
        rangeList.add(new RangeElement(WcpsConstants.MSG_DYNAMIC_TYPE, WcpsConstants.MSG_UNSIGNED_INT, null));

        try {
            Set<Pair<String,String>> emptyMetadata = new HashSet<Pair<String,String>>();
            CoverageMetadata metadata = new CoverageMetadata(
                    coverageName,
                    Templates.RECTIFIED_GRID_COVERAGE,
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
     * @param newValue Replace single value (e.g. when a coordinate transform is operated on this element).
     */
    public void setSingleValue(String newValue) {
        value = newValue;
    }
    
    public boolean isMetadataExpr() {
        return child instanceof MetadataScalarExpr;
    }
    
    // Purpose: differentiate between a numeric- and a timestamp-based temporal subset
    public boolean isStringScalarExpr() {
        return child instanceof StringScalarExpr;
    }
}
