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

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

/**
 * Fills a template for a metadata object
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class MetadataObjectXMLSerializer {

    /**
     * Constructor for the class
     */
    public MetadataObjectXMLSerializer() {

    }

    /**
     * Returns the serialized object based on its corresponding template
     *
     * @return a filled template based on the object's values
     * @throws IOException
     */
    public String serialize(@Nullable ISerializableMetadataObject metadataObject) throws IOException {
        if (metadataObject == null) {
            return NULL_SERIALIZATION;
        }
        String template = IOUtils.toString(metadataObject.getStreamToTemplate());
        Map<String, String> values = metadataObject.getTemplateVariables();
        return generateFilledTemplate(template, values);
    }

    /**
     * Returns the serialization of a collection based on the serialize method for individual objects
     *
     * @param collection a collection of metadata objects
     * @return the serialized collection
     * @throws IOException
     */
    public String serializeCollection(@Nullable Iterable<? extends ISerializableMetadataObject> collection) throws IOException {
        if (collection == null) {
            return NULL_SERIALIZATION;
        }
        StringBuilder serializedObject = new StringBuilder();
        for (ISerializableMetadataObject metadataObject : collection) {
            serializedObject.append(serialize(metadataObject)).append("\n");
        }
        return serializedObject.toString();
    }

    /**
     * Replaces each template variable with the corresponding value in the map and returns the filled template
     *
     * @param template a template to be filled
     * @param values   the values to fill the template with
     * @return the filled template
     */
    private static String generateFilledTemplate(@NotNull String template, @NotNull Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            //avoid a npe in case a field is missing
            String variableValue = entry.getValue() == null ? NULL_SERIALIZATION : entry.getValue();
            String variableName = TEMPLATE_VARIABLE_CHARACTER + entry.getKey() + TEMPLATE_VARIABLE_CHARACTER;
            template = template.replace(variableName, variableValue);
        }
        return template;
    }

    private static final String NULL_SERIALIZATION = "";
    private static final String TEMPLATE_VARIABLE_CHARACTER = "$";

}
