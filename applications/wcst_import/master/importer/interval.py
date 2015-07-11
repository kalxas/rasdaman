class Interval:
    def __init__(self, low, high=None):
        """
        Class to represent an interval of values
        :param str|float|int low: the low value
        :param str|float|int high: the high value
        """
        self.low = low
        self.high = high

    def __str__(self):
        """
        Returns the string representation of the interval
        :rtype: str
        """
        if self.high is None:
            return str(self.low)
        else:
            return str(self.low) + "," + str(self.high)
