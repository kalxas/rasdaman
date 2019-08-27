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

PROG=$(basename $0)

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

TEST_COLL="test_tmp"
TMP_COLL="test_tmp_select_into"
TEST_TARGET=test_up_tgt
TEST_SOURCE=test_up_src

# set to "true" to run valgrind on update queries
run_valgrind=false
# run_valgrind=true

# ------------------------------------------------------------------------------
# test dependencies
#
check_rasdaman

# check data types
check_type GreySet

select_scalar()
{
    local q="$1"
    $RASQL -q "$q" --out string | grep 'Result ' | sed 's/^.*: //'
}

add_cells()
{
    select_scalar "select add_cells(c)$2 from $1 as c"
}

# ------------------------------------------------------------------------------
# drop test collection if they already exists
#
logn "test initialization..."
drop_colls $TEST_COLL
feedback

#
# ------------------------------------------------------------------------------
# Tests for updating with tiff-encoded data. Repeat all tests with filePaths
# format parameters, as well as a file passed with -f to rasql.
#
# 1. Source and target have same dimension (2)
#
#    a) Test for updates inside, intersecting, and non-intersecting the target
#
# 2. Source has lower dimension (2) than target (3) (target is sliced)
#
#    a) Slice target at all 3 dimensions
#
# For both cases test for all type sizes (types char, short, float, double)
#

TESTDATA="$SCRIPT_DIR/testdata"

run_update_test()
{
    local celltype="$1"     # cell type name
    local settype="$2"      # set type name
    local cellsize="$3"     # cell size in bytes
    local formatparams="$4" # true to use format decode parameters, otherwise it uses -f argument for rasql
    local f="$TESTDATA/${celltype}_${cellsize}.tif"

    local mddwidth=20
    local cellval="$cellsize"
    local mddval=
    local fileparam=
    local filepaths=

    local defaultval="0"
    local band=""
    if [ "$settype" == RGBSet ]; then
        defaultval="{0c,0c,0c}"
        band=".2"
    fi

    if [ "$formatparams" == true ]; then
        log "testing filePaths in encode format params, cell type ${celltype}"
        filepaths='{\"filePaths\":[\"'$f'\"]}'
        mddval="<[0:0] 0c>"
    else
        log "testing -f rasql parameter, cell type ${celltype}"
        fileparam="-f $f"
        mddval="\$1"
    fi
    sum=$(($mddwidth * $mddwidth * $cellval))

    init_mdd()
    {
        local mdddom="$1"
        local tiledom="$2"
        $RASQL --quiet -q "create collection $TEST_COLL $settype"
        # init MDD object with sdom [0:19,0:19] of 0s with 20 "unusual" tiles
        if [ "$formatparams" == true ]; then
            $RASQL --quiet -q "insert into $TEST_COLL values marray i in $mdddom values ($celltype)$defaultval"
        else
            $RASQL --quiet -q "insert into $TEST_COLL values marray i in $mdddom values ($celltype)$defaultval tiling aligned $tiledom tile size $(($cellsize * 1 * 30))"
        fi  
    }

    test()
    {
        local prefix="$1"
        local middle="$2"
        local suffix="$3"

        get_sdom()
        {
            echo "${prefix}$1,${middle}$2${suffix}"
        }

        query()
        {
            if [ "$run_valgrind" == true ]; then
                valgrind --tool=memcheck --leak-check=full \
                $DIRECTQL --quiet -q "$1" $2 2>&1 | grep --quiet "ERROR SUMMARY: 0 errors"
                if [ $? -ne 0 ]; then
                    valgrind --tool=memcheck --leak-check=full --track-origins=yes \
                    $DIRECTQL --quiet -q "$1" $2
                    exit 1
                fi
            else
                $RASQL --quiet -q "$1" $2 || exit 1
            fi
        }

        local setdom=$(get_sdom "*:*" "*:*")

        # update completely inside
        query "update $TEST_COLL as m set m[$setdom] assign shift(decode($mddval, \"GDAL\", \"$filepaths\"), [5,5])" "$fileparam"
        res=$(add_cells "$TEST_COLL" "$band")
        check_result "$sum" "$res" "    update source is covered by the target object"

        # update intersecting, mdd obj is now [0:39,0:39]
        query "update $TEST_COLL as m set m[$setdom] assign shift(decode($mddval, \"GDAL\", \"$filepaths\"), [20,20])" "$fileparam"
        res=$(select_scalar "select add_cells(c[$(get_sdom 20:39 20:39)]$band) from $TEST_COLL as c")
        check_result "$sum" "$res" "    update source is intersecting the target object"
        res=$(add_cells "$TEST_COLL" "$band")
        exp=$((($sum * 2) - (5 * 5 * $cellval)))
        check_result "$exp" "$res" "    update source is intersecting the target object full check"

        # update non-intersecting, mdd obj is now [0:69,0:69]
        query "update $TEST_COLL as m set m[$setdom] assign shift(decode($mddval, \"GDAL\", \"$filepaths\"), [50,50])" "$fileparam"
        res=$(select_scalar "select add_cells(c[$(get_sdom 50:69 50:69)]$band) from $TEST_COLL as c")
        check_result "$sum" "$res" "    update source is not intersecting the target object"
        res=$(add_cells "$TEST_COLL" "$band")
        exp=$((($sum * 3) - (5 * 5 * $cellval)))
        check_result "$exp" "$res" "    update source is not intersecting the target object full check"

        # update intersecting, mdd obj is now still [0:69,0:69]
        query "update $TEST_COLL as m set m[$setdom] assign shift(decode($mddval, \"GDAL\", \"$filepaths\"), [35,35])" "$fileparam"
        res=$(select_scalar "select add_cells(c[$(get_sdom 35:54 35:54)]$band) from $TEST_COLL as c")
        check_result "$sum" "$res" "    update source is intersecting independent tiles of the target object"
    }

    log "  test same dimension update for cell type ${celltype}"
    init_mdd "[0:29,0:29]" "[0:0,0:29]"
    test "" "" ""
    drop_colls "$TEST_COLL"

    # 3D testing
    settype="${settype}3"

    log "  test different dimension update sliced at first dimension"
    init_mdd "[0:0,0:29,0:29]" "[0:0,0:0,0:29]"
    test "0," "" ""
    drop_colls "$TEST_COLL"

    log "  test different dimension update sliced at first dimension (non-existing domain)"
    init_mdd "[0:0,0:29,0:29]" "[0:0,0:0,0:29]"
    test "1," "" ""
    drop_colls "$TEST_COLL"

    log "  test different dimension update sliced at second dimension"
    init_mdd "[0:29,0:29,0:0]" "[0:0,0:29,0:0]"
    test "" "0," ""
    drop_colls "$TEST_COLL"

    log "  test different dimension update sliced at third dimension"
    init_mdd "[0:29,0:29,0:0]" "[0:0,0:29,0:0]"
    test "" "" ",0"
    drop_colls "$TEST_COLL"

    log "  test different dimension update sliced at third dimension (non-existing domain)"
    init_mdd "[0:29,0:29,0:0]" "[0:0,0:29,0:0]"
    test "" "" ",100"
    drop_colls "$TEST_COLL"
}

