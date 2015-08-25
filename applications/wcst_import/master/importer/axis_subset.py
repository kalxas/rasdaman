from master.provider.metadata.coverage_axis import CoverageAxis
from master.importer.interval import Interval


class AxisSubset:
    def __init__(self, coverage_axis, interval):
        """
        A representation of a subset on one axis
        :param CoverageAxis coverage_axis: the definition of the axis on which the subset is being done
        :param Interval interval: the interval of the subset
        """
        self.coverage_axis = coverage_axis
        self.interval = interval

    def __str__(self):
        return self.coverage_axis.axis.label + "(" + str(self.interval) + ")"