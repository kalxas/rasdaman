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
#    This script will remove all imported coverages from WCST_Import so petascopedb should be empty after make check.
################################################################################
# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
  SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

rm -rf "$SCRIPT_DIR/output"

result=$(wget -qO- "$PETASCOPE_URL?service=WCS&version=2.0.1&request=GetCapabilities")

coverage_ids=($(grep -oP "(?<=<wcs:CoverageId>)[^<]+"  <<< "$result"))

for coverage_id in "${coverage_ids[@]}"; do
    # All test coverages import in test wcst_import will be removed
    if [[ "$coverage_id" == test_* ]]; then
        logn "removing coverage '$coverage_id'... "
        delete_coverage "$coverage_id"
        check
    fi
done

# mean_summer_airtemp is a demo coverage imported by petascope_insertdemo.sh and is used in WS client, tab WCS ProcessCoverages
# after the test interface for WS client, now it can be removed here as other imported test coverages
delete_coverage "mean_summer_airtemp"

print_summary
exit_script
