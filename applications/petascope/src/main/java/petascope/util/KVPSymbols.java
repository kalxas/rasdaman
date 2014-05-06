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
package petascope.util;

/**
 * All recognized keys for KVP requests
 *
 * @author <a href="mailto:m.rusu@jacobs-university.de">Mihaela Rusu</a>
 */
public interface KVPSymbols {

    public static final String KEY_ACCEPTFORMATS       = "acceptformats";
    public static final String KEY_ACCEPTLANGUAGES     = "acceptlanguages";
    public static final String KEY_ACCEPTVERSIONS      = "acceptversions";
    public static final String KEY_COVERAGEID          = "coverageid";
    public static final String KEY_FORMAT              = "format";
    public static final String KEY_INTERPOLATION       = "interpolation";
    public static final String KEY_MEDIATYPE           = "mediatype";
    public static final String KEY_RANGESUBSET         = "rangesubset";
    public static final String KEY_REQUEST             = "request";
    public static final String KEY_SCALEAXES           = "scaleaxes";
    public static final String KEY_SCALEEXTENT         = "scaleextent";
    public static final String KEY_SCALEFACTOR         = "scalefactor";
    public static final String KEY_SCALESIZE           = "scalesize";
    public static final String KEY_SERVICE             = "service";
    public static final String KEY_SUBSET              = "subset";
    public static final String KEY_VERSION             = "version";
    public static final String VERSIONS_SEP            = ",";

    // rasql KVP
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_QUERY    = "query";
    public static final String KEY_USERNAME = "username";
}
