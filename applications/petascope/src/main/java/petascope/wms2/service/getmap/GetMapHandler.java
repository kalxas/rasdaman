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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.metadata.*;
import petascope.wms2.rasdaman.RasdamanService;
import petascope.wms2.service.base.Handler;
import petascope.wms2.service.base.RequestCacheEngine;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInternalException;
import petascope.wms2.service.getmap.access.RasqlQueryGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for the GetMap operation
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class GetMapHandler implements Handler<GetMapRequest, GetMapResponse> {
    /**
     * Constructor for the class
     *
     * @param rasdamanService the rasdaman service
     * @param cacheEngine     the cache engine
     */
    public GetMapHandler(RasdamanService rasdamanService, RequestCacheEngine cacheEngine) {
        this.rasdamanService = rasdamanService;
        this.cacheEngine = cacheEngine;
    }

    @NotNull
    @Override
    public GetMapResponse handle(@NotNull GetMapRequest request) throws WMSException {
        MergedLayer mergedLayer = getMergedLayer(request);
        RasqlQueryGenerator queryGenerator = new RasqlQueryGenerator(mergedLayer);
        String query = queryGenerator.generateQuery();
        GetMapResponse response = new GetMapResponse(request.getFormat().getFormat(), rasdamanService.executeQuery(query));
        cacheEngine.addCachedResponse(request.getRawRequest(), response);
        return response;
    }

    /**
     * Returns a merged layer from the request. Given a GetMap request this will produce all the information
     * necessary to create the final rasql query that will get us the result.
     *
     * @param request the get map request
     * @return the merged layer
     * @throws WMSInternalException
     */
    private MergedLayer getMergedLayer(GetMapRequest request) throws WMSInternalException {
        BoundingBox bbox = getOrThrow(request.getBbox(), "bounding box");
        List<RasdamanLayer> rasdamanLayers = getRasdamanLayers(request.getLayers());
        List<Dimension> dimensions = getOrThrow(request.getDimensions(), "dimensions");
        List<Style> styles = getOrThrow(request.getStyles(), "style");
        List<Layer> layers = getOrThrow(request.getLayers(), "layers");
        boolean transparent = request.isTransparent();
        int width = request.getWidth();
        int height = request.getHeight();
        GetMapFormat format = getOrThrow(request.getFormat(), "format");
        Crs crs = getOrThrow(request.getCrs(), "crs");
        return new MergedLayer(crs, layers, bbox, request.getOriginalBbox(), rasdamanLayers, dimensions, styles, width, height, format, transparent);
    }

    /**
     * Given a list of layers, it returns the rasdaman layers corresponding to it in the same order
     *
     * @param layers the requested layers
     * @return the rasdaman layers
     * @throws WMSInternalException
     */
    private List<RasdamanLayer> getRasdamanLayers(@Nullable List<Layer> layers) throws WMSInternalException {
        if (layers == null) {
            throw new WMSInternalException(new Exception("The requested layers were not translated correctly."));
        }
        List<RasdamanLayer> rasdamanLayers = new ArrayList<RasdamanLayer>();
        for (Layer layer : layers) {
            rasdamanLayers.add(layer.getRasdamanLayers().iterator().next());
        }
        return rasdamanLayers;
    }

    /**
     * Returns the value if not null, or throws a wms internal exception if the object is null
     * We use this as an assertion method, in theory the variables passed here should not be null, but if
     * there was some programming error upstream, better catch them here than let them slip into the handler.
     *
     * @param valueOrNull the value to be checked
     * @param type        the type to be described in the exception
     * @param <T>         parameter for the value type
     * @return the value that was checked
     * @throws WMSInternalException
     */
    private static <T> T getOrThrow(@Nullable T valueOrNull, String type) throws WMSInternalException {
        if (valueOrNull == null) {
            throw new WMSInternalException(new Exception("The requested" + type + " was not translated correctly"));
        }
        return valueOrNull;
    }

    private final RasdamanService rasdamanService;
    private final RequestCacheEngine cacheEngine;
}
