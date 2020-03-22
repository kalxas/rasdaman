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
#  Designed as a generic engine, reusable for all W*S test suites.
#
# PRECONDITIONS
#   Postgres, Rasdaman installed and running
#
# Usage: ./test_wcps.sh
#
# CHANGE HISTORY
#       2012-Jun-14     DM         created
#       2013-May-26     NK         added oracle verification
#       2013-Jul-23     AB         re-use fixes (in test_wms definition)
#       2013-Aug-06     DM         refactor functionality into common.sh
#       2013-Dec-03     DM         known_fails file listing queries that are known to fail
#

PROG=$(basename $0)

# get dir of linking script
SOURCE="${BASH_SOURCE[0]}"
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

SYSTEST_DIR=$(echo "$SCRIPT_DIR" | sed 's|\(.*/systemtest\).*|\1|')
[ -d "$SYSTEST_DIR/util" ] || error "could not determine system test dir: $SYSTEST_DIR"

. "$SYSTEST_DIR/util/common.sh"

# determine actual service
SVC_NAME=$(basename $SCRIPT_DIR | cut -d "_" -f 2)
log "Testing service: $SVC_NAME"

#
# constants
#
TESTDATA_PATH="$SCRIPT_DIR/testdata"
QUERIES_PATH="$SCRIPT_DIR/queries"
[ -d "$QUERIES_PATH" ] || error "Queries directory not found: $QUERIES_PATH"
ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Oracles directory not found: $ORACLE_PATH"
OUTPUT_PATH="$SCRIPT_DIR/output"

KNOWN_FAILS="$SCRIPT_DIR/known_fails"

#
# indicates whether to drop data before/after running tests: 0 = no, 1 = yes
# --drop turns this option on
#
DROP_DATA=0

#
# indicates whether to ingest data before running tests: 0 = no, 1 = yes
# --no-ingest turns this option off
#
INGEST_DATA=1

#
# constants for GDAL version checks
JP2_MIME='image/jp2'
MULTIPART_MIME='multipart/related'
gmljp2_enabled=$( check_gdal_version 1 10 ) # GDAL >= 1.10

# Drop coverage imported by wcst_import.sh to Petascope
drop_petascope_data() {
  "$SYSTEST_DIR/testcases_services/test_zero_cleanup/test.sh"
}

drop_data()
{
  [ $DROP_DATA -eq 0 ] && return
  [ "$SVC_NAME" == "secore" -o "$SVC_NAME" == "nullvalues" ] || drop_colls $TEST_GREY $TEST_GREY2 $TEST_RGB2 $TEST_GREY3D $TEST_GREY4D $TEST_COMPLEX $TEST_CFLOAT32 $TEST_CFLOAT64 $TEST_CINT16 $TEST_CINT32 $TEST_STRUCT
  [ "$SVC_NAME" == "clipping" -o "$SVC_NAME" == "secore" -o "$SVC_NAME" == "select" -o "$SVC_NAME" == "nullvalues" ] || drop_petascope_data
  [ "$SVC_NAME" == "nullvalues" ] && drop_nullvalues_data
  [ "$SVC_NAME" == "subsetting" ] && drop_colls $TEST_SUBSETTING_1D $TEST_SUBSETTING $TEST_SUBSETTING_SINGLE $TEST_SUBSETTING_3D
}

ingest_data()
{
  [ $INGEST_DATA -eq 0 ] && return
  [ "$SVC_NAME" == "select" -o "$SVC_NAME" == "clipping" ] && import_rasql_data "$TESTDATA_PATH"
  [ "$SVC_NAME" == "rasdapy" ] && py_import_rasql_data "$TESTDATA_PATH"
  [ "$SVC_NAME" == "rasdapy3" ] && py_import_rasql_data "$TESTDATA_PATH"
  [ "$SVC_NAME" == "nullvalues" ] && import_nullvalues_data "$TESTDATA_PATH"
  [ "$SVC_NAME" == "subsetting" ] && import_subsetting_data "$TESTDATA_PATH"
  if [ -e "$TESTDATA_PATH/complex.binary" ] ; then
    if [ "$SVC_NAME" == "select" -o "$SVC_NAME" == "nullvalues" ]; then
      check_type Gauss2Set
      drop_colls $TEST_COMPLEX
      create_coll $TEST_COMPLEX Gauss2Set
      insert_into $TEST_COMPLEX "$TESTDATA_PATH/complex.binary" "" "" "--mddtype Gauss2Image --mdddomain [0:7,0:7]"
    fi
  fi
}

