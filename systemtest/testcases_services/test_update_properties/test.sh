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
#    Compare user's properties files with current properties files.
#
################################################################################

# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

UPDATE=""$SCRIPT_DIR"/../../../applications/petascope/update_properties.sh"

log "Testing properties file migration"

# 1. Get the test input in old, new, oracle directories.
# store the original properties files from data_suite folder to be updated after testing
TMP_DIR="$SCRIPT_DIR/tmp"
TMP_OLD_DIR="$TMP_DIR/old"
TMP_NEW_DIR="$TMP_DIR/new"
DATA_SUITE_DIR="$SCRIPT_DIR/data_suite"
ORACLE_DIR="$SCRIPT_DIR/oracle"

# 2. Remove files in tmp folder and copy test suite from data_suite to data and preparing to test
logn 'Preparing tmp data directories for test cases...'
rm -rf "$TMP_DIR" || error "Error: Could not delete tmp folder '$TMP_DIR'." # remove old data directory
cp -R "$DATA_SUITE_DIR" "$TMP_DIR" || error "Error: Failed copying '$DATA_SUITE_DIR' to '$TMP_DIR'" 
echo "ok."

# 3. Iterate each file in the input directories, run the update_properties.sh and check the result between data/old/ directory and oracle directory
for f in $TMP_OLD_DIR/*; do
    # 3.1 Get the fileName in OLD directory
    test_case_name=${f##*/}
    old_properties_file="$f"
    new_properties_file="$TMP_NEW_DIR/$test_case_name"

    logn "Running test case '$test_case_name'... "
    # 3.2 Run update_properties.sh script
    "$UPDATE" "$old_properties_file" "$new_properties_file" >> /dev/null # call script update_petascope.sh with inputs: old file and new file

    # 3.3 Check if script doest not run correctly
    if [[ $? != 0 ]]; then
        NUM_FAIL=$(($NUM_FAIL + 1))
        echo 'failed executing update_properties.sh.';
    else # run correctly
        oracle_properties_file="$ORACLE_DIR/$test_case_name"
        # 3.4 Compare updated properties file in tmp/old with oracle file
        cmp "$old_properties_file" "$oracle_properties_file" # compare the output (old file) with oracle file

        if [[ $? -ne 0 ]]; then # if the output file is not the same with oracle_properties_file
            log "Failed: Updated properties file is diffrent from oracle file. Done."
            NUM_FAIL=$(($NUM_FAIL + 1))
	    else
            # 3.5 Check if test case has deprecated properties then it should create backup for this old properties file
            if [[ "$test_case_name" =~ "removed" ]]; then
                back_up_exist=$(find "$TMP_OLD_DIR/" -name "$test_case_name*.bak")
                if [[ -z "back_up_exist" ]]; then
                    echo "failed, test case did not create a backup file."
                    NUM_FAIL=$(($NUM_FAIL + 1))
                    # no need to compare updated properties file with oracle file
                    continue
                fi   
            fi
	        echo "passed."
            NUM_SUC=$(($NUM_SUC + 1))
        fi
    fi # end check update_properties.sh

done # end foreach files in folder

# print summary from util/common.sh
print_summary
exit_script
