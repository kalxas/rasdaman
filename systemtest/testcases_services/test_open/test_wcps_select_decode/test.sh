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

readonly ENDPOINT="$PETASCOPE_URL?SERVICE=WCS&VERSION=2.0.1&REQUEST=ProcessCoverages"
readonly OAPI_ENDPOINT="$PETASCOPE_OAPI"

handle_output() {
  # $1 test case name
  local test_name="$1"
  local oracle_file="$ORACLE/$test_name"
  local output_file="$OUTPUT/$test_name"

  logn "Checking test case '$test_name'..."

  if [ ! -f "$oracle_file" ]; then
    cp "$output_file" "$oracle_file"
  else
    compare_output_to_oracle "$output_file" "$oracle_file" 
    if [ "$?" == "0" ]; then
        loge "ok"
    else
        loge "failed"
    fi
  fi
}

# Clean the output directory if exists
rm -rf "$OUTPUT"
mkdir -p "$OUTPUT"

# Add more test files here
readonly TEST_2D_TIFF_3BANDS_FILE_PATH="$SCRIPT_DIR/../../test_all_wcst_import/testdata/001-3D_Timeseries_Regular/SCALED_M_LSTDA_2015-01.TIFF"
readonly TEST_2D_TIFF_1BAND_FILE_PATH="$SCRIPT_DIR/../../test_all_wcst_import/testdata/075-wcps_mean_summer_air_temp/mean_summer_airtemp.tif"


TEST_NAME="1-test_encode"
$CURL "$ENDPOINT" \
     -F 'query=for c in (decode($1)) return encode(c, "tiff")' \
     -F "1=@$TEST_2D_TIFF_3BANDS_FILE_PATH" > "$OUTPUT/$TEST_NAME"
handle_output "$TEST_NAME"

TEST_NAME="2-test_binary_operator"
$CURL "$ENDPOINT" \
     -F 'query=for c in (decode($1)) return encode((unsigned char)(c + 15*1.5 - c * 0.5) , "tiff")' \
     -F "1=@$TEST_2D_TIFF_3BANDS_FILE_PATH" > "$OUTPUT/$TEST_NAME"
handle_output "$TEST_NAME"

TEST_NAME="3-test_band_extraction"
$CURL "$ENDPOINT" \
     -F 'query=for c in (decode($1)) return encode(c.band1 , "tiff")' \
     -F "1=@$TEST_2D_TIFF_3BANDS_FILE_PATH" > "$OUTPUT/$TEST_NAME"
handle_output "$TEST_NAME"

TEST_NAME="4-test_band_combination"
$CURL "$ENDPOINT" \
     -F 'query=for c in (decode($1)) return encode({red: c.band2; green: c.band1; blue: c.band3} , "tiff")' \
     -F "1=@$TEST_2D_TIFF_3BANDS_FILE_PATH" > "$OUTPUT/$TEST_NAME"
handle_output "$TEST_NAME"

TEST_NAME="5-two_positional_file_parameters"
$CURL "$ENDPOINT" \
     -F 'query=for $c in (decode($1)), $d in (decode($1)) return encode((unsigned char)($c + $d - $c * 1.5) , "tiff")' \
     -F "1=@$TEST_2D_TIFF_3BANDS_FILE_PATH" > "$OUTPUT/$TEST_NAME" \
     -F "2=@$TEST_2D_TIFF_3BANDS_FILE_PATH" > "$OUTPUT/$TEST_NAME"
handle_output "$TEST_NAME"

TEST_NAME="6-subset_avg_cells"
$CURL "$ENDPOINT" \
     -F 'query=for $c in (decode($1)), $d in (decode($1)) return avg(($c + $d)[Lat(0:90), Long(-180:180)])' \
     -F "1=@$TEST_2D_TIFF_3BANDS_FILE_PATH" > "$OUTPUT/$TEST_NAME"
handle_output "$TEST_NAME"

TEST_NAME="7-positional_parameter_not_in_return_expression"
$CURL "$ENDPOINT" \
     -F 'query=for $c in (decode($1)), $d in (decode($2)), $e in (decode($3)) return 1 + 2' \
     -F "1=@$TEST_2D_TIFF_3BANDS_FILE_PATH" \
     -F "2=@$TEST_2D_TIFF_3BANDS_FILE_PATH" \
     -F "3=@$TEST_2D_TIFF_3BANDS_FILE_PATH"  > "$OUTPUT/$TEST_NAME"
handle_output "$TEST_NAME"

TEST_NAME="8-test_oapi_wcps_decode"
$CURL "$ENDPOINT" \
     -F 'query=for $c in (decode($1)), $d in test_mean_summer_airtemp return encode($c - 50 + $d - 100, "jpeg")' \
     -F "1=@$TEST_2D_TIFF_1BAND_FILE_PATH" > "$OUTPUT/$TEST_NAME"

handle_output "$TEST_NAME"


# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC

