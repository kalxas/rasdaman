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
  $RASQL -q "insert into $c values decode(\$1)" -f "$TESTDATA_PATH"/rgb.png > /dev/null || exit $RC_ERROR

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
  $RASQL -q "insert into $c values decode(\$1)" -f "$TESTDATA_PATH"/mr_1.png > /dev/null || exit $RC_ERROR

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
# import 3D geo-referenced irregular time series
#
function import_irr_cube_2()
{
  local TESTDATA_PATH="$1"
  data_folder="${TESTDATA_PATH}/${COLL}"
  datatype_file="${data_folder}/GeostatPredictionTypes.txt"
  if [ ! -d "$data_folder" ] || \
     [ $( find "$data_folder" -name *.geo.tif | wc -l ) -ne 4 ] || \
     [ ! -f "$datatype_file" ]
  then
    error "testdata in "$data_folder" not found or partially missing."
  fi

  c=$COLL
  c_basetype='GeostatPredictionPixel'
  c_marraytype='GeostatPredictionCube'
  c_colltype='GeostatPredictionSet3'
  c_crs_s="$SECORE_URL"'/crs/EPSG/0/32633'
  c_crs_t="$SECORE_URL"'/crs/OGC/0/AnsiDate'
  t_vector=2 # coefficients in time are relative to this bi-daily vector

  # SWE metadata (prediction/variance)
  declare -a c_swe_uom_code=('ug/m3' '(ug/m3)2')
  declare -a c_swe_label=('prediction' 'variance')
  declare -a c_swe_description=('kriged mean surface PM10 exposure' 'kriging error on PM10 prediction')

  #
  # START
  #
  # Drop/re-insert data types for this collection (re-insert on existing types throws error)
  $RASDL --delsettype  "$c_colltype"      > /dev/null || exit $RC_ERROR
  $RASDL --delmddtype  "$c_marraytype"    > /dev/null || exit $RC_ERROR
  $RASDL --delbasetype "$c_basetype"      > /dev/null || exit $RC_ERROR
  $RASDL --insert --read "$datatype_file" > /dev/null || exit $RC_ERROR

  # Import
  $RASIMPORT -d "$data_folder" \
             -s 'tif' \
             -t  ${c_marraytype}:${c_colltype} \
             --coll $c \
             --coverage-name $c \
             --crs-uri  "$c_crs_s":"$c_crs_t" \
             --crs-order 0:1:2  \
             --3D top \
             --csz "$t_vector" \
             --z-coords 148653:148655:148657:148660 > /dev/null || exit $RC_ERROR
             # ANSI date numbers for 2008 Jan {1-3-5-8} 00:00:00Z (ANSI dates are integers: no hour resolution)

  # rasimport is still poor on SWE metadata handling: update it with richer information (range type)
  _qry="SELECT id FROM ps_coverage WHERE name='${c}'"
  c_id=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" )
  for index in 0 1
  do
      # get ID of UoM
      _qry="SELECT id FROM ps_uom WHERE code='${c_swe_uom_code[$index]}'"
      c_swe_uom_id[$index]=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" )
      if [ -z "${c_swe_uom_id[$index]}"]
      then
          _qry="INSERT INTO ps_uom (code) VALUES ('${c_swe_uom_code[$index]}') RETURNING id"
          c_swe_uom_id[$index]=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" | head -n 1 )
      fi
      # Get ID of SWE Quantity
      _qry="SELECT id FROM ps_quantity WHERE uom_id=${c_swe_uom_id[$index]} \
               AND label='${c_swe_label[$index]}' \
               AND description='${c_swe_description[$index]}'"
      c_swe_quantity_id[$index]=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" )
      if [ -z "${c_swe_quantity_id[$index]}" ]
      then
          _qry="INSERT INTO ps_quantity (uom_id, label, description) \
                VALUES (${c_swe_uom_id[$index]}, '${c_swe_label[$index]}', '${c_swe_description[$index]}') \
                RETURNING id"
          c_swe_quantity_id[$index]=$( $PSQL -X -P t -P format=unaligned -c "${_qry}" | head -n 1 )
          fi
      # Finally, update the range type component associated to this quantity:
      $PSQL -c "UPDATE ps_range_type_component SET field_id=${c_swe_quantity_id[$index]} \
                WHERE coverage_id=$c_id AND component_order=$index;" > /dev/null || exit $RC_ERROR
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
  "$INITWMS" australia_wms $c EPSG:4326 -l '2:4:8:16' -h localhost -p $WCPS_PORT > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    log "Warning: WMS initialization for $c failed."
  fi
  "$FILLPYR" $c --tasplit > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    log "Warning: WMS pyramid creation for $c failed."
  fi
}

