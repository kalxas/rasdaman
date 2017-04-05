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
#    stress.sh
# Description
#    This script tests the connections from WMSServlet in Petascope to petascopedb from Postgresql. By default, Postgresql supports 100 connections at the same time.
#    The problem is fixed when WMSServlet releases connection as soon as possible.

################################################################################
# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
  SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

# run every test cases open folders
# change directory to the test_open
. cd "$SCRIPT_DIR"

# list all the subdirectories of test_open
for d in */ ; do
	scriptFile="$d/test.sh"
	if [ ! -x "$scriptFile" ]; then
		log "Script test.sh is not executable in folder: $d."
	else
		log "Running test case: $d"
		"./$scriptFile"

		# check the result of script
		check
	fi
done

log "done."
exit_script
