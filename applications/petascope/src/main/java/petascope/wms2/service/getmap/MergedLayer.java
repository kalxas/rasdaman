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

package petascope.wms2.service.getmap;

import java.math.BigDecimal;
import petascope.ConfigManager;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.metadata.legacy.Coverage;
import petascope.wcps2.metadata.legacy.CoverageRegistry;
import petascope.wcps2.util.CrsComputer;
import petascope.wms2.metadata.*;
import petascope.wms2.service.exception.error.WMSInvalidBbox;
import petascope.wms2.service.exception.error.WMSInvalidDimensionValue;
import petascope.wms2.service.getmap.access.RasdamanSubset;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import petascope.exceptions.WCSException;
import petascope.util.CrsProjectionUtil;

import petascope.wcps2.metadata.model.ParsedSubset;
import static petascope.util.CrsUtil.getAxesLabels;

/**
 * A merged layer is a layer composed of other layers that were requested in the get map request. What we do is
 * create a list of rasdaman layers in the order imposed by the standard, an intersection of the bounding boxes and dimensions,
 * a merging of styles and adding in the end the other necessary parameters to have all the information needed to
 * build a rasql query that will give us the wanted result.
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public final class MergedLayer {

    /**
     * Constructor for the class
     *
     * @param crs            the requested crs
     * @param layers         list of layers
     * @param boundingBox    the bounding box for this layer
     * @param originalBoundingBox the original bounding box for this rasdaman layer as requested by the user
     * @param rasdamanLayers the rasdaman layers corresponding to the layers requested in the client's request
     * @param dimensions     the dimensions corresponding to the layers and the client's request
     * @param styles         the styles requested by the client
     * @param height         the height of the response
     * @param width          the width of the response
     * @param format         the format of the response
     * @param transparent    boolean value indicating if the layer should have the nodata values transparent
     */
    public MergedLayer(Crs crs, List<Layer> layers, BoundingBox boundingBox, BoundingBox originalBoundingBox,
                       List<RasdamanLayer> rasdamanLayers,  List<Dimension> dimensions, List<Style> styles,
                       int width, int height, GetMapFormat format, boolean transparent) {
        this.crs = crs;
        this.layers = layers;
        this.boundingBox = boundingBox;
        this.originalBoundingBox = originalBoundingBox;
        this.rasdamanLayers = rasdamanLayers;
        this.dimensions = dimensions;
        this.styles = styles;
        this.height = height;
        this.width = width;
        this.format = format;
        this.transparent = transparent;
    }

    /**
     * Returns a list of rasdaman subsets out of the dimensions and the given bouding box
     *
     * @return the rasdaman subsets
     * @throws WMSInvalidDimensionValue
     * @throws WMSInvalidBbox
     */
    public List<RasdamanSubset> getRasdamanSubsets() throws WMSInvalidDimensionValue, WMSInvalidBbox {
        List<RasdamanSubset> rasdamanSubsets = new ArrayList<RasdamanSubset>(dimensions.size() + 2); //extra dimensions + axis x + axis y
        rasdamanSubsets.addAll(dimensionsToSubset());
        rasdamanSubsets.addAll(boundingBoxToSubset(boundingBox));
        return rasdamanSubsets;
    }

    public List<RasdamanSubset> getRasdamanExtendSubsets() throws WMSInvalidDimensionValue, WMSInvalidBbox {
        List<RasdamanSubset> rasdamanSubsets = new ArrayList<RasdamanSubset>(dimensions.size() + 2); //extra dimensions + axis x + axis y
        rasdamanSubsets.addAll(dimensionsToSubset());
        rasdamanSubsets.addAll(boundingBoxToSubset(originalBoundingBox));
        return rasdamanSubsets;
    }

    /**
     * Returns the rasdaman subsets out of the given bounding box
     * e.g: bbox=-80,-20,-40,50 -> [0:200,25:40]
     * @return the rasdaman subsets
     * @throws WMSInvalidBbox
     */
    private List<RasdamanSubset> boundingBoxToSubset(BoundingBox boundingBox) throws WMSInvalidBbox {
        try {
            List<RasdamanSubset> rasdamanSubsets = new ArrayList<RasdamanSubset>(2);
            DbMetadataSource dbMetadataSource = new DbMetadataSource(ConfigManager.METADATA_DRIVER,
                    ConfigManager.METADATA_URL,
                    ConfigManager.METADATA_USER,
                    ConfigManager.METADATA_PASS, false);

            CoverageRegistry coverageRegistry = new CoverageRegistry(dbMetadataSource);
            Coverage coverage = coverageRegistry.lookupCoverage(layers.get(0).getName());
            CoverageMetadata coverageMetadata = coverage.getCoverageMetadata();

            // e.g: http://opengis.net/def/crs/EPSG/0/4326
            String nativeCrs = CrsUtil.CrsUri.createCompound(coverageMetadata.getCrsUris());
            // e.g: EPSG:4326
            String nativeCrsEpsgCode = CrsUtil.getEPSGCode(nativeCrs);
            // e.g: EPSG:3857
            String requestedCrsEpsgCode = crs.getCrsName();

            // NOTE: if outputCr is different with nativeCrs then it will need to transform the bounding box
            // from requestedCrs to nativeCrs (e.g: EPSG:3857 - > EPSG:4326)
            BoundingBox bbox = new BoundingBox(boundingBox);

            // This is used when requestedCrs (e.g: 3857 - XY Order) is different with nativeCrs (e.g: 4326 - YX Order)
            // Then after transform the Bbox from 3857 to 4326, it will need to set correct transformed subset by axis order.
            // e.g: (-120:90) to Long axis instead of Lat axis, otherwise it will translate wrong bounding box in grid coordinates
            // and can stop the server due to large request.
            List<String> axisLabels = getAxesLabels(coverageMetadata.getCrsUris());
            boolean isNativeXYOrder = CrsUtil.isXYCoverage(axisLabels, coverageMetadata);
            // Set the output bounding box in Rasql
            setOutputBBox(isNativeXYOrder);
            if (!nativeCrsEpsgCode.equals(requestedCrsEpsgCode)) {
                isProjection = true;
                bbox = this.transformBbox(isNativeXYOrder, requestedCrsEpsgCode, nativeCrsEpsgCode);
            }

            int index = 0;
            for (String axisLabel : axisLabels) {
                DomainElement dom = coverageMetadata.getDomainByName(axisLabel);
                if (dom.getType().equals(AxisTypes.X_AXIS) || dom.getType().equals(AxisTypes.Y_AXIS)) {
                    String min = String.valueOf(bbox.getMinx());
                    String max = String.valueOf(bbox.getMaxx());
                    if (index != 0) {
                        min = String.valueOf(bbox.getMiny());
                        max = String.valueOf(bbox.getMaxy());
                    }
                    CrsComputer crsComputer = new CrsComputer(dom.getLabel(), nativeCrs,
                            new ParsedSubset<String>(min, max), coverage, coverageRegistry);
                    ParsedSubset<Long> indices = crsComputer.getPixelIndices(true);
                    rasdamanSubsets.add(new RasdamanSubset(dom.getOrder(), indices.getLowerLimit(), indices.getUpperLimit()));
                    index += 1;
                }
            }

            // close the connection after using
            dbMetadataSource.closeConnection();
            return rasdamanSubsets;
        } catch (Exception e) {
            throw new WMSInvalidBbox(boundingBox.toString(), e.getMessage());
        }
    }

    /**
     * Returns the dimensions of the requested layers as rasdaman subsets
     *
     * @return the rasdaman subsets
     * @throws WMSInvalidDimensionValue
     */
    private List<RasdamanSubset> dimensionsToSubset() throws WMSInvalidDimensionValue {
        List<RasdamanSubset> rasdamanSubsets = new ArrayList<RasdamanSubset>(dimensions.size());
        for (Dimension dimension : dimensions) {
            rasdamanSubsets.add(dimension.getExtentAsSubset());
        }
        return rasdamanSubsets;
    }

    /**
     * Returns all the rasdaman layers in this request
     *
     * @return the rasdaman layers in the request
     */
    public List<RasdamanLayer> getRasdamanLayers() {
        return rasdamanLayers;
    }

    /**
     * Returns the height requested by the client
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the width requested by the client
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the styles
     *
     * @return a list of styles requested by the client
     */
    public List<Style> getStyles() {
        return styles;
    }

    /**
     * The format requested by the client
     *
     * @return the format requested by the client
     */
    public GetMapFormat getFormat() {
        return format;
    }

    /**
     * Boolean value indicating if nodata pixels should be rendered transparent
     *
     * @return transparency of the merged layer
     */
    public boolean isTransparent() {
        return transparent;
    }

    /**
     * Set the bounding box in the requested CRS
     */
    public void setOutputBBox(boolean isXYOrder) {
        // it will need to use projection() in Rasql later with the original bounding box
        // The requested bounding box can be in XY or YX order
        this.originalBoundingBoxStr = originalBoundingBoxStrTemplate.replace("$xmin", String.valueOf(originalBoundingBox.getMinx()))
                                      .replace("$ymin", String.valueOf(originalBoundingBox.getMiny()))
                                      .replace("$xmax", String.valueOf(originalBoundingBox.getMaxx()))
                                      .replace("$ymax", String.valueOf(originalBoundingBox.getMaxy()))
                                      .replace("$crs", crs.getCrsName());
        if (isXYOrder) {
            this.originalBoundingBoxStr = originalBoundingBoxStrTemplate.replace("$xmin", String.valueOf(originalBoundingBox.getMiny()))
                                          .replace("$ymin", String.valueOf(originalBoundingBox.getMinx()))
                                          .replace("$xmax", String.valueOf(originalBoundingBox.getMaxy()))
                                          .replace("$ymax", String.valueOf(originalBoundingBox.getMaxx()))
                                          .replace("$crs", crs.getCrsName());
        }
    }

    /**
     * Return the bounding box in the requested CRS
     * @return
     */
    public String getOutputBBoxStr() {
        return this.originalBoundingBoxStr;
    }

    /**
     * Return the projection for the transformation from nativeCrs to requestedCrs
     * @return
     */
    public String getProjectionStr() {
        return this.projectionStr;
    }

    /**
     * When requestedCs is different with nativeCrs then need to use projection in Rasql
     * @return
     */
    public boolean projection() {
        return this.isProjection;
    }

    /**
     * Transform a requested bounding box from requestedCrs (e.g: 3857) in nativeCrs (e.g: 4326)
     * @param isNativeXYOrder check if coverage is XY axis order in native CRS
     * @param nativeCrsEpsgCode
     * @param requestedCrsEpsgCode
     * @return
     * @throws WMSInvalidBbox
     */
    private BoundingBox transformBbox(boolean isNativeXYOrder, String requestedCrsEpsgCode, String nativeCrsEpsgCode) throws WMSInvalidBbox {
        double xmin, ymin, xmax, ymax;
        List<BigDecimal> transformedCoords = new LinkedList<BigDecimal>();

        try {
            // NOTE: request bounding box can be in YX (EPSG:4326) or XY (EPSG:3857) order
            // We can have 2 cases:
            // + Native order is YX and request with in XY (e.g: bbox=12464999.982,-5548370.985,17393850.457,-1003203.187&crs=EPSG:3857)
            // + Native order is XY and request with in YX (e.g: bbox=-44.525,111.975,-8.978,156.279&crs=EPSG:4326)

            double[] srcCoords = {
                originalBoundingBox.getMinx(),
                originalBoundingBox.getMiny(),
                originalBoundingBox.getMaxx(),
                originalBoundingBox.getMaxy()
            };

            // If request bounding box is YX then Geotools will convert error (e.g: Lat=111.975 is invalid), need to swap
            if (isNativeXYOrder) {
                srcCoords[0] = originalBoundingBox.getMiny();
                srcCoords[1] = originalBoundingBox.getMinx();
                srcCoords[2] = originalBoundingBox.getMaxy();
                srcCoords[3] = originalBoundingBox.getMaxx();
            }

            transformedCoords = CrsProjectionUtil.transformBoundingBox(requestedCrsEpsgCode, nativeCrsEpsgCode,
                                srcCoords);

            // if native is also XY order
            xmin = transformedCoords.get(0).doubleValue();
            ymin = transformedCoords.get(1).doubleValue();
            xmax = transformedCoords.get(2).doubleValue();
            ymax = transformedCoords.get(3).doubleValue();

            if (!isNativeXYOrder) {
                // native is YX order
                xmin = transformedCoords.get(1).doubleValue();
                ymin = transformedCoords.get(0).doubleValue();
                xmax = transformedCoords.get(3).doubleValue();
                ymax = transformedCoords.get(2).doubleValue();
            }

        } catch (WCSException e) {
            // This bounding box cannot be transformed
            throw new WMSInvalidBbox(boundingBox.toString(), e.getMessage());
        }

        // transformed subsets from nativeCrs to outputCrs
        BoundingBox bbox = new BoundingBox(null, xmin, ymin, xmax, ymax, null);
        projectionStr =  projectionStrTemplate.replace("$xmin", String.valueOf(xmin))
                         .replace("$ymin", String.valueOf(ymin))
                         .replace("$xmax", String.valueOf(xmax))
                         .replace("$ymax", String.valueOf(ymax))
                         .replace("$nativeCrs", nativeCrsEpsgCode)
                         .replace("$outputCrs", requestedCrsEpsgCode);
        if (!isNativeXYOrder) {
            // Lat, Long which is not the order of domains in Rasql
            projectionStr = projectionStrTemplate.replace("$xmin", String.valueOf(ymin))
                            .replace("$ymin", String.valueOf(xmin))
                            .replace("$xmax", String.valueOf(ymax))
                            .replace("$ymax", String.valueOf(xmax))
                            .replace("$nativeCrs", nativeCrsEpsgCode)
                            .replace("$outputCrs", requestedCrsEpsgCode);
        }

        return bbox;
    }

    private final BoundingBox boundingBox;
    private final BoundingBox originalBoundingBox;
    private String projectionStr;
    private final String projectionStrTemplate = "\"$xmin,$ymin,$xmax,$ymax\", \"$nativeCrs\", \"$outputCrs\"";
    private String originalBoundingBoxStr;
    private final String originalBoundingBoxStrTemplate = "xmin=$xmin;ymin=$ymin;xmax=$xmax;ymax=$ymax;crs=$crs";
    private boolean isProjection = false;
    private final List<RasdamanLayer> rasdamanLayers;
    private final List<Dimension> dimensions;
    private final List<Style> styles;
    private final int height;
    private final int width;
    private final GetMapFormat format;
    private final List<Layer> layers;
    private final boolean transparent;
    private final Crs crs;
}
