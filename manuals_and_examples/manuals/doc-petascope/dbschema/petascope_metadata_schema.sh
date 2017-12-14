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
# ------------------------------------------------------------------------------
#
# SYNOPSIS
# ./petascope_metadata_schema.sh
#
# Description
#  Command-line utility for creating the petascope schema diagram representation.
#
#  Database connection details are read from petascope.properties
#
# PRECONDITIONS
#  1) PostgreSQL server must be running
#  2) petascope.properties should be present, and the metadata_user should
#     have appropriate read rights in postgres.
#  3) SchemaSpy should be present at the provided location

PROG=`basename $0`

if [ $# != 3 ]
then
  echo "Usage: $0 schemaspy-jar postgresql-jar properties-file"
  echo "   schemaspy-jar: SchemaSpy jar file (can use relative or absolute path)"
  echo "   postgresql-jar:  postgresql driver jar file"
  echo "   properties-file:  petascope.properties file"
  echo "where relative or absolute paths are possible."
  echo "Example: $0 ./schemaSpy_5.0.0.jar postgresql.jar /etc/petascope.properties"
	exit 0
fi
# Parameters definition and initiation

SCHEMASPY=$1
# org.postgresql jar
PS_DRIVER=$2
# petascope settings file
SETTINGS=$3

CODE_OK=0
CODE_FAIL=255

# schema name
PS_SCHEMA=public

# new schema diagram folder
SCHEMA_FOLDER=schema

# ------------------------------------------------------------------------------
# functions
# ------------------------------------------------------------------------------

#
# logging
#
log() {
  echo "$PROG: $*"
}
error() {
  echo "$PROG: $*" >&2
  echo "$PROG: exiting." >&2
  exit $CODE_FAIL
}

#
# checks
#
check_postgres() {
  which psql > /dev/null || error "PostgreSQL missing, please add psql to the PATH."
  pgrep postgres > /dev/null
  if [ $? -ne 0 ]; then
    pgrep postmaster > /dev/null || error "The PostgreSQL service is not started."
  fi
}
check_paths() {
  if [ ! -f "$SETTINGS" ]; then
	  error "petascope settings not found at the provided location: $SETTINGS"
  fi
  if [[ ! -f $SCHEMASPY ]]; then
    error "SchemaSpy not found at the provided location: $SCHEMASPY"
  fi
  if [[ ! -f $PS_DRIVER ]]; then
    error "PostgreSQL driver not found at the provided location: $PS_DRIVER"
  fi
}

# ------------------------------------------------------------------------------
# work
# ------------------------------------------------------------------------------

check_paths

#
# postgres connection details
#
PS_USER=`grep 'metadata_user=' "$SETTINGS" | awk -F "=" '{print $2}'`
PS_USER="${PS_USER#"${PS_USER%%[![:space:]]*}"}"
PS_PASS=`grep 'metadata_pass=' "$SETTINGS" | awk -F "=" '{print $2}'`
PS_PASS="${PS_PASS#"${PS_PASS%%[![:space:]]*}"}"
PS_DB=`grep 'metadata_url=' "$SETTINGS" | awk -F "/" '{print $4}' | tr -d '\n'`
PS_HOST=`grep 'metadata_url=' "$SETTINGS" | awk -F ":|/" '{print $5}' | tr -d '\n'`
PS_PORT=`grep 'metadata_url=' "$SETTINGS" | awk -F ":|/" '{print $6}' | tr -d '\n'`

# print some info
log "postgres settings read from $SETTINGS"
log "  user: $PS_USER"
log "  host: $PS_HOST"
log "  port: $PS_PORT"
log "  db: $PS_DB"
log "  driver: $PS_DRIVER"

check_postgres

#
# create schema folder if not present
#

if [[ ! -e $SCHEMA_FOLDER ]]; then
    mkdir -p $SCHEMA_FOLDER
elif [[ ! -d $SCHEMA_FOLDER ]]; then
    error "$SCHEMA_FOLDER already exists but is not a directory"
fi
#
# create schema diagram using the arguments above
#
java -jar $SCHEMASPY -t pgsql -db $PS_DB -host $PS_HOST:$PS_PORT -u $PS_USER -o $SCHEMA_FOLDER -p $PS_PASS -s $PS_SCHEMA -dp $PS_DRIVER
if [ $? -ne 0 ]; then
  error "error encountered while generating the diagram"
fi
#
# done
#
log "Schema diagram can be found in $SCHEMA_FOLDER folder"
log "done, exiting."
exit $CODE_OK
