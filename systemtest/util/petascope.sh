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
#	  petascope.sh
# Description
#	  Common functionality used by petascope test scripts, like
#   - shortcuts for commands
#   - dropping and inserting coverages
#
################################################################################

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
UTIL_SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$UTIL_SCRIPT_DIR"/common.sh


# ------------------------------------------------------------------------------
# drop coverages in global variable $COLLS
#
function drop_petascope()
{
  check_postgres
  for c in $COLLS; do
    logn "deleting coverage $c from petascope... "

    c_id=$($PSQL -c  "select id from PS_Coverage where name = '$c' " | head -3 | tail -1) > /dev/null
    if [ "$c_id" != "(0 rows)" ]; then
      x_id=$($PSQL -c "select id from PS_domain where coverage = $c_id and type=1" | head -3 | tail -1) > /dev/null
      y_id=$($PSQL -c "select id from PS_domain where coverage = $c_id and type=2" | head -3 | tail -1) > /dev/null

      $PSQL -c "delete from PS_Coverage where id = $c_id" > /dev/null
      $PSQL -c "delete from PS_CellDomain where coverage = $c_id" > /dev/null
      $PSQL -c "delete from PS_Domain where coverage = $c_id" > /dev/null
      $PSQL -c "delete from PS_Range where coverage = $c_id" > /dev/null
      $PSQL -c "delete from PS_InterpolationSet where coverage = $c_id" > /dev/null
      $PSQL -c "delete from PS_NullSet where coverage = $c_id" > /dev/null
      $PSQL -c "delete from PS_CrsDetails where coverage = $c_id" > /dev/null
      if [ "$x_id" != "(0 rows)" ]; then
        $PSQL -c "delete from PS_crsset where axis = $x_id" > /dev/null
      fi
      if [ "$y_id" != "(0 rows)" ]; then
        $PSQL -c "delete from PS_crsset where axis = $y_id" > /dev/null
      fi

      echo ok.
    else
      echo no such coverage found.
    fi

  done

  log done.
}

