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
# Copyright 2003 - 2020 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#    test.sh
# Description
#    This script tests insert a coverage and then rename it to a different name

################################################################################
# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
  SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

readonly COVERAGE_ID="test_update_coverage_id"
readonly NEW_COVERAGE_ID="test_update_coverage_id_NEW"

sed "s@PETASCOPE_URL@$PETASCOPE_URL@g" "$SCRIPT_DIR/ingest.template.json" > "$SCRIPT_DIR/ingest.json"

# First, import 2D coverage with scale levels from ingredient file
wcst_import.sh "$SCRIPT_DIR/ingest.json" > /dev/null 2>&1
check_result 0 $? "importing test data"

# Then, rename the coverage id to a new coverage id
admin_endpoint=$(echo "$PETASCOPE_URL" | sed "s/ows/admin/")

update_coverage_id_request="$admin_endpoint/UpdateCoverageId?COVERAGEID=$COVERAGE_ID&NEWID=$NEW_COVERAGE_ID"
result=$(get_http_return_code "$update_coverage_id_request")
check_result "200" "$result" "update coverage id to a new id"

# Then, check the new coverage id exists
update_coverage_id_request="$PETASCOPE_URL?SERVICE=WCS&VERSION=2.0.1&REQUEST=DescribeCoverage&COVERAGEID=$NEW_COVERAGE_ID"
result=$(get_http_return_code "$update_coverage_id_request")
check_result "200" "$result" "check new coverage id exists"

# And delete this test coverage
delete_coverage_request="$PETASCOPE_URL?service=WCS&request=DeleteCoverage&version=2.0.1&coverageId=$NEW_COVERAGE_ID"
result=$(get_http_return_code "$delete_coverage_request")
check_result "200" "$result" "delete coverate $NEW_COVERAGE_ID"

# print summary from util/common.sh
print_summary
exit $RC
