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
import petascope.wms2.service.base.Request;

import java.util.Arrays;
import java.util.List;

/**
 * Representation of a get map request in WMS 1.3
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class GetMapRequest extends Request {

    /**
     * Constructor for the class. You can use the GetMapRequestBuilder to construct a GetMapRequest object easier
     *
     * @param request         the base request to the server
     * @param layers          the layers requested
     * @param styles          the styles requested
     * @param crs             the crs requested
     * @param bbox            the bbox requested
     * @param format          the format requested
     * @param width           the width requested
     * @param height          the height requested
     * @param transparent     flag to indicate if the background should be transparent
     * @param backgroundColor the background color
     * @param exceptionFormat the format of the exception
     * @param time            the time extent if there is one
     * @param elevation       the elevation extent if there is one
     * @param dimensions      the extent of any extra dimensions
     */
    public GetMapRequest(@NotNull Request request, @Nullable List<Layer> layers, @Nullable List<Style> styles, @Nullable Crs crs,
                         @Nullable BoundingBox bbox, @NotNull GetMapFormat format, int width, int height, boolean transparent,
                         @Nullable String backgroundColor, @Nullable String exceptionFormat, @Nullable String time,
                         @Nullable String elevation, @Nullable List<Dimension> dimensions) {
        super(request);
        this.layers = layers;
        this.styles = styles;
        this.crs = crs;
        this.bbox = bbox;
        this.originalBbox = new BoundingBox(bbox);
        this.format = format;
        this.width = width;
        this.height = height;
        this.transparent = transparent;
        this.backgroundColor = backgroundColor;
        this.exceptionFormat = exceptionFormat;
        this.time = time;
        this.elevation = elevation;
        this.dimensions = dimensions;
    }

    /**
     * Returns the expected value for the request parameter for this type of request
     *
     * @return the expected value for the request parameter
     */
    public static String getRequestType() {
        return REQUEST_TYPE;
    }

    /**
     * Returns the name for the WMS GetMap parameter dimension
     *
     * @return the name for the dimension parameter
     */
    public static String getDimensionParamName() {
        return DIMENSION_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter elevation
     *
     * @return the name for the elevation parameter
     */
    public static String getElevationParamName() {
        return ELEVATION_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter time
     *
     * @return the name for the time parameter
     */
    public static String getTimeParamName() {
        return TIME_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter exception format
     *
     * @return the name for the exception format parameter
     */
    public static String getExceptionParamName() {
        return EXCEPTION_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter background color
     *
     * @return the name for the background color parameter
     */
    public static String getBgcolorParamName() {
        return BGCOLOR_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter transparent
     *
     * @return the name for the transparent parameter
     */
    public static String getTransparentParamName() {
        return TRANSPARENT_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter height
     *
     * @return the name for the height parameter
     */
    public static String getHeightParamName() {
        return HEIGHT_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter width
     *
     * @return the name for the width parameter
     */
    public static String getWidthParamName() {
        return WIDTH_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter format
     *
     * @return the name for the format parameter
     */
    public static String getFormatParamName() {
        return FORMAT_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter bounding box
     *
     * @return the name for the bounding box parameter
     */
    public static String getBboxParamName() {
        return BBOX_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter crs
     *
     * @return the name for the crs parameter
     */
    public static String getCrsParamName() {
        return CRS_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter style
     *
     * @return the name for the style parameter
     */
    public static String getStyleParamName() {
        return STYLE_PARAM_NAME;
    }

    /**
     * Returns the name for the WMS GetMap parameter layer
     *
     * @return the name for the layer parameter
     */
    public static String getLayerParamName() {
        return LAYER_PARAM_NAME;
    }

    /**
     * Returns the layers requested
     *
     * @return the layers requested
     */
    @Nullable
    public List<Layer> getLayers() {
        return layers;
    }

    /**
     * Returns the styles requested
     *
     * @return the styles requested
     */
    @Nullable
    public List<Style> getStyles() {
        return styles;
    }

    /**
     * Returns the crs requested
     *
     * @return the crs requested
     */
    @Nullable
    public Crs getCrs() {
        return crs;
    }

    /**
     * Returns the bbox requested by the user. This bbox can be modified by validators or handlers to make it conform
     * to certain specifications. See {@link GetMapRequest#getOriginalBbox()} for the original bbox
     *
     * @return the bbox requested
     */
    @Nullable
    public BoundingBox getBbox() {
        return bbox;
    }

    /**
     * Returns the format requested
     *
     * @return the format requested
     */
    @NotNull
    public GetMapFormat getFormat() {
        return format;
    }

    /**
     * Returns the width requested
     *
     * @return the width requested
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height requested
     *
     * @return the height requested
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the transparency requested
     *
     * @return the transparency requested
     */
    public boolean isTransparent() {
        return transparent;
    }

    /**
     * Returns the background color requested
     *
     * @return the background color requested
     */
    @Nullable
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns the exception format requested
     *
     * @return the exception format requested
     */
    @Nullable
    public String getExceptionFormat() {
        return exceptionFormat;
    }

    /**
     * Returns the time requested
     *
     * @return the time requested
     */
    @Nullable
    public String getTime() {
        return time;
    }

    /**
     * Returns the elevation requested
     *
     * @return the elevation requested
     */
    @Nullable
    public String getElevation() {
        return elevation;
    }

    /**
     * Returns the dimensions requested
     *
     * @return the dimensions requested
     */
    @Nullable
    public List<Dimension> getDimensions() {
        return dimensions;
    }

    /**
     * Returns the original bounding box as supplied by the user. See {@link GetMapRequest#getBbox()} to understand
     * the difference between the two
     *
     * @return the original bounding box
     */
    public BoundingBox getOriginalBbox(){
        return originalBbox;
    }


    @Nullable
    private final List<Layer> layers;
    @Nullable
    private final List<Style> styles;
    @Nullable
    private final Crs crs;
    @Nullable
    private final BoundingBox bbox;
    @Nullable
    private final BoundingBox originalBbox;
    @NotNull
    private final GetMapFormat format;
    private final int width;
    private final int height;
    private final boolean transparent;
    @Nullable
    private final String backgroundColor;
    @Nullable
    private final String exceptionFormat;
    @Nullable
    private final String time;
    @Nullable
    private final String elevation;
    @Nullable
    private final List<Dimension> dimensions;

    private final static String REQUEST_TYPE = "GetMap";
    private final static String LAYER_PARAM_NAME = "layers";
    private final static String STYLE_PARAM_NAME = "styles";
    private final static String CRS_PARAM_NAME = "crs";
    private final static String BBOX_PARAM_NAME = "bbox";
    private final static String FORMAT_PARAM_NAME = "format";
    private final static String WIDTH_PARAM_NAME = "width";
    private final static String HEIGHT_PARAM_NAME = "height";
    private final static String TRANSPARENT_PARAM_NAME = "transparent";
    private final static String BGCOLOR_PARAM_NAME = "bgcolor";
    private final static String EXCEPTION_PARAM_NAME = "exception";
    private final static String TIME_PARAM_NAME = "time";
    private final static String ELEVATION_PARAM_NAME = "elevation";
    private final static String DIMENSION_PARAM_NAME = "dim_";
}
