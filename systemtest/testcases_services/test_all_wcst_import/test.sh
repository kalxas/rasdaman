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
#    Using wcst-import coverages to Petascope and test result
#
################################################################################

# get script name
PROG=$( basename $0 )

RC_OK=0
RC_ERROR=1

# Test case which needs to create a collection in rasdaman to test
COLLECTION_EXISTS="collection_exists"
COLLECTION_NAME="test_wcst_import_collection_exists"

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh


# get the test datas and recipes from folder
TEST_DATA="$SCRIPT_DIR/test_data"

# the coverageIDs need to import but will not be delete
COVERAGE_ID_LIST=("test_time3d" "test_wms_4326" "test_wms_3857")


# Check if coverage ID should be deleted or keep for other test cases (by folder name "contains")
COVERAGE_FOLDER_LIST=("wcps" "wcs" "wms")
keepCoverageByFolderName() {
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
clearResumeFile() {
    RESUME_FILE=$(find $1 -type f -name "*.resume.json")
    if [[ ! -z "$RESUME_FILE" ]]; then
        rm "$RESUME_FILE"
    fi
}

PETASCOPE_URL="$PETASCOPE_URL"/'ows'
# 1. Iterate folders in test data
for TEST_CASE in $TEST_DATA/*; do
    # each folder is a coverage with image files and recipe
    # 1.1 get the recipe in $TEST_CASE directory (NOTE: -L to find in symbolic directory)
    RECIPE_FILE_TEMPLATE=$(find -L $TEST_CASE -type f -name "*.template.json")

    # 1.2 copy the template file to ingest.json (this file will be used to ingest data)
    RECIPE_FILE="$TEST_CASE"/'ingest.json'
    cp "$RECIPE_FILE_TEMPLATE" "$RECIPE_FILE"

    TEST_CASE_NAME=$(basename "$TEST_CASE")
    log "Checking test case name: ""$TEST_CASE_NAME"

    # 1.2.1 If test case name is "collection_exists" then need to import a test collection in rasdaman before
    if [[ "$TEST_CASE_NAME" == "$COLLECTION_EXISTS" ]]; then
	log "Ingesting a sample collection: $COLLECTION_NAME."
        $(rasql --quiet -q "CREATE COLLECTION $COLLECTION_NAME RGBSet" --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD)
        if [[ $? != 0 ]]; then
            NUM_FAIL=$(($NUM_FAIL + 1))
            log "+ Error: when creating test_wcst_import_collection_exists in rasdaman"
            continue # change to another test case to test
        fi
    fi

    # 1.3 replace all the default with the current system configuration from systemtest/util/common.sh
    sed -i "s@PETASCOPE_URL@$PETASCOPE_URL@g" "$RECIPE_FILE"
    sed -i "s@SECORE_URL@$SECORE_URL@g" "$RECIPE_FILE"

    # 1.4 execute wcst_import with $RECIPE_FILE
    if [[ "$TEST_CASE" == *"error"* ]]; then
        # This test returns error, then check with test.oracle
        outputError=`python "$SCRIPT_DIR/../../../applications/wcst_import/wcst_import.py" $RECIPE_FILE 2>&1`
        oracleError=`cat $TEST_CASE/test.oracle`

        # 1.5 check if output contains the error message from test.oracle
        if [[ "$outputError" == *"$oracleError"* ]]; then
            NUM_SUC=$(($NUM_SUC + 1))
            log "+ Pass: Output error is identical with output oracle. ""$TEST_CASE_NAME"
        else
            NUM_FAIL=$(($NUM_FAIL + 1))
            log "+ Error: Output error is different from output oracle."
        fi

        clearResumeFile "$TEST_CASE"
        # Got the test result for this test case, check next test case
        continue
    else
        # This test will succeed, check coverage exists later
        python "$SCRIPT_DIR/../../../applications/wcst_import/wcst_import.py" "$RECIPE_FILE"
    fi

    # 2 Check if wcst_import runs successfully
    if [[ $? != 0 ]]; then
        # 2.1 error when ingesting data
        log "+ Error: When ingesting data by wcst_import, test case: ""$TEST_CASE_NAME"
        NUM_FAIL=$(($NUM_FAIL + 1))
        continue
    else # 2.2 run correctly
        log "+ Pass 1: When ingesting data by wcst_import, test case: ""$TEST_CASE_NAME"
        # 2.3 remove file resume.json to clean
        clearResumeFile "$TEST_CASE"

        # 2.4 Get coverage id from ingest.json
        COVERAGE_ID=$(grep -Po -m 1 '"coverage_id":.*?[^\\]".*' $RECIPE_FILE | awk -F'"' '{print $4}')

        # 2.4.1 using WCS to check coverage does exist in Petascope
        DESCRIBE_COVERAGE_URL="$PETASCOPE_URL?service=WCS&request=DescribeCoverage&version=2.0.1&coverageId=$COVERAGE_ID"
        RETURN=$(wget --spider -S "$DESCRIBE_COVERAGE_URL" 2>&1 | grep "HTTP/" | awk '{print $2}')
        if [[ $RETURN != 200 ]]; then
            log "+ Error: Coverage ID: $COVERAGE_ID does not exist when describe in Petascope."
            NUM_FAIL=$(($NUM_FAIL + 1))
            continue
        else # 2.5 coverage does exist (return HTTP 200)
            log "+ Pass 2: Coverage ID: $COVERAGE_ID does exist when describe in Petascope."

            # 2.6 NOTE: if in recipe has ""wms_import" ingredient then need to check WMS service also
            grep -q "wms_import" "$RECIPE_FILE"
            # Return 0 means wms_import does exist in recipe file
            if [[ $? == 0 ]]; then
            # Get page content
                content=$(wget "$PETASCOPE_URL?service=WMS&version=1.3.0&request=GetCapabilities" -q -O -)
                if [[ $content != *$COVERAGE_ID* ]]; then
                    log "+ Error: Coverage ID: $COVERAGE_ID does not exist in WMS Service."
                    NUM_FAIL=$(($NUM_FAIL + 1))
                    continue
                else
                    log "+ Pass 2_WMS: Coverage ID: $COVERAGE_ID does exist in WMS Service."
                fi
            fi

            # Check if the folder name is in unwanted delete coverage IDs list
            keepCoverageByFolderName "$TEST_CASE_NAME"
            IS_REMOVE=$?

            if [[ "$IS_REMOVE" == 1 ]]; then
                # 2.7 it is good when coverage does exist then now delete coverage
                DELETE_COVERAGE_URL="$PETASCOPE_URL?service=WCS&request=DeleteCoverage&version=2.0.1&coverageId=$COVERAGE_ID"
                RETURN=$(wget --spider -S "$DELETE_COVERAGE_URL" 2>&1 | grep "HTTP/" | awk '{print $2}')
                if [[ $RETURN != 200 ]]; then
                    log "+ Error: Coverage ID: $COVERAGE_ID could not be deleted in Petascope."
                    NUM_FAIL=$(($NUM_FAIL + 1))
                else # 2.8 coverage is deleted (return HTTP 200)
                    log "+ Pass 3: Coverage ID: $COVERAGE_ID could be deleted in Petascope."
                    # Only when delete successfully then could say test is passed 100%
                    NUM_SUC=$(($NUM_SUC + 1))
                fi
            else
                log "+ Pass 3: Keep Coverage ID: *$COVERAGE_ID* for other test."
                NUM_SUC=$(($NUM_SUC + 1))
            fi
        fi
    fi

    # 2.7.1 remove created collection in rasdaman
    if [[ "$TEST_CASE_NAME" == "$COLLECTION_EXISTS" ]]; then
	log "Cleaning collection: $COLLECTION_NAME."
        $(rasql --quiet -q "DROP COLLECTION $COLLECTION_NAME" --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD)
        if [[ $? != 0 ]]; then
            log "+ Error: Could not remove added collection: $COLLECTION_NAME."
            NUM_FAIL=$(($NUM_FAIL + 1))
        fi
        log "Done."
    fi
    echo -e
done

# print summary from util/common.sh
print_summary
exit $RC
