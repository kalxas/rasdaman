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

PROG=$(basename $0)

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

#
# paths
#
TESTDATA_PATH="$SCRIPT_DIR/testdata"
[ -d "$TESTDATA_PATH" ] || error "Testdata directory not found: $TESTDATA_PATH"
ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Oracle directory not found: $ORACLE_PATH"
OUTPUT_PATH="$SCRIPT_DIR/output"
mkdir -p "$OUTPUT_PATH"

TEST_FILE="$TESTDATA_PATH/tos_O1_2001-2002.nc"
EXIT_CODE_FAIL=255
EXIT_CODE_OK=0

COLL3D=test_netcdf_3ds
readonly BASETYPES="octet char short ushort long ulong float double struct"

# arg 1: base type
# arg 2: dimension
get_collname()
{
    echo "$1_$2d_systemtest"
}

# arg 1: base type
get_max()
{
    local -r bt="$1"
    case "$bt" in
        char) echo "128c";;
        ushort) echo "32768us";;
        ulong) echo "2147483648ul";;
    esac
}

generate_axes()
{
    if [ $1 -eq 3 ]; then
        echo "t, x, y"
    else
        echo "t, z, x, y"
    fi
}

create_collection()
{
    local -r base_type="$1"
    local -r dim="$2"
    local -r cn="$(get_collname $base_type $dim)"
    cell_type="$base_type"
    if [ "$base_type" == "struct" ]; then
        cell_type="(v1 short, v2 short)"
    fi
    $RASQL -q "create type ${base_type}_Array${dim}D_SystemTest as ${cell_type} mdarray [ $(generate_axes $dim) ]" > /dev/null 2>&1
    $RASQL -q "create type ${base_type}_Set${dim}D_SystemTest as set ( ${base_type}_Array${dim}D_SystemTest )" > /dev/null 2>&1
    $RASQL -q "create collection $cn ${base_type}_Set${dim}D_SystemTest" > /dev/null 2>&1
}

drop_data()
{
    log ""
    log "dropping testdata"
    for base_type in $BASETYPES; do
        for dim in 3 4; do
            $RASQL -q "drop collection $(get_collname $base_type $dim)" > /dev/null 2>&1
            $RASQL -q "drop type ${base_type}_Set${dim}D_SystemTest" > /dev/null 2>&1
            $RASQL -q "drop type ${base_type}_Array${dim}D_SystemTest" > /dev/null 2>&1
        done
    done
    log "done."
}

_cleanup()
{
  drop_data
}
trap '_cleanup' EXIT


# ------------------------------------------------------------------------------
# test dependencies
#
check_rasdaman

check_type FloatSet3

#
# if GRIB is not configured, don't execute the test
#
$RASQL -q "drop collection float_3d" > /dev/null 2>&1
$RASQL -q "create collection float_3d FloatSet3" > /dev/null
$RASQL -q 'insert into float_3d values inv_netcdf($1, "vars=values")' -f "$TESTDATA_PATH/float_3d.nc" --quiet 2>&1 | grep "rasdaman error 218: Exception: Conversion format is not supported." > /dev/null
if [ $? -eq 0 ]; then
    log "netCDF format not supported, skipping test."
    $RASQL -q "drop collection float_3d" > /dev/null 2>&1
    exit $RC_OK
fi
$RASQL -q "drop collection float_3d" > /dev/null 2>&1

# ------------------------------------------------------------------------------
# create test collections
#

check_output()
{
    if [ -f rasql_1.nc ]; then
        mv rasql_1.nc "$out_file"
        check_netcdf
        ncdump "$out_file" > "$out_cdl_file"
        grep "$test_filename" known_fails > /dev/null
        if [ $? -ne 0 ]; then
            cmp "$out_cdl_file" "$oracle_file" > /dev/null
            check_result 0 $? "exported output matches oracle"
        fi
    fi
}

update_filenames()
{
    test_file="$TESTDATA_PATH/$filename.nc"
    oracle_file="$ORACLE_PATH/$filename.cdl"
    out_file="$OUTPUT_PATH/$filename.nc"
    out_cdl_file="$OUTPUT_PATH/$filename.cdl"
}

for dim in 3 4; do
    for base_type in $BASETYPES; do
        #[ "$base_type" == "octet" ] || continue
        coll_name="$(get_collname $base_type $dim)"
        filename="${base_type}_${dim}d"
        update_filenames
        
        log "-----------------------------------------------------------------------------"
        log "testing dimension '$dim', base type '$base_type'"
        log ""
        
        create_collection $base_type $dim
        
        vars="values"
        [ "$base_type" == "struct" ] && vars="v1;v2"
        if [ -f "$test_file" ]; then
            $RASQL -q 'insert into '$coll_name' values inv_netcdf($1, "vars='$vars'")' -f "$test_file" > /dev/null
            check_result 0 $? "inserting $filename"
        else
            if [ $dim -eq 3 ]; then
                $RASQL -q "insert into $coll_name values marray i in [0:1,0:9,0:4] values ($base_type) (i[0] + i[1] + i[2] + $(get_max $base_type))" > /dev/null
            else
                $RASQL -q "insert into $coll_name values marray i in [0:1,0:1,0:9,0:4] values ($base_type) (i[0] + i[1] + i[2] + i[3] + $(get_max $base_type))" > /dev/null
            fi
            check_result 0 $? "inserting generated $base_type data"
        fi
        
        #$RASQL -q 'select csv(c) from '$coll_name' as c' --out string
        if [ -f "$json_file" ]; then
            json_params=$(cat "$json_file" | tr -d '\n' | tr -s ' ' | sed 's/"/\\"/g')
            echo "$json_params" > $json_file.ql
        fi
        $RASQL -q 'select encode(c, "netcdf", "vars='$vars'") from '$coll_name' as c' --out file > /dev/null
        check_result 0 $? "exporting ${base_type}_${dim}d_systemtest to netcdf"
        
        check_output
    done
done

#
# Test JSON export
#
# It seems impossible to make this work by loading the JSON format parameters from a 
# file, so that's why they are listed manually here...
#

setup_json_export()
{
    log "-----------------------------------------------------------------------------"
    log "testing JSON export, $base_type ${dim}D"
    coll_name="$(get_collname $base_type $dim)"
    filename="${base_type}_${dim}d_json_export"
    update_filenames
}

base_type=octet
dim=3
setup_json_export
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15, 45] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"global\": { \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\" } }}") from '$coll_name' as c' --out file > /dev/null
check_output

base_type=octet
dim=4
setup_json_export
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"depth\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15] }, \"depth\": { \"type\": \"float\", \"metadata\": { \"standard_name\": \"depth\", \"long_name\": \"depth\", \"units\": \"meters\", \"axis\": \"Z\" }, \"data\": [45.2] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"global\": { \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\" } }}") from '$coll_name' as c' --out file > /dev/null
check_output

# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
