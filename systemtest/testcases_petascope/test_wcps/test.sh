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
#  Command-line utility for testing WCPS functionality in petascope.
#
# PRECONDITIONS
#   Postgres, Rasdaman installed and running
#
# Usage: ./test_wcps.sh
#
# CHANGE HISTORY
#       2012-Jun-14     DM         created
#       2012-May-26     NK         added oracle verification
#

PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/petascope.sh

#
# constants
#
TESTDATA_PATH="$SCRIPT_DIR/testdata"
[ -d "$TESTDATA_PATH" ] || error "Testdata directory not found: $TESTDATA_PATH"
QUERIES_PATH="$SCRIPT_DIR/queries"
[ -d "$QUERIES_PATH" ] || error "Queries directory not found: $QUERIES_PATH"
ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Oracles directory not found: $ORACLE_PATH"
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
  # remove the temporary file used in the oracle verification output dir
  rm -f "$OUTPUT_PATH/temporary"

  # remove the temporary file used in the oracle verification oracle dir
  rm -f "$ORACLE_PATH/temporary"

  [ $failed -eq 0 ]
  exit
}

function check_result()
{
  if [ $? != 0 ]; then
    failed=$(($failed + 1))
    log " ->  QUERY FAILED"
  else
    log " ->  QUERY PASSED"
  fi
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
check_gdal

# run import if necessary
import_data

mkdir -p "$OUTPUT_PATH"
pushd "$QUERIES_PATH" > /dev/null

for f in *.test; do

  [ -f "$f" ] || continue
  
  # check if rasdaman is running
  $RASQL -q 'select c from RAS_COLLECTIONNAMES as c' --out string > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    log "rasdaman down, exiting..."
    cleanup
  fi

  # test header
  echo ""
  echo "--------------------------------------------------------"
  echo "running test: $f"
  echo
  cat "$f"
  echo

  # URL encode query
  f_enc=`cat $f | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g'`

  # send to petascope
  f_out="$OUTPUT_PATH/$f.out"
  time $WGET -q --post-data "query=$f_enc" $WCPS_URL -O "$f_out"

  custom_script=${f:0:-5}".oracle.sh"
  oracle_data=${f:0:-5}".oracle.data"
  oracle_exception=${f:0:-5}".oracle.exception"

  if [ -f "$ORACLE_PATH/$custom_script" ]; then
    log "custom script"
    "$ORACLE_PATH/$custom_script" $f_out
    check_result
  else
    grep "$ORACLE_PATH/$oracle_data" "Stack trace" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
      #do exception comparison
      log "exception comparison"
      lineNo=$(grep -n "Stack trace" "$ORACLE_PATH/$oracle_data")
      lineNo=$(sed 's/[^0-9]*//g' <<< $lineNo)
      head -"$lineNo" "$f_out" > "$OUTPUT_PATH/temporary"
      head -"$lineNo" "$ORACLE_PATH/$oracle_data" > "$ORACLE_PATH/temporary"
      cmp "$ORACLE_PATH/temporary" "$OUTPUT_PATH/temporary" 2>&1
      check_result
    elif [ -f "$ORACLE_PATH/$oracle_data" ]; then 
      gdalinfo "$f_out" > /dev/null 2>&1
      if [ $? -eq 0 ]; then
        #do image comparison
        log "image comparison"
        gdal_translate -of GTiff -co "TILED=YES" "$f_out" "$OUTPUT_PATH/temporary" > /dev/null
        gdal_translate -of GTiff -co "TILED=YES" "$ORACLE_PATH/$oracle_data" "$ORACLE_PATH/temporary" > /dev/null
        cmp "$ORACLE_PATH/temporary" "$OUTPUT_PATH/temporary" 2>&1
      else
        #byte comparison
        log "byte comparison"
        cmp "$ORACLE_PATH/$oracle_data" "$f_out" 2>&1
      fi
      check_result
    else 
      log " -> NO ORACLE FOUND"
      cp $f_out "$ORACLE_PATH/$oracle_data"
      failed=$(($failed + 1))
    fi
  fi

  total=$(($total + 1))

done

popd > /dev/null

cleanup
