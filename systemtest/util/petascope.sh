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
  id=`$PSQL -c  "select id from PS9_Coverage where name = '$c' " | head -3 | tail -1`
  test1=0
  if [[ "$id" == \(0*\) ]]; then
    test1=1
  fi

  $RASQL -q 'select r from RAS_COLLECTIONNAMES as r' --out string | egrep "\b$c\b" > /dev/null
  test2=$?
  [ $test1 -eq 0 -a $test2 -eq 0 ]
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
  M=`psql -d petascopedb -c "SELECT * FROM information_schema.tables WHERE table_schema = 'public'" | grep ps9_multipoint`
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
  local TESTDATA_PATH="$1"
  if [ ! -f "$TESTDATA_PATH/eobs.nc" ]; then
    error "testdata file $TESTDATA_PATH/eobs.nc not found"
  fi
  c=$COLL

  X=101
  Y=232
  T=6

  c_colltype='ShortSet3'
  c_basetype='short'
  c_covtype='RectifiedGridCoverage'

  c_crs_t="$SECORE_URL"'/crs/OGC/0/Temporal?epoch="1950-01-01T00:00:00"&uom="d"'
  c_crs_s="$SECORE_URL"'/crs/EPSG/0/4326'
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
  $RASQL -q "insert into $c values ($c_basetype) inv_netcdf(\$1, \"vars=tg\")" -f "$TESTDATA_PATH"/eobs.nc > /dev/null || exit $RC_ERROR

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
              (SELECT id FROM ps9_range_data_type WHERE name='$c_basetype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_basetype' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps9_crs (uri) SELECT '$c_crs_t' WHERE NOT EXISTS (SELECT 1 FROM ps9_crs WHERE uri='$c_crs_t');" > /dev/null
  $PSQL -c "INSERT INTO ps9_crs (uri) SELECT '$c_crs_s' WHERE NOT EXISTS (SELECT 1 FROM ps9_crs WHERE uri='$c_crs_s');" > /dev/null
  $PSQL -c "INSERT INTO ps9_domain_set (coverage_id, native_crs_ids) \
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
  local TESTDATA_PATH="$1"
  if [ ! -f "$TESTDATA_PATH/rgb.png" ]; then
    error "testdata file $TESTDATA_PATH/rgb.png not found"
  fi
  c=$COLL
  X=400
  Y=344

  c_colltype='RGBSet'
  c_basetype='unsigned char'
  c_covtype='RectifiedGridCoverage'

  c_crs="$SECORE_URL"'/crs/OGC/0/Index2D'
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
              (SELECT id FROM ps9_range_data_type WHERE name='$c_basetype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_basetype' LIMIT 1));" > /dev/null || exit $RC_ERROR
  # G
  $PSQL -c "INSERT INTO ps9_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band2', 1, \
              (SELECT id FROM ps9_range_data_type WHERE name='$c_basetype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_basetype' LIMIT 1));" > /dev/null || exit $RC_ERROR
  # B
  $PSQL -c "INSERT INTO ps9_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band3', 2, \
              (SELECT id FROM ps9_range_data_type WHERE name='$c_basetype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_basetype' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps9_crs (uri) SELECT '$c_crs' WHERE NOT EXISTS (SELECT 1 FROM ps9_crs WHERE uri='$c_crs');" > /dev/null
  $PSQL -c "INSERT INTO ps9_domain_set (coverage_id, native_crs_ids) \
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
# import 2D char data
#
function import_mr()
{
  local TESTDATA_PATH="$1"
  if [ ! -f "$TESTDATA_PATH/mr_1.png" ]; then
    error "testdata file $TESTDATA_PATH/mr_1.png not found"
  fi

  c=$COLL
  X=256
  Y=211

  c_colltype='GreySet'
  c_basetype='char'
  c_covtype='RectifiedGridCoverage'

  c_crs="$SECORE_URL"'/crs/OGC/0/Index2D'
  min_x_geo_coord=0
  max_y_geo_coord=211
  x_res='1'
  y_res='-1'

  c_band='value'

  #
  # START
  #

  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values ($c_basetype) inv_png(\$1)" -f "$TESTDATA_PATH"/mr_1.png > /dev/null || exit $RC_ERROR

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
              (SELECT id FROM ps9_range_data_type WHERE name='$c_basetype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_basetype' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps9_crs (uri) SELECT '$c_crs' WHERE NOT EXISTS (SELECT 1 FROM ps9_crs WHERE uri='$c_crs');" > /dev/null
  $PSQL -c "INSERT INTO ps9_domain_set (coverage_id, native_crs_ids) \
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

  # add GMLCOV and OWS extra metadata
  GMLCOV=gmlcov
  OWS=ows
  $PSQL -c "INSERT INTO ps9_extra_metadata (coverage_id, metadata_type_id, value) VALUES (\
              $c_id, (SELECT id FROM ps9_extra_metadata_type WHERE type='$OWS'),\
              'test ows metadata');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_extra_metadata (coverage_id, metadata_type_id, value) VALUES (\
              $c_id, (SELECT id FROM ps9_extra_metadata_type WHERE type='$GMLCOV'),\
              'test gmlcov metadata');" > /dev/null || exit $RC_ERROR
}

# ------------------------------------------------------------------------------
#
# import 3D irregular time-series
#
function import_irr_cube_1()
{
  # No need to check for file: the payload is dynamically created here inside the function

  c=$COLL # = collection name = coverage name
  X=10    # regular axis
  Y=10    # regular axis
  Z=6     # irregular axis
  Z_coeffs=( 0 1 2 3 5 8 )

  c_colltype='UShortSet3'     # See `rasdl -p` -> ``set types'
  c_basetype='unsigned short' # See ql-guide.pdf, Table 1 ``rasdl base types''
  c_covtype='ReferenceableGridCoverage' # See GMLCOV

  c_crs="$SECORE_URL"'/crs/OGC/0/Index3D'
  min_x_geo_coord=0
  min_y_geo_coord=0
  min_z_geo_coord=0
  x_res='1'
  y_res='1'
  z_res='1'

  c_band='value'

  #
  # START
  #

  # init the collection
  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values marray x in [0:$(( $Z-1 )),0:$(( $Y-1 )),0:$(( $Z-1 ))] values ($c_basetype)0" > /dev/null || exit $RC_ERROR

  # Fill in the slices row-by-row with domainSet reflected in the rangeSet: Z_coeffs are hundreds, X are tens, Y are units.
  sequence=( 0 1 2 3 4 5 6 7 8 9 )
  for SLICE in $( seq 0 $(( ${#Z_coeffs[@]}-1 )) ); do
      for ROW in $( seq 0 $(( $X-1 )) ); do
          # make this available to subshell in sed
          export LINE_BASE=$(( ${Z_coeffs[$SLICE]} * 100 + $ROW * 10 ))
          # update the line. Eg SLICE=2, ROW=3 -> LINE_BASE=230
          # RasQL = "update $c as m set m[3,0:9,2] assign (ushort) < [0:9] 230, 231, 232, 233, 234, 235, 236, 237, 238, 239 >
          $RASQL -q "update $c as m set m[$ROW,0:$(( $Y-1 )),$SLICE] \
                        assign ($c_basetype) < \
                               [0:$(( $Y-1 ))] \
                                $( echo ${sequence[@]} \
                                | sed -r 's/[0-9]/echo $(( & + $LINE_BASE ))/ge' \
                                | sed 's/ echo/,/g') >" > /dev/null || exit $RC_ERROR
      done
  done


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
              (SELECT id FROM ps9_range_data_type WHERE name='$c_basetype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_basetype' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps9_crs (uri) SELECT '$c_crs' WHERE NOT EXISTS (SELECT 1 FROM ps9_crs WHERE uri='$c_crs');" > /dev/null
  $PSQL -c "INSERT INTO ps9_domain_set (coverage_id, native_crs_ids) \
            VALUES ($c_id, ARRAY[(SELECT id FROM ps9_crs WHERE uri='$c_crs')]);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_gridded_domain_set (coverage_id, grid_origin) \
            VALUES ($c_id, '{$min_x_geo_coord, $min_y_geo_coord, $min_z_geo_coord}');" > /dev/null || exit $RC_ERROR

  # grid axes:
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 0);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 1);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 2);" > /dev/null || exit $RC_ERROR

  # offset vectors
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=0), \
              '{$x_res,0,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=1), \
              '{0,$y_res,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=2), \
              '{0,0,$z_res}');" > /dev/null || exit $RC_ERROR

  # coefficients
  for i in "${!Z_coeffs[@]}"; do
      $PSQL -c "INSERT INTO ps9_vector_coefficients (grid_axis_id, coefficient, coefficient_order) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=2), \
              ${Z_coeffs[$i]}, $i)" > /dev/null || exit $RC_ERROR
  done
}


# ------------------------------------------------------------------------------
#
# import 2D geo-referenced data
#
function import_mst()
{
  local TESTDATA_PATH="$1"
  if [ ! -f "$TESTDATA_PATH/mean_summer_airtemp.tif" ]; then
    error "testdata file $TESTDATA_PATH/mean_summer_airtemp.tif not found"
  fi

  c=$COLL
  X=885
  Y=710

  c_colltype='GreySet'
  c_basetype='char'
  c_covtype='RectifiedGridCoverage'

  c_crs="$SECORE_URL"'/crs/EPSG/0/4326'

  min_x_geo_coord="111.975"
  max_y_geo_coord="-8.975"
  x_res='0.05'
  y_res='-0.05'

  c_band='value'

  #
  # START
  #

  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values ($c_basetype) inv_tiff(\$1)" -f "$TESTDATA_PATH"/mean_summer_airtemp.tif > /dev/null || exit $RC_ERROR

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
              (SELECT id FROM ps9_range_data_type WHERE name='$c_basetype'), \
              (SELECT id FROM ps9_quantity WHERE description='$c_basetype' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps9_crs (uri) SELECT '$c_crs' WHERE NOT EXISTS (SELECT 1 FROM ps9_crs WHERE uri='$c_crs');" > /dev/null
  $PSQL -c "INSERT INTO ps9_domain_set (coverage_id, native_crs_ids) \
            VALUES ($c_id, ARRAY[(SELECT id FROM ps9_crs WHERE uri='$c_crs')]);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_gridded_domain_set (coverage_id, grid_origin) \
            VALUES ($c_id, '{$max_y_geo_coord, $min_x_geo_coord}');" > /dev/null || exit $RC_ERROR
  # grid axes:
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 0);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 1);" > /dev/null || exit $RC_ERROR

  # offset vectors
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=0), \
              '{0, $x_res}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps9_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps9_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=1), \
              '{$y_res, 0}');" > /dev/null || exit $RC_ERROR

  # initialize WMS
  "$INITWMS" australia_wms mean_summer_airtemp EPSG:4326 -l '2:4:8:16' -h localhost -p $WCPS_PORT > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    log "Warning: WMS initialization for mean_summer_airtemp failed."
  fi
  "$FILLPYR" mean_summer_airtemp --tasplit > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    log "Warning: WMS pyramid creation for mean_summer_airtemp failed."
  fi
}


