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
 * Representation of the geographic bounding box of a layer. Definition according to the standard:
 * <p/>
 * Every named Layer shall have exactly one <EX_GeographicBoundingBox> element that is either stated explicitly
 * or inherited from a parent Layer. EX_GeographicBoundingBox states, via the elements westBoundLongitude,
 * eastBoundLongitude, southBoundLatitude, and northBoundLatitude, the minimum bounding rectangle in decimal
 * degrees of the area covered by the Layer. EX_GeographicBoundingBox shall be supplied regardless of what CRS
 * the map server may support, but it may be approximate if the data are not natively in geographic coordinates. The
 * purpose of EX_GeographicBoundingBox is to facilitate geographic searches without requiring coordinate
 * transformations by the search engine.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "ex_geographic_bounding_box")
public class EXGeographicBoundingBox implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param westBoundLongitude  the west bound of the area
     * @param eastBoundLongitude  the east bound of the area
     * @param southBoundLatitude the south bound of the area
     * @param northBoundLatitude the north bound of the area
     */
    public EXGeographicBoundingBox(@NotNull String westBoundLongitude, @NotNull String eastBoundLongitude,
                                   @NotNull String southBoundLatitude, @NotNull String northBoundLatitude) {
        this.westBoundLongitude = westBoundLongitude;
        this.eastBoundLongitude = eastBoundLongitude;
        this.southBoundLatitude = southBoundLatitude;
        this.northBoundLatitude = northBoundLatitude;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected EXGeographicBoundingBox() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(ConfigManager.PATH_TO_TEMPLATES + "EX_GeographicBoundingBox.tpl.xml");
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
        ret.put("westBoundLongitude", westBoundLongitude);
        ret.put("eastBoundLongitude", eastBoundLongitude);
        ret.put("southBoundLatitude", southBoundLatitude);
        ret.put("northBoundLatitude", northBoundLatitude);
        return ret;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String westBoundLongitude;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String eastBoundLongitude;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String southBoundLatitude;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String northBoundLatitude;
}
