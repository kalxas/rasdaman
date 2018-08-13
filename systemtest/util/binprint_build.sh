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
# Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

#
# Script to build and install the binprint program.
#

# determine script directory
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# load config file
CONF_FILE="$SCRIPT_DIR/../conf/test.cfg"
if [ -f "$CONF_FILE" ]; then
    . "$CONF_FILE"
else
    echo "$CONF_FILE not found. Please build rasdaman."
fi

# build
make binprint || exit 1

# install
if [ -d "$RMANHOME/bin" ]; then
    install -m 755 binprint "$RMANHOME/bin"
else
    echo "$RMANHOME/bin not found, cannot install binprint."
    exit 1
fi

echo "done, binprint installed in $RMANHOME/bin."
