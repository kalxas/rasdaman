#!/bin/bash
#
# =~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~ #
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
# =~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~ #
#
# SYNOPSIS
#  test.sh
# Description
#  Command-line utility for testing SECORE.
#
# PRECONDITIONS
#   Tomcat installed and running
#   SECORE deployed
#
# Usage: ./test.sh
#

PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../util/common.sh

#
# constants
#
QUERIES_PATH="$SCRIPT_DIR/queries"
[ -d "$QUERIES_PATH" ] || error "Queries directory not found: $QUERIES_PATH"

# query file sufixes
KVP_QUERY_SUFFIX=".kvp"
REST_QUERY_SUFFIX=".rest"

# SECORE_URL keyword inside files (compoundings and equality tests)
SECORE_KEY="%SECORE_URL%"

# dir holding query outputs and oracles
ORACLE_PATH="$SCRIPT_DIR/oracle"
ORACLE_SUFFIX="oracle"
OUTPUT_PATH="$SCRIPT_DIR/output"
OUTPUT_SUFFIX="out"
mkdir -p "$OUTPUT_PATH"

#
# variables
#
failed=0
total=0

#
# cleanup stuff
#
function cleanup()
{
  loge "--------------------------------------------------------"
  loge
  if [ $total -ne 0 ]; then
    log "  PASSED:" $(($total - $failed))
    log "  FAILED: $failed"
    loge "          ------------"
    log "   TOTAL: $total"
    loge ""
  fi

  log "cleanup..."
  #raserase_colls
  [ $failed -eq 0 ]
  exit
}

# trap keyboard interrupt (control-c)
trap cleanup SIGINT


# ------------------------------------------------------------------------------
# work
# ------------------------------------------------------------------------------

#
# checks
#
check_secore
check_wget

#
# run KVP and REST queries
#
pushd "$QUERIES_PATH" > /dev/null

for f in $( ls | grep -E "${KVP_QUERY_SUFFIX}|${REST_QUERY_SUFFIX}"); do

  [ -f "$f" ] || continue
  
  # test single file
  #[[ "$f" == 09-*.kvp ]] || continue

  # test header
  loge ""
  loge "--------------------------------------------------------"
  log "running test: $f"
  loge
  
  # Replace SECORE_URL in the queries (with /crs-compound and /equal)
  uri="$( cat $f | sed "s#${SECORE_KEY}#${SECORE_URL}#g" )"

  # URL encode query
  #f_enc=`cat $f | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g'`
  uri_enc=$( echo $uri | tr -d ' \n' )
  
  # prepend SECORE endpoint (defined in test.cfg)
  req="${SECORE_URL}$uri_enc"
  
  log $req
  loge

  # send to secore
  f_out="$OUTPUT_PATH/$f.$OUTPUT_SUFFIX"
  time $WGET -q $req -O "$f_out"

  loge
  f_oracle="$ORACLE_PATH/$f.$ORACLE_SUFFIX"
  diff "$f_out" "$f_oracle" > /dev/null
  # TODO [ticket #363] Match against ./oracle/$f_out.oracle instead and
  #      IF ( correspondent oracle/$f_out.oracle.sh exists ) 
  #        THEN run it and
  #           IF [ $? -ne 0 ] THEN failed
  #        ELSE diff .oracle and .out
  #      END IF

  if [ $? -ne 0 ]; then
    failed=$(($failed + 1))
    log " ->  QUERY FAILED"
  else
    log " ->  QUERY PASSED"
  fi
  
  total=$(($total + 1))

done

popd > /dev/null

cleanup
