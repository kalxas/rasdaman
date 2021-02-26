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

#
# test functions
#

run_test() {
    $WGET --content-on-error --trust-server-names --content-disposition \
         "$endpoint" > "$OUTPUT_DIR/$f.log" 2>&1
    [ -f "$f" ]
    check_result 0 $? "  check if $f exists"
}
test_wcs() {
    endpoint="$PETASCOPE_URL?service=WCS&version=2.0.1&request=GetCoverage&coverageId=test_mr&subset=i(0,20)&subset=j(0,5)&format=$1"
    f="test_mr.$2"
    run_test
}
test_wcps() {
    endpoint="$PETASCOPE_URL?service=WCS&version=2.0.1&request=ProcessCoverages&query=for c in (test_rgb) return encode(c, \"$1\")"
    f="test_rgb.$2"
    run_test
}
test_multipart() {
    endpoint="$PETASCOPE_URL?service=WCS&version=2.0.1&request=GetCoverage&coverageId=test_mean_summer_airtemp&format=$1&mediaType=multipart/related"
    f="test_mean_summer_airtemp.$2"
    run_test
}

OUTPUT_DIR="$SCRIPT_DIR/output"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

log "Testing download result with coverage ID as file name"
log ""

pushd "$OUTPUT_DIR" > /dev/null

for args in "application/netcdf netcdf nc" "image/png png png" "image/tiff tiff tiff"; do
    mime=$(echo "$args" | awk '{print $1}')
    format=$(echo "$args" | awk '{print $2}')
    suffix=$(echo "$args" | awk '{print $3}')

    log "test download output encoded in $format"
    test_wcs $mime $suffix
    test_multipart $mime $suffix
    test_wcps $format $suffix
done

popd > /dev/null

print_summary
exit $RC
