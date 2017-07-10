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
package petascope.wcps2.metadata.service;

import java.util.List;
import petascope.wcps2.exception.processing.IncompatibleBandNameInSwitchCaseExpression;
import petascope.wcps2.exception.processing.IncompatibleNumberOfBandsInSwitchCaseException;
import petascope.wcps2.metadata.model.RangeField;

/**
 * This class will provide utility method for handle RangeField from coverage
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class RangeFieldService {
    
    public static void validateRangeFields(List<RangeField> firstRangeFields, List<RangeField> secondRangeFields) {
        // check if the next case expression has the same band names and band numbers
        if (firstRangeFields.size() != secondRangeFields.size()) {
            throw new IncompatibleNumberOfBandsInSwitchCaseException(firstRangeFields.size(), secondRangeFields.size());
        } else {
            // check if all the band names are as same as the first case expression
            // e.g: case 1: return {red, green, blue}, case 2: return {red, green1, blue} is not valid
            for (int j = 0; j < firstRangeFields.size(); j++) {
                String firstRangeFieldName = firstRangeFields.get(j).getName();
                String secondRangeFieldName = secondRangeFields.get(j).getName();
                if (!firstRangeFieldName.equals(secondRangeFieldName)) {
                    throw new IncompatibleBandNameInSwitchCaseExpression(firstRangeFieldName, secondRangeFieldName);
                }
            }
        }
    }
}
