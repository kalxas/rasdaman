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
# ------------------------------------------------------------------------------
#
# SYNOPSIS
#  rasql.sh
# Description
#  Common functionality pertaining to running rasql queries
#
################################################################################



# ------------------------------------------------------------------------------
# execute a rasql query by taking into account rasdaman availability. The rasql
# query is not passed directly here, as we can never account for all the
# possible cases. Instead, it's wrapped into a function, and the function name
# is passed here.
#
# This function will execute the function retrying maximum 5 times until the
# called function returns a 0.
#
# arg 1: function name to execute
run_query()
{
  local func="$1"

  local rc=1
  local times=0
  while [ $rc -ne 0 ]; do

    # repeat a failing query maximum 5 times
    if [ $times -gt 5 ]; then
      echo "failed importing to rasdaman"
      break
    fi

    # execute function
    $func
    rc=$?

    if [ $rc -ne 0 ]; then
      times=$(($times + 1))
      echo ""
      logn "failed, repeating $times... "
    fi
  done
  return $rc
}


# ------------------------------------------------------------------------------
# check if collection exists in rasdaman
# arg 1: collection name
# return 0 if found in rasdaman, non-zero otherwise
#
function check_coll()
{
  local coll_name="$1"
  $RASQL -q 'select r from RAS_COLLECTIONNAMES as r' --out string | egrep "\b$coll_name\b" > /dev/null
}

# ------------------------------------------------------------------------------
# check if collection is empty
# arg 1: collection name
# return 0 if empty, 1 otherwise
#
function is_coll_empty()
{
  local coll_name="$1"
  $RASQL -q "select oid(r) from $coll_name as r" --out string | egrep "\bQuery result collection has 0 element\b" > /dev/null
}


# ------------------------------------------------------------------------------
# check user-defined types, if not present testdata/types.dl is read by rasdl.
# arg 1: set type name
#
function check_user_type()
{
  local SET_TYPE="$1"
  $RASDL -p | egrep --quiet  "\b$SET_TYPE\b"
  if [ $? -ne 0 ]; then
    $RASDL -r $TESTDATA_PATH/types.dl -i > /dev/null
  fi
}


# ------------------------------------------------------------------------------
# check user-defined types, if not present testdata/types.dl is read by rasdl.
# arg 1: set type name
#
function check_user_type_file()
{
  local SET_TYPE="$1"
  local TYPES_FILE="$2"
  $RASDL -p | egrep --quiet  "\b$SET_TYPE\b"
  if [ $? -ne 0 ]; then
    $RASDL -r $TESTDATA_PATH/$TYPES_FILE -i > /dev/null
  fi
}


# ------------------------------------------------------------------------------
# check built-in types, if not present error is thrown
# arg 1: set type name
#
function check_type()
{
  SET_TYPE="$1"
  $RASDL -p | egrep --quiet  "\b$SET_TYPE\b"
  if [ $? -ne 0 ]; then
    error "rasdaman basic type $SET_TYPE not found, please insert with rasdl first."
  fi
}


# ------------------------------------------------------------------------------
# drop collections passed as argument
#
function drop_colls()
{
  check_rasdaman
  for c in $*; do
    $RASQL -q 'select r from RAS_COLLECTIONNAMES as r' --out string | egrep "\b$c\b" > /dev/null
    if [ $? -eq 0 ]; then
      $RASQL -q "drop collection $c" > /dev/null
    fi
  done
}


# ------------------------------------------------------------------------------
# drop types
# arg 1: set type
# arg 2: mdd type
# arg 3: pixel type, can be empty if there's none
#
function drop_types()
{
  $RASDL --delsettype "$1" > /dev/null
  $RASDL --delmddtype "$2" > /dev/null
  [ -n "$3" ] && $RASDL --delbasetype "$3" > /dev/null
}


# ------------------------------------------------------------------------------
# insert data into collection
# arg 1: coll name
# arg 2: file name
# arg 3: extra conversion options
# arg 4: conversion function
#
function insert_into()
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
  $RASQL --quiet -q "insert into $coll_name values $values" -f $file_name $rasql_opts > /dev/null
  feedback
}


# ------------------------------------------------------------------------------
# select data from collection
# arg 1: coll name
# arg 2: file name
# arg 3: conversion function
#
function export_to_file()
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
  $RASQL --quiet -q "select $values from $coll_name as c" --out file --outfile $file_name > /dev/null
  feedback
}


# ------------------------------------------------------------------------------
# create rasdaman collection
# arg 1: coll name
# arg 2: coll type
#
function create_coll()
{
  local coll_name="$1"
  local coll_type="$2"
  logn "creating collection... "
  $RASQL --quiet -q "create collection $coll_name $coll_type" > /dev/null
  feedback
}


#
# import data used in rasql tests. Expects arguments
# $1 - testdata dir holding files to be imported
#
function import_rasql_data()
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
  
  # check data types
  check_type GreySet
  check_type GreySet3
  check_type RGBSet
  
  drop_colls $TEST_GREY $TEST_GREY2 $TEST_RGB2 $TEST_GREY3D

  create_coll $TEST_GREY GreySet
  insert_into $TEST_GREY "$TESTDATA_PATH/mr_1.png" "" "decode"

  create_coll $TEST_GREY2 GreySet
  insert_into $TEST_GREY2 "$TESTDATA_PATH/mr2_1.png" "" "decode"

  create_coll $TEST_RGB2 RGBSet
  insert_into $TEST_RGB2 "$TESTDATA_PATH/rgb.png" "" "decode"

  create_coll $TEST_GREY3D GreySet3
  $RASQL -q "insert into $TEST_GREY3D values \$1" -f "$TESTDATA_PATH/50k.bin" --mdddomain "[0:99,0:99,0:4]" --mddtype GreyCube > /dev/null
}


#
# import data used in rasql tests. Expects arguments
# $1 - testdata dir holding files to be imported
#
function import_nullvalues_data()
{
  local TESTDATA_PATH="$1"
  if [ ! -d "$TESTDATA_PATH" ]; then
    error "testdata path $TESTDATA_PATH not found."
  fi
  if [ ! -f "$TESTDATA_PATH/types.dl" ]; then
    error "testdata file $TESTDATA_PATH/types.dl not found"
  fi

  local mdd_type=NullValueArrayTest
  local set_type=NullValueSetTest

  # check data types and insert if not available
  check_user_type $set_type

  drop_colls $TEST_NULL

  create_coll $TEST_NULL $set_type
  $RASQL -q "insert into $TEST_NULL values marray x in [0:3,0:3] values (char)(x[0] + x[1] + 1)" | tee -a $LOG
}

#
# drop null values test data, including imported null types
#
drop_nullvalues_data()
{
  drop_colls $TEST_NULL
  drop_types NullValueSetTest NullValueArrayTest
}
