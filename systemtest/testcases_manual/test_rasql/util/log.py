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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""

import logging
import sys
import os

"""
Configuration of the python logger
Colouring is added to display the error and status messages and a new
log level called title is introduced with a value > debug but < info
"""

TITLE_LEVEL = 25
logging.addLevelName(TITLE_LEVEL, "TITLE")

def title(self, message, *args, **kws):
    self.log(TITLE_LEVEL, message, *args, **kws)

SUCCESS_LEVEL = 26
logging.addLevelName(SUCCESS_LEVEL, "SUCCESS")

def success(self, message, *args, **kws):
    self.log(SUCCESS_LEVEL, message, *args, **kws)

logging.Logger.title = title
logging.Logger.success = success

def add_coloring_to_emit_ansi(fn):
    # add methods we need to the class
    def new(*args):
        # then add colors if it's run interactively
        levelno = args[1].levelno
        if levelno >= logging.CRITICAL:
            color = '\x1b[31m'  # red
        elif levelno >= logging.ERROR:
            color = '\x1b[31m'  # red
        elif levelno >= logging.WARNING:
            color = '\x1b[33m'  # yellow
        elif levelno == SUCCESS_LEVEL:
            color = '\x1b[32m'  # green
        elif levelno == TITLE_LEVEL:
            color = '\033[1m'  # bold
        else:
            color = '\x1b[0m'  # normal
        args[1].msg = color + args[1].msg + '\x1b[0m'  # normal
        # print "after"
        return fn(*args)

    return new

# add coloring only if it's an interactive use, rather than redirection/piping
if os.isatty(1):
    logging.StreamHandler.emit = add_coloring_to_emit_ansi(logging.StreamHandler.emit)

# https://stackoverflow.com/a/24956305
class MaxLevelFilter(logging.Filter):
    '''Filters (lets through) all messages with level < LEVEL'''
    def __init__(self, level):
        self.level = level

    def filter(self, record):
        # "<" instead of "<=": since logger.setLevel is inclusive, this should be exclusive
        return record.levelno < self.level

# messages lower than WARNING go to stdout, lower than DEBUG are ignored
stdout_hdlr = logging.StreamHandler(sys.stdout)
stdout_hdlr.addFilter(MaxLevelFilter(logging.WARNING))
stdout_hdlr.setLevel(logging.DEBUG)

# messages >= WARNING (and >= STDOUT_LOG_LEVEL) go to stderr
stderr_hdlr = logging.StreamHandler(sys.stderr)
stderr_hdlr.setLevel(logging.WARNING)

log = logging.getLogger("rasdaman_systemtest")
log.setLevel(logging.DEBUG)
log.addHandler(stdout_hdlr)
log.addHandler(stderr_hdlr)
