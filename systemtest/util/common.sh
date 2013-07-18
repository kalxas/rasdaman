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
#	  common.sh
# Description
#	  Common functionality used by test scripts, like
#   - shortcuts for commands
#   - logging functions
#   - various check functions
#
################################################################################

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
UTIL_SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$UTIL_SCRIPT_DIR"/../conf/test.cfg


# ------------------------------------------------------------------------------
# script return codes
#
RC_OK=0
RC_ERROR=1


# ------------------------------------------------------------------------------
# testing statistics
#
NUM_TOTAL=0 # the number of manipulations
NUM_FAIL=0  # the number of fail manipulations
NUM_SUC=0   # the number of success manipulations


# ------------------------------------------------------------------------------
# command shortcuts
#
export RASQL="rasql --server $RASMGR_HOST --port $RASMGR_PORT --user $RASMGR_ADMIN_USER \
              --passwd $RASMGR_ADMIN_PASSWD --database $RASDB"
export RASCONTROL="rascontrol --host $RASMGR_HOST --port $RASMGR_PORT"
export RASDL="rasdl -d $RASDB"

# check connection itself
export PGSQL="psql -d $RASDB --port $PG_PORT"

# check for petascope
export PSQL="psql -d $PS_DB --port $PG_PORT"

export WGET="wget"
export GDALINFO="gdalinfo -noct -checksum"


# ------------------------------------------------------------------------------
# logging
#
LOG="$SCRIPT_DIR/log"
OLDLOG="$LOG.save"

function log()
{
  echo "$PROG: $*"
}

function loge()
{
  echo "$*"
}

function logn()
{
  echo -n "$PROG: $*"
}

function feedback()
{
  if [ $? -ne 0 ]; then
    loge failed.
  else
    loge ok.
  fi
}

function error()
{
  echo "$PROG: $*"
  echo "$PROG: exiting."
  exit $RC_ERROR
}

# ------------------------------------------------------------------------------
# setup log
#
if [ -n "$SCRIPT_DIR" ]; then
  if [ -f $LOG ]; then
	  echo Old logfile found, copying it to $OLDLOG
    rm -f $OLDLOG
	  mv $LOG $OLDLOG
  fi
  
  # all output that goes to stdout is redirected to log too
  exec >  >(tee -a $LOG)
  exec 2> >(tee -a $LOG >&2)

  NOW=`date`
  log "starting test at $NOW"
  log ""
fi


# ------------------------------------------------------------------------------
# dependency checks
#
function check_rasdaman()
{
  which rasmgr > /dev/null
  if [ $? -ne 0 ]; then
    error "rasdaman not installed, please add to the PATH."
  fi
  pgrep rasmgr > /dev/null
  if [ $? -ne 0 ]; then
    error "rasdaman not started, please start with start_rasdaman.sh"
  fi
  $RASCONTROL -x 'list srv -all' > /dev/null
  if [ $? -ne 0 ]; then
    error "no rasdaman servers started."
  fi
}

function check_postgres()
{
  which psql > /dev/null
  if [ $? -ne 0 ]; then
    error "PostgreSQL missing, please add psql to the PATH."
  fi
  pgrep postgres > /dev/null
  if [ $? -ne 0 ]; then
    pgrep postmaster > /dev/null || error "The PostgreSQL service is not started."
  fi
  $PGSQL --list > /dev/null 2>&1
  if [ $? -eq 2 ]; then
    error "Wrong PostgreSQL credentials for current user"
  fi
}

function check_wget()
{
  which wget > /dev/null
  if [ $? -ne 0 ]; then
    error "wget missing, please install."
  fi
}

function check_petascope()
{
  $PSQL --list | egrep "\b$PS_DB\b" > /dev/null
  if [ $? -ne 0 ]; then
    error "No petascope database present, please install petascope first."
  fi
  $WGET -q $WCPS_URL -O /dev/null
  if [ $? -ne 0 ]; then
    error "failed connecting to petascope at $WCPS_URL, please deploy it first."
  fi
}

function check_secore()
{
  $WGET -q "${SECORE_URL}/crs" -O /dev/null
  if [ $? -ne 0 ]; then
    error "failed connecting to SECORE at $SECORE_URL, please deploy it first."
  fi
}

function check_netcdf()
{
  which ncdump > /dev/null
  if [ $? -ne 0 ]; then
    error "netcdf tools missing, please add ncdump to the PATH."
  fi
}

function check_gdal()
{
  which gdal_translate > /dev/null
  if [ $? -ne 0 ]; then
    error "gdal missing, please add gdal_translate to the PATH."
  fi
}

# ------------------------------------------------------------------------------
# check if collection exists in rasdaman and petascope
# arg 1: collection name
# arg 2: error message in case collection doesn't exist
#
function check_collection()
{
  id=`$PSQL -c  "select id from PS_Coverage where name = '$COLLS' " | head -3 | tail -1`
  test1=0
  if [[ "$id" == \(0*\) ]]; then
    test1=1
  fi

  $RASQL -q 'select r from RAS_COLLECTIONNAMES as r' --out string | egrep "\b$COLLS\b" > /dev/null
  test2=$?
  [ $test1 -eq 0 -a $test2 -eq 0 ]
}

# ------------------------------------------------------------------------------
# check if collection exists in rasdaman
# arg 1: collection name
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
  SET_TYPE="$1"
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
# drop collections in global variable $COLLS
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


# ------------------------------------------------------------------------------
# print test summary
#
function print_summary()
{
  NUM_TOTAL=$(($NUM_SUC + $NUM_FAIL))
  NOW=`date`

  log "-------------------------------------------------------"
  log "Test summary"
  log ""
  log "  Test finished at: $NOW"
  log "  Total tests run : $NUM_TOTAL"
  log "  Successful tests: $NUM_SUC"
  log "  Failed tests    : $NUM_FAIL"
  log "  Detail test log : $LOG"
  log "-------------------------------------------------------"

  # compute return code
  if [ $NUM_TOTAL -eq $NUM_SUC ]; then
    RC=$RC_OK
  else
    RC=$RC_ERROR
  fi
}