#
# run update test
#
# TODO: add support for raw generated data

for formatparams in true false; do
    for p in "char GreySet 1" "short ShortSet 2" "float FloatSet 4" "double DoubleSet 8" "char RGBSet 3"; do
        run_update_test $p $formatparams
    done
done


# ------------------------------------------------------------------------------
# start test
#
create_coll $TEST_COLL GreySet


logn "inserting MDD into collection... "
$RASQL --quiet -q "insert into $TEST_COLL values marray x in [0:255, 0:210] values 1c"
check

# ------------------------------------------------------------------------------

logn "updating MDD from collection... "
$RASQL --quiet -q "update $TEST_COLL as a set a assign a[0:179,0:54] + 1c"
check

# ------------------------------------------------------------------------------

logn "testing SELECT INTO a new collection... "
$RASQL --quiet -q "select c / 2 into $TMP_COLL from $TEST_COLL as c"
check

sdom1=`$RASQL -q "select sdom(c) from $TMP_COLL as c" --out string`
sdom2=`$RASQL -q "select sdom(c) from $TEST_COLL as c" --out string`
check_result "$sdom1" "$sdom2" "testing select into"

# ------------------------------------------------------------------------------

# insert another object, so we test deleting all objects from one collection
$RASQL --quiet -q "select c / 2 into $TMP_COLL from $TEST_COLL as c"

logn "delete all MDDs from a collection... "
$RASQL --quiet -q "delete from $TMP_COLL"
check

sdom=$($RASQL --quiet -q "select sdom(c) from $TMP_COLL as c" --out string)
check_result "" "$sdom" "deleting all objects from a collection"

# ------------------------------------------------------------------------------

mdd_type=NullValueArrayTest2
set_type=NullValueSetTest2

# check data types and insert if not available
check_user_type $set_type
if [ $? -ne 0 ]; then
    $RASQL --quiet -q "create type $mdd_type as char mdarray [ x, y ]" > /dev/null | tee -a $LOG
    $RASQL --quiet -q "create type $set_type as set ( $mdd_type null values [1] )" > /dev/null | tee -a $LOG
