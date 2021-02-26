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
# Copyright 2003 - 2019 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
# SYNOPSIS
#    test.sh
# Description
#    This script tests petascope embedded with an input parameter (--petascope.confDir)
#    to a folder containing customized petascope.properties

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

petascope_war_file="$RMANHOME/share/rasdaman/war/rasdaman.war"

etc_dir="$RMANHOME/etc"
etc_dir_tmp="/tmp/etc_tmp"
cp -r "$etc_dir" "$etc_dir_tmp"

port="9090"

# replace port from default one to 9090
sed -i "/server.port=/c\server.port=$port" "$etc_dir_tmp/petascope.properties"

logn "Starting embedded petascope (wait for 20 seconds)..."
nohup java -jar "$petascope_war_file" --petascope.confDir="$etc_dir_tmp" > nohup.out 2>&1 &
pid=$!

# Wait embedded petascope starts
sleep 20

$WGET -q --spider "http://localhost:$port/rasdaman/ows?service=WCS&version=2.0.1&request=GetCapabilities"

# defined in common.sh
check_result 0 $? "test embedded petascope with customized etc dir"

# Then kill embedded petascope
kill -9 "$pid"
rm -rf "$etc_dir_tmp"

# print summary from util/common.sh
print_summary
exit $RC

