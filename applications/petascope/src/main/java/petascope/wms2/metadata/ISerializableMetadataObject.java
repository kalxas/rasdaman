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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * A metadata object stores metadata that can be written to a template. The template can be any file that contains
 * zero or more variables ($variableName) corresponding to values of the metadata object.
 * A MetadataObjectXMLSerializer can be used to produce a filled template from the two.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
interface ISerializableMetadataObject {

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    public InputStream getStreamToTemplate();

    /**
     * Returns for each variable allowed in the template a corresponding values. The variable name should NOT include the $ prefix
     *
     * @return a map of form (variableKey -> variableValue)
     */
    @NotNull
    public Map<String, String> getTemplateVariables() throws IOException;

}
