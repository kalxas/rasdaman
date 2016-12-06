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

package petascope.wms2.metadata;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.service.exception.error.WMSInvalidDimensionValue;
import petascope.wms2.service.getmap.access.RasdamanSubset;
import petascope.wms2.util.ConfigManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent a WMS dimension element. According to the standard the definition is:
 * The optional <Dimension> element is used in service metadata to declare that one or more dimensional
 * parameters are relevant to a layer or group of layers. Table C.1 shows the fields in a Dimension element: a
 * mandatory name, a mandatory measurement units specifier with an optional unitSymbol, a mandatory string
 * indicating available value(s) of the corresponding dimension, an optional default value used when the client does
 * not specify one, and several optional Boolean attributes indicating whether multiple values of the dimension may
 * be requested at the same time, whether the nearest available value of a dimension will be returned in response to
 * a request for a nearby value, and in the case of temporal extents, whether the most recent map may be requested
 * without specifying its exact time
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "dimension")
public class Dimension implements IPersistentMetadataObject, ISerializableMetadataObject {

    /**
     * Constructor for the class
     *
     * @param name           the name of the dimension
     * @param units          the units of measure for the dimension
     * @param unitSymbol     the symbol for the above mentioned units
     * @param defaultValue   the default value for this dimension
     * @param multipleValues Boolean attribute indicating whether multiple values of the dimension may be
     *                       requested. 0 (or “false”) = single values only; 1 (or “true”) = multiple values permitted. Default = 0.
     * @param nearestValue   Boolean attribute indicating whether nearest value of the dimension will be returned in
     *                       response to a request for a nearby value. 0 (or “false”) = request value(s) must
     *                       correspond exactly to declared extent value(s); 1 (or “true”) = request values may be approximate. Default = 0.
     * @param current        Boolean attribute valid only for temporal extents (i.e. if attribute name="time"). This
     *                       attribute, if it either 1 or “true”, indicates (a) that temporal data are normally kept
     *                       current and (b) that the request parameter TIME may include the keyword “current” instead of an ending value (see C.4.1). Default = 0.
     * @param extent         Text content indicating available value(s) for dimension.
     * @param order          the order of this axis in rasdaman (e.g. z might be the third axis in rasdaman)
     * @param layer          the layer to which this dimension belongs to
     */
    public Dimension(@NotNull String name, @NotNull String units, @Nullable String unitSymbol, @Nullable String defaultValue,
                     boolean multipleValues, boolean nearestValue, boolean current,
                     @NotNull String extent, int order, @Nullable Layer layer) {
        this.name = name;
        this.units = units;
        this.unitSymbol = unitSymbol;
        this.defaultValue = defaultValue;
        this.multipleValues = multipleValues;
        this.nearestValue = nearestValue;
        this.current = current;
        this.extent = extent;
        this.order = order;
        this.layer = layer;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected Dimension() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(ConfigManager.PATH_TO_TEMPLATES + "Dimension.tpl.xml");
    }

    /**
     * Returns for each variable allowed in the template a corresponding values. The variable name should NOT include the $ prefix
     *
     * @return a map of form (variableKey -> variableValue)
     */
    @NotNull
    @Override
    public Map<String, String> getTemplateVariables() throws IOException {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("name", name);
        ret.put("units", units);
        ret.put("unitSymbol", unitSymbol);
        ret.put("defaultValue", defaultValue);
        ret.put("multipleValues", String.valueOf(multipleValues));
        ret.put("nearestValue", String.valueOf(nearestValue));
        ret.put("current", String.valueOf(current));
        ret.put("extent", extent);
        return ret;
    }

    /**
     * Returns the extent of the dimension
     *
     * @return the extent of the dimension
     */
    public String getExtent() {
        return extent;
    }

    /**
     * Sets the extent of the dimension
     *
     * @param extent the extent of the dimension
     */
    public void setExtent(String extent) {
        this.extent = extent;
    }

    /**
     * Returns the order on the rasdaman axes for this dimension
     *
     * @return the order on the rasdaman axes
     */
    public int getOrder() {
        return order;
    }

    /**
     * Returns the extent of the dimension as a rasdaman subset
     *
     * @return the extent of the dimension
     * @throws WMSInvalidDimensionValue
     */
    public RasdamanSubset getExtentAsSubset() throws WMSInvalidDimensionValue {
        if (extent == null || extent.equals("")) {
            throw new WMSInvalidDimensionValue("");
        }
        String[] extentComponents = extent.split("/");
        if (extentComponents.length == 1) {
            long lext = Long.parseLong(extentComponents[0]);
            return new RasdamanSubset(order, lext, lext);
        } else if (extentComponents.length > 1) {
            long lextMin = Long.parseLong(extentComponents[0]);
            long lextMax = Long.parseLong(extentComponents[extentComponents.length - 1]);
            return new RasdamanSubset(order, lextMin, lextMax);
        } else {
            throw new WMSInvalidDimensionValue(extent);
        }
    }

    @DatabaseField(canBeNull = false)
    private int id;

    @DatabaseField(canBeNull = false, columnName = NAME_COLUMN_NAME)
    private String name;

    @DatabaseField(canBeNull = false)
    private String units;

    @DatabaseField(canBeNull = true)
    private String unitSymbol;

    @DatabaseField(canBeNull = true)
    private String defaultValue;

    @DatabaseField(canBeNull = true)
    private boolean multipleValues;

    @DatabaseField(canBeNull = true)
    private boolean nearestValue;

    @DatabaseField(canBeNull = true)
    private boolean current;

    @DatabaseField(canBeNull = false)
    private String extent;

    @DatabaseField(canBeNull = false)
    private int order;

    @DatabaseField(canBeNull = false, foreign = true)
    private Layer layer;

    public static final String NAME_COLUMN_NAME = "name";

}
