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
    log_failed "----------------------------------------------------------------------"
    log_failed ""
}

# Check if Petascope is deployed (imported from util/petascope.sh)
check_petascope || exit $RC_ERROR

# 1. Iterate folders in test data
for test_case in $TEST_DATA/*; do
    # each folder is a coverage with image files and recipe
    # 1.1 get the recipe in $test_case directory (NOTE: -L to find in symbolic directory)
    recipe_file_template=$(find -L $test_case -type f -name "*.template.json")
    if [ -z "$recipe_file_template" ]; then
        log "Test case '$test_case' is obsolete, removing."
        rm -rf "$test_case"
        continue
    fi

    # 1.2 copy the template file to ingest.json (this file will be used to ingest data)
    recipe_file="$test_case"/'ingest.json'
    cp "$recipe_file_template" "$recipe_file"

    test_case_name=$(basename "$test_case")
    log "Checking test case name: ""$test_case_name"

    # 1.2.1 If test case name is "collection_exists" then need to import a test collection in rasdaman before
    if [[ "$test_case_name" == "$COLLECTION_EXISTS" ]]; then
	    logn "Ingesting a sample collection: $COLLECTION_NAME."
        rasql -q "CREATE COLLECTION $COLLECTION_NAME RGBSet" --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD > /dev/null 2>&1
    fi

    # 1.3 replace all the default with the current system configuration from systemtest/util/common.sh
    sed -i "s@PETASCOPE_URL@$PETASCOPE_URL@g" "$recipe_file"
    sed -i "s@SECORE_URL@$SECORE_URL@g" "$recipe_file"

    # 1.4 execute wcst_import with $recipe_file
    if [[ "$test_case" == *"error_"* ]]; then
        # This test returns error, then check with test.oracle
        outputError=`python "$SCRIPT_DIR/../../../applications/wcst_import/wcst_import.py" $recipe_file 2>&1`
        oracleError=`cat $test_case/test.oracle`

        logn "Checking error output is identical with oracle output..."

        # 1.5 check if output contains the error message from test.oracle
        if [[ "$outputError" == *"$oracleError"* ]]; then
            check_passed
        else            
            check_failed
            write_to_failed_log "$test_case" "Error output is different from oracle output."            
        fi

        clear_resume_file "$test_case"
        # Got the test result for this test case, check next test case        
        log "----------------------------------------------------------------------"
        log ""

        continue
    else
        logn "Test import coverage... "
        # This test will succeed, check coverage exists later
        wcst_import.sh "$recipe_file" >> "$LOG_FILE"
    fi

    if [[ $? != 0 ]]; then
        sleep 2
        # In Debian, it can failed without reason in some test cases, try it again can make it work
        echo ""
        logn "First import does not succeed, try one more time... "
        wcst_import.sh "$recipe_file" >> "$LOG_FILE"        
    fi
    
    if [[ $? != 0 ]]; then
        sleep 2
        # In Debian, it can failed without reason in some test cases, try it again can make it work
        echo ""
        logn "Second import does not succeed, try one more time... "
        wcst_import.sh "$recipe_file" >> "$LOG_FILE"
    fi    

    # 2 Check if wcst_import runs successfully
    if [[ $? != 0 ]]; then
        # 2.1 error when ingesting data
        check_failed        
        write_to_failed_log "$test_case" "Failed importing coverage."                
        log "----------------------------------------------------------------------"
        log ""

        continue
    else
        # 2.2 run correctly
        check_passed

        grep -q '"mock": true' "$recipe_file"
        if [[ $? == 0 ]]; then
            # It is a mock import, nothing has been ingested
            log "----------------------------------------------------------------------"
            log ""
            continue
        fi
        
        # 2.3 remove file resume.json to clean
        clear_resume_file "$test_case"        

        # 2.4 Get coverage id from ingest.json
        COVERAGE_ID=$(grep -Po -m 1 '"coverage_id":.*?[^\\]".*' $recipe_file | awk -F'"' '{print $4}')

        # 2.4.1 using WCS to check coverage does exist in Petascope
        DESCRIBE_COVERAGE_URL="$PETASCOPE_URL?service=WCS&request=DescribeCoverage&version=2.0.1&coverageId=$COVERAGE_ID"
        logn "Check if coverage exists in Petascope WCS..."
        RETURN=$(get_http_return_code "$DESCRIBE_COVERAGE_URL")
        if [[ $RETURN != 200 ]]; then            
            check_failed
            write_to_failed_log "$test_case" "CoverageID does not exist in Petascope WCS."  
            log "----------------------------------------------------------------------"
            log ""                      
            continue
        else # 2.5 coverage does exist (return HTTP 200)
            check_passed

            # 2.6 NOTE: if in recipe has ""wms_import" ingredient then need to check WMS service also
            grep -q "wms_import" "$recipe_file"
            # Return 0 means wms_import does exist in recipe file
            if [[ $? == 0 ]]; then
                # Get page content
                logn "Test coverage does exist in Petascope WMS... "
                content=$(wget "$PETASCOPE_URL?service=WMS&version=1.3.0&request=GetCapabilities" -q -O -)
                if [[ $content != *$COVERAGE_ID* ]]; then                    
                    check_failed
                    write_to_failed_log "$test_case" "CoverageID does not exist in Petascope WMS."                    
                    log "----------------------------------------------------------------------"
                    log ""                    
                    continue
                else
                    check_passed
                fi
            fi

            # Check if the folder name is in unwanted delete coverage IDs list
            keep_coverage_by_folder_name "$test_case_name"
            IS_REMOVE=$?

            if [[ "$IS_REMOVE" == 1 ]]; then
                # 2.7 it is good when coverage does exist then now delete coverage
                logn "Test delete coverage from Petascope WCS... "
                delete_coverage "$COVERAGE_ID"
                if [[ $? != 0 ]]; then                    
                    check_failed         
                    write_to_failed_log "$test_case" "Cannot delete CoverageID in Petascope WCS."                               
                else # 2.8 coverage is deleted (return HTTP 200)
                    check_passed
                fi
            fi
        fi
    fi

    # 2.7.1 remove created collection in rasdaman
    if [[ "$test_case_name" == "$COLLECTION_EXISTS" ]]; then
        logn "Cleaning collection: $COLLECTION_NAME."
        rasql -q "DROP COLLECTION $COLLECTION_NAME" --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD > /dev/null 2>&1
        logn "Done."
    fi
    echo -e

    log "----------------------------------------------------------------------"
    log ""
done

# print summary from util/common.sh
print_summary
exit_script
