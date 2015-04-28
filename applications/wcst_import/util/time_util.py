from lib.arrow import api as arrow
from lib.arrow.parser import ParserError
from recipes.shared.runtime_exception import RuntimeException


class DateTimeUtil:
    def __init__(self, datetime, dt_format=None):
        """
        :param str datetime: the datetime value
        :param str dt_format: the datetime format, if none is given we'll try to guess
        """
        try:
            if dt_format is None:
                self.datetime = arrow.get(datetime)
            else:
                self.datetime = arrow.get(datetime, dt_format)
        except ParserError as pe:
            dt_format_err = "auto" if dt_format is None else dt_format
            raise RuntimeException("Failed to parse the date " + datetime + " using format " + dt_format_err)

    def to_ansi(self):
        """
        Returns the datetime in iso format
        :return:
        """
        return self.datetime.isoformat()