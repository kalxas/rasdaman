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
# check user-defined types
# arg 1: set type name
#
function check_user_type()
{
  local SET_TYPE="$1"
  $RASQL -q "select c from RAS_SET_TYPES as c" --out string | egrep --quiet "\b$SET_TYPE\b"
}


# ------------------------------------------------------------------------------
# check built-in types, if not present error is thrown
# arg 1: set type name
#
function check_type()
{
  SET_TYPE="$1"
  $RASQL -q "select c from RAS_SET_TYPES as c" --out string | egrep --quiet  "\b$SET_TYPE\b" \
    || error "rasdaman type $SET_TYPE not found, please create it first."
}


# ------------------------------------------------------------------------------
# drop collections passed as argument
#
function drop_colls()
{
  #check_rasdaman
  for c in "$@"; do
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
  local t=
  for t in "$@"; do
    $RASQL --quiet -q "drop type $t" > /dev/null 2>&1
  done
}


# ------------------------------------------------------------------------------
# insert data into collection
# arg 1: coll name
# arg 2: file name
# arg 3: extra conversion options
# arg 4: conversion function
# arg 5: rasql options
#
function insert_into()
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

  logn "inserting data into $coll_name... "
  $RASQL --quiet -q "insert into $coll_name values $values $tiling" -f $file_name $rasql_opts > /dev/null
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
function update()
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
  $RASQL --quiet -q "update $coll_name set $coll_name$target_domain assign shift($values,$shift_point)" -f $file_name > /dev/null
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

  logn "selecting data from $coll_name... "
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
  logn "creating collection $coll_name $coll_type... "
  $RASQL --quiet -q "create collection $coll_name $coll_type" > /dev/null
  feedback
}


