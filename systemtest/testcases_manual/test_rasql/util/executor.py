#!/usr/bin/env python2
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

import os
import subprocess
import time
from log import log
from threading import Timer

def kill_proc(proc, timed_out):
    timed_out["value"] = True
    proc.kill()

def execute(command, working_dir=None, stdin=None, env=None,
            throw_on_error=False, shell=False, timeout_sec=-1):
    """
    Executes the command in the given working directory (or cwd by default).

    :param list[str] command: the command as a list of words, e.g.
     "make install" => ["make", "install"]
    :param str working_dir: the path to the working dir, defaults to cwd
    :param str stdin: a string to be given as the stdin input
    :param dict env: a environment dict as returned by os.environ.copy()
    :param bool throw_on_error: set to false to prevent the executor from
     throwing exceptions when the return code is different than 0
    :param bool shell: to run the command through a shell
    :param int timeout_sec: timeout in seconds before terminating the command
    :return: returns stdout, stderr, exit code
    :rtype: (str, str, int)
    """
    start = time.time()
    if command is None or command == []:
        return "", "", 0
    stdout, stderr, return_code = "", "", 0
    if working_dir is None:
        working_dir = os.getcwd()
    cmd_string = " ".join(command) if type(command) == list else command
    try:
        proc = subprocess.Popen(command,
                                cwd=working_dir,
                                stdin=subprocess.PIPE,
                                stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE,
                                env=env,
                                shell=shell)

        # execute command, with or without a timeout
        if timeout_sec > 0:
            # will be set to True if the process timed out
            timed_out = {"value": False}
            timer = Timer(timeout_sec, kill_proc, [proc, timed_out])
            try:
                # start a timer that will kill the process if it isn't
                # cancelled below before the process returns
                timer.start()
                stdout, stderr = proc.communicate(input=stdin)
            finally:
                timer.cancel()

            if timed_out["value"]:
                stderr += "\nCommand execution timed out after " + \
                          str(timeout_sec) + " seconds."
            else:
                return_code = proc.returncode
        else:
            stdout, stderr = proc.communicate(input=stdin)
            return_code = proc.returncode

    except Exception:
        log.error("""Failed execution
  command: {}
  stdout:  {}
  stderr:  {}
  return:  {}""".format(cmd_string, stdout, stderr, return_code))
    finally:
        log.debug("Executed command in {}ms: {}".format(time.time() - start, cmd_string))

    return stdout, stderr, return_code
