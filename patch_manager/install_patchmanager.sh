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
# install_patchmanager.sh - build and install patchmanager egg
#
# SYNTAX
#	install_patchmanager.sh
#
# DESCRIPTION
#	This script build and install patchmanager egg. There are variables that
# need to be adapted before the script is run in install_patchmanager.cfg
#

# ------------------------------------------------------------------------------
# script return codes
#
RC_OK=0
RC_ERROR=1

PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

CFG_FILE=install_patchmanager.cfg
CFG="$SCRIPT_DIR/$CFG_FILE"

# ------------------------------------------------------------------------------
# functions
# ------------------------------------------------------------------------------

panic(){
  echo "$PROG: $@"
  echo "$PROG: exiting."
  exit $RC_ERROR
}
log(){
  echo "$PROG: $@"
}

load_conf(){
  [ -f "$CFG" ] || panic "configuration file not found, please copy $CFG_FILE.template to $CFG_FILE and adapt."
  . "$CFG"
}
cleanup(){
  rm -rf build dist PatchManager.egg-info
}
build(){
  [ $(which python) ] || panic "python not installed, cannot build patchmanager."
  python setup.py bdist_egg
}
backup(){
  mkdir -p backups
  if [ -f $TRAC_DIR/plugins/PatchManager-0.2-*.egg ]; then
    cp $TRAC_DIR/plugins/PatchManager-0.2-*.egg backups/PatchManager-0.2.egg.backup_$(date +"%d%m%g%H%M%S")
  fi
}
install_egg(){
  if [ -n "$TRAC_DIR" ]; then
    cp -v dist/*.egg $TRAC_DIR/plugins/ || panic "failed installing patchmanager to $TRAC_DIR/plugins"
    [ -n "$APACHE_USER" ] && sudo chown $APACHE_USER: $TRAC_DIR/plugins/PatchManager-0.2-*.egg
    [ -n "$APACHE_SVC" ] && sudo $APACHE_SVC reload
  else
    log "TRAC directory not specified in $CFG_FILE, patchmanager has to be installed manually"
  fi
}

# ------------------------------------------------------------------------------
# work
# ------------------------------------------------------------------------------

pushd "$SCRIPT_DIR" > /dev/null

log "building and installing patchmanager..."
load_conf
cleanup
build
backup
install_egg
log "done."

popd > /dev/null