#
# import data used in rasql tests. Expects arguments
# $1 - testdata dir holding files to be imported
# $2 - if not empty data is inserted insitu
#
function import_rasql_data()
{
  local TESTDATA_PATH="$1"
  local INSITU=
  local STORAGE_CLAUSE=
  if [ -n "$2" ]; then
    if [ "$2" == "insitu" ]; then
      INSITU="$2"
    else
      STORAGE_CLAUSE="$2"
    fi
  fi
  if [ ! -d "$TESTDATA_PATH" ]; then
    error "testdata path $TESTDATA_PATH not found."
  fi
  for f in mr_1.png rgb.png mr2_1.png 50k.bin 23k.bin; do
    if [ ! -f "$TESTDATA_PATH/$f" ]; then
      error "testdata file $TESTDATA_PATH/$f not found"
    fi
  done

  # check data types
  check_type GreySet
  check_type GreySet3
  check_type RGBSet
  check_type Gauss2Set
  check_type Gauss1Set
  check_type CInt16Set
  check_type CInt32Set
  check_type DoubleSet3
  check_type DoubleSet
  drop_colls $TEST_GREY $TEST_GREY2 $TEST_RGB2 $TEST_GREY3D $TEST_GREY4D $TEST_STRUCT
  drop_colls $TEST_CFLOAT32 $TEST_CFLOAT64 $TEST_CINT16 $TEST_CINT32
  drop_colls $TEST_OVERLAP $TEST_OVERLAP_3D test_oneD test_twoD test_threeD test_threeD_two_objects test_twoD_named

  # create the struct_cube_set type
  $RASQL -q "select c from RAS_SET_TYPES as c" --out string | egrep --quiet  "\bstruct_cube_set\b"
  if [ $? -ne 0 ]; then
    log "rasdaman type struct_cube_set not found, inserting..."
    $RASQL -q "create type struct_pixel as ( x1 float, x2 double, x3 octet, x4 double, x5 short )" > /dev/null
    $RASQL -q "create type struct_cube as struct_pixel mdarray [ x, y, z ]" > /dev/null
    $RASQL -q "create type struct_cube_set as set ( struct_cube )" > /dev/null
  fi

  create_coll $TEST_STRUCT struct_cube_set
  $RASQL -q "insert into $TEST_STRUCT values \$1 $STORAGE_CLAUSE" -f "$TESTDATA_PATH/23k.bin" --mdddomain "[0:99,0:9,0:0]" --mddtype struct_cube > /dev/null

  # create the GreySet4 type
  $RASQL -q "select c from RAS_SET_TYPES as c" --out string | egrep --quiet  "\bGreySet4\b"
  if [ $? -ne 0 ]; then
    log "rasdaman type GreySet4 not found, inserting..."
    $RASQL -q "create type GreyTesseract as char mdarray [ x0, x1, x2, x3 ]" > /dev/null
    $RASQL -q "create type GreySet4 as set ( GreyTesseract )" > /dev/null
  fi

  create_coll $TEST_GREY4D GreySet4
  $RASQL -q "insert into $TEST_GREY4D values \$1 $STORAGE_CLAUSE" -f "$TESTDATA_PATH/50k.bin" --mdddomain "[0:9,0:9,0:9,0:49]" --mddtype GreyTesseract > /dev/null

  #create type for 2D named axes '0:time and 1:space'
  $RASQL -q 'CREATE TYPE test_sortNamedAxis AS double mdarray [time (0:9), space (0:2)]' > /dev/null
  $RASQL -q 'CREATE TYPE test_DoubleSet_named AS SET (test_sortNamedAxis)' > /dev/null


  create_coll $TEST_GREY GreySet
  create_coll $TEST_GREY2 GreySet
  create_coll $TEST_RGB2 RGBSet
  create_coll $TEST_GREY3D GreySet3
  create_coll $TEST_CFLOAT32 Gauss1Set
  create_coll $TEST_CFLOAT64 Gauss2Set
  create_coll $TEST_CINT16 CInt16Set
  create_coll $TEST_CINT32 CInt32Set
  create_coll $TEST_OVERLAP GreySet
  create_coll $TEST_OVERLAP_3D GreySet3
  create_coll test_oneD DoubleSet1
  create_coll test_twoD DoubleSet
  create_coll test_threeD DoubleSet3
  create_coll test_threeD_two_objects DoubleSet3
  create_coll test_twoD_named test_DoubleSet_named
  insert_into $TEST_GREY "$TESTDATA_PATH/mr_1.png" "" "decode" "" "tiling aligned [0:49,0:29] tile size 1500 $STORAGE_CLAUSE"
  insert_into $TEST_GREY2 "$TESTDATA_PATH/mr2_1.png" "" "decode" "" "tiling aligned [0:49,0:29] tile size 1500 $STORAGE_CLAUSE"
  insert_into $TEST_RGB2 "$TESTDATA_PATH/rgb.png" "" "decode" "" "tiling aligned [0:49,0:49] tile size 7500 $STORAGE_CLAUSE"
  insert_into $TEST_CFLOAT32 "$TESTDATA_PATH/cfloat32_image.tif" "" "decode"
  insert_into $TEST_CFLOAT64 "$TESTDATA_PATH/cfloat64_image.tif" "" "decode"
  insert_into $TEST_CINT16 "$TESTDATA_PATH/cint16_image.tif" "" "decode"
  insert_into $TEST_CINT32 "$TESTDATA_PATH/cint32_image.tif" "" "decode"

  $RASQL -q 'insert into test_oneD values decode($1, "csv", "{ \"formatParameters\":{ \"domain\": \"[0:29]\",\"basetype\": \"double\" } }")' -f $TESTDATA_PATH/twoD.csv > /dev/null
  $RASQL -q 'insert into test_twoD values decode($1, "csv", "{ \"formatParameters\":{ \"domain\": \"[0:9, 0:2]\",\"basetype\": \"double\" } }")' -f $TESTDATA_PATH/twoD.csv > /dev/null
  $RASQL -q 'insert into test_threeD values decode($1, "csv", "{ \"formatParameters\":{ \"domain\": \"[0:9, 0:4, 0:1]\",\"basetype\": \"double\" } }")' -f $TESTDATA_PATH/threeD.csv > /dev/null
  $RASQL -q 'insert into test_threeD_two_objects values decode($1, "csv", "{ \"formatParameters\":{ \"domain\": \"[0:9, 0:4, 0:1]\",\"basetype\": \"double\" } }")' -f $TESTDATA_PATH/threeD.csv > /dev/null
  $RASQL -q 'insert into test_threeD_two_objects values decode($1, "csv", "{ \"formatParameters\":{ \"domain\": \"[0:9, 0:4, 0:1]\",\"basetype\": \"double\" } }")' -f $TESTDATA_PATH/threeD.csv > /dev/null
  $RASQL -q 'insert into test_twoD_named values decode($1, "csv", "{ \"formatParameters\":{ \"domain\": \"[0:9, 0:2]\",\"basetype\": \"double\" } }")' -f $TESTDATA_PATH/twoD.csv > /dev/null

  add_overlap_data
  $RASQL -q "insert into $TEST_GREY3D values \$1 $STORAGE_CLAUSE" -f "$TESTDATA_PATH/50k.bin" --mdddomain "[0:99,0:99,0:4]" --mddtype GreyCube > /dev/null
}

