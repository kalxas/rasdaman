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
    $RASQL -q "drop collection $cn" > /dev/null 2>&1
    $RASQL -q "create collection $cn ${base_type}_Set${dim}D_SystemTest" > /dev/null 2>&1
}

drop_collection()
{
    local -r base_type="$1"
    local -r dim="$2"
    local -r cn="$(get_collname $base_type $dim)"
    $RASQL -q "drop collection $cn" > /dev/null 2>&1
    $RASQL -q "drop type ${base_type}_Set${dim}D_SystemTest" > /dev/null 2>&1
    $RASQL -q "drop type ${base_type}_Array${dim}D_SystemTest" > /dev/null 2>&1
}

cmp_files()
{
    local out="$1"
    local ora="$2"

    # on CentOS 7 we get only L as a suffix for long metadata scalars..
    # so it's necessary to fix the output/oracle
    sed -i 's/LL/L/g' "$out"
    sed 's/LL/L/g' "$ora" > "$out.oracle"
    ora="$out.oracle"
        
    cmp "$out" "$ora" > /dev/null 2>&1
    check_result 0 $? "exported output matches oracle"
}

check_output()
{
    if [ -f rasql_1.nc ]; then
        mv rasql_1.nc "$out_file"
        ncdump "$out_file" > "$out_cdl_file"

        check_known_fail "$1"
        if [ $? -eq 0 ]; then
            log "$1 is a known_fail, skipping test"
        else
            cmp_files "$out_cdl_file" "$oracle_file"
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

drop_data()
{
    log ""
    log "dropping testdata"
    local bt=
    local dim=
    for bt in $BASETYPES; do
        for dim in 3 4; do
           drop_collection "$base_type" "$dim"
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
check_netcdf
check_type FloatSet3

# ------------------------------------------------------------------------------
# test helpers
# ------------------------------------------------------------------------------


# Test JSON decode parameters
#
run_json_import_test()
{
    local -r insert_func="$1"
    local -r base_type="$3"
    local -r dim="$4"
    local -r desc="$2"

    log "-----------------------------------------------------------------------------"
    log "testing JSON import: $desc, $base_type ${dim}D"
    coll_name="$(get_collname $base_type $dim)"
    filename="${base_type}_${dim}d_json_import_$desc"
    update_filenames
    test_file="$TESTDATA_PATH/${base_type}_${dim}d.nc"
    vars="values"
    [ "$base_type" == "struct" ] && vars="v1;v2"

    create_collection "$base_type" "$dim"
    $insert_func
    check_result 0 $? "inserting generated $base_type data with $desc parameter"
    $RASQL -q 'select encode(c, "netcdf", "vars='$vars'") from '$coll_name' as c' --out file > /dev/null
    check_result 0 $? "exporting $coll_name to netcdf"
    check_output
    drop_collection "$base_type" "$dim"
}

# Test "transpose" for conversion from netCDF
#
run_transpose_import_test()
{
    local -r base_type="$1"
    local -r dim="$2"

    log "-----------------------------------------------------------------------------"
    log "testing transposition on import: $base_type ${dim}D"
    coll_name="$(get_collname $base_type $dim)"
    filename="${base_type}_${dim}d_transpose_import"
    update_filenames
    test_file="$TESTDATA_PATH/${base_type}_${dim}d.nc"
    vars="values"
    [ "$base_type" == "struct" ] && vars="v1;v2"
    if [ -f "$test_file" ]; then
        create_collection "$base_type" "$dim"

        # insert file
        x=$((dim-1))
        y=$((dim-2))
        if [ "$base_type" == "struct" ]; then
            $RASQL -q 'insert into '$coll_name' values decode($1, "netCDF", "{\"variables\": [\"v1\", \"v2\"], \"transpose\": ['"${x}"','"${y}"']}")' \
                   -f "$test_file" > /dev/null 2>&1
        else
            $RASQL -q 'insert into '$coll_name' values decode($1, "netCDF", "{\"variables\": [\"'$vars'\"], \"transpose\": ['"${x}"','"${y}"']}")' \
                   -f "$test_file" > /dev/null 2>&1
        fi
        check_result 0 $? "inserting generated $base_type data with transpose parameter"

        # export data
        $RASQL -q 'select encode(c, "netcdf", "vars='$vars'") from '$coll_name' as c' --out file > /dev/null
        check_result 0 $? "exporting $coll_name to netcdf"
        check_output
        drop_collection "$base_type" "$dim"
    else
        log "No available ${dim}-dimensional test data of type $base_type."
    fi
}

setup_json_export()
{
    local suffix="$1"
    coll_name="$(get_collname $base_type $dim)"
    filename="${base_type}_${dim}d_json_export${suffix}"
    log "testing json export, $filename"
    update_filenames
}

# ------------------------------------------------------------------------------
# execute tests
# ------------------------------------------------------------------------------


#
# if NetCDF is not configured, don't execute the test
#
$RASQL -q "drop collection float_3d" > /dev/null 2>&1
$RASQL -q "create collection float_3d FloatSet3" > /dev/null
$RASQL -q 'insert into float_3d values inv_netcdf($1, "vars=values")' \
       -f "$TESTDATA_PATH/float_3d.nc" --quiet 2>&1 | \
       grep "rasdaman error 218: Exception: Conversion format is not supported." > /dev/null
if [ $? -eq 0 -o "$HAVE_NETCDF" != ON ]; then
    log "netCDF format not supported, skipping test."
    $RASQL -q "drop collection float_3d" > /dev/null 2>&1
    exit $RC_OK
fi
$RASQL -q "drop collection float_3d" > /dev/null 2>&1

# ------------------------------------------------------------------------------
# Test JSON decode parameters
#
insert_variables() {
    $RASQL -q 'insert into '$coll_name' values decode($1, "netCDF", "{\"variables\": [\"'$vars'\"]}")' \
           -f "$test_file" > /dev/null 2>&1
}
run_json_import_test insert_variables variables octet 3

insert_filepath() {
    $RASQL -q 'insert into '$coll_name' values decode($1, "netCDF", "{\"variables\": [\"'$vars'\"], \"filePaths\": [\"'$test_file'\"]}")' \
           -f "$SCRIPT_DIR/test.sh" > /dev/null 2>&1
}
run_json_import_test insert_filepath filepath octet 3

insert_subsetDomain() {
    $RASQL -q 'insert into '$coll_name' values decode($1, "netCDF", "{\"variables\": [\"'$vars'\"], \"filePaths\": [\"'$test_file'\"], \"subsetDomain\": \"[0:0,0:9,0:2]\"}")' \
           -f "$SCRIPT_DIR/test.sh" > /dev/null 2>&1
}
run_json_import_test insert_subsetDomain subsetDomain octet 3

# ------------------------------------------------------------------------------
# transposing with wrong indices returns an error.
#
log "-----------------------------------------------------------------------------"
log "testing bad transposition on import: octet 3D"
filename="$octet_3d_bad_transpose_import"
update_filenames
create_collection octet 3
coll_name="$(get_collname octet 3)"
vars="values"
$RASQL -q 'insert into '$coll_name' values decode($1, "netCDF", "{\"variables\": [\"'$vars'\"], \"transpose\": [0,1]}")' \
       -f "$TESTDATA_PATH/octet_3d.nc" > /dev/null 2>&1
check_result 255 $? "generating $base_type data with transpose parameter and bad indices is not possible"
drop_collection octet 3


# Now verifying correct transpositions

# transpose insert tests
for dim in 3 4; do
    for base_type in $BASETYPES; do
        run_transpose_import_test $base_type $dim
    done
done

# other insert tests
for dim in 3 4; do
    for base_type in $BASETYPES; do
        #[ "$base_type" == "octet" ] || continue
        coll_name="$(get_collname $base_type $dim)"
        filename="${base_type}_${dim}d"
        update_filenames

        log "-----------------------------------------------------------------------------"
        log "testing dimension $dim, base type '$base_type'"

        create_collection "$base_type" "$dim"

        vars="values"
        [ "$base_type" == "struct" ] && vars="v1;v2"
        if [ -f "$test_file" ]; then
            $RASQL -q 'insert into '$coll_name' values decode($1, "netCDF", "vars='$vars'")' -f "$test_file" > /dev/null 2>&1
            check_result 0 $? "inserting $filename"
        else
            if [ $dim -eq 3 ]; then
                $RASQL -q "insert into $coll_name values marray i in [0:1,0:9,0:4] values ($base_type) (i[0] + i[1] + i[2] + $(get_max $base_type))" > /dev/null
            else
                $RASQL -q "insert into $coll_name values marray i in [0:1,0:1,0:9,0:4] values ($base_type) (i[0] + i[1] + i[2] + i[3] + $(get_max $base_type))" > /dev/null
            fi
            check_result 0 $? "inserting generated $base_type data"
        fi

        if [ -f "$json_file" ]; then
            json_params=$(cat "$json_file" | tr -d '\n' | tr -s ' ' | sed 's/"/\\"/g')
            echo "$json_params" > $json_file.ql
        fi
        $RASQL -q 'select encode(c, "netcdf", "vars='$vars'") from '$coll_name' as c' --out file > /dev/null
        check_result 0 $? "exporting $coll_name to netcdf"
        check_output "$filename"
    done
done


# ------------------------------------------------------------------------------
# Test JSON export
#
# It seems impossible to make this work by loading the JSON format parameters from a
# file, so that's why they are listed manually here...

log "-----------------------------------------------------------------------------"

base_type=octet
dim=3
setup_json_export
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15, 45] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\" } }") from '$coll_name' as c' --out file > /dev/null
check_output

