#!/bin/bash
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.    If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#    test.sh
# Description
#    Import coverage with scale_levels via WCST_Import and manual requests.
#
################################################################################

# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done

SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

readonly COVERAGE_ID="test_import_scale_levels"
readonly LEVELS=(2 4 6)

# NOTE: script_dir is folder of children test cases and it needs to be specified in path variables
readonly ORACLE="$SCRIPT_DIR/oracle"
readonly OUTPUT="$SCRIPT_DIR/output"

check_data() {
    for level in "${LEVELS[@]}"; do
        collection_name="$COVERAGE_ID"_"$level"
        file_name="level_$level.tif"

        rasql -q 'select encode(c, "tiff") from '"$collection_name"' as c' > /dev/null 2>&1 --out file
        mv "rasql_1.tif" "$OUTPUT/$file_name" > /dev/null 2>&1
        cmp "$ORACLE/$file_name" "$OUTPUT/$file_name" > /dev/null 2>&1
        check_result "0" "$?" "downscaled level $level"
    done
}

sed "s@PETASCOPE_URL@$PETASCOPE_URL@g" "$SCRIPT_DIR/ingest.template.json" > "$SCRIPT_DIR/ingest.json"

# Clean the output directory if exists
rm -rf "$OUTPUT"
mkdir -p "$OUTPUT"

# First, import 2D coverage with scale levels from ingredient file
$WCST_IMPORT "$SCRIPT_DIR/ingest.json" > /dev/null 2>&1
check_result 0 $? "importing data"

# Then, check downscaled collections with oracles
log "Test that downscaled collections match the oracles..."
check_data

# Then, delete one of the downscaled collection by level
delete_scale_level_request="$PETASCOPE_URL?service=WCS&request=DeleteScaleLevel&version=2.0.1&coverageId=$COVERAGE_ID&level=4"
result=$(get_http_return_code "$delete_scale_level_request")
check_result "200" "$result" "delete downscaled collection level 4"

# Then, insert this downscaled collection by level
insert_scale_level_request="$PETASCOPE_URL?service=WCS&request=InsertScaleLevel&version=2.0.1&coverageId=$COVERAGE_ID&level=4"
result=$(get_http_return_code "$insert_scale_level_request")
check_result "200" "$result" "insert downscaled collection level 4"

# Finally, recheck the downscaled collections data with oracle files
log "Retest that downscaled collections match the oracles..."
check_data

# And delete this test coverage
delete_coverage_request="$PETASCOPE_URL?service=WCS&request=DeleteCoverage&version=2.0.1&coverageId=$COVERAGE_ID"
result=$(get_http_return_code "$delete_coverage_request")
check_result "200" "$result" "delete coverate $COVERAGE_ID"

# TODO: check that downscaled collections are deleted as well

# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC

