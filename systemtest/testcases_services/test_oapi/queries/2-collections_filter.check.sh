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
# Copyright 2003-2022 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

if ! command -v jq &> /dev/null
then    
    # jq command doesn't exist (for centos7), ignore the test
    exit 0
fi

out="$1"
oracle="$2"

nout="$out.output_tmp"

# use jq tool to filter any object which doesn't start with id = test_*
jq ".collections | map(select(.id | startswith(\"test_\")))"  "$out"  > "$nout"


diff -b "$nout" "$oracle" > /dev/null 2>&1
exit $?
