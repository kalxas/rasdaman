import os
import sys
from util.log import log
import traceback
import time

from daemon import Daemon
import wcst_import as wi



PIDFILE = '/run/user/' + str(os.getuid()) + '/Wcst_Import_Daemon.pid'
LOGFILE = sys.argv[1] + '.wcst_import.log'


DAEMON_ACTION_ARGN = 2

class wcst_import_daemon(Daemon):

    def run(self):

        wi.main()
        
        while True:
            time.sleep(120)

            
        


if __name__ == "__main__":

    daemon = wcst_import_daemon(PIDFILE, os.devnull, LOGFILE, LOGFILE);


    if 'start' == sys.argv[DAEMON_ACTION_ARGN]:
        daemon.start()
       

    elif 'stop' == sys.argv[DAEMON_ACTION_ARGN]:
        print "Stopping ..."
        daemon.stop()

    elif 'restart' == sys.argv[DAEMON_ACTION_ARGN]:
        print "Restarting ..."
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
    
