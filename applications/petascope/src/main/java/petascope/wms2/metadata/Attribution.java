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
 * Class to represent the Attribution field of a layer in WMS 1.3.0. According to the standard, its definition is:
 * <p/>
 * The optional <Attribution> element provides a way to identify the source of the geographic information used in
 * Layer or collection of Layers. Attribution encloses several optional elements: <OnlineResource> states the data
 * provider’s URL; <Title> is a human-readable string naming the data provider; <LogoURL> is the URL of a logo
 * image. Client applications may choose to display one or more of these items. A <Format> element in LogoURL
 * indicates the MIME type of the logo image, and the attributes width and height state the size of the image in pixels.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "attribution")
public class Attribution implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param title              the title of the attribution
     * @param onlineResourceHelp a link to the description of the attribution
     * @param logoWidth          the width of the logo
     * @param logoHeight         the height of the logo
     * @param logoFormat         the format of the logo
     * @param logoOnlineResource an url to the logo image
     */
    public Attribution(@NotNull String title, @NotNull String onlineResourceHelp,
                       int logoWidth, int logoHeight, @NotNull String logoFormat,
                       @NotNull String logoOnlineResource) {
        this.title = title;
        this.onlineResourceHelp = onlineResourceHelp;
        this.logoWidth = logoWidth;
        this.logoHeight = logoHeight;
        this.logoFormat = logoFormat;
        this.logoOnlineResource = logoOnlineResource;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected Attribution() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(PATH_TO_TEMPLATES + "Attribution.tpl.xml");
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
        ret.put("title", this.title);
        ret.put("onlineResourceHelp", this.onlineResourceHelp);
        ret.put("logoWidth", String.valueOf(this.logoWidth));
        ret.put("logoHeight", String.valueOf(this.logoHeight));
        ret.put("logoFormat", this.logoFormat);
        ret.put("logoOnlineResource", this.logoOnlineResource);
        return ret;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    @NotNull
    private String title;

    @DatabaseField(canBeNull = false)
    @NotNull
    private String onlineResourceHelp;

    @DatabaseField(canBeNull = false)
    private int logoWidth;

    @DatabaseField(canBeNull = false)
    private int logoHeight;

    @DatabaseField(canBeNull = false)
    @NotNull
    private String logoFormat;

    @DatabaseField(canBeNull = false)
    @NotNull
    private String logoOnlineResource;
}
