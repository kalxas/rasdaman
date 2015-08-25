from master.provider.metadata.axis import Axis
from util.crs_util import CRSAxis


class IrregularAxis(Axis):
    def __init__(self, label, uomLabel, low, high, origin, coefficients, crs_axis):
        """
        An irregular axis is defined by its crs axis and a list of coefficients
        :param str label: the label of the axis
        :param str uomLabel: the unit of measure
        :param str | float | int low: the low value on the axis bounding interval
        :param str | float | int high: the high value on the axis bounding interval
        :param str | float | int origin: the origin on the axis
        :param list[str | float | int] coefficients: a list of coefficients describing the points
        :param CRSAxis crs_axis: the corresponding virtual crs axis of which this axis is an instance of
        :return:
        """
        Axis.__init__(self, label, uomLabel, low, high, origin, crs_axis)
        self.coefficient = coefficients