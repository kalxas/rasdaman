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

import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcps2.error.managed.processing.InvalidAxisInDomainExpressionException;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.NumericSlicing;
import petascope.wcps2.metadata.model.NumericTrimming;
import petascope.wcps2.metadata.service.CrsUtility;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;

/**
 * Translator class for the domain(coverageExpression, axisLabel, CRS) operation
 * in wcps
 * <code>
 * for c in (eobstest) return domain(c[Lat(20:30)], Lat, "http://localhost:8080/def/crs/EPSG/0/4326")
 * </code>
 * returns
 * <code>
 * * NOTE: it will not regard to the trimming expression Lat(20:30) inside coverage
 * returns full domain interval of Lat: [-40:75.5]
 *
 * for c in (eobstest) return domain(c[Lat(20:30)], Lat, "CRS:1")
 * returns
 *  [0,231] in grid-coordinate
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class DomainExpressionHandler {

    /**
     * Constructor for the class
     *
     * @param coverageExpression the coverage to get ($c,axisName, CRS)
     * @param axisName the name of axis (e.g Lat, Long,...)
     * @param axisCrs
     * @return
     */
    public static WcpsMetadataResult handle(WcpsResult coverageExpression, String axisName, String axisCrs) {

        WcpsMetadataResult metadataResult;
        // if axisName and axisCrs is belonge to coverageExpression then can just get the bounding of axis from coverageExpression
        if (isValid(coverageExpression, axisName, axisCrs)) {
            String result = getDomainByAxisCrs(coverageExpression, axisName, axisCrs);
            metadataResult = new WcpsMetadataResult(coverageExpression.getMetadata(), result);
        } else {
            throw new InvalidAxisInDomainExpressionException(axisName, axisCrs);
        }
        return metadataResult;
    }

    /**
     * Get the domain for the axis with the correct crsURI from coverageExpression
     * @param coverageExpression
     * @param axisName
     * @param crsUri
     * @return
     */
    private static String getDomainByAxisCrs(WcpsResult coverageExpression, String axisName, String axisCrs) {
        String result = "";

        Axis axis = coverageExpression.getMetadata().getAxisByName(axisName);

        if (axis.getGridBounds() instanceof NumericTrimming) {
            // Trimming
            String lowBound = "";
            String highBound = "";

            // Grid axis CRS
            if (axisCrs.contains(CrsUtil.INDEX_CRS_PREFIX) || axisCrs.equals(CrsUtil.GRID_CRS)) {
                lowBound = ((NumericTrimming)axis.getGridBounds()).getLowerLimit().toPlainString();
                highBound = ((NumericTrimming)axis.getGridBounds()).getUpperLimit().toPlainString();
            } else if (axis.getAxisType().equals(AxisTypes.T_AXIS)) {
                // Time - now only in grid axis
                lowBound = ((NumericTrimming)axis.getGridBounds()).getLowerLimit().toPlainString();
                highBound = ((NumericTrimming)axis.getGridBounds()).getUpperLimit().toPlainString();
            } else if (axis.getAxisType().equals(AxisTypes.X_AXIS)
                   || axis.getAxisType().equals(AxisTypes.Y_AXIS)) {
                // geo-referenced axis which is not grid axis (geoBounds), e.g: Lat, Long
                lowBound = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit().toPlainString();
                highBound = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit().toPlainString();
            } else {
                // Unknow axisType, use grid bounds
                lowBound = ((NumericTrimming)axis.getGridBounds()).getLowerLimit().toPlainString();
                highBound = ((NumericTrimming)axis.getGridBounds()).getUpperLimit().toPlainString();
            }

            result = TRIMMING_TEMPLATE.replace("$lowBound", lowBound).replace("$highBound", highBound);
        } else {
            // Slicing
            String bound = "";

            // Grid axis CRS
            if (axisCrs.contains(CrsUtil.INDEX_CRS_PREFIX) || axisCrs.equals(CrsUtil.GRID_CRS)) {
                bound = ((NumericSlicing)axis.getGridBounds()).getBound().toPlainString();
            } else if (axis.getAxisType().equals(AxisTypes.T_AXIS)) {
                // Time - now only in grid axis
                bound = ((NumericSlicing)axis.getGridBounds()).getBound().toPlainString();
            } else if (axis.getAxisType().equals(AxisTypes.X_AXIS)
                   || axis.getAxisType().equals(AxisTypes.Y_AXIS)) {
                // geo-referenced axis which is not grid axis (geoBounds), e.g: Lat, Long
                bound = ((NumericSlicing)axis.getGeoBounds()).getBound().toPlainString();
            } else {
                // Unknow axisType, use grid bounds
                bound = ((NumericSlicing)axis.getGridBounds()).getBound().toPlainString();
            }

            result = SLICING_TEMPLATE.replace("$lowBound", bound);
        }

        // NOTE: add this axis to axesBBox for bounding box
        coverageExpression.getMetadata().getAxesBBox().add(axis);
        return result;
    }

    /**
     * check if axisCRS is belonged to axisName (e.g Lat is "4326" and "CRS:1")
     * @param coverageExpression
     * @param axisName
     * @param crsUri
     * @return
     */
    private static boolean isValid(WcpsResult coverageExpression, String axisName, String crsUri) {
        // e.g: Index2D
        String gridCrs = CrsUtility.getImageCrsUri(coverageExpression.getMetadata());
        String gridCrsCode = CrsUtil.CrsUri.getCode(gridCrs);

         // check if axisName belonged to coverageExpression first
        for (Axis axis:coverageExpression.getMetadata().getAxes()) {
            // if coverage contains axisName then check the crsUri belonged to axis also
            if (axis.getLabel().contains(axisName)) {
                String axisCrsCode = CrsUtil.CrsUri.getCode(axis.getCrsUri());
                String inputCrsCode = CrsUtil.CrsUri.getCode(crsUri);

                if (crsUri.contains(CrsUtil.INDEX_CRS_PREFIX) || crsUri.equals(CrsUtil.GRID_CRS)) {
                    // IndexCrs always belonged to axis
                    return true;
                } else if (axisCrsCode.equals(inputCrsCode)) {
                    return true;
                } else if (inputCrsCode.equals(gridCrsCode)) {
                    // if it is "IndexND" then also accept
                    return true;
                } else {
                    // e.g: Lat:"4326" and Lat:"3857" is not identical
                    return false;
                }
            }
        }
        return false;
    }

    private static final String TRIMMING_TEMPLATE = "($lowBound:$highBound)";
    private static final String SLICING_TEMPLATE = "($lowBound)";
}