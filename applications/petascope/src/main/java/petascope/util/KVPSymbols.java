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

    String KEY_ACCEPTFORMATS       = "acceptformats";
    String KEY_ACCEPTLANGUAGES     = "acceptlanguages";
    String KEY_ACCEPTVERSIONS      = "acceptversions";
    String KEY_COVERAGEID          = "coverageid";
    String KEY_FORMAT              = "format";
    String KEY_INTERPOLATION       = "interpolation";
    String KEY_MEDIATYPE           = "mediatype";
    String KEY_RANGESUBSET         = "rangesubset";
    String KEY_REQUEST             = "request";
    String KEY_SCALEAXES           = "scaleaxes";
    String KEY_SCALEEXTENT         = "scaleextent";
    String KEY_SCALEFACTOR         = "scalefactor";
    String KEY_SCALESIZE           = "scalesize";
    String KEY_SERVICE             = "service";
    String KEY_SUBSET              = "subset";
    String KEY_VERSION             = "version";
    String KEY_PROCESS_COVERAGES   = "ProcessCoverages";
    String KEY_QUERY               = "query";
    String VERSIONS_SEP            = ",";
    // This supports *Coverage Summary* GetCapabilities&sections=CoverageSummary|Contents|All to return standard GetCapabilities (#153)
    String KEY_SECTIONS                                       = "sections";
    String VALUE_SECTIONS_COVERAGESUMMARY_COVEARGE_SUMMARY    = "CoverageSummary";
    String VALUE_SECTIONS_COVERAGESUMMARY_CONTENTS            = "Contents";
    String VALUE_SECTIONS_COVERAGESUMMARY_ALL                 = "All";
    String KEY_OUTPUTCRS           = "outputcrs";
    String KEY_SUBSETCRS           = "subsettingcrs";   
    String KEY_GET_CAPABILITIES    = "GetCapabilities";
    String KEY_GET_COVERAGE        = "GetCoverage";
    String KEY_DESCRIBE_COVERAGE   = "DescribeCoverage";

    // rasql KVP
    String KEY_PASSWORD = "password";    
    String KEY_USERNAME = "username";
}
