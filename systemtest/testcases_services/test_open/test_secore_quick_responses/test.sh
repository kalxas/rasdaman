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

OUTPUT_FOLDER="output"
rm -rf "$OUTPUT_FOLDER"
mkdir -p "$OUTPUT_FOLDER"

N=20
log "Test SECORE response time with $N concurrent queries..."
for i in $(seq 20); do
   log "  sending request $i..."
   wget -q "$SECORE_URL" -O "$OUTPUT_FOLDER/secore$i.txt" &
done

log "Waiting for all requests to finish..."
wait

result=true
log "All queries finished, checking results."
for f in "$OUTPUT_FOLDER"/*; do
   if ! grep ellipsoid "$f" -q; then
      log "  response $f invalid:"
      cat "$f"
      echo
      result=false
   fi
done

# check the test result
check_result "true" "$result" "SECORE responses as expected"

# print summary from util/common.sh
print_summary
exit $RC
