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
#	test.sh
# Description
#	Command-line utility for testing rasdaman.
#	1) Send rasql query
#	2) Get response
#	3) Compare the response with the expected result
#	4) Give out the testing result.
#
# PRECONDITIONS
# 	Postgres, Rasdaman installed
#
# Usage: ./test.sh
#         images needed for testing shall be put in directory of testdata
# Parameters:
#
#
# CHANGE HISTORY
#       2013-Aug-03     Dimitar Misev       created
#

PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

TRAC_URL="http://rasdaman.org/ticket"

#
# paths
#
TESTDATA_PATH="$SCRIPT_DIR/testdata"
[ -d "$TESTDATA_PATH" ] || error "Testdata directory not found: $TESTDATA_PATH"
RASQL_TESTDATA_PATH="$SCRIPT_DIR/../../testcases_mandatory/test_select/testdata"
[ -d "$RASQL_TESTDATA_PATH" ] || error "Testdata directory not found: $RASQL_TESTDATA_PATH"
PETASCOPE_TESTDATA_PATH="$SCRIPT_DIR/../../testcases_services/test_wcps/testdata"
[ -d "$PETASCOPE_TESTDATA_PATH" ] || error "Testdata directory not found: $PETASCOPE_TESTDATA_PATH"
QUERIES_PATH="$SCRIPT_DIR/queries"
[ -d "$QUERIES_PATH" ] || error "Queries directory not found: $QUERIES_PATH"
ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Oracles directory not found: $ORACLE_PATH"
OUTPUT_PATH="$SCRIPT_DIR/output"
mkdir -p "$OUTPUT_PATH"
rm -f "$OUTPUT_PATH"/*


#
# cleanup stuff
#
function cleanup()
{
  drop_colls $TEST_GREY $TEST_GREY2 $TEST_RGB2
  drop_petascope_data

  loge "--------------------------------------------------------"
  loge
  if [ $NUM_TOTAL -ne 0 ]; then
    print_summary
  fi
  # remove the temporary file used in the oracle verification output dir
  rm -f "$OUTPUT_PATH/temporary"

  # remove the temporary file used in the oracle verification oracle dir
  rm -f "$ORACLE_PATH/temporary"

  if [ $NUM_TOTAL -ne 0 ]; then
    exit $RC
  else
    exit $RC_OK
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
import_petascope_data "$PETASCOPE_TESTDATA_PATH"
import_rasql_data "$RASQL_TESTDATA_PATH"
echo

pushd "$QUERIES_PATH" > /dev/null

for f in *; do
  
  # skip non-files
  [ -f "$f" ] || continue
  
  # skip scripts, we only want queries
  [[ "$f" == *.pre.sh || "$f" == *.post.sh || "$f" == *.check.sh ]] && continue
  
  # uncomment below for single query test
  #[[ "$f" == 57-* ]] || continue
  
  # check if rasdaman is running
  $RASQL -q 'select c from RAS_COLLECTIONNAMES as c' --out string > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    log "rasdaman down, exiting..."
    cleanup
  fi
  
  # determine test type based on extension
  test_type=`echo "$f" | sed 's/.*\.//'`
  if [ "$test_type" == "fixed" ]; then
    fixedf=`echo "$f" | sed 's/.fixed$//'`
    test_type=`echo "$fixedf" | sed 's/.*\.//'`
    FIXED=1
  fi
  if [ "$test_type" == "kvp" -o "$test_type" == "xml" ]; then
    SVC_NAME="wcs"
  else
    SVC_NAME="$test_type"
  fi
  
  # get ticket number
  ticket_no=`echo "$f" | awk -F '_' '{ print $1; }' | awk -F '-' '{ print $2; }'`
  ticket_url="$TRAC_URL/$ticket_no"
  
  
  # print test header
  echo "running $test_type test: $f"
  echo "ticket URL: $ticket_url"
  echo
  cat "$f"
  echo
  
  run_test
  
done

popd > /dev/null

# ------------------------------------------------------------------------------
# test summary
#
cleanup
