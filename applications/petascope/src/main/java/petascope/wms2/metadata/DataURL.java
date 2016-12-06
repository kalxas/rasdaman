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
import petascope.wms2.util.ConfigManager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A representation of the data url field in the metadata. According to the standard the definition is:
 * A server may use DataURL to offer a link to the underlying data represented by a particular layer. The enclosed
 * Format element indicates the file format MIME type of the data file.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "dataurl")
public class DataURL implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param format         the format of the url
     * @param onlineResource the href to the data
     */
    public DataURL(@NotNull String format, @NotNull String onlineResource) {
        this.format = format;
        this.onlineResource = onlineResource;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected DataURL() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(ConfigManager.PATH_TO_TEMPLATES + "DataURL.tpl.xml");
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
        ret.put("format", format);
        ret.put("onlineResource", onlineResource);
        return ret;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String format;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String onlineResource;
}