# ------------------------------------------------------------------------------
#
# import 3D eobs data
#
function import_eobs()
{
  c=$COLLS
  
  X=100
  Y=231
  min_x_geo_coord=25
  min_y_geo_coord=-40
  max_x_geo_coord=75
  max_y_geo_coord=75

  $RASQL -q "create collection $c ShortSet3" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values (short) inv_netcdf(\$1, \"vars=tg\")" -f "$TESTDATA_PATH"/eobs.nc > /dev/null || exit $RC_ERROR
  
  # general coverage information (name, type, ...)
  $PSQL -c "insert into PS_Coverage (name, nulldefault, interpolationtypedefault, nullresistancedefault, type) values ( '$c','0', 5, 2, 'RectifiedGridCoverage')" > /dev/null || exit $RC_ERROR

  # get the coverage id
  c_id=$($PSQL -c  "select id from PS_Coverage where name = '$c' " | head -3 | tail -1) > /dev/null || exit $RC_ERROR

  # describe the pixel domain
  $PSQL -c "insert into PS_CellDomain (coverage, i, lo, hi )  values ( $c_id, 0, 0, 5 )" > /dev/null || exit $RC_ERROR
  $PSQL -c "insert into PS_CellDomain (coverage, i, lo, hi )  values ( $c_id, 1, 0, $X )" > /dev/null || exit $RC_ERROR
  $PSQL -c "insert into PS_CellDomain (coverage, i, lo, hi )  values ( $c_id, 2, 0, $Y )" > /dev/null || exit $RC_ERROR

  # describe the geo domain
  $PSQL -c "insert into PS_Domain (coverage, i, name, type, numLo, numHi) values ( $c_id, 0, 't', 5, 0, 5 )" > /dev/null
  $PSQL -c "insert into PS_Domain (coverage, i, name, type, numLo, numHi) values ( $c_id, 1, 'x', 1, $min_x_geo_coord, $max_x_geo_coord )" > /dev/null || exit $RC_ERROR
  $PSQL -c "insert into PS_Domain (coverage, i, name, type, numLo, numHi) values ( $c_id, 2, 'y', 2, $min_y_geo_coord, $max_y_geo_coord )" > /dev/null || exit $RC_ERROR

  # describe the datatype of the coverage cell values
  $PSQL -c "insert into PS_Range (coverage, i, name, type) values ($c_id, 0, 'value', 4)" > /dev/null || exit $RC_ERROR

  # set of interpolation methods and null values for the coverage
  $PSQL -c "insert into PS_InterpolationSet (coverage, interpolationType, nullResistance) values ( $c_id, 5, 2)" > /dev/null || exit $RC_ERROR
  $PSQL -c "insert into PS_NullSet (coverage, nullValue) values ( $c_id, '0')" > /dev/null || exit $RC_ERROR

  # geo-referecing information about the coverage
  $PSQL -c "insert into PS_CrsDetails (coverage, low1, high1, low2, high2) values ( $c_id, $min_x_geo_coord, $max_x_geo_coord, $min_y_geo_coord, $max_y_geo_coord)" > /dev/null || exit $RC_ERROR

  # set the crs for the axes
  x_id=$($PSQL -c "select id from PS_domain where coverage = $c_id and type=1" | head -3 | tail -1) > /dev/null || exit $RC_ERROR
  y_id=$($PSQL -c "select id from PS_domain where coverage = $c_id and type=2" | head -3 | tail -1) > /dev/null || exit $RC_ERROR
  t_id=$($PSQL -c "select id from PS_domain where coverage = $c_id and name='t'" | head -3 | tail -1) > /dev/null || exit $RC_ERROR
  $PSQL -c "insert into PS_crsset ( axis, crs) values ( $x_id, 9)" > /dev/null || exit $RC_ERROR
  $PSQL -c "insert into PS_crsset ( axis, crs) values ( $y_id, 9)" > /dev/null || exit $RC_ERROR
  $PSQL -c "insert into PS_crsset ( axis, crs) values ( $t_id, 8)" > /dev/null || exit $RC_ERROR
}


# ------------------------------------------------------------------------------
#
# import 2D rgb data
#
function import_rgb()
{
  c=$COLLS
  X=400
  Y=344

  min_x_geo_coord=0
  min_y_geo_coord=0
  max_x_geo_coord=$X
  max_y_geo_coord=$Y

  $RASQL -q "create collection $c RGBSet" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values inv_png(\$1)" -f "$TESTDATA_PATH"/rgb.png > /dev/null || exit $RC_ERROR

  # general coverage information (name, type, ...)
  $PSQL -c "insert into PS_Coverage (name, nulldefault, interpolationtypedefault, nullresistancedefault, type) values ( '$c','{0,0,0}', 5, 2, 'RectifiedGridCoverage')" > /dev/null

  # get the coverage id
  c_id=$($PSQL -c  "select id from PS_Coverage where name = '$c' " | head -3 | tail -1) > /dev/null

  # describe the pixel domain
  $PSQL -c "insert into PS_CellDomain (coverage, i, lo, hi )  values ( $c_id, 0, 0, $X)" > /dev/null
  $PSQL -c "insert into PS_CellDomain (coverage, i, lo, hi )  values ( $c_id, 1, 0, $Y)" > /dev/null

  # describe the geo domain
  $PSQL -c "insert into PS_Domain (coverage, i, name, type, numLo, numHi) values ( $c_id, 0, 'x', 1, $min_x_geo_coord, $max_x_geo_coord )" > /dev/null
  $PSQL -c "insert into PS_Domain (coverage, i, name, type, numLo, numHi) values ( $c_id, 1, 'y', 2, $min_y_geo_coord, $max_y_geo_coord )" > /dev/null

  # describe the datatype of the coverage cell values
  $PSQL -c "insert into PS_Range (coverage, i, name, type) values ($c_id, 0, 'red', 7)" > /dev/null
  $PSQL -c "insert into PS_Range (coverage, i, name, type) values ($c_id, 1, 'green', 7)" > /dev/null
  $PSQL -c "insert into PS_Range (coverage, i, name, type) values ($c_id, 2, 'blue', 7)" > /dev/null

  # set of interpolation methods and null values for the coverage
  $PSQL -c "insert into PS_InterpolationSet (coverage, interpolationType, nullResistance) values ( $c_id, 5, 2)" > /dev/null
  $PSQL -c "insert into PS_NullSet (coverage, nullValue) values ( $c_id, '{0,0,0}')" > /dev/null

  # geo-referecing information about the coverage
  $PSQL -c "insert into PS_CrsDetails (coverage, low1, high1, low2, high2) values ( $c_id, $min_x_geo_coord, $max_x_geo_coord, $min_y_geo_coord, $max_y_geo_coord)" > /dev/null

  # set the crs for the axes
  x_id=$($PSQL -c "select id from PS_domain where coverage = $c_id and type=1" | head -3 | tail -1) > /dev/null
  y_id=$($PSQL -c "select id from PS_domain where coverage = $c_id and type=2" | head -3 | tail -1) > /dev/null
  $PSQL -c "insert into PS_crsset ( axis, crs) values ( $x_id, 8)" > /dev/null
  $PSQL -c "insert into PS_crsset ( axis, crs) values ( $y_id, 8)" > /dev/null
}


# ------------------------------------------------------------------------------
#
# import 2D rgb data
#
function import_mr()
{
  c=$COLLS
  X=256
  Y=211

  min_x_geo_coord=0
  min_y_geo_coord=0
  max_x_geo_coord=$X
  max_y_geo_coord=$Y

  $RASQL -q "create collection $c GreySet" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values (char) inv_png(\$1)" -f "$TESTDATA_PATH"/mr_1.png > /dev/null || exit $RC_ERROR

  # general coverage information (name, type, ...)
  $PSQL -c "insert into PS_Coverage (name, nulldefault, interpolationtypedefault, nullresistancedefault, type) values ( '$c','0', 5, 2, 'RectifiedGridCoverage')" > /dev/null

  # get the coverage id
  c_id=$($PSQL -c  "select id from PS_Coverage where name = '$c' " | head -3 | tail -1) > /dev/null

  # describe the pixel domain
  $PSQL -c "insert into PS_CellDomain (coverage, i, lo, hi )  values ( $c_id, 0, 0, $X)" > /dev/null
  $PSQL -c "insert into PS_CellDomain (coverage, i, lo, hi )  values ( $c_id, 1, 0, $Y)" > /dev/null

  # describe the geo domain
  $PSQL -c "insert into PS_Domain (coverage, i, name, type, numLo, numHi) values ( $c_id, 0, 'x', 1, $min_x_geo_coord, $max_x_geo_coord )" > /dev/null
  $PSQL -c "insert into PS_Domain (coverage, i, name, type, numLo, numHi) values ( $c_id, 1, 'y', 2, $min_y_geo_coord, $max_y_geo_coord )" > /dev/null

  # describe the datatype of the coverage cell values
  $PSQL -c "insert into PS_Range (coverage, i, name, type) values ($c_id, 0, 'value', 2)" > /dev/null

  # set of interpolation methods and null values for the coverage
  $PSQL -c "insert into PS_InterpolationSet (coverage, interpolationType, nullResistance) values ( $c_id, 5, 2)" > /dev/null
  $PSQL -c "insert into PS_NullSet (coverage, nullValue) values ( $c_id, '0')" > /dev/null

  # geo-referecing information about the coverage
  $PSQL -c "insert into PS_CrsDetails (coverage, low1, high1, low2, high2) values ( $c_id, $min_x_geo_coord, $max_x_geo_coord, $min_y_geo_coord, $max_y_geo_coord)" > /dev/null

  # set the crs for the axes
  x_id=$($PSQL -c "select id from PS_domain where coverage = $c_id and type=1" | head -3 | tail -1) > /dev/null
  y_id=$($PSQL -c "select id from PS_domain where coverage = $c_id and type=2" | head -3 | tail -1) > /dev/null
  $PSQL -c "insert into PS_crsset ( axis, crs) values ( $x_id, 8)" > /dev/null
  $PSQL -c "insert into PS_crsset ( axis, crs) values ( $y_id, 8)" > /dev/null
}


# ------------------------------------------------------------------------------
#
# import testdata: data is only imported if it isn't already in petascope
#
function import_data()
{
  COLLECTIONS="rgb mr eobstest"
  
  for COLLS in $COLLECTIONS; do
    check_collection
    if [ $? -ne 0 ]; then
      drop_colls
      drop_petascope
      logn "importing $COLLS... "
      
      counter=0
      while [ 1 -eq 1 ]; do
      
        if [ "$COLLS" == "rgb" ]; then
          import_rgb && break
        elif [ "$COLLS" == "mr" ]; then
          import_mr && break
        elif [ "$COLLS" == "eobstest" ]; then
          import_eobs && break
        fi
        
        raserase_colls > /dev/null 2>&1
        counter=$(($counter + 1))
        if [ $counter -eq 5 ]; then
          echo failed.
          exit $RC_ERROR
        fi
      done
      echo ok.
    else
      log "$COLLS already imported."
    fi
  done
}
