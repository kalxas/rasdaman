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

GML_FILE="$SCRIPT_DIR/exampleRectifiedGridCoverage-1-1band-1d.gml"

WCST_REQUEST="$PETASCOPE_URL?coverageRef=file://$GML_FILE&request=InsertCoverage&service=WCS&version=2.0.1"

ORACLE_FILE="$SCRIPT_DIR/oracle.gml"
OUTPUT_FILE="$SCRIPT_DIR/output.gml"

curl -s "$WCST_REQUEST" 2>&1 > "$OUTPUT_FILE"

cmp "$ORACLE_FILE" "$OUTPUT_FILE" >> /dev/null

# check result
check

rm -rf "$OUTPUT_FILE"

# print summary from util/common.sh
print_summary
exit $RC
