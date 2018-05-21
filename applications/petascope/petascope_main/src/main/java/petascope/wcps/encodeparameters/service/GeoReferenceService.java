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
package petascope.wcps.encodeparameters.service;

import petascope.util.CrsUtil;
import petascope.core.BoundingBox;
import petascope.wcps.encodeparameters.model.GeoReference;
import petascope.wcps.exception.processing.NotGeoReferencedCoverageInCrsTransformException;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Build GeoReference object
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class GeoReferenceService {
    /**
     * Build GeoReference containing crs, bbox from coverage metadata
     * @param metadata
     * @return
     */
    public GeoReference buildGeoReference(WcpsCoverageMetadata metadata) {
        GeoReference geoReference = null;
        // coverage metadata is null in case such as return condense +
        BoundingBoxExtractorService bboxExtractorService = new BoundingBoxExtractorService();
        String xyCrs = metadata.getXYCrs();

        // No transformation between xyCrs and outputCrs
        if (!CrsUtil.isGridCrs(xyCrs) && !CrsUtil.isIndexCrs(xyCrs)) {
            // xyCrs is geo-referenced CRS
            BoundingBox bbox = bboxExtractorService.extract(metadata);

            // Only get the EPSG code, e.g: http://opengis.net/def/crs/epsg/0/4326 -> epsg:4326
            String crs = CrsUtil.CrsUri.getAuthorityCode(xyCrs);
            geoReference = new GeoReference(bbox, crs);
        }
        
        return geoReference;
    }
}
