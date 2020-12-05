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
# Copyright 2003-2020 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#    test.sh
# Description
#    Test WCPS POST request with decode() operator from local files
#
################################################################################

# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done

SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

readonly ORACLE="$SCRIPT_DIR/oracle"
readonly OUTPUT="$SCRIPT_DIR/output"

readonly WCPS_ENDPOINT="$PETASCOPE_URL?service=WCS&version=2.0.1&request=ProcessCoverages&query="
readonly RASQL_SERVLET_ENDPOINT="$RASQL_SERVLET?username=rasguest&password=rasguest&query="

# Send a HEAD request and grap the MIME type in the headers
get_content_type() {
  # $1 is url endpoint
  local endpoint="$1"
  # $2 is query
  local query=$(urlencode "$2")
  local url="$endpoint""$query"
  curl -sI "$url" | grep 'Content-Type:' | cut -d' ' -f2
}

# write the request content to a test ouput file
write_to_output_file() {
  # $1 test case name
  local test_name="$1"
  # $2 test output
  local output="$2"
  local output_file="$OUTPUT/$test_name"

  echo "$output" > "$output_file"
  
}

handle_output() {
  # $1 test case name
  local test_name="$1"
  local oracle_file="$ORACLE/$test_name"
  local output_file="$OUTPUT/$test_name"

  log "Checking test case '$test_name'..."

  if [ ! -f "$oracle_file" ]; then
    cp "$output_file" "$oracle_file"
  else
    compare_output_to_oracle "$output_file" "$oracle_file"
  fi
}

handle() {
  # $1 test case name
  local test_name="$1"
  # $2 is url endpoint
  local endpoint="$2"
  # $3 is query
  local query="$3"

  output=$(get_content_type "$endpoint" "$query")
  write_to_output_file "$test_name" "$output"
  handle_output "$test_name"
}

# Clean the output directory if exists
rm -rf "$OUTPUT"
mkdir -p "$OUTPUT"

# Add more test files here

test_name="1-rasql_encode_tiff"
query='select encode(c, "GTiff", "{ \"nodata\": 200 }") from test_mr as c'
handle "$test_name" "$RASQL_SERVLET_ENDPOINT" "$query"

test_name="2-rasql_encode_png"
query='select encode(c[0:10,0:20], "image/png", "{ \"nodata\": 200 }") from test_mr as c'
handle "$test_name" "$RASQL_SERVLET_ENDPOINT" "$query"

test_name="3-rasql_encode_project_json"
query='select encode( (( project( c, "0.0, 0.0, 10.0, 30.0", "EPSG:4326", "EPSG:3857") )), "json", "{ \"cell_methods\": \"time: mean (interval: 30 minutes)\" }") from test_mr as c'
handle "$test_name" "$RASQL_SERVLET_ENDPOINT" "$query"

test_name="4-wcps_encode_jpeg"
query='for c in (test_mr) return encode(c, "jpeg")'
handle "$test_name" "$WCPS_ENDPOINT" "$query"

test_name="5-wcps_encode_csv"
query='for c in (test_mr) return encode(c, "text/csv")'
handle "$test_name" "$WCPS_ENDPOINT" "$query"


# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC

