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
 * Class representation of the crs element belonging to a layer. According to the standard the definition is:
 * <p/>
 * Every Layer is available in one or more layer coordinate reference systems. 6.7.3 discusses the Layer CRS. In
 * order to indicate which Layer CRSs are available, every named Layer shall have at least one <CRS> element that
 * is either stated explicitly or inherited from a parent Layer. The root <Layer> element shall include a sequence of
 * zero or more CRS elements listing all CRSs that are common to all subsidiary layers. A child layer may optionally
 * add to the list inherited from a parent layer. Any duplication shall be ignored by clients.
 * When a Layer is available in several coordinate reference systems, the list of available CRS values shall be
 * represented as a sequence of <CRS> elements, each of which contains only a single CRS name.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "crs")
public class Crs implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param crs the crs name
     */
    public Crs(@NotNull String crs) {
        this.crs = crs;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected Crs() {

    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(PATH_TO_TEMPLATES + "Crs.tpl.xml");
    }

    /**
     * Returns the crs name
     *
     * @return the crs name
     */
    @NotNull
    public String getCrsName() {
        return crs;
    }

    /**
     * Returns for each variable allowed in the template a corresponding values. The variable name should NOT include the $ prefix
     *
     * @return a map of form (variableKey -> variableValue)
     */
    @NotNull
    @Override
    public Map<String, String> getTemplateVariables() throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("crs", crs);
        return map;
    }

    @NotNull
    @DatabaseField(id = true, columnName = CRS_COLUMN_NAME)
    private String crs;

    public static final String CRS_COLUMN_NAME = "crs";
}
