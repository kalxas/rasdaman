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

# set to something to enable this update.
enabled=

if [ -n "$enabled" ]; then

FILESTORAGE_TILES_SUBDIR="TILES"

[ -n "$DBCONN" ] || { log "cannot run update, DBCONN env variable is not set."; exit 0; }
RASDATA=$(dirname "$DBCONN") # $DBCONN is exported in update_db.sh
[ -w "$RASDATA" -a -x "$RASDATA" ] || error "no write permissions to the rasdaman data directory: $RASDATA"

echo
log "changing permissions of tile files in $RASDATA..."

find "$RASDATA/$FILESTORAGE_TILES_SUBDIR" -type f -exec chmod 660 {} \;

log "done."

fi
