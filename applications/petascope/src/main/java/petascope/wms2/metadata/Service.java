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

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.util.ConfigManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent the service field set of a WMS 1.3 get capabilities response. According to the standard the definition is:
 * <p/>
 * The first part of the service metadata is a <Service> element providing general metadata for the server as a whole.
 * It shall include a Name, Title, and Online Resource URL. Optional service metadata includes Abstract, Keyword
 * List, Contact Information, Fees, Access Constraints, and limits on the number of layers in a request or the output
 * size of maps.
 * The Service Name shall be “WMS” in the case of a WMS.
 * The Service Title is at the discretion of the provider, and should be brief yet descriptive enough to identify this
 * server in a menu with other servers.
 * The Abstract element allows a descriptive narrative providing more information about the enclosing object.
 * The OnlineResource element within the Service element may be used to refer to the web site of the service
 * provider. There are other OnlineResource elements used for the URL prefix of each supported operation (see
 * below).
 * <p/>
 * A list of keywords or keyword phrases describing the server as a whole should be included to help catalogue
 * searching. Each keyword may be accompanied by a “vocabulary” attribute to indicate the defining authority for
 * that keyword. One “vocabulary” attribute value is defined by this International Standard: the value
 * “ISO 19115:2003” refers to the metadata topic category codes defined in ISO 19115:2003, B.5.27; information
 * communities may define other “vocabulary” attribute values. No particular vocabulary is mandated by this
 * International Standard.
 * Contact Information should be included.
 * The optional <LayerLimit> element in the service metadata is a positive integer indicating the maximum number of
 * layers a client is permitted to include in a single GetMap request. If this element is absent, the server imposes no
 * limit.
 * <p/>
 * The optional <MaxWidth> and <MaxHeight> elements in the service metadata are positive integers indicating the
 * maximum width and height values that a client is permitted to include in a single GetMap request. If either element
 * is absent the server imposes no limit on the corresponding parameter.
 * The optional elements <Fees> and <AccessConstraints> may be omitted if they do not apply to the server. If
 * either of those elements is present, the reserved word “none” (case-insensitive) shall be used if there are no fees
 * or access constraints, as follows: <Fees>none</Fees>, <AccessConstraints>none</AccessConstraints>. When
 * constraints are imposed, no precise syntax has been defined for the text content of these elements, but client
 * applications may display the content for user information and action.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "service")
public class Service implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param name              the name of the service
     * @param title             the title of the service
     * @param serviceAbstract   the abstract description of the service
     * @param onlineResource    an url to a more detailed description of the service
     * @param fees              the fees that are requested for access
     * @param accessConstraints any additional access constraints
     */
    public Service(@NotNull String name, @NotNull String title,
                   @NotNull String serviceAbstract, @NotNull String onlineResource,
                   @NotNull String fees,
                   @NotNull ContactInformation information,
                   @NotNull String accessConstraints) {
        this.name = name;
        this.title = title;
        this.serviceAbstract = serviceAbstract;
        this.onlineResource = onlineResource;
        this.contactInformation = information;
        this.fees = fees;
        this.accessConstraints = accessConstraints;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected Service() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(ConfigManager.PATH_TO_TEMPLATES + "Service.tpl.xml");
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
        ret.put("serviceAbstract", serviceAbstract);
        ret.put("onlineResource", onlineResource);
        ret.put("fees", fees);
        ret.put("contactInformation", serializeContactInformation());
        ret.put("keywords", serializeKeywords());
        ret.put("accessConstraints", accessConstraints);
        return ret;
    }

    /**
     * Serializes the keywords associated with the service using the xml representation
     *
     * @return the serialized keywords as a string
     * @throws IOException
     */
    private String serializeKeywords() throws IOException {
        MetadataObjectXMLSerializer serializer = new MetadataObjectXMLSerializer();
        return serializer.serializeCollection(keywords);
    }

    /**
     * Serializes the contact information associated with the service using the xml representation
     *
     * @return the serialized contact information as a string
     * @throws IOException
     */
    private String serializeContactInformation() throws IOException {
        MetadataObjectXMLSerializer serializer = new MetadataObjectXMLSerializer();
        return serializer.serialize(contactInformation);
    }

    /**
     * Returns the keywords associated with this service
     *
     * @return a list of keywords
     */
    @Nullable
    public ForeignCollection<ServiceKeyword> getKeywords() {
        return keywords;
    }

    /**
     * Returns the contact information
     *
     * @return the contact information
     */
    @NotNull
    public ContactInformation getContactInformation() {
        return contactInformation;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String name;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String title;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String serviceAbstract;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String onlineResource;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String fees;

    @NotNull
    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    private ContactInformation contactInformation;

    @org.jetbrains.annotations.Nullable
    @ForeignCollectionField(eager = false)
    private ForeignCollection<ServiceKeyword> keywords;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String accessConstraints;
}
