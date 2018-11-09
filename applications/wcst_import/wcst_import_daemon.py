import os
import sys
from util.log import log
import traceback
import time

from daemon import Daemon
import wcst_import as wi



PIDFILE = sys.argv[1] + '.wcst_import.pid'
LOGFILE = sys.argv[1] + '.wcst_import.log'


DAEMON_ACTION_ARGN = 2
WATCH_INTERVAL_ARGN = 3

class wcst_import_daemon(Daemon):

    def run(self):
        
        while True:
            # if the pid file was deleted, exit
            if not self.running():
                sys.stderr.write("Pidfile was not found\n")
                sys.exit(1)

            wi.main()
            time.sleep(SLEEP_TIME)

            


            
        


if __name__ == "__main__":

    daemon = wcst_import_daemon(PIDFILE, os.devnull, LOGFILE, LOGFILE);


    if 'start' == sys.argv[DAEMON_ACTION_ARGN]:
        SLEEP_TIME = float(sys.argv[WATCH_INTERVAL_ARGN])
        daemon.start()
       

    elif 'stop' == sys.argv[DAEMON_ACTION_ARGN]:
        print "Stopping ..."
        daemon.stop()

    elif 'restart' == sys.argv[DAEMON_ACTION_ARGN]:
        print "Restarting ..."
        SLEEP_TIME = float(sys.argv[WATCH_INTERVAL_ARGN])
        daemon.restart()

    elif 'status' == sys.argv[DAEMON_ACTION_ARGN]:
        if daemon.running():
            pid = daemon.get_pid()
            print 'wcst_import is running as pid %s' % str(pid)
        else:
            print 'wcst_import is not running.'

    else:
        print "[wcst_import:] Unknown command"
        sys.exit(2)
    
