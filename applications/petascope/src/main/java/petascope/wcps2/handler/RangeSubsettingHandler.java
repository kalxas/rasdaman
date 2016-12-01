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
package petascope.wcps2.handler;

import org.apache.commons.lang3.math.NumberUtils;
import petascope.wcps2.error.managed.processing.RangeFieldNotFound;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataService;
import petascope.wcps2.result.WcpsResult;

/**
 * Translation node from wcps to rasql for range subsetting.
 * Example:
 * <code>
 * $c1.red
 * </code>
 * translates to
 * <code>
 * c1.red
 * </code>
 * select encode(scale( ((c[*:*,*:*,0:0]).0) [*:*,*:*,0], [0:2,0:1] ), "csv") from irr_cube_2 AS c
 * SELECT encode(SCALE( ((c[*:*,*:*,0:0]).0) [*:*,*:*,0:0], [0:2,0:1]), "csv" ) FROM irr_cube_2 AS c
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class RangeSubsettingHandler {

    public static WcpsResult handle(String fieldName, WcpsResult coverageExp, WcpsCoverageMetadataService wcpsCoverageMetadataService) {

        WcpsCoverageMetadata metadata = coverageExp.getMetadata();

        String rangeField = fieldName.trim();
        if (!NumberUtils.isNumber(rangeField)) {
            if (!wcpsCoverageMetadataService.checkIfRangeFieldExists(metadata, rangeField)) {
                throw new RangeFieldNotFound(rangeField);
            }
        } else {
            int intRangeField;
            try {
                intRangeField = Integer.parseInt(rangeField);
            } catch (NumberFormatException ex) {
                //only ints supported for range subsetting
                throw new RangeFieldNotFound(rangeField);
            }
            if (!wcpsCoverageMetadataService.checkRangeFieldNumber(coverageExp.getMetadata(), intRangeField)) {
                throw new RangeFieldNotFound(rangeField);
            }
        }

        // use rangeIndex instead of rangeName
        int rangeFieldIndex = wcpsCoverageMetadataService.getRangeFieldIndex(metadata, rangeField);

        String coverageExprStr = coverageExp.getRasql().trim();
        String rasql = TEMPLATE.replace("$coverageExp", coverageExprStr)
                       .replace("$rangeFieldIndex", String.valueOf(rangeFieldIndex));

        wcpsCoverageMetadataService.removeUnusedRangeFields(metadata, rangeFieldIndex);

        // NOTE: we need to remove all the un-used range fields in coverageExpression's metadata
        // or it will add to netCDF extra metadata and have error in Rasql encoding.
        return new WcpsResult(coverageExp.getMetadata(), rasql);
    }

    private static final String TEMPLATE = "$coverageExp.$rangeFieldIndex";
}
