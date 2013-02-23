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
#	5)images needed for testing shall be put in directory of images 	
# Usage: ./test.sh 
#        
# CHANGE HISTORY
#       2009-Sep-16     J.Yu       created
#

# further tests will be done on dem, inv_dem, tor and inv_tor, after their implementations.

# Variables
PROGNAME=`basename $0`
DIR_NAME=$(dirname $0)
LOG_DIR=$DIR_NAME  
LOG=$LOG_DIR/log
OLDLOG=$LOG.save
USERNAME=rasadmin	
PASSWORD=rasadmin
DATABASE=RASBASE
IMAGEDIR=$DIR_NAME/testdata
ORACLE_DIR=$DIR_NAME/oracle
RASQL="rasql --quiet --user $USERNAME --passwd $PASSWORD"
RASDL="rasdl"
GDALINFO="gdalinfo -noct -checksum"

  CODE_OK=0
  CODE_FAIL=255

# NUM_TOTAL: the number of manipulations
# NUM_FAIL: the number of fail manipulations
# NUM_SUC: the number of success manipulations
  NUM_TOTAL=0
  NUM_FAIL=0
  NUM_SUC=0 
  
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  
#--------------- check if old logfile exists ----------------------------------
if [ -f $LOG ]
then
	echo Old logfile found, copying it to $OLDLOG
	mv $LOG $OLDLOG
fi

echo "Test by:"$PROGNAME" at "`date`|tee $LOG

#---------------------------Precondition------------------------------------------
# check the Postgres
ps -e | grep --quiet -w postgres
if [ $? -ne 0 ]
then
   echo no postgres available|tee -a $LOG
   exit $CODE_FAIL
fi

# check the Rasdaman
ps -e | grep --quiet rasmgr
if [ $? -ne 0 ]
then
   echo no rasmgr available|tee -a $LOG 
   exit $CODE_FAIL
fi

# check gdalinfo
which gdalinfo > /dev/null
if [ $? -ne 0 ]
then
   echo gdal tools missing, please add gdalinfo to the PATH|tee -a $LOG 
   exit $CODE_FAIL
fi

# check usr
#
# check data collection
$RASQL -q "select r from RAS_COLLECTIONNAMES as r"
if [ $? -ne 0 ]
then
   echo no data collection available|tee -a $LOG 
   exit $CODE_FAIL
fi

# check data type
$RASDL --print|grep --quiet GreySet
if [ $? -ne 0 ]
then
   echo no GreSet type available|tee -a $LOG 
   exit $CODE_FAIL
fi

$RASDL --print|grep --quiet RGBSet
if [ $? -ne 0 ]
then
   echo no RGBSet type available, try create_db.sh|tee -a $LOG 
   exit $CODE_FAIL
fi

$RASDL -p | grep --quiet 'TestSet'
if [ $? -ne 0 ]; then
  $RASDL -r $IMAGEDIR/types.dl -i > /dev/null
fi


# check data set

#--------------------------initiation--------------------------------------------
# drop collection if they already exists

if   $RASQL -q "select r from RAS_COLLECTIONNAMES as r" --out string|grep test_tmp 
then
	echo dropping collection ... | tee -a $LOG
	$RASQL -q "drop collection test_tmp" | tee -a $LOG
fi

#
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
fi
local extraopts="$6"

echo -----$fun and inv_$fun conversion------ | tee -a $LOG

echo creating collection ... | tee -a $LOG
$RASQL -q "create collection test_tmp $colltype" ||
  echo Error creating collection  test_tmp | tee -a $LOG

echo inserting collection ... | tee -a $LOG
$RASQL -q "insert into test_tmp values inv_$inv_fun(\$1 $extraopts)" -f $IMAGEDIR/$f.$inv_ext ||
  echo Error inserting $inv_fun image | tee -a $LOG

echo extracting collection ... | tee -a $LOG
$RASQL -q "select $fun(a) from test_tmp as a" --out file --outfile $f || 
  echo Error extracting $fun image | tee -a $LOG

echo  comparing images | tee -a $LOG
if [ -f "$ORACLE_DIR/$f.$ext.checksum" ]; then
  $GDALINFO $f.$ext | grep 'Checksum' > $f.$ext.result
  diff $ORACLE_DIR/$f.$ext.checksum $f.$ext.result
else
  cmp $IMAGEDIR/$f.$ext $f.$ext
fi

if [ $? != "0" ]
then
  echo input and output does not match | tee -a $LOG
  NUM_FAIL=$(($NUM_FAIL + 1))
else
  echo input and output match | tee -a $LOG
  NUM_SUC=$(($NUM_SUC + 1))
fi

$RASQL -q "drop collection test_tmp" | tee -a $LOG
rm -f $f*
}

################## jpeg() and inv_jpeg() #######################
run_test jpeg jpeg jpg jpg GreySet

################## tiff() and inv_tiff() #######################
run_test tiff tiff tif tif GreySet
run_test tiff tiff tif tif TestSet ", \"sampletype=octet\""

################## png() and inv_png() #######################
run_test png png png png GreySet

################## bmp() and inv_bmp() #######################
run_test bmp bmp bmp bmp GreySet

################## vff() and inv_vff() #######################
run_test vff vff vff vff GreySet

################## hdf() and inv_hdf() #######################

# run hdf test only if hdf was compiled in
grep 'HAVE_HDF 1' $SCRIPT_DIR/../../../config.h

if [ $? -eq 0 ]; then
  run_test hdf hdf hdf hdf GreySet
fi

################## csv() #######################

run_test csv png csv png GreySet
run_test csv png csv png RGBSet

#
################# summary #######################
#
NUM_TOTAL=$(($NUM_SUC + $NUM_FAIL))

# Print the summary
echo
echo "test done at "`date`|tee -a $LOG
echo "Total conversions: "$NUM_TOTAL|tee -a $LOG
echo "Successful conversion number: "$NUM_SUC|tee -a $LOG
echo "Failed conversion number: "$NUM_FAIL|tee -a $LOG
echo "Detail test log is in "$LOG 

if [ $NUM_TOTAL -eq $NUM_SUC ]; then
  exit $CODE_OK
else
  exit $CODE_FAIL
fi
