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
DBLINK_SQL=$( locate -b dblink*.sql 2>/dev/null | head -n 1 )
# dblink needs to get installed via CREATE EXTENSION in Postgresql > 9.1
PSQL_VERSION="$( $PSQL --version | head -n 1 | awk '{ print $3 }' )"
PSQL_VERSION_M="$( echo "$PSQL_VERSION" | awk -F '.' '{ print $1 }' )"
PSQL_VERSION_m="$( echo "$PSQL_VERSION" | awk -F '.' '{ print $2 }' )"

# ------------------------------------------------------------------------------
# functions
# ------------------------------------------------------------------------------

#
# PostgreSQL utilities
#
reset_search_path() {
  logn "resetting PostgreSQL search path to its default ... "
  # Need to alter the role, otherwise just SETting the new search path will be session-wide.
  # To check the role's configuration, see:  SELECT rolname, rolconfig FROM pg_roles WHERE rolname='petauser';
  $PSQL -c "ALTER ROLE $PS_USER SET search_path TO DEFAULT" > /dev/null 2>&1
  echo "ok."
}
rollback() {
  log "rolling back ... "
  move_wms_tables "$INTERIM_SCHEMA" # to public
  drop_postgresql_schema "$INTERIM_SCHEMA"
  reset_search_path
  log "rollback done."
}

#
# checks
#
# Override check_ret() to drop interim schema automatically:
check_ret() {
  if [ "$1" -ne 0 ]; then
    error_message=${2:-"FAILED (return value $1)."}
    log "$error_message"
    rollback
    error
  fi
}
check_dblink() {
  if [ -z "$DBLINK_SQL" ]; then
    log "dblink PostgreSQL additional module was not found (package postgresql-contrib)."
    log "please install it if not yet done, and update the 'locate' database (sudo updatedb)."
    rollback
    error "cannot migrate without dblink module."
  fi
}

#
# interrupt handler
#
sigint_handler() {
  rollback
  error "user interrupt."
}
# trap keyboard interrupt (control-c)
trap sigint_handler SIGINT

# ------------------------------------------------------------------------------


#
# PostgreSQL schemas
#
INTERIM_SCHEMA='tmp'                          # Temporary schema where to run the update
 BACKUP_SCHEMA="${BACKUP_SCHEMA_PREFIX}$( basename $UPGRADE_SCRIPTS_DIR )" # Back-up schema for allowing the user to revert to the initial state

#
# SQL scripts
#
 GLOBAL_CONST_SQL="global_const.sql"
       MACROS_SQL="macros.sql"
      MIGRATE_SQL="migrate.sql"
   NFIRST_CRS_SQL="north_first_crss.sql"
     POPULATE_SQL="populate.sql"
       SCHEMA_SQL="schema.sql"
SCHEMA_MULTIPOINT="schema_multipoint.sql"
     TRIGGERS_SQL="triggers.sql"
    UTILITIES_SQL="utilities.sql"

#
# Create separate schema where to try the major update.
# Keep public in the path so that running services can still work.
#
echo
logn "setting PostgreSQL search path to view "$INTERIM_SCHEMA" schema as well ..."
$PSQL -c "CREATE SCHEMA $INTERIM_SCHEMA"      > /dev/null 2>&1
$PSQL -c "ALTER ROLE $PS_USER SET search_path TO $INTERIM_SCHEMA,public" > /dev/null 2>&1
check_ret $?
echo "ok."

#
# run the SQL scripts
# the first schema is the $INTERIM_SCHEMA, so tables are created there now.
#

# Set standard_conforming_strings flag (on by default from PostgreSQL 9.1)
# ``ordinary string literals ('...') treat backslashes literally, as specified in the SQL standard''
# http://www.postgresql.org/docs/current/static/runtime-config-compatible.html#GUC-STANDARD-CONFORMING-STRINGS
$PSQL -c "ALTER ROLE $PS_USER SET standard_conforming_strings = on" > /dev/null 2>&1


