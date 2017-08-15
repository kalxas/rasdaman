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
# Copyright 2003 - 2017 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# ------------------------------------------------------------------------------
#
# SYNOPSIS
#    test.sh
# Description
#    This script is used to build and run test web interface for wcs_client and SECORE by a Java application which uses Selenium library as GUI test driver.

# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

# NOTE: as the output from Ubuntu are different from Centos because of font, size,... so only run this test on Centos
cat /etc/*-release | grep 'CentOS' > /dev/null

if [ $? -ne 0 ]; then
   log "test web interfaces will not run on this OS, skipping."
   exit 0
fi

phantomjs_path="/tmp/phantomjs"

if [ ! -f "$phantomjs_path" ]; then
    log "Preparing to download phantomjs..."
    wget http://kahlua.eecs.jacobs-university.de:8080/test_wcsclient_insertcoverage/phantomjs -O "$phantomjs_path"
fi

if [ ! -f "$phantomjs_path" ]; then
   log "phantomjs ***does not exist*** in $phantomjs_path." 
   exit 1
else
   log "phantomjs does exist in $phantomjs_path."    
fi

chmod +x "/tmp/phantomjs"

# then, build the Java application
cd "$SCRIPT_DIR/TestWebInterfaces"
mvn package > /dev/null

# NOTE: run jar file at source folder not target folder
mv "$SCRIPT_DIR/TestWebInterfaces/target/test_web_interfaces-spring-boot.jar" "$SCRIPT_DIR/TestWebInterfaces/"

# It will need petascope and secore ports from test.cfg file for test application
java -jar test_web_interfaces-spring-boot.jar $PETASCOPE_PORT $SECORE_PORT

exit $?
