from util.crs_util import CRSAxis


class Axis:
    def __init__(self, label, uomLabel, low, high, origin, crs_axis):
        """
        Class to represent one gml axis
        :param label: the label of the axis
        :param uomLabel: the uom of the axis
        :param low: the bbox low
        :param high: the bbox high
        :param origin: the origin of the axis
        :param CRSAxis crs_axis: the crs axis definition for the axis corresponding to this subset. As some axis might
        be created otherwise than through a crs_axis this parameter is optional and should be checked before being
        used in other classes
        """
        self.label = label
        self.uomLabel = uomLabel
        self.low = low
        self.high = high
        self.origin = origin
        # TODO: Handle the coefficient on irregular axis in some other way
        self.coefficient = None
        self.crs_axis = crs_axis
