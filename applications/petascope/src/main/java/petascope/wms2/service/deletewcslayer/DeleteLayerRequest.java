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

package petascope.wms2.service.deletewcslayer;

import org.jetbrains.annotations.NotNull;
import petascope.wms2.metadata.Layer;
import petascope.wms2.service.base.Request;

/**
 * Request class for DeleteLayer request type
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class DeleteLayerRequest extends Request {

    /**
     * Constructor for the class
     * @param request the base request
     * @param layer the extra layer
     */
    protected DeleteLayerRequest(@NotNull Request request, Layer layer) {
        super(request);
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }

    public static String getRequestParamValue() {
        return REQUEST_PARAM_VALUE;
    }

    public static String getLayerParamName() {
        return LAYER_PARAM_NAME;
    }

    private final Layer layer;
    public static final String LAYER_PARAM_NAME = "layer";
    private static final String REQUEST_PARAM_VALUE = "DeleteLayer";
}
