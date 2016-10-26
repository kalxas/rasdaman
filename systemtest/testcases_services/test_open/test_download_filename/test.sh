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
#    Download  result from request (WCS, WCPS) with coverage name instead of ows
#
################################################################################

# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

log "--- Testing download result with coverage ID as file name ---"
WCS_ENDPOINT_TEMPLATE=$PETASCOPE_URL'/ows?service=WCS&version=2.0.1&request=GetCoverage&coverageId=test_mr&subset=i(0,20)&subset=j(0,5)&format=$FORMAT'
WCPS_ENDPOINT_TEMPLATE=$PETASCOPE_URL'/ows?service=WCS&version=2.0.1&request=ProcessCoverages&query=for c in (test_rgb) return encode(c,"$FORMAT")'

# with multipart
WCS_ENDPOINT_MULTIPART_TEMPLATE=$PETASCOPE_URL'/ows?service=WCS&version=2.0.1&request=GetCoverage&coverageId=test_mr&subset=i(0,20)&subset=j(0,5)&format=$FORMAT&mediaType=multipart/related'
# WCPS does not have this feature, but the output should have same file name

OUTPUT_DIR=$SCRIPT_DIR"/"output

# function to download coverage to output
function downloadAndCheck() {
    wget -q --trust-server-names --content-disposition "$1" -P  $OUTPUT_DIR
    wget -q --trust-server-names --content-disposition "$2" -P  $OUTPUT_DIR
    # e.g: png
    TYPE=$3

    RESULT=0

    # test_mr
    if [ -f "$OUTPUT_DIR/test_mr."$TYPE ]; then
        log "PASS: test_mr."$TYPE" exist"
    else
        log "FAIL: test_mr."$TYPE" does not exist"
        RESULT=1
    fi

    # test_rgb
    if [ -f "$OUTPUT_DIR/test_rgb."$TYPE ]; then
        log "PASS: test_rgb."$TYPE" exist"
    else
        log "FAIL: test_rgb."$TYPE" does not exist"
        RESULT=1
    fi

    if [ $RESULT == 0 ]; then
        NUM_SUC=$(($NUM_SUC + 1))
    else
        NUM_FAIL=$(($NUM_FAIL + 1))
    fi
}


# 1. PNG
log "+ Test download encoding PNG..."
WCS_ENDPOINT=$(echo $WCS_ENDPOINT_TEMPLATE | sed 's/$FORMAT/image\/png/g')
WCPS_ENDPOINT=$(echo $WCPS_ENDPOINT_TEMPLATE | sed 's/$FORMAT/png/g')
downloadAndCheck "$WCS_ENDPOINT" "$WCPS_ENDPOINT" "png"

# 2. JPEG2000
log "+ Test download encoding JPEG2000..."
WCS_ENDPOINT=$(echo $WCS_ENDPOINT_TEMPLATE | sed 's/$FORMAT/image\/jp2/g')
WCPS_ENDPOINT=$(echo $WCPS_ENDPOINT_TEMPLATE | sed 's/$FORMAT/jpeg2000/g')
downloadAndCheck "$WCS_ENDPOINT" "$WCPS_ENDPOINT" "jp2"

# 3. TIFF
log "+ Test download encoding TIFF..."
WCS_ENDPOINT=$(echo $WCS_ENDPOINT_TEMPLATE | sed 's/$FORMAT/image\/tiff/g')
WCPS_ENDPOINT=$(echo $WCPS_ENDPOINT_TEMPLATE | sed 's/$FORMAT/tiff/g')
downloadAndCheck "$WCS_ENDPOINT" "$WCPS_ENDPOINT" "tiff"

# 4. NETCDF
log "+ Test download encoding NETCDF..."
WCS_ENDPOINT=$(echo $WCS_ENDPOINT_TEMPLATE | sed 's/$FORMAT/application\/netcdf/g')
WCPS_ENDPOINT=$(echo $WCPS_ENDPOINT_TEMPLATE | sed 's/$FORMAT/netcdf/g')
downloadAndCheck "$WCS_ENDPOINT" "$WCPS_ENDPOINT" "nc"

# 5. JPEG
log "+ Test download encoding JPEG..."
WCS_ENDPOINT=$(echo $WCS_ENDPOINT_MULTIPART_TEMPLATE | sed 's/$FORMAT/image\/jpeg/g')
WCPS_ENDPOINT=$(echo $WCPS_ENDPOINT_TEMPLATE | sed 's/$FORMAT/jpeg/g')
downloadAndCheck "$WCS_ENDPOINT" "$WCPS_ENDPOINT" "jpeg"

# print summary from util/common.sh
print_summary
exit $RC
