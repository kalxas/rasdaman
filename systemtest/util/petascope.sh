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
  c_rangetype='short'
  c_covtype='RectifiedGridCoverage'

  # SWE metadata
  c_swe_uom_code='Celsius'
  c_swe_label='tg'
  c_swe_description='mean temperature'
  c_swe_definition_uri='http://eca.knmi.nl/download/ensembles/ensembles.php'
  c_swe_nil_value='-9999'
  c_swe_nil_reason='http://www.opengis.net/def/nil/OGC/0/missing'


  # domainSet
  c_crs_t="$SECORE_URL"'/crs/OGC/0/Temporal?epoch="1950-01-01T00:00:00"&uom="d"'
  c_crs_s="$SECORE_URL"'/crs/EPSG/0/4326'
  grid_origin_t='0.5'   # 0 days from epoch (interval centre: 12h of 01-Jan-1950)
  grid_origin_x='25.25' # UL pixel centre
  grid_origin_y='75.25' #
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
  $PSQL -c "INSERT INTO ps_coverage (name, gml_type_id, native_format_id) \
            VALUES ('$c', (SELECT id FROM ps_gml_subtype WHERE subtype='$c_covtype'), \
            (SELECT id FROM ps_mime_type WHERE mime_type='application/x-octet-stream'));" > /dev/null || exit $RC_ERROR

  # get the coverage id
  c_id=$($PSQL -c  "SELECT id FROM ps_coverage WHERE name = '$c' " | head -3 | tail -1) > /dev/null || exit $RC_ERROR

  # get the collection OID (note: take the first OID)
  c_oid=$($RASQL -q "select oid(m) from $c as m" --out string | grep ' 1:' | awk -F ':' '{print $2}' | tr -d ' \n') > /dev/null || exit $RC_ERROR

  # range set: link the coverage to the rasdaman collection
  $PSQL -c "INSERT INTO ps_rasdaman_collection (name, oid) VALUES ('$c', $c_oid);" > /dev/null
  $PSQL -c "INSERT INTO ps_range_set (coverage_id, storage_id) VALUES (\
              (SELECT id FROM ps_coverage WHERE name='$c'), \
              (SELECT id FROM ps_rasdaman_collection WHERE name='$c'));" > /dev/null || exit $RC_ERROR

  # describe the datatype of the coverage cell values (range type)
  # get ID of UoM
  _qry="SELECT id FROM ps_uom WHERE code='${c_swe_uom_code}'"
  c_swe_uom_id=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" )
  if [ -z "$c_swe_uom_id" ]
  then
      _qry="INSERT INTO ps_uom (code) VALUES ('${c_swe_uom_code}') RETURNING id"
      c_swe_uom_id=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" | head -n 1 )
  fi
  # Get IDs of NILs
  _qry="SELECT id FROM ps_nil_value WHERE value='${c_swe_nil_value}' AND reason='${c_swe_nil_reason}'"
  c_swe_nil_id=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" )
  if [ -z "$c_swe_nil_id" ]
  then
      _qry="INSERT INTO ps_nil_value (value, reason) VALUES ('${c_swe_nil_value}', '${c_swe_nil_reason}') RETURNING id"
      c_swe_nil_id=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" | head -n 1 )
  fi
  # Get ID of SWE Quantity
  _qry="SELECT id FROM ps_quantity WHERE uom_id=${c_swe_uom_id} AND label='${c_swe_label}' AND description='${c_swe_description}'"
  c_swe_quantity_id=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" )
  if [ -z "$c_swe_quantity_id" ]
  then
      _qry="INSERT INTO ps_quantity (uom_id, label, description, definition_uri, nil_ids) \
            VALUES (${c_swe_uom_id}, '${c_swe_label}', '${c_swe_description}', '${c_swe_definition_uri}', ARRAY[${c_swe_nil_id}]) \
            RETURNING id"
      c_swe_quantity_id=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" | head -n 1 )
  fi
  # Finally, create the range type component associated to this quantity:
  $PSQL -c "INSERT INTO ps_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band', 0, \
              (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), \
              ${c_swe_quantity_id});" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps_crs (uri) SELECT '$c_crs_t' WHERE NOT EXISTS (SELECT 1 FROM ps_crs WHERE uri='$c_crs_t');" > /dev/null
  $PSQL -c "INSERT INTO ps_crs (uri) SELECT '$c_crs_s' WHERE NOT EXISTS (SELECT 1 FROM ps_crs WHERE uri='$c_crs_s');" > /dev/null
  $PSQL -c "INSERT INTO ps_domain_set (coverage_id, native_crs_ids) \
            VALUES ($c_id, ARRAY[\
              (SELECT id FROM ps_crs WHERE uri='$c_crs_t'),
              (SELECT id FROM ps_crs WHERE uri='$c_crs_s')]\
            );" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_gridded_domain_set (coverage_id, grid_origin) \
            VALUES ($c_id, '{$grid_origin_t, $grid_origin_y, $grid_origin_x}');" > /dev/null || exit $RC_ERROR
  # grid axes:
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 0);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 1);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 2);" > /dev/null || exit $RC_ERROR
  # offset vectors (note: WGS84 has `Lat` first)
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=0), \
              '{$t_res,0,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=1), \
              '{0,0,$x_res}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=2), \
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

  c_colltype='RGBSet'
  c_basetype='char'
  c_rangetype='unsigned char'
  c_covtype='RectifiedGridCoverage'

  c_crs="$SECORE_URL"'/crs/OGC/0/Index2D'
  grid_origin_x=0
  grid_origin_y=343
  x_res='1'
  y_res='-1'

  c_band1='red'
  c_band2='green'
  c_band3='blue'

  #
  # START
  #

  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values ($c_basetype) inv_png(\$1)" -f "$TESTDATA_PATH"/rgb.png > /dev/null || exit $RC_ERROR

  # general coverage information (name, type, ...)
  $PSQL -c "INSERT INTO ps_coverage (name, gml_type_id, native_format_id) \
            VALUES ('$c', (SELECT id FROM ps_gml_subtype WHERE subtype='$c_covtype'), \
            (SELECT id FROM ps_mime_type WHERE mime_type='application/x-octet-stream'));" > /dev/null || exit $RC_ERROR

  # get the coverage id
  c_id=$($PSQL -c  "SELECT id FROM ps_coverage WHERE name = '$c' " | head -3 | tail -1) > /dev/null || exit $RC_ERROR

  # get the collection OID (note: take the first OID)
  c_oid=$($RASQL -q "select oid(m) from $c as m" --out string | grep ' 1:' | awk -F ':' '{print $2}' | tr -d ' \n') > /dev/null || exit $RC_ERROR

  # range set: link the coverage to the rasdaman collection
  $PSQL -c "INSERT INTO ps_rasdaman_collection (name, oid) VALUES ('$c', $c_oid);" > /dev/null
  $PSQL -c "INSERT INTO ps_range_set (coverage_id, storage_id) VALUES (\
              (SELECT id FROM ps_coverage WHERE name='$c'), \
              (SELECT id FROM ps_rasdaman_collection WHERE name='$c'));" > /dev/null || exit $RC_ERROR

  # describe the datatype of the coverage cell values (range type)
  # note: assign dimensionless quantity
  # R
  $PSQL -c "INSERT INTO ps_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band1', 0, \
              (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), \
              (SELECT id FROM ps_quantity WHERE label='$c_rangetype' AND description='primitive' LIMIT 1));" > /dev/null || exit $RC_ERROR
  # G
  $PSQL -c "INSERT INTO ps_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band2', 1, \
              (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), \
              (SELECT id FROM ps_quantity WHERE label='$c_rangetype' AND description='primitive' LIMIT 1));" > /dev/null || exit $RC_ERROR
  # B
  $PSQL -c "INSERT INTO ps_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band3', 2, \
              (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), \
              (SELECT id FROM ps_quantity WHERE label='$c_rangetype' AND description='primitive' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps_crs (uri) SELECT '$c_crs' WHERE NOT EXISTS (SELECT 1 FROM ps_crs WHERE uri='$c_crs');" > /dev/null
  $PSQL -c "INSERT INTO ps_domain_set (coverage_id, native_crs_ids) \
            VALUES ($c_id, ARRAY[(SELECT id FROM ps_crs WHERE uri='$c_crs')]);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_gridded_domain_set (coverage_id, grid_origin) \
            VALUES ($c_id, '{$grid_origin_x, $grid_origin_y}');" > /dev/null || exit $RC_ERROR
  # grid axes:
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 0);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 1);" > /dev/null || exit $RC_ERROR
  # offset vectors
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=0), \
              '{$x_res,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=1), \
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

  c_colltype='GreySet'
  c_basetype='char'
  c_rangetype='unsigned char'
  c_covtype='RectifiedGridCoverage'

  c_crs="$SECORE_URL"'/crs/OGC/0/Index2D'
  grid_origin_x=0
  grid_origin_y=210
  x_res='1'
  y_res='-1'

  c_band='value'

  #
  # START
  #

  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values ($c_basetype) inv_png(\$1)" -f "$TESTDATA_PATH"/mr_1.png > /dev/null || exit $RC_ERROR

  # general coverage information (name, type, ...)
  $PSQL -c "INSERT INTO ps_coverage (name, gml_type_id, native_format_id) \
            VALUES ('$c', (SELECT id FROM ps_gml_subtype WHERE subtype='$c_covtype'), \
            (SELECT id FROM ps_mime_type WHERE mime_type='application/x-octet-stream'));" > /dev/null || exit $RC_ERROR

  # get the coverage id
  c_id=$($PSQL -c  "SELECT id FROM ps_coverage WHERE name = '$c' " | head -3 | tail -1) > /dev/null || exit $RC_ERROR

  # get the collection OID (note: take the first OID)
  c_oid=$($RASQL -q "select oid(m) from $c as m" --out string | grep ' 1:' | awk -F ':' '{print $2}' | tr -d ' \n') > /dev/null || exit $RC_ERROR

  # range set: link the coverage to the rasdaman collection
  $PSQL -c "INSERT INTO ps_rasdaman_collection (name, oid) VALUES ('$c', $c_oid);" > /dev/null
  $PSQL -c "INSERT INTO ps_range_set (coverage_id, storage_id) VALUES (\
              (SELECT id FROM ps_coverage WHERE name='$c'), \
              (SELECT id FROM ps_rasdaman_collection WHERE name='$c'));" > /dev/null || exit $RC_ERROR

  # describe the datatype of the coverage cell values (range type)
  # note: assign dimensionless quantity
  $PSQL -c "INSERT INTO ps_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band', 0, \
              (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), \
              (SELECT id FROM ps_quantity WHERE label='$c_rangetype' AND description='primitive' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps_crs (uri) SELECT '$c_crs' WHERE NOT EXISTS (SELECT 1 FROM ps_crs WHERE uri='$c_crs');" > /dev/null
  $PSQL -c "INSERT INTO ps_domain_set (coverage_id, native_crs_ids) \
            VALUES ($c_id, ARRAY[(SELECT id FROM ps_crs WHERE uri='$c_crs')]);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_gridded_domain_set (coverage_id, grid_origin) \
            VALUES ($c_id, '{$grid_origin_x, $grid_origin_y}');" > /dev/null || exit $RC_ERROR
  # grid axes:
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 0);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 1);" > /dev/null || exit $RC_ERROR

  # offset vectors
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=0), \
              '{$x_res,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=1), \
              '{0,$y_res}');" > /dev/null || exit $RC_ERROR

  # add GMLCOV and OWS extra metadata
  GMLCOV=gmlcov
  OWS=ows
  $PSQL -c "INSERT INTO ps_extra_metadata (coverage_id, metadata_type_id, value) VALUES (\
              $c_id, (SELECT id FROM ps_extra_metadata_type WHERE type='$OWS'),\
              '<test>ows</test>');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_extra_metadata (coverage_id, metadata_type_id, value) VALUES (\
              $c_id, (SELECT id FROM ps_extra_metadata_type WHERE type='$GMLCOV'),\
              '<test>gmlcov</test>');" > /dev/null || exit $RC_ERROR
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
  c_rangetype='unsigned short'
  c_covtype='ReferenceableGridCoverage' # See GMLCOV

  c_crs="$SECORE_URL"'/crs/OGC/0/Index3D'
  grid_origin_x=0
  grid_origin_y=0
  grid_origin_t=0
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
  $PSQL -c "INSERT INTO ps_coverage (name, gml_type_id, native_format_id) \
            VALUES ('$c', (SELECT id FROM ps_gml_subtype WHERE subtype='$c_covtype'), \
            (SELECT id FROM ps_mime_type WHERE mime_type='application/x-octet-stream'));" > /dev/null || exit $RC_ERROR

  # get the coverage id
  c_id=$($PSQL -c  "SELECT id FROM ps_coverage WHERE name = '$c' " | head -3 | tail -1) > /dev/null || exit $RC_ERROR

  # get the collection OID (note: take the first OID)
  c_oid=$($RASQL -q "select oid(m) from $c as m" --out string | grep ' 1:' | awk -F ':' '{print $2}' | tr -d ' \n') > /dev/null || exit $RC_ERROR

  # range set: link the coverage to the rasdaman collection
  $PSQL -c "INSERT INTO ps_rasdaman_collection (name, oid) VALUES ('$c', $c_oid);" > /dev/null
  $PSQL -c "INSERT INTO ps_range_set (coverage_id, storage_id) VALUES (\
              (SELECT id FROM ps_coverage WHERE name='$c'), \
              (SELECT id FROM ps_rasdaman_collection WHERE name='$c'));" > /dev/null || exit $RC_ERROR

  # describe the datatype of the coverage cell values (range type)
  # note: assign dimensionless quantity
  $PSQL -c "INSERT INTO ps_range_type_component (coverage_id, name, component_order, data_type_id, field_id) VALUES (\
              $c_id, '$c_band', 0, \
              (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), \
              (SELECT id FROM ps_quantity WHERE label='$c_rangetype' AND description='primitive' LIMIT 1));" > /dev/null || exit $RC_ERROR

  # describe the geo (`index` in this case..) domain
  $PSQL -c "INSERT INTO ps_crs (uri) SELECT '$c_crs' WHERE NOT EXISTS (SELECT 1 FROM ps_crs WHERE uri='$c_crs');" > /dev/null
  $PSQL -c "INSERT INTO ps_domain_set (coverage_id, native_crs_ids) \
            VALUES ($c_id, ARRAY[(SELECT id FROM ps_crs WHERE uri='$c_crs')]);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_gridded_domain_set (coverage_id, grid_origin) \
            VALUES ($c_id, '{$grid_origin_x, $grid_origin_y, $grid_origin_t}');" > /dev/null || exit $RC_ERROR

  # grid axes:
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 0);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 1);" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_grid_axis (gridded_coverage_id, rasdaman_order) VALUES ($c_id, 2);" > /dev/null || exit $RC_ERROR

  # offset vectors
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=0), \
              '{$x_res,0,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=1), \
              '{0,$y_res,0}');" > /dev/null || exit $RC_ERROR
  $PSQL -c "INSERT INTO ps_rectilinear_axis (grid_axis_id, offset_vector) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=2), \
              '{0,0,$z_res}');" > /dev/null || exit $RC_ERROR

  # coefficients
  for i in "${!Z_coeffs[@]}"; do
      $PSQL -c "INSERT INTO ps_vector_coefficients (grid_axis_id, coefficient, coefficient_order) VALUES (\
              (SELECT id FROM ps_grid_axis WHERE gridded_coverage_id=$c_id AND rasdaman_order=2), \
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
  c_colltype='GreySet'
  c_marraytype='GreyImage'
  c_crs="$SECORE_URL"'/crs/EPSG/0/4326'

  #
  # START
  #
  $RASIMPORT -f "${TESTDATA_PATH}/mean_summer_airtemp.tif" \
             --coll $c \
             --coverage-name $c \
             -t  ${c_marraytype}:${c_colltype} \
             --crs-uri  "$c_crs" \
             --crs-order 1:0  > /dev/null || exit $RC_ERROR

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
# Import xyz-rgb point cloud demo data
#

