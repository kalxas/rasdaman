#!/bin/sh
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

PROG=$(basename $0)

log()  { echo "$PROG: $@"; }
error(){ echo "$PROG: $@" >&2; exit $RC_ERROR; }

table=$($SQLITE "SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_NULLVALUEPAIRS'" 2> /dev/null)

if [ -z "$table" ]; then
    updateFile=update6-sqlite.sql
    [ -n "$SCRIPT_DIR" ] && updateFile="$SCRIPT_DIR/$updateFile"
    [ -f "$updateFile" ] || error "Update script not found: $updateFile"

    $SQLITE < "$updateFile"
    exit $?
fi