fi

TEST_NULL=test_null
TEST_NULL_INTO=test_null_into
drop_colls $TEST_NULL
drop_colls $TEST_NULL_INTO
create_coll $TEST_NULL $set_type

$RASQL --quiet -q "insert into $TEST_NULL values marray x in [0:3,0:3] values (char)(x[0] + x[1] + 1)"
$RASQL --quiet -q "select c - 2c into $TEST_NULL_INTO from $TEST_NULL as c"
result=$(add_cells "$TEST_NULL_INTO")
exp_result="34"
check_result $exp_result $result "select into with null value transfer"

drop_colls $TEST_NULL
drop_colls $TEST_NULL_INTO
drop_types $set_type $mdd_type

# ------------------------------------------------------------------------------

logn "dropping collection $TMP_COLL... "
$RASQL --quiet -q "drop collection $TMP_COLL"
check

# ------------------------------------------------------------------------------

logn "deleting MDD from collection... "
$RASQL --quiet -q "delete from $TEST_COLL as a where all_cells(a>0)"
check

# ------------------------------------------------------------------------------

logn "dropping collection $TEST_COLL... "
$RASQL --quiet -q "drop collection $TEST_COLL"
check

# ------------------------------------------------------------------------------
# test if rasdaman throws an error when importing array with wrong type
# ------------------------------------------------------------------------------

TEST_COLL=test_rgb_wrong
create_coll $TEST_COLL GreySet

$RASQL --quiet -q "insert into $TEST_COLL values decode(\$1)" -f $SCRIPT_DIR/testdata/rgb.png 2>&1 | grep -F -q "959"
check_result 0 $? "inserting a wrong type... "

drop_colls $TEST_COLL

# ------------------------------------------------------------------------------

TEST_COLL=test_insert
drop_colls $TEST_COLL
logn "create collection $TEST_COLL... "
$RASQL --quiet -q "create collection $TEST_COLL GreySet"
check

logn "dropping collection $TEST_COLL... "
$RASQL --quiet -q "drop collection $TEST_COLL"
check

TEST_COLL=test_select
drop_colls $TEST_COLL
logn "create collection $TEST_COLL... "
$RASQL --quiet -q "create collection $TEST_COLL GreySet"
check

logn "dropping collection $TEST_COLL... "
$RASQL --quiet -q "drop collection $TEST_COLL"
check

# ------------------------------------------------------------------------------
# test updates with null values
# ------------------------------------------------------------------------------

log ""
log "testing updates with null values attached to source array (char)"

TEST_COLL=test_update_nulls_greyset
drop_colls $TEST_COLL
logn "  create collection $TEST_COLL... "
$RASQL --quiet -q "create collection $TEST_COLL GreySet"
check

logn "  inserting testdata... "
$RASQL --quiet -q "insert into $TEST_COLL values <[0:1,0:1] 0c, 1c; 2c, 3c>"
check

res=$(add_cells "$TEST_COLL")
check_result "6" "$res" "  checking testdata"

logn "  updating testdata with null values... "
$RASQL --quiet -q "update $TEST_COLL as c set c[0:1,0:1] assign <[0:1,0:1] 6c, 0c; 1c, 7c> null values [6:7]"
check

# current array: <[0:1,0:1] 0c, 0c; 1c, 3c>
res=$(add_cells "$TEST_COLL")
check_result "4" "$res" "  checking updated testdata"

logn "  dropping collection $TEST_COLL... "
$RASQL --quiet -q "drop collection $TEST_COLL"
check


log ""
log "testing updates with null values attached to source array (rgb)"

TEST_COLL=test_update_nulls_rgb
drop_colls $TEST_COLL
logn "  create collection $TEST_COLL... "
$RASQL --quiet -q "create collection $TEST_COLL RGBSet"
check

logn "  inserting testdata... "
$RASQL --quiet -q "insert into $TEST_COLL values <[0:1,0:1] {1c, 1c, 1c}, {0c, 1c, 2c}; {1c, 2c, 3c}, {2c, 2c, 2c}>"
check

res=$(add_cells "$TEST_COLL")
check_result "{ 4, 6, 8 }" "$res" "  checking testdata"

logn "  updating testdata with null values... "
$RASQL --quiet -q "update $TEST_COLL as c set c[0:1,0:1] assign <[0:1,0:1] {4c, 5c, 6c}, {1c, 1c, 1c}; {2c, 2c, 2c}, {0c, 12c, 6c}> null values [1:2]"
check

