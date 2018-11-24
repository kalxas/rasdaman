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
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community. If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#	 test.sh
# Description
#	 Command-line utility for crash testing rasdaman.
#
#  The script executes update and retreival queries; on every query it kills
#  the server after a random interval with a SIGUSR1 signal.
#  The database is subsequently checked for potential corruption.
#
# PRECONDITIONS
# 	1)
# Usage: ./test.sh
#

# Variables
PROG=$(basename $0)

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

export RASDATA=

#set -u
#set -e

# ------------------------------------------------------------------------------


readonly QUERY_DIR="$SCRIPT_DIR/queries"
readonly QUERY_METRICS_DIR="$SCRIPT_DIR/queries_metrics"
readonly BUG_DIR="$SCRIPT_DIR/bugs"
readonly KILL_SIGNAL="USR1"
readonly QUERY_COUNT=$(ls "$QUERY_DIR"/*.sh | wc -l)


# ------------------------------------------------------------------------------
# functions
#

#
# query metrics get/setters, persistently managed accross test runs in $QUERY_METRICS_DIR
#
get_db_checksum()
{
  pushd "$DB_DIR" > /dev/null
  sqlite3 RASBASE .dump > db.sql
  md5sum $(find . -type f | egrep -v '(RASBASE$|RASBASE-journal|insert\.|remove\.|update\.)'  | tr '\n' ' ') | md5sum | awk '{ print $1; }'
  rm -f db.sql
  popd > /dev/null
}
get_pre_db_checksum()
{
  local -r q_id="$1"
  [ -f "$QUERY_METRICS_DIR"/"$q_id".pre_db_checksum ] && cat "$QUERY_METRICS_DIR"/"$q_id".pre_db_checksum
}
set_pre_db_checksum()
{
  local -r q_id="$1"
  local -r db_checksum=$(get_db_checksum "$DB_DIR")
  log "  database checksum before running query: $db_checksum"
  echo "$db_checksum" > "$QUERY_METRICS_DIR"/"$q_id".pre_db_checksum
  cp -r "$DB_DIR" "$QUERY_METRICS_DIR"/"$q_id".pre_db
  sqlite3 "$QUERY_METRICS_DIR"/"$q_id".pre_db/RASBASE .dump > "$QUERY_METRICS_DIR"/"$q_id".pre_db/RASBASE.sql
}
get_post_db_checksum()
{
  local -r q_id="$1"
  [ -f "$QUERY_METRICS_DIR"/"$q_id".post_db_checksum ] && cat "$QUERY_METRICS_DIR"/"$q_id".post_db_checksum
}
set_post_db_checksum()
{
  local -r q_id="$1"
  local -r db_checksum=$(get_db_checksum "$DB_DIR")
  log "  database checksum after running query: $db_checksum"
  echo "$db_checksum" > "$QUERY_METRICS_DIR"/"$q_id".post_db_checksum
  cp -r "$DB_DIR" "$QUERY_METRICS_DIR"/"$q_id".post_db
  sqlite3 "$QUERY_METRICS_DIR"/"$q_id".post_db/RASBASE .dump > "$QUERY_METRICS_DIR"/"$q_id".post_db/RASBASE.sql
}
get_query_execution_time()
{
  local -r q_id="$1"
  [ -f "$QUERY_METRICS_DIR"/"$q_id".query_time ] && cat "$QUERY_METRICS_DIR"/"$q_id".query_time
}
set_query_execution_time()
{
  local -r q_id="$1"
  local -r q_execution_time="$2"
  log "  query execution time: $q_execution_time"
  echo "$q_execution_time" > "$QUERY_METRICS_DIR"/"$q_id".query_time
}
record_query_metrics()
{
  mkdir -p "$QUERY_METRICS_DIR"
  pushd "$QUERY_DIR" > /dev/null

  for query_file in *.sh; do
    [ -f "$QUERY_METRICS_DIR"/$query_file.query_time ] && continue

    recreate_rasbase

    . "$query_file"
    log "record metrics of query $(get_query)"

    pre_query
    set_pre_db_checksum "$query_file"

    start_timer
    run_query
    stop_timer

    set_query_execution_time "$query_file" "$(get_time)"

    restart_rasdaman
    set_post_db_checksum "$query_file"
  done

  popd > /dev/null
}

#
# get a random time to wait before killing the server. This is taken to be always
# after the first 1/3 of the measured time (so 1/3 + random(2/3)),
# as RASBASE action generally comes last in the query evaluation.
#
get_random_sleep_time()
{
  local max_time="$1"
  echo "scale=5; ((($RANDOM/32767) * ($max_time * 2 / 3)) + ($max_time / 3)) / 1000" | bc
}

get_random_query_file()
{
  local -r q_id=$(shuf -i 1-"$QUERY_COUNT" -n 1)
  ls "$QUERY_DIR"/*.sh | sed "$q_id"'q;d'
}

archive_logs()
{
  local -r q_id=$(basename "$1" | sed 's/.sh//')
  local -r q_file="$1"
  mkdir -p "$BUG_DIR"
  local log_archive_dir=$(mktemp -d "$BUG_DIR/bug.$q_id.XXXXXXX")
  cp "$q_file" "$log_archive_dir"
  cp -r "$LOG_DIR" "$log_archive_dir"
  cp -r "$DB_DIR" "$log_archive_dir"/"$q_id".sh.curr_db
  sqlite3 "$log_archive_dir"/"$q_id".sh.curr_db/RASBASE .dump > "$log_archive_dir"/"$q_id".sh.curr_db/RASBASE.sql
  cp -r "$QUERY_METRICS_DIR"/"$q_id".sh.pre_db "$log_archive_dir"
  cp -r "$QUERY_METRICS_DIR"/"$q_id".sh.post_db "$log_archive_dir"
  diff -x RASBASE -x RASBASE-journal "$log_archive_dir"/"$q_id".sh.curr_db "$log_archive_dir"/"$q_id".sh.pre_db > "$log_archive_dir"/"$q_id".sh.pre_db.diff || true
  diff -x RASBASE -x RASBASE-journal "$log_archive_dir"/"$q_id".sh.curr_db "$log_archive_dir"/"$q_id".sh.post_db > "$log_archive_dir"/"$q_id".sh.post_db.diff || true
  log "  captured logs in $log_archive_dir"
}

handle_inconsistent_db_state()
{
  local -r q_file_name="$1"
  local -r q_file="$2"
  log ""
  log "inconsistent database state while executing query $q_file_name."
  pushd "$LOG_DIR" > /dev/null
  grep -A15 "test handler caught signal SIGUSR1" *
  popd > /dev/null
  archive_logs "$q_file"
  log ""
}

_cleanup()
{
  sleep 0.5
  loge ""
  log "exiting..."
  restore_configuration
  restart_rasdaman
  rm -rf "$DB_DIR"
  log "done."
  trap - EXIT
}
trap '_cleanup' EXIT

_print_errors()
{
   local -r lineno="$1"
   local -r srcfile="$2"
   loge "$srcfile:$lineno - command returned non-zero exit code."
}
trap '_print_errors "${LINENO}" "${BASH_SOURCE}"' ERR

# ------------------------------------------------------------------------------
# start test
#

check_filestorage_dependencies
prepare_configuration 1
record_query_metrics

while [ true ]; do

  recreate_rasbase

  query_file=$(get_random_query_file)
  query_file_name=$(basename "$query_file")

  query_execution_time=$(get_query_execution_time "$query_file_name")
  query_pre_db_checksum=$(get_pre_db_checksum "$query_file_name")
  query_post_db_checksum=$(get_post_db_checksum "$query_file_name")

  random_sleep_time=$(get_random_sleep_time "$query_execution_time")
  random_sleep_time_sec=$(echo "scale=3; $random_sleep_time * 1000.0" | bc)

  log ""
  log "-------------------------------------------------------------------------------------"
  log "Testing query $query_file_name: total run time $query_execution_time ms, interrupting after $random_sleep_time_sec ms."
  log ""

  source "$query_file"
  pre_query

  pid=$(get_server_pid)

  curr_db_checksum=$(get_db_checksum "$DB_DIR")
  if [ "$curr_db_checksum" != "$query_pre_db_checksum" ]; then
    archive_logs "$QUERY_DIR/$query_file_name"
    error "current db size $curr_db_checksum doesn't match recorded pre-query db size $query_pre_db_checksum (dir $DB_DIR)."
  fi

  (run_query) &
  sleep $random_sleep_time

  [ -n "$pid" ] && kill -"$KILL_SIGNAL" "$pid"
  sleep 0.3

  restart_rasdaman
  sleep 0.3

  curr_db_checksum=$(get_db_checksum "$DB_DIR")
  if [ "$curr_db_checksum" != "$query_pre_db_checksum" -a "$curr_db_checksum" != "$query_post_db_checksum" ]; then
    handle_inconsistent_db_state "$query_file_name" "$query_file"
  fi

done
