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
package org.rasdaman.migration.service.wms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.wms.BoundingBox;
import org.rasdaman.domain.wms.EXGeographicBoundingBox;
import org.rasdaman.domain.wms.Layer;
import org.rasdaman.domain.wms.LayerAttribute;
import org.rasdaman.domain.wms.Style;
import org.rasdaman.migration.legacy.LegacyWMSEXGeographicBoundingBox;
import org.rasdaman.migration.legacy.LegacyWMSLayer;
import org.rasdaman.migration.legacy.LegacyWMSStyle;
import org.rasdaman.repository.interfaces.AbstractCoverageRepository;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.wms.exception.WMSInvalidDimensionalityException;

/**
 *
 * Class which translates the legacy WMS 1.3 to new one and persist to database
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class WMSLayerTranslatingService {

    private static final Logger log = LoggerFactory.getLogger(WMSLayerTranslatingService.class);

    @Autowired
    private AbstractCoverageRepository abstractCoverageRepository;

    /**
     * Create a WMS layer from legacy WMS layer
     *
     * @param legacyLayer
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     * @throws petascope.wms.exception.WMSInvalidDimensionalityException
     */
    public Layer create(LegacyWMSLayer legacyLayer) throws PetascopeException, SecoreException, WMSInvalidDimensionalityException {;

        Layer layer = new Layer();
        // Some main properties from legacy WMS   
        // NOTE: a layer name in WMS is a WCS coverage id (and with legacy WMS only supports 2D coverages).
        // Also the CRS is missing from layer in legacy object, so need to fetch it from the new persistent coverage.
        String layerName = legacyLayer.getName();
        Coverage coverage = this.abstractCoverageRepository.findOneByCoverageId(layerName);
        if (coverage == null) {
            log.info("*******Coverage for WMS 1.3 layer: " + layerName + " does not exist, please migrate coverage's metadata first, so layer will not be migrated.*******");
            return null;
        }
        
        // NOTE: Add coverage's CRS was stripped SECORE prefix with string place holder when persisting in database, add it back here.
        CoverageRepostioryService.addCrsPrefix(coverage);
        
        // e.g: EPSG:4326
        String crs = CrsUtil.getEPSGCode(coverage.getEnvelope().getEnvelopeByAxis().getSrsName());
        // crs needs to be stripped SECORE prefix as well
        crs = CrsUtil.CrsUri.toDbRepresentation(crs);

        layer.setName(layerName);
        layer.setTitle(legacyLayer.getTitle());
        layer.setLayerAbstract(legacyLayer.getLayerAbstract());
        layer.setCrss(ListUtil.valuesToList(crs));

        // Some optional properties for layer (which is the same from legacy WMS layer), e.g: opaque, cascaded, queryable,...
        LayerAttribute layerAttribute = new LayerAttribute();
        layer.setLayerAttribute(layerAttribute);

        // Each layer can contain multiple bounding boxes but legacy database only contain one
        // NOTE: legacy boundingbox's CRS from the CRS of 2D geo XY axes (layer's CRS)
        BoundingBox bbox = this.createBoundingBox(coverage);
        bbox.setCrs(crs);
        layer.setBoundingBoxes(ListUtil.valuesToList(bbox));

        // Each layer can contain multiple styles and one EXGeographicBoundingBox
        EXGeographicBoundingBox exBBox = this.createEXBBox(legacyLayer.getExBBox());
        List<Style> styles = this.createStyles(legacyLayer.getStyles());

        layer.setExGeographicBoundingBox(exBBox);
        layer.setStyles(styles);

        return layer;
    }

    /**
     * Create a BoundingBox object from the persistent coverage.
     *
     * @param coverage
     * @return
     */
    private BoundingBox createBoundingBox(Coverage coverage) throws PetascopeException, SecoreException, WMSInvalidDimensionalityException {
        BoundingBox boundingBox = new BoundingBox();
        // Bounding box also does not exist in legacy database, so just fetch it with current 2D geo bounds
        // NOTE: WMS 1.3, the XY order is depent on the CRS order (e.g: EPSG:4326 is Lat, Long so minx (actually is minLat) and maxx (actually is maxLat)).
        List<GeoAxis> geoAxes = ((GeneralGridCoverage) coverage).getGeoAxes();
        if (geoAxes.size() > 2) {
            throw new WMSInvalidDimensionalityException(geoAxes.size());

        }
        int i = 0;
        for (GeoAxis geoAxis : geoAxes) {
            if (i == 0) {
                boundingBox.setMinx(geoAxis.getLowerBoundNumber());
                boundingBox.setMaxx(geoAxis.getUpperBoundNumber());
            } else {
                boundingBox.setMiny(geoAxis.getLowerBoundNumber());
                boundingBox.setMaxy(geoAxis.getUpperBoundNumber());
            }
            i++;
        }

        return boundingBox;
    }

    /**
     * Create a list of WMS styles from legacy list of WMS styles for a layer.
     *
     * @param legacyStyles
     * @return
     */
    private List<Style> createStyles(List<LegacyWMSStyle> legacyStyles) {
        List<Style> styles = new ArrayList<>();
        for (LegacyWMSStyle legacyStyle : legacyStyles) {
            Style style = new Style();
            style.setName(legacyStyle.getName());
            style.setTitle(legacyStyle.getTitle());
            style.setStyleAbstract(legacyStyle.getStyleAbstract());
            // Rasql fragment for the style to apply on a collection and result is a 2D image
            style.setRasqlQueryTransformer(legacyStyle.getRasqlQueryTransformer());

            styles.add(style);
        }
        return styles;
    }

    /**
     * Create a list of WMS EXGeographicBoundingBox from a legacy
     * EXGeographicBoundingBox for a layer.
     *
     * @param legacyEXBBox
     * @return
     */
    private EXGeographicBoundingBox createEXBBox(LegacyWMSEXGeographicBoundingBox legacyEXBBox) {
        EXGeographicBoundingBox exBBox = new EXGeographicBoundingBox();
        exBBox.setWestBoundLongitude(new BigDecimal(legacyEXBBox.getWestBoundLongitude()));
        exBBox.setEastBoundLongitude(new BigDecimal(legacyEXBBox.getEastBoundLongitude()));
        exBBox.setSouthBoundLatitude(new BigDecimal(legacyEXBBox.getSouthBoundLatitude()));
        exBBox.setNorthBoundLatitude(new BigDecimal(legacyEXBBox.getNorthBoundLatitude()));

        return exBBox;
    }

}
