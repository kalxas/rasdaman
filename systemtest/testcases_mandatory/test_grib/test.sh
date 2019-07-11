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

PROG=$(basename $0)

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

#
# paths
#
TESTDATA_PATH="$SCRIPT_DIR/testdata"
[ -d "$TESTDATA_PATH" ] || error "Testdata directory not found: $TESTDATA_PATH"

TEST_FILE="$TESTDATA_PATH/test5messages.grib2"
EXIT_CODE_FAIL=255
EXIT_CODE_OK=0

drop_data()
{
  #drop_colls test_grib2_3d
  drop_colls test_grib2_4d
  $RASQL -q "drop type DoubleSet4D_SystemTest" > /dev/null 2>&1
  $RASQL -q "drop type DoubleArray4D_SystemTest" > /dev/null 2>&1
}

_cleanup()
{
  drop_data
}
trap '_cleanup' EXIT

# ------------------------------------------------------------------------------
# test dependencies
#
check_rasdaman


# ------------------------------------------------------------------------------
# drop collection if they already exists
#
drop_data

#
# if GRIB is not configured, don't execute the test
#
$RASQL -q "create collection test_grib2_3d DoubleSet3" > /dev/null
$RASQL -q 'insert into test_grib2_3d values inv_grib($1, "{ \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0:0,0:719,0:360]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:719,0:360]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:719,0:360]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:719,0:360]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:719,0:360]\" } ] } }")' -f "$TEST_FILE" --quiet 2>&1 | grep "rasdaman error 218: Exception: Conversion format is not supported." > /dev/null
if [ $? -eq 0 -o "$HAVE_GRIB" != ON ]; then
    log "GRIB format support disabled, skipping test."
    exit $RC_OK
fi
drop_colls test_grib2_3d

# ------------------------------------------------------------------------------
# create test collections
#

# check data types
check_type DoubleSet3

$RASQL -q "create type DoubleArray4D_SystemTest as double mdarray [ t, p, x, y ]" > /dev/null
$RASQL -q "create type DoubleSet4D_SystemTest as set ( DoubleArray4D_SystemTest )" > /dev/null

$RASQL -q "create collection test_grib2_3d DoubleSet3" > /dev/null
$RASQL -q "create collection test_grib2_4d DoubleSet4D_SystemTest" > /dev/null

#
# test 3D
#

