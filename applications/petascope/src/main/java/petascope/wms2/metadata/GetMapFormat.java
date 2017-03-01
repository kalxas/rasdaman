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
 * Representation of a format metadata element for get map requests.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "getmap_format")
public class GetMapFormat implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param format the format in mimetype style
     */
    public GetMapFormat(@NotNull String format, @NotNull String rasdamanFormat) {
        this.format = format;
        this.rasdamanFormat = rasdamanFormat;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected GetMapFormat() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(ConfigManager.PATH_TO_TEMPLATES + "Format.tpl.xml");
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
        return ret;
    }

    /**
     * Returns the rasdaman format corresponding to this get map format
     *
     * @return the corresponding rasdaman format
     */
    @NotNull
    public String getRasdamanFormat() {
        return rasdamanFormat;
    }

    /**
     * Returns the format of the get map request
     *
     * @return the format of a get map request
     */
    @NotNull
    public String getFormat() {
        return format;
    }

    @NotNull
    @DatabaseField(id = true)
    private String format;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String rasdamanFormat;
}
