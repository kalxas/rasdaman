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

import org.jetbrains.annotations.NotNull;
import petascope.wms2.util.ConfigManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a Get Capabilities document based on the WMS 1.3 standard. The definition accord to the standard is:
 * <p/>
 * When invoked on a WMS, the response to a GetCapabilities request shall be an XML document containing
 * service metadata formatted according to the XML Schema in E.1. The schema specifies the mandatory and
 * optional content of the service metadata and how the content is formatted. The XML document shall contain a
 * root element named WMS_Capabilities in the “http://www.opengis.net/wms” namespace. This element shall
 * contain an XML Schema instance schemaLocation attribute that binds the “http://www.opengis.net/wms”
 * namespace to the schema in E.1. The schema bound to the root attribute may be a copy of the schema in E.1
 * instead of the master copy at the URL stated in the annex provided none of the normative content of the schema
 * is changed. The schema copy shall be located at a fully-qualified and accessible URL to permit XML validating
 * software to retrieve it. The response shall be valid according to XML Schema validation rules.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class GetCapabilities implements ISerializableMetadataObject {

    /**
     * Constructor for the class
     *
     * @param version          the version for the WMS service
     * @param schemaLocation   the xml schema location
     * @param updateSequence   the update sequence number for the service. By convention, we use the Service id
     * @param serviceUrl       the url to the WMS service
     * @param service          the service metadata object
     * @param getCapFormats    a list of get capabilities formats
     * @param getMapFormats    a list of get map formats
     * @param exceptionFormats a list of exception formats
     * @param layers           a list of layers
     */
    public GetCapabilities(@NotNull String version, @NotNull String schemaLocation,
                           int updateSequence, @NotNull String serviceUrl, @NotNull Service service,
                           @NotNull Iterable<GetCapabilitiesFormat> getCapFormats, @NotNull Iterable<GetMapFormat> getMapFormats,
                           @NotNull Iterable<ExceptionFormat> exceptionFormats, @NotNull Iterable<Layer> layers) {
        this.version = version;
        this.schemaLocation = schemaLocation;
        this.updateSequence = updateSequence;
        this.serviceUrl = serviceUrl;
        this.service = service;
        this.getCapFormats = getCapFormats;
        this.getMapFormats = getMapFormats;
        this.exceptionFormats = exceptionFormats;
        this.layers = layers;
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(ConfigManager.PATH_TO_TEMPLATES + "GetCapabilities.tpl.xml");
    }

    /**
     * Returns for each variable allowed in the template a corresponding values. The variable name should NOT include the $ prefix
     *
     * @return a map of form (variableKey -> variableValue)
     */
    @NotNull
    @Override
    public Map<String, String> getTemplateVariables() throws IOException {
        MetadataObjectXMLSerializer serializer = new MetadataObjectXMLSerializer();
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("version", version);
        ret.put("schemaLocation", schemaLocation);
        ret.put("serviceUrl", serviceUrl);
        ret.put("updateSequence", String.valueOf(updateSequence));
        ret.put("service", serializer.serialize(service));
        ret.put("getMapFormats", serializer.serializeCollection(getMapFormats));
        ret.put("getCapabilitiesFormats", serializer.serializeCollection(getCapFormats));
        ret.put("exceptionFormats", serializer.serializeCollection(exceptionFormats));
        ret.put("layers", serializer.serializeCollection(layers));
        return ret;
    }

    @NotNull
    private final String version;

    @NotNull
    private final String schemaLocation;

    private final int updateSequence;

    @NotNull
    private final String serviceUrl;

    @NotNull
    private final Service service;

    @NotNull
    private final Iterable<GetCapabilitiesFormat> getCapFormats;

    @NotNull
    private final Iterable<GetMapFormat> getMapFormats;

    @NotNull
    private final Iterable<ExceptionFormat> exceptionFormats;

    @NotNull
    private final Iterable<Layer> layers;
}
