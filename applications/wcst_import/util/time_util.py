from lib.arrow import api as arrow
from lib.arrow.parser import ParserError
from util.crs_util import CRSUtil
from master.error.runtime_exception import RuntimeException


class DateTimeUtil:

    CRS_CODE_ANSI_DATE = "AnsiDate"
    CRS_CODE_UNIX_TIME = "UnixTime"

    def __init__(self, datetime, dt_format=None, time_crs=None):
        """
        :param str datetime: the datetime value
        :param str dt_format: the datetime format, if none is given we'll try to guess
        """
        try:
            if dt_format is None:
                self.datetime = arrow.get(datetime)
            else:
                self.datetime = arrow.get(datetime, dt_format)
            if time_crs is None:
                self.time_crs_code = self.CRS_CODE_ANSI_DATE
            else:
                tmp_crs = CRSUtil(time_crs)
                self.time_crs_code = tmp_crs.get_crs_code()
        except ParserError as pe:
            dt_format_err = "auto" if dt_format is None else dt_format
            raise RuntimeException("Failed to parse the date " + datetime + " using format " + dt_format_err)

    def to_string(self):
        """
        Returns the datetime as a string, formatted depending on the time CRS code
        :return:
        """
        if self.time_crs_code == self.CRS_CODE_ANSI_DATE:
            return self.to_ansi()
        elif self.time_crs_code == self.CRS_CODE_UNIX_TIME:
            return self.to_unix()
        else:
            return self.to_unknown()

    def to_ansi(self):
        """
        Returns the datetime in iso format
        :return: str
        """
        return '"' + self.datetime.isoformat() + '"'

    def to_unix(self):
        """
        Returns the datetime in unix format
        :return: str
        """
        return str(self.datetime.timestamp)

    def to_unknown(self):
        """
        Handle unknown time CRS
        :return: throw RuntimeException
        """
        raise RuntimeException("Unsupported time CRS " + self.time_crs_code)