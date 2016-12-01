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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a metadata url element for WMS 1.3. According to the standard the definition is:
 * <p/>
 * A server should use one or more <MetadataURL> elements to offer detailed, standardized metadata about the
 * data corresponding to a particular layer. The “type” attribute indicates the standard to which the metadata
 * complies. Two “type” attribute values are defined by this International Standard: the value “ISO 19115:2003”
 * refers to ISO 19115:2003; the value “FGDC:1998” refers to FGDC-STD-001-1998 [1]. An information community
 * may define meanings for other “type” attribute values. The enclosed <Format> element indicates the file format
 * MIME type of the metadata record.
 * <p/>
 * MetadataURL elements are not inherited by child Layers.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "metadata_url")
public class MetadataURL implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param type           the type of the external metadata
     * @param onlineResource the url to the external metadata
     */
    public MetadataURL(@NotNull String type, @NotNull String onlineResource) {
        this.type = type;
        this.onlineResource = onlineResource;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected MetadataURL() {}

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(PATH_TO_TEMPLATES + "MetadataURL.tpl.xml");
    }

    /**
     * Returns for each variable allowed in the template a corresponding values. The variable name should NOT include the $ prefix
     *
     * @return a map of form (variableKey -> variableValue)
     */
    @NotNull
    @Override
    public Map<String, String> getTemplateVariables() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("type", type);
        ret.put("onlineResource", onlineResource);
        return ret;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String type;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String onlineResource;
}
