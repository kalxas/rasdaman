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

package petascope.wms2.service.exception.response;

import org.apache.commons.io.IOUtils;
import petascope.wms2.service.exception.error.WMSException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class that serializes wms exceptions into a compliant xml template
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
class WMSExceptionSerializer {

    /**
     * Constructor for the class
     */
    public WMSExceptionSerializer() {
    }

    /**
     * Serializes the wms exception into a compliant xml template
     *
     * @param exception the exception to be serialized
     * @return the serialized exception
     * @throws IOException
     */
    public String serialize(WMSException exception) throws IOException {
        InputStream stream = this.getClass().getResourceAsStream(PATH_TO_TEMPLATES + "WMSException.tpl.xml");
        String template = IOUtils.toString(stream);
        template = template.replace("$ExceptionCode$", exception.getExceptionCode());
        template = template.replace("$ExceptionText$", exception.getErrorMessage());
        return template;
    }

    /**
     * Contains the path to the xml templates relative to the classpath
     */
    private final static String PATH_TO_TEMPLATES = "/templates/wms/";

}
