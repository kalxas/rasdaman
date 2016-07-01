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
package petascope.wcps2.handler;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.NumericTrimming;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;

/**
 * Translator class for the imageCrsDomain(coverageExpression, axisLabel)
 * operation in wcps
 *
 * imageCrsDomain (coverageExpr):
 * for c in (eobstest) return imageCrsDomain(c[Lat(20:30), Long(30:40)])
 * returns
 * (0:5, 60:70, 20:30) in grid-coordinate (t, Long, Lat) respectively
 *
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class ImageCrsDomainExpressionHandler {

    public static WcpsMetadataResult handle(WcpsResult coverageExpression) {
        // just iterate the axes and get the grid bound for each axis
        String rasql = "";
        List<String> axisBounds = new ArrayList<String>();
        List<Axis> axesBBox = coverageExpression.getMetadata().getAxesBBox();

        for (Axis axis : coverageExpression.getMetadata().getAxes()) {
            // This is used to set bounding box in case of scale() or extend() with imageCrsdomain()
            String tmp = "";
            if (axis.getGridBounds() instanceof NumericTrimming) {
                // Trimming
                // NOTE: not add slice for bounding box e.g: Lat(0), need to check if NumericSubset is slicing or trimming
                String lowBound = ((NumericTrimming)axis.getGridBounds()).getLowerLimit().toPlainString();
                String highBound = ((NumericTrimming)axis.getGridBounds()).getUpperLimit().toPlainString();
                tmp = TRIMMING_TEMPLATE.replace("$lowBound", lowBound).replace("$highBound", highBound);

                // No add slicing axis to bounding box
                axesBBox.add(axis);

                // Only add trimming domain interval to Rasql
                // e.g: imageCrsDomain(c[t("1950-01-01"), Long(43:44), Lat(24:25)]) - return (43:44,24:25)
                axisBounds.add(tmp);
            }
        }

        // (0:5,0:100,0:231)
        rasql = "(" + StringUtils.join(axisBounds, ",") + ")";
        WcpsMetadataResult wcpsMetadataResult = new WcpsMetadataResult(coverageExpression.getMetadata(), rasql);
        return wcpsMetadataResult;
    }

    private static final String TRIMMING_TEMPLATE = "$lowBound:$highBound";
    private static final String SLICING_TEMPLATE = "$lowBound";
    public static final String IMAGE_CRS_DOMAIN = "imageCrsdomain";
}
