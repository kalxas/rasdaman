#!/bin/bash
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

#
# Encode array (at given file path $1) in format $2 with sdom $3 and values in each cell $4.
# Sdom and values are placed into rasql marray expression as given.
#
# stdout: the result file path (it could be different from the given $1 with automatically appended extension)
# exit code: the exit code from running directql = non-zero on error
#

# determine script directory
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# loads also rasql.sh
. "$SCRIPT_DIR/common.sh"

usage()
{
cat <<EOF
Generate an array (at given file path) encoded in given format, sdom and values in each cell.

Usage: ./generate_data.sh <result_file_path> <format> <sdom> <cell_values>

The script runs a rasql expression of the form:
SELECT encode(MARRAY i IN <sdom> VALUES <cell_values>, "<format>")

Therefore the <cell_values> can include an iterator i as well.

The result file path is printed to stdout, which might be slightly different from the
provided path (an extension could be automatically appended). Non-zero exit code indicates
success as usual.

Example: ./generate_data.sh "/tmp/test_ones" "GTiff" "[0:10,0:10]" "i[0] + 0.5f"
EOF
}

if [ $# -ne 4 ]; then
    usage
    exit 1
fi

# function defined in rasql.sh
generate_data "$1" "$2" "$3" "$4"