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
# Copyright 2003 - 2018 Peter Baumann /
# rasdaman GmbH.
# SYNOPSIS
#	test.sh
# Description
#   Test create, insert (update), delete rasdaman collections via RasdaPy API (systemtest/testcases_mandatory/test_rasdapy/rasql.py)
#   It is called in systemtest/util/test_oracle.sh for importing data and is imported in systesmtest/util/common.sh to run queries.
PROG=`basename $0`

# ------------------------------------------------------------------------------
# check if collection exists in rasdaman
# arg 1: collection name
# return 0 if found in rasdaman, non-zero otherwise
#
function py_check_coll()
{
  local coll_name="$1"
  $PY_RASQL -q 'select r from RAS_COLLECTIONNAMES as r' --out string | egrep "\b$coll_name\b" > /dev/null
}
# ------------------------------------------------------------------------------
# check built-in types, if not present error is thrown
# arg 1: set type name
#
function py_check_type()
{
  SET_TYPE="$1"
  $PY_RASQL -q "select c from RAS_SET_TYPES as c" --out string | egrep --quiet  "\b$SET_TYPE\b" \
    || error "rasdaman type $SET_TYPE not found, please create it first."
}

# ------------------------------------------------------------------------------
# check if collection is empty
# arg 1: collection name
# return 0 if empty, 1 otherwise
#
function py_is_coll_empty()
{
  local coll_name="$1"
  $PY_RASQL -q "select oid(r) from $coll_name as r" --out string | egrep "\bQuery result collection has 0 element\b" > /dev/null
}
# ------------------------------------------------------------------------------
# drop types
# arg 1: set type
# arg 2: mdd type
# arg 3: pixel type, can be empty if there's none
#
function py_drop_types()
{
  $PY_RASQL "drop type $1" > /dev/null
  $PY_RASQL "drop type $2" > /dev/null
  [ -n "$3" ] && $PY_RASQL "drop type $3" > /dev/null
}


# ------------------------------------------------------------------------------
# drop collections passed as argument
#
function py_drop_colls()
{
  check_rasdaman
  for c in $*; do
    $PY_RASQL -q 'select r from RAS_COLLECTIONNAMES as r' --out string | egrep "\b$c\b" > /dev/null
    if [ $? -eq 0 ]; then
      $PY_RASQL -q "drop collection $c" > /dev/null
    fi
  done
}

# ------------------------------------------------------------------------------
# insert data into collection
# arg 1: coll name
# arg 2: file name
# arg 3: extra conversion options
# arg 4: conversion function
#
function py_insert_into()
{
  local coll_name="$1"
  local file_name="$2"
  local extraopts="$3"
  local inv_fun="$4"
  local rasql_opts=$5
  local tiling="$6"

  local values="$inv_fun(\$1 $extraopts)"
  if [ -z "$inv_fun" ]; then
    values="\$1"
  fi

  logn "inserting data... "
  #echo "insert into $coll_name values $values, file: $file_name"
  $PY_RASQL -q "insert into $coll_name values $values $tiling" -f $file_name $rasql_opts > /dev/null
  feedback
}


# ------------------------------------------------------------------------------
# update collection data
# arg 1: coll name
# arg 2: file name
# arg 3: extra conversion options
# arg 4: conversion function
# arg 5: target domain
# arg 6: shift point
#
function py_update()
{
  local coll_name="$1"
  local file_name="$2"
  local extraopts="$3"
  local inv_fun="$4"
  local target_domain="$5"
  local shift_point="$6"
  local values="$inv_fun(\$1 $extraopts)"
  if [ -z "$inv_fun" ]; then
    values="\$1"
  fi
  logn "updating $coll_name..."
  $PY_RASQL  -q "update $coll_name set $coll_name$target_domain assign shift($values,$shift_point)" -f $file_name > /dev/null
  feedback
}