# ------------------------------------------------------------------------------
#
# import 4D floating-point data
#
function import_double_1d()
{
  c=$COLL

  c_colltype='DoubleSet1'
  c_basetype='double'
  c_rangetype='double'
  c_covtype='GridCoverage'

  c_band='value'

  #
  # START
  #

  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values marray i in [0:20] values (double)(i / 2)" > /dev/null || exit $RC_ERROR

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
}

# ------------------------------------------------------------------------------
#
# import 4D floating-point data
#
function import_float_4d()
{
  c=$COLL

  c_colltype='SystemtestFloatSet4'
  c_basetype='float'
  c_rangetype='float'
  c_covtype='GridCoverage'

  c_band='value'

  #
  # START
  #

  # import custom collection type
  $RASDL -p | egrep "\b${c_colltype}\b" > /dev/null
  if [ $? -ne 0 ]; then
    cat > /tmp/types.dl << EOF
typedef marray <float, 4> SystemtestFloatArray4;
typedef set<SystemtestFloatArray4> SystemtestFloatSet4;
EOF
    $RASDL -r /tmp/types.dl -i > /dev/null 2>&1 || error "Failed inserting custom 4D float collection type."
  fi

  $RASQL -q "create collection $c $c_colltype" > /dev/null || exit $RC_ERROR
  $RASQL -q "insert into $c values marray i in [0:0,0:0,0:39,-20:19] values (float)(i[2] * i[3])" > /dev/null || exit $RC_ERROR

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
  COLLECTIONS="rgb mr eobstest mean_summer_airtemp mean_summer_airtemp_repeat float_4d double_1d irr_cube_1 irr_cube_2 $multi_coll"
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
        elif [ "$COLL" == "mean_summer_airtemp" -o "$COLL" == "mean_summer_airtemp_repeat" ]; then
          import_mst "$TESTDATA_PATH" && break
        elif [ "$COLL" == "float_4d" ]; then
          import_float_4d "$TESTDATA_PATH" && break
        elif [ "$COLL" == "double_1d" ]; then
          import_double_1d "$TESTDATA_PATH" && break
        elif [ "$COLL" == "irr_cube_1" ]; then
          import_irr_cube_1 "$TESTDATA_PATH" && break
        elif [ "$COLL" == "irr_cube_2" ]; then
          import_irr_cube_2 "$TESTDATA_PATH" && break
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
  COLLECTIONS="rgb mr eobstest mean_summer_airtemp mean_summer_airtemp_repeat float_4d double_1d irr_cube_1 irr_cube_2 $multi_coll"
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
  PC_CRS1="$SECORE_URL"'/crs/EPSG/0/27700'
	PC_CRS2="$SECORE_URL"'/crs/EPSG/0/5701'

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
	$PSQL -c  "DELETE FROM ps_coverage where name='$PC_DATASET'" > /dev/null
  else
    log "$PC_DATASET not found in the database."
  fi

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

	# If the crss do not exist, insert them into ps_crs and retrieve the ids
	CRS_ID1=`$PSQL -X -P t -P format=unaligned -c "SELECT id FROM ${DB_TABLE_PREFIX}_crs WHERE uri='$PC_CRS1';" | head -3 | tail -1`
  if [ -z "$CRS_ID1" ]; then
		$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_crs(uri) VALUES('$PC_CRS1');" > /dev/null || exit 1 $RC_ERROR
    CRS_ID1=`$PSQL -c "SELECT id FROM ${DB_TABLE_PREFIX}_crs WHERE uri='$PC_CRS1';" | head -3 | tail -1`
	fi

	CRS_ID2=`$PSQL -X -P t -P format=unaligned -c "SELECT id FROM ${DB_TABLE_PREFIX}_crs WHERE uri='$PC_CRS2';" | head -3 | tail -1`
  if [ -z "$CRS_ID2" ]; then
		$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_crs(uri) VALUES('$PC_CRS2');" > /dev/null || exit 1 $RC_ERROR
    CRS_ID2=`$PSQL -c "SELECT id FROM ${DB_TABLE_PREFIX}_crs WHERE uri='$PC_CRS2';" | head -3 | tail -1`
	fi

	# Insert (coverage_id, crs_id) into ps_domain_set
	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_domain_set VALUES($COVERAGE_ID,'{${CRS_ID1},${CRS_ID2}}');" > /dev/null || exit $RC_ERROR

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

	# Inserting coverage extension into ps_bounding_box table_schema
  lower_left=`$PSQL -X -P t -P format=unaligned -c "SELECT min(St_X(coordinate)) || ',' || min(St_Y(coordinate)) || ',' || min(St_Z(coordinate)) \
		  FROM ${DB_TABLE_PREFIX}_coverage,    ${DB_TABLE_PREFIX}_multipoint WHERE ${DB_TABLE_PREFIX}_coverage.name='$PC_COVERAGE' \
			AND ${DB_TABLE_PREFIX}_coverage.id=${DB_TABLE_PREFIX}_multipoint.coverage_id;" | head -3 | tail -1`
	upper_right=`$PSQL -X -P t -P format=unaligned -c "SELECT max(St_X(coordinate)) || ',' || max(St_Y(coordinate)) || ',' || max(St_Z(coordinate)) \
			FROM ${DB_TABLE_PREFIX}_coverage,       ${DB_TABLE_PREFIX}_multipoint 	WHERE ${DB_TABLE_PREFIX}_coverage.name='$PC_COVERAGE' \
			AND ${DB_TABLE_PREFIX}_coverage.id = ${DB_TABLE_PREFIX}_multipoint.coverage_id;" | head -3 | tail -1`
  $PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_bounding_box(coverage_id, lower_left, upper_right) \
			VALUES($COVERAGE_ID,'{${lower_left}}','{${upper_right}}');" > /dev/null || exit $RC_ERROR

}

