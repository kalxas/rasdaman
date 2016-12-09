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

package petascope.wms2.service.deletestyle;

import org.jetbrains.annotations.NotNull;
import petascope.wms2.metadata.Style;
import petascope.wms2.service.base.Request;

/**
 * Request class for DeleteStyle request type
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class DeleteStyleRequest extends Request {

    /**
     * Constructor for the class
     * @param request the base request
     * @param style the style of input layer
     */
    protected DeleteStyleRequest(@NotNull Request request, Style style) {
        super(request);
        this.style = style;
    }
    
    public Style getStyle() {
        return style;
    }

    public static String getRequestParamValue() {
        return REQUEST_PARAM_VALUE;
    }

    public static String getLayerParamName() {
        return LAYER_PARAM_NAME;
    }
    
    public static String getStyleParamName() {
        return STYLE_PARAM_NAME;
    }

    private final Style style;
    public static final String LAYER_PARAM_NAME = "layer";
    public static final String STYLE_PARAM_NAME = "style";
    private static final String REQUEST_PARAM_VALUE = "DeleteStyle";
}
