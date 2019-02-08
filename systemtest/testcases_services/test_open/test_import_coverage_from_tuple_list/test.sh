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
#    This script tests importing a new coverage from GML tuple list instead of file as wcst_import.sh does normally.

# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

mkdir -p "$SCRIPT_DIR/output/"

ORACLE_FILE="$SCRIPT_DIR/oracle.tiff"
OUTPUT_FILE="$SCRIPT_DIR/output/output.tiff"

COVERAGE_ID="C0001"

logn "Testing import a coverage from tuple list in GML file..."

GML_TEMPLATE_FILE="$SCRIPT_DIR/exampleRectifiedGridCoverage-1.xml.in"
GML_FILE="$SCRIPT_DIR/exampleRectifiedGridCoverage-1.xml"

# update local SECORE URL from template file
sed "s@SECORE_URL@$SECORE_URL@g" "$GML_TEMPLATE_FILE" > "$GML_FILE"

# this query will be encoded in python script with urllib
import_coverage_request="$PETASCOPE_URL?service=WCS&version=2.0.1&request=InsertCoverage&coverageRef=file://$GML_FILE"
curl -s -S "$import_coverage_request" 2>&1 >> "$LOG_FILE"

# then, check the imported coverage encoded in PNG
get_coverage_request="$PETASCOPE_URL?service=WCS&version=2.0.1&request=GetCoverage&coverageId=$COVERAGE_ID&format=tiff"
curl -s "$get_coverage_request" > "$OUTPUT_FILE"

cmp "$ORACLE_FILE" "$OUTPUT_FILE" 2>&1 > /dev/null

# check result
check

# delete imported test coverage
delete_coverage "$COVERAGE_ID"

rm -rf "$GML_FILE"

# print summary from util/common.sh
print_summary
exit $RC
