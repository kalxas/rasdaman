import functools


@functools.total_ordering
class TimeGdalTuple:
    def __init__(self, time, filepath):
        """
        This class is a representation of a tuple containing a date time value and a filepath
        :param util.time_util.DateTimeUtil time: the time of the record
        :param str filepath: the filepath to the gdal dataset
        """
        self.time = time
        self.filepath = filepath

    def __eq__(self, other):
        """
        Compares two tuples ==
        :param TimeGdalTuple other: the tuple to compare with
        :rtype bool
        """
        return self.time.datetime == other.time.datetime

    def __lt__(self, other):
        """
        Compares two tuples <
        :param TimeGdalTuple other: the tuple to compare with
        :rtype bool
        """
        return self.time.datetime < other.time.datetime