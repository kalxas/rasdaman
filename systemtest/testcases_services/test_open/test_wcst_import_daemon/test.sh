#!/bin/bash
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.    If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#    test.sh
# Description
#    Checks wcst_import.sh daemon usage
#
################################################################################

PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done

SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../../util/common.sh

ingredients_file_testdata="$SCRIPT_DIR/../../test_all_wcst_import/testdata/wcs_import_order_descending_test_import_order_descending_irregular_time_netcdf/ingest.template.json"

relpath() { 
    python -c "import os.path; print os.path.relpath('$1','${2:-$PWD}')"
}
ingest_json=$(relpath "$SCRIPT_DIR/ingest.json")

sed "s@PETASCOPE_URL@$PETASCOPE_URL@g" "$ingredients_file_testdata" > "$ingest_json"

# array for holding posible daemon commands
# in case the daemon's functionality is extended, add the new commands at the end of the array
declare -a daemon_commands=( \
    "wcst_import.sh -i $RASADMIN_CREDENTIALS_FILE $ingest_json --daemon start" \
    "wcst_import.sh -i $RASADMIN_CREDENTIALS_FILE $ingest_json --daemon status" \
    "wcst_import.sh -i $RASADMIN_CREDENTIALS_FILE $ingest_json --daemon restart" \
    "wcst_import.sh -i $RASADMIN_CREDENTIALS_FILE $ingest_json --daemon stop" \
    "wcst_import.sh -i $RASADMIN_CREDENTIALS_FILE $ingest_json --watch 0.5")

get_daemon_pid() {
    ps aux | grep "$ingest_json" | grep -v "grep" | tr -s " " | cut -d " " -f2
}

stop_daemon() {
    log_message=`${daemon_commands[3]} > /dev/null 2>&1`
}
start_daemon() {
    log_message=`${daemon_commands[0]} > /dev/null 2>&1`
}
status_daemon() {
    ${daemon_commands[1]}
}
watch_daemon_0_5s() {
    log_message=`${daemon_commands[4]}`
}

check_start() {
    daemon_pid=$(get_daemon_pid)
    [ -n "$daemon_pid" ]
    check_result 0 $? "checking --daemon start"
}
check_status() {
    daemon_pid=$(get_daemon_pid)
    message=$"$1"
    check_result "$daemon_pid" "${message//[^0-9]/}" "checking --daemon status"
}
check_restart() {
    old_pid=$"$1"
    new_pid=$(get_daemon_pid)
    [ "$old_pid" != "$new_pid" ]
    check_result 0 $? "checking --daemon restart"
}
check_stop() {
    daemon_pid=$(get_daemon_pid)
    [ -z "$daemon_pid" ]
    check_result 0 $? "checking --daemon stop"
}

# check if --watch is looking for existence of pidfile each [interval] seconds
check_watch_pid() {
    check_start
    rm -f "$ingest_json.wcst_import.pid"
    sleep 2

    [ -z "$(get_daemon_pid)" ]
    check_result 0 $? "checking pidfile removal during --watch [interval]"
    stop_daemon
}

# check if --watch is looking for new data each [interval] seconds
check_watch_data() {
    watch_daemon_0_5s
    first_modified=$(stat -c "%Y" $ingest_json.wcst_import.log)
    sleep 2
    last_modified=$(stat -c "%Y" $ingest_json.wcst_import.log)

    [ "$first_modified" != "$last_modified" ]
    check_result 0 $? "checking data access during --watch [interval]"
    stop_daemon
}

#
# actual tests
#

check_daemon_status_on_manuall_kill() {
    log "check daemon status on manual daemon kill..."

    start_daemon
    sleep 1

    # kill the process of the daemon manually and check for correct behaviour afterwards
    daemon_pid=$(get_daemon_pid)
    for pid in $daemon_pid; do
        kill "$pid"
    done
    check_status "$(status_daemon)"

    start_daemon
    check_start
    stop_daemon
    check_stop
}

# set the daemon to look for pidfile and newdata every 1 seconds
# remove the pidfile
# check if the daemon was stopped
check_pidfile_removed() {
    log "check that daemon stops when pid file is removed..."

    watch_daemon_0_5s
    rm -f "$ingest_json.wcst_import.pid"
    # daemon will stop when pid file is removed, but wait more than 0.5 seconds to make sure it really stopped
    sleep 2

    check_stop
}


for (( i = 0; i < ${#daemon_commands[@]} ; i++ )); do
    log ""
    log "test ${daemon_commands[$i]}"

    current_pid=$(get_daemon_pid)
    log_message=`${daemon_commands[$i]}`

    case "$i" in
        0)
            check_start
            ;;
        1)
            check_status "$log_message"
            ;;
        2) 
            check_restart "$current_pid"
            ;;
        3)
            check_stop
            ;;
        4)
            check_watch_pid
            check_watch_data
            ;;
        *)
    esac

    if [ -n "$log_message" ]; then
        log "wcst_import.sh output: $log_message"
    fi
done

log ""
check_daemon_status_on_manuall_kill

log ""
check_pidfile_removed

print_summary
exit $RC
