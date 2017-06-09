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
package petascope.wcps2.handler;

import petascope.util.CrsUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import petascope.core.CrsDefinition;
import petascope.wcps2.exception.processing.IdenticalAxisNameInCrsTransformException;
import petascope.wcps2.exception.processing.InvalidDimensionInBoundingBoxException;
import petascope.wcps2.exception.processing.InvalidOutputCrsProjectionInCrsTransformException;
import petascope.wcps2.exception.processing.Not2DXYGeoreferencedAxesCrsTransformException;
import petascope.wcps2.exception.processing.NotGeoReferenceAxisNameInCrsTransformException;
import petascope.wcps2.exception.processing.NotIdenticalCrsInCrsTransformException;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.NumericTrimming;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.result.WcpsResult;

/**
 * Class to handle an crsTransform coverage expression  <code>
 * encode(
 *      crsTransform($c, {Lat:"http://localhost:8080/def/crs/epsg/0/4326", Long:"http://localhost:8080/def/crs/epsg/0/4326"),
 *                       {b1("near", "1,2,3")},
 * "tiff", "NODATA=0")
 * </code> returns a Rasql query  <code>
 * encode(project(c, {20,30,40,50}, "EPSG:3857, EPSG:4326"),
 *        "xmin=1000,ymin=15000,xmax=2000,ymax=25000", "tiff", "NODATA=0")
 * </code>
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class CrsTransformHandler {

    /**
     * Constructor for the class
     *
     * @param coverageExpression the coverage expression that is encoded
     * @param axisCrss List of 2 coverage's axes and their CRS (e.g:
     * http://opengis.net/def/crs/epsg/0/4326)
     * @param rangeInterpolations List of ranges and their interpolation type
     * (e.g: near, bilinear, average,..) and null values (e.g: "1,2,3")
     * @param wcpsCoverageMetadataService
     * @return
     */
    public WcpsResult handle(WcpsResult coverageExpression, HashMap<String, String> axisCrss, HashMap<String, HashMap<String, String>> rangeInterpolations) {
        checkValid(axisCrss);
        String rasql = getBoundingBox(coverageExpression, axisCrss);

        WcpsCoverageMetadata metadata = coverageExpression.getMetadata();
        metadata.setOutputCrsUri(axisCrss.values().toArray()[0].toString());
        WcpsResult result = new WcpsResult(metadata, rasql);
        return result;
    }

    /**
     * Check if crsTrasnformExpression is valid
     */
    private void checkValid(HashMap<String, String> axisCrss) {

        Set<String> keys = axisCrss.keySet();
        String[] axisNameArray = keys.toArray(new String[keys.size()]);
        String[] crsArray = Arrays.copyOf(axisCrss.values().toArray(), axisCrss.values().toArray().length, String[].class);

        // 1. axisName should be different (not as {Long:CRS_A, Long:CRS_B})
        if (axisCrss.size() != 2) {
            throw new IdenticalAxisNameInCrsTransformException(axisNameArray[0], axisNameArray[0]);
        }

        // 2. it should have same axis CRS (e.g: epsg:4326)
        String crsX = CrsUtil.CrsUri.getAuthorityCode(crsArray[0]);
        String crsY = CrsUtil.CrsUri.getAuthorityCode(crsArray[1]);
        if (!crsX.equals(crsY)) {
            throw new NotIdenticalCrsInCrsTransformException(crsX, crsY);
        }

        // 3. it can only subset 2D and input coverage with geo-referenced axis (native CRS)
        // i.e: don't support to project between a geo-referenced axis (e.g: Lat:"4326")
        // and time/pressure axis (e.g:t:"ansidate")
        if (CrsDefinition.X_ALIASES.indexOf(axisNameArray[0]) == -1 && CrsDefinition.Y_ALIASES.indexOf(axisNameArray[0]) == -1) {
            throw new NotGeoReferenceAxisNameInCrsTransformException(axisNameArray[0]);
        } else if (CrsDefinition.Y_ALIASES.indexOf(axisNameArray[1]) == -1 && CrsDefinition.X_ALIASES.indexOf(axisNameArray[1]) == -1) {
            throw new NotGeoReferenceAxisNameInCrsTransformException(axisNameArray[1]);
        }

        // 4. if outputCrs is GridCRS (Index%dD or CRS:1) is not valid geo-referenced CRS to transform
        String axisCrss1 = axisCrss.values().toArray()[0].toString();
        String axisCrss2 = axisCrss.values().toArray()[1].toString();

        if (axisCrss1.contains(CrsUtil.INDEX_CRS_PREFIX) || CrsUtil.isGridCrs(axisCrss1)) {
            throw new InvalidOutputCrsProjectionInCrsTransformException(axisCrss1, axisNameArray[0]);
        } else if (axisCrss2.contains(CrsUtil.INDEX_CRS_PREFIX) || CrsUtil.isGridCrs(axisCrss2)) {
            throw new InvalidOutputCrsProjectionInCrsTransformException(axisCrss2, axisNameArray[1]);
        }
    }

    public String getBoundingBox(WcpsResult coverageExpression, HashMap<String, String> axisCrss) {
        String outputStr = "";

        // Get the calculated coverage in grid axis with Rasql
        String covRasql = coverageExpression.getRasql();

        // Get bounding box of calculated coverage
        WcpsCoverageMetadata covMetadata = coverageExpression.getMetadata();
        List<Axis> axisList = covMetadata.getXYAxes();

        // NOTE: only trimming subset is used to set bounding box and axisList need to have 2 axes (X,Y)
        // It can support 3D netCdf, so need to handle this in EncodedCoverageHandler as well
        if (axisList.size() < 2) {
            throw new Not2DXYGeoreferencedAxesCrsTransformException(axisList.size());
        }
        String xMin = String.valueOf(((NumericTrimming) axisList.get(0).getGeoBounds()).getLowerLimit().toPlainString());
        String xMax = String.valueOf(((NumericTrimming) axisList.get(0).getGeoBounds()).getUpperLimit().toPlainString());
        String yMin = String.valueOf(((NumericTrimming) axisList.get(1).getGeoBounds()).getLowerLimit().toPlainString());
        String yMax = String.valueOf(((NumericTrimming) axisList.get(1).getGeoBounds()).getUpperLimit().toPlainString());

        // Handle bounding_box to project
        String bbox = xMin + "," + yMin + "," + xMax + "," + yMax;

        // Handle source_crs, target_crs to project
        // (NOTE: sourceCrs can be compoundCrs, e.g: irr_cube_2) then need to get the crsUri from axis not from coverage metadata
        String axisName = axisCrss.keySet().iterator().next();
        Axis axis = covMetadata.getAxisByName(axisName);
        String covCRS = axis.getNativeCrsUri();
        String outputCrs = axisCrss.values().toArray()[0].toString();
        String sourceCRS = CrsUtil.CrsUri.getAuthorityCode(covCRS);
        String targetCRS = CrsUtil.CrsUri.getAuthorityCode(outputCrs);

        outputStr = TEMPLATE.replace("$COVERAGE_EXPRESSION", covRasql).replace("$BOUNDING_BOX", bbox)
                .replace("$SOURCE_CRS", sourceCRS).replace("$TARGET_CRS", targetCRS);

        return outputStr;
    }

    // e.g Rasql query: select encode(project( c[0,-10:10,51:71], "20.0,40.0,30.0,50.0", "EPSG:4326", "EPSG:32633" ),
    // "GTiff", "xmin=20000.0;xmax=300000.0;ymin=400000.0;ymax=500000.0;crs=EPSG:32633") from eobstest AS c where oid(c)=1537
    private final String TEMPLATE = "project( $COVERAGE_EXPRESSION, \"$BOUNDING_BOX\", \"$SOURCE_CRS\", \"$TARGET_CRS\" )";

}
