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
# Copyright 2003 - 2018 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#

################################################################################
# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
  SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

# Petascope should throw exception if axis label specified in GML for WCS-T does not exist in the coverage's CRS.
# In this example: 'Height' does not exist in 'http://www.opengis.net/def/crs/EPSG/0/5621' but 'H'.
GML_TEMPLATE_FILE="$SCRIPT_DIR/exampleRectifiedGridCoverage-1-1band-1d.gml.in"
GML_FILE="$SCRIPT_DIR/exampleRectifiedGridCoverage-1-1band-1d.gml"

# update local SECORE URL from template file
sed "s@SECORE_URL@$SECORE_URL@g" "$GML_TEMPLATE_FILE" > "$GML_FILE"

WCST_REQUEST="$PETASCOPE_URL?coverageRef=file://$GML_FILE&request=InsertCoverage&service=WCS&version=2.0.1"

ORACLE_FILE="$SCRIPT_DIR/oracle.gml"
OUTPUT_FILE="$SCRIPT_DIR/output.gml"

$CURL "$WCST_REQUEST" 2>&1 > "$OUTPUT_FILE"

cmp "$ORACLE_FILE" "$OUTPUT_FILE" >> /dev/null

# check result
check_result 0 $? "no exception if axis label in GML does not exist in coverage CRS"

# Delete this test coverage
delete_coverage wcst_axis_label_not_exist_in_crs

rm -rf "$OUTPUT_FILE" "$GML_FILE"

# print summary from util/common.sh
print_summary
exit $RC
