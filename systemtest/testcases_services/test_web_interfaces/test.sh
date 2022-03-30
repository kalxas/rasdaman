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

OUTPUT_PATH="$SCRIPT_DIR/output"
# NOTE: before running any test queries in test directory, remove all the output files to make it clean first
if [ -d "$OUTPUT_PATH" ]; then
    logn "Cleaning output directory... "
    rm -rf "$OUTPUT_PATH"
    echo "Done."
fi
# then create the output directory
mkdir -p "$OUTPUT_PATH"

# NOTE: as the output from Ubuntu are different from Centos because of font, size,... so only run this test on Centos
cat /etc/*-release | grep 'CentOS' > /dev/null

if [ $? -ne 0 ]; then
   log "test web interfaces will not run on this OS, skipping."
   exit 0
fi

temp_dir="/tmp/test_web_interfaces"
mkdir -p "$temp_dir"

firefox_download_path="$temp_dir/firefox.tar.bz2"
geckodriver_download_path="$temp_dir/geckodriver.tar.gz"

firefox_binary_path="$temp_dir/firefox/firefox"
geckodriver_binary_path="$temp_dir/geckodriver"

if [ ! -f "$firefox_binary_path" ]; then
   log "Downloading firefox v99 from server..."

   curl -sS "https://download-installer.cdn.mozilla.net/pub/firefox/releases/99.0.1/linux-x86_64/en-GB/firefox-99.0.1.tar.bz2" -o "$firefox_download_path"
   cd "$temp_dir" && tar -xf "$firefox_download_path"
fi

if [ ! -f "$geckodriver_binary_path" ]; then
   log "Downloading firefox - geckodriver v0.31 from server..."

   curl -sSL "https://github.com/mozilla/geckodriver/releases/download/v0.31.0/geckodriver-v0.31.0-linux64.tar.gz" -o "$geckodriver_download_path"
   cd "$temp_dir" && tar -xf "$geckodriver_download_path"
fi



if [ ! -f "$firefox_binary_path" ]; then
   log "firefox binary does not exist at $firefox_binary_path." 
   exit 1
fi

if [ ! -f "$geckodriver_binary_path" ]; then
   log "firefox - geckodriver binary does not exist at $geckodriver_binary_path." 
   exit 1
fi

log "Building test web interfaces application..."

# then, build the Java application
cd "$SCRIPT_DIR/TestWebInterfaces"
mvn -q clean && mvn -q package > $OUTPUT_PATH/mvn_package.log 2>&1

# NOTE: run jar file at source folder not target folder
mv "$SCRIPT_DIR/TestWebInterfaces/target/test_web_interfaces-spring-boot.jar" "$SCRIPT_DIR/TestWebInterfaces/"

log "Running test web interfaces application..."

# It will need petascope and secore ports from test.cfg file for test application
java -jar test_web_interfaces-spring-boot.jar $PETASCOPE_PORT $SECORE_PORT > $OUTPUT_PATH/test.log 2>&1
result="$?"

# remove any leftover firefox instances on this temp dir
pkill -f "$temp_dir"

if [ "$result" -eq 0 ]; then
    log "TEST PASSED"
    exit 0
else
    log "TEST FAILED"
    exit 1
fi
