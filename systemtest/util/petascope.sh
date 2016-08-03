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
#  petascope.sh
# Description
#  Common functionality pertaining to petascope, like
#   - import/removal of petascope data
#
################################################################################


# ------------------------------------------------------------------------------
# check if coverage exists in rasdaman and petascope
# arg 1: coverage name
#
function check_cov()
{
  local c="$1"
  id=`$PSQL -c  "select id from ps_coverage where name = '$c' " | head -3 | tail -1`
  test1=0
  if [[ "$id" == \(0*\) ]]; then
    test1=1
  fi

  $RASQL -q 'select r from RAS_COLLECTIONNAMES as r' --out string | egrep "\b$c\b" > /dev/null
  test2=$?

	# For multipoint coverages consider only test1
	multi_coll="Parksmall"
	if [[ $multi_coll == *$c* ]]
	then
		[ $test1 -eq 0 ]
	else
	[ $test1 -eq 0 -a $test2 -eq 0 ]
	fi
}

#
# Check if petascope is initialized and running
#
function check_petascope()
{
  $PSQL --list | egrep "\b$PS_DB\b" > /dev/null
  if [ $? -ne 0 ]; then
    log "no petascope database present, please install petascope first."
    return 1
  fi
  $WGET -q $WCPS_URL -O /dev/null
  if [ $? -ne 0 ]; then
    log "failed connecting to petascope at $WCPS_URL, please deploy it first."
    return 1
  fi
  return 0
}

# ------------------------------------------------------------------------------
#
# Check if multipoint is enabled
# return
#   0 - if multipoint is enabled
#   1 - if it is not enabled
#

function check_multipoint()
{
  M=`psql -d petascopedb -c "SELECT * FROM information_schema.tables WHERE table_schema = 'public'" | grep ps_multipoint`
  if [ ! "$M" ]; then
    echo 1
  else
    echo 0
  fi
}

# ------------------------------------------------------------------------------
#
# drop coverages in global variable $COLL
#
function drop_petascope()
{
  check_postgres
  for c in $*; do
    logn "deleting coverage $c from petascope... "

    c_id=$($PSQL -c  "select id from ps_coverage where name = '$c' " | head -3 | tail -1) > /dev/null
    if [[ "$c_id" != \(0\ *\) ]]; then
      # Drop the coverage cascades to the other tables
      $PSQL -c "DELETE FROM ps_coverage WHERE id=$c_id" > /dev/null
      echo ok.
    else
      echo no such coverage found.
    fi

  done

  log done.
}

function drop_petascope_data()
{
  res=$(check_multipoint)
  if [ $res -eq 0 ]; then
    multi_coll="Parksmall"
  fi
  COLLECTIONS="rgb mr eobstest mean_summer_airtemp mean_summer_airtemp_repeat float_4d double_1d irr_cube_1 irr_cube_2 $multi_coll"
  drop_petascope $COLLECTIONS
  drop_colls $COLLECTIONS
  log "dropping wms..."
  "$DROPWMS" australia_wms > /dev/null
}