base_type=octet
dim=4
setup_json_export
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"depth\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15] }, \"depth\": { \"type\": \"float\", \"metadata\": { \"standard_name\": \"depth\", \"long_name\": \"depth\", \"units\": \"meters\", \"axis\": \"Z\" }, \"data\": [45.2] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\" } }") from '$coll_name' as c' --out file > /dev/null
check_output

base_type=struct
dim=3
setup_json_export
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15, 45] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos1\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } }, \"tos2\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\"} }") from '$coll_name' as c' --out file > /dev/null
check_output

# ------------------------------------------------------------------------------
# Test netcdf encode with nodata option (for fill values) and json parameters

base_type=octet
dim=3
setup_json_export "_fillvalues"
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15, 45] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\" }, \"nodata\":[0] }") from '$coll_name' as c' --out file > /dev/null
check_output

base_type=struct
dim=3
setup_json_export "_fillvalues"
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15, 45] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos1\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } }, \"tos2\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\"}, \"nodata\":[-9999, -9999] }") from '$coll_name' as c' --out file > /dev/null
check_output

# ------------------------------------------------------------------------------
# Test netcdf encode with transpose option and json parameters

base_type=octet
dim=3
setup_json_export "_transpose"
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15, 45] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\"}, \"transpose\": [2,1] }") from '$coll_name' as c' --out file > /dev/null
check_output

