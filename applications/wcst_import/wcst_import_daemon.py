"""
 *
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""

import os
import sys
import time

from daemon import Daemon
import wcst_import as wi
from ArgumentsParser import parse_arguments

DAEMON_ACTION_ARGN = 2
WATCH_INTERVAL_ARGN = 3


class WCSTImportDaemon(Daemon):

    def run(self):
        
        while True:
            # if the pid file was deleted, exit
            if not self.running():
                sys.stderr.write("Pid file was not found\n")
                sys.exit(1)
            try:
              wi.main()
            except:
              # exceptions thrown by wcst_import (e.g. no new files to import)
              # shouldn't stop the daemon
              pass
            time.sleep(SLEEP_TIME)


if __name__ == "__main__":

    arguments = parse_arguments()

    PIDFILE = arguments.ingredients_file + ".wcst_import.pid"
    LOGFILE = arguments.ingredients_file + ".wcst_import.log"

    daemon_action = arguments.daemon
    daemon_watch = arguments.watch

    daemon = WCSTImportDaemon(PIDFILE, os.devnull, LOGFILE, LOGFILE);

    if "start" == daemon_action:
        print "Starting wcst_import daemon ..."
        SLEEP_TIME = daemon_watch
        daemon.start()
    elif "stop" == daemon_action:
        print "Stopping wcst_import daemon ..."
        daemon.stop()
    elif "restart" == daemon_action:
        print "Restarting  wcst_import daemon ..."
        SLEEP_TIME = daemon_watch
        daemon.restart()
    elif "status" == daemon_action:
        if daemon.running():
            pid = daemon.get_pid()
            print "wcst_import daemon is running as pid {}".format(pid)
        else:
            print "wcst_import daemon is not running."
    else:
        print "Unknown daemon action {}".format(daemon_action)
        sys.exit(2)

