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
 * Representation of a keyword metadata element. Definition according to the standard:
 * <p/>
 * A list of keywords or keyword phrases describing the server as a whole should be included to help catalogue
 * searching. Each keyword may be accompanied by a “vocabulary” attribute to indicate the defining authority for
 * that keyword. One “vocabulary” attribute value is defined by this International Standard: the value
 * “ISO 19115:2003” refers to the metadata topic category codes defined in ISO 19115:2003, B.5.27; information
 * communities may define other “vocabulary” attribute values. No particular vocabulary is mandated by this
 * International Standard.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "keyword")
public class ServiceKeyword implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param keyword the keyword associated with some element
     */
    public ServiceKeyword(@NotNull String keyword, @NotNull Service service) {
        this.keyword = keyword;
        this.service = service;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected ServiceKeyword() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(ConfigManager.PATH_TO_TEMPLATES + "Keyword.tpl.xml");
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
        ret.put("keyword", keyword);
        return ret;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String keyword;

    @NotNull
    @DatabaseField(foreign = true)
    private Service service;
}