# current array: <[0:1,0:1] {4c, 5c, 6c}, {0c, 1c, 2c}; {1c, 2c, 3c}, {0c, 12c, 6c}>
res=$(add_cells "$TEST_COLL")
check_result "{ 5, 20, 17 }" "$res" "  checking updated testdata"

logn "  dropping collection $TEST_COLL... "
$RASQL --quiet -q "drop collection $TEST_COLL"
check

# ------------------------------------------------------------------------------

TEST_COLL=test123123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789
drop_colls $TEST_COLL

$RASQL --quiet -q "create collection $TEST_COLL GreySet" 2>&1 | grep -F -q "974"
check_result 0 $? "create collection with name longer than 200 characters... "

$RASQL --quiet -q "drop collection $TEST_COLL" 2>&1 | grep -F -q "1013"
check_result 0 $? "dropping collection with name longer than 200 characters... "

drop_colls $TEST_COLL

# ------------------------------------------------------------------------------
# tests for updating with the FROM clause

#drop collections, if they exist
drop_colls $TEST_TARGET $TEST_SOURCE

#create collections
create_coll $TEST_TARGET GreySet
create_coll $TEST_SOURCE GreySet
$RASQL --quiet -q "insert into $TEST_TARGET values marray x in [0:299, 0:299] values 0c"
$RASQL --quiet -q "insert into $TEST_SOURCE values marray x in [0:299, 0:299] values 1c"

# check update with incompatible base type
exp_result="rasdaman error 434: Execution error: Cell base types of binary induce operation are incompatible."
result=$($RASQL --quiet -q "update $TEST_TARGET as c set c assign decode(\$1)" -f $SCRIPT_DIR/testdata/rgb.png 2>&1)
check_result "$exp_result" "$result"
result=$($RASQL --quiet -q "update $TEST_TARGET as c set c assign decode(\$1)" -f $SCRIPT_DIR/testdata/double_8.tif 2>&1)
check_result "$exp_result" "$result"

logn "testing UPDATE ... FROM at a single point on two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS t SET t[0:0,0:0] ASSIGN s[0:0,0:0] FROM $TEST_SOURCE as s"
result=$(select_scalar "select c[0,0] from $TEST_TARGET as c")
exp_result="1"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on non-overlapping domains from a single collection... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[0:9,0:9] ASSIGN shift(b[0:9,10:19], [0,-10])+1c FROM $TEST_TARGET AS b"
result=$(select_scalar "select c[5,5] from $TEST_TARGET as c")
exp_result="1"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on non-overlapping domains from a single collection... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[100:109,100:109] ASSIGN shift(b[100:109,110:119], [0,-10])+2c FROM $TEST_TARGET AS b WHERE ( 0 = 0 )"
result=$(select_scalar "select c[105,105] from $TEST_TARGET as c")
exp_result="2"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on overlapping domains from a single collection... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[0:9,20:29] ASSIGN b[0:9,20:29]+3c FROM $TEST_TARGET AS b"
result=$(select_scalar "select c[5,25] from $TEST_TARGET as c")
exp_result="3"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on overlapping domains from a single collection... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[100:109,120:129] ASSIGN b[100:109,120:129]+4c FROM $TEST_TARGET AS b WHERE ( 0 = 0 )"
result=$(select_scalar "select c[105,125] from $TEST_TARGET as c")
exp_result="4"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on non-overlapping grid domains from two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[10:19,0:9] ASSIGN shift(b[10:19,10:19], [0,-10]) +7c FROM $TEST_SOURCE AS b"
result=$(select_scalar "select c[15,5] from $TEST_TARGET as c")
exp_result="8"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on non-overlapping grid domains from two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[110:119,100:109] ASSIGN shift(b[110:119,110:119], [0,-10]) +8c FROM $TEST_SOURCE AS b WHERE ( 0 = 0 )"
result=$(select_scalar "select c[115,105] from $TEST_TARGET as c")
exp_result="9"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on overlapping grid domains from two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[10:19,10:19] ASSIGN b[10:19,10:19] +9c FROM $TEST_SOURCE AS b"
result=$(select_scalar "select c[15,15] from $TEST_TARGET as c")
exp_result="10"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on overlapping grid domains from two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[110:119,110:119] ASSIGN b[110:119,110:119] +10c FROM $TEST_SOURCE AS b WHERE ( 0 = 0 )"
result=$(select_scalar "select c[115,115] from $TEST_TARGET as c")
exp_result="11"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on two distinct collections, where the collection described in the FROM clause appears in the SET clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET b[10:19,10:19] ASSIGN b[10:19,10:19] +11c FROM $TEST_SOURCE AS b"
result=$(select_scalar "select c[15,15] from $TEST_SOURCE as c")
exp_result="12"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, where the collection described in the FROM clause appears in the SET clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET b[120:129,110:119] ASSIGN b[120:129,110:119] +12c FROM $TEST_SOURCE AS b WHERE ( 0 = 0 )"
result=$(select_scalar "select c[125,115] from $TEST_SOURCE as c")
exp_result="13"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, with the WHERE clause a function of the FROM clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[130:139,110:119] ASSIGN b[130:139,110:119] +13c FROM $TEST_SOURCE AS b WHERE some_cells( b = 1 )"
result=$(select_scalar "select c[135,115] from $TEST_TARGET as c")
exp_result="14"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, with the WHERE clause a function of the UPDATE clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[140:149,110:119] ASSIGN b[140:149,110:119] +14c FROM $TEST_SOURCE AS b WHERE some_cells( a = 0 )"
result=$(select_scalar "select c[145,115] from $TEST_TARGET as c")
exp_result="15"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, with the WHERE clause a function of both the FROM clause and the UPDATE clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[150:159,110:119] ASSIGN b[150:159,110:119] +15c FROM $TEST_SOURCE AS b WHERE some_cells( a = b )"
result=$(select_scalar "select c[155,115] from $TEST_TARGET as c")
exp_result="16"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, with the WHERE clause returning FALSE... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[200:205,200:205] ASSIGN b[200:205,200:205] FROM $TEST_SOURCE as b WHERE some_cells( b = 0 )"
result=$(select_scalar "select c[205,205] from $TEST_TARGET as c")
exp_result="0"
check_result $exp_result $result