#
# cleanup stuff
#
cleanup()
{
  drop_data
  print_summary
  exit_script
}

# trap keyboard interrupt (control-c)
trap cleanup SIGINT


# ------------------------------------------------------------------------------
# work
# ------------------------------------------------------------------------------

#
# checks
#
if [ "$SVC_NAME" = "rasdapy" -a "$OS_VERSION" = "$OS_UBUNTU1604" ]; then
  # rasdapy runs with 10s per query on Ubuntu 16.04 for an unknown reason
  exit $RC_SKIP
fi
if [ "$SVC_NAME" = "rasdapy3" ]; then
  # rasql.py doesn't work well on these OS (python3 is not selected properly)
  [ "$OS_NAME" = "ubuntu" ] && [ "$OS_VERSION_NUMBER" -lt 1804 ] && exit $RC_SKIP
  [ "$OS_NAME" = "centos" ] && [ "$OS_VERSION_NUMBER" -lt 8 ] && exit $RC_SKIP
fi

check_curl
check_gdal

echo "$SCRIPT_DIR" | grep "test_secore" > /dev/null
if [ $? -eq 0 ]; then
  check_secore || exit $RC_SKIP
fi
echo "$SCRIPT_DIR" | grep "testcases_services" > /dev/null
if [ $? -eq 0 ]; then
  if [ "$SVC_NAME" != "secore" ]; then
    check_petascope || exit $RC_SKIP
  fi
fi
[ "$SVC_NAME" != "secore" ] && check_rasdaman && check_rasdaman_available

#
# check options
#

usage() {
  cat <<EOF
Usage: ./test.sh [ OPTION... ]

Supported options:

  <queryfile>   test only the given query file name (e.g. "project.rasql")

  --drop        drop the testing data after the test finishes
  --no-ingest   do not ingest testing data before running the test

  -h, --help    show this message
EOF
  exit 2
}

test_single_file=
for i in $*; do
  case $i in
    --help|-h)   usage;;
    --drop)      DROP_DATA=1;;
    --no-ingest) INGEST_DATA=0;;
    *)           test_single_file=$i;;
  esac
done


if [ -n "$test_single_file" ]; then
  [ -f "$QUERIES_PATH/$test_single_file" ] || error "$test_single_file not found."
else
  rm -rf "$OUTPUT_PATH"
  mkdir -p "$OUTPUT_PATH"
fi

start_timer

# run import if necessary
drop_data
ingest_data

stop_timer

loge

log "$(printf '%4s %5ss   data preparation' '' $(get_time_s))"

pushd "$QUERIES_PATH" > /dev/null

loge

total_test_no=$(ls | grep -E -v '\.(pre|post|check)\.sh$' | grep -E -v '\.(template|file)$' | wc -l)
curr_test_no=0

for f in *; do

  # uncomment for single test run
  if [ -n "$test_single_file" ]; then
    [[ "$f" == "$test_single_file" ]] || continue
  fi

  # skip non-files
  [ -f "$f" ] || continue
  # skip scripts, we only want queries
  [[ "$f" == *.pre.sh || "$f" == *.post.sh || "$f" == *.check.sh ]] && continue
  [[ "$f" == *.template || "$f" == *.file ]] && continue

  curr_test_no=$(($curr_test_no + 1))

  if [ "$SVC_NAME" == "wcs" ]; then
    # Skip multipoint tests
    [[ "$f" == *multipoint* ]] && continue
    # Skip GMLJP2 tests if GDAL version is not >= 1.10 (format mime jp2 + multipart -- @see #745)
    if grep -q "$JP2_MIME" "$f"; then
      if grep -q "$MULTIPART_MIME" "$f"; then
        [[ "$gmljp2_enabled" -ne 0 ]] && continue
      fi
    fi
  fi

  start_timer

  run_test "$f"

  stop_timer

  # print result of this test case
  print_testcase_result "$f" "$status" "$total_test_no" "$curr_test_no"

done

popd > /dev/null

cleanup