#adds the necessary data to the $TEST_OVERLAP collection
function add_overlap_data()
{
  #2d
  insert_into $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "" "tiling aligned [0:59,0:59] tile size 3600"
  update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[0:255,211:421]" "[0,211]"
  update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[256:511,211:421]" "[256,211]"
  update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[256:511,0:210]" "[256,0]"
  update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[100:355,100:310]" "[100,100]"
  update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[200:455,-100:110]" "[200,-100]"
  update $TEST_OVERLAP "$TESTDATA_PATH/mr_1.png" "" "decode" "[-100:155,50:260]" "[-100,50]"
  #3d
  $RASQL -q "insert into $TEST_OVERLAP_3D values <[0:0,0:0,0:0] 0c> TILING ALIGNED [0:0,0:59,0:59] TILE SIZE 3600"> /dev/null
  update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,0:255,0:210]" "[0,0]"
  update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,0:255,211:421]" "[0,211]"
  update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,256:511,211:421]" "[256,211]"
  update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,256:511,0:210]" "[256,0]"
  update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,100:355,100:310]" "[100,100]"
  update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,200:455,-100:110]" "[200,-100]"
  update $TEST_OVERLAP_3D "$TESTDATA_PATH/mr_1.png" "" "decode" "[0,-100:155,50:260]" "[-100,50]"
}


