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

STRUCT_TYPE_NAME="TestStructType"
MARRAY_DIM_TYPE_NAME="TestMarrayDimType"
MARRAY_DOM_TYPE_NAME="TestMarrayDomType"
SET_TYPE_NAME="TestSetType"
SET_TYPE_NAME_NULL_VALUES="TestSetTypeNullValues"


TEST_STRUCT_TYPE="CREATE TYPE $STRUCT_TYPE_NAME AS (red char, green char, blue char)"
TEST_MARRAY_DIM_TYPE="CREATE TYPE $MARRAY_DIM_TYPE_NAME AS $STRUCT_TYPE_NAME MDARRAY [a0,a1]"
TEST_MARRAY_DOM_TYPE="CREATE TYPE $MARRAY_DOM_TYPE_NAME AS (red char, green char, blue char) MDARRAY [a0(0:10),a1(0:*)]"
TEST_SET_TYPE="CREATE TYPE $SET_TYPE_NAME AS SET ($MARRAY_DIM_TYPE_NAME)"
TEST_SET_TYPE_NULL_VALUES="CREATE TYPE $SET_TYPE_NAME_NULL_VALUES AS SET ($MARRAY_DOM_TYPE_NAME NULL VALUES [1:3])"


# ------------------------------------------------------------------------------
# test dependencies
#
check_postgres
check_rasdaman

# ------------------------------------------------------------------------------
# start test
#

# check if RAS_TYPES collection exist
function test_select_type() {

logn "SELECT $1 FROM $1 ... "
$RASQL --quiet -q "SELECT $1 FROM $1" --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD
if [ $? -eq 0 ]; then
    echo ok.
    NUM_SUC=$(($NUM_SUC + 1))
else
	echo failed.
	NUM_FAIL=$(($NUM_FAIL + 1))
fi
}

# check if a collection type is created and if it exists in the RAS_TYPES collections
function test_create_type() {

logn "$1 ... "
$RASQL --quiet -q "$1" --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD
if [ $? -eq 0 ]; then
    TYPES=$($RASQL -q "SELECT a FROM $2 a" --out string)
    echo $TYPES | grep -F -q "$1"
    if [ $? -eq 0 ]; then
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
}

# check drop type
function test_drop_type() {
logn "DROP TYPE $1 ... "
$RASQL --quiet -q "DROP TYPE $1" --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD
if [ $? -eq 0 ]; then
	echo ok.
    NUM_SUC=$(($NUM_SUC + 1))
else
	echo failed.
	NUM_FAIL=$(($NUM_FAIL + 1))
fi
}

test_select_type "RAS_STRUCT_TYPES"
test_select_type "RAS_MARRAY_TYPES"
test_select_type "RAS_SET_TYPES"

# if RAS_TYPES collections exist then run the rest of the tests
if [ $NUM_FAIL -eq 0 ]; then
    test_create_type "$TEST_STRUCT_TYPE" "RAS_STRUCT_TYPES"
    test_create_type "$TEST_MARRAY_DIM_TYPE" "RAS_MARRAY_TYPES"
    test_create_type "$TEST_MARRAY_DOM_TYPE" "RAS_MARRAY_TYPES"
    test_create_type "$TEST_SET_TYPE" "RAS_SET_TYPES"
    test_create_type "$TEST_SET_TYPE_NULL_VALUES" "RAS_SET_TYPES"

    test_drop_type "$SET_TYPE_NAME"
    test_drop_type "$SET_TYPE_NAME_NULL_VALUES"
    test_drop_type "$MARRAY_DIM_TYPE_NAME"
    test_drop_type "$MARRAY_DOM_TYPE_NAME"
    test_drop_type "$STRUCT_TYPE_NAME"
fi

# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