# ------------------------------------------------------------------------------
#
# import testdata: data is only imported if it isn't already in petascope
# $1 - testdata dir holding files to be imported
#
function import_petascope_data()
{
  local TESTDATA_PATH="$1"
  if [ ! -d "$TESTDATA_PATH" ]; then
    error "testdata path $TESTDATA_PATH not found."
  fi

  res=$(check_multipoint)
  local multi_coll=""
  if [ $res -eq 0 ]; then
    multi_coll="Parksmall"
  fi
  COLLECTIONS="rgb mr eobstest mean_summer_airtemp irr_cube_1 $multi_coll"
  for COLL in $COLLECTIONS; do
    check_cov $COLL
    if [ $? -ne 0 ]; then
      drop_colls $COLL
      drop_petascope $COLL
      logn "importing $COLL... "

      counter=0
      while [ 1 -eq 1 ]; do

        if [ "$COLL" == "rgb" ]; then
          import_rgb "$TESTDATA_PATH" && break
        elif [ "$COLL" == "mr" ]; then
          import_mr "$TESTDATA_PATH" && break
        elif [ "$COLL" == "eobstest" ]; then
          import_eobs "$TESTDATA_PATH" && break
        elif [ "$COLL" == "mean_summer_airtemp" ]; then
          import_mst "$TESTDATA_PATH" && break
        elif [ "$COLL" == "irr_cube_1" ]; then
          import_irr_cube_1 "$TESTDATA_PATH" && break
        elif [ "$COLL" == "Parksmall" ]; then
          import_pointcloud_data "$TESTDATA_PATH" && break
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

function drop_petascope_data()
{
  res=$(check_multipoint)
  if [ $res -eq 0 ]; then
    multi_coll="Parksmall"
  fi
  COLLECTIONS="rgb mr eobstest mean_summer_airtemp irr_cube_1 $multi_coll"
  drop_petascope $COLLECTIONS
  drop_colls $COLLECTIONS
  log "dropping wms..."
  "$DROPWMS" australia_wms > /dev/null
}

#
# Import point cloud demo data
#

function import_pointcloud_data()
{
  local TESTDATA_PATH="$1"
  if [ ! -d "$TESTDATA_PATH" ]; then
    error "testdata path $TESTDATA_PATH not found."
  fi

  PC_DATASET="Parksmall"
  PC_FILE="Parksmall.xyz"
  PC_CRS="$SECORE_URL"'/crs/EPSG/0/4327'

  id=`$PSQL -c  "select id from ps9_coverage where name='$PC_DATASET'" | head -3 | tail -1`
  test "$id" != "0"
  if [ $? -eq 0 ]; then
    logn "dropping $PC_DATASET... "
    $PSQL -c  "delete from ps9_coverage where name='$PC_DATASET'"
    echo ok.
  else
    log "$PC_DATASET not found in the database."
  fi

  logn "importing $PC_DATASET... "
  python "$UTIL_SCRIPT_DIR"/import_pointcloud.py --file "$TESTDATA_PATH/$PC_FILE" --crs "$PC_CRS"
  echo ok.

}
