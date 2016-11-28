#!/bin/bash
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.    If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#    stress.sh
# Description
#    This script tests the connections from WMSServlet in Petascope to petascopedb from Postgresql. By default, Postgresql supports 100 connections at the same time.
#    The problem is fixed when WMSServlet releases connection as soon as possible.

################################################################################
# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
  SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh


# test WMS query
PETASCOPE_END_POINT="$PETASCOPE_URL?"
WMS_TEST_END_POINT_TEMPLATE=$PETASCOPE_END_POINT"service=WMS&version=1.3.0&request=GetMap&layers=test_wms_4326&bbox=MIN_MAX_LAT_LONG_TEMPLATE&crs=EPSG:4326&width=600&height=600&format=image/png"

min_lat=-40
min_long=120
max_lat=-10
max_long=150

fixed=yes
for i in {1..10}
do

  min_lat=$(echo "$min_lat + 0.000001" | bc -l)
  min_long=$(echo "$min_long + 0.000001" | bc -l)
  max_lat=$(echo "$max_lat - 0.000001" | bc -l)
  max_long=$(echo "$max_long - 0.000001" | bc -l)

  min_max_lat_long=$min_lat","$min_long","$max_lat","$max_long

  wms_test_end_point=$(echo "$WMS_TEST_END_POINT_TEMPLATE" | sed "s/MIN_MAX_LAT_LONG_TEMPLATE/$min_max_lat_long/g")

  # query with small change in boundingbox for each request
  wget -q --spider "$wms_test_end_point"

  sleep 0.1

  numb_connection=$(psql -c "SELECT sum(numbackends) FROM pg_stat_database;" -d $PS_DB 2>&1 | awk 'NR==3{print $1}')

  if [[ $numb_connection == "" || $numb_connection -gt 20 ]]; then
  	fixed=no
    break
  fi
done

log "done."

# check result
check_result "yes" "$fixed" "testing WMS open connections"

# print summary from util/common.sh
print_summary
exit $RC
