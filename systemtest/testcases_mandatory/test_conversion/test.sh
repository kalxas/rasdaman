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
#  test.sh
# Description
#  Command-line utility for testing rasdaman.
#  1)creating collection
#   2)insert images into collection
#   3)extract images
#   4)compare
#   5)cleanup
#
# PRECONDITIONS
#   1)Postgres Server must be running
#   2)Rasdaman Server must be running
#   3)database RASBASE must exists
#   4)rasql utility must be fully running
#   5)images needed for testing shall be put in directory of images
# Usage: ./test.sh
#
# CHANGE HISTORY
#       2009-Sep-16     J.Yu       created
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
ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Expected results directory not found: $ORACLE_PATH"


# ------------------------------------------------------------------------------
# test dependencies
#
check_postgres
check_rasdaman
check_gdal

# check data types
check_type GreySet
check_type RGBSet
check_type Gauss1Set # 2D CFloat32 collection type
check_type Gauss2Set # 2D CFloat64 collection type
check_type CInt32Set
check_type CInt16Set

check_user_type TestSet
if [ $? -ne 0 ]; then
    $RASQL --quiet -q "create type TestPixel as (band1 octet, band2 octet, band3 octet, band4 octet)"
    $RASQL --quiet -q "create type TestArray as TestPixel MDARRAY [a0,a1]"
    $RASQL --quiet -q "create type TestSet as set (TestArray)"
fi
check_user_type TestFLSet
if [ $? -ne 0 ]; then
    $RASQL --quiet -q "create type TestStruct as (f float, l long)"
    $RASQL --quiet -q "create type TestFL as TestStruct MDARRAY [a0,a1]"
    $RASQL --quiet -q "create type TestFLSet as set (TestFL)"
fi

drop_data()
{
    drop_colls test_tmp
    for t in TestFLSet TestFL TestStruct TestSet TestArray TestPixel; do
        $RASQL -q "drop type $t" > /dev/null 2>&1
    done
}
_cleanup()
{
  drop_data
}
trap '_cleanup' EXIT


# ------------------------------------------------------------------------------
# drop collection if they already exists
#
drop_colls test_tmp

# ------------------------------------------------------------------------------
#function running the transposition test with the updated formats interface
#
function run_transpose_test()
{
    log ----- png and png GreySet transpose conversion ------

    create_coll test_tmp GreySet
    $RASQL -q 'insert into test_tmp values decode($1, "png", "{\"transpose\": [0,1]}")' \
           -f $TESTDATA_PATH/mr_1.png --quiet > /dev/null 2>&1
    $RASQL -q 'select encode(m, "png", "{\"transpose\": [0,1] }" ) from test_tmp as m' \
           --out file --outfile mr_1 --quiet > /dev/null

    logn "comparing images: "
    if [ -f "$ORACLE_PATH/mr_1.png.checksum" ]; then
      $GDALINFO mr_1.png | grep 'Checksum' > mr_1.png.result
      diff $ORACLE_PATH/mr_1.png.checksum mr_1.png.result > /dev/null
    else
      cmp $TESTDATA_PATH/mr_1.png mr_1.png > /dev/null
    fi

    check_result 0 $? "input and output match"

    drop_colls test_tmp
    rm -f mr_1*
}

