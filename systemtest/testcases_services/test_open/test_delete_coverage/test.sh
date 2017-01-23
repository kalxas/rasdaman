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
# SYNOPSIS
#    test.sh
# Description
#   This script will test delete coverage in cases:
#   + It will stop rasdaman when trying to delete a coverageID by WCS request. 
#   + It will remove a collection and try to delete the according coverageID by WCS request.
################################################################################

# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

WCS_ENDPOINT_DELETE_COVERAGE=$PETASCOPE_URL'?service=WCS&version=2.0.1&request=DeleteCoverage&coverageId='
WCS_ENDPOINT_DESCRIBE_COVERAGE=$PETASCOPE_URL'?service=WCS&version=2.0.1&request=DescribeCoverage&coverageId='

COVERAGE_ID='test_delete_coverage'

# check collection exists
function check_collection_exist() {
    rasql -q "select dbinfo(c) from $COVERAGE_ID as c"
}

# check coverageId exists
function check_coverage_exist() {
    wget -q --trust-server-names --content-disposition "$WCS_ENDPOINT_DESCRIBE_COVERAGE""$COVERAGE_ID"
}

# delete rasdaman collection
function delete_collection {
    rasql -q 'drop collection '$COVERAGE_ID --user rasadmin --passwd rasadmin
}

# delete coverage by WCS request
function delete_coverage {
    wget -q --trust-server-names --content-disposition "$WCS_ENDPOINT_DELETE_COVERAGE""$COVERAGE_ID"   
}



# 1. Stop rasdaman and delete coverageID by WCS request
type start_rasdaman.sh > /dev/null 2>&1

if [ $? -eq 0 ]; then
    log "testing delete coverageId when rasdaman is stopped."
    stop_rasdaman.sh
    sleep 1
    delete_coverage

    # start rasdaman after checking delete coverage
    start_rasdaman.sh    

    # check if coverage, collection exist
    check_collection_exist
    result_1=$?
    check_coverage_exist
    result_2=$?

    # if collection and coverageId exist then test passed    
    if [[ "$result_1" -eq 0 && "$result_2" -eq 0 ]]; then
        # check result
        check_result "pass" "pass" "testing delete coverageId when rasdaman is stopped."
    else
        check_result "pass" "fail" "testing delete coverageId when rasdaman is stopped."
    fi  
fi

# 2. Remove the collection and delete coverageId by WCS request
# delete collection
delete_collection

# try to delete coverage
delete_coverage

# check coverage exists
check_coverage_exist
result=$?

# if coverage metadata is removed, then test passed
if [ "$result" -ne 0 ]; then
    check_result "pass" "pass" "testing delete coverageId when collection is removed."
else
    check_result "pass" "fail" "testing delete coverageId when collection is removed."
fi

# print summary from util/common.sh
print_summary
exit $RC

