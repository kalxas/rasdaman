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
import petascope.wms2.util.ConfigManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a style metadata element in WMS 1.3. According to the standard the definition is:
 * <p/>
 * Zero or more Styles may be advertised for a Layer or collection of layers using <Style> elements, each of which
 * shall have <Name> and <Title> elements. The style's Name is used in the Map request STYLES parameter. The
 * Title is a human-readable string. If only a single style is available, that style is known as the “default” style and
 * need not be advertised by the server.
 * <p/>
 * A <Style> may contain several other elements. <Abstract> provides a narrative description while <LegendURL>
 * contains the location of an image of a map legend appropriate to the enclosing style. A <Format> element in
 * LegendURL indicates the MIME type of the legend image, and the optional attributes width and height state
 * the size of the image in pixels. Servers should provide the width and height attributes if known at the time of
 * processing the GetCapabilities request. The legend image should clearly represent the symbols, lines and colours
 * used in the map portrayal. The legend image should not contain text that duplicates the Title of the layer, because
 * that information is known to the client and may be shown to the user by other means.
 * <p/>
 * Style declarations are inherited by child Layers. A child shall not redefine a Style with the same Name as one
 * inherited from a parent. A child may define a new Style with a new Name that is not available for the parent Layer.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "style")
public class Style implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param name                  the name of the style
     * @param title                 the title of the style
     * @param styleAbstract         the abstract of the style
     * @param legendURL             the url to the legend of the style
     * @param rasqlQueryTransformer a fragment of rasql style that can be applied over a layer to transform it
     * @param layer                 the layer object to which this style should belong to
     */
    public Style(@NotNull String name, @NotNull String title,
                 @NotNull String styleAbstract, @Nullable LegendURL legendURL,
                 @NotNull String rasqlQueryTransformer, @NotNull Layer layer) {
        this.name = name;
        this.title = title;
        this.styleAbstract = styleAbstract;
        this.legendURL = legendURL;
        this.rasqlQueryTransformer = rasqlQueryTransformer;
        this.layer = layer;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected Style() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(ConfigManager.PATH_TO_TEMPLATES + "Style.tpl.xml");
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
        ret.put("title", title);
        ret.put("abstract", styleAbstract);
        ret.put("legendURL", serializeLegendUrl());
        return ret;
    }

    /**
     * Returns the rasql query transformer
     *
     * @return the rasql query transformer
     */
    @NotNull
    public String getRasqlQueryTransformer() {
        return rasqlQueryTransformer;
    }
    
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Serializes the legend url field in xml format according to its template
     *
     * @return the serialized field as a string
     * @throws IOException
     */
    private String serializeLegendUrl() throws IOException {
        MetadataObjectXMLSerializer serializer = new MetadataObjectXMLSerializer();
        return serializer.serialize(legendURL);
    }

    @NotNull
    @DatabaseField(id = true, columnName = NAME_COLUMN_NAME)
    private String name;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String title;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String styleAbstract;

    @Nullable
    @DatabaseField(foreign = true)
    private LegendURL legendURL;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String rasqlQueryTransformer;

    @NotNull
    @DatabaseField(foreign = true)

    private Layer layer;
    public static final String NAME_COLUMN_NAME = "name";
}