# ------------------------------------------------------------------------------
#function running the scalar csv encoding test now that the output style should be enforced
#
function run_csv_scalar_test()
{
    log ----- csv scalar encode no-collection test ------

    $RASQL -q 'select encode(37, "csv")' --out file --outfile scalar1 > /dev/null 2>&1

    logn "comparing csv scalar output with oracle: "
    cmp $ORACLE_PATH/scalar1.csv.oracle scalar1.csv > /dev/null > /dev/null 2>&1
    check_result 0 $? "input and output match"

    log ----- json scalar encode no-collection test -----

    $RASQL -q 'select encode(37, "json")' --out file --outfile scalar1 > /dev/null 2>&1

    logn "comparing csv scalar output with oracle: "
    cmp $ORACLE_PATH/scalar1.json.oracle scalar1.json > /dev/null
    check_result 0 $? "input and output match"

    log ----- csv scalar encode test ------
#import data
    create_coll test_tmp GreySet
    $RASQL -q 'insert into test_tmp values decode($1)' -f $TESTDATA_PATH/mr_1.png --quiet > /dev/null 2>&1

    $RASQL -q 'select encode(c[100,100], "csv") from test_tmp as c' --out file --outfile scalar2 --quiet > /dev/null 2>&1

    logn "comparing csv scalar output with oracle: "
    cmp $ORACLE_PATH/scalar2.csv.oracle scalar2.csv > /dev/null
    check_result 0 $? "input and output match"

    log ----- json scalar encode test ------

    $RASQL -q 'select encode(c[100,100], "json") from test_tmp as c' --out file --outfile scalar2 --quiet > /dev/null 2>&1

    logn "comparing json scalar output with oracle: "
    cmp $ORACLE_PATH/scalar2.json.oracle scalar2.json > /dev/null
    check_result 0 $? "input and output match"

#drop data
    drop_colls test_tmp
    rm -f scalar*
}

# ------------------------------------------------------------------------------
# function runnning the test
#
function run_test()
{
    local fun=$1
    local inv_fun=$2
    local ext=$3
    local inv_ext=$4
    local colltype=$5
    local f="mr_1"
    if [ "$colltype" == RGBSet ]; then
      f=rgb
    elif [ "$colltype" == TestSet ]; then
      f=multiband
    elif [ "$colltype" == Gauss1Set ]; then
      f=cfloat32_image
    elif [ "$colltype" == Gauss2Set -a "$inv_fun" == decode ]; then
      f=cfloat64_image
    elif [ "$colltype" == Gauss2Set ]; then
      f=gauss
    elif [ "$colltype" == CInt32Set ]; then
      f=cint32_image
    elif [ "$colltype" == CInt16Set ]; then
      f=cint16_image
    elif [ "$colltype" == BoolSet ]; then
      f=bool_image
    elif [ "$colltype" == ULongSet ]; then
      f=ulong_image
    elif [ "$colltype" == ShortSet ]; then
      f=short_image
    elif [ "$colltype" == UShortSet ]; then
      f=ushort_image
    elif [ "$colltype" == LongSet ]; then
      f=long_image
    elif [ "$colltype" == FloatSet ]; then
      f=float_image
    elif [ "$colltype" == DoubleSet ]; then
      f=double_image
    fi

    local decodeopts="$6"   # used on data insertion
    local encodeopts="$7"   # used on data selection
    local rasqlopts="$8"
    local oracle2="$9"      # use second oracle with suffix _2

    log ----- $fun and $inv_fun $colltype conversion ------

    create_coll test_tmp $colltype
    insert_into test_tmp "$TESTDATA_PATH/$f.$inv_ext" "$decodeopts" "$inv_fun" "$rasqlopts"
    export_to_file test_tmp "$f" "$fun" "$encodeopts"

    logn "comparing images: "
    if [ -n "$oracle2" -a -f "$ORACLE_PATH/$f.$ext.checksum_2" ]; then
      $GDALINFO $f.$ext | grep 'Checksum' > $f.$ext.result
      diff $ORACLE_PATH/$f.$ext.checksum_2 $f.$ext.result > /dev/null
    elif [ -f "$ORACLE_PATH/$f.$ext.checksum" ]; then
      $GDALINFO $f.$ext | grep 'Checksum' > $f.$ext.result
      diff $ORACLE_PATH/$f.$ext.checksum $f.$ext.result > /dev/null
    else
      cmp $TESTDATA_PATH/$f.$ext $f.$ext > /dev/null
    fi
    rc=$?
    check_result 0 $rc "input and output match"
    #[ $rc -ne 0 ] && exit 1

    drop_colls test_tmp
    rm -f $f*
}


