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
#	1)creating collection
# 	2)insert images into collection
# 	3)extract images
# 	4)compare
# 	5)cleanup
#
# PRECONDITIONS
# 	1)Postgres Server must be running
# 	2)Rasdaman Server must be running
# 	3)database RASBASE must exists
# 	4)rasql utility must be fully running
# 	5)images needed for testing shall be put in directory of images
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
check_type Gauss2Set
check_user_type TestSet



# ------------------------------------------------------------------------------
# drop collection if they already exists
#
drop_colls test_tmp

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
elif [ "$colltype" == Gauss2Set ]; then
  f=gauss
fi
local decodeopts="$6"   # used on data insertion
local encodeopts="$7"   # used on data selection
local rasqlopts="$8"

log ----- $fun and $inv_fun conversion ------

create_coll test_tmp $colltype
insert_into test_tmp "$TESTDATA_PATH/$f.$inv_ext" "$decodeopts" "$inv_fun" "$rasqlopts"
export_to_file test_tmp "$f" "$fun" "$encodeopts"

logn "comparing images: "
if [ -f "$ORACLE_PATH/$f.$ext.checksum" ]; then
  $GDALINFO $f.$ext | grep 'Checksum' > $f.$ext.result
  diff $ORACLE_PATH/$f.$ext.checksum $f.$ext.result > /dev/null
else
  cmp $TESTDATA_PATH/$f.$ext $f.$ext > /dev/null
fi

if [ $? != "0" ]
then
  echo input and output do not match
  NUM_FAIL=$(($NUM_FAIL + 1))
else
  echo input and output match
  NUM_SUC=$(($NUM_SUC + 1))
fi
NUM_TOTAL=$(($NUM_TOTAL + 1))

drop_colls test_tmp
rm -f $f*
}

################## test nodata ###############################

create_coll test_tmp RGBSet
insert_into test_tmp "$TESTDATA_PATH/rgb.png" "" "decode"

$RASQL -q 'select encode(c, "GTiff") from test_tmp as c' --out file --outfile nodata > /dev/null
res=`gdalinfo nodata.tif | grep "NoData Value=0" | wc -l`
if [ $res -eq 3 ]; then
  log "default nodata value test failed."
  NUM_FAIL=$(($NUM_FAIL + 1))
else
  log "default nodata value test passed."
  NUM_SUC=$(($NUM_SUC + 1))
fi
NUM_TOTAL=$(($NUM_TOTAL + 1))
rm -f nodata*
$RASQL -q 'select encode(c, "GTiff", "nodata=200") from test_tmp as c' --out file --outfile nodata > /dev/null
res=`gdalinfo nodata.tif | grep "NoData Value=200" | wc -l`
if [ $res -eq 3 ]; then
  log "custom nodata value test passed."
  NUM_SUC=$(($NUM_SUC + 1))
else
  log "custom nodata value test failed."
  NUM_FAIL=$(($NUM_FAIL + 1))
fi
NUM_TOTAL=$(($NUM_TOTAL + 1))
rm -f nodata*

drop_colls test_tmp

################## jpeg() and inv_jpeg() #######################
run_test jpeg inv_jpeg jpg jpg GreySet
run_test jpeg decode jpg jpg GreySet
run_test jpeg decode jpg jpg GreySet ", \"jpeg2000\", \"QUALITY=10;\""

################## tiff() and inv_tiff() #######################
run_test tiff inv_tiff tif tif GreySet
run_test tiff inv_tiff tif tif TestSet ", \"sampletype=octet\""
run_test tiff decode tif tif GreySet
run_test tiff decode tif tif GreySet ", \"gtiff\", \"ZLEVEL=1;\""

################## (TestArray) inv_tiff() ####################
log ----- user-defined type conversion ------

create_coll test_tmp TestSet
$RASQL -q 'insert into test_tmp values (TestPixel) inv_tiff($1)' -f "$TESTDATA_PATH/multiband.tif" > /dev/null
if [ $? -eq 0 ]; then
  log "user-defined base type cast inv_tiff test passed."
  NUM_SUC=$(($NUM_SUC + 1))
else
  log "user-defined base type cast inv_tiff test failed."
  NUM_FAIL=$(($NUM_FAIL + 1))
fi
NUM_TOTAL=$(($NUM_TOTAL + 1))
$RASQL -q 'insert into test_tmp values (TestPixel) decode($1)' -f "$TESTDATA_PATH/multiband.tif" > /dev/null
if [ $? -eq 0 ]; then
  log "user-defined base type cast decode test passed."
  NUM_SUC=$(($NUM_SUC + 1))
