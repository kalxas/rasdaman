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

  local values="$inv_fun(\$1 $extraopts)"
  if [ -z "$inv_fun" ]; then
    values="\$1"
  fi

  logn "inserting data... "
  $RASQL --quiet -q "insert into $coll_name values $values" -f $file_name > /dev/null
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

  local values="$fun(c)"
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
  
  # check data types
  check_type GreySet
  check_type RGBSet
  
  drop_colls $TEST_GREY $TEST_GREY2 $TEST_RGB2

  create_coll $TEST_GREY GreySet
  insert_into $TEST_GREY "$TESTDATA_PATH/mr_1.png" "" "inv_png"

  create_coll $TEST_GREY2 GreySet
  insert_into $TEST_GREY2 "$TESTDATA_PATH/mr2_1.png" "" "inv_png"

  create_coll $TEST_RGB2 RGBSet
  insert_into $TEST_RGB2 "$TESTDATA_PATH/rgb.png" "" "inv_png"
}
