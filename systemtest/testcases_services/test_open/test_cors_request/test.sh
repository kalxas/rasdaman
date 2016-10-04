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
#    Check if Petascope can allow CORS request (i.e: a client want to request resource from different domain).
#
################################################################################

# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

PETASCOPE_ENDPOINT=$PETASCOPE_URL'/ows'

log "Testing Petascope allow CORS..."

# Redirect standar error to file to get the verbose result (not download from URL and export to output file).
curl -H "Origin: http://test.com"   -H "Access-Control-Request-Method: POST"   -H "Access-Control-Request-Headers: X-Requested-With" -X OPTIONS --verbose   $PETASCOPE_ENDPOINT 2> output.txt

# Check the result to see Petascope allows CORS
grep -R "Access-Control-Allow-Origin: *" output.txt
check

log "done."

# print summary from util/common.sh
print_summary
exit $RC
