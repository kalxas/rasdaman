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

import org.jetbrains.annotations.Nullable;
import petascope.wms2.metadata.*;
import petascope.wms2.service.base.Request;

import java.util.List;

/**
 * Helper class to construct the GetMap request. As this request has way too many parameters, we preferred a builder class
 * design pattern as opposed to a constructor to limit the amount of bugs that could arise from wrong ordering of the
 * parameters or default values
 */
public class GetMapRequestBuilder {
    /**
     * Sets the base request
     *
     * @param request the base request
     * @return the builder
     */
    public GetMapRequestBuilder setRequest(Request request) {
        this.request = request;
        return this;
    }

    /**
     * Sets the layers request
     *
     * @param layers the layers of the request
     * @return the builder
     */
    public GetMapRequestBuilder setLayers(List<Layer> layers) {
        this.layers = layers;
        return this;
    }

    /**
     * Sets the styles of  request
     *
     * @param styles the styles of the request
     * @return the builder
     */
    public GetMapRequestBuilder setStyles(List<Style> styles) {
        this.styles = styles;
        return this;
    }

    /**
     * Sets the crs of request
     *
     * @param crs the crs of the request
     * @return the builder
     */
    public GetMapRequestBuilder setCrs(Crs crs) {
        this.crs = crs;
        return this;
    }

    /**
     * Sets the bounding box request
     *
     * @param bbox the bounding box of the request
     * @return the builder
     */
    public GetMapRequestBuilder setBbox(BoundingBox bbox) {
        this.bbox = bbox;
        return this;
    }

    /**
     * Sets the format of the  request
     *
     * @param format the format of the response to this request
     * @return the builder
     */
    public GetMapRequestBuilder setFormat(GetMapFormat format) {
        this.format = format;
        return this;
    }

    /**
     * Sets the width request
     *
     * @param width the width of the request
     * @return the builder
     */
    public GetMapRequestBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * Sets the height of the request
     *
     * @param height the height of the request
     * @return the builder
     */
    public GetMapRequestBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * Sets the transparent flag of the request
     *
     * @param transparent the transparent flag of the request
     * @return the builder
     */
    public GetMapRequestBuilder setTransparent(boolean transparent) {
        this.transparent = transparent;
        return this;
    }

    /**
     * Sets the base request
     *
     * @param backgroundColor the background color of the request
     * @return the builder
     */
    public GetMapRequestBuilder setBackgroundColor(@Nullable String backgroundColor) {
        if (backgroundColor != null) {
            this.backgroundColor = backgroundColor;
        }
        return this;
    }

    /**
     * Sets the base request
     *
     * @param exceptionFormat the exceptionFormat of the request
     * @return the builder
     */
    public GetMapRequestBuilder setExceptionFormat(@Nullable String exceptionFormat) {
        this.exceptionFormat = exceptionFormat;
        return this;
    }

    /**
     * Sets the time of the request
     *
     * @param time the time of the request
     * @return the builder
     */
    public GetMapRequestBuilder setTime(@Nullable String time) {
        this.time = time;
        return this;
    }

    /**
     * Sets the elevation fo the request
     *
     * @param elevation the elevation of the request
     * @return the builder
     */
    public GetMapRequestBuilder setElevation(@Nullable String elevation) {
        this.elevation = elevation;
        return this;
    }

    /**
     * Sets the dimensions request
     *
     * @param dimensions the dimensions of the request
     * @return the builder
     */
    public GetMapRequestBuilder setDimensions(@Nullable List<Dimension> dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    /**
     * Creates the get map request based on the parameters you have provided
     *
     * @return the GetMapRequest
     */
    public GetMapRequest createGetMapRequest() {
        return new GetMapRequest(request, layers, styles, crs, bbox, format, width, height, transparent, backgroundColor, exceptionFormat, time, elevation, dimensions);
    }

    private Request request;
    private List<Layer> layers;
    private List<Style> styles;
    private Crs crs;
    private BoundingBox bbox;
    private GetMapFormat format;
    private int width;
    private int height;
    private boolean transparent = false;
    private String backgroundColor = BACKGROUND_COLOR_DEFAULT;
    private String exceptionFormat = EXCEPTION_FORMAT_DEFAULT;
    private String time = null;
    private String elevation = null;
    private List<Dimension> dimensions = null;
    private final static String BACKGROUND_COLOR_DEFAULT = "FFFFFF";
    private final static String EXCEPTION_FORMAT_DEFAULT = "XML";
}