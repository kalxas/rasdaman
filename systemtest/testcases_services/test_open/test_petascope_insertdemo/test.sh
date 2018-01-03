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
# Copyright 2003 - 2017 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#    test.sh
# Description
#    This script tests petascope_insertdemo.sh script which will import sample coverages to local petascope

################################################################################
# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
  SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh


# run the petascope_insertdemo script
petascope_insertdemo.sh > /dev/null 2>&1

# check result
check

# Remove imported sample coverages
# delete_coverage is a WCS request in common.sh
delete_coverage "AverageTemperature"
delete_coverage "AverageChlorophyll"
# don't delete this one as it is used for WS client test with WCS ProcessCoverages tab in dropdownbox
# delete_coverage "mean_summer_airtemp"

log "done."

# print summary from util/common.sh
print_summary
exit $RC
