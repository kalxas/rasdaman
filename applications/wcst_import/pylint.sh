#!/bin/bash
# *
# * This file is part of rasdaman community.
# *
# * Rasdaman community is free software: you can redistribute it and/or modify
# * it under the terms of the GNU General Public License as published by
# * the Free Software Foundation, either version 3 of the License, or
# * (at your option) any later version.
# *
# * Rasdaman community is distributed in the hope that it will be useful,
# * but WITHOUT ANY WARRANTY; without even the implied warranty of
# * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# * See the GNU  General Public License for more details.
# *
# * You should have received a copy of the GNU  General Public License
# * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
# *
# * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
# *
# * For more information please see <http://www.rasdaman.org>
# * or contact Peter Baumann via <baumann@rasdaman.com>.
# *

#
# This script runs pylint on the wcst_import code.
#

PROG=$(basename "$0")

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

log()  { echo "$PROG: $*"; }
error(){ echo "$PROG: $*" >&2; exit 1; }
header() {
  log "-----------------------------------------------------------------------"
  log "running: $PYLINT '$1'"
  log ""
}

if ! type pylint > /dev/null 2>&1; then
  log "pylint not found; please install it first with 'pip3 install pylint=2.13.4', or make sure that 'pylint' is on the PATH."
  exit 0
fi

pushd "$SCRIPT_DIR" > /dev/null

PYTHONPATH="$SCRIPT_DIR"
export PYTHONPATH
PYLINT="pylint --rcfile=pylint.cfg"

# run on .py files
for f in *.py; do
  header "$f"
  $PYLINT "$f" || error "failed linting file $f"
done

# run on directories, except for lib and ingredients
for d in *; do
  [ -d "$d" ] || continue
  [[ "$d" = lib || "$d" = ingredients ]] && continue

  header "$d"
  $PYLINT "$d" || error "failed linting directory $d"
done

popd > /dev/null

log "done."
exit 0