function run_csv_test()
{
    local colltype=$1
    local f=$2
    local decodeopts=$3   # used on data insertion

    log ----- $fun and $inv_fun $colltype conversion ------

    create_coll test_tmp $colltype
    insert_into test_tmp "$TESTDATA_PATH/$f.csv" "$decodeopts" "inv_csv"
    export_to_file test_tmp "$f" "csv"

    logn "comparing images: "
    cmp $ORACLE_PATH/$f.csv $f.csv > /dev/null
    check_result 0 $? "input and output match"

    drop_colls test_tmp
    rm -f $f*
}

################################################################################
#
#                               TESTS start below
#
################################################################################


################## test nodata ###############################

create_coll test_tmp RGBSet
insert_into test_tmp "$TESTDATA_PATH/rgb.png" "" "decode"

$RASQL -q 'select encode(c, "GTiff") from test_tmp as c' --out file --outfile nodata > /dev/null
res=`gdalinfo nodata.tif | grep "NoData Value=0" | wc -l`
check_result 0 $res "default nodata test"
rm -f nodata*

$RASQL -q 'select encode(c, "GTiff", "nodata=200") from test_tmp as c' --out file --outfile nodata > /dev/null
res=`gdalinfo nodata.tif | grep "NoData Value=200" | wc -l`
check_result 3 $res "custom nodata test"
rm -f nodata*

$RASQL -q 'select encode(c, "GTiff", "{ \"nodata\": 200 }") from test_tmp as c' --out file --outfile nodata > /dev/null
res=`gdalinfo nodata.tif | grep "NoData Value=200" | wc -l`
check_result 3 $res "custom nodata test"
rm -f nodata*

$RASQL -q 'select encode(c, "tiff") from test_tmp as c' --out file --outfile nodata > /dev/null
check_result 0 $? "default tiff test"
rm -f nodata*

################## test invalid json ###############################

$RASQL -q 'select encode(c, "tiff", "{ \"nodata\": 200:201 }") from test_tmp as c' --out file --outfile nodata > /dev/null 2>&1
check_result 255 $? "invalid json test"
rm -f nodata*

################## test georeference ###############################

$RASQL -q 'select encode(c, "GTiff", "{ \"nodata\": [200,201,202], \"geoReference\": { \"bbox\": { \"xmin\": 0.5, \"xmax\": 30, \"ymin\": -15, \"ymax\": 50.3}, \"crs\": \"EPSG:4326\" }, \"metadata\": \"metadata test\" }") from test_tmp as c' --out file --outfile geo > /dev/null
res=`gdalinfo geo.tif | grep "4326" | wc -l`
check_result 1 $res "test crs in encode"
res=`gdalinfo geo.tif | grep "0.5" | wc -l`
check_result 3 $res "test xmin in encode"
res=`gdalinfo geo.tif | grep "30.0" | wc -l`
check_result 3 $res "test xmax in encode"
res=`gdalinfo geo.tif | grep "\-15.00" | wc -l`
check_result 2 $res "test ymin in encode"
res=`gdalinfo geo.tif | grep "50.30" | wc -l`
check_result 2 $res "test ymax in encode"
res=`gdalinfo geo.tif | grep "metadata test" | wc -l`
check_result 1 $res "test metadata in encode"
res=`gdalinfo geo.tif | grep "200" | wc -l`
check_result 4 $res "test nodata values"
res=`gdalinfo geo.tif | grep "200 201 202" | wc -l`
check_result 1 $res "test nodata values"

rm -f geo*

drop_colls test_tmp

################## test color table, gcps ######################

create_coll test_tmp GreySet
insert_into test_tmp "$TESTDATA_PATH/mr_1.png" "" "decode"

