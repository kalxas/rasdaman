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
package petascope.wcps2.encodeparameters.service;

import petascope.exceptions.WCSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;
import petascope.core.BoundingBox;
import petascope.exceptions.PetascopeException;
import petascope.wcps2.encodeparameters.model.GeoReference;
import petascope.wcps2.exception.processing.InvalidBoundingBoxInCrsTransformException;
import petascope.wcps2.exception.processing.NotGeoReferencedCoverageInCrsTransformException;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;

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
        String outputCrs = metadata.getOutputCrsUri();

        // transformation from xyCrs to outputCrs
        if (outputCrs != null && !CrsUtil.isGridCrs(outputCrs) && !CrsUtil.isIndexCrs(outputCrs)) {
            // NOTE: not allow to transform from CRS:1 or IndexND to a geo-referenced CRS
            if (CrsUtil.isGridCrs(xyCrs) || CrsUtil.isIndexCrs(xyCrs)) {
                throw new NotGeoReferencedCoverageInCrsTransformException();
            }
            // transform bbox
            BoundingBox bbox = bboxExtractorService.extract(metadata);
            try {
                bbox = CrsProjectionUtil.transformBoundingBox(xyCrs, outputCrs, bbox);
            } catch (PetascopeException ex) {
                String bboxStr = "xmin=" + bbox.getXMin() + "," + "ymin=" + bbox.getYMin() + ","
                               + "xmax=" + bbox.getXMax() + "," + "ymax=" + bbox.getYMax();
                throw new InvalidBoundingBoxInCrsTransformException(bboxStr, outputCrs, ex.getMessage());
            }

            // Only get the EPSG code, e.g: http://opengis.net/def/crs/epsg/0/4326 -> epsg:4326
            String crs = CrsUtil.CrsUri.getAuthorityCode(outputCrs);
            geoReference = new GeoReference(bbox, crs);
        } else {
            // No transformation between xyCrs and outputCrs
            if (!CrsUtil.isGridCrs(xyCrs) && !CrsUtil.isIndexCrs(xyCrs)) {
                // xyCrs is geo-referenced CRS
                BoundingBox bbox = bboxExtractorService.extract(metadata);

                // Only get the EPSG code, e.g: http://opengis.net/def/crs/epsg/0/4326 -> epsg:4326
                String crs = CrsUtil.CrsUri.getAuthorityCode(xyCrs);
                geoReference = new GeoReference(bbox, crs);
            }
        }
        
        return geoReference;
    }
}
