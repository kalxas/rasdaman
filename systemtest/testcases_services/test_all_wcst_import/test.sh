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
#    test.sh
# Description
#    Using wcst-import coverages to petascope and test result
#
################################################################################

# get script name
PROG=$( basename $0 )

RC_OK=0
RC_ERROR=1

# By default, it creates ingest.json from ingest.template.json before importing data
# But, input argument can tell it to just import data instead.
CREATE_INGEST_FILES=$2

if [ -z "$CREATE_INGEST_FILES" ]; then
    CREATE_INGEST_FILES=0
fi

# Test case which needs to create a collection in rasdaman to test
COLLECTION_EXISTS="collection_exists"
COLLECTION_NAME="test_wcst_import_collection_exists"

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh


# get the test datas and recipes from folder
TEST_DATA="$SCRIPT_DIR/testdata"

# store files: *.ingest.json.log here
OUTPUT_DIR="$SCRIPT_DIR/output"

# Check if coverage ID should be deleted or keep for other test cases (by folder name "contains")
COVERAGE_FOLDER_LIST=("wcps" "wcs" "wms" "tmp")
keep_coverage_by_folder_name() {
    for FOLDER_NAME in "${COVERAGE_FOLDER_LIST[@]}"
    do
        # if folder name contains the pattern, then will not remove the coverageID of recipe file which is inside this folder
        if [[ "$1" =~ "$FOLDER_NAME" ]]; then
            return 0
        fi
    done

    return 1
}

# after importing recipe, remove this resume.json file
clear_resume_file() {           
    find . -type f -name "*.resume.json" -delete    
}

write_to_failed_log() {
    # $1 is test case name
    # $2 is the reason
    log_failed "Test case: $1...failed."
    log_failed "Reason: $2"
}

run_wcst_import() {
    # $1 is test case name
    # $2 is input ingredient file
    local test_cases_output_dir="$OUTPUT_DIR/$1"
    mkdir -p "$test_cases_output_dir"
    local wcst_import_log="$test_cases_output_dir/wcst_import.log"

    local ingredient_file="$2"
    wcst_import.sh "$ingredient_file" -q >> "$wcst_import_log" 2>&1
}

# Check if petascope is deployed (imported from util/petascope.sh)
check_petascope || exit $RC_ERROR

# 0. cleaning output directory
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

total_test_no=$(ls -ld "$TEST_DATA"/* | wc -l)
curr_test_no=0

# 1. Iterate folders in test data
for test_case in $TEST_DATA/*; do

    curr_test_no=$(($curr_test_no + 1))
    status="$ST_PASS"

    start_timer

    # each folder is a coverage with image files and recipe
    # 1.1 get the recipe in $test_case directory (NOTE: -L to find in symbolic directory)
    recipe_file_template=$(find -L $test_case -type f -name "*.template.json")
    if [ -z "$recipe_file_template" ]; then
        log "Test case '$test_case' is obsolete, removing."
        rm -rf "$test_case"
        continue
    fi

    # 1.2 copy the template file to ingest.json (this file will be used to ingest data)
    recipe_file="$test_case/ingest.json"

    if [ "$CREATE_INGEST_FILES" -eq 0 ]; then
        cp "$recipe_file_template" "$recipe_file"
        # 1.3 replace all the default with the current system configuration from systemtest/util/common.sh
        sed -i "s@PETASCOPE_URL@$PETASCOPE_URL@g" "$recipe_file"
        sed -i "s@SECORE_URL@$SECORE_URL@g" "$recipe_file"
    fi
    
    test_case_name=$(basename "$test_case")
    mkdir -p "$OUTPUT_DIR/$test_case_name/"

    # 1.2.1 If test case name is "collection_exists" then need to import a test collection in rasdaman before
    if [[ "$test_case_name" == "$COLLECTION_EXISTS" ]]; then
        rasql -q "CREATE COLLECTION $COLLECTION_NAME RGBSet" --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD > /dev/null 2>&1
    fi

    # 1.4 execute wcst_import with $recipe_file
    if [[ "$test_case" == *"error_"* ]]; then
        # This test returns error, then check with test.oracle
        outputError=`wcst_import.sh $recipe_file -q`
	    echo "$outputError" > "$OUTPUT_DIR/$test_case_name/test.output"
        oracleError=`cat $test_case/test.oracle`

        # 1.5 check if output contains the error message from test.oracle
        if [[ "$outputError" == *"$oracleError"* ]]; then
            status="$ST_PASS"
        else            
            status="$ST_FAIL"
            write_to_failed_log "$test_case" "Error output is different from oracle output."            
        fi

    else        
        # This test will succeed, check coverage exists later
        run_wcst_import "$test_case_name" "$recipe_file"

        # Some errors occurred, need to retry
        if [[ $? != 0 ]]; then
            write_to_failed_log "$test_case" "Failed importing coverage, retrying."
            sleep 1
            run_wcst_import "$test_case_name" "$recipe_file"    
        fi

        # 2 Check if wcst_import runs successfully
        if [[ $? != 0 ]]; then
            # 2.1 error when ingesting data
            status="$ST_FAIL"        
            write_to_failed_log "$test_case" "Failed importing coverage."
        else
            # 2.2 run correctly

            grep -q '"mock": true' "$recipe_file"
            if [[ $? == 0 ]]; then
                # It is a mock import, nothing has been ingested
                continue
            fi
            
            # 2.4 Get coverage id from ingest.json
            COVERAGE_ID=$(grep -Po -m 1 '"coverage_id":.*?[^\\]".*' $recipe_file | awk -F'"' '{print $4}')

            # Check if the folder name is in unwanted delete coverage IDs list
            keep_coverage_by_folder_name "$test_case_name"
            IS_REMOVE=$?

            status="$ST_PASS"

            if [[ "$IS_REMOVE" == 1 ]]; then
                # 2.7 it is good when coverage does exist then now delete coverage
                delete_coverage "$COVERAGE_ID"
                if [[ $? != 0 ]]; then                    
                    status="$ST_FAIL"         
                    write_to_failed_log "$test_case" "Cannot delete coverage."
                fi
            fi            
        fi

        # remove file resume.json
        clear_resume_file "$test_case"        
    fi

    # 2.7.1 remove created collection in rasdaman
    if [[ "$test_case_name" == "$COLLECTION_EXISTS" ]]; then
        rasql -q "DROP COLLECTION $COLLECTION_NAME" --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD > /dev/null 2>&1
    fi

    stop_timer

    get_return_code $status
    update_result

    # print result of this test case
    print_testcase_result "$test_case_name" "$status" "$total_test_no" "$curr_test_no"  

done


# Finally, copy result log file of these testcases to output directory
# Iterate folders in test data
for test_case in $TEST_DATA/*; do
    test_case_name=$(basename "$test_case")
    mv "$test_case/ingest.json.log" "$OUTPUT_DIR/$test_case_name/" 2> /dev/null
done

# print summary from util/common.sh
print_summary
exit_script
