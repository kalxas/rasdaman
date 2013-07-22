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
# drop coverages in global variable $COLL
#
function drop_petascope()
{
  check_postgres
  for c in $COLL; do
    logn "deleting coverage $c from petascope... "

    c_id=$($PSQL -c  "select id from ps9_coverage where name = '$c' " | head -3 | tail -1) > /dev/null
    if [[ "$c_id" != \(0\ *\) ]]; then
      # Drop the coverage cascades to the other tables (for rasdaman collection an explicit trigger would be needed -- TODO)
      $PSQL -c "DELETE FROM ps9_coverage WHERE id=$c_id" > /dev/null
      $PSQL -c "DELETE FROM ps9_rasdaman_collection WHERE name='$c'" > /dev/null

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
  c=$COLL
  X=101
  Y=232
  T=6

  c_colltype='ShortSet3'
  c_datatype='short'
  c_covtype='RectifiedGridCoverage'

  c_crs_t='http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0.1/Temporal?epoch="1950-01-01T00:00:00"&uom="d"'
  c_crs_s='http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4326'
  min_t_geo_coord=0
  min_x_geo_coord=25
  max_y_geo_coord='75.5'
  t_res=1
  x_res='0.5'
  y_res='-0.5'

  c_band='value'

  #
  # START
  #

  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values ($c_datatype) inv_netcdf(\$1, \"vars=tg\")" -f "$TESTDATA_PATH"/eobs.nc > /dev/null || exit $RC_ERROR

  # general coverage information (name, type, ...)
  $PSQL -c "INSERT INTO ps9_coverage (name, gml_type_id, native_format_id) \
            VALUES ('$c', (SELECT id FROM ps9_gml_subtype WHERE subtype='$c_covtype'), \
            (SELECT id FROM ps9_mime_type WHERE mime_type='application/x-octet-stream'));" > /dev/null || exit $RC_ERROR

  # get the coverage id
  c_id=$($PSQL -c  "SELECT id FROM ps9_coverage WHERE name = '$c' " | head -3 | tail -1) > /dev/null || exit $RC_ERROR

  # get the collection OID (note: take the first OID)
  c_oid=$($RASQL -q "select oid(m) from $c as m" --out string | grep ' 1:' | awk -F ':' '{print $2}' | tr -d ' \n') > /dev/null || exit $RC_ERROR

  # range set: link the coverage to the rasdaman collection
  $PSQL -c "INSERT INTO ps9_rasdaman_collection (name, oid) VALUES ('$c', $c_oid);" > /dev/null
  $PSQL -c "INSERT INTO ps9_range_set (coverage_id, storage_id) VALUES (\
              (SELECT id FROM ps9_coverage WHERE name='$c'), \
              (SELECT id FROM ps9_rasdaman_collection WHERE name='$c'));" > /dev/null || exit $RC_ERROR

  # describe the datatype of the coverage cell values (range type)
  # note: assign dimensionless quantity
  $PSQL -c "INSERT INTO ps9_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band', 0, \
              (SELECT id FROM ps9_range_data_type WHERE name='$c_datatype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_datatype' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps9_crs (uri) VALUES ('$c_crs_t');" > /dev/null # no harm if duplicate error is thrown
  $PSQL -c "INSERT INTO ps9_crs (uri) VALUES ('$c_crs_s');" > /dev/null # 
  $PSQL -c "INSERT INTO ps9_domain_set (coverage_id, native_crs_id) \
            VALUES ($c_id, ARRAY[\
              (SELECT id FROM ps9_crs WHERE uri='$c_crs_t'),
              (SELECT id FROM ps9_crs WHERE uri='$c_crs_s')]\
            );" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_gridded_domain_set (coverage_id, grid_origin) \
            VALUES ($c_id, '{$min_t_geo_coord, $max_y_geo_coord, $min_x_geo_coord}');" > /dev/null || exit $RC_ERROR
  # grid axes:
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 0);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 1);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 2);" > /dev/null || exit $RC_ERROR
  # offset vectors (note: WGS84 has `Lat` first)
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=0), \
              '{$t_res,0,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=1), \
              '{0,0,$x_res}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=2), \
              '{0,$y_res,0}');" > /dev/null || exit $RC_ERROR
}


