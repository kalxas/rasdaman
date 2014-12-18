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

package petascope.wms2.service.insertstyle;

import org.jetbrains.annotations.NotNull;
import petascope.wms2.service.base.Request;

/**
 * Inserts a style into the database based on a rasql query
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class InsertStyleRequest extends Request {

    /**
     * Constructor for the class
     *
     * @param request                the base request to the service
     * @param rasqlTransformFragment the rasql query fragment to be used on each layer
     * @param styleName
     */
    public InsertStyleRequest(@NotNull Request request, String rasqlTransformFragment, String styleName, String styleAbstract, String layerName) {
        super(request);
        this.rasqlTransformFragment = rasqlTransformFragment;
        this.styleName = styleName;
        this.layerName = layerName;
        this.styleAbstract = styleAbstract;

    }

    /**
     * Returns the rasql query fragment
     *
     * @return the rasql query fragment
     */
    public String getRasqlTransformFragment() {
        return rasqlTransformFragment;
    }

    /**
     * Returns the style name
     *
     * @return the style name
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * Returns the style abstract
     *
     * @return the style abstract
     */
    public String getStyleAbstract() {
        return styleAbstract;
    }

    /**
     * Returns the layer id
     *
     * @return the id of the layer
     */
    public String getLayerName() {
        return layerName;
    }

    /**
     * Gets the request parameter name for the rasql query
     *
     * @return the param name
     */
    public static String getRequestParameterRasqlQuery() {
        return REQUEST_PARAMETER_RASQL_QUERY;
    }

    /**
     * Gets the request parameter name for the style name
     *
     * @return the param name
     */
    public static String getRequestParameterStyleName() {
        return REQUEST_PARAMETER_STYLE_NAME;
    }

    /**
     * Returns the request parameter value
     *
     * @return the request parameter value for this type of request
     */
    public static String getRequestParameterValue() {
        return REQUEST_PARAMETER_VALUE;
    }

    /**
     * Returns the name of the parameter to be used for the style abstract
     *
     * @return the style abstract parameter name
     */
    public static String getRequestParameterStyleAbstract() {
        return REQUEST_PARAMETER_STYLE_ABSTRACT;
    }

    /**
     * Returns the name of the parameter to be used for the style layer id
     *
     * @return the style layer id parameter name
     */
    public static String getRequestParameterStyleLayerId() {
        return REQUEST_PARAMETER_STYLE_LAYER_ID;
    }

    private final String rasqlTransformFragment;
    private final String styleName;
    private final String styleAbstract;
    private final String layerName;
    private static final String REQUEST_PARAMETER_VALUE = "InsertStyle";
    private static final String REQUEST_PARAMETER_RASQL_QUERY = "rasqlTransformFragment";
    private static final String REQUEST_PARAMETER_STYLE_NAME = "name";
    private static final String REQUEST_PARAMETER_STYLE_ABSTRACT = "abstract";
    private static final String REQUEST_PARAMETER_STYLE_LAYER_ID = "layer";
}