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

import com.sun.istack.Nullable;
import org.jetbrains.annotations.NotNull;
import petascope.wms2.metadata.*;
import petascope.wms2.metadata.dao.PersistentMetadataObjectProvider;
import petascope.wms2.service.base.Parser;
import petascope.wms2.service.exception.error.*;
import petascope.wms2.servlet.WMSGetRequest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser for a get map request that gets a raw wms request and returns a typed GetMap request
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class GetMapParser extends Parser<GetMapRequest> {

    /**
     * Constructor for the class
     *
     * @param persistentMetadataObjectProvider the provider of persistence metadata classes
     */
    public GetMapParser(PersistentMetadataObjectProvider persistentMetadataObjectProvider) {
        this.persistentMetadataObjectProvider = persistentMetadataObjectProvider;
    }

    /**
     * Returns true if the parser can parse this request
     *
     * @param rawRequest the raw wms http request
     * @return true if it can be parsed, false otherwise
     */
    @Override
    public boolean canParse(WMSGetRequest rawRequest) {
        String requestValue = rawRequest.getGetValueByKey(GetMapRequest.getRequestParameterRequest());
        return requestValue != null && requestValue.equalsIgnoreCase(GetMapRequest.getRequestType());
    }

    /**
     * Parses the request into typed GetMap request
     *
     * @param rawRequest the raw wms http request
     * @return the typed request
     * @throws petascope.wms2.service.exception.error.WMSInternalException
     * @throws petascope.wms2.service.exception.error.WMSInvalidLayerException
     * @throws petascope.wms2.service.exception.error.WMSInvalidStyleException
     * @throws petascope.wms2.service.exception.error.WMSInvalidBbox
     * @throws petascope.wms2.service.exception.error.WMSInvalidWidth
     * @throws petascope.wms2.service.exception.error.WMSInvalidHeight
     * @throws petascope.wms2.service.exception.error.WMSInvalidCrsException
     * @throws petascope.wms2.service.exception.error.WMSInvalidDimensionValue
     * @throws petascope.wms2.service.exception.error.WMSInvalidFormatException
     */
    @Override
    public GetMapRequest parse(WMSGetRequest rawRequest) throws WMSInternalException, WMSInvalidLayerException,
                                                                WMSInvalidStyleException, WMSInvalidBbox,
                                                                WMSInvalidWidth, WMSInvalidHeight,
                                                                WMSInvalidCrsException, WMSInvalidDimensionValue,
                                                                WMSInvalidFormatException {
        try {
            GetMapRequestBuilder builder = new GetMapRequestBuilder();
            Crs crs = new Crs(rawRequest.getGetValueByKey(GetMapRequest.getCrsParamName()));
            GetMapRequest request = builder
                .setLayers(getLayers(rawRequest.getGetValueByKey(GetMapRequest.getLayerParamName())))
                .setStyles(getStyles(rawRequest.getGetValueByKey(GetMapRequest.getStyleParamName())))
                .setCrs(crs)
                .setBbox(getBoundingBox(rawRequest.getGetValueByKey(GetMapRequest.getBboxParamName()), crs))
                .setWidth(getWidth(rawRequest.getGetValueByKey(GetMapRequest.getWidthParamName())))
                .setHeight(getHeight(rawRequest.getGetValueByKey(GetMapRequest.getHeightParamName())))
                .setFormat(getFormat(rawRequest.getGetValueByKey(GetMapRequest.getFormatParamName())))
                .setTransparent(getTransparent(rawRequest.getGetValueByKey(GetMapRequest.getTransparentParamName())))
                .setBackgroundColor(rawRequest.getGetValueByKey(GetMapRequest.getTransparentParamName()))
                .setExceptionFormat(rawRequest.getGetValueByKey(GetMapRequest.getExceptionParamName()))
                .setTime(rawRequest.getGetValueByKey(GetMapRequest.getTimeParamName()))
                .setElevation(rawRequest.getGetValueByKey(GetMapRequest.getElevationParamName()))
                .setDimensions(getDimensions(rawRequest))
                .setRequest(parseBaseRequest(rawRequest))
                .createGetMapRequest();
            return request;
        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
    }

    /**
     * Returns the get map format requested by the client
     *
     * @param format the format requested by the client
     * @return the get map format
     * @throws WMSInvalidFormatException
     * @throws SQLException
     */
    private GetMapFormat getFormat(@Nullable String format) throws WMSInvalidFormatException, SQLException {
        if (format == null) {
            throw new WMSInvalidFormatException("");
        }
        GetMapFormat mapFormat = persistentMetadataObjectProvider.getGetMapFormat().queryForId(format);
        if (mapFormat == null) {
            throw new WMSInvalidFormatException(format);
        }
        return mapFormat;
    }

    /**
     * Returns any other dimensions different from elevation or time. A dimension parameter is defined as anything
     * that starts with DIM_
     *
     * @param rawRequest the raw request
     * @return a list of dimensions
     */
    private List<Dimension> getDimensions(WMSGetRequest rawRequest) throws WMSInvalidDimensionValue, WMSInternalException {
        try {
            Map<String, String> allParams = rawRequest.getAllGetValues();
            List<Dimension> retDimensions = new ArrayList<Dimension>();
            for (String key : allParams.keySet()) {
                if (key.startsWith(GetMapRequest.getDimensionParamName())) {
                    String dimensionName = key.replaceFirst(GetMapRequest.getDimensionParamName(), "");
                    List<Dimension> dims = persistentMetadataObjectProvider.getDimension()
                                                                           .queryForEq(Dimension.NAME_COLUMN_NAME, dimensionName);
                    if (dims.isEmpty()) {
                        throw new WMSInvalidDimensionValue(dimensionName);
                    }
                    String dimVal = allParams.get(key);
                    dims.get(0).setExtent(dimVal);
                    retDimensions.add(dims.get(0));
                }
            }
            return retDimensions;
        } catch (SQLException e) {
            throw new WMSInternalException(e);
        }
    }

    /**
     * Gets the transparency parameter
     *
     * @param transparentString the transparency parameter
     * @return true if transparent parameter is "T(t)R(r)U(u)E(e)", false otherwise
     */
    private boolean getTransparent(String transparentString) {
        if (transparentString == null) {
            return false;
        }
        return Boolean.parseBoolean(transparentString);
    }

    /**
     * Parses the width of the request
     *
     * @param width the width of the request in string format
     * @return the width in integer format
     * @throws WMSInvalidWidth
     */
    private int getWidth(String width) throws WMSInvalidWidth {
        try {
            return Integer.parseInt(width);
        } catch (NumberFormatException e) {
            if (width == null) {
                width = "";
            }
            throw new WMSInvalidWidth(width);
        }
    }


    /**
     * Parses the height of the request
     *
     * @param height the height of the request in string format
     * @return the height in integer format
     * @throws WMSInvalidHeight
     */
    private int getHeight(String height) throws WMSInvalidHeight {
        try {
            return Integer.parseInt(height);
        } catch (NumberFormatException e) {
            if (height == null) {
                height = "";
            }
            throw new WMSInvalidHeight(height);
        }
    }

    /**
     * Returns a crs metadata object based on the user crs
     *
     * @param crs the crs metadata object
     * @return the crs
     * @throws SQLException
     * @throws WMSInvalidCrsException
     */
    private Crs getCrs(String crs) throws SQLException, WMSInvalidCrsException {
        if (crs == null) {
            throw new WMSInvalidCrsException("");
        }
        List<Crs> metaCrses = persistentMetadataObjectProvider.getCrs().queryForEq(Crs.CRS_COLUMN_NAME, crs);
        if (metaCrses.isEmpty()) {
            throw new WMSInvalidCrsException(crs);
        }
        return metaCrses.get(0);

    }

    /**
     * Returns the a list of metadata layers based on what the user requested
     *
     * @param layersString the layer string the user requested
     * @return a list of metadata layers
     * @throws SQLException
     * @throws WMSInvalidLayerException
     */
    @NotNull
    private List<Layer> getLayers(@Nullable String layersString) throws SQLException, WMSInvalidLayerException {
        if (layersString == null) {
            return new ArrayList<Layer>();
        }
        String[] layerStrings = (layersString.split(","));
        List<Layer> layers = new ArrayList<Layer>(layerStrings.length);
        for (String layerName : layerStrings) {
            List<Layer> currentLayer = persistentMetadataObjectProvider.getLayer().queryForEq(Layer.NAME_COLUMN_NAME, layerName);
            if (currentLayer.isEmpty()) {
                throw new WMSInvalidLayerException(layerName);
            }
            layers.add(currentLayer.get(0));
        }
        return layers;
    }

    /**
     * Returns the bounding box of the request
     *
     * @param bboxString the bbox string of the request in form minx,miny,maxx,maxy
     * @param crs        the crs of the bbox
     * @return the metadata bbox
     * @throws WMSInvalidBbox
     */
    private BoundingBox getBoundingBox(@Nullable String bboxString, Crs crs) throws WMSInvalidBbox {
        if (bboxString == null) {
            throw new WMSInvalidBbox("");
        }
        final String[] bboxComponents = bboxString.split(",");
        if (bboxComponents.length != 4) {
            throw new WMSInvalidBbox(bboxString);
        }
        try {
            return new BoundingBox(crs,
                Double.parseDouble(bboxComponents[0].trim()),
                Double.parseDouble(bboxComponents[1].trim()),
                Double.parseDouble(bboxComponents[2].trim()),
                Double.parseDouble(bboxComponents[3].trim()),
                null);
        } catch (NumberFormatException e) {
            throw new WMSInvalidBbox(bboxString);
        }
    }

    /**
     * Returns the a list of metadata styles based on what the user requested
     *
     * @param stylesString the style string the user requested
     * @return a list of metadata styles
     * @throws SQLException
     * @throws WMSInvalidStyleException
     */
    @NotNull
    private List<Style> getStyles(@Nullable String stylesString) throws SQLException, WMSInvalidStyleException {
        if (stylesString == null || stylesString.trim().equals("")) {
            return new ArrayList<Style>();
        }
        String[] styleStrings = (stylesString.split(","));
        List<Style> styles = new ArrayList<Style>(styleStrings.length);
        for (String styleName : styleStrings) {
            Style currentStyle = persistentMetadataObjectProvider.getStyle().queryForId(styleName.trim());
            if (currentStyle == null) {
                throw new WMSInvalidStyleException(styleName.trim());
            }
            styles.add(currentStyle);
        }
        return styles;
    }

    private final PersistentMetadataObjectProvider persistentMetadataObjectProvider;
}