function import_pointcloud_data()
{
  local TESTDATA_PATH="$1"
  if [ ! -d "$TESTDATA_PATH" ]; then
    error "testdata path $TESTDATA_PATH not found."
  fi

  PC_COVERAGE="Parksmall"
  PC_FILE="Parksmall.xyz"
  PC_CRS="$SECORE_URL"'/crs/EPSG/0/4327'

	# Batch insert size
  MAX_BATCH_INSERT=500

  # Number of components in each line, e.g., in xyz-rgb file is 6
	PC_COMPONENTS=6

	# Petascope table prefix
	DB_TABLE_PREFIX="ps"

	c_rangetype='unsigned char'

  id=`$PSQL -c  "select id from ps_coverage where name='$PC_DATASET'" | head -3 | tail -1`
  test "$id" != "0"
  if [ $? -eq 0 ]; then
    logn "dropping $PC_DATASET... "
    $PSQL -c  "delete from ps_coverage where name='$PC_DATASET'"
    echo ok.
  else
    log "$PC_DATASET not found in the database."
  fi

  logn "importing $PC_DATASET... "

	# Inserting the general coverage info to ps_coverage table
	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_coverage(name, gml_type_id, native_format_id) VALUES( '$PC_COVERAGE', \
		(SELECT id FROM ${DB_TABLE_PREFIX}_gml_subtype WHERE subtype='MultiPointCoverage'), \
		(SELECT id FROM ${DB_TABLE_PREFIX}_mime_type WHERE mime_Type='application/x-octet-stream'));" > /dev/null || exit $RC_ERROR

	# Finding the coverage id
  COVERAGE_ID=`$PSQL -c "SELECT id FROM ${DB_TABLE_PREFIX}_coverage WHERE name='$PC_COVERAGE'" | head -3 | tail -1`

	# Inserting the range type (attributes) information (rgb) in ps_range_type_component
	# coverage_id | name (band name) | data_type_id (8-bit signed integer from ps_range_data_type) |
	# component_order (r:0 g:1 b:2) | field_id (from ps_quantity col for unsigned char) | field_table (ps_quantity)
	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_range_type_component (coverage_id, name, data_type_id, component_order, field_id, field_table) \
		VALUES ($COVERAGE_ID, 'red', (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), 0, (SELECT id FROM ps_quantity WHERE \
		label='$c_rangetype' AND description='primitive' LIMIT 1), '${DB_TABLE_PREFIX}_quantity');" > /dev/null || exit $RC_ERROR

	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_range_type_component (coverage_id, name, data_type_id, component_order, field_id, field_table) \
		VALUES ($COVERAGE_ID, 'green', (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), 1, (SELECT id FROM ps_quantity WHERE \
		label='$c_rangetype' AND description='primitive' LIMIT 1), '${DB_TABLE_PREFIX}_quantity');" > /dev/null || exit $RC_ERROR

	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_range_type_component (coverage_id, name, data_type_id, component_order, field_id, field_table) \
		VALUES ($COVERAGE_ID, 'blue', (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), 2, (SELECT id FROM ps_quantity WHERE \
		label='$c_rangetype' AND description='primitive' LIMIT 1), '${DB_TABLE_PREFIX}_quantity');" > /dev/null || exit 1 $RC_ERROR

	# If the crs does not exist, insert it into ps_crs and retrieve the id
	CRS_ID=`$PSQL -X -P t -P format=unaligned -c "SELECT id FROM ${DB_TABLE_PREFIX}_crs WHERE uri='$PC_CRS';" | head -3 | tail -1`
  if [ -z "$CRS_ID" ]; then
		$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_crs(uri) VALUES('$PC_CRS');" > /dev/null || exit 1 $RC_ERROR
    CRS_ID=`$PSQL -c "SELECT id FROM ${DB_TABLE_PREFIX}_crs WHERE uri='$PC_CRS';" | head -3 | tail -1`
	fi

	# Insert (coverage_id, crs_id) into ps_domain_set
	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_domain_set VALUES($COVERAGE_ID,'{$CRS_ID}');" > /dev/null || exit $RC_ERROR

	# Reading the file line by line
	insert_stmt="INSERT INTO ${DB_TABLE_PREFIX}_multipoint(coverage_id,coordinate,value) VALUES"
	line_count=0

	while read line || [ -n "$line" ];
	do
	# Split the line
    IFS=' ' read -a array <<< "$line"

    # Skip the line if it is incomplete
    [ "${#array[@]}" -ne "$PC_COMPONENTS" ] && continue

    # Extract x,y,x,r,g, and b components from the array
    x=${array[0]}
    y=${array[1]}
    z=${array[2]}
    r=${array[3]}
    g=${array[4]}
    b=${array[5]}

    if [ "$line_count" -gt 0 ]
    then
	insert_stmt+=","
    fi
    insert_stmt+="($COVERAGE_ID,'POINT($x $y $z)','{$r,$g,$b}')"

    line_count=`expr $line_count + 1`

    # If MAX_BATCH_INSERT limit is reached, insert into the table
    if [ "$line_count" -eq "$MAX_BATCH_INSERT" ]
    then
			$PSQL -c "$insert_stmt" > /dev/null || exit $RC_ERROR
	insert_stmt="INSERT INTO ${DB_TABLE_PREFIX}_multipoint(coverage_id,coordinate,value) VALUES"
	line_count=0
    fi

	done < "$TESTDATA_PATH/$PC_FILE"

	# Inserting remaining points
	if [[ "$insert_stmt" == *POINT*  ]]; then
	$PSQL -c "$insert_stmt"  > /dev/null || exit $RC_ERROR
	fi

	log "Point cloud coverage $PC_COVERAGE is imported"
  log ok.

}