base_type=double
dim=3
setup_json_export "_transpose"
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15, 45] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\"}, \"transpose\": [2,1] }") from '$coll_name' as c' --out file > /dev/null
check_output

base_type=struct
dim=3
setup_json_export "_transpose"
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"lat\", \"lon\"], \"variables\": { \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15, 45] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos1\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } }, \"tos2\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\"}, \"transpose\": [1,2] }") from '$coll_name' as c' --out file > /dev/null
check_output

# ------------------------------------------------------------------------------
# Test netcdf encode with grid mapping variable

base_type=float
dim=3
setup_json_export "_grid_mapping"
$RASQL -q 'select encode(c, "netcdf", "{ \"dimensions\": [\"time\", \"lat\", \"lon\"], \"variables\": { \"rotated_pole\":{\"name\":\"rotated_pole\",\"type\": \"int\", \"data\": [], \"metadata\":{\"grid_mapping_name\":\"rotated_latitude_longitude\",\"grid_north_pole_longitude\":\"-170\",\"units\":\"degrees\",\"axis\":\"X\"}}, \"time\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"time\", \"long_name\": \"time\", \"units\": \"days since 2001-1-1\", \"axis\": \"T\", \"calendar\": \"360_day\", \"bounds\": \"time_bnds\", \"original_units\": \"seconds since 2001-1-1\" }, \"data\": [15, 45] }, \"lat\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"latitude\", \"long_name\": \"latitude\", \"units\": \"degrees_north\", \"axis\": \"Y\", \"bounds\": \"lat_bnds\", \"original_units\": \"degrees_north\" }, \"data\": [-79.5, -78.5, -77.5, -76.5, -75.5, -74.5, -73.5, -72.5, -71.5, -70.5] }, \"lon\": { \"type\": \"double\", \"metadata\": { \"standard_name\": \"longitude\", \"long_name\": \"longitude\", \"units\": \"degrees_east\", \"axis\": \"X\", \"bounds\": \"lon_bnds\", \"original_units\": \"degrees_east\" }, \"data\": [1, 3, 5, 7, 9] }, \"tos\": { \"type\": \"byte\", \"metadata\": { \"standard_name\": \"sea_surface_temperature\", \"long_name\": \"Sea Surface Temperature\", \"units\": \"K\", \"cell_methods\": \"time: mean (interval: 30 minutes)\", \"_FillValue\": 1e20, \"missing_value\": 1e20, \"original_name\": \"sosstsst\", \"original_units\": \"degC\", \"history\": \" At 16:37:23 on 01/11/2005: CMOR altered the data in the following ways: added 2.73150E+02 to yield output units; Cyclical dimension was output starting at a different lon;\" } } }, \"metadata\": { \"title\": \"IPSL model output prepared for IPCC Fourth Assessment SRES A2 experiment\", \"institution\": \"IPSL (Institut Pierre Simon Laplace, Paris, France)\", \"source\": \"IPSL-CM4_v1 (2003) : atmosphere : LMDZ (IPSL-CM4_IPCC, 96x71x19), ocean ORCA2 (ipsl_cm4_v1_8, 2x2L31); sea ice LIM (ipsl_cm4_v\", \"contact\": \"Sebastien Denvil, sebastien.denvil@ipsl.jussieu.fr\", \"project_id\": \"IPCC Fourth Assessment\", \"table_id\": \"Table O1 (13 November 2004)\", \"experiment_id\": \"SRES A2 experiment\", \"realization\": 1, \"cmor_version\": 0.96, \"Conventions\": \"CF-1.0\", \"history\": \"YYYY/MM/JJ: data generated; YYYY/MM/JJ+1 data transformed At 16:37:23 on 01/11/2005, CMOR rewrote data to comply with CF standards and IPCC Fourth Assessment requirements\", \"references\": \"Dufresne et al, Journal of Climate, 2015, vol XX, p 136\", \"comment\": \"Test drive\"}, \"transpose\": [2,1] }") from '$coll_name' as c' --out file > /dev/null
check_output

