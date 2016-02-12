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
#       2009-Sep-16     J.Yu       created
#       2010-Apr-13     J.Yu       revise on input folder structure to support different queries input, including folders on mandatory, bug fixed, bug unfixed, and other queries.
#
# Parameters definistion and initiation

set -o nounset

PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

#
# paths
#
ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Expected results directory not found: $ORACLE_PATH"
QUERY_PATH="$SCRIPT_DIR/queries"
[ -d "$QUERY_PATH" ] || error "Rasql query dir not found: $QUERY_PATH"
OUTPUT_PATH="$SCRIPT_DIR/output"
mkdir -p "$OUTPUT_PATH"

FAILED="$SCRIPT_DIR"/failed_cases

QUERY="" # query
Q_ID=""  # query identifier (file name)

TEST_COLL=test_coll
TEST_COLL3=test_coll3

# ------------------------------------------------------------------------------
# test dependencies
#
check_postgres
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
  local f=tmp.unknown
  
  if [ ! -f $f ]; then
    log "Failed executing select query." | tee -a $LOG
    NUM_FAIL=$(($NUM_FAIL + 1))
    echo "----------------------------------------------------------------------" >> $FAILED
    echo $q_id >> $FAILED
    echo $QUERY >> $FAILED
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
  mv $q_id "$OUTPUT_PATH"
  cmp $ORACLE_PATH/$q_id "$OUTPUT_PATH/$q_id"
  if [ $? != 0 ]; then
    log "Result of query contains error."
	  NUM_FAIL=$(($NUM_FAIL + 1))
    echo "----------------------------------------------------------------------" >> $FAILED
    echo $q_id >> $FAILED
    echo $QUERY >> $FAILED
  else
    log "Result of query is correct."
    NUM_SUC=$(($NUM_SUC + 1))
  fi
}


# ------------------------------------------------------------------------------
# test by queries
#
  	
rm -f tmp.unknown tmp.csv $FAILED
# Query by query for extracting some aspects of tested data
for i in $QUERY_PATH/*.rasql; do

  # Send query in query folder.
	Q_ID=`basename $i`

  echo "----------------------------------------------------------------------" | tee -a $LOG
  echo | tee -a $LOG
  log "running queries in $Q_ID"
  echo "" | tee -a $LOG

  # initialize collections
  drop_colls $TEST_COLL $TEST_COLL3
  create_coll $TEST_COLL GreySet
  create_coll $TEST_COLL3 GreySet3

  counter=0
  while read QUERY; do
    q_id=$Q_ID.$counter

    # first run insert/update query
    echo "----------------------------------------------------------------------" | tee -a $LOG
    log "$q_id:"
    log "  $QUERY"

    $RASQL -q "$QUERY" --quiet
    if [ $? -ne 0 ]; then
      log "Failed executing update query." | tee -a $LOG
      NUM_FAIL=$(($NUM_FAIL + 1))
      echo "----------------------------------------------------------------------" >> $FAILED
      echo $q_id >> $FAILED
      echo $QUERY >> $FAILED
      continue
    fi

    coll_name="$TEST_COLL"
    echo "$QUERY" | grep "$TEST_COLL3"
    if [ $? -eq 0 ]; then
      coll_name="$TEST_COLL3"
    fi

    # test result contents
    $RASQL -q "select c from $coll_name as c" --out file --outfile tmp > /dev/null
    run_test "$q_id.bin"

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