#
# Import tsurf tin demo data
#

function import_tin_data()
{
	#set -x
	local TESTDATA_PATH="$1"
  if [ ! -d "$TESTDATA_PATH" ]; then
    error "testdata path $TESTDATA_PATH not found."
  fi

  TIN_COVERAGE="wc_dtm_small"
  TIN_FILE="wc_dtm_small.ts"
  TIN_CRS="$SECORE_URL"'/crs/EPSG/0/27700'

	# Batch insert size
  MAX_BATCH_INSERT=50

	VERTEX_SECTION="VRTX"
	TRIANGLE_SECTION="TRGL"
	TIN_STR="TIN"

	# Data type of the range set
	c_rangetype='unsigned long'

  # Number of components in each line, here is 3
	TIN_COMPONENTS=5

	# Building MultiSurface Coverage Information

	# Inserting the general coverage info to ps_coverage table
	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_coverage(name, gml_type_id, native_format_id) VALUES( '$TIN_COVERAGE', \
		(SELECT id FROM ${DB_TABLE_PREFIX}_gml_subtype WHERE subtype='MultiSurfaceCoverage'), \
		(SELECT id FROM ${DB_TABLE_PREFIX}_mime_type WHERE mime_Type='application/x-octet-stream'));" > /dev/null || exit 1 # replace with $RC_ERROR

	# Finding the coverage id
	COVERAGE_ID=`$PSQL -c "SELECT id FROM ${DB_TABLE_PREFIX}_coverage WHERE name='$TIN_COVERAGE'" | head -3 | tail -1`

	# Inserting the range type (attributes) information in ps_range_type_component
	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_range_type_component (coverage_id, name, data_type_id, component_order, field_id, field_table) \
		VALUES ($COVERAGE_ID, 'tin_value', (SELECT id FROM ps_range_data_type WHERE name='$c_rangetype'), 0, (SELECT id FROM ps_quantity \
		WHERE label='$c_rangetype' AND description='primitive' LIMIT 1), '${DB_TABLE_PREFIX}_quantity');" > /dev/null || exit $RC_ERROR

  # If the crs does not exist, insert it into ps_crs and retrieve the id
	CRS_ID=`$PSQL -X -P t -P format=unaligned -c "SELECT id FROM ${DB_TABLE_PREFIX}_crs \
		WHERE uri='$TIN_CRS';" | head -3 | tail -1`
	if [ -z "$CRS_ID" ]; then
		$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_crs(uri) VALUES('$TIN_CRS');" > /dev/null || exit $RC_ERROR
	  CRS_ID=`$PSQL -c "SELECT id FROM ${DB_TABLE_PREFIX}_crs WHERE uri='$TIN_CRS';" | head -3 | tail -1`
	fi

  # Insert (coverage_id, crs_id) into ps_domain_set
	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_domain_set VALUES($COVERAGE_ID,'{${CRS_ID}}');" > /dev/null || exit $RC_ERROR

	# Find the start line number of vertex and triangle sections
	VRTX_START_LINE=`grep -n "$VERTEX_SECTION" $TESTDATA_PATH/$TIN_FILE | head -n 1 | cut -f1 -d:`
	TRGL_START_LINE=`grep -n "$TRIANGLE_SECTION" $TESTDATA_PATH/$TIN_FILE | head -n 1 | cut -f1 -d:`

	# Update ps_uom with the new uom for meter (m)
	UOM=`$PSQL -X -P t -P format=unaligned -c "SELECT id FROM ${DB_TABLE_PREFIX}_uom WHERE code='m';" | head -3 | tail -1`
	if [ -z "$UOM" ]; then
		  $PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_uom(code) VALUES('m');" > /dev/null || exit $RC_ERROR
		  UOM=`$PSQL -c "SELECT id FROM ${DB_TABLE_PREFIX}_uom WHERE code='m';" | head -3 | tail -1`
	fi
	QUANTITY=`$PSQL -X -P t -P format=unaligned -c "SELECT id FROM ${DB_TABLE_PREFIX}_quantity \
		WHERE label='H' AND uom_id=$UOM AND description='height';" | head -3 | tail -1`
	if [ -z "$QUANTITY" ]; then
		  $PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_quantity(uom_id,	label, description, definition_uri) \
				VALUES($UOM,'H','height','$TIN_CRS2');" > /dev/null || exit $RC_ERROR
	fi

	# Total line in the file
	END_LINE=`wc -l < $TESTDATA_PATH/$TIN_FILE`

	# Loop over lines in TRGL section starting from TRGL_START_LINE
	insert_stmt="INSERT INTO ${DB_TABLE_PREFIX}_multisurface(coverage_id,surface,value) VALUES"
	batch_line_count=0
	total_line_count=`expr $TRGL_START_LINE - 1`

	awk "NR >= $TRGL_START_LINE" $TESTDATA_PATH/$TIN_FILE |
	while read line;
	do
		total_line_count=`expr $total_line_count + 1`

		# Check if the line starts with TRGL
		if [[ "$line" == ${TRIANGLE_SECTION}* ]]
		then
			# Split the trgl line
			IFS=' ' read -a tarray <<< "$line"

			# Skip the line if it is incomplete
			[ "${#tarray[@]}" -ne "$TIN_COMPONENTS" ] && continue

			# Extract point id from the array an get the point coordinates
			# Go to vertex line number using head and tail
			line_no=`expr ${tarray[1]} + $VRTX_START_LINE - 1`
			point1=$(head -${line_no} $TESTDATA_PATH/$TIN_FILE | tail -1 | cut -d ' ' -f 3,4,5)
			IFS=' ' read -a parray <<< "$point1"
			range1=${parray[2]}
			point1="${parray[0]} ${parray[1]}"

			line_no=`expr ${tarray[2]} + $VRTX_START_LINE - 1`
			point2=$(head -${line_no} $TESTDATA_PATH/$TIN_FILE | tail -1 | cut -d ' ' -f 3,4,5)
			IFS=' ' read -a parray <<< "$point2"
			range2=${parray[2]}
			point2="${parray[0]} ${parray[1]}"

			line_no=`expr ${tarray[3]} + $VRTX_START_LINE - 1`
			point3=$(head -${line_no} $TESTDATA_PATH/$TIN_FILE | tail -1 | cut -d ' ' -f 3,4,5)
			IFS=' ' read -a parray <<< "$point3"
			range3=${parray[2]}
			point3="${parray[0]} ${parray[1]}"

			range="${range1},${range2},${range3}"

			if [ "$batch_line_count" -gt 0 ]
				then
					insert_stmt+=","
			fi

		  insert_stmt+="($COVERAGE_ID,'TIN(((${point1},${point2},${point3},${point1})))','{$range}')"
			batch_line_count=`expr $batch_line_count + 1`

			# If MAX_BATCH_INSERT limit is reached, insert into the table
			if [ "$batch_line_count" -eq "$MAX_BATCH_INSERT" ]
			then
				$PSQL -c "$insert_stmt" > /dev/null || exit $RC_ERROR
				insert_stmt="INSERT INTO ${DB_TABLE_PREFIX}_multisurface(coverage_id,surface,value) VALUES"
				batch_line_count=0
			fi
		fi

		# If it is the last line, insert the rest
		if [ "$total_line_count" == $END_LINE ]
		then
			$PSQL -c "$insert_stmt" > /dev/null || exit $RC_ERROR
		fi

	done

	# Inserting coverage extension into ps_bounding_box table_schema
	lower_left=`$PSQL -X -P t -P format=unaligned -c "SELECT St_XMin(ST_Extent(surface)) || ',' || St_YMin(ST_Extent(surface)) \
		FROM ${DB_TABLE_PREFIX}_coverage,${DB_TABLE_PREFIX}_multisurface WHERE ${DB_TABLE_PREFIX}_coverage.name='$TIN_COVERAGE' \
		AND ${DB_TABLE_PREFIX}_coverage.id=${DB_TABLE_PREFIX}_multisurface.coverage_id;" | head -3 | tail -1`

	upper_right=`$PSQL -X -P t -P format=unaligned -c "SELECT St_XMax(ST_Extent(surface)) || ',' || St_YMax(ST_Extent(surface)) \
		FROM ${DB_TABLE_PREFIX}_coverage,${DB_TABLE_PREFIX}_multisurface 	WHERE ${DB_TABLE_PREFIX}_coverage.name='$TIN_COVERAGE' \
		AND ${DB_TABLE_PREFIX}_coverage.id = ${DB_TABLE_PREFIX}_multisurface.coverage_id;" | head -3 | tail -1`
	$PSQL -c "INSERT INTO ${DB_TABLE_PREFIX}_bounding_box(coverage_id, lower_left, upper_right) \
		VALUES($COVERAGE_ID,'{${lower_left}}','{${upper_right}}');" > /dev/null || exit $RC_ERROR

	logn "Tin coverage $TIN_COVERAGE is imported."
	log ok.

}