# utilities + constants (cget/cset)
logn "updating utilities and global constants ... "
$PSQL $SINGLE_TRANSACTION -f "$UPGRADE_SCRIPTS_DIR/$UTILITIES_SQL" > /dev/null 2>&1
check_ret $?
$PSQL $SINGLE_TRANSACTION -f "$UPGRADE_SCRIPTS_DIR/$GLOBAL_CONST_SQL"  > /dev/null 2>&1
check_ret $?
echo "ok."

# triggers
logn "updating triggers ... "
$PSQL $SINGLE_TRANSACTION -f "$UPGRADE_SCRIPTS_DIR/$TRIGGERS_SQL" > /dev/null 2>&1
check_ret $?
echo "ok."


# WMS tables: either create or move if already existing
get_wms_tables # fill WMS_TABLES and WMS_TABLES_CSV arrays
count_qry="SELECT COUNT(*) FROM pg_tables WHERE tablename ILIKE ANY(ARRAY[${WMS_TABLES_CSV}]) AND schemaname = 'public'"
wms_schema_tables_count=$( $PSQL -P t -P format=unaligned -c "$count_qry"  2>/dev/null )
if [ $wms_schema_tables_count -ne ${#WMS_TABLES[@]} ]
then
    # This petascopedb is being created from scratch (or WMS tables have been dropped): create new ones
    logn "creating WMS tables ... "
    $PSQL $SINGLE_TRANSACTION -f "$SCRIPT_DIR/$WMS_SCHEMA_SQL" > /dev/null 2>&1
    check_ret $?
    echo "ok."
else
    # Move the WMS tables to this schema
    for wms_table in "${WMS_TABLES[@]}"
    do
        logn "moving WMS table $wms_table to $INTERIM_SCHEMA schema ... "
        $PSQL -c "ALTER TABLE public.$wms_table SET SCHEMA $INTERIM_SCHEMA" > /dev/null 2>&1
        psql_exit_status=$?
        if [ $psql_exit_status != 0 ]
        then
            rollback
            error "FAILED: problems encountered while moving WMS tables to schema '$INTERIM_SCHEMA'."
        fi
        echo "ok."
    done
fi


# Create new schema
logn "creating schema for new PS_* tables ... "
$PSQL $SINGLE_TRANSACTION -f "$UPGRADE_SCRIPTS_DIR/$SCHEMA_SQL" > /dev/null 2>&1
check_ret $?
echo "ok."
#
logn "populating tables with some required metadata ... "
$PSQL $SINGLE_TRANSACTION -f "$UPGRADE_SCRIPTS_DIR/$POPULATE_SQL" > /dev/null 2>&1
check_ret $?
echo "ok."


# Enabling PostGIS and creating Multipoint schema
        POSTGIS_SQL='postgis.sql'
    SPATIAL_REF_SQL='spatial_ref_sys.sql'
POSTGIS_REQ_VERSION=2
 GEOM_COLUMNS_TABLE='geometry_columns'
 #
 logn "looking for $POSTGIS_SQL and $SPATIAL_REF_SQL files (multipoint support) ..."
     POSTGIS_SQL_PATH=$( locate -b '\'"${POSTGIS_SQL}"     2>/dev/null | head -n 1 )
 SPATIAL_REF_SQL_PATH=$( locate -b '\'"${SPATIAL_REF_SQL}" 2>/dev/null | head -n 1 )
echo " Ok."
# is postgis.sql installed?
if [ -z "$POSTGIS_SQL_PATH" -o ! -f "$POSTGIS_SQL_PATH" ]
then
    log "WARNING: PostGIS (postgis.sql) not found. Multipoint support will not be installed."
else
    # version >= 2.0 ?
    POSTGIS_VERSION=$( grep "INSTALL VERSION" "$POSTGIS_SQL_PATH" | awk '{ print $4; }' | tr -d "'" )
    log "detected PostGIS version: $POSTGIS_VERSION"
    if [ "${POSTGIS_VERSION%%\.*}" -lt "$POSTGIS_REQ_VERSION" ]
    then
        log "WARNING: PostGIS version $POSTGIS_REQ_VERSION is required, multipoint support will not be installed."
    else
        # install postgis extension
        if [ "$PSQL_VERSION_M" -ge 9 -a "$PSQL_VERSION_m" -ge 1 ]
        then
            # Postgresql >= 9.1  -> use CREATE EXTENSION
            logn "Creating 'postgis' extension in the $PS_DB..."
            $PSQL -c 'CREATE EXTENSION IF NOT EXISTS postgis' > /dev/null
            check_ret $?
            echo "ok."
        else
            # Postgresql <  9.1  -> use psql
            if [ -n "$POSTGIS_SQL_PATH" -a -n "$SPATIAL_REF_SQL_PATH" ]
            then
                log "using \`\`${POSTGIS_SQL_PATH}'' and \`\`${SPATIAL_REF_SQL_PATH}''."
                logn "installing PostGIS in $PS_DB ... "
                $PSQL -c "SELECT * FROM $GEOM_COLUMNS_TABLE LIMIT 1;" > /dev/null 2>&1
                if [ $? -ne 0 ]
                then
                    $PSQL -f "$POSTGIS_SQL_PATH"     -v ON_ERROR_STOP=1 > /dev/null # do not use SINGLE_TRANSACTION opt here (it is self-wrapped)
                    $PSQL -f "$SPATIAL_REF_SQL_PATH" -v ON_ERROR_STOP=1 > /dev/null #
                    echo "ok."
                else
                    log "PostGIS seems already enabled in $PS_DB (table $GEOM_COLUMNS_TABLE detected)."
                fi
            else
                log "WARNING: $POSTGIS_SQL and/or $SPATIAL_REF_SQL not found, please install PostGIS (>= ${POSTGIS_REQ_VERSION}.0) and retry."
                log "in case of fresh installations of PostGIS, please update the locate database and retry (\`\`man updatedb'')"
            fi
        fi
        # add tables for multipoint
        logn "adding tables for multipoint coverages support ... "
        $PSQL $SINGLE_TRANSACTION -f "$UPGRADE_SCRIPTS_DIR/$SCHEMA_MULTIPOINT" > /dev/null 2>&1
        check_ret $?
        echo "ok."
    fi
fi


# migration
schema_is_empty  # public schema
if [ $? -ne 0 ]
then
    logn "installing dblink module to $PS_DB and RASBASE db... "
    check_dblink
    if [ "$PSQL_VERSION_M" -ge 9 -a "$PSQL_VERSION_m" -ge 1 ]
    then
        # Postgresql >= 9.1  -> use CREATE EXTENSION
        $PSQL              -c 'CREATE EXTENSION IF NOT EXISTS dblink' > /dev/null 2>&1
        $PSQL -d "RASBASE" -c 'CREATE EXTENSION IF NOT EXISTS dblink' > /dev/null 2>&1
    else
        # Postgresql <  9.1  -> use psql
        $PSQL -f "$DBLINK_SQL"              > /dev/null 2>&1
        $PSQL -f "$DBLINK_SQL" -d "RASBASE" > /dev/null 2>&1
    fi
    echo "ok."

    logn "creating table with north-first EPSG CRSs ... "
    $PSQL $SINGLE_TRANSACTION -f "$UPGRADE_SCRIPTS_DIR/$NFIRST_CRS_SQL" > /dev/null 2>&1
    check_ret $?
    echo "ok."

    logn "migrating existing coverages from PS_* tables ... "
    $PSQL $SINGLE_TRANSACTION -f "$UPGRADE_SCRIPTS_DIR/$MIGRATE_SQL"
    check_ret $?
    echo "ok."

    logn "drop table with north-first EPSG CRSs ... "
    table_name=$($PSQL -X -P t -P format=unaligned -c "SELECT cget('TABLE_PS9_NORTH_FIRST_CRSS');" );
    $PSQL -c "DROP TABLE IF EXISTS $table_name" > /dev/null 2>&1
    check_ret $?
    echo "ok."
else
    log "no relations found in the public schema: nothing to migrate."
fi


# sql macros
logn "updating macros ... "
$PSQL $SINGLE_TRANSACTION -f "$UPGRADE_SCRIPTS_DIR/$MACROS_SQL" > /dev/null 2>&1
check_ret $?
echo "ok."


# --------------------------------------------------------------------------
#
# Successful update process: backup old tables to a separate schema, and
# move new tables from interim to public PostgreSQL schema.
#

# Rename new tables from PS9_ to PS_:
logn "renaming new tables and database objects to have PS_ prefix ... "
$PSQL -c "SELECT change_prefixes('$INTERIM_SCHEMA', cget('PS9_PREFIX'), cget('PS_PREFIX'))" > /dev/null 2>&1
check_ret $?
echo "ok."

# Change constants accordingly, to make macros and triggers work.
logn "renaming constants to have PS_ prefix ... "
$PSQL -c "SELECT set_constants(cget('PS_PREFIX'))" > /dev/null 2>&1
check_ret $?
echo "ok."

# Rename DEFAULTs for tables demultiplexing
logn "fixing tables' name references ... "
$PSQL -c "ALTER TABLE ps_range_set ALTER COLUMN storage_table SET DEFAULT 'ps_rasdaman_collection';\
          ALTER TABLE ps_range_type_component ALTER COLUMN field_table SET DEFAULT 'ps_quantity'" > /dev/null 2>&1
check_ret $?
echo "ok."
# ... and correct possibly existing applied DEFAULTs:
logn "renaming tables references in inserted tuples... "
$PSQL -c "UPDATE ps_range_set SET storage_table=cget('TABLE_PS9_RASDAMAN_COLLECTION') WHERE storage_table='ps9_rasdaman_collection';\
          UPDATE ps_range_type_component SET field_table = 'ps_quantity'              WHERE field_table='ps9_quantity'" > /dev/null 2>&1
check_ret $?
echo "ok."

# Fix CRS for eobstest if it has been migrated
eobstest_qry="SELECT COUNT(*) FROM ps_coverage WHERE name='eobstest'"
eobstest_flag=$( $PSQL -X -P t -P format=unaligned -c "${eobstest_qry}" 2>/dev/null )
if [ "$eobstest_flag" -eq 1 ]
then
    eobstest_crs_update_qry="UPDATE ps_domain_set SET native_crs_ids = \
    ARRAY[(SELECT id FROM ps_crs WHERE uri=cget('CRS_EOBSTEST_T')), \
    native_crs_ids[2]] WHERE coverage_id=(SELECT id FROM ps_coverage WHERE name='eobstest')";
    logn "updating time CRS for eobstest... "
    $PSQL -c "${eobstest_crs_update_qry}" > /dev/null 2>&1
    check_ret $?
    echo "ok."
fi

# backup public schema
logn "backup pre-upgrade schema ... "
$PSQL -c "ALTER SCHEMA public RENAME TO $BACKUP_SCHEMA" > /dev/null 2>&1
check_ret $?
echo "ok."
schema_is_empty "$BACKUP_SCHEMA"
if [ $? -eq 0 ]
then
    log "no relations in the backup schema "$BACKUP_SCHEMA": drop it."
    drop_postgresql_schema "$BACKUP_SCHEMA"
fi

# tmp schema to public
logn "publishing upgraded metadata tables ... "
$PSQL -c "ALTER SCHEMA $INTERIM_SCHEMA RENAME TO public" > /dev/null 2>&1
echo "ok."

# search path back to its default (usually "$user,public")
reset_search_path

log "done, exiting."
exit $CODE_OK
