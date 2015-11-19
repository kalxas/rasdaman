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

BASEDIR=$(dirname "${BASH_SOURCE[0]}") # get current directory of test script

# get the update script
DIR=$(cd -P "$BASEDIR" && cd ../../.. && pwd) # go to rasdaman directory

. "$DIR"/systemtest/util/common.sh

UPDATE="$DIR/applications/petascope/update_properties.sh"

log "--- Testing updating values between new and old configuration files ---"

# 1. Get the test input in old, new, oracle directories.
OLD_DIR="$BASEDIR"'/data/old'
NEW_DIR="$BASEDIR"'/data/new'
ORACLE_DIR="$BASEDIR"'/oracle'


# 2. Remove files in data/ and copy test suite from data_suite to data and preparing to test
log 'Preparing input data directories.'
rm -rf "$BASEDIR"'/data' || error "Error: Could not delete old data." # remove old data directory
cp -R "$BASEDIR"'/data_suite' "$BASEDIR"'/data' || error "Error: Failed copying $BASEDIR/data_suite to $BASEDIR/data" # copy data_suite to data
log "Done."

# 3. Check that data/old, data/new, oracle/ has the same input files
OLD_COUNT=$(find "$BASEDIR"'/data/old' -type f | wc -l) # count files in the old directory
NEW_COUNT=$(find "$BASEDIR"'/data/old' -type f | wc -l) # count files in the new directory
ORACLE_COUNT=$(find "$BASEDIR"'/oracle' -type f | wc -l) # count files in the oracle directory

echo 'Number of files to test is: '$OLD_COUNT;

if [[ $OLD_COUNT != $NEW_COUNT ]]; then
    error "Error: Different files between old and new directories in ./data/, please check.";
elif [[ $NEW_COUNT != $ORACLE_COUNT ]]; then
    error "Error: Different files between old/new and oracle directories in ./data/ and ./oracle/, please check.";
fi


# 4. Iterate each file in the input directories, run the update_properties.sh and check the result between data/old/ directory and oracle directory
for f in $OLD_DIR/*; do
    # 4.1 Get the fileName in OLD directory
    name=$(echo ${f##*/} | awk -F '_' '{print $1}') # get fileName and split by '_'

    newName="$NEW_DIR/$name"_new # must have the _new postfix

    # 4.2 Run update_properties.sh script
    $("$UPDATE" "$f" "$newName" >> /dev/null) # call script update_petascope.sh    with inputs: old file and new file

    # 4.3 Check if script doest not run correctly
    if [[ $? != 0 ]]; then
        NUM_FAIL=$(($NUM_FAIL + 1))
        log 'Failed in update_properties.sh. Please check old and new inputs are correct files. Done.';

    else # run correctly

        oracleFile="$ORACLE_DIR/$name"_oracle

        # 4.4 Check oldName in $OLD_DIR with oracle/oldName_oracle
        log "Comparing output file:"
        log    "+"$f
        log    "+"$oracleFile

        check=$(cmp "$f" "$oracleFile") # compare the output (old file) with oracle file
        if [[ "$check" != ''    ]]; then # if the output file is not the same with oracleFile
            log "Failed: Output file is diffrent with oracle file. Done."
            log "Trace: cmp ""$f" "$oracleFile"
            NUM_FAIL=$(($NUM_FAIL + 1))
	      else
	          log "Pass: Output file is identical to oracle file. Done."
            NUM_SUC=$(($NUM_SUC + 1))
        fi
    fi # end check update_properties.sh

done # end foreach files in folder

# print summary from util/common.sh
print_summary
exit $RC
