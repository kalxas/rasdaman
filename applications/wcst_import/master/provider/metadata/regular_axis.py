from master.provider.metadata.axis import Axis


class RegularAxis(Axis):
    def __init__(self, label, uomLabel, low, high, origin, crs_axis):
        Axis.__init__(self, label, uomLabel, low, high, origin, crs_axis)