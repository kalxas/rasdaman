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

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
UTIL_SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$UTIL_SCRIPT_DIR"/../../../util/common.sh

f=/tmp/rastmp

c=test_insert

$RASQL -q "select marray i in [0:20,0:20] values 1c from mr as c" --out file --outfile $f > /dev/null

mv $f.unknown $f

$RASQL -q "create collection $c GreySet" > /dev/null
$RASQL -q "insert into $c values \$1" -f $f --mdddomain "[0:100021,0:100021]" --mddtype GreyImage > /dev/null

rm -f $f
