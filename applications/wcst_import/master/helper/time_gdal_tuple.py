import functools
from util.file_obj import File


@functools.total_ordering
class TimeFileTuple:
    def __init__(self, time, file):
        """
        This class is a representation of a tuple containing a date time value and a file
        :param util.time_util.DateTimeUtil time: the time of the record
        :param File file: the file associated with this tuple
        """
        self.time = time
        self.file = file

    def __eq__(self, other):
        """
        Compares two tuples ==
        :param TimeFileTuple other: the tuple to compare with
        :rtype bool
        """
        return self.time.datetime == other.time.datetime

    def __lt__(self, other):
        """
        Compares two tuples <
        :param TimeFileTuple other: the tuple to compare with
        :rtype bool
        """
        return self.time.datetime < other.time.datetime