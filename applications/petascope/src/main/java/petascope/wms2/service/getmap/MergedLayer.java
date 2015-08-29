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

import petascope.ConfigManager;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.metadata.Coverage;
import petascope.wcps2.metadata.CoverageRegistry;
import petascope.wcps2.metadata.Interval;
import petascope.wcps2.util.CrsComputer;
import petascope.wms2.metadata.*;
import petascope.wms2.service.exception.error.WMSInvalidBbox;
import petascope.wms2.service.exception.error.WMSInvalidCrsUriException;
import petascope.wms2.service.exception.error.WMSInvalidDimensionValue;
import petascope.wms2.service.getmap.access.RasdamanSubset;

import java.util.ArrayList;
import java.util.List;

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
public class MergedLayer {

    /**
     * Constructor for the class
     *
     * @param boundingBox    the bounding box for this layer
     * @param mapBoundingBox the bounding box for the whole map
     * @param rasdamanLayers the rasdaman layers corresponding to the layers requested in the client's request
     * @param dimensions     the dimensions corresponding to the layers and the client's request
     * @param styles         the styles requested by the client
     * @param height         the height of the response
     * @param width          the width of the response
     * @param format         the format of the response
     */
    public MergedLayer(List<Layer> layers, BoundingBox boundingBox, BoundingBox mapBoundingBox, List<RasdamanLayer> rasdamanLayers,
                       List<Dimension> dimensions, List<Style> styles, int width, int height, GetMapFormat format) {
        this.layers = layers;
        this.boundingBox = boundingBox;
        this.rasdamanLayers = rasdamanLayers;
        this.mapBoundingBox = mapBoundingBox;
        this.dimensions = dimensions;
        this.styles = styles;
        this.height = height;
        this.width = width;
        this.format = format;
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
        rasdamanSubsets.addAll(boundingBoxToSubset());
        return rasdamanSubsets;
    }

    /**
     * Returns the rasdaman subset out of the given bounding box
     *
     * @return the rasdaman subset
     * @throws WMSInvalidBbox
     */
    private List<RasdamanSubset> boundingBoxToSubset() throws WMSInvalidBbox {
        try {
            List<RasdamanSubset> rasdamanSubsets = new ArrayList<RasdamanSubset>(2);
            DbMetadataSource dbMetadataSource = new DbMetadataSource(ConfigManager.METADATA_DRIVER,
                ConfigManager.METADATA_URL,
                ConfigManager.METADATA_USER,
                ConfigManager.METADATA_PASS, false);

            CoverageRegistry coverageRegistry = new CoverageRegistry(dbMetadataSource);
            Coverage coverage = coverageRegistry.lookupCoverage(layers.get(0).getName());
            CoverageMetadata coverageMetadata = coverage.getCoverageMetadata();
            String crs = CrsUtil.CrsUri.createCompound(coverageMetadata.getCrsUris());
            try {
                int index = 0;
                for (String axisLabel : getAxesLabels(coverageMetadata.getCrsUris())) {
                    DomainElement dom = coverageMetadata.getDomainByName(axisLabel);
                    if (dom.getType().equals(AxisTypes.X_AXIS) || dom.getType().equals(AxisTypes.Y_AXIS)) {
                        String min = String.valueOf(this.boundingBox.getMinx());
                        String max = String.valueOf(this.boundingBox.getMaxx());
                        if (index != 0) {
                            min = String.valueOf(this.boundingBox.getMiny());
                            max = String.valueOf(this.boundingBox.getMaxy());
                        }
                        CrsComputer crsComputer = new CrsComputer(dom.getLabel(), crs,
                            new Interval<String>(min, max), coverage, coverageRegistry);
                        Interval<Long> indices = crsComputer.getPixelIndices(true);
                        rasdamanSubsets.add(new RasdamanSubset(dom.getOrder(), indices.getLowerLimit(), indices.getUpperLimit()));
                        index += 1;
                    }
                }
            } catch (PetascopeException e) {
                throw new WMSInvalidCrsUriException(crs);
            } catch (SecoreException e) {
                throw new WMSInvalidCrsUriException(crs);
            }

            return rasdamanSubsets;
        } catch (Exception e) {
            throw new WMSInvalidBbox(boundingBox.toString());
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

    private final BoundingBox boundingBox;
    private final List<RasdamanLayer> rasdamanLayers;
    private final List<Dimension> dimensions;
    private final List<Style> styles;
    private final int height;
    private final int width;
    private final GetMapFormat format;
    private final BoundingBox mapBoundingBox;
    private final List<Layer> layers;
}
