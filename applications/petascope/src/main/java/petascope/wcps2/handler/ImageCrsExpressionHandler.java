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

import petascope.wcps2.metadata.service.CrsUtility;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;

/**
 * Translator class for imageCrs of the coverage
 * Image CRS of the coverage, allowing direct grid point addressing  
 * the same image CRS is supported by all axes of a coverage.  
 * <code>
 * for c in (mr), d in (rgb) return imageCrs(c)
 * </code>
 *
 * <code>
 * Grid CRS (old: CRS:1, new: http://www.opengis.net/def/crs/OGC/0/Index2D )
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class ImageCrsExpressionHandler {

    /**
     * Return an imageCrs (Index%dD) from the coverage expression (e.g: Index2D (mr), Index3D (irr_cube_1)
     * @param coverageExpression
     * @return
     */
    public static WcpsMetadataResult handle(WcpsResult coverageExpression) {
        String imageCrsUri = CrsUtility.getImageCrsUri(coverageExpression.getMetadata());
        WcpsMetadataResult wcpsResult = new WcpsMetadataResult(coverageExpression.getMetadata(), imageCrsUri);
        return wcpsResult;
    }
}
