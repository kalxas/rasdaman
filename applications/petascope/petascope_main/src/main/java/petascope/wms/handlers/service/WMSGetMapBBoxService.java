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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wms.handlers.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.AxisTypes;
import petascope.core.BoundingBox;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.CrsProjectionUtil;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.ParsedSubset;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.service.CoordinateTranslationService;
import petascope.wcps.metadata.service.SubsetParsingService;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;
import petascope.wms.handlers.model.WMSLayer;

/**
 * Utility for handling WMS GetMap bounding box
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class WMSGetMapBBoxService {

    @Autowired
    private SubsetParsingService subsetParsingService;
    @Autowired
    private CoordinateTranslationService coordinateTranslationService;
    @Autowired
    private WMSGetMapWCPSMetadataTranslatorService wmsGetMapWCPSMetadataTranslatorService;


    /**
     * NOTE: GDAL always transform with XY order (i.e: Rasdaman grid order), not
     * by CRS order. So request from WMS must be swapped from YX order to XY
     * order for bounding box.
     */
    public BoundingBox swapYXBoundingBox(BoundingBox inputBBox, String sourceCrs) throws PetascopeException, SecoreException {
        String crsUri = CrsUtil.getEPSGFullUri(sourceCrs);
        CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(crsUri);        // x, y, t,... 
        BigDecimal minX = inputBBox.getXMin();
        BigDecimal minY = inputBBox.getYMin();
        BigDecimal maxX = inputBBox.getXMax();
        BigDecimal maxY = inputBBox.getYMax();

        BoundingBox bbox = new BoundingBox();
        bbox.setXMin(new BigDecimal(inputBBox.getXMin().toPlainString()));
        bbox.setYMin(new BigDecimal(inputBBox.getYMin().toPlainString()));
        bbox.setXMax(new BigDecimal(inputBBox.getXMax().toPlainString()));
        bbox.setYMax(new BigDecimal(inputBBox.getYMax().toPlainString()));

        if (crsDefinition.getAxes().get(0).getType().equals(AxisTypes.Y_AXIS)) {
            // CRS axis is YX order so must swap the input bbox to XY order (e.g: EPSG:4326 (Lat, Long) to Long, Lat.
            BigDecimal minTemp = new BigDecimal(minX.toPlainString());
            BigDecimal maxTemp = new BigDecimal(maxX.toPlainString());
            minX = new BigDecimal(minY.toPlainString());
            minY = new BigDecimal(minTemp.toPlainString());
            maxX = new BigDecimal(maxY.toPlainString());
            maxY = new BigDecimal(maxTemp.toPlainString());

            bbox.setXMin(minX);
            bbox.setYMin(minY);
            bbox.setXMax(maxX);
            bbox.setYMax(maxY);
        }

        return bbox;
    }
    
    /**
     * Given 3 bounding boxes:
     * originalRequestBBox (bbox in GetMap request, e.g: in EPSG:4326)
     * layerBBoxRequestCRS (bbox of a layer in the request CRS, e.g: EPSG:4326)
     * layerBBoxNativeCRS (bbox of a layer from its native CRS, e.g: EPSG:32632)
     * 
     * Get the intersected bbox between the original request bbox and the layer's bbox in native CRS.
     * For example, requestBBox (1) is the whole map in EPSG:4326, Long-Lat order (-180,-90,180,90)
     * and the layer is a Sentinel 2 UTM 3262 scene. Then, project the geo bounds of this layer from EPSG:32632 -> EPSG:4326 (2).
     * Find the intersection between (1) and (2).
     * Project the intersection from EPSG:4326 to UTM:32632
     */
    public BoundingBox getIntersectedBBoxInNativeCRS(BoundingBox originalRequestBBox, 
                                                     BoundingBox layerBBoxRequestCRS, BoundingBox layerBBoxNativeCRS,
                                                     String requestCRS, String nativeCRS) throws PetascopeException, WCSException, SecoreException {
        BigDecimal xMinRequestCRS;
        BigDecimal yMinRequestCRS;
        BigDecimal xMaxRequestCRS;
        BigDecimal yMaxRequestCRS;
        
        // X axis
        if (originalRequestBBox.getXMin().compareTo(layerBBoxRequestCRS.getXMin()) >= 0            
            && originalRequestBBox.getXMax().compareTo(layerBBoxRequestCRS.getXMax()) <= 0) {
            
            // request within layer's bbox
            xMinRequestCRS = originalRequestBBox.getXMin();
            xMaxRequestCRS = originalRequestBBox.getXMax();
        } else if (originalRequestBBox.getXMin().compareTo(layerBBoxRequestCRS.getXMin()) < 0            
            && originalRequestBBox.getXMax().compareTo(layerBBoxRequestCRS.getXMax()) > 0) {
            
            // request contains layers' bbox
            xMinRequestCRS = layerBBoxRequestCRS.getXMin();
            xMaxRequestCRS = layerBBoxRequestCRS.getXMax();
        } else {
            // intersect
            
            xMinRequestCRS = originalRequestBBox.getXMin().compareTo(layerBBoxRequestCRS.getXMin()) > 0 
                           ? originalRequestBBox.getXMin() : layerBBoxRequestCRS.getXMin();
            xMaxRequestCRS = originalRequestBBox.getXMax().compareTo(layerBBoxRequestCRS.getXMax()) < 0 
                           ? originalRequestBBox.getXMax() : layerBBoxRequestCRS.getXMax();
        }
        
        // Y axis
        if (originalRequestBBox.getYMin().compareTo(layerBBoxRequestCRS.getYMin()) >= 0            
            && originalRequestBBox.getYMax().compareTo(layerBBoxRequestCRS.getYMax()) <= 0) {
            
            // request within layer's bbox
            yMinRequestCRS = originalRequestBBox.getYMin();
            yMaxRequestCRS = originalRequestBBox.getYMax();
        } else if (originalRequestBBox.getYMin().compareTo(layerBBoxRequestCRS.getYMin()) < 0            
            && originalRequestBBox.getYMax().compareTo(layerBBoxRequestCRS.getYMax()) > 0) {
            
            // request contains layers' bbox
            yMinRequestCRS = layerBBoxRequestCRS.getYMin();
            yMaxRequestCRS = layerBBoxRequestCRS.getYMax();
        } else {
            // intersect
            
            yMinRequestCRS = originalRequestBBox.getYMin().compareTo(layerBBoxRequestCRS.getYMin()) > 0 
                           ? originalRequestBBox.getYMin() : layerBBoxRequestCRS.getYMin();
            yMaxRequestCRS = originalRequestBBox.getYMax().compareTo(layerBBoxRequestCRS.getYMax()) < 0 
                           ? originalRequestBBox.getYMax() : layerBBoxRequestCRS.getYMax();
        }
        
        
        // e.g: in request CRS (EPSG:4326)
        BoundingBox intersectionBBoxRequestCRS = new BoundingBox(xMinRequestCRS, yMinRequestCRS, xMaxRequestCRS, yMaxRequestCRS);
        // e.g: in layer's native CRS (EPSG:32632)
        BoundingBox intersectBBoxNativeCRS = this.transformBoundingBox(intersectionBBoxRequestCRS, requestCRS, nativeCRS);
        
        return intersectBBoxNativeCRS;        
    }

    /**
     * Transform the input BBox from sourceCrs to targetCrs
     */
    public BoundingBox transformBoundingBox(BoundingBox inputBBox, String sourceCRS, String targetCRS)
            throws WCSException, PetascopeException, SecoreException {

        // Beware! Inserted values pairs needs to be in order X-coordinate and then Y-coordinate.
        // If you are inserting latitude/longitude values in decimal format, then the longitude should be first value of the pair (X-coordinate) and latitude the second value (Y-coordinate).        
        BigDecimal xMin = inputBBox.getXMin();
        BigDecimal yMin = inputBBox.getYMin();
        BigDecimal xMax = inputBBox.getXMax();
        BigDecimal yMax = inputBBox.getYMax();
        
        BoundingBox sourceCRSBBOX = new BoundingBox(xMin, yMin, xMax, yMax);
        BoundingBox targetCRSBBox = CrsProjectionUtil.transformBBox(sourceCRSBBOX, sourceCRS, targetCRS);
        
        return targetCRSBBox;
    }

    /**
     * If request bbox is outside of first layer's geo XY axes bounds, adjust it
     * to fit with coverage's geo XY axes bounds to avoid server killed by Rasql
     * query.
     */
    public void fitBBoxToCoverageGeoXYBounds(BoundingBox bbox, String layerName) throws PetascopeException {
        WcpsCoverageMetadata wcpsCoverageMetadata = this.wmsGetMapWCPSMetadataTranslatorService.translate(layerName);

        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();
        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);

        BigDecimal originalGeoLowerBoundX = axisX.getGeoBounds().getLowerLimit();
        BigDecimal originalGeoUpperBoundX = axisX.getGeoBounds().getUpperLimit();

        BigDecimal originalGeoLowerBoundY = axisY.getGeoBounds().getLowerLimit();
        BigDecimal originalGeoUpperBoundY = axisY.getGeoBounds().getUpperLimit();

        NumericSubset geoSubsetX = new NumericTrimming(bbox.getXMin(), bbox.getXMax());
        NumericSubset geoSubsetY = new NumericTrimming(bbox.getYMin(), bbox.getYMax());

        axisX.setGeoBounds(geoSubsetX);
        axisY.setGeoBounds(geoSubsetY);

        List<Subset> subsets = new ArrayList<>();
        subsets.add(new Subset(geoSubsetX, axisX.getNativeCrsUri(), axisX.getLabel()));
        subsets.add(new Subset(geoSubsetY, axisY.getNativeCrsUri(), axisY.getLabel()));

        subsetParsingService.fitToSampleSpaceRegularAxes(subsets, wcpsCoverageMetadata);

        if (bbox.getXMin().compareTo(originalGeoLowerBoundX) < 0) {
            bbox.setXMin(originalGeoLowerBoundX);
        }
        if (bbox.getXMax().compareTo(originalGeoUpperBoundX) > 0) {
            bbox.setXMax(originalGeoUpperBoundX);
        }

        if (bbox.getYMin().compareTo(originalGeoLowerBoundY) < 0) {
            bbox.setYMin(originalGeoLowerBoundY);
        }
        if (bbox.getYMax().compareTo(originalGeoUpperBoundY) > 0) {
            bbox.setYMax(originalGeoUpperBoundY);
        }
        
        
        if (bbox.getXMin().compareTo(bbox.getXMax()) > 0) {
            BigDecimal temp = new BigDecimal(bbox.getXMin().toPlainString());
            bbox.setXMin(bbox.getXMax());
            bbox.setXMax(temp);
        }
        if (bbox.getYMin().compareTo(bbox.getYMax()) > 0) {
            BigDecimal temp = new BigDecimal(bbox.getYMin().toPlainString());
            bbox.setYMin(bbox.getYMax());
            bbox.setYMax(temp);
        }
    }

    /**
     * When projection is needed, create an extended Geo BBox from original
     * BBox.
     */
    public BoundingBox createExtendedGeoBBox(Axis axisX, Axis axisY, BoundingBox requestBBox) {
        
        BigDecimal offsetGeoX = requestBBox.getXMax().subtract(requestBBox.getXMin());
        BigDecimal offsetGeoY = requestBBox.getYMax().subtract(requestBBox.getYMin());
        
        // This is used only when zooming to maximum level to not show gaps 
        // or at the corners of layer
        BigDecimal minOffsetGeoX = axisX.getResolution().multiply(new BigDecimal(2)).abs();
        BigDecimal minOffsetGeoY = axisY.getResolution().multiply(new BigDecimal(2)).abs();
        
        if (offsetGeoX.compareTo(minOffsetGeoX) < 0) {
            offsetGeoX = minOffsetGeoX;
        }
        
        if (offsetGeoY.compareTo(minOffsetGeoY) < 0) {
            offsetGeoY = minOffsetGeoY;
        }
        
        BigDecimal newGeoLowerBoundX = requestBBox.getXMin().subtract(offsetGeoX);
        BigDecimal newGeoUpperBoundX = requestBBox.getXMax().add(offsetGeoX);
        
        BigDecimal newGeoLowerBoundY = requestBBox.getYMin().subtract(offsetGeoY);
        BigDecimal newGeoUpperBoundY = requestBBox.getYMax().add(offsetGeoY);
        
        BoundingBox extendedFittedGeoBBbox = new BoundingBox(newGeoLowerBoundX, newGeoLowerBoundY, newGeoUpperBoundX, newGeoUpperBoundY);
        return extendedFittedGeoBBbox;
    }

    
    /**
     * When projection is needed, from ExtendedGeoBBox, calculate grid bounds
     * for it.
     */
    public BoundingBox createExtendedGridBBox(Axis axisX, Axis axisY, BoundingBox extendedFittedGeoBBbox) throws PetascopeException {

        ParsedSubset<BigDecimal> parsedGeoSubsetX = new ParsedSubset<>(extendedFittedGeoBBbox.getXMin(), extendedFittedGeoBBbox.getXMax());
        WcpsSubsetDimension subsetDimensionX = new WcpsTrimSubsetDimension(axisX.getLabel(), axisX.getNativeCrsUri(),
                parsedGeoSubsetX.getLowerLimit().toPlainString(), parsedGeoSubsetX.getUpperLimit().toPlainString());

        BoundingBox extendedFittedGridBBox = new BoundingBox();

        ParsedSubset<Long> parsedGridSubsetX = coordinateTranslationService.geoToGridSpatialDomain(axisX, subsetDimensionX, parsedGeoSubsetX);
        extendedFittedGridBBox.setXMin(new BigDecimal(parsedGridSubsetX.getLowerLimit()));
        extendedFittedGridBBox.setXMax(new BigDecimal(parsedGridSubsetX.getUpperLimit()));

        ParsedSubset<BigDecimal> parsedGeoSubsetY = new ParsedSubset<>(extendedFittedGeoBBbox.getYMin(), extendedFittedGeoBBbox.getYMax());
        WcpsSubsetDimension subsetDimensionY = new WcpsTrimSubsetDimension(axisY.getLabel(), axisY.getNativeCrsUri(),
                parsedGeoSubsetY.getLowerLimit().toPlainString(), parsedGeoSubsetY.getUpperLimit().toPlainString());
        ParsedSubset<Long> parsedGridSubsetY = coordinateTranslationService.geoToGridSpatialDomain(axisY, subsetDimensionY, parsedGeoSubsetY);
        extendedFittedGridBBox.setYMin(new BigDecimal(parsedGridSubsetY.getLowerLimit()));
        extendedFittedGridBBox.setYMax(new BigDecimal(parsedGridSubsetY.getUpperLimit()));

        return extendedFittedGridBBox;
    }

    /**
     * In case of requesting bounding box in a CRS which is different from
     * layer's native CRS, it translates request BBOX from source CRS to native
     * CRS then extend this transformed BBox by a constant in both width and
     * height sizes.
     *
     * This allows rasdaman to query result without missing values in the
     * corners because of projection() will return a rotated result.
     */
    public BoundingBox createExtendedGeoBBox(WMSLayer wmsLayer) throws PetascopeException {

        WcpsCoverageMetadata wcpsCoverageMetadata = this.wmsGetMapWCPSMetadataTranslatorService.createWcpsCoverageMetadataForDownscaledLevelByOriginalXYBBox(wmsLayer);
        List<Axis> xyAxes = wcpsCoverageMetadata.getXYAxes();

        Axis axisX = xyAxes.get(0);
        Axis axisY = xyAxes.get(1);

        BoundingBox extendedGeoBBox = this.createExtendedGeoBBox(axisX, axisY, wmsLayer.getRequestBBox());
        
        return extendedGeoBBox;
    }

}
