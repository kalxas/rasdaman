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
#    This script tests to post an encoded WCPS query instead of plain text WCPS query
#    example: for c in (test_mr) return encode (c[i(0:20),j(0:20)] + 5, "png")
#    encoded: for%20c%20in%20(test_mr)%20return%20encode%20(c%5Bi(0%3A20)%2Cj(0%3A20)%5D%20%2B%205%2C%20%22png%22)
# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh


# encoded WCPS query
WCPS_ENDPOINT=$PETASCOPE_URL"?service=WCS&version=2.0.1&request=ProcessCoverages&query="
QUERY="for%20c%20in%20(test_mr)%20return%20encode%20(c%5Bi(0%3A20)%2Cj(0%3A20)%5D%20%2B%205%2C%20%22png%22)"

log "Test encoded WCPS query..."
log $WCPS_ENDPOINT
log $QUERY
python "$SCRIPT_DIR"/test.py $WCPS_ENDPOINT $QUERY

# defined in common.sh
check

log "done."

# print summary from util/common.sh
print_summary
exit $RC

