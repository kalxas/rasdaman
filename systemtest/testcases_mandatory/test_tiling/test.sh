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

set -o nounset

PROG=$(basename $0)

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Expected results directory not found: $ORACLE_PATH"
QUERY_PATH="$SCRIPT_DIR/queries"
[ -d "$QUERY_PATH" ] || error "Rasql query dir not found: $QUERY_PATH"
OUTPUT_PATH="$SCRIPT_DIR/output"
mkdir -p "$OUTPUT_PATH"

QUERY="" # query
Q_ID=""  # query identifier (file name)

TEST_COLL=test_coll
TEST_COLL3=test_coll3

# ------------------------------------------------------------------------------
# test dependencies
#
check_rasdaman

# check data types
check_type GreySet
check_type GreySet3


# ------------------------------------------------------------------------------
# test function
#
# arg 1: query id
# arg 2: only specify for dbinfo test
function run_test()
{
  local q_id="$1"
  local f=tmp.txt
  if [ $# -gt 1 ]; then
    f=tmp.unknown
  fi
  
  if [ ! -f $f ]; then
    log "Failed executing select query."
    NUM_FAIL=$(($NUM_FAIL + 1))

    log_failed "----------------------------------------------------------------------"
    log_failed "$q_id"
    log_failed "$QUERY"    
    return
  fi
  if [ $# -gt 1 ]; then
    sed -i '/oid/d' $f
    sed -i '/baseType/d' $f
  fi
  mv $f $q_id
  if [ ! -f "$ORACLE_PATH/$q_id" ]; then
    cp "$q_id" "$ORACLE_PATH/$q_id"
  fi

  # Compare the result byte by byte with the expected result in oracle folder
  local out_file="$OUTPUT_PATH/$q_id"
  local ora_file="$ORACLE_PATH/$q_id"
  mv $q_id "$out_file"
  if [ ! -f "$ora_file" ]; then
    log "  warning: oracle not found - $ora_file; output will be copied."
    cp "$out_file" "$ora_file"
  fi
  cmp "$ora_file" "$out_file"
  if [ $? != 0 ]; then
    log "Result of query contains error."
    NUM_FAIL=$(($NUM_FAIL + 1))
    log_failed "----------------------------------------------------------------------"
    log_failed "$q_id"
    log_failed "$QUERY"  
  else
    log "Result of query is correct."
    NUM_SUC=$(($NUM_SUC + 1))
  fi
}


# ------------------------------------------------------------------------------
# test by queries
#
      
rm -f tmp.unknown tmp.csv $FAILED_LOG_FILE
# Query by query for extracting some aspects of tested data
for i in $QUERY_PATH/*.rasql; do

  # Send query in query folder.
  Q_ID=$(basename $i)

  log "----------------------------------------------------------------------"
  log ""
  log "running queries in $Q_ID"
  log ""

  # initialize collections
  drop_colls $TEST_COLL $TEST_COLL3
  create_coll $TEST_COLL GreySet
  create_coll $TEST_COLL3 GreySet3

  counter=0
  while read QUERY; do
    q_id=$Q_ID.$counter

    # first run insert/update query
    log "----------------------------------------------------------------------"
    log "$q_id:"
    log "  $QUERY"

    $RASQL -q "$QUERY" --quiet
    if [ $? -ne 0 ]; then
      log "Failed executing update query."
      NUM_FAIL=$(($NUM_FAIL + 1))
      log_failed "----------------------------------------------------------------------"
      log_failed "$q_id"
      log_failed "$QUERY"  
      continue
    fi

    coll_name="$TEST_COLL"
    echo "$QUERY" | grep "$TEST_COLL3"
    if [ $? -eq 0 ]; then
      coll_name="$TEST_COLL3"
    fi

    # test result contents
    $RASQL -q "select avg_cells(c) from $coll_name as c" --out file --outfile tmp > /dev/null
    run_test "$q_id.txt"

    # test dbinfo for tile structure
    $RASQL -q "select dbinfo(c, \"printtiles=1\") from $coll_name as c" --out file --outfile tmp > /dev/null
    run_test "$q_id" "dbinfo"

    counter=$(($counter+1))
    QUERY=""
  done < "$i"


done

drop_colls $TEST_COLL $TEST_COLL3


# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
