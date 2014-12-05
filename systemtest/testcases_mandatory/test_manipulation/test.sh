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
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community. If not, see <http://www.gnu.org/licenses/>.
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
#	1)creating collection
#	2)insert MDD into TEST_COLLECTION
#	3)update the MDD
#	4)delete MDD
#	5)drop TEST_COLLECTION
#
# PRECONDITIONS
# 	1)Postgres Server must be running
# 	2)Rasdaman Server must be running
# 	3)database RASBASE must exists
# 	4)rasql utility must be fully running
# Usage: ./test.sh
#
# CHANGE HISTORY
#    2009-Sep-16   J.Yu    created
#    2010-July-04  J.Yu    add precondition


# Variables
PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

TEST_COLLECTION="test_tmp"
TMP_COLLECTION="test_tmp_select_into"


# ------------------------------------------------------------------------------
# test dependencies
#
check_postgres
check_rasdaman

# check data types
check_type GreySet


# ------------------------------------------------------------------------------
# drop test collection if they already exists
#
logn "test initialization..."
drop_colls $TEST_COLLECTION
feedback

# ------------------------------------------------------------------------------
# start test
#
create_coll $TEST_COLLECTION GreySet

logn "inserting MDD into collection... "
$RASQL --quiet -q "insert into $TEST_COLLECTION values marray x in [0:255, 0:210] values 1c"
if [ $? -eq 0 ]; then
	echo ok.
	NUM_SUC=$(($NUM_SUC + 1))
else
	echo failed.
	NUM_FAIL=$(($NUM_FAIL + 1))
fi

# ------------------------------------------------------------------------------

logn "updating MDD from collection... "
$RASQL --quiet -q "update $TEST_COLLECTION as a set a assign a[0:179,0:54] + 1c"
if [ $? -eq 0 ]; then
	echo ok.
	NUM_SUC=$(($NUM_SUC + 1))
else
	echo failed.
	NUM_FAIL=$(($NUM_FAIL + 1))
fi

# ------------------------------------------------------------------------------

logn "testing SELECT INTO a new collection... "
$RASQL --quiet -q "select c / 2 into $TMP_COLLECTION from $TEST_COLLECTION as c"
if [ $? -eq 0 ]; then
	sdom1=`$RASQL -q "select sdom(c) from $TMP_COLLECTION as c" --out string`
	sdom2=`$RASQL -q "select sdom(c) from $TEST_COLLECTION as c" --out string`
	if [ "$sdom1" == "$sdom2" ]; then
		echo ok.
		NUM_SUC=$(($NUM_SUC + 1))
	else
		echo failed.
		NUM_FAIL=$(($NUM_FAIL + 1))
	fi
else
	echo failed.
	NUM_FAIL=$(($NUM_FAIL + 1))
fi

# ------------------------------------------------------------------------------

# insert another object, so we test deleting all objects from one collection
$RASQL --quiet -q "select c / 2 into $TMP_COLLECTION from $TEST_COLLECTION as c"

logn "delete all MDDs from a collection... "
$RASQL --quiet -q "delete from $TMP_COLLECTION"
if [ $? -eq 0 ]; then
	sdom=`$RASQL --quiet -q "select sdom(c) from $TMP_COLLECTION as c" --out string`
	if [ -z "$sdom" ]; then
		echo ok.
		NUM_SUC=$(($NUM_SUC + 1))
	else
		echo failed.
		NUM_FAIL=$(($NUM_FAIL + 1))
	fi
else
	echo failed.
	NUM_FAIL=$(($NUM_FAIL + 1))
fi

# ------------------------------------------------------------------------------

mdd_type=NullValueArrayTest2
set_type=NullValueSetTest2

# check data types and insert if not available
TESTDATA_PATH="$SCRIPT_DIR/testdata"
check_user_type $set_type

TEST_NULL=test_null
TEST_NULL_INTO=test_null_into
drop_colls $TEST_NULL
drop_colls $TEST_NULL_INTO
create_coll $TEST_NULL $set_type

logn "testing SELECT INTO a new collection with null value transfer... "
$RASQL --quiet -q "insert into $TEST_NULL values marray x in [0:3,0:3] values (char)(x[0] + x[1] + 1)"
$RASQL --quiet -q "select c - 2c into $TEST_NULL_INTO from $TEST_NULL as c"
result=$($RASQL -q "select add_cells(c) from $TEST_NULL_INTO as c" --out string | grep 'Result ' | awk '{ print $4 }')
exp_result="34"
if [ "$result" == "$exp_result" ]; then
  echo ok.
  NUM_SUC=$(($NUM_SUC + 1))
else
  echo failed.
  NUM_FAIL=$(($NUM_FAIL + 1))
fi

drop_colls $TEST_NULL
drop_colls $TEST_NULL_INTO
drop_types $set_type $mdd_type

# ------------------------------------------------------------------------------

logn "dropping collection $TMP_COLLECTION... "
$RASQL --quiet -q "drop collection $TMP_COLLECTION"
if [ $? -eq 0 ]; then
	echo ok.
	NUM_SUC=$(($NUM_SUC + 1))
else
	echo failed.
	NUM_FAIL=$(($NUM_FAIL + 1))
fi

# ------------------------------------------------------------------------------

logn "deleting MDD from collection... "
$RASQL --quiet -q "delete from $TEST_COLLECTION as a where all_cells(a>0)"
if [ $? -eq 0 ]; then
	echo ok.
	NUM_SUC=$(($NUM_SUC + 1))
else
	echo failed.
	NUM_FAIL=$(($NUM_FAIL + 1))
fi

# ------------------------------------------------------------------------------

logn "dropping collection $TEST_COLLECTION... "
$RASQL --quiet -q "drop collection $TEST_COLLECTION"
if [ $? -eq 0 ]; then
	echo ok.
	NUM_SUC=$(($NUM_SUC + 1))
else
	echo failed.
	NUM_FAIL=$(($NUM_FAIL + 1))
fi


# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
