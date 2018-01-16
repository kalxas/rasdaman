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

  local values="$inv_fun(\$1 $extraopts)"
  if [ -z "$inv_fun" ]; then
    values="\$1"
  fi

  logn "inserting data... "
  #echo "insert into $coll_name values $values, file: $file_name"
  $PY_RASQL -q "insert into $coll_name values $values" -f $file_name $PY_RASQL_opts > /dev/null
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
  $PY_RASQL -q "select $values from $coll_name as c" --out file --outfile $file_name > /dev/null
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
  if [ ! -f "$TESTDATA_PATH/mr_1.png" ]; then
    error "testdata file $TESTDATA_PATH/mr_1.png not found"
  fi
  if [ ! -f "$TESTDATA_PATH/rgb.png" ]; then
    error "testdata file $TESTDATA_PATH/rgb.png not found"
  fi
  if [ ! -f "$TESTDATA_PATH/mr2_1.png" ]; then
    error "testdata file $TESTDATA_PATH/mr2_1.png not found"
  fi
  if [ ! -f "$TESTDATA_PATH/50k.bin" ]; then
    error "testdata file $TESTDATA_PATH/50k.bin not found"
  fi
  if [ ! -f "$TESTDATA_PATH/23k.bin" ]; then
    error "testdata file $TESTDATA_PATH/23k.bin not found"
  fi
  
  # check data types
  check_type GreySet
  check_type GreySet3
  check_type RGBSet
  
  py_drop_colls $TEST_GREY $TEST_GREY2 $TEST_RGB2 $TEST_GREY3D $TEST_GREY4D $TEST_STRUCT

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
  py_insert_into $TEST_GREY "$TESTDATA_PATH/mr_1.png" "" "decode"

  py_create_coll $TEST_GREY2 GreySet
  py_insert_into $TEST_GREY2 "$TESTDATA_PATH/mr2_1.png" "" "decode"

  py_create_coll $TEST_RGB2 RGBSet
  py_insert_into $TEST_RGB2 "$TESTDATA_PATH/rgb.png" "" "decode"

  py_create_coll $TEST_GREY3D GreySet3
  $PY_RASQL -q "insert into $TEST_GREY3D values \$1" -f "$TESTDATA_PATH/50k.bin" --mdddomain "[0:99,0:99,0:4]" --mddtype GreyCube > /dev/null
}






