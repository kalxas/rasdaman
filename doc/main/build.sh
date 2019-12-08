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
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.      
#

#
# This script generates HTML and PDF versions of the documentation.
#

# exit when command returns non-zero
set -e

PROG=$(basename $0)

log()  { echo -e    "$PROG: $@"; }
logn() { echo -e -n "$PROG: $@"; }
sep()  { log "-------------------------------------------------------------"; }

sep
log "building html..."
make html > /dev/null
log "html built."

[ "$1" = html ] && exit

sep
log "building pdf..."
log ""
log "generating latex..."
make latex > /dev/null

logn "fixing latex document... "
pushd _build/latex > /dev/null
sed -i 's/\[utf8\]/[utf8x]/g' rasdaman.tex
echo "ok."

log "compiling latex..."
make > /dev/null
log "pdf built."

log "done."
