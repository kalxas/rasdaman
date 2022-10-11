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
# Copyright 2003-2022 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#    test.sh
# Description
#    Import data to rasdaman with wcst_import.sh and check correctness.
#
################################################################################

# get script name
PROG=$(basename "$0")

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

SYSTEST_DIR=$(echo "$SCRIPT_DIR" | sed 's|\(.*/systemtest\).*|\1|')
[ -d "$SYSTEST_DIR/util" ] || error "could not determine system test dir: $SYSTEST_DIR"

. "$SYSTEST_DIR/util/common.sh"

# get the test datas and recipes from folder
TESTDATA_DIR="$SCRIPT_DIR/testdata"
# store files: *.ingest.json.log here
OUTPUT_DIR="$SCRIPT_DIR/output"

# The first argument, if specified, indicates a single testcase to execute.
SINGLE_TEST_CASE=
[ -n "$1" ] && SINGLE_TEST_CASE="$1"

# Return 0 if test with name $1 should be preserved in petascope, so it can be
# reused in later tests (test_wcs, test_wcps, etc); Otherwise, return 1.
KEEP_COVERAGE_PATTERN_LIST=("wcps" "wcs" "wms" "wmts" "tmp" "custom_recipe")
keep_coverage_by_folder_name() {
    local FOLDER_NAME
    for FOLDER_NAME in "${KEEP_COVERAGE_PATTERN_LIST[@]}"; do
        if [[ "$1" =~ $FOLDER_NAME ]]; then
            return 0
        fi
    done
    return 1
}
# after importing recipe, remove this resume.json file
clear_resume_file() {           
    find . -type f -name '*.resume.json' -delete    
}
write_to_failed_log() {
    # $1 is test case name
    # $2 is the reason
    log_failed "Test case: $1...failed. Reason: $2"
}
run_wcst_import() {
    # $1 is test case name
    # $2 is input ingredient file
    local test_outdir="$OUTPUT_DIR/$1"
    mkdir -p "$test_outdir"
    local wcst_import_log="$test_outdir/wcst_import.log"
    local ingredient_file="$2"
    $WCST_IMPORT "$ingredient_file" > "$wcst_import_log" 2>&1
}
skip_test() {
    # print the current test as skipped
    update_result
    status="$ST_SKIP"
    print_testcase_result "$test_case_name" "$status" "$total_test_no" "$curr_test_no"
}

# Check if petascope is deployed (imported from util/petascope.sh)
check_petascope || exit $RC_ERROR

# 0. clean output directory
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Test case which needs to create a collection in rasdaman to test
COLLECTION_EXISTS="collection_exists"
COLLECTION_NAME="test_wcst_import_collection_exists"

if [ -z "$SINGLE_TEST_CASE" ]; then
    total_test_no=$(ls -ld "$TESTDATA_DIR"/* | wc -l)
    testcases="$TESTDATA_DIR"/*
else
    total_test_no=1
    testcases="$TESTDATA_DIR/$SINGLE_TEST_CASE"
fi

curr_test_no=0

# 1. Iterate folders in test data

for test_case in $testcases; do

    test_case_name=$(basename "$test_case")
    curr_test_no=$((curr_test_no + 1))
    status="$ST_PASS"

    start_timer

    if [[ "$OS_VERSION" == "$OS_CENTOS7" && "$test_case_name" == *"overview"* ]]; then
        # NOTE: centos 7 and ubuntu 16.04 with gdal version 1.x does not support importing overview
        skip_test
        continue
    fi

    # each folder is a coverage with image files and recipe
    # 1.1 get the recipe in $test_case directory (NOTE: -L to find in symbolic directory)
    recipe_file_template=$(find -L "$test_case" -type f -name "ingest.template.json")
    if [ -z "$recipe_file_template" ]; then
        log "Test case '$test_case' is obsolete, removing."
        rm -rf "$test_case"
        continue
    fi

    # 1.2 instantiate ingest.template.json to ingest.json (this file will be used to ingest data)
    # with the current system configuration from systemtest/util/common.sh
    recipe_file="$test_case/ingest.json"
    sed -e "s@PETASCOPE_URL@$PETASCOPE_URL@g" \
        -e "s@SECORE_URL@$SECORE_URL@g" \
        -e "s@CURRENT_ABSOLUTE_DIR@$test_case@g" "$recipe_file_template" > "$recipe_file"

    # Get coverage id from ingest.json
    COVERAGE_ID=$(grep -Po -m 1 '"coverage_id":.*?[^\\]".*' "$recipe_file" | awk -F'"' '{print $4}')

    # 1.3 If test case name is "collection_exists" then need to import a test collection in rasdaman before
    if [ "$test_case_name" = "$COLLECTION_EXISTS" ]; then
        create_coll "$COLLECTION_NAME" RGBSet > /dev/null 2>&1
    fi

    mkdir -p "$OUTPUT_DIR/$test_case_name/"

    #
    # 1.4 execute wcst_import with $recipe_file
    #
    if [[ "$test_case" == *error* ]]; then

        # 1.4.1 This test returns error, then check with test.oracle
        outputError=$($WCST_IMPORT "$recipe_file" 2>&1)
	    echo "$outputError" > "$OUTPUT_DIR/$test_case_name/test.output"
        oracleError=$(cat "$test_case/test.oracle")

        # Check if output contains the error message from test.oracle
        if [[ "$outputError" == *"$oracleError"* ]]; then
            status="$ST_PASS"
        else            
            status="$ST_FAIL"
            write_to_failed_log "$test_case" "Error output is different from oracle output."            
        fi

    else

        # 1.4.2 This test is expected to succeed, if not that's an error
        if ! run_wcst_import "$test_case_name" "$recipe_file"; then
            status="$ST_FAIL"
            write_to_failed_log "$test_case" "Failed importing coverage $COVERAGE_ID."
        else
            if grep -q '"mock": true' "$recipe_file"; then
                # It is a mock import, nothing has been ingested
                continue
            fi

            # Check if the folder name is in unwanted delete coverage IDs list
            keep_coverage_by_folder_name "$test_case_name"
            IS_REMOVE=$?
            if [[ "$IS_REMOVE" == 1 ]]; then
                delete_coverage "$COVERAGE_ID"
                rc=$?
                if [[ $rc != 0 ]]; then                    
                    status="$ST_FAIL"         
                    write_to_failed_log "$test_case" "Failed deleting coverage $COVERAGE_ID."
                fi
            fi            
        fi

        # remove file resume.json
        clear_resume_file "$test_case"
    fi

    # 1.5 remove created collection in rasdaman
    if [ "$test_case_name" = "$COLLECTION_EXISTS" ]; then
        drop_colls "$COLLECTION_NAME"
    fi

    stop_timer

    get_return_code "$status"
    update_result

    # print result of this test case
    print_testcase_result "$test_case_name" "$status" "$total_test_no" "$curr_test_no"

done

# 2. copy result log file of these testcases to output directory
for test_case in $testcases; do
    test_case_name=$(basename "$test_case")
    log="$test_case/ingest.json.log"
    outdir="$OUTPUT_DIR/$test_case_name/"
    mkdir -p "$outdir"
    [ -f "$log" ] && mv "$log" "$outdir"
done

# print summary from util/common.sh
print_summary
exit_script
