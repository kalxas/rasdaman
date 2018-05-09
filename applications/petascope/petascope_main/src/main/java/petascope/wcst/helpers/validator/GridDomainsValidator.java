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
package petascope.wcst.helpers.validator;

import org.apache.commons.lang3.math.NumberUtils;
import petascope.wcst.exceptions.WCSTInvalidComputedDomainCellCount;

import java.util.*;
import org.rasdaman.domain.cis.IndexAxis;
import org.springframework.stereotype.Service;
import petascope.util.ras.RasConstants;
import static petascope.util.ras.RasConstants.RASQL_BOUND_SEPARATION;
import static petascope.util.ras.RasConstants.RASQL_OPEN_SUBSETS;
import static petascope.util.ras.RasConstants.RASQL_CLOSE_SUBSETS;

/**
 * Validate the grid domains from the input coverage slice update request and
 * the calculated output (affectedDomain) with current existing coverage
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class GridDomainsValidator {

    /**
     * Checks that the domains are compatible: they have the same total cell
     * count and they have the same cell count by non-slice dimension.
     *
     * IndexAxis (cellDomainElement)
     *
     * @param inputCoverageDomain
     * @param affectedDomain
     * @throws WCSTInvalidComputedDomainCellCount
     */
    public void validate(List<IndexAxis> inputCoverageDomain, String affectedDomain) throws WCSTInvalidComputedDomainCellCount {
        validateDomainsCellCount(inputCoverageDomain, affectedDomain);
        validateCellCountsByDimension(inputCoverageDomain, affectedDomain);
    }

    /**
     * Check that the total cells count of persisted coverage is equivalent to
     * input coverage
     *
     * @param inputCoverageDomain
     * @param affectedDomain
     * @throws WCSTInvalidComputedDomainCellCount
     */
    private void validateDomainsCellCount(List<IndexAxis> inputCoverageDomain, String affectedDomain) throws WCSTInvalidComputedDomainCellCount {
        if (getDomainCellCount(inputCoverageDomain) != getDomainCellCount(affectedDomain)) {
            throw new WCSTInvalidComputedDomainCellCount(getCellDomainListStringRepresentation(inputCoverageDomain), affectedDomain);
        }
    }

    /**
     * Check that the total cells for each dimension is equivalent between
     * persisted coverage and input coverage
     *
     * @param inputCoverageDomain
     * @param affectedDomain
     * @throws WCSTInvalidComputedDomainCellCount
     */
    private void validateCellCountsByDimension(List<IndexAxis> inputCoverageDomain, String affectedDomain) throws WCSTInvalidComputedDomainCellCount {
        List<Long> inputDimCellCount = getNonSliceCellCounts(inputCoverageDomain);
        List<Long> affectedDimCellCount = getNonSliceCellCounts(affectedDomain);
        if (!inputDimCellCount.containsAll(affectedDimCellCount) || !affectedDimCellCount.containsAll(inputDimCellCount)) {
            throw new WCSTInvalidComputedDomainCellCount(getCellDomainListStringRepresentation(inputCoverageDomain), affectedDomain);
        }
    }

    /**
     * Get the list of total cells from non slice dimension (i.e: higher bound >
     * lower bound)
     *
     * @param domain
     * @return
     */
    private List<Long> getNonSliceCellCounts(List<IndexAxis> indexAxes) {
        List<Long> result = new ArrayList<Long>();
        for (IndexAxis indexAxis : indexAxes) {
            if (indexAxis.getUpperBound() > indexAxis.getLowerBound()) {
                result.add(indexAxis.getUpperBound() - indexAxis.getLowerBound() + 1);
            }
        }
        return result;
    }

    /**
     * Get list of total cells from affected domain of persisted coverage
     *
     * @param affectDomain
     * @return
     */
    private List<Long> getNonSliceCellCounts(String affectDomain) {
        List<Long> result = new ArrayList<>();
        String[] domainParts = affectDomain.replace(RASQL_OPEN_SUBSETS, "").replace(RASQL_CLOSE_SUBSETS, "").split(",");
        for (String domainPart : domainParts) {
            if (domainPart.contains(RASQL_BOUND_SEPARATION)) {
                Long low = new Long(domainPart.split(RASQL_BOUND_SEPARATION)[0].trim());
                Long high = new Long(domainPart.split(RASQL_BOUND_SEPARATION)[1].trim());
                if (high > low) {
                    result.add(high - low + 1);
                }
            }
        }
        return result;
    }

    /**
     *
     * Total cell counts for all dimension of input coverage
     *
     * @param indexAxes
     * @return
     */
    private Long getDomainCellCount(List<IndexAxis> indexAxes) {
        Long result = new Long("1");
        for (IndexAxis indexAxis : indexAxes) {
            result *= indexAxis.getUpperBound() - indexAxis.getLowerBound() + 1;
        }
        return result;
    }

    /**
     * Total cell counts for all affected domains of persisted coverage
     *
     * @param affectDomain
     * @return
     */
    private long getDomainCellCount(String affectDomain) {
        long result = 1;
        String[] domainParts = affectDomain.replace(RASQL_OPEN_SUBSETS, "").replace(RASQL_CLOSE_SUBSETS, "").split(",");
        for (String domainPart : domainParts) {
            if (domainPart.contains(RASQL_BOUND_SEPARATION)) {
                //trimming only, slicing doesn't add to the cell count
                long low = NumberUtils.toInt(domainPart.split(RASQL_BOUND_SEPARATION)[0].trim());
                long high = NumberUtils.toInt(domainPart.split(RASQL_BOUND_SEPARATION)[1].trim());
                result *= high - low + 1;
            }
        }
        return result;
    }

    /**
     *
     * Return the representation string of input coverage domains
     *
     * @param cellDomainElements
     * @return
     */
    private String getCellDomainListStringRepresentation(List<IndexAxis> indexAxes) {
        String result = RASQL_OPEN_SUBSETS;
        
        int count = 0;
        for (IndexAxis indexAxis : indexAxes) {
            result += indexAxis.getLowerBound() + RASQL_BOUND_SEPARATION + indexAxis.getUpperBound();
            if (count < indexAxes.size() - 1) {
                result += ",";
            }
            count++;
        }
        result += RASQL_CLOSE_SUBSETS;

        return result;
    }
}
