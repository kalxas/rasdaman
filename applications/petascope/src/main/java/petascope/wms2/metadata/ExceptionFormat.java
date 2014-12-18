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
 * Class representation of an exception format according to the WMS 1.3 standard. According to the standard the definition is:
 *
 * Upon receiving a request that is invalid according to this International Standard, the server shall issue a service
 * exception report. The service exception report is meant to describe to the client application or its human user the
 * reason(s) that the request is invalid. The EXCEPTIONS parameter in a request indicates the format in which the
 * client wishes to be notified of service exceptions. The allowed service exception formats are defined for each
 * operation below.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */

@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "exception_format")
public class ExceptionFormat implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param format the format in mimetype style
     */
    public ExceptionFormat(@NotNull String format) {
        this.format = format;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected ExceptionFormat() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(PATH_TO_TEMPLATES + "Format.tpl.xml");
    }

    /**
     * Returns the format of the exception to be returned
     * @return the exception format
     */
    @NotNull
    public String getFormat() {
        return format;
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

    @NotNull
    @DatabaseField(id = true)
    private String format;
}