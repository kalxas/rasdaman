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

import org.jetbrains.annotations.NotNull;
import petascope.wms2.service.exception.error.WMSException;

/**
 * An exception response to be used when the client requests that we throw the service exceptions in xml format
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class XMLExceptionResponse extends ExceptionResponse {
    /**
     * Constructor for the class
     *
     * @param exception the exception to be transformed into a response
     */
    public XMLExceptionResponse(@NotNull WMSException exception) {
        super(exception);
    }

    @Override
    public byte[] toBytes() {
        WMSExceptionSerializer serializer = new WMSExceptionSerializer();
        String result;
        try {
            result = serializer.serialize(exception);
            return result.getBytes(getDefaultEncoding());
        } catch (Exception e) {
            throw new IllegalArgumentException("The serializer was not initialized correctly. Please check that a template exists"
                                               + "in $CATALINA_HOME/webapps/rasdaman/WEB-INF/classes/templates/wms/WMSException.tpl.xml");
        }
    }
}
