"""
 *
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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
from master.request.wcst import WCSTExecutor


class ConfigManager:
    """
     Holds the global information needed across the application.
     The values below will be overridden as soon as the application is initialized, the values
     below serve only as documentation
    """
    wcs_service = "http://localhost:8080/rasdaman/ows"
    service_is_local = True

    admin_service = "http://localhost:8080/rasdaman/admin"
    crs_resolver = ""
    default_crs = "http://localhost:8080/def/OGC/0/Index2D"
    tmp_directory = "/tmp/rasdaman_wcst_import/"
    mock = True
    insitu = False
    black_listed = False
    automated = False
    default_null_values = []
    root_url = "file://"
    executor = WCSTExecutor(wcs_service)
    default_field_name_prefix = "field_"
    default_unit_of_measure = "10^0"
    subset_correction = False
    skip = False
    retry = False
    retries = 5
    retry_sleep = 1
    slice_restriction = None
    resumer_dir_path = ""
    description_max_no_slices = 5
    track_files = True
    # input file name of wcst_import.sh
    ingredient_file_name = ""

    user = ""
    passwd = ""

    # (no limit, cache all files)
    gdal_cache_size=-1

    # Default mimeType is none -> gdal driver for petascope
    mime_type = None

    has_resume_file = False