$RASQL -q 'select encode(c, "GTiff", "{ \"nodata\": 0, \"geoReference\": { \"GCPs\": [ { \"pixel\": 0, \"info\": \"gcp left\", \"line\": 0, \"x\": 15.5, \"y\": 12.3 } ], \"crs\": \"EPSG:4326\" }, \"colorPalette\": { \"paletteInterp\": \"RGB\", \"colorTable\": [[255,0,0], [255,0,0]], \"colorInterp\": [ \"Red\" ] } }") from test_tmp as c' --out file --outfile geo > /dev/null
res=`gdalinfo geo.tif | grep "4326" | wc -l`
check_result 1 $res "test gcp crs in encode"
res=`gdalinfo geo.tif | grep "Id=1, Info=" | wc -l`
check_result 1 $res "test gcp id and info in encode"
res=`gdalinfo geo.tif | grep '(0,0) -> (15.5,12.3,0)' | wc -l`
check_result 1 $res "test gcp ref in encode"
res=`gdalinfo geo.tif | grep 'Color Table (RGB with 256 entries)' | wc -l`
check_result 1 $res "test color table in encode"
res=`gdalinfo geo.tif | grep "255,0,0" | wc -l`
check_result 2 $res "test color entries in encode"

rm -f geo*

drop_colls test_tmp

################## jpeg() and inv_jpeg() #######################
if [ "$HAVE_JPEG" = ON ]; then
run_test jpeg inv_jpeg jpg jpg GreySet
run_test jpeg decode jpg jpg GreySet
run_test jpeg decode jpg jpg GreySet ", \"jpeg2000\", \"QUALITY=10;\""
run_test jpeg decode jpg jpg GreySet ', "jpeg2000", "{ \"formatParameters\": { \"QUALITY\": \"10\" } }"'
fi


################## tiff() and inv_tiff() #######################
if [ "$HAVE_TIFF" = ON ]; then
run_test tiff inv_tiff tif tif GreySet
run_test tiff inv_tiff tif tif TestSet ", \"sampletype=octet\""
run_test tiff decode tif tif GreySet
run_test tiff decode tif tif GreySet ", \"gtiff\", \"ZLEVEL=1;\""
run_test tiff decode tif tif GreySet ', "gtiff", "{ \"formatParameters\": { \"ZLEVEL\": \"1\" } }"'
run_test tiff decode tif tif BoolSet
run_test tiff decode tif tif ULongSet
run_test tiff decode tif tif ShortSet
run_test tiff decode tif tif UShortSet
run_test tiff decode tif tif LongSet
run_test tiff decode tif tif FloatSet
run_test tiff decode tif tif DoubleSet
fi

run_test encode decode tif tif CInt16Set '' ', "tiff"'
run_test encode decode tif tif CInt32Set '' ', "tiff"'
run_test encode decode tif tif Gauss1Set '' ', "tiff"'
run_test encode decode tif tif Gauss2Set '' ', "tiff"'


################## (TestArray) inv_tiff() ####################
log ----- user-defined type conversion ------

create_coll test_tmp TestSet

if [ "$HAVE_TIFF" = ON ]; then
$RASQL -q 'insert into test_tmp values (TestPixel) inv_tiff($1)' -f "$TESTDATA_PATH/multiband.tif" > /dev/null
check_result 0 $? "user-defined base type cast inv_tiff test"
$RASQL -q 'select inv_tiff($1)' -f "$TESTDATA_PATH/multiband.tif" > /dev/null
check_result 0 $? "user-defined base type inv_tiff test."
fi

$RASQL -q 'insert into test_tmp values (TestPixel) decode($1)' -f "$TESTDATA_PATH/multiband.tif" > /dev/null
check_result 0 $? "user-defined base type cast decode test."

drop_colls test_tmp

################## png() and transpose #######################
run_transpose_test

################## png() and inv_png() #######################
if [ "$HAVE_PNG" = ON ]; then
run_test png inv_png png png GreySet
run_test png decode png png GreySet
run_test png decode png png GreySet ", \"png\", \"ZLEVEL=1;\""
fi

################## bmp() and inv_bmp() #######################
if [ "$HAVE_BMP" = ON ]; then
run_test bmp inv_bmp bmp bmp GreySet "" "" "" "_2"
run_test bmp decode bmp bmp GreySet "" "" "" "_2"
fi

################## hdf() and inv_hdf() #######################

# run hdf test only if hdf was compiled in
if [ "$HAVE_HDF" = ON ]; then
run_test hdf inv_hdf hdf hdf GreySet
fi

############### export large data (ticket 240) ###############

if [ "$HAVE_TIFF" = ON ]; then

