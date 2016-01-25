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
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

. ../../../util/common.sh # load prepare_xml_file util
out="$1"
oracle="$2"

prepare_xml_file "$out"

# replace CRS link with REQUEST
sed -r 's/but.*points/but REQUEST points/' "$out" > "$out".tmp

# diff
diff -b "$out".tmp "$oracle" > /dev/null 2>&1
rc=$?

# remove out file
rm -f "$out"
# move replaced file to out file
mv "$out".tmp "$out"

exit $rc
