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

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.core.Metadata;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.util.WCPSConstants;

public class ScalarExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static Logger log = LoggerFactory.getLogger(ScalarExpr.class);

    private IRasNode child;
    private CoverageInfo info;
    private boolean singleNumericValue = false;
    private double dvalue;

    public ScalarExpr(Node node, XmlQuery xq) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        
        String nodeName = node.getNodeName();
        
        Node childNode = node;
        while ((childNode != null) && childNode.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
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
                    log.trace(WCPSConstants.MSG_MATCHED_METADATA_SCALAR_EXPR);
                } catch (WCPSException e) {
                    child = null;
                }
            }
        }

//            BooleanScalarExprType
        if (child == null) {
            if (BooleanScalarExpr.NODE_NAMES.contains(nodeName)) {
                try {
                    child = new BooleanScalarExpr(node, xq);
                    log.trace(WCPSConstants.MSG_MATCHED_BOOLEAN_SCALAR_EXPR);
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
                    singleNumericValue = ((NumericScalarExpr) child).isSingleValue();
                    dvalue = ((NumericScalarExpr) child).getSingleValue();
                    log.trace(WCPSConstants.MSG_MATCHED_NUMERIC_SCALAR_EXPR);
                } catch (WCPSException e) {
                    child = null;
                }
            }
        }

//            ReduceScalarExprType
        if (child == null) {
            if (node.getNodeName().equals(WCPSConstants.MSG_REDUCE)) {
                childNode = node.getFirstChild();
            }
            while ((childNode != null) && childNode.getNodeName().equals("#" + WCPSConstants.MSG_TEXT)) {
                childNode = childNode.getNextSibling();
            }
            n = childNode.getNodeName().toLowerCase();
            if (ReduceScalarExpr.NODE_NAMES.contains(n)) {
                try {
                    child = new ReduceScalarExpr(node, xq);
                    log.trace(WCPSConstants.MSG_MATCHED_REDUCE_SCALAR_EXPR);
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
                    log.trace(WCPSConstants.MSG_MATCHED_STRING_SCALAR_EXPR);
                } catch (WCPSException e) {
                    child = null;
                }
            }
        }

        // Error check
        if (child == null) {
            throw new WCPSException(WCPSConstants.ERRTXT_INVALID_COVERAGE_EXPR + ": " + node.getNodeName());
        } else {
            // Add it to the children for XML tree re-traversing
            super.children.add(child);
        }

        Metadata meta = createScalarExprMetadata(xq);
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
    private Metadata createScalarExprMetadata(XmlQuery xq) throws WCPSException {
        List<CellDomainElement> cellDomainList = new LinkedList<CellDomainElement>();
        List<RangeElement> rangeList = new LinkedList<RangeElement>();
        //HashSet<String> nullSet = new HashSet<String>();
        Set<String> nullSet = new HashSet<String>();
        String nullDefault = "0";
        nullSet.add(nullDefault);
        //HashSet<InterpolationMethod> interpolationSet = new HashSet<InterpolationMethod>();
        Set<InterpolationMethod> interpolationSet = new HashSet<InterpolationMethod>();
        InterpolationMethod interpolationDefault = new InterpolationMethod(WCPSConstants.MSG_NONE, WCPSConstants.MSG_NONE);
        interpolationSet.add(interpolationDefault);
        String coverageName = WCPSConstants.MSG_SCALAR_EXPR;
        List<DomainElement> domainList = new LinkedList<DomainElement>();

        // Build domain metadata
        cellDomainList.add(new CellDomainElement(new BigInteger("1"), new BigInteger("1"), AxisTypes.X_AXIS));
        String crs = CrsUtil.GRID_CRS;
        HashSet<String> crsset = new HashSet<String>();
        crsset.add(crs);
        Collection<String> allowedAxes = xq.getMetadataSource().getAxisNames();
        DomainElement domain = new DomainElement(AxisTypes.X_AXIS, AxisTypes.X_AXIS, 1.0, 1.0, null, null, crsset, allowedAxes, null);
        domainList.add(domain);
        // "unsigned int" is default datatype
        rangeList.add(new RangeElement(WCPSConstants.MSG_DYNAMIC_TYPE, WCPSConstants.MSG_UNSIGNED_INT, null));

        try {
            /** NOTE(campalani): nullSet and interpolationSet need to be declared
             * as "Set" to be accepted by Metadata constructor (see above). 
             * Then, Java polymorphism will understand the subtype (e.g. HashSet) on its own.
             */            
            Metadata metadata = new Metadata(cellDomainList, rangeList, nullSet,
                    nullDefault, interpolationSet, interpolationDefault,
                        coverageName, WCPSConstants.MSG_GRID_COVERAGE, domainList, null);
            return metadata;
        } catch (PetascopeException ex) {
            throw (WCPSException) ex;
        } catch (Exception ex) {
            throw (WCPSException) ex;
        }
    }

    public boolean isSingleValue() {
        return singleNumericValue;
    }

    public double getSingleValue() {
        return dvalue;
    }
    
    /** (campalani)
     * @param newD Replace numeric single value (e.g. when a coordinate transform is operated on this element).
     */
    public void setSingleValue(double newD) {
        dvalue = newD;
    }
    
    public boolean isMetadataExpr() {
        return child instanceof MetadataScalarExpr;
    }
}
