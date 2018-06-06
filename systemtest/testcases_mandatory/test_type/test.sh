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
TEST_SET_TYPE_NULL_VALUES="CREATE TYPE $SET_TYPE_NAME_NULL_VALUES AS SET ($MARRAY_DOM_TYPE_NAME NULL VALUES [1.000000:3.000000])"


# ------------------------------------------------------------------------------
# test dependencies
#
check_postgres
check_rasdaman

# ------------------------------------------------------------------------------
# start test
#

# check if a collection type is created and if it exists in the RAS_TYPES collections
# $1: type name
# $2: types collection name
test_create_type()
{
    logn "$1 ... "
    $RASQL --quiet -q "$1"
    if [ $? -eq 0 ]; then
        $RASQL -q "SELECT a FROM $2 a" --out string | grep -F -q "$1"
        check
    else
    	check_failed
    fi
}

# check drop type
test_drop_type()
{
    $RASQL --quiet -q "DROP TYPE $1"
    check_result 0 $? "DROP TYPE $1"
}

drop_type_quiet()
{
    $RASQL --quiet -q "DROP TYPE $1" > /dev/null 2>&1
}

test_invalid_drop_type()
{
    $RASQL --quiet -q "DROP TYPE $1" 2>&1 | grep -F -q "Exception"
    check_result 0 $? "DROP TYPE $1"
}

# $1 is the type name, $2 is the format for the structure to be created
test_invalid_create_type()
{
    $RASQL --quiet -q "$1" 2>&1 | grep -F -q "Exception"
    check_result 0 $? "$1"
}


# check if RAS_TYPES collection exist
test_select_type()
{
    $RASQL --quiet -q "SELECT $1 FROM $1"
    check_result 0 $? "SELECT $1 FROM $1"
}
test_select_type "RAS_STRUCT_TYPES"
test_select_type "RAS_MARRAY_TYPES"
test_select_type "RAS_SET_TYPES"

# if RAS_TYPES collections exist then run the rest of the tests
if [ $NUM_FAIL -eq 0 ]; then

#testing type creation
    drop_type_quiet "$SET_TYPE_NAME_NULL_VALUES"
    drop_type_quiet "$SET_TYPE_NAME"
    drop_type_quiet "$MARRAY_DOM_TYPE_NAME"
    drop_type_quiet "$MARRAY_DIM_TYPE_NAME"
    drop_type_quiet "$STRUCT_TYPE_NAME"
    drop_type_quiet "TestGreySetNullValues"
    drop_type_quiet "test_waxlake_set"
    drop_type_quiet "test_waxlake_mdd"
    drop_type_quiet "test_waxlake_base"

    test_create_type "$TEST_STRUCT_TYPE" "RAS_STRUCT_TYPES"
    test_create_type "$TEST_MARRAY_DIM_TYPE" "RAS_MARRAY_TYPES"
    test_create_type "$TEST_MARRAY_DOM_TYPE" "RAS_MARRAY_TYPES"
    test_create_type "$TEST_SET_TYPE" "RAS_SET_TYPES"
    test_create_type "$TEST_SET_TYPE_NULL_VALUES" "RAS_SET_TYPES"

#testing error when making struct of structs
    STRUCT_OF_STRUCT_TYPE_NAME="TestStructOfStructsType"
    TEST_STRUCT_OF_STRUCT_TYPE_A="CREATE TYPE $STRUCT_OF_STRUCT_TYPE_NAME AS (x1 char, x2 $STRUCT_TYPE_NAME, x3 char)"
    TEST_STRUCT_OF_STRUCT_TYPE_B="CREATE TYPE $STRUCT_OF_STRUCT_TYPE_NAME AS (x1 $STRUCT_TYPE_NAME, x2 char, x3 char)"
    TEST_STRUCT_OF_STRUCT_TYPE_C="CREATE TYPE $STRUCT_OF_STRUCT_TYPE_NAME AS (x1 char, x2 char, x3 $STRUCT_TYPE_NAME)"

    test_invalid_create_type "$TEST_STRUCT_OF_STRUCT_TYPE_A"
    test_invalid_create_type "$TEST_STRUCT_OF_STRUCT_TYPE_B"
    test_invalid_create_type "$TEST_STRUCT_OF_STRUCT_TYPE_C"
#testing error when dropping in-use types
    test_invalid_drop_type "$STRUCT_TYPE_NAME"
    test_invalid_drop_type "$MARRAY_DOM_TYPE_NAME"

    COLL_TYPE_NAME="TestCollType"

    $RASQL --quiet -q "create collection $COLL_TYPE_NAME $SET_TYPE_NAME"
    test_invalid_drop_type "$SET_TYPE_NAME"
    $RASQL --quiet -q "drop collection $COLL_TYPE_NAME"
#testing altering collection type
    $RASQL --quiet -q "create collection $COLL_TYPE_NAME GreySet"
    # create type with null values 2 and alter type
    $RASQL --quiet -q "CREATE TYPE TestGreySetNullValues AS SET (GreyImage NULL VALUES [2:2])"
    $RASQL --quiet -q "alter collection $COLL_TYPE_NAME set type TestGreySetNullValues"
    # test if the output corresponds to the new type
    $RASQL --quiet -q "insert into $COLL_TYPE_NAME values <[0:0,0:1] 1c, 2c>"
    res=$($RASQL -q "select add_cells(c) from $COLL_TYPE_NAME as c" --out string | grep Result | awk '{ print $4; }')
    check_result 1 $res "ALTER COLLECTION with new type"
    # test alter type to incompatible type
    $RASQL --quiet -q "alter collection $COLL_TYPE_NAME set type FloatSet" > /dev/null 2>&1
    check_result 255 $? "ALTER COLLECTION with incompatible type"
    # cleanup
    $RASQL --quiet -q "drop collection $COLL_TYPE_NAME"
    test_drop_type "TestGreySetNullValues"
#testing type dropping
    test_drop_type "$SET_TYPE_NAME"
    test_drop_type "$SET_TYPE_NAME_NULL_VALUES"
    test_drop_type "$MARRAY_DIM_TYPE_NAME"
    test_drop_type "$MARRAY_DOM_TYPE_NAME"
    test_drop_type "$STRUCT_TYPE_NAME"

# test composite base type selection
    test_create_type "CREATE TYPE test_waxlake_base AS (b1 char, b2 char, b3 char)" "RAS_STRUCT_TYPES"
    test_create_type "CREATE TYPE test_waxlake_mdd AS test_waxlake_base MDARRAY [a0,a1]" "RAS_MARRAY_TYPES"
    test_create_type "CREATE TYPE test_waxlake_set AS SET (test_waxlake_mdd)" "RAS_SET_TYPES"
    $RASQL --quiet -q "create collection test_waxlake test_waxlake_set"
    $RASQL --quiet -q "INSERT INTO test_waxlake VALUES <[0:0,0:0] {0c,0c,0c}> "

    $RASQL --quiet -q "drop collection test_waxlake"
    test_drop_type "test_waxlake_set"
    test_drop_type "test_waxlake_mdd"
    test_drop_type "test_waxlake_base"
fi

# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
