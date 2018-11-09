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

sed "s@PETASCOPE_URL@$PETASCOPE_URL@g" "$ingredients_file_testdata" > "$SCRIPT_DIR/ingest.json"

# array for holding posible daemon commands
# in case the daemon's functionality is extended, add the new commands at the end of the array
declare -a daemon_commands=("wcst_import.sh "$SCRIPT_DIR/ingest.json" --daemon start" \
	"wcst_import.sh "$SCRIPT_DIR/ingest.json" --daemon status" \
	"wcst_import.sh "$SCRIPT_DIR/ingest.json" --daemon restart" \
	"wcst_import.sh "$SCRIPT_DIR/ingest.json" --daemon stop" \
	"wcst_import.sh "$SCRIPT_DIR/ingest.json" --watch 0.5")


check_start() {
	daemon_pid=$(ps aux | grep -E -v "grep" | grep "$SCRIPT_DIR/ingest.json")

	logn "Checking --daemon start ... "

	if [ "$daemon_pid" > /dev/null ]; then
		check_passed
	else
		check_failed
	fi

}

check_status() {
	daemon_pid=$(ps aux | grep -E -v "grep" | grep "$SCRIPT_DIR/ingest.json" | tr -s " " | cut -d " " -f2)
	message=$"$1"

	logn "Checking --daemon status ... "

	if [ "$daemon_pid" == "${message//[^0-9]/}" ]; then
		check_passed
	else
		check_failed
	fi

}

check_restart() {
	old_pid=$"$1"
	new_pid=$(ps aux | grep -E -v "grep" | grep "$SCRIPT_DIR/ingest.json" | tr -s " " | cut -d " " -f2)

	logn "Checking --daemon restart ... "

	if ! [ "$old_pid" == "$new_pid" ]; then
		check_passed
	else
		check_failed
	fi
}

check_stop() {
	daemon_pid=$(ps aux | grep -E -v "grep" | grep "$SCRIPT_DIR/ingest.json")

	logn "Checking --daemon stop ... "

	if ! [ "$daemon_pid" > /dev/null ];then 
		check_passed
	else
		check_failed
	fi
}

# check if --watch is looking for existence of pidfile each [interval] seconds
check_watch_pid() {
	daemon_pid=$(ps aux | grep -E -v "grep" | grep "$SCRIPT_DIR/ingest.json")

	logn "Checking --watch [interval] ... "

	if [ "$daemon_pid" > /dev/null ]; then
		rm "$SCRIPT_DIR/ingest.json.wcst_import.pid"
		sleep 1

		daemon_pid=$(ps aux | grep -E -v "grep" | grep "$SCRIPT_DIR/ingest.json")
		if ! [ "$daemon_pid" > /dev/null ];then 
			check_passed
		else
			check_failed
		fi
	else
		check_failed
	fi

	# stop the daemon
	log_message=`${daemon_commands[3]} > /dev/null 2>&1`
}

# check if --watch is looking for new data each [interval] seconds
check_watch_data() {
	logn "Checking --watch [interval] ... "

	log_message=`${daemon_commands[4]}`

	first_modified=$(stat -c "%Y" $SCRIPT_DIR/ingest.json.wcst_import.log)
	sleep 1
	last_modified=$(stat -c "%Y" $SCRIPT_DIR/ingest.json.wcst_import.log)

	if [ "$first_modified" != "$last_modified" ]; then
		check_passed
	else
		check_failed
	fi

	# stop the daemon
	log_message=`${daemon_commands[3]} > /dev/null 2>&1`

}



test_case_1() {
	loge "\nRunning: Test_case_1 ...\n\n"

	log_message=`${daemon_commands[0]} > /dev/null 2>&1`

	# kill the process of the daemon manually and check for correct behaviour afterwards
	daemon_pid=$(ps aux | grep -E -v "grep" | grep "$SCRIPT_DIR/ingest.json" | tr -s " " | cut -d " " -f2)
	kill "$daemon_pid"
	status_message=`${daemon_commands[1]}`
	check_status "$status_message"

	`${daemon_commands[0]} > /dev/null 2>&1`
	check_start
	
	# stop the daemon
	`${daemon_commands[3]} > /dev/null 2>&1`

}

# set the daemon to look for pidfile and newdata every 1 seconds
# remove the pidfile
# check if the daemon correctly exists
test_case_2() {
	loge "\nRunning: Test_case_2 ...\n\n"

	`${daemon_commands[4]} > /dev/null 2>&1`
	rm "$SCRIPT_DIR/ingest.json.wcst_import.pid"
	sleep 1

	check_stop
}


for (( i = 0; i < ${#daemon_commands[@]} ; i++ )); do
	loge "\nRunning: ${daemon_commands[$i]} ...\n\n"

	current_pid=$(ps aux | grep -E -v "grep" | grep "$SCRIPT_DIR/ingest.json" | tr -s " " | cut -d " " -f2)
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
		loge "$log_message"
	fi
done

test_case_1
test_case_2

print_summary
