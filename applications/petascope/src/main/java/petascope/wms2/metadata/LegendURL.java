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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representation of a LengendURL element in a style in WMS 1.3. According to the standard the definition is:
 * <p/>
 * LegendURL contains the location of an image of a map legend appropriate to the enclosing style. A <Format> element in
 * LegendURL indicates the MIME type of the legend image, and the optional attributes width and height state
 * the size of the image in pixels. Servers should provide the width and height attributes if known at the time of
 * processing the GetCapabilities request. The legend image should clearly represent the symbols, lines and colours
 * used in the map portrayal. The legend image should not contain text that duplicates the Title of the layer, because
 * that information is known to the client and may be shown to the user by other means
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "legend_url")
public class LegendURL implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param width          the width of the legend
     * @param height         the height of the legend
     * @param format         the format of the legend
     * @param onlineResource a link to the legend in image format
     */
    public LegendURL(@NotNull String width, @NotNull String height, @NotNull String format, @NotNull String onlineResource) {
        this.width = width;
        this.height = height;
        this.format = format;
        this.onlineResource = onlineResource;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected LegendURL() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(PATH_TO_TEMPLATES + "LegendURL.tpl.xml");
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
        ret.put("width", width);
        ret.put("height", height);
        ret.put("format", format);
        ret.put("onlineResource", onlineResource);
        return ret;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String width;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String height;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String format;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String onlineResource;


}