log "----- export large data  conversion test ------"
COLL=test_large
TYPE=GreySet
f=rasql_1.tif

drop_colls $COLL
create_coll $COLL $TYPE
$RASQL -q "insert into $COLL values marray i in [0:999,0:9999] values 2c" > /dev/null
$RASQL -q 'select tiff(c) from test_large as c' --out file --quiet
if [ -f $f ]; then
  output=`file $f`
  if [ "$output" == "rasql_1.tif: data" ]; then
    log "exporting large format-encoded data test failed."
    NUM_FAIL=$(($NUM_FAIL + 1))
  else
    log "exporting large format-encoded data test passed."
    NUM_SUC=$(($NUM_SUC + 1))
  fi
else
  log "exporting large format-encoded data test failed."
  NUM_FAIL=$(($NUM_FAIL + 1))
fi
NUM_TOTAL=$(($NUM_TOTAL + 1))
rm -f $f
drop_colls $COLL

fi # HAVE_TIFF

################ GML in JPEG2000 encoding ####################

log '------ GML in JPEG2000 conversion --------'
# JP2OpenJPEG supports GML box from GDAL 1.10: check if gdal library can support this format (in common.sh)
FORMAT_CODE="JP2OpenJPEG"
check_jpeg2000_enabled
if [ $? -ne 0 ]
then
    log "skipping test for GMLJP2 encoding: $FORMAT_CODE is not enabled (see \`$GDALINFO --formats\`)."
elif [ -f "/etc/centos-release" ]; then
    log "skipping test for GMLJP2 encoding on CentOS."
else
    # params:
            CODEC="jp2" # J2K does not foresee GML
        TEST_FILE="${TESTDATA_PATH}/mr_1.jpg"
         GML_FILE="${TESTDATA_PATH}/mr_1.gml"
    ORACLE_GMLJP2="${TESTDATA_PATH}/mr_1.jp2" # do not modify this
        COLL_NAME="mr4gmljp2"
       OUT_GMLJP2="gmljp2.jp2" # .jp2 appended by RasQL
    # insert/retrieve
    create_coll "$COLL_NAME" GreySet
    insert_into "$COLL_NAME" "$TESTDATA_PATH/mr_1.png" "" "inv_png"
    $RASQL -q "select encode(c, \"${FORMAT_CODE}\", \
                 \"xmin=0;xmax=666;ymin=0;ymax=999;crs=EPSG:2000;\
                   CODEC=${CODEC};\
                   config=GMLJP2OVERRIDE ${GML_FILE}\") \
               from $COLL_NAME AS c" --out file --outfile ${OUT_GMLJP2%.*} > /dev/null

    # compare/register
    cmp $OUT_GMLJP2 $ORACLE_GMLJP2 > /dev/null 2>&1
    #check_result 0 $? "input and output match"

    # cleanup
    drop_colls "$COLL_NAME"
    rm -f $OUT_GMLJP2
fi


################## csv() #######################

run_test csv inv_png csv png GreySet
run_test csv inv_png csv png RGBSet
run_test csv decode csv png GreySet
run_test csv decode csv png RGBSet
if [ "$HAVE_PNG" = ON ]; then
run_test encode inv_png csv png GreySet '' ', "csv"'
fi


run_csv_test FloatSet1 csv_float1 ",\"domain=[0:5];basetype=float\""
run_csv_test FloatSet csv_float2 ",\"domain=[0:2,1:3];basetype=float\""
run_csv_test GreySet3 csv_char3 ",\"domain=[0:1,1:2,2:3];basetype=char\""
run_csv_test FloatSet3 csv_float3 ",\"domain=[0:2,1:2,4:6];basetype=float\""
run_csv_test ShortSet3 csv_short3 ",\"domain=[-1:1,0:0,4:7];basetype=short\""
run_csv_test ULongSet3 csv_ulong3 ",\"domain=[1:3,2:3,-3:-2];basetype=ulong\""
#run_csv_test DoubleSet3 csv_double3 ",\"domain=[100:102,105:106,-100:-99];basetype=double\""
run_csv_test UShortSet3 csv_ushort3 ",\"domain=[1:3,0:2,4:6];basetype=ushort\""
run_csv_test LongSet3 csv_long3 ",\"domain=[0:0,1:1,2:2];basetype=long\""
run_csv_test RGBSet csv_rgb2 ",\"domain=[0:0,0:1];basetype=struct{char red, char green, char blue}\""
run_csv_test RGBSet3 csv_rgb3 ",\"domain=[0:1,0:1,0:1];basetype=struct{char red, char green, char blue}\""
run_csv_test TestFLSet csv_testfl2 ",\"domain=[0:1,0:2];basetype=struct{ float f, long l }\""
run_csv_test TestFLSet csv_testfl2 ',"{ \"formatParameters\": { \"domain\": \"[0:1,0:2]\", \"basetype\": \"struct{ float f, long l }\" } }"'