# ------------------------------------------------------------------------------
# select data from collection
# arg 1: coll name
# arg 2: file name
# arg 3: conversion function
#
function py_export_to_file()
{
  local coll_name="$1"
  local file_name="$2"
  local fun="$3"
  local extraopts="$4"

  local values="$fun(c $extraopts)"
  if [ -z "$fun" ]; then
    values="c"
  fi

  logn "selecting data... "
  $
   -q "select $values from $coll_name as c" --out file --outfile $file_name > /dev/null
  feedback
}


# ------------------------------------------------------------------------------
# create rasdaman collection
# arg 1: coll name
# arg 2: coll type
#
function py_create_coll()
{
  local coll_name="$1"
  local coll_type="$2"
  logn "creating collection... "
  $PY_RASQL -q "create collection $coll_name $coll_type" > /dev/null
  feedback
}


#
# import data used in rasql tests. Expects arguments
# $1 - testdata dir holding files to be imported
#
function py_import_rasql_data()
{
  local TESTDATA_PATH="$1"
 
  if [ ! -d "$TESTDATA_PATH" ]; then
    error "testdata path $TESTDATA_PATH not found."
  fi
  for f in mr_1.png rgb.png mr2_1.png 50k.bin 23k.bin; do
    if [ ! -f "$TESTDATA_PATH/$f" ]; then
      error "testdata file $TESTDATA_PATH/$f not found"
    fi
  done
  
  # check data types
  py_check_type GreySet
  py_check_type GreySet3
  py_check_type RGBSet
  py_check_type Gauss2Set
  py_check_type Gauss1Set
  py_check_type CInt16Set
  py_check_type CInt32Set
  py_drop_colls $TEST_GREY $TEST_GREY2 $TEST_RGB2 $TEST_GREY3D $TEST_GREY4D $TEST_STRUCT
  py_drop_colls $TEST_CFLOAT32 $TEST_CFLOAT64 $TEST_CINT16 $TEST_CINT32
  #py_drop_colls $TEST_OVERLAP $TEST_OVERLAP_3D


  #create the struct_cube_set type
  $PY_RASQL -q "select c from RAS_SET_TYPES as c" --out string | egrep --quiet  "\bstruct_cube_set\b"
  if [ $? -ne 0 ]; then
    log "rasdaman type struct_cube_set not found, inserting..."
    $PY_RASQL -q "create type struct_pixel as ( x1 float, x2 double, x3 octet, x4 double, x5 short )" > /dev/null
    $PY_RASQL -q "create type struct_cube as struct_pixel mdarray [ x, y, z ]" > /dev/null
    $PY_RASQL -q "create type struct_cube_set as set ( struct_cube )" > /dev/null
  fi

  py_create_coll $TEST_STRUCT struct_cube_set
  $PY_RASQL -q "insert into $TEST_STRUCT values \$1" -f "$TESTDATA_PATH/23k.bin" --mdddomain "[0:99,0:9,0:0]" --mddtype struct_cube > /dev/null

  #create the GreySet4 type
  $PY_RASQL -q "select c from RAS_SET_TYPES as c" --out string | egrep --quiet  "\bGreySet4\b"
  if [ $? -ne 0 ]; then
    log "rasdaman type GreySet4 not found, inserting..."
    $PY_RASQL -q "create type GreyTesseract as char mdarray [ x0, x1, x2, x3 ]" > /dev/null
    $PY_RASQL -q "create type GreySet4 as set ( GreyTesseract )" > /dev/null
  fi

  py_create_coll $TEST_GREY4D GreySet4
  $PY_RASQL -q "insert into $TEST_GREY4D values \$1" -f "$TESTDATA_PATH/50k.bin" --mdddomain "[0:9,0:9,0:9,0:49]" --mddtype GreyTesseract > /dev/null

  py_create_coll $TEST_GREY GreySet
  py_create_coll $TEST_GREY2 GreySet
  py_create_coll $TEST_RGB2 RGBSet
  py_create_coll $TEST_GREY3D GreySet3
  py_create_coll $TEST_CFLOAT32 Gauss1Set
  py_create_coll $TEST_CFLOAT64 Gauss2Set
  py_create_coll $TEST_CINT16 CInt16Set
  py_create_coll $TEST_CINT32 CInt32Set
  #py_update doesn't work as expected. ticket 2269
  #py_create_coll $TEST_OVERLAP GreySet
  #py_create_coll $TEST_OVERLAP_3D GreySet3
  py_insert_into $TEST_GREY "$TESTDATA_PATH/mr_1.png" "" "decode" "" "tiling aligned [0:49,0:29] tile size 1500"
  py_insert_into $TEST_GREY2 "$TESTDATA_PATH/mr2_1.png" "" "decode" "" "tiling aligned [0:49,0:29] tile size 1500"
  py_insert_into $TEST_RGB2 "$TESTDATA_PATH/rgb.png" "" "decode" "" "tiling aligned [0:49,0:49] tile size 7500"
  py_insert_into $TEST_CFLOAT32 "$TESTDATA_PATH/cfloat32_image.tif" "" "decode"
  py_insert_into $TEST_CFLOAT64 "$TESTDATA_PATH/cfloat64_image.tif" "" "decode"
  py_insert_into $TEST_CINT16 "$TESTDATA_PATH/cint16_image.tif" "" "decode"
  py_insert_into $TEST_CINT32 "$TESTDATA_PATH/cint32_image.tif" "" "decode"
  #py_add_overlap_data
  $PY_RASQL -q "insert into $TEST_GREY3D values \$1" -f "$TESTDATA_PATH/50k.bin" --mdddomain "[0:99,0:99,0:4]" --mddtype GreyCube > /dev/null


  py_insert_into $TEST_CFLOAT32 "$TESTDATA_PATH/cfloat32_image.tif" "" "decode"
  py_insert_into $TEST_CFLOAT64 "$TESTDATA_PATH/cfloat64_image.tif" "" "decode"
  py_insert_into $TEST_CINT16 "$TESTDATA_PATH/cint16_image.tif" "" "decode"
  py_insert_into $TEST_CINT32 "$TESTDATA_PATH/cint32_image.tif" "" "decode"


  log "Importing error collection..."
  # Create a failed test to import data to collection and Rasdapy should not print binary error
  ERROR_COLLECTION="test_import_error"
  py_create_coll $ERROR_COLLECTION BoolSet
  py_insert_into $ERROR_COLLECTION "$TESTDATA_PATH/50k.bin" "" "decode" 2>&1 | grep 'Exception: Error executing query. Reason' > /dev/null
  check_result "0" $? "Checking expected error"
  py_drop_colls $ERROR_COLLECTION
}

#adds the necessary data to the $TEST_OVERLAP collection
function py_add_overlap_data()
{
  #2d
  py_insert_into $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "" "tiling aligned [0:59,0:59] tile size 3600"
  py_update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[0:255,211:421]" "[0,211]"
  py_update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[256:511,211:421]" "[256,211]"
  py_update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[256:511,0:210]" "[256,0]"
  py_update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[100:355,100:310]" "[100,100]"
  py_update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[200:455,-100:110]" "[200,-100]"
  py_update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[-100:155,50:260]" "[-100,50]"
  #3d
  $PY_RASQL -q "insert into $TEST_OVERLAP_3D values <[0:0,0:0,0:0] 0c> TILING ALIGNED [0:0,0:59,0:59] TILE SIZE 3600"> /dev/null
  py_update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,0:255,0:210]" "[0,0]"
  py_update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,0:255,211:421]" "[0,211]"
  py_update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,256:511,211:421]" "[256,211]"
  py_update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,256:511,0:210]" "[256,0]"
  py_update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,100:355,100:310]" "[100,100]"
  py_update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,200:455,-100:110]" "[200,-100]"
  py_update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,-100:155,50:260]" "[-100,50]"
}







