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
package petascope.core;

import java.util.Set;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;

/**
 * A IMetadataSource is anything that can read metadata for a given coverage
 * name. It must be able to list all coverages which it knows, return CoverageMetadata
 * for each one, and also map a format (e.g. "png") to its formatToMimetype
 * (e.g. "image/png").
 */
public interface IMetadataSource {

    Set<String> coverages() throws PetascopeException;

    String formatToMimetype(String format);
    String mimetypeToFormat(String mime);
    String formatToGdalid(String format);
    String gdalidToFormat(String gdalid);

    CoverageMetadata read(String coverageName) throws PetascopeException, SecoreException;
}
