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
# ./update8.sh
#
# DESCRIPTION
#  Major update of petascopedb database:
#    :: sets up the new (empty) group of tables (those who initially were PS9_* prefixed)
#       into an interim schema space [1]
#    :: try the migration procedure of the coverages' metadata possibly stored in petascopedb
#     --> IF no error was produced, the old group of tables is backed-up to a separate schema (called `ps_pre_update8')
#         and the interim schema is moved to the default `public' one for use.
#     --> OTHERWISE the interim schema is dropped and the database is left untouched.
#
# [1] http://www.postgresql.org/docs/current/static/ddl-schemas.html
#
# NOTES
#  A) Petascope (on rasdaman v.9.X) will not work until this update is successfully run.
#  B) A special case is represented by the tables for WMS (see `update5.sql'):
#     there is no migration involved for such tables and they must be either left as-is
#     when present, or re-created when the update process starts from /this/ update
#     (hence missing the update5.sql update: this happens on fresh new install of rasdaman 9
#     where going through previous update scripts is pointless on an empty database).
#  C) This script is meant to be called by `update_petascopedb.sh': it uses env variables
#     exported inside that.
#
# PRECONDITIONS
#  1) PostgreSQL server must be running, with `dblink' module installed in case migration
#     of existing coverages is needed (fetch OID from RASBASE).
#  2) etc/petascope.properties should be present, and the metadata_user should
#     have appropriate write rights in postgres.
#  3) A folder with the same base-name of this script must be present, containing the following files:
#       - global_const.sql
#       - macros.sql
#       - migrate.sql
#       - populate.sql
#       - schema_multipoint.sql
#       - schema.sql
#       - triggers.sql
#       - utilities.sql
#  4) A running service connected to petascopedb should be temporarily stopped:
#     in order to avoid fully qualified table names in each upgrade SQL files
#     (hard-wiring the temporary schema inside them) the search path of PostgreSQL
#     can be temporarily updated to assume the temporary schema as default;
#     during the upgrade, if we keep the `public' schema in the search path of PostgreSQL,
#     the running service is broken anyway because some tables' name (eg ps_coverage) clash
#     across the two schemas, this way Petascope would be selecting data from different schemas while reading a coverage's metadata.
#     Whereas the service could technically be kept running, it would need a restart
#     in any case in order to effectively upgrade both the new Petascope and its new schema.
#     The upgrade of the schema usually requires some seconds on an average database.
#  5) In order to have a migration script without fully qualified names for tables,
#     a temporary different prefix (PS9_) is used for the new tables.
# ------------------------------------------------------------------------------

PROG=$( basename $0 )
UPGRADE_SCRIPTS_DIR="$SCRIPT_DIR/${PROG%.sh}"
SQLITE_VERSION="$( $SQLITE --version | head -n 1 | awk '{ print $1 }' )"
SQLITE_VERSION_M="$( echo "$SQLITE_VERSION" | awk -F '.' '{ print $1 }' )"
SQLITE_VERSION_m="$( echo "$SQLITE_VERSION" | awk -F '.' '{ print $2 }' )"

# ------------------------------------------------------------------------------
# functions
# ------------------------------------------------------------------------------

#
# checks
#
# Override check_ret() to drop interim schema automatically:

#
# interrupt handler
#
sigint_handler() {
  error "user interrupt."
}
# trap keyboard interrupt (control-c)
trap sigint_handler SIGINT

# ------------------------------------------------------------------------------


#
# SQL scripts
#
 GLOBAL_CONST_SQL="global_const.sql"
   NFIRST_CRS_SQL="north_first_crss.sql"
     POPULATE_SQL="populate.sql"
       SCHEMA_SQL="schema.sql"
SCHEMA_MULTIPOINT="schema_multipoint.sql"
    UTILITIES_SQL="utilities.sql"
   WMS_SCHEMA_SQL="wms.sql"

#
# run the SQL scripts
#

# utilities + constants (cget/cset)
logn "updating global constants ... "
exec_sql "$UPGRADE_SCRIPTS_DIR/$GLOBAL_CONST_SQL"
check_ret $?
echo "ok."

# WMS tables
logn "creating WMS tables ... "
exec_sql "$SCRIPT_DIR/$WMS_SCHEMA_SQL"
check_ret $?
echo "ok."


# Create new schema
logn "creating schema for new PS_* tables ... "
exec_sql "$UPGRADE_SCRIPTS_DIR/$SCHEMA_SQL"
check_ret $?
echo "ok."
#
logn "populating tables with some required metadata ... "
exec_sql "$UPGRADE_SCRIPTS_DIR/$POPULATE_SQL"
check_ret $?
echo "ok."

# search path back to its default (usually "$user,public")
reset_search_path

log "done, exiting."
exit $CODE_OK