check_3D_insertion()
{
expected="  Result element 1: [0:4,0:719,0:360]"
result=$($RASQL -q 'select sdom(c) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array domain"

expected="  Result element 1: 0.122111803631888"
result=$($RASQL -q 'select avg_cells(c) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check full array contents"

expected="  Result element 1: 0.1035237765466262"
result=$($RASQL -q 'select avg_cells(c[1,*:*,*:*]) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array slice contents"

expected="  Result element 1: 1.355"
result=$($RASQL -q 'select avg_cells(c[3,700:*,5]) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array row"

expected="  Result element 1: -1.083333333333333"
result=$($RASQL -q 'select avg_cells(c[1,2,5:10]) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array column"
}

log "3D insertion"
$RASQL -q 'insert into test_grib2_3d values decode($1, "application/x-ogc-grib", "{ \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0:0,0:719,0:360]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:719,0:360]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:719,0:360]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:719,0:360]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:719,0:360]\" } ] } }")' -f "$TEST_FILE" > /dev/null
update_result
check_3D_insertion
drop_colls test_grib2_3d

log ""
log "test filePaths parameter"
$RASQL -q "create collection test_grib2_3d DoubleSet3" > /dev/null
$RASQL -q 'insert into test_grib2_3d values decode($1, "application/x-ogc-grib", "{ \"filePaths\": [\"'$TEST_FILE'\"], \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0:0,0:719,0:360]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:719,0:360]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:719,0:360]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:719,0:360]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:719,0:360]\" } ] } }")' -f "$SCRIPT_DIR/test.sh" > /dev/null
update_result
check_3D_insertion
drop_colls test_grib2_3d

log ""
log "test subsetDomain parameter"
$RASQL -q "create collection test_grib2_3d DoubleSet3" > /dev/null
$RASQL -q 'insert into test_grib2_3d values decode($1, "application/x-ogc-grib", "{ \"filePaths\": [\"'$TEST_FILE'\"], \"subsetDomain\": \"[1:1,0:719,0:360]\", \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0:0,0:719,0:360]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:719,0:360]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:719,0:360]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:719,0:360]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:719,0:360]\" } ] } }")' -f "$SCRIPT_DIR/test.sh" > /dev/null
update_result

expected="  Result element 1: [1:1,0:719,0:360]"
result=$($RASQL -q 'select sdom(c) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array domain"

expected="  Result element 1: 0.1035237765466262"
result=$($RASQL -q 'select avg_cells(c) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check full array contents"

expected="  Result element 1: 0.1035237765466262"
result=$($RASQL -q 'select avg_cells(c[1,*:*,*:*]) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array slice contents"

expected="  Result element 1: -1.083333333333333"
result=$($RASQL -q 'select avg_cells(c[1,2,5:10]) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array column"
drop_colls test_grib2_3d

log ""
log "test subsetDomain parameter non-slice"
$RASQL -q "create collection test_grib2_3d DoubleSet3" > /dev/null
$RASQL -q 'insert into test_grib2_3d values decode($1, "application/x-ogc-grib", "{ \"filePaths\": [\"'$TEST_FILE'\"], \"subsetDomain\": \"[1:2,0:719,0:360]\", \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0:0,0:719,0:360]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:719,0:360]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:719,0:360]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:719,0:360]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:719,0:360]\" } ] } }")' -f "$SCRIPT_DIR/test.sh" > /dev/null
update_result

expected="  Result element 1: [1:2,0:719,0:360]"
result=$($RASQL -q 'select sdom(c) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array domain"

expected="  Result element 1: 0.1173613034779906"
result=$($RASQL -q 'select avg_cells(c) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check full array contents"

expected="  Result element 1: 0.1035237765466262"
result=$($RASQL -q 'select avg_cells(c[1,*:*,*:*]) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array slice contents"

expected="  Result element 1: -1.083333333333333"
result=$($RASQL -q 'select avg_cells(c[1,2,5:10]) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array column"

expected="  Result element 1: -0.1960000000000001"
result=$($RASQL -q 'select avg_cells(c[1:1,0:4,0:4]) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check subset contents"
drop_colls test_grib2_3d

log ""
log "test subsetDomain parameter region"
$RASQL -q "create collection test_grib2_3d DoubleSet3" > /dev/null
$RASQL -q 'insert into test_grib2_3d values decode($1, "application/x-ogc-grib", "{ \"filePaths\": [\"'$TEST_FILE'\"], \"subsetDomain\": \"[1:1,0:4,0:4]\", \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0:0,0:719,0:360]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:719,0:360]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:719,0:360]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:719,0:360]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:719,0:360]\" } ] } }")' -f "$SCRIPT_DIR/test.sh" > /dev/null
update_result

expected="  Result element 1: [1:1,0:4,0:4]"
result=$($RASQL -q 'select sdom(c) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check array domain"

expected="  Result element 1: -0.1960000000000001"
result=$($RASQL -q 'select avg_cells(c) from test_grib2_3d as c' --out string | grep Result)
check_result "$expected" "$result" "check full array contents"
drop_colls test_grib2_3d


log ""
log "4D insertion"
$RASQL -q 'insert into test_grib2_4d values decode($1, "GRIB", "{ \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0:0,0:0,0:719,0:360]\" }, { \"msgId\": 2, \"domain\": \"[0:0,1:1,0:719,0:360]\" }, { \"msgId\": 3, \"domain\": \"[1:1,0:0,0:719,0:360]\" }, { \"msgId\": 4, \"domain\": \"[2:2,0:0,0:719,0:360]\" }, { \"msgId\": 5, \"domain\": \"[2:2,2:2,0:719,0:360]\" } ] } }")' -f "$TEST_FILE" > /dev/null
update_result

expected="  Result element 1: [0:2,0:2,0:719,0:360]"
result=$($RASQL -q 'select sdom(c) from test_grib2_4d as c' --out string | grep Result)
check_result "$expected" "$result" "check array domain"

expected="  Result element 1: 0.06783989090660443"
result=$($RASQL -q 'select avg_cells(c) from test_grib2_4d as c' --out string | grep Result)
check_result "$expected" "$result" "check full array contents"

expected="  Result element 1: 0.1035237765466262"
result=$($RASQL -q 'select avg_cells(c[0,1,*:*,*:*]) from test_grib2_4d as c' --out string | grep Result)
check_result "$expected" "$result" "check array slice contents"

expected="  Result element 1: 1.355"
result=$($RASQL -q 'select avg_cells(c[2,0,700:*,5]) from test_grib2_4d as c' --out string | grep Result)
check_result "$expected" "$result" "check array row"

expected="  Result element 1: -1.083333333333333"
result=$($RASQL -q 'select avg_cells(c[0,1,2,5:10]) from test_grib2_4d as c' --out string | grep Result)
check_result "$expected" "$result" "check array column"

expected="  Result element 1: 0"
result=$($RASQL -q 'select avg_cells(c[2,1,*:*,*:*]) from test_grib2_4d as c' --out string | grep Result)
check_result "$expected" "$result" "check empty array slice contents"

log ""
log "test error conditions"

$RASQL -q 'insert into test_grib2_3d values inv_grib($1, "{ \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0:0,0:360,0:719]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:360,0:719]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:360,0:719]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:360,0:719]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:360,0:719]\" } ] } }")' -f "$TEST_FILE" > /dev/null 2>&1
check_result $EXIT_CODE_FAIL $? "swapped x and y"

$RASQL -q 'insert into test_grib2_3d values inv_grib($1, "{ \"internalStructure\": { messageDomains: [ { \"msgId\": 1, \"domain\": \"[0:0,0:360,0:719]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:360,0:719]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:360,0:719]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:360,0:719]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:360,0:719]\" } ] } }")' -f "$TEST_FILE" > /dev/null 2>&1
check_result $EXIT_CODE_FAIL $? "invalid json (unquoted fields)"

$RASQL -q 'insert into test_grib2_3d values inv_grib($1, "{ \"internalStructure\": { \"messageDomains\": \"[0:0,0:360,0:719]\",\"[1:1,0:360,0:719]\",\"[2:2,0:360,0:719]\",\"[3:3,0:360,0:719]\",\"[4:4,0:360,0:719]\" ] } }")' -f "$TEST_FILE" > /dev/null 2>&1
check_result $EXIT_CODE_FAIL $? "invalid json (missing bracket)"

$RASQL -q 'insert into test_grib2_3d values decode($1, "GRIB", "{ \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0,0:719,0:360]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:719,0:360]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:719,0:360]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:719,0:360]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:719,0:360]\" } ] } }")' -f "$TEST_FILE" > /dev/null 2>&1
check_result $EXIT_CODE_FAIL $? "invalid message domain (contains slices)"

$RASQL -q 'insert into test_grib2_3d values decode($1, "GRIB", "{ \"internalStructure\": { \"messageDomains\": [ { \"msgId\": 1, \"domain\": \"[0:0,0:719,0:360]\" },{ \"msgId\": 2, \"domain\": \"[1:1,0:709,0:360]\" },{ \"msgId\": 3, \"domain\": \"[2:2,0:719,0:350]\" },{ \"msgId\": 4, \"domain\": \"[3:3,0:719,0:360]\" },{ \"msgId\": 5, \"domain\": \"[4:4,0:719,0:360]\" } ] } }")' -f "$TEST_FILE" > /dev/null 2>&1
check_result $EXIT_CODE_FAIL $? "mismatching message domains (different x/y)"



# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