# ------------------------------------------------------------------------------
#
# import 2D rgb data
#
function import_rgb()
{
  c=$COLL
  X=400
  Y=344

  c_colltype='RGBSet'
  c_datatype='unsigned char'
  c_covtype='RectifiedGridCoverage'

  c_crs='http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0.1/Index2D'
  min_x_geo_coord=0
  max_y_geo_coord=344
  x_res='1'
  y_res='-1'

  c_band1='red'
  c_band2='green'
  c_band3='blue'

  #
  # START
  #

  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values inv_png(\$1)" -f "$TESTDATA_PATH"/rgb.png > /dev/null || exit $RC_ERROR

  # general coverage information (name, type, ...)
  $PSQL -c "INSERT INTO ps9_coverage (name, gml_type_id, native_format_id) \
            VALUES ('$c', (SELECT id FROM ps9_gml_subtype WHERE subtype='$c_covtype'), \
            (SELECT id FROM ps9_mime_type WHERE mime_type='application/x-octet-stream'));" > /dev/null || exit $RC_ERROR

  # get the coverage id
  c_id=$($PSQL -c  "SELECT id FROM ps9_coverage WHERE name = '$c' " | head -3 | tail -1) > /dev/null || exit $RC_ERROR

  # get the collection OID (note: take the first OID)
  c_oid=$($RASQL -q "select oid(m) from $c as m" --out string | grep ' 1:' | awk -F ':' '{print $2}' | tr -d ' \n') > /dev/null || exit $RC_ERROR

  # range set: link the coverage to the rasdaman collection
  $PSQL -c "INSERT INTO ps9_rasdaman_collection (name, oid) VALUES ('$c', $c_oid);" > /dev/null
  $PSQL -c "INSERT INTO ps9_range_set (coverage_id, storage_id) VALUES (\
              (SELECT id FROM ps9_coverage WHERE name='$c'), \
              (SELECT id FROM ps9_rasdaman_collection WHERE name='$c'));" > /dev/null || exit $RC_ERROR

  # describe the datatype of the coverage cell values (range type)
  # note: assign dimensionless quantity
  # R
  $PSQL -c "INSERT INTO ps9_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band1', 0, \
              (SELECT id FROM ps9_range_data_type WHERE name='$c_datatype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_datatype' LIMIT 1));" > /dev/null || exit $RC_ERROR
  # G
  $PSQL -c "INSERT INTO ps9_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band2', 1, \
              (SELECT id FROM ps9_range_data_type WHERE name='$c_datatype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_datatype' LIMIT 1));" > /dev/null || exit $RC_ERROR
  # B
  $PSQL -c "INSERT INTO ps9_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band3', 2, \
              (SELECT id FROM ps9_range_data_type WHERE name='$c_datatype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_datatype' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps9_crs (uri) VALUES ('$c_crs');" > /dev/null # no harm if duplicate error is thrown
  $PSQL -c "INSERT INTO ps9_domain_set (coverage_id, native_crs_id) \
            VALUES ($c_id, ARRAY[(SELECT id FROM ps9_crs WHERE uri='$c_crs')]);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_gridded_domain_set (coverage_id, grid_origin) \
            VALUES ($c_id, '{$min_x_geo_coord, $max_y_geo_coord}');" > /dev/null || exit $RC_ERROR
  # grid axes:
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 0);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 1);" > /dev/null || exit $RC_ERROR
  # offset vectors
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=0), \
              '{$x_res,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=1), \
              '{0,$y_res}');" > /dev/null || exit $RC_ERROR
}


# ------------------------------------------------------------------------------
#
# import 2D greyscale data
#
function import_mr()
{
  c=$COLL
  X=256
  Y=211

  c_colltype='GreySet'
  c_datatype='char'
  c_covtype='RectifiedGridCoverage'

  c_crs='http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0.1/Index2D'
  min_x_geo_coord=0
  max_y_geo_coord=211
  x_res='1'
  y_res='-1'

  c_band='value'

  #
  # START
  #

  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values ($c_datatype) inv_png(\$1)" -f "$TESTDATA_PATH"/mr_1.png > /dev/null || exit $RC_ERROR

  # general coverage information (name, type, ...)
  $PSQL -c "INSERT INTO ps9_coverage (name, gml_type_id, native_format_id) \
            VALUES ('$c', (SELECT id FROM ps9_gml_subtype WHERE subtype='$c_covtype'), \
            (SELECT id FROM ps9_mime_type WHERE mime_type='application/x-octet-stream'));" > /dev/null || exit $RC_ERROR

  # get the coverage id
  c_id=$($PSQL -c  "SELECT id FROM ps9_coverage WHERE name = '$c' " | head -3 | tail -1) > /dev/null || exit $RC_ERROR

  # get the collection OID (note: take the first OID)
  c_oid=$($RASQL -q "select oid(m) from $c as m" --out string | grep ' 1:' | awk -F ':' '{print $2}' | tr -d ' \n') > /dev/null || exit $RC_ERROR

  # range set: link the coverage to the rasdaman collection
  $PSQL -c "INSERT INTO ps9_rasdaman_collection (name, oid) VALUES ('$c', $c_oid);" > /dev/null
  $PSQL -c "INSERT INTO ps9_range_set (coverage_id, storage_id) VALUES (\
              (SELECT id FROM ps9_coverage WHERE name='$c'), \
              (SELECT id FROM ps9_rasdaman_collection WHERE name='$c'));" > /dev/null || exit $RC_ERROR

  # describe the datatype of the coverage cell values (range type)
  # note: assign dimensionless quantity
  $PSQL -c "INSERT INTO ps9_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band', 0, \
              (SELECT id FROM ps9_range_data_type WHERE name='$c_datatype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_datatype' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps9_crs (uri) VALUES ('$c_crs');" > /dev/null # no harm if duplicate error is thrown
  $PSQL -c "INSERT INTO ps9_domain_set (coverage_id, native_crs_id) \
            VALUES ($c_id, ARRAY[(SELECT id FROM ps9_crs WHERE uri='$c_crs')]);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_gridded_domain_set (coverage_id, grid_origin) \
            VALUES ($c_id, '{$min_x_geo_coord, $max_y_geo_coord}');" > /dev/null || exit $RC_ERROR
  # grid axes:
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 0);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 1);" > /dev/null || exit $RC_ERROR

  # offset vectors
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=0), \
              '{$x_res,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=1), \
              '{0,$y_res}');" > /dev/null || exit $RC_ERROR
}


# ------------------------------------------------------------------------------
#
# import testdata: data is only imported if it isn't already in petascope
#
function import_data()
{
  COLLECTIONS="rgb mr eobstest"
  
  for COLL in $COLLECTIONS; do
    check_collection
    if [ $? -ne 0 ]; then
      drop_colls $COLL
      drop_petascope
      logn "importing $COLL... "
      
      counter=0
      while [ 1 -eq 1 ]; do
      
        if [ "$COLL" == "rgb" ]; then
          import_rgb && break
        elif [ "$COLL" == "mr" ]; then
          import_mr && break
        elif [ "$COLL" == "eobstest" ]; then
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
      log "$COLL already imported."
    fi
  done
}
