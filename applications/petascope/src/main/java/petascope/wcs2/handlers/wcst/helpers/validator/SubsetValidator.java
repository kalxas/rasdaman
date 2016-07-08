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
package petascope.wcs2.handlers.wcst.helpers.validator;

import org.apache.commons.lang3.math.NumberUtils;
import petascope.exceptions.wcst.WCSTInvalidComputedDomainCellCount;
import petascope.wcps.metadata.CellDomainElement;

import java.util.*;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class SubsetValidator {

    /**
     * Checks that the domains are compatible: they have the same total cell count and they have the same
     * cell count by non-slice dimension.
     * @param inputCoverageDomain
     * @param affectedDomain
     * @throws WCSTInvalidComputedDomainCellCount
     */
    public void validate(List<CellDomainElement> inputCoverageDomain, String affectedDomain) throws WCSTInvalidComputedDomainCellCount {
        validateDomainsCellCount(inputCoverageDomain, affectedDomain);
        validateCellCountsByDimension(inputCoverageDomain, affectedDomain);
    }

    private void validateDomainsCellCount(List<CellDomainElement> inputCoverageDomain, String affectedDomain) throws WCSTInvalidComputedDomainCellCount {
        if(getDomainCellCount(inputCoverageDomain) != getDomainCellCount(affectedDomain)){
            throw new WCSTInvalidComputedDomainCellCount(getCellDomainListStringRepresentation(inputCoverageDomain), affectedDomain);
        }
    }

    private void validateCellCountsByDimension(List<CellDomainElement> inputCoverageDomain, String affectedDomain) throws WCSTInvalidComputedDomainCellCount {
        List<Integer> inputDimCellCount = getNonSliceCellCounts(inputCoverageDomain);
        List<Integer> affectedDimCellCount = getNonSliceCellCounts(affectedDomain);
        if(!inputDimCellCount.containsAll(affectedDimCellCount) || !affectedDimCellCount.containsAll(inputDimCellCount)){
            throw new WCSTInvalidComputedDomainCellCount(getCellDomainListStringRepresentation(inputCoverageDomain), affectedDomain);
        }
    }

    private List<Integer> getNonSliceCellCounts(List<CellDomainElement> domain){
        List<Integer> result = new ArrayList<Integer>();
        for(CellDomainElement cellDomainElement: domain){
            if(cellDomainElement.getHiInt() > cellDomainElement.getLoInt()){
                result.add(Integer.valueOf(cellDomainElement.getHiInt() - cellDomainElement.getLoInt() + 1));
            }
        }
        return result;
    }

    private List<Integer> getNonSliceCellCounts(String domain){
        List<Integer> result = new ArrayList<Integer>();
        String[] domainParts = domain.replace("[", "").replace("]", "").split(",");
        for(String domainPart : domainParts) {
            if (domainPart.contains(":")) {
                int low = NumberUtils.toInt(domainPart.split(":")[0].trim());
                int high = NumberUtils.toInt(domainPart.split(":")[1].trim());
                if(high > low){
                    result.add(Integer.valueOf(high - low + 1));
                }
            }
        }
        return result;
    }

    private int getDomainCellCount(List<CellDomainElement> domain){
        int result = 1;
        for(CellDomainElement domainElement: domain){
            result *= domainElement.getHiInt() - domainElement.getLoInt() + 1;
        }
        return result;
    }

    private int getDomainCellCount(String domain){
        int result = 1;
        String[] domainParts = domain.replace("[", "").replace("]", "").split(",");
        for(String domainPart : domainParts){
            if(domainPart.contains(":")) {
                //trimming only, slicing doesn't add to the cell count
                int low = NumberUtils.toInt(domainPart.split(":")[0].trim());
                int high = NumberUtils.toInt(domainPart.split(":")[1].trim());
                result *= high - low + 1;
            }
        }
        return result;
    }

    private String getCellDomainListStringRepresentation(List<CellDomainElement> cellDomainElements){
        String result = "[";
        int count = 0;
        for(CellDomainElement cellDomainElement: cellDomainElements){
            result += cellDomainElement.getLo() + ":" + cellDomainElement.getHi();
            if(count < cellDomainElements.size() - 1){
                result += ",";
            }
            count++;
        }
        result += "]";
        return result;
    }
}
