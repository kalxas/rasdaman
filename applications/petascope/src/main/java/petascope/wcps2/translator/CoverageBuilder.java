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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.translator;

import petascope.core.CoverageMetadata;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.WcpsConstants;
import petascope.util.XMLSymbols;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.CoverageInfo;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps.server.core.RangeElement;
import petascope.wcps2.error.managed.processing.CoverageMetadataException;
import petascope.wcps2.metadata.Coverage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Common class for operations that build coverages
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public abstract class CoverageBuilder extends CoverageExpression {


    /**
     * Constructs the coverage metadata needed to support this operation
     */
    protected void constructCoverageMetadata() {
        try {
            List<CellDomainElement> cellDomainList = new LinkedList<CellDomainElement>();
            List<RangeElement> rangeList = new LinkedList<RangeElement>();
            List<DomainElement> domainList = new LinkedList<DomainElement>();
            List<String> crs = new ArrayList<String>(1);
            crs.add(CrsUtil.GRID_CRS);
            int order = 0;
            for (AxisIterator ai : axisIterators) {
                // Build domain metadata
                String axisName = ai.getVariableName().toRasql();
                String axisType = ai.getAxisName();
                CellDomainElement cellDomain = new CellDomainElement(ai.getInterval().getLowerBound(), ai.getInterval().getUpperBound(), order);
                DomainElement domain = new DomainElement(new BigDecimal(ai.getInterval().getLowerBound()), new BigDecimal(ai.getInterval().getUpperBound()), axisName,
                    axisType, CrsUtil.PURE_UOM, crs.get(0), order, BigInteger.valueOf(ai.getInterval().cellCount()), false, false);
                cellDomainList.add(cellDomain);
                domainList.add(domain);
                order += 1;
            }

            // "unsigned int" is default datatype
            rangeList.add(new RangeElement(WcpsConstants.MSG_DYNAMIC_TYPE, WcpsConstants.MSG_UNSIGNED_INT, null));
            Set<Pair<String, String>> emptyMetadata = new HashSet<Pair<String, String>>();
            CoverageMetadata metadata = new CoverageMetadata(coverageName, XMLSymbols.LABEL_GRID_COVERAGE, "", emptyMetadata, crs, cellDomainList, domainList, Pair.of(BigInteger.ZERO, ""), rangeList);
            CoverageInfo covMeta = new CoverageInfo(metadata);
            Coverage coverage = new Coverage(coverageName, covMeta, metadata);
            setCoverage(coverage);
        } catch (Exception e) {
            throw new CoverageMetadataException(e);
        }
    }

    public ArrayList<AxisIterator> getAxisIterators() {
        return axisIterators;
    }

    protected ArrayList<AxisIterator> axisIterators;
    protected String coverageName;
}
