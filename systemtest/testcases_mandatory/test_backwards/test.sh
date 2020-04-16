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
#	1) copy the backup RASBASE from the backup folder
#	2) test arbitrary selection on the collections to ensure they exist and can be accessed
#	3) test updating data on the collections to ensure they can be updated properly in subdomains
#	4) test updating data on the collections to ensure their domains can be extended, with proper tilings and proper data setting.
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
#       2018-Jun-23     B.Bell     copied from test_tiling, changed to use directql and reference an old RASBASE 
#
#
# Parameters definistion and initiation

set -o nounset

PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

KNOWN_FAILS="$SCRIPT_DIR/known_fails"

OLDDBDIR="$UTIL_SCRIPT_DIR/../testcases_mandatory/test_backwards/old_databases"

RASDB="$OLDDBDIR/version_9.6/RASBASE"

RASDATA="$OLDDBDIR/version_9.6"

DIRECTQL="directql --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD \
                 --database $RASDB"
#
# paths
#
ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Expected results directory not found: $ORACLE_PATH"
QUERY_PATH="$SCRIPT_DIR/queries"
[ -d "$QUERY_PATH" ] || error "Rasql query dir not found: $QUERY_PATH"
OUTPUT_PATH="$SCRIPT_DIR/output"
mkdir -p "$OUTPUT_PATH"

QUERY="" # query
Q_ID=""  # query identifier (file name)

TEST_REGULAR=test_regular
TEST_ALIGNED=test_aligned

# ------------------------------------------------------------------------------
# check database
if [ "$BASEDB" = "pgsql" ]; then
  # test backwords only works with sqlite
  echo "Test Backwards only runs with SQLite selected as database."
  exit $RC_SKIP
  
fi
# test dependencies
#
check_postgres
check_rasdaman

# check data types
check_type GreySet

#
# function for resetting the "old" database to the expected initial state
function reset_db()
{
  rm -r $OLDDBDIR/version_9.6/
  cp -r $OLDDBDIR/version_9.6.backup/ $OLDDBDIR/version_9.6/
}

# ------------------------------------------------------------------------------
# test function
#
# arg 1: query id
# arg 2: only specify for dbinfo test
function run_test()
{
  local q_id="$1"
  local f=""

  if [ $2 = "dbinfo" ]; then
    f="tmp.unknown"
  fi

  if [ $2 = "select" ]; then
    f="tmp.json"
  fi

  grep "$q_id" "$KNOWN_FAILS" &> /dev/null
  local known_fail=$?

# in case the select query fails
  if [ ! -e "$f" ]; then
    if [ $known_fail -ne 0 ]; then
      log "Failed executing select query."
      NUM_FAIL=$(($NUM_FAIL + 1))
    else
      log "Failed executing select query. Case is a known fail: skipping test."
    fi
    NUM_TOTAL=$(($NUM_TOTAL + 1))
    log_failed "----------------------------------------------------------------------"
    log_failed "$q_id"
    return
  fi

  if [ $# -gt 1 ]; then
    sed -i '/oid/d' $f
    sed -i '/baseType/d' $f
  fi
  mv $f $q_id
  if [ ! -f "$ORACLE_PATH/$q_id" ]; then
    cp "$q_id" "$ORACLE_PATH/$q_id" > /dev/null
  fi

  # Compare the result byte by byte with the expected result in oracle folder
  mv $q_id "$OUTPUT_PATH"
  cmp $ORACLE_PATH/$q_id "$OUTPUT_PATH/$q_id" > /dev/null
  local rc=$?

  if [ $known_fail -ne 0 ]; then
    if [ $rc -ne 0 ]; then
      #Test failed,  not skipped
      NUM_FAIL=$(($NUM_FAIL + 1))
      log "TEST FAILED: $q_id"
      log_failed "TEST FAILED: $q_id"
      log_failed "    Result of query contains error." 
    else
      #Test successful, not skipped.
      log "TEST PASSED: $q_id"
      NUM_SUC=$(($NUM_SUC + 1))
    fi
  else
    #we still check if it is fixed
    if [ $rc -ne 0 ]; then
      #Test failed, but is a known fail
      log_failed "TEST SKIPPED: $q_id"
      log_failed "    Result of query contains error."
    else
      #Test passed, but is a known fail
      log "KNOWN FAIL PASSED: $q_id"
    fi
  fi
  #Regardless, we add to the total # tests run
  NUM_TOTAL=$(($NUM_TOTAL + 1))
}

function try_update
{
  local QUERY="$1"
  local q_id="$2.update"
  grep "$q_id" "$KNOWN_FAILS" &> /dev/null
  local known_fail=$?

  $DIRECTQL -q "$QUERY" --quiet > /dev/null
  local rc=$?
  if [ $known_fail -ne 0 ]; then
    if [ $rc -ne 0 ]; then
      log_failed "Failed executing update query: $q_id"
      log_failed "    Update query throws an error."
      NUM_FAIL=$(($NUM_FAIL + 1))
    else
      log "Update query successful."
      NUM_SUC=$(($NUM_SUC + 1))
    fi
  else
    if [ $rc -ne 0 ]; then
      log_failed "TEST SKIPPED: $q_id"
      log_failed "    Update query throws an error."
    else
      log "KNOWN FAIL PASSED: $q_id"
      NUM_SUC=$(($NUM_SUC + 1))
    fi
  fi

  NUM_TOTAL=$(($NUM_TOTAL + 1))
  return $rc
}

# ------------------------------------------------------------------------------
# backup db state
#
cp -r $OLDDBDIR/version_9.6/ $OLDDBDIR/version_9.6.backup/

# ------------------------------------------------------------------------------
# test by queries
#
  	
rm -f tmp.unknown tmp.csv $FAILED_LOG_FILE
# Query by query for extracting some aspects of tested data
for i in $QUERY_PATH/*.directql; do

  # Send query in query folder.
  Q_ID=`basename $i`

  log "----------------------------------------------------------------------"
  log ""
  log "running queries in $Q_ID"
  log ""

  # overwrite current database with the backup, each time the test is run!
  log "----------------------------------------------------------------------"
  log ""
  log "resetting the old database to initial conditions..."
  reset_db
  log "...done."
  log ""

  counter=0
  while read QUERY; do
    q_id=$Q_ID.$counter

    # first run an update query on the old database
    log "----------------------------------------------------------------------"
    log "$q_id:"

    try_update "$QUERY" "$q_id"
    if [ $? -ne 0 ]; then
      log "Skipping the select and dbinfo tests for $q_id."
      NUM_TOTAL=$(($NUM_TOTAL + 2))  
      counter=$(($counter+1))
      QUERY=""    
      continue
    fi
    #if it is not TEST_REGULAR, then it is TEST_ALIGNED
    coll_name="$TEST_REGULAR"
    echo "$QUERY" | grep "$TEST_ALIGNED" > /dev/null
    if [ $? -eq 0 ]; then
      coll_name="$TEST_ALIGNED"
    fi

    # test result contents
    $DIRECTQL -q "select encode(c, \"json\") from $coll_name as c" --out file --outfile tmp > /dev/null
    run_test "$q_id.json" "select"

    # test dbinfo for tile structure
    $DIRECTQL -q "select dbinfo(c, \"printtiles=1\") from $coll_name as c" --out file --outfile tmp > /dev/null
    run_test "$q_id" "dbinfo"

    counter=$(($counter+1))
    QUERY=""
  done < "$i"

#cleanup -- comment out to investigate errors!
  reset_db

done

#reset current db, drop backup db, and reset environment variables
reset_db
rm -r -f $OLDDBDIR/version_9.6.backup/

# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
