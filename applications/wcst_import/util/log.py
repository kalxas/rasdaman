import logging
import sys

"""
Configuration of the python logger
Colouring is added to display the error and status messages and a new
log level called title is introduced with a value > debug but < info
"""

SUCCESS_LEVEL = 25
logging.addLevelName(SUCCESS_LEVEL, "SUCCESS")

TITLE_LEVEL = 5
logging.addLevelName(TITLE_LEVEL, "TITLE")


def add_coloring_to_emit_ansi(fn):
    # add methods we need to the class
    def new(*args):
        levelno = args[1].levelno
        if levelno >= 50:
            color = '\x1b[31m'  # red
        elif levelno >= 40:
            color = '\x1b[31m'  # red
        elif levelno >= 30:
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


def title(self, message, *args, **kws):
    self.log(TITLE_LEVEL, message, *args, **kws)


def success(self, message, *args, **kws):
    self.log(SUCCESS_LEVEL, message, *args, **kws)


logging.Logger.title = title
logging.Logger.success = success
logging.StreamHandler.emit = add_coloring_to_emit_ansi(logging.StreamHandler.emit)
log = logging.getLogger("WCSTILogger")
log.setLevel(TITLE_LEVEL)
log.addHandler(logging.StreamHandler(sys.stdout))