else
  log "user-defined base type cast decode test failed."
  NUM_FAIL=$(($NUM_FAIL + 1))
fi
NUM_TOTAL=$(($NUM_TOTAL + 1))
$RASQL -q 'select inv_tiff($1)' -f "$TESTDATA_PATH/multiband.tif" > /dev/null
if [ $? -eq 0 ]; then
  log "user-defined base type inv_tiff test passed."
  NUM_SUC=$(($NUM_SUC + 1))
else
  log "user-defined base type inv_tiff test failed."
  NUM_FAIL=$(($NUM_FAIL + 1))
fi
NUM_TOTAL=$(($NUM_TOTAL + 1))
drop_colls test_tmp

################## png() and inv_png() #######################
run_test png inv_png png png GreySet
run_test png decode png png GreySet
run_test png decode png png GreySet ", \"png\", \"ZLEVEL=1;\""

################## bmp() and inv_bmp() #######################
run_test bmp inv_bmp bmp bmp GreySet
run_test bmp decode bmp bmp GreySet

################## vff() and inv_vff() #######################
run_test vff inv_vff vff vff GreySet

################## hdf() and inv_hdf() #######################

# run hdf test only if hdf was compiled in
grep 'HAVE_HDF 1' $SCRIPT_DIR/../../../config.h

if [ $? -eq 0 ]; then
  run_test hdf inv_hdf hdf hdf GreySet
fi

################ GML in JPEG2000 encoding ####################

log '------ GML in JPEG2000 conversion --------'
# JP2OpenJPEG supports GML box from GDAL 1.10: check GDAL version
    GDALINFO="$( which gdalinfo )"
GDAL_VERSION="$( $GDALINFO --version | awk -F ' |,' '{ print $2 }' | grep -o -e '.[0-9.]\+' )" # M.m version
 FORMAT_CODE="JP2OpenJPEG"
GDAL_VERSION_MAJOR=$(echo $GDAL_VERSION | awk -F '.' '{ print $1; }')
GDAL_VERSION_MINOR=$(echo $GDAL_VERSION | awk -F '.' '{ print $2; }')
if [ $GDAL_VERSION_MAJOR -eq 1 -a $GDAL_VERSION_MINOR -lt 10 ]
then
    log "skipping test for GMLJP2 encoding: GDAL 1.10 required (found GDAL $GDAL_VERSION)."
elif [ -z "$( $GDALINFO --formats | grep $FORMAT_CODE )" ]
then
    log "skipping test for GMLJP2 encoding: $FORMAT_CODE is not enabled (see \`$GDALINFO --formats\`)."
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
    cmp $OUT_GMLJP2 $ORACLE_GMLJP2 > /dev/null
    if [ $? != 0 ]
    then
        echo input and output do not match
        NUM_FAIL=$(($NUM_FAIL + 1))
    else
        echo input and output match
        NUM_SUC=$(($NUM_SUC + 1))
    fi

    # cleanup
    drop_colls "$COLL_NAME"
    rm $OUT_GMLJP2
fi
NUM_TOTAL=$(($NUM_TOTAL + 1))


################## csv() #######################

run_test csv inv_png csv png GreySet
run_test csv inv_png csv png RGBSet
run_test csv decode csv png GreySet
run_test csv decode csv png RGBSet
run_test encode inv_png csv png GreySet '' ', "csv"'

run_test csv "" csv binary Gauss2Set "" "" "--mddtype Gauss2Image --mdddomain [0:1,-1:1]"

############## csv(order=inner_outer) #######
log ----- csv with inner_outer order conversion ------
create_coll test_tmp GreySet
insert_into test_tmp "$TESTDATA_PATH/mr_1.png" "" "inv_png"
export_to_file test_tmp "mr_1" "csv" ', "order=inner_outer"'
logn "comparing images: "
cmp $TESTDATA_PATH/mr_1_inner_outer.csv mr_1.csv > /dev/null
if [ $? != "0" ]
then
  echo input and output do not match
  NUM_FAIL=$(($NUM_FAIL + 1))
else
  echo input and output match
  NUM_SUC=$(($NUM_SUC + 1))
fi
NUM_TOTAL=$(($NUM_TOTAL + 1))
drop_colls test_tmp
rm -f mr_1.csv

# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
