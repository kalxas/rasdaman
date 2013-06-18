#!/bin/bash
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#  test.sh
# Description
#  Command-line utility for testing WCS functionality in petascope.
#
# PRECONDITIONS
#   Postgres, Rasdaman, Tomcat installed and running
#   Petascope deployed
#
# Usage: ./test.sh
#
# CHANGE HISTORY
#       2012-Nov-12     DM         created
#

PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"


. "$SCRIPT_DIR"/../../util/petascope.sh

#
# constants
#
WCPS_DIR="$SCRIPT_DIR"/../test_wcps
TESTDATA_PATH="$WCPS_DIR/testdata"
[ -d "$TESTDATA_PATH" ] || error "Testdata directory not found: $TESTDATA_PATH"
QUERIES_PATH="$SCRIPT_DIR/queries"
[ -d "$QUERIES_PATH" ] || error "Queries directory not found: $QUERIES_PATH"

# query file sufixes
KVP_QUERY_SUFFIX=".kvp"
XML_QUERY_SUFFIX=".xml"

# dir holding query outputs
OUTPUT_PATH="$SCRIPT_DIR/output"
mkdir -p "$OUTPUT_PATH"

#
# variables
#
failed=0
total=0

#
# cleanup stuff
#
function cleanup()
{
  loge "--------------------------------------------------------"
  loge
  if [ $total -ne 0 ]; then
    log "  PASSED:" $(($total - $failed))
    log "  FAILED: $failed"
    loge "          ------------"
    log "   TOTAL: $total"
    loge ""
  fi

  log "cleanup..."
  #raserase_colls
  [ $failed -eq 0 ]
  exit
}

# trap keyboard interrupt (control-c)
trap cleanup SIGINT


# ------------------------------------------------------------------------------
# work
# ------------------------------------------------------------------------------

#
# checks
#
check_petascope
check_postgres
check_rasdaman
check_wget

# run import if necessary
import_data

mkdir -p "$OUTPUT_PATH"

#
# run KVP queries
#
pushd "$QUERIES_PATH" > /dev/null

for f in *$KVP_QUERY_SUFFIX; do

  [ -f "$f" ] || continue
  
  # test single file
  #[ "$f" == "48-slice_scale.test" ] || continue

  # test header
  loge ""
  loge "--------------------------------------------------------"
  log "running test: $f"
  loge
  
  # URL encode query
  #f_enc=`cat $f | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g'`
  f_enc=`cat $f | tr -d '\n'`
  
  # prepend petascope WCS endpoint (defined in test.cfg)
  kvp_req="$WCS_URL?$f_enc"
  
  echo $kvp_req
  loge

  # send to petascope
  f_out="$OUTPUT_PATH/$f.out"
  time $WGET -q $kvp_req -O "$f_out"

  loge
  egrep -i "(error|exception)" "$f_out" > /dev/null
  if [ $? -eq 0 ]; then
    failed=$(($failed + 1))
    log " ->  QUERY FAILED"
  else
    log " ->  QUERY PASSED"
  fi
  
  total=$(($total + 1))

done

popd > /dev/null

cleanup
