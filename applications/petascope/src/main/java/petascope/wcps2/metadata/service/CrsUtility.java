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
import petascope.core.CrsDefinition;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import static petascope.util.CrsUtil.CrsUri;

/**
 * This class will provide utility method for handle crs
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class CrsUtility {

    /**
     * Return the axisType (x, y, t) from an axis
     *
     * @param axis
     * @return
     */
    public static String getAxisType(Axis axis) {
        String axisName = axis.getLabel();
        String crsUri = axis.getCrsUri();

        String axisType = "";

        // e.g: return x for axis "Long", type y for axis "Lat", type t for "t/ansi/" based on crs (not by axisName)
        CrsDefinition crsDefinition = getCrsDefinitionByCrsUri(crsUri);
        axisType = CrsUtil.getAxisType(crsDefinition, axisName);

        return axisType;
    }

    /**
     * Return a CrsDefinition from CrsUri
     * @param crsUri
     * @return
     */
    public static CrsDefinition getCrsDefinitionByCrsUri(String crsUri) {
        String authority = CrsUtil.CrsUri.getAuthority(crsUri);
        String version = CrsUtil.CrsUri.getVersion(crsUri);
        String code = CrsUtil.CrsUri.getCode(crsUri);
        String type = null;

        CrsDefinition crsDefinition = new CrsDefinition(authority, version, code, type);
        return crsDefinition;
    }

    /**
     *
     * imageCrsUri is just a grid CRS (IndexND) for all axes (not as compoundCrs
     * or geo-referenced CRS) usage: for c in (mr) return imageCrs(c), return:
     * http://.../Index2D
     *
     * @param coverageExpression
     * @return
     */
    public static String getImageCrsUri(WcpsCoverageMetadata coverageExpression) {
        // replace "Index%dD" with the number of axes in coverage
        String imageCrsUri = CrsUtil.OPENGIS_INDEX_ND_PATTERN;
        int numberOfAxes = coverageExpression.getAxes().size();
        imageCrsUri = imageCrsUri.replace(CrsUtil.INDEX_CRS_PATTERN_NUMBER, String.valueOf(numberOfAxes));
        return imageCrsUri;
    }

    /**
     * create a grid CRS (IndexND) from list of axis
     *
     * @param axes
     * @return
     */
    public static String getImageCrsUri(List<Axis> axes) {
        // replace "Index%dD" with the number of axes in coverage
        String imageCrsUri = CrsUtil.OPENGIS_INDEX_ND_PATTERN;
        int numberOfAxes = axes.size();
        imageCrsUri = imageCrsUri.replace(CrsUtil.INDEX_CRS_PATTERN_NUMBER, String.valueOf(numberOfAxes));
        return imageCrsUri;
    }

    /**
     * Only strip quotes ("") if first character and last character of crsUri is
     * "" (e.g: ""http://..../4326"")
     *
     * @param crsUri
     * @return stripped String
     */
    public static String stripBoundingQuotes(String crsUri) {
        if (crsUri.startsWith("\"") && crsUri.endsWith("\"")) {
            return crsUri.substring(1, crsUri.length() - 1);
        }
        return crsUri;
    }

    /**
     * Check if provided axisName and a CRS in subset dimension is identical
     * with the axis in coverage
     *
     * @param axisName
     * @param axisCrs
     * @param wcpsCoverageMetadata
     * @return
     */
    public static boolean identicalCrsCode(String axisName, String axisCrs, WcpsCoverageMetadata wcpsCoverageMetadata) {
        String crsCode = CrsUri.getCode(axisCrs);

        // native axis
        Axis nativeAxis = wcpsCoverageMetadata.getAxisByName(axisName);
        String nativeAxisCrs = nativeAxis.getCrsUri();
        String nativeCrsCode = CrsUri.getCode(nativeAxisCrs);

        // if same as: epsg:4326, epsg:4326 then it is identical
        if (crsCode.equals(nativeCrsCode)) {
            return true;
        }
        return false;
    }

    /**
     * Check if a SubsetDimension uses a subsettingCrs which is as same as
     * coverage's native Crs If not then this dimension need to be transformed
     * with the provided subsettingCrs e.g: encode(c[t(1),
     * Long:"http://..../0/3857"(120000:130000),
     * Lat:"http://.../0/3857"(15000:160000)], "tiff", "nodata=0") then
     * 120000:130000 should be convert from 3857 to 4326 of Lat This will also
     * check if IndexND belonged to the coverage (e.g: it should be identical
     * Index2D).
     *
     * @param axisName
     * @param axisCrs
     * @param wcpsCoverageMetadata
     * @return
     */
    public static boolean geoReferencedSubsettingCrs(String axisName, String axisCrs, WcpsCoverageMetadata wcpsCoverageMetadata) {
        // First check only support subsettingCrs in geo-referenced axis (not t)
        // native axis
        Axis nativeAxis = wcpsCoverageMetadata.getAxisByName(axisName);
        if ( (nativeAxis.getAxisType().equals(AxisTypes.X_AXIS))
             || (nativeAxis.getAxisType().equals(AxisTypes.Y_AXIS)) ) {
            // NOTE: if subsettingCrs is Index%d then it will not need to transform
            // TODO: Remove GRID_CRS support soon
            if (axisCrs.contains(CrsUtil.INDEX_CRS_PREFIX) || axisCrs.equals(CrsUtil.GRID_CRS)) {
                return false;
            } else if (!identicalCrsCode(axisName, axisCrs, wcpsCoverageMetadata)) {
                return true;
            }
        }

        return false;
    }
}