#
# import data used in rasql tests. Expects arguments
# $1 - testdata dir holding files to be imported
#
function import_nullvalues_data()
{
  #
  # check data types and insert if not available
  #
  local mdd_type=NullValueArrayTest
  local set_type=NullValueSetTest
  check_user_type $set_type
  if [ $? -ne 0 ]; then
    $RASQL -q "create type $mdd_type as char mdarray [ x, y ]" > /dev/null | tee -a $LOG
    $RASQL -q "create type $set_type as set ( $mdd_type null values [5:7] )" > /dev/null | tee -a $LOG
  fi
  local mdd_type3d=NullValueArrayTest3D
  local set_type3d=NullValueSetTest3D
  check_user_type $set_type3d
  if [ $? -ne 0 ]; then
    $RASQL -q "create type $mdd_type3d as char mdarray [ x, y, z ]" > /dev/null | tee -a $LOG
    $RASQL -q "create type $set_type3d as set ( $mdd_type3d null values [5:7] )" > /dev/null | tee -a $LOG
  fi
  local mdd_flt_type=NullValueFloatArrayTest
  local set_flt_type=NullValueFloatSetTest
  check_user_type $set_flt_type
  if [ $? -ne 0 ]; then
    $RASQL -q "create type $mdd_flt_type as float mdarray [ x, y ]" > /dev/null | tee -a $LOG
    $RASQL -q "create type $set_flt_type as set ( $mdd_flt_type null values [nan, 3.14:3.33] )" > /dev/null | tee -a $LOG
  fi

  local mdd_type_grey=TestGreyMddNull
  local set_type_grey=TestGreySetNull
  check_user_type $set_type_grey
  if [ $? -ne 0 ]; then
    $RASQL -q "create type $mdd_type_grey as char mdarray [ x, y ]" > /dev/null | tee -a $LOG
    $RASQL -q "create type $set_type_grey as set ( $mdd_type_grey null values [255] )" > /dev/null | tee -a $LOG
  fi

  #
  # drop any existing data and insert again
  #
  drop_colls $TEST_NULL $TEST_NULL_FLOAT $TEST_NULL3D $TEST_GREY_NULL $TEST_RGB2 $TEST_NULL3D_HOLES

  create_coll $TEST_NULL $set_type
  $RASQL -q "insert into $TEST_NULL values marray x in [0:3,0:3] values (char)(x[0] + x[1] + 1)" > /dev/null | tee -a $LOG

  create_coll $TEST_NULL3D_HOLES $set_type3d
  $RASQL -q "insert into $TEST_NULL3D_HOLES values marray x in [0:0,0:3,0:3] values (char)(x[0] + x[1] + 1)" > /dev/null | tee -a $LOG
  $RASQL -q "update $TEST_NULL3D_HOLES as m set m assign marray x in [3:3,0:3,0:3] values (char)(x[0] + x[1] + 1)" > /dev/null | tee -a $LOG

  create_coll $TEST_NULL3D $set_type3d
  $RASQL -q "insert into $TEST_NULL3D values marray x in [0:3,0:3,0:3] values (char)(x[0] + x[1] + 1)" > /dev/null | tee -a $LOG

  create_coll $TEST_NULL_FLOAT $set_flt_type
  $RASQL -q "insert into $TEST_NULL_FLOAT values (float) <[0:2,0:2] nanf, 0.0f, 3.13f; 3.14f, 3.15f, 3.33f; 3.33334f, 3.34f, nanf>" > /dev/null | tee -a $LOG

  create_coll $TEST_GREY_NULL $set_type_grey
  $RASQL -q "insert into $TEST_GREY_NULL values decode(\$1)" -f "$SCRIPT_DIR/testdata/mr_1.png" > /dev/null | tee -a $LOG

  create_coll $TEST_RGB2 RGBSet
  insert_into $TEST_RGB2 "$SCRIPT_DIR/../test_select/testdata/rgb.png" "" "decode" "" "tiling aligned [0:49,0:49] tile size 7500 $STORAGE_CLAUSE"
}

#
# drop subsetting test data
#
drop_subsetting_data()
{
  drop_colls $TEST_SUBSETTING_1D $TEST_SUBSETTING $TEST_SUBSETTING_SINGLE $TEST_SUBSETTING_3D $TEST_SUBSETTING_HOLES
}

