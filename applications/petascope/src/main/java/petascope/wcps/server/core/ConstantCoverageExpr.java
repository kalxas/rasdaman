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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.core.CoverageMetadata;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.WcpsConstants;
import petascope.util.XMLSymbols;
import petascope.wcs2.templates.Templates;

public class ConstantCoverageExpr extends AbstractRasNode implements ICoverageInfo {

    private static Logger log = LoggerFactory.getLogger(ConstantCoverageExpr.class);

    private String covName;
    private List<AxisIterator> iterators;
    private ConstantList valueList;
    private CoverageInfo info;
    private String axisIteratorString;
    private int requiredListSize = 1;

    public ConstantCoverageExpr(Node node, XmlQuery xq)
            throws WCPSException, SecoreException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        log.trace(node.getNodeName());

        iterators = new ArrayList<AxisIterator>();

        while (node != null) {
            String name = node.getNodeName();
            if (name.equals(WcpsConstants.MSG_NAME)) {
                covName = node.getTextContent();
                log.trace("  " + WcpsConstants.MSG_COVERAGE + " " + covName);
            } else if (name.equals(WcpsConstants.MSG_AXIS_ITERATOR )) {
                log.trace("over: add axis iterator.");
                AxisIterator it = new AxisIterator(node.getFirstChild(), xq, WcpsConstants.MSG_TEMP);
                iterators.add(it);
            } else {
                log.trace("value list");
                valueList = new ConstantList(node, xq);
                node = valueList.getLastNode();
                super.children.add(valueList);
            }

            node = node.getNextSibling();
            while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                node = node.getNextSibling();
            }
        }

        try {
            buildMetadata(xq);
        } catch (PetascopeException pEx) {
            throw new WCPSException("Cannot build coverage metadata.", pEx);
        } catch (SecoreException sEx) {
            throw sEx;
        }
        buildAxisIteratorDomain();

        // Sanity check: dimensions should match number of constants in the list
        if (valueList.getSize() != requiredListSize) {
            throw new WCPSException("The number of constants in the list do not match the dimensions specified.");
        }
        // Sanity check: metadata should have already been build
        if (info == null) {
            throw new WCPSException("Could not build constant coverage metadata.");
        }
    }

    public String toRasQL() {
        String result = "< ";
        result += axisIteratorString + " ";
        result += valueList.toRasQL();
        result += ">";

        return result;
    }

    public CoverageInfo getCoverageInfo() {
        return info;
    }

    /* Concatenates all the AxisIterators into one large multi-dimensional object,
     * that will be used to build to RasQL query. Also counts how many elements
     * fit in the specified dimensions. */
    private void buildAxisIteratorDomain() {
        requiredListSize = 1;
        axisIteratorString = "";
        axisIteratorString += "[";

        for (int i = 0; i < iterators.size(); i++) {
            if (i > 0) {
                axisIteratorString += ", ";
            }
            AxisIterator ai = iterators.get(i);
            axisIteratorString += ai.getInterval();
            requiredListSize *= (ai.getHigh().intValue() - ai.getLow().intValue() + 1);
        }

        axisIteratorString += "]";
    }

    /** Builds full metadata for the newly constructed coverage **/
    private void buildMetadata(XmlQuery xq) throws WCPSException, PetascopeException, SecoreException {
        log.trace("Building metadata...");
        List<CellDomainElement> cellDomainList = new LinkedList<CellDomainElement>();
        List<RangeElement> rangeList = new LinkedList<RangeElement>();
        String coverageName = covName;
        List<DomainElement> domainList = new LinkedList<DomainElement>();
        List<String> crs = new ArrayList<String>(1);
        crs.add(CrsUtil.GRID_CRS);

        Iterator<AxisIterator> i = iterators.iterator();
        int order = 0;
        while (i.hasNext()) {
            // Build domain metadata
            AxisIterator ai = i.next();
            String axisName = ai.getVar();
            String axisType = ai.getAxisType();

            CellDomainElement cellDomain = new CellDomainElement(ai.getLow().toString(), ai.getHigh().toString(), order);
            DomainElement domain = new DomainElement(
                    new BigDecimal(ai.getLow()),
                    new BigDecimal(ai.getHigh()),
                    axisName,
                    axisType,
                    CrsUtil.PURE_UOM,
                    crs.get(0),
                    order,
                    BigInteger.valueOf(ai.getHigh().intValue()-ai.getLow().intValue()+1),
                    !axisType.equals(AxisTypes.Y_AXIS),
                    false); // FIXME uom = null
            cellDomainList.add(cellDomain);
            domainList.add(domain);
            order += 1;
        }

        // TODO: check element datatypes and their consistency
        // "unsigned int" is default datatype
        rangeList.add(new RangeElement(WcpsConstants.MSG_DYNAMIC_TYPE, WcpsConstants.MSG_UNSIGNED_INT, null));
        Set<Pair<String,String>> emptyMetadata = new HashSet<Pair<String,String>>();
        CoverageMetadata metadata = new CoverageMetadata(
                coverageName,
                XMLSymbols.LABEL_GRID_COVERAGE,
                "", // native format
                emptyMetadata, // extra-metadata
                crs,
                cellDomainList,
                domainList,
                Pair.of(BigInteger.ZERO, ""),
                rangeList
                );
        // Let the top-level query know the full metadata about us
        xq.getMetadataSource().addDynamicMetadata(covName, metadata);
        info = new CoverageInfo(metadata);
    }
}
