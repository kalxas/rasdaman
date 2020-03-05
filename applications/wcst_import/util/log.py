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
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""

import logging
import sys
import os
from logging import Filter

from util.time_util import DateTimeUtil

"""
Configuration of the python logger
Colouring is added to display the error and status messages and a new
log level called title is introduced with a value > debug but < info
"""

TITLE_LEVEL = 5
logging.addLevelName(TITLE_LEVEL, "TITLE")

def title(self, message, *args, **kws):
    self.log(TITLE_LEVEL, message, *args, **kws)

SUCCESS_LEVEL = 15
logging.addLevelName(SUCCESS_LEVEL, "SUCCESS")

def success(self, message, *args, **kws):
    self.log(SUCCESS_LEVEL, message, *args, **kws)

logging.Logger.title = title
logging.Logger.success = success


def add_coloring_to_emit_ansi(fn):
    # add methods we need to the class
    def new(*args):
        # add colors
        levelno = args[1].levelno
        if levelno >= logging.CRITICAL:
            color = '\033[1m\x1b[31m'  # red
        elif levelno >= logging.ERROR:
            color = '\033[1m\x1b[31m'  # red
        elif levelno >= logging.WARNING:
            color = '\033[1m\x1b[33m'  # yellow
        elif levelno == SUCCESS_LEVEL:
            color = '\033[1m\x1b[32m'  # green
        elif levelno == TITLE_LEVEL:
            color = '\033[1m'          # bold
        else:
            color = '\x1b[0m'          # normal
        args[1].msg = color + args[1].msg + '\x1b[0m'  # normal
        # print "after"
        return fn(*args)

    return new

# add coloring only if it's an interactive use, rather than redirection/piping
if os.isatty(1):
    logging.StreamHandler.emit = add_coloring_to_emit_ansi(logging.StreamHandler.emit)

log = logging.getLogger()

class SingleLevelFilter(logging.Filter):
    def __init__(self, passlevel, reject):
        self.passlevel = passlevel
        self.reject = reject

    def filter(self, record):
        if self.reject:
            return (record.levelno > self.passlevel)
        else:
            return (record.levelno <= self.passlevel)

# Default logging level
log.setLevel(logging.DEBUG)

# Lower than level info (called by method of log (e.g: log.debug()) in other classes) go to stdout
handler1 = logging.StreamHandler(sys.stdout)
filter1 = SingleLevelFilter(logging.INFO, False)
handler1.addFilter(filter1)
log.addHandler(handler1)

# Higher than info (called by method of log (e.g: log.error()) in other classes) go to stderr
handler2 = logging.StreamHandler(sys.stderr)
filter2 = SingleLevelFilter(logging.INFO, True)
handler2.addFilter(filter2)
log.addHandler(handler2)


def make_bold(input_text):
    """
    Create a bold text from a normal text.
    :param str input_text: normal text.
    """
    if os.isatty(1):
        return "\033[1m" + input_text + "\033[0;0m"
    else:
        return input_text


def prepend_time(input_text):
    """
    Make input text with also newline character and datetime stamp as prefix.
    :param str input_text: input text to be logged
    """
    now = DateTimeUtil.get_now_datetime_stamp()
    result = "\n[{}] {}".format(now, input_text)

    return result