run_csv_scalar_test

############## csv(order=inner_outer) #######
log ----- csv with inner_outer order conversion ------
create_coll test_tmp GreySet
insert_into test_tmp "$TESTDATA_PATH/mr_1.png" "" "decode"
export_to_file test_tmp "mr_1" "csv" ', "order=inner_outer"'

logn "comparing images: "
cmp $TESTDATA_PATH/mr_1_inner_outer.csv mr_1.csv > /dev/null
check_result 0 $? "input and output match"

export_to_file test_tmp "mr_1" "csv" ', "{ \"formatParameters\": { \"order\": \"inner_outer\" } }"'

logn "comparing images: "
cmp $TESTDATA_PATH/mr_1_inner_outer.csv mr_1.csv > /dev/null
check_result 0 $? "input and output match"

drop_colls test_tmp
rm -f mr_1.csv

############## csv(order=invalid_order) #######
log ------- csv with invalid_order conversion --------
create_coll test_tmp GreySet
insert_into test_tmp "$TESTDATA_PATH/mr_1.png" "" "decode"
$RASQL --quiet -q "select csv(c, \"order=invalid_order\") from test_tmp as c" --out file --outfile "csv" 2>&1 | grep -F -q "242"
check_result 0 $? "invalid_order"

rm -f mr_1.csv

############## csv(transpose) #######
log ----------------- csv transpose -----------------
$RASQL --quiet -q 'select encode(c[0:9,40:49], "csv", "{ \"transpose\": [0,1] }") from test_tmp as c' --out file --outfile "csv_transpose"
check_result 0 $? "csv(transpose)"

logn "comparing images: "
cmp $TESTDATA_PATH/csv_transpose.csv csv_transpose.csv > /dev/null
check_result 0 $? "input and output match"

drop_colls test_tmp
rm -f csv_transpose.csv

############ built-in decode ################
log ----- built-in decode test -----------------------
drop_colls test_builtin_decode
create_coll test_builtin_decode FloatSet3
f=csv_float3
$RASQL -q 'insert into test_builtin_decode values decode($1, "CSV", "domain=[0:2,1:2,4:6];basetype=float")' -f $TESTDATA_PATH/$f.csv > /dev/null
export_to_file test_builtin_decode "$f" "csv"
cmp $ORACLE_PATH/$f.csv $f.csv > /dev/null
check_result 0 $? "input and output match"
rm -f $f*
drop_colls test_builtin_decode

############# json ##########################
log ----- json conversion ----------------------------
f=json_float3
create_coll test_tmp "FloatSet3"
insert_into test_tmp "$TESTDATA_PATH/$f.json" ", \"json\", \"domain=[0:2,1:2,4:6];basetype=float\"" "decode"
export_to_file test_tmp "$f" "encode" ', "json"'
logn "comparing images: "
cmp $ORACLE_PATH/$f.json $f.json > /dev/null
check_result 0 $? "input and output match"
rm -f $f*

export_to_file test_tmp "$f" "encode" ', "json", ""'
logn "comparing images: "
cmp $ORACLE_PATH/$f.json $f.json > /dev/null
check_result 0 $? "input and output match"
rm -f $f*

drop_colls test_tmp

# ------------------------------------------------------------------------------
# test summary
#
print_summary

exit $RC