#		
# import data used in rasql subsetting tests. Expects arguments		
# $1 - testdata dir holding files to be imported		
#		
function import_subsetting_data()		
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
  
  if [ ! -f "$TESTDATA_PATH/101.bin" ]; then		
	  error "tesdata file $TESTDATA_PATH/101.bin not found"		
	fi		
			
  # check data types		
  check_type GreySet1		
  check_type GreySet		
  check_type RGBSet		
  check_type GreySet3		
	 		
  drop_subsetting_data		
			
  create_coll $TEST_SUBSETTING_1D GreySet1		
  $RASQL -q "insert into $TEST_SUBSETTING_1D values \$1" -f "$TESTDATA_PATH/101.bin" --mdddomain "[0:100]" --mddtype GreyString > /dev/null		
			
  create_coll $TEST_SUBSETTING GreySet		
  # this creates an object of size: [0:255,0:210]		
  insert_into $TEST_SUBSETTING "$TESTDATA_PATH/mr_1.png" "" "decode"		
	 				
  # we extend this to an object of size: [0:755,0:710]		
  # materializing data at: [500:755,500:710]		
  $RASQL -q "update $TEST_SUBSETTING as m set m assign shift(decode(\$1), [500, 500])" -f "$TESTDATA_PATH/mr_1.png" --quiet > /dev/null		
	 				
  # and let's extend negative in order to test negative indexing: [-500:755,-500:710]		
  $RASQL -q "update $TEST_SUBSETTING as m set m assign shift(decode(\$1), [-500, -500])" -f "$TESTDATA_PATH/mr_1.png" --quiet > /dev/null		
	 			
  create_coll $TEST_SUBSETTING_SINGLE RGBSet		
  insert_into $TEST_SUBSETTING_SINGLE "$TESTDATA_PATH/rgb.png" "" "decode"		
			
  create_coll $TEST_SUBSETTING_3D GreySet3		
  $RASQL -q "insert into $TEST_SUBSETTING_3D values marray i in [0:0,-500:-500,-500:-500] values 0c" --quiet > /dev/null		
  $RASQL -q "update $TEST_SUBSETTING_3D as m set m[0,*:*,*:*] assign shift(decode(\$1), [-500, -500])" -f "$TESTDATA_PATH/mr_1.png" --quiet > /dev/null		
  $RASQL -q "update $TEST_SUBSETTING_3D as m set m[1,*:*,*:*] assign shift(decode(\$1), [500, 500])" -f "$TESTDATA_PATH/mr_1.png" --quiet > /dev/null

  local coll=$TEST_SUBSETTING_HOLES
  local settype=${TEST_SUBSETTING_HOLES}_Set
  local datafile="$TESTDATA_PATH/float_2d_with_nulls.tif"
  $RASQL -q "drop type $settype" > /dev/null 2>&1
  $RASQL -q "CREATE TYPE $settype AS SET (FloatImage NULL VALUES [-9999])" > /dev/null 2>&1
  $RASQL -q "create collection $coll $settype" --quiet
  $RASQL -q "insert into $coll values <[-100:-100,-570:-570] 0f>" --quiet
  $RASQL -q "update $coll as m set m[*:*,*:*] assign <[-1000:-1000,-1670:-1670] 0f>" --quiet
  # $datafile: 64x110 = [-984:-921,-681:-571]
  $RASQL -q "update $coll as m set m[*:*,*:*] assign shift(decode(\$1), [-984, -681])" -f "$datafile" --quiet
}

#
# drop null values test data, including imported null types
#
drop_nullvalues_data()
{
  drop_colls $TEST_NULL $TEST_NULL3D $TEST_NULL_FLOAT $TEST_GREY_NULL
  drop_types NullValueSetTest3D NullValueArrayTest3D 
  drop_types NullValueFloatSetTest NullValueFloatArrayTest
  drop_types NullValueSetTest NullValueArrayTest 
  drop_types TestGreySetNull TestGreyMddNull
}

#
# Generate an array (at given file path) encoded in given format, sdom and values in each cell.
#
# Usage: generate_data <result_file_path> <format> <sdom> <cell_values>
#
# The script runs a rasql expression of the form:
# SELECT encode(MARRAY i IN <sdom> VALUES <cell_values>, "<format>")
#
# Therefore the <cell_values> can include an iterator i as well.
#
# The result file path is printed to stdout, which might be slightly different from the
# provided path (an extension could be automatically appended). Non-zero exit code indicates
# success as usual.
#
# Example: ./generate_data.sh "/tmp/test_ones" "GTiff" "[0:10,0:10]" "i[0] + 0.5f"
#
generate_data()
{                            # example values:
  local result_filepath="$1" #   myresult.tif
  local encode_format="$2"   #   tiff
  local data_sdom="$3"       #   [0:10,0:10]
  local data_values="$4"     #   1.5f
  $DIRECTQL -q "select encode(marray i in $data_sdom values ($data_values), \"$encode_format\")" \
            --out file --outfile "$result_filepath" | grep '  Result object' | sed 's/^ *Result .* file \(.*\)\.\.\..*/\1/'
}