logn "testing UPDATE ... FROM where the FROM clause consists of a list of two collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[210:215,210:215] ASSIGN b[210:215,210:215] + c[210:215,210:215] FROM $TEST_SOURCE as b, $TEST_SOURCE as c"
result=$(select_scalar "select c[212,212] from $TEST_TARGET as c")
exp_result="2"
check_result $exp_result $result

#drop data so test can be rerun safely
drop_colls $TEST_TARGET $TEST_SOURCE
# end of tests for performing an UPDATE with the FROM clause


# ------------------------------------------------------------------------------
# Updates should properly initialize new tiles with null values
# http://rasdaman.org/ticket/1981

drop_test_update_nullvalues() {
    # clean up
    $RASQL --quiet -q "drop collection test_update_nullvalues" > /dev/null 2>&1
    for t in test_update_nullvalues_Set test_update_nullvalues_Array test_update_nullvalues_Cell; do
      $RASQL --quiet -q "drop type $t" > /dev/null 2>&1
    done
}

logn "testing null value initialization on update... "

drop_test_update_nullvalues

$RASQL --quiet -q 'CREATE TYPE test_update_nullvalues_Cell AS ( band0 long ,band1 long ,band2 long ,band3 long ,band4 long ,band5 long ,band6 long ,band7 long ,band8 long  )'
$RASQL --quiet -q 'CREATE TYPE test_update_nullvalues_Array AS test_update_nullvalues_Cell MDARRAY [D0,D1,D2]'
$RASQL --quiet -q 'CREATE TYPE test_update_nullvalues_Set AS SET (test_update_nullvalues_Array NULL VALUES [-9999])'
$RASQL --quiet -q 'CREATE COLLECTION test_update_nullvalues test_update_nullvalues_Set'

$RASQL --quiet -q 'INSERT INTO test_update_nullvalues VALUES <[0:0,0:0,0:0] {0l,0l,0l,0l,0l,0l,0l,0l,0l}> TILING ALIGNED [0:47, 0:19, 0:19] TILE SIZE 19200'

currdir="$(pwd)"
$RASQL --quiet -q 'UPDATE test_update_nullvalues SET test_update_nullvalues[0,0:0,0:0] ASSIGN <[0:0,0:0] { 1483228800l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l }>'
$RASQL --quiet -q 'UPDATE test_update_nullvalues SET test_update_nullvalues[1,0:0,0:0] ASSIGN <[0:0,0:0] { 1483230600l, 0l, 0l, 1900l, 965800l, -63400l, 1007099l, -600l, -9999l }>'

result=$(select_scalar "select c[1,0,0] from test_update_nullvalues as c")
exp_result="{ 1483230600, 0, 0, 1900, 965800, -63400, 1007099, -600, -9999 }"
check_result "$exp_result" "$result"

drop_test_update_nullvalues
# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
