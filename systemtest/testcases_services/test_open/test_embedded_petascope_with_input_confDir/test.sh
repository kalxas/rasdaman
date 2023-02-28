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

port="9090"
petascope_war_file="$RMANHOME/share/rasdaman/war/rasdaman.war"

etc_dir="$RMANHOME/etc"
etc_dir_tmp="/tmp/etc_tmp"
rm -rf "$etc_dir_tmp"
cp -r "$etc_dir" "$etc_dir_tmp"

temp_petascope_properties="$etc_dir_tmp/petascope.properties"
temp_secore_properties="$etc_dir_tmp/secore.properties"
log_file="$SCRIPT_DIR/petascope.log"
mkdir -p "$etc_dir_tmp/secore"

skip_test_if_not_postgresql_petascopedb

# replace port from default one to 9090
sed -i "/server.port=/c\server.port=$port" "$temp_petascope_properties"
sed -i "s@allow_write_requests_from=127.0.0.1@allow_write_requests_from=1.2.3.4@g" "$temp_petascope_properties"
sed -i "s@secore_urls=.*@secore_urls=$SECORE_URL@g" "$temp_petascope_properties"
sed -i "s@secoredb.path=.*@secoredb.path=$etc_dir_tmp/secore@g" "$temp_secore_properties"
sed -i "s@log4j.appender.rollingFile.File=.*@log4j.appender.rollingFile.File=$log_file@g" "$temp_petascope_properties"
sed -i "s@log4j.appender.rollingFile.rollingPolicy.ActiveFileName=.*@log4j.appender.rollingFile.rollingPolicy.ActiveFileName=$log_file@g" "$temp_petascope_properties"

logn "Starting embedded petascope..."

nohup java -jar "$petascope_war_file" --petascope.confDir="$etc_dir_tmp" > nohup.out 2>&1 &
pid=$!

# Wait embedded petascope starts
sleep 10
if [ ! -f "$log_file" ]; then
  log "$log_file not found."
  exit $RC_ERROR
fi
( tail -F -n0 --quiet "$log_file" 2> /dev/null & ) | timeout 40 grep -q "Started ApplicationMain"
if [ $? -eq 124 ]; then
    # the grep timed out
  log "startup timedout, petascope.log:"
  cat "$log_file"
  exit $RC_ERROR
fi

$WGET -q --spider "http://localhost:$port/rasdaman/ows?service=WCS&version=2.0.1&request=GetCapabilities"
# defined in common.sh
check_result 0 $? "test embedded petascope with customized etc dir"

# Try to delete a coverage, but the IP is not allowed in petascope.properties
wget -q --spider "http://localhost:$port/rasdaman/ows?service=WCS&version=2.0.1&request=DeleteCoverage&coverageId=test_123"
check_result 8 $? "test write request: DeleteCoverage is not allowed from localhost"

# Try to insert a test coverage with rasadmin and it should bypass IP check
fileref="file:/$SCRIPT_DIR/testdata/insertcoverage.gml"
$WGET -q --spider "http://localhost:$port/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1&REQUEST=InsertCoverage&coverageRef=$fileref"
check_result 0 $? "test write request: InsertCoverage is allowed for rasadmin user"

# Try to delete a test coverage with rasadmin and it should bypass IP check
$WGET -q --spider "http://localhost:$port/rasdaman/ows?SERVICE=WCS&VERSION=2.0.1&REQUEST=DeleteCoverage&coverageId=test_mr_TO_BE_DELETED"
check_result 0 $? "test write request: DeleteCoverage is allowed for rasadmin user"

# Then kill embedded petascope
kill -9 "$pid"
#rm -rf "$etc_dir_tmp"

# print summary from util/common.sh
print_summary
exit "$RC"
