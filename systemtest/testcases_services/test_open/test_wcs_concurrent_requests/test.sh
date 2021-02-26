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
#    Check if SECORE can handle multiple requests in short time
#
################################################################################
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

# NOTE: script_dir is folder of children test cases and it needs to be specified in path variables
readonly ORACLE_DIR="$SCRIPT_DIR/oracle"
readonly ORACLE_FILE="$ORACLE_DIR/valid_response.oracle"
[ -f "$ORACLE_FILE" ] || error "$ORACLE_FILE not found"

readonly OUTPUT_DIR="$SCRIPT_DIR/output"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

readonly WCS_REQUEST="$PETASCOPE_URL?service=WCS&Request=GetCoverage&version=2.0.1&subset=Lat(-30,-10.5)&subset=Long(120,150)&CoverageId=test_mean_summer_airtemp&format=image/jpeg"

readonly rasserver_count=$(ps aux | grep rasserver | grep -v grep | wc -l)
readonly REQUEST_NO=$(($rasserver_count * 2))

log "test petascope WCS with $REQUEST_NO concurrent requests..."
pids=""
for i in $(seq $REQUEST_NO); do
   out="$OUTPUT_DIR/wcs_request_${i}.out"
   $WGET -q "$WCS_REQUEST" -O "$out" &
   pids="$pids $!"
done

log "waiting for requests to finish..."
wait $pids
log "all requests finished."

ORACLE_FILE_OUT="$OUTPUT_DIR/valid_response.oracle"
cp "$ORACLE_FILE" "$ORACLE_FILE_OUT"
prepare_gdal_file "$ORACLE_FILE_OUT"

for i in $(seq $REQUEST_NO); do
   out="$OUTPUT_DIR/wcs_request_${i}.out"
   NUM_TOTAL=$(($NUM_TOTAL+1))

   # skip non-existing files
   if [ ! -f "$out" ]; then
     log_colored_failed "output file $out not found."
     NUM_FAIL=$(($NUM_FAIL + 1))
     continue
   fi
   [ -s "$out" ] || continue # skip empty files

   prepare_gdal_file "$out"
   cmp "$out" "$ORACLE_FILE_OUT" > /dev/null 2>&1
   [ $? -eq 0 ] && check_passed quiet
   NUM_TOTAL=$(($NUM_TOTAL-1)) # it was added again in check_passed
done

[ $NUM_SUC -gt 0 ]
check_result 0 $? "at least one request returned valid result"

# print summary from util/common.sh
print_summary
exit $RC