# ------------------------------------------------------------------------------
# Test netcdf encode with crs + bbox

logn "testing encode with crs+bbox format parameters results in crs variable in output... "

coll=test_mean_summer_airtemp_rasql
drop_colls $coll
$RASQL -q "create collection $coll GreySet" > /dev/null
$RASQL -q "insert into $coll values decode(\$1)" -f "$TESTDATA_PATH/mean_summer_airtemp.tif" > /dev/null

outfile="$OUTPUT_PATH/crs_latitude_longitude"
$RASQL -q 'SELECT encode(c, "application/netcdf" , "{\"dimensions\":[\"Lon\",\"Lat\"],\"variables\":[
{\"type\":\"double\",\"data\":[112.225,112.725,113.225,113.725,114.225,114.725,115.225,115.725,116.225,116.725,117.225,117.725,118.225,118.725,119.225,119.725,120.225,120.725,121.225,121.725,122.225,122.725,123.225,123.725,124.225,124.725,125.225,125.725,126.225,126.725,127.225,127.725,128.225,128.725,129.225,129.725,130.225,130.725,131.225,131.725,132.225,132.725,133.225,133.725,134.225,134.725,135.225,135.725,136.225,136.725,137.225,137.725,138.225,138.725,139.225,139.725,140.225,140.725,141.225,141.725,142.225,142.725,143.225,143.725,144.225,144.725,145.225,145.725,146.225,146.725,147.225,147.725,148.225,148.725,149.225,149.725,150.225,150.725,151.225,151.725,152.225,152.725,153.225,153.725,154.225,154.725,155.225,155.725,156.225],\"name\":\"Lon\",\"metadata\":{}},
{\"type\":\"double\",\"data\":[-9.224999999999987,-9.724999999999987,-10.224999999999987,-10.724999999999987,-11.224999999999987,-11.724999999999987,-12.224999999999987,-12.724999999999987,-13.224999999999987,-13.724999999999987,-14.224999999999987,-14.724999999999987,-15.224999999999987,-15.724999999999987,-16.224999999999987,-16.724999999999987,-17.224999999999987,-17.724999999999987,-18.224999999999987,-18.724999999999987,-19.224999999999987,-19.724999999999987,-20.224999999999987,-20.724999999999987,-21.224999999999987,-21.724999999999987,-22.224999999999987,-22.724999999999987,-23.224999999999987,-23.724999999999987,-24.224999999999987,-24.724999999999987,-25.224999999999987,-25.724999999999987,-26.224999999999987,-26.724999999999987,-27.224999999999987,-27.724999999999987,-28.224999999999987,-28.724999999999987,-29.224999999999987,-29.724999999999987,-30.224999999999987,-30.724999999999987,-31.224999999999987,-31.724999999999987,-32.22499999999999,-32.72499999999999,-33.22499999999999,-33.72499999999999,-34.22499999999999,-34.72499999999999,-35.22499999999999,-35.72499999999999,-36.22499999999999,-36.72499999999999,-37.22499999999999,-37.72499999999999,-38.22499999999999,-38.72499999999999,-39.22499999999999,-39.72499999999999,-40.22499999999999,-40.72499999999999,-41.22499999999999,-41.72499999999999,-42.22499999999999,-42.72499999999999,-43.22499999999999,-43.72499999999999,-44.22499999999999],\"name\":\"Lat\",\"metadata\":{}},
{\"type\":\"char\",\"name\":\"Gray\",\"metadata\":{\"description\":\"\",\"units\":\"10^0\"}}],
\"geoReference\":{\"crs\":\"EPSG:4326\",\"bbox\":{\"xmin\":111.975,\"ymin\":-44.474999999999987,\"xmax\":156.475,\"ymax\":-8.974999999999987}},
\"nodata\":[]}")  FROM test_mean_summer_airtemp_rasql AS c' --out file --outfile "$outfile" > /dev/null
if [ $? -eq 0 ]; then
    out="$outfile.nc"
    [ -f "$out" ] || out="$outfile.unknown"
    if [ -f "$out" ]; then
        ncdump -h "$out" > "$outfile.cdl"
        ora="$ORACLE_PATH/crs_latitude_longitude.cdl"
        [ -f "$ora.$OS_VERSION" ] && ora="$ora.$OS_VERSION"
        cmp -s "$outfile.cdl" "$ora"
        check
    else
        check_failed
    fi
else
    check_failed
fi
drop_colls test_mean_summer_airtemp_rasql

# ------------------------------------------------------------------------------
print_summary
exit $RC
