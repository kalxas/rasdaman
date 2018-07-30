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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.handlers.kvp.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.ListUtil;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import static petascope.wcs2.handlers.kvp.KVPWCSGetCoverageHandler.RANGE_NAME;

/**
 * Service class for Range Subset handler of GetCoverageKVP class
 *
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class KVPWCSGetCoverageRangeSubsetService {

    public KVPWCSGetCoverageRangeSubsetService() {

    }

    /**
     * Get the requested range subsets and apply the range subsets in
     * WcpsCoverageMetadata object e.g: coverage with 10 bands, and the request
     * is: rangesubset=b1:b3,b5,b7,b9:b10
     *
     * @param wcpsCoverageMetadata
     * @param rangeSubsets
     */
    public void handleRangeSubsets(WcpsCoverageMetadata wcpsCoverageMetadata, String[] rangeSubsets) throws WCSException {

        // The ranges of persisted coverage
        List<RangeField> originalRangeFields = wcpsCoverageMetadata.getRangeFields();
        // The range of translated coverage which can contain with less or more range fields than the persisted ranges.
        List<RangeField> translatedRangeFields = new ArrayList<>();

        for (String rangeSubset : rangeSubsets) {
            // It can be: b1, b3 or b5:b7,...
            if (rangeSubset.contains(":")) {
                // range subset is interval with lowerRange:upperRange
                String lowerRangeName = rangeSubset.split(":")[0].trim();
                String upperRangeName = rangeSubset.split(":")[1].trim();

                // validate the range sequence
                int lowerRangeIndex = this.getRangeFieldIndex(originalRangeFields, lowerRangeName);
                int upperRangeIndex = this.getRangeFieldIndex(originalRangeFields, upperRangeName);

                // e.g: coverage has red, green, blue ranges and request is green:red
                if (lowerRangeIndex > upperRangeIndex) {
                    throw new WCSException(ExceptionCode.IllegalFieldSequence, "Lower limit is above the upper limit in the range field interval, received: ." + rangeSubset);
                }

                // add all the ranges from lowerIndex:upperIndex as requested ranges
                for (int i = lowerRangeIndex; i <= upperRangeIndex; i++) {
                    translatedRangeFields.add(originalRangeFields.get(i));
                }
            } else {
                // range subset is 1 range name 
                int rangeIndex = this.getRangeFieldIndex(originalRangeFields, rangeSubset);

                // add this range as a requested range
                translatedRangeFields.add(originalRangeFields.get(rangeIndex));
            }
        }

        // Now, the translated request ranges override the original ranges
        wcpsCoverageMetadata.setRangeFields(translatedRangeFields);
    }

    /**
     * Generate the range constructor from the requested ranges e.g: origin
     * coverage has 3 ranges and requested with range subset: red,green { red:
     * c[i(0:10)]; green: c[i(0:10)]}
     *
     * @param wcpsCoverageMetadata
     * @param generatedCoverageExpression
     * @param rangeSubsets
     * @return
     */
    public String generateRangeConstructorWCPS(WcpsCoverageMetadata wcpsCoverageMetadata,
            String generatedCoverageExpression, String rangeSubsets) {
        if (rangeSubsets == null) {
            // If no range is requested, just use all the ranges of coverage
            return generatedCoverageExpression.replace(RANGE_NAME, "");
        } else {
            // Create the range constructor for all the requested ranges
            String rangeConstructorExpression = "{ ";

            List<String> rangeExpressions = new ArrayList<>();
            // Red: c[i(0:10]]
            for (RangeField rangeField : wcpsCoverageMetadata.getRangeFields()) {
                // replace the rangeName with the current range name: e.g c[i(0:20)].rangeName -> c[i(0:20)].Red
                rangeExpressions.add(rangeField.getName() + ": "
                        + generatedCoverageExpression.replace(RANGE_NAME, "." + rangeField.getName()));
            }

            rangeConstructorExpression += ListUtil.join(rangeExpressions, "; ") + " }";

            return rangeConstructorExpression;
        }
    }

    /**
     * Check if range name exist in list of ranges
     *
     * @param rangeName
     * @return
     */
    private int getRangeFieldIndex(List<RangeField> rangeFields, String rangeName) throws WCSException {
        int i = 0;
        for (RangeField rangeField : rangeFields) {
            if (rangeField.getName().equals(rangeName)) {
                return i;
            }
            i++;
        }
        // Cannot find the range, throw exception
        throw new WCSException(ExceptionCode.InvalidRequest, "Cannot find the rangeName: " + rangeName + " from the list of ranges.");
    }
}
