#!/bin/bash
#!/bin/ksh
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
PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

#
# paths
#
TESTDATA_PATH="$SCRIPT_DIR/testdata"
[ -d "$TESTDATA_PATH" ] || error "Testdata directory not found: $TESTDATA_PATH"
QUERY_PATH="$SCRIPT_DIR/test_rasql"
[ -d "$QUERY_PATH" ] || error "Rasql query dir not found: $QUERY_PATH"

FAILED="$SCRIPT_DIR"/failed_cases

QUERY="" # query
Q_ID=""  # query identifier (file name)

TEST_GREY=test_grey
TEST_GREY2=test_grey2
TEST_RGB2=test_rgb2

# ------------------------------------------------------------------------------
# test dependencies
#
check_postgres
check_rasdaman

# check data types
check_type GreySet
check_type RGBSet

# ------------------------------------------------------------------------------
# drop test collection if they already exists
#
log "test initialization..."

drop_colls $TEST_GREY $TEST_GREY2 $TEST_RGB2

create_coll $TEST_GREY GreySet
insert_into $TEST_GREY "$TESTDATA_PATH/mr_1.png" "" "decode"

create_coll $TEST_GREY2 GreySet
insert_into $TEST_GREY2 "$TESTDATA_PATH/mr2_1.png" "" "decode"

create_coll $TEST_RGB2 RGBSet
insert_into $TEST_RGB2 "$TESTDATA_PATH/rgb.png" "" "decode"


# ------------------------------------------------------------------------------
# test by queries
#

log "running tests"
echo "" | tee -a $LOG

rm -f tmp.unknown $FAILED
# Query by query for extracting some aspects of tested data
for i in $QUERY_PATH/*.rasql; do
  # Send query in query folder.
	Q_ID=`basename $i`
  echo "----------------------------------------------------------------------" | tee -a $LOG
  log " test query in $Q_ID"
  echo "" | tee -a $LOG
  QUERY=`cat $i`
	$RASQL -q "$QUERY" --quiet
  if [ $? -eq 0 ]; then 
	  echo "bug is fixed"
	  NUM_SUC=$(($NUM_SUC + 1))		
  else 
	  echo "bug is unfixed"
    NUM_FAIL=$(($NUM_FAIL + 1))
  fi
done

drop_colls $TEST_GREY $TEST_GREY2 $TEST_RGB2


# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC_OK